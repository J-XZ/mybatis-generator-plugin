package space.jxz.mybatis.generator;

import org.mybatis.generator.api.ConnectionFactory;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.JDBCConnectionFactory;
import org.mybatis.generator.internal.ObjectFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Create by jxz
 * @Date 2018/5/16
 */
public class Utils {
    static Connection getConnection(Context context) {
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

    /**
     * key tableName
     */
    private static Map<String, Map<String, Set<String>>> getUniqueKeysCache = new ConcurrentHashMap<>();

    /**
     * @return key:index name, value:set of column name
     */
    static Map<String, Set<String>> getUniqueKeys(Context context, IntrospectedTable introspectedTable) {
        if (getUniqueKeysCache.containsKey(introspectedTable.getFullyQualifiedTableNameAtRuntime())) {
            return getUniqueKeysCache.get(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        }

        Map<String, Set<String>> map = new HashMap<>();
        try (Connection connection = getConnection(context);
             ResultSet rs = connection.getMetaData().getIndexInfo(
                     introspectedTable.getTableConfiguration().getCatalog(),
                     introspectedTable.getTableConfiguration().getSchema(),
                     introspectedTable.getFullyQualifiedTableNameAtRuntime(),
                     true, false);) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                Set<String> set = map.get(indexName);
                if (set==null){
                    set = new HashSet<>();
                    map.put(indexName, set);
                }
                set.add(columnName);
            }
            getUniqueKeysCache.put(introspectedTable.getFullyQualifiedTableNameAtRuntime(), map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取索引信息失败");
        }
    }

    static Set<String> getPrimaryKeyColumnNames(IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        Set<String> pkNames = new HashSet<>();
        for (IntrospectedColumn primaryKeyColumn : primaryKeyColumns) {
            pkNames.add(primaryKeyColumn.getActualColumnName());
        }
        return pkNames;
    }

    /**
     * This is not works for mysql:
     * 1 introspectedTable.getRemarks()
     * 2 connection.getMetaData().getTables(..).getString("REMARKS")
     */
    static String getTableComments(Context context, IntrospectedTable introspectedTable) {
        try (Connection connection = getConnection(context);
             Statement statement = connection.prepareStatement(String.format(
                     "select TABLE_COMMENT from information_schema.tables " +
                             "where table_schema='%s' and table_name = '%s'",
                     connection.getCatalog(), introspectedTable.getFullyQualifiedTableNameAtRuntime()));
             ResultSet rs = ((PreparedStatement) statement).executeQuery()) {
            rs.next();
            return rs.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取表注释失败");
        }
    }


}
