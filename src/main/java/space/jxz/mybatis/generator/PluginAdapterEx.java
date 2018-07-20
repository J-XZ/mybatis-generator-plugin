package space.jxz.mybatis.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.config.Context;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Create by jxz
 * @Date 2018/7/20
 */
public abstract class PluginAdapterEx extends PluginAdapter {

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        beforeGenerate();
    }

    /**
     * 此时还未开始遍历Context#introspectedTables, 可以修改introspectedTables列表
     */
    public void beforeGenerate() {

    }


    public Connection getConnection() {
        Context context = getContext();
        Connection connection = null;
        try {
            java.lang.reflect.Method method = context.getClass().getDeclaredMethod("getConnection");
            method.setAccessible(true);
            connection = (Connection) method.invoke(context);
            if (connection.isClosed()) { // isClosed() throws SQLException
                connection = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            connection = null;
        }

        if (connection == null) {
            throw new RuntimeException("获取连接失败，可能此插件不支持当前mybatis generator版本");
        }
        return connection;
    }

    @SuppressWarnings("unchecked")
    public List<IntrospectedTable> getIntrospectTables() {
        Context context = getContext();
        try {
            Field field = context.getClass().getDeclaredField("introspectedTables");
            field.setAccessible(true);
            return (List<IntrospectedTable>) field.get(context);
        } catch (Exception e) {
            throw new RuntimeException("getIntrospectTables failed", e);
        }
    }

    /**
     * 多个table configration之间可能有重复的表 (比如使用了通配符的时候), 此方法去除重复的表,
     * 只保留同名表多个配置中的第一个配置, 即关联到此表的<table>..</table>配置中最上面的生效
     */
    public void retainFirstIntrospectedTable() {
        List<IntrospectedTable> list = getIntrospectTables();
        Set<String> names = new HashSet<>();
        ListIterator<IntrospectedTable> iterator = list.listIterator();
        while (iterator.hasNext()) {
            IntrospectedTable next = iterator.next();
            String name = next.getFullyQualifiedTable().getFullyQualifiedTableNameAtRuntime();
            if (names.contains(name)) {
                iterator.remove();
            } else {
                names.add(name);
            }
        }
    }



    /**
     * key tableName
     */
    private Map<String, Map<String, Set<String>>> getUniqueKeysCache = new ConcurrentHashMap<>();

    /**
     * @return key:index name, value:set of column name
     */
    public Map<String, Set<String>> getUniqueKeys(IntrospectedTable introspectedTable) {
        return getUniqueKeysCache.computeIfAbsent(introspectedTable.getFullyQualifiedTableNameAtRuntime(), (key) -> {

            Map<String, Set<String>> map = new HashMap<>();
            try (Connection connection = getConnection();
                 ResultSet rs = connection.getMetaData().getIndexInfo(
                         introspectedTable.getTableConfiguration().getCatalog(),
                         introspectedTable.getTableConfiguration().getSchema(),
                         introspectedTable.getFullyQualifiedTableNameAtRuntime(),
                         true, false);) {
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    String columnName = rs.getString("COLUMN_NAME");
                    Set<String> set = map.computeIfAbsent(indexName, k -> new HashSet<>());
                    set.add(columnName);
                }
                return map;
            } catch (Exception e) {
                throw new RuntimeException("获取索引信息失败", e);
            }

        });
    }


    /**
     * 获取自增列名, 不存在自增列时返回null
     * @param introspectedTable
     * @return
     */
    public String getAutoIncrementColumnName(IntrospectedTable introspectedTable) {
        try (Connection connection = getConnection();
             Statement statement = connection.prepareStatement(
                     "SHOW COLUMNS FROM " + introspectedTable.getFullyQualifiedTable());
             ResultSet rs = ((PreparedStatement) statement).executeQuery()) {

            while (rs.next()) {
                String extra = rs.getString("Extra");
                if (extra.contains("auto_increment")) {
                    return rs.getString("Field");
                }
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException("获取表注释失败", e);
        }
    }


    public Set<String> getPrimaryKeyColumnNames(IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        Set<String> pkNames = new HashSet<>();
        for (IntrospectedColumn primaryKeyColumn : primaryKeyColumns) {
            pkNames.add(primaryKeyColumn.getActualColumnName());
        }
        return pkNames;
    }

    /**
     * mysql驱动不能使用如下两种方法获取注释
     * 1 introspectedTable.getRemarks()
     * 2 connection.getMetaData().getTables(..).getString("REMARKS")
     */
    public String getTableComment(IntrospectedTable introspectedTable) {
        try (Connection connection = getConnection();
             Statement statement = connection.prepareStatement(String.format(
                     "select TABLE_COMMENT from information_schema.tables " +
                             "where table_schema='%s' and table_name = '%s'",
                     connection.getCatalog(), introspectedTable.getFullyQualifiedTableNameAtRuntime()));
             ResultSet rs = ((PreparedStatement) statement).executeQuery()) {
            rs.next();
            return rs.getString(1);
        } catch (Exception e) {
            throw new RuntimeException("获取表注释失败", e);
        }
    }



}
