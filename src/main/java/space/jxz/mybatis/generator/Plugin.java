package space.jxz.mybatis.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.GeneratedKey;

import java.util.*;

public class Plugin extends PluginAdapter {
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        commentClass(topLevelClass, introspectedTable);
        return true;
    }

    // make all fields public
    @Override
    public boolean modelFieldGenerated(
            Field field,
            TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable,
            ModelClassType modelClassType) {
        field.setVisibility(JavaVisibility.PUBLIC);
        commentField(field, introspectedTable, introspectedColumn);
        return true;
    }

    // remove evil getters
    @Override
    public boolean modelGetterMethodGenerated(
            Method method,
            TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable,
            ModelClassType modelClassType) {
        return false;
    }

    // remove evil setters
    @Override
    public boolean modelSetterMethodGenerated(
            Method method,
            TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable,
            ModelClassType modelClassType) {
        return false;
    }

    // add limit-and-offset-related fields and methods into example classes
    @Override
    public boolean modelExampleClassGenerated(
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Field limit = new Field();
        limit.setName("limit");
        limit.setVisibility(JavaVisibility.PROTECTED);
        limit.setType(FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper());
        topLevelClass.addField(limit);
        context.getCommentGenerator().addFieldComment(limit, introspectedTable);

        Method getLimit = new Method();
        getLimit.setName("getLimit");
        getLimit.setVisibility(JavaVisibility.PUBLIC);
        getLimit.setReturnType(FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper());
        getLimit.addBodyLine("return limit;");
        topLevelClass.addMethod(getLimit);
        context.getCommentGenerator().addGeneralMethodComment(getLimit, introspectedTable);

        Method setLimit = new Method();
        setLimit.setName("setLimit");
        setLimit.setVisibility(JavaVisibility.PUBLIC);
        setLimit.addParameter(
                new Parameter(FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper(), "limit"));
        setLimit.addBodyLine("this.limit = limit;");
        topLevelClass.addMethod(setLimit);
        context.getCommentGenerator().addGeneralMethodComment(setLimit, introspectedTable);

        Field offset = new Field();
        offset.setName("offset");
        offset.setVisibility(JavaVisibility.PROTECTED);
        offset.setType(FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper());
        topLevelClass.addField(offset);
        context.getCommentGenerator().addFieldComment(offset, introspectedTable);

        Method getOffset = new Method();
        getOffset.setName("getOffset");
        getOffset.setVisibility(JavaVisibility.PUBLIC);
        getOffset.setReturnType(FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper());
        getOffset.addBodyLine("return offset;");
        topLevelClass.addMethod(getOffset);
        context.getCommentGenerator().addGeneralMethodComment(getOffset, introspectedTable);

        Method setOffset = new Method();
        setOffset.setName("setOffset");
        setOffset.setVisibility(JavaVisibility.PUBLIC);
        setOffset.addParameter(
                new Parameter(FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper(), "offset"));
        setOffset.addBodyLine("this.offset = offset;");
        topLevelClass.addMethod(setOffset);
        context.getCommentGenerator().addGeneralMethodComment(setOffset, introspectedTable);

        return true;
    }

    // add limit-and-offset-related sql part into selectByExample()
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
        element.addElement(getLimitOffsetClauseElement(null));
        return true;
    }


    private boolean isGenarateComment(){
        String value = context.getCommentGeneratorConfiguration().getProperties().getProperty("suppressAllComments");
        return value==null || !value.equalsIgnoreCase("true");
    }

    /**
     * 添加注释
     */
    private void commentField(JavaElement element, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        if (!isGenarateComment()){
            return;
        }

        element.getJavaDocLines().clear();
        element.addJavaDocLine("/**");
        element.addJavaDocLine(" * This field was generated by MyBatis Generator.");
        element.addJavaDocLine(" * ");
        String remark = introspectedColumn.getRemarks();
        if (remark != null && remark.length() > 1) {
            StringBuilder sb = new StringBuilder(remark);
            while (sb.length() > 0) {
                int end = Math.min(80, sb.length());
                String line = sb.substring(0, end);
                sb.delete(0, end);
                element.addJavaDocLine(" * " + line);
            }
            element.addJavaDocLine(" *");
        }
        element.addJavaDocLine(" * Table:     " + introspectedTable.getFullyQualifiedTable());
        element.addJavaDocLine(" * Column:    " + introspectedColumn.getActualColumnName());
        element.addJavaDocLine(" * Nullable:  " + introspectedColumn.isNullable());
        element.addJavaDocLine(" */");
    }
    /**
     * 添加注释
     */
    private void commentClass(JavaElement element, IntrospectedTable introspectedTable) {
        if (!isGenarateComment()){
            return;
        }

        element.getJavaDocLines().clear();
        element.addJavaDocLine("/**");
        element.addJavaDocLine(" * This class was generated by MyBatis Generator.");
        element.addJavaDocLine(" * Any modify will be lose after regenerate!");
        element.addJavaDocLine(" * ");
//        String remark = introspectedTable.getRemarks();
        String remark = Utils.getTableComments(context, introspectedTable);
        if (remark != null && remark.length() > 1) {
            StringBuilder sb = new StringBuilder(remark);
            while (sb.length() > 0) {
                int end = Math.min(80, sb.length());
                String line = sb.substring(0, end);
                sb.delete(0, end);
                element.addJavaDocLine(" * " + line);
            }
            element.addJavaDocLine(" *");
        }
        element.addJavaDocLine(" * Table: " + introspectedTable.getFullyQualifiedTable());
        element.addJavaDocLine(" */");
    }

    // add new methods into mapper interfaces
    @Override
    public boolean clientGenerated(
            Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (Utils.getUniqueKeys(context, introspectedTable).size() > 1) {
            System.out.println(String.format("INFO : 表 %s 有多于一个UniqueKey,"
                    + "不会生成insertOrUpdateByUniqueKey()和insertSelectiveOrUpdateByUniqueKeySelective()方法",
                    introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        } else {
            addInsertOrUpdateByUniqueKeyMethod(interfaze, introspectedTable);
            addInsertSelectiveOrUpdateByUniqueKeySelectiveMethod(interfaze, introspectedTable);
        }
//        addInsertOrUpdateManuallyMethod(interfaze, introspectedTable);
//        addInsertSelectiveOrUpdateManuallyMethod(interfaze, introspectedTable);
        addSelectManuallyByExampleMethod(interfaze, introspectedTable);
        addSelectManuallyByPrimaryKeyMethod(interfaze, introspectedTable);
        addUpdateManuallyByExampleMethod(interfaze, introspectedTable);
        addUpdateManuallyByPrimaryKeyMethod(interfaze, introspectedTable);

        return true;
    }

    // add new elements into mapper XMLs
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        XmlElement root = document.getRootElement();
        if (Utils.getUniqueKeys(context, introspectedTable).size() > 1) {
            System.out.println(String.format("INFO : 表 %s 有多于一个UniqueKey,"
                            + "不会生成insertOrUpdateByUniqueKey()和insertSelectiveOrUpdateByUniqueKeySelective()方法",
                    introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        } else {
            addInsertOrUpdateByUniqueKeyElement(root, introspectedTable);
            addInsertSelectiveOrUpdateByUniqueKeySelectiveElement(root, introspectedTable);
        }

//        addInsertOrUpdateManuallyElement(root, introspectedTable);
//        addInsertSelectiveOrUpdateManuallyElement(root, introspectedTable);
        addSelectManuallyByExampleElement(root, introspectedTable);
        addSelectManuallyByPrimaryKeyElement(root, introspectedTable);
        addUpdateManuallyByExampleElement(root, introspectedTable);
        addUpdateManuallyByPrimaryKeyElement(root, introspectedTable);
        return true;
    }


    // add insertOrUpdateByUniqueKey() method
    private void addInsertOrUpdateByUniqueKeyMethod(Interface interfaze, IntrospectedTable introspectedTable) {
        Method method = new Method();
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        method.setName("insertOrUpdateByUniqueKey");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());

        Parameter record = new Parameter(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "record");
        record.addAnnotation("@Param(\"record\")");
        method.addParameter(record);

        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        interfaze.addMethod(method);
    }

    // add XML element for insertOrUpdateByUniqueKey()
    private void addInsertOrUpdateByUniqueKeyElement(XmlElement parent, IntrospectedTable introspectedTable) {
        XmlElement element = new XmlElement("insert");
        context.getCommentGenerator().addComment(element);

        element.addAttribute(new Attribute("id", "insertOrUpdateByUniqueKey"));
        element.addAttribute(new Attribute("parameterType", "map"));

        GeneratedKey gk = introspectedTable.getGeneratedKey();
        String gkColumnName = "";
        String updateGK = "";
        if (gk != null) {
            IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
            if (introspectedColumn != null) {
                gkColumnName = introspectedColumn.getActualColumnName();
                if (gk.isJdbcStandard()) {
                    element.addAttribute(new Attribute("useGeneratedKeys", "true"));
                    element.addAttribute(new Attribute("keyProperty", "record." + introspectedColumn.getJavaProperty()));
                    element.addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName()));
                } else {
                    element.addElement(getSelectKeyElement(introspectedColumn, gk));
                }
                StringBuilder sb = new StringBuilder();
                OutputUtilities.xmlIndent(sb, 1);
                updateGK = sb.append(String.format("%s = last_insert_id(%s),",
                        introspectedColumn.getActualColumnName(), introspectedColumn.getActualColumnName())).toString();
            }
        }
        StringBuilder insertClause = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();
        StringBuilder updateClause = new StringBuilder();
        StringBuilder doNothingUpdateClause = new StringBuilder();
        insertClause.append("insert into ")
                .append(introspectedTable.getFullyQualifiedTableNameAtRuntime())
                .append(" (");
        valuesClause.append("values (");

        List<String> valuesClauses = new ArrayList<>();
        List<String> updateClauses = new ArrayList<>();

        Set<String> ukColumns = Utils.getUniqueKeys(context, introspectedTable).values().iterator().next();
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        for (int i = 0; i < columns.size(); i++) {
            IntrospectedColumn introspectedColumn = columns.get(i);

            insertClause.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            String parameterClause = MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
            valuesClause.append(parameterClause.substring(0, 2))
                    .append("record.")
                    .append(parameterClause.substring(2));

            // process update clauses for not primaryKey columns
            if (!ukColumns.contains(introspectedColumn.getActualColumnName())
                    && !gkColumnName.equals(introspectedColumn.getActualColumnName())) {
                OutputUtilities.xmlIndent(updateClause, 1);
                updateClause.append(introspectedColumn.getActualColumnName())
                        .append(" = ")
                        .append(parameterClause.substring(0, 2))
                        .append("record.")
                        .append(parameterClause.substring(2))
                        .append(",");
                updateClauses.add(updateClause.toString());
                updateClause.setLength(0);
            }

            if (doNothingUpdateClause.length() == 0){
                doNothingUpdateClause.append(introspectedColumn.getActualColumnName())
                        .append(" = ")
                        .append(introspectedColumn.getActualColumnName());
            }

            if (i + 1 < columns.size()) {
                insertClause.append(", ");
                valuesClause.append(", ");
            }

            if (valuesClause.length() > 80) {
                element.addElement(new TextElement(insertClause.toString()));
                insertClause.setLength(0);
                OutputUtilities.xmlIndent(insertClause, 1);

                valuesClauses.add(valuesClause.toString());
                valuesClause.setLength(0);
                OutputUtilities.xmlIndent(valuesClause, 1);
            }
        }

        insertClause.append(')');
        element.addElement(new TextElement(insertClause.toString()));

        valuesClause.append(')');
        valuesClauses.add(valuesClause.toString());

        // if table has a generated key, append string like `id = last_insert_id(id)` to updateClause
        // to ensures the returned id always references the inserted entity or the updated entity
        if(!updateGK.isEmpty()){
            updateClauses.add(updateGK);
        }

        for (String clause: valuesClauses) {
            element.addElement(new TextElement(clause));
        }

        element.addElement(new TextElement("on duplicate key update "));
        if (updateClauses.size() > 0) {
            for (int i = 0; i < updateClauses.size(); i++) {
                String clause = updateClauses.get(i);
                if (i == updateClauses.size() - 1) {
                    clause = clause.substring(0, clause.length() - 1);
                }
                element.addElement(new TextElement(clause));
            }
        } else {
            element.addElement(new TextElement("<!-- 确保on duplicate key update子句不为空， 避免sql语法错误 -->"));
            element.addElement(new TextElement(doNothingUpdateClause.toString()));
        }

        parent.addElement(element);
    }

    // add insertOrUpdateManually() method
    private void addInsertOrUpdateManuallyMethod(Interface interfaze, IntrospectedTable introspectedTable) {
        Method method = new Method();
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        method.setName("insertOrUpdateManually");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());

        Parameter record = new Parameter(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "record");
        record.addAnnotation("@Param(\"record\")");
        method.addParameter(record);
        Parameter updateClause = new Parameter(FullyQualifiedJavaType.getStringInstance(), "updateClause");
        updateClause.addAnnotation("@Param(\"updateClause\")");
        method.addParameter(updateClause);

        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        interfaze.addMethod(method);
    }

    // add XML element for insertOrUpdateManually()
    private void addInsertOrUpdateManuallyElement(XmlElement parent, IntrospectedTable introspectedTable) {
        XmlElement element = new XmlElement("insert");
        context.getCommentGenerator().addComment(element);

        element.addAttribute(new Attribute("id", "insertOrUpdateManually"));
        element.addAttribute(new Attribute("parameterType", "map"));

        GeneratedKey gk = introspectedTable.getGeneratedKey();
        String updateGK = "";
        if (gk != null) {
            IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
            if (introspectedColumn != null) {
                if (gk.isJdbcStandard()) {
                    element.addAttribute(new Attribute("useGeneratedKeys", "true"));
                    element.addAttribute(new Attribute("keyProperty", "record." + introspectedColumn.getJavaProperty()));
                    element.addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName()));
                } else {
                    element.addElement(getSelectKeyElement(introspectedColumn, gk));
                }
                updateGK = String.format(", %s = last_insert_id(%s)",
                        introspectedColumn.getActualColumnName(), introspectedColumn.getActualColumnName());
            }
        }
        StringBuilder insertClause = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();
        insertClause.append("insert into ")
                .append(introspectedTable.getFullyQualifiedTableNameAtRuntime())
                .append(" (");
        valuesClause.append("values (");

        List<String> valuesClauses = new ArrayList<>();
        List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(
                introspectedTable.getAllColumns());
        for (int i = 0; i < columns.size(); i++) {
            IntrospectedColumn introspectedColumn = columns.get(i);

            insertClause.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            String parameterClause = MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
            valuesClause.append(parameterClause.substring(0, 2))
                    .append("record.")
                    .append(parameterClause.substring(2));

            if (i + 1 < columns.size()) {
                insertClause.append(", ");
                valuesClause.append(", ");
            }

            if (valuesClause.length() > 80) {
                element.addElement(new TextElement(insertClause.toString()));
                insertClause.setLength(0);
                OutputUtilities.xmlIndent(insertClause, 1);

                valuesClauses.add(valuesClause.toString());
                valuesClause.setLength(0);
                OutputUtilities.xmlIndent(valuesClause, 1);
            }
        }

        insertClause.append(')');
        element.addElement(new TextElement(insertClause.toString()));

        valuesClause.append(')');
        valuesClauses.add(valuesClause.toString());

        for (String clause: valuesClauses) {
            element.addElement(new TextElement(clause));
        }

        // if table has a generated key, append string like `id = last_insert_id(id)` to updateClause
        // to ensures the returned id always references the inserted entity or the updated entity
        element.addElement(new TextElement("on duplicate key update ${updateClause}" + updateGK));

        parent.addElement(element);
    }

    // add insertSelectiveOrUpdateManually() method
    private void addInsertSelectiveOrUpdateManuallyMethod(Interface interfaze, IntrospectedTable introspectedTable) {
        Method method = new Method();
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        method.setName("insertSelectiveOrUpdateManually");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());

        Parameter record = new Parameter(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "record");
        record.addAnnotation("@Param(\"record\")");
        method.addParameter(record);
        Parameter updateClause = new Parameter(FullyQualifiedJavaType.getStringInstance(), "updateClause");
        updateClause.addAnnotation("@Param(\"updateClause\")");
        method.addParameter(updateClause);

        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        interfaze.addMethod(method);
    }

    // add XML element for insertSelectiveOrUpdateManually()
    private void addInsertSelectiveOrUpdateManuallyElement(XmlElement parent, IntrospectedTable introspectedTable) {
        XmlElement element = new XmlElement("insert");
        context.getCommentGenerator().addComment(element);

        element.addAttribute(new Attribute("id", "insertSelectiveOrUpdateManually"));
        element.addAttribute(new Attribute("parameterType", "map"));

        GeneratedKey gk = introspectedTable.getGeneratedKey();
        String updateGK = "";
        if (gk != null) {
            IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
            if (introspectedColumn != null) {
                if (gk.isJdbcStandard()) {
                    element.addAttribute(new Attribute("useGeneratedKeys", "true"));
                    element.addAttribute(new Attribute("keyProperty", "record." + introspectedColumn.getJavaProperty()));
                    element.addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName()));
                } else {
                    element.addElement(getSelectKeyElement(introspectedColumn, gk));
                }
                updateGK = String.format(", %s = last_insert_id(%s)",
                        introspectedColumn.getActualColumnName(), introspectedColumn.getActualColumnName());
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("insert into ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        element.addElement(new TextElement(sb.toString()));

        XmlElement insertTrimElement = new XmlElement("trim");
        insertTrimElement.addAttribute(new Attribute("prefix", "("));
        insertTrimElement.addAttribute(new Attribute("suffix", ")"));
        insertTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        element.addElement(insertTrimElement);

        XmlElement valuesTrimElement = new XmlElement("trim");
        valuesTrimElement.addAttribute(new Attribute("prefix", "values ("));
        valuesTrimElement.addAttribute(new Attribute("suffix", ")"));
        valuesTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        element.addElement(valuesTrimElement);

        for (IntrospectedColumn introspectedColumn: ListUtilities.
                removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns())) {

            if (introspectedColumn.isSequenceColumn()
                    || introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
                sb.setLength(0);
                sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                sb.append(',');
                insertTrimElement.addElement(new TextElement(sb.toString()));

                sb.setLength(0);
                String parameterClause = MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
                sb.append(parameterClause.substring(0, 2));
                sb.append("record.");
                sb.append(parameterClause.substring(2));
                sb.append(',');
                valuesTrimElement.addElement(new TextElement(sb.toString()));

                continue;
            }

            XmlElement insertNotNullElement = new XmlElement("if");
            sb.setLength(0);
            sb.append("record.");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null");
            insertNotNullElement.addAttribute(new Attribute(
                    "test", sb.toString()));

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities
                    .getEscapedColumnName(introspectedColumn));
            sb.append(',');
            insertNotNullElement.addElement(new TextElement(sb.toString()));
            insertTrimElement.addElement(insertNotNullElement);

            XmlElement valuesNotNullElement = new XmlElement("if");
            sb.setLength(0);
            sb.append("record.");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null");
            valuesNotNullElement.addAttribute(new Attribute(
                    "test", sb.toString()));

            sb.setLength(0);
            String parameterClause = MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
            sb.append(parameterClause.substring(0, 2));
            sb.append("record.");
            sb.append(parameterClause.substring(2));
            sb.append(',');
            valuesNotNullElement.addElement(new TextElement(sb.toString()));
            valuesTrimElement.addElement(valuesNotNullElement);
        }

        // if table has a generated key, append string like `id = last_insert_id(id)` to updateClause
        // to ensures the returned id always references the inserted entity or the updated entity
        element.addElement(new TextElement("on duplicate key update ${updateClause}" + updateGK));

        parent.addElement(element);
    }



    // add insertSelectiveOrUpdateByUniqueKeySelective() method
    private void addInsertSelectiveOrUpdateByUniqueKeySelectiveMethod(Interface interfaze, IntrospectedTable introspectedTable) {
        Method method = new Method();
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        method.setName("insertSelectiveOrUpdateByUniqueKeySelective");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());

        Parameter record = new Parameter(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "record");
        record.addAnnotation("@Param(\"record\")");
        method.addParameter(record);

        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        interfaze.addMethod(method);
    }

    // add XML element for insertSelectiveOrUpdateByUniqueKeySelective()
    private void addInsertSelectiveOrUpdateByUniqueKeySelectiveElement(XmlElement parent, IntrospectedTable introspectedTable) {
        XmlElement element = new XmlElement("insert");
        context.getCommentGenerator().addComment(element);

        element.addAttribute(new Attribute("id", "insertSelectiveOrUpdateByUniqueKeySelective"));
        element.addAttribute(new Attribute("parameterType", "map"));

        GeneratedKey gk = introspectedTable.getGeneratedKey();
        String updateGK = "";
        String gkColumnName = "";
        if (gk != null) {
            IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
            if (introspectedColumn != null) {
                gkColumnName = introspectedColumn.getActualColumnName();
                if (gk.isJdbcStandard()) {
                    element.addAttribute(new Attribute("useGeneratedKeys", "true"));
                    element.addAttribute(new Attribute("keyProperty", "record." + introspectedColumn.getJavaProperty()));
                    element.addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName()));
                } else {
                    element.addElement(getSelectKeyElement(introspectedColumn, gk));
                }
                updateGK = String.format("%s = last_insert_id(%s)",
                        introspectedColumn.getActualColumnName(), introspectedColumn.getActualColumnName());
            }
        }

        StringBuilder sb = new StringBuilder();

        Set<String> ukColumns = Utils.getUniqueKeys(context, introspectedTable).values().iterator().next();

        sb.append("insert into ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        element.addElement(new TextElement(sb.toString()));

        XmlElement insertTrimElement = new XmlElement("trim");
        insertTrimElement.addAttribute(new Attribute("prefix", "("));
        insertTrimElement.addAttribute(new Attribute("suffix", ")"));
        insertTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        element.addElement(insertTrimElement);

        XmlElement valuesTrimElement = new XmlElement("trim");
        valuesTrimElement.addAttribute(new Attribute("prefix", "values ("));
        valuesTrimElement.addAttribute(new Attribute("suffix", ")"));
        valuesTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        element.addElement(valuesTrimElement);
        

        List<Element> onDupClauseElements = new ArrayList<>();
        StringBuilder doNothingUpdateClause = new StringBuilder();

        for (IntrospectedColumn introspectedColumn: introspectedTable.getAllColumns()) {
            if (doNothingUpdateClause.length() == 0) {
                doNothingUpdateClause.append(introspectedColumn.getActualColumnName())
                        .append(" = ")
                        .append(introspectedColumn.getActualColumnName());
            }

            if (introspectedColumn.isSequenceColumn()
                    || introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
                sb.setLength(0);
                sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                sb.append(',');
                insertTrimElement.addElement(new TextElement(sb.toString()));

                sb.setLength(0);
                String parameterClause = MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
                sb.append(parameterClause.substring(0, 2));
                sb.append("record.");
                sb.append(parameterClause.substring(2));
                sb.append(',');
                valuesTrimElement.addElement(new TextElement(sb.toString()));

                if (!ukColumns.contains(introspectedColumn.getActualColumnName())
                        && !gkColumnName.equals(introspectedColumn.getActualColumnName())){
                    sb.setLength(0);
                    OutputUtilities.xmlIndent(sb, 1);
                    sb.append(parameterClause.substring(0, 2));
                    sb.append("record.");
                    sb.append(parameterClause.substring(2));
                    sb.append(',');
                    onDupClauseElements.add(new TextElement(sb.toString()));
                }

                continue;
            }

            XmlElement insertNotNullElement = new XmlElement("if");
            sb.setLength(0);
            sb.append("record.");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null");
            insertNotNullElement.addAttribute(new Attribute(
                    "test", sb.toString()));

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities
                    .getEscapedColumnName(introspectedColumn));
            sb.append(',');
            insertNotNullElement.addElement(new TextElement(sb.toString()));
            insertTrimElement.addElement(insertNotNullElement);

            XmlElement valuesNotNullElement = new XmlElement("if");
            sb.setLength(0);
            sb.append("record.");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null");
            valuesNotNullElement.addAttribute(new Attribute(
                    "test", sb.toString()));

            sb.setLength(0);
            String parameterClause = MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
            sb.append(parameterClause.substring(0, 2));
            sb.append("record.");
            sb.append(parameterClause.substring(2));
            sb.append(',');
            valuesNotNullElement.addElement(new TextElement(sb.toString()));
            valuesTrimElement.addElement(valuesNotNullElement);

            if (!ukColumns.contains(introspectedColumn.getActualColumnName())
                    && !gkColumnName.equals(introspectedColumn.getActualColumnName())){
                XmlElement updateNotNullElement = new XmlElement("if");
                sb.setLength(0);
                sb.append("record.");
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(" != null");
                updateNotNullElement.addAttribute(new Attribute(
                        "test", sb.toString()));

                sb.setLength(0);
                sb.append(introspectedColumn.getActualColumnName());
                sb.append(" = ");
                sb.append(parameterClause.substring(0, 2));
                sb.append("record.");
                sb.append(parameterClause.substring(2));
                sb.append(',');
                updateNotNullElement.addElement(new TextElement(sb.toString()));
                onDupClauseElements.add(updateNotNullElement);
            }

        }


        element.addElement(new TextElement("on duplicate key update "));
        for (Element e : onDupClauseElements) {
            element.addElement(e);
        }

        if (!updateGK.isEmpty()) {
            // if table has a generated key, append string like `id = last_insert_id(id)` to updateClause
            // to ensures the returned id always references the inserted entity or the updated entity
            sb.setLength(0);
            OutputUtilities.xmlIndent(sb, 1);
            element.addElement(new TextElement(sb.append(
                    "<!-- 有generated key时确保不管是执行insert还是update子句，都返回所影响的行的自增列的值 -->").toString()));
            sb.setLength(0);
            OutputUtilities.xmlIndent(sb, 1);
            element.addElement(new TextElement(sb.append(updateGK).toString()));
        } else {
            /**
             *  如果运行时给的参数除了unique key字段之外其他字段都为空，
             *  会values子句不为空，但on duplicate..子句为空, 避免这种情况
             */
            sb.setLength(0);
            OutputUtilities.xmlIndent(sb, 1);
            element.addElement(new TextElement(sb.append(
                    "<!-- 确保on duplicate key update子句后面不为空， 避免sql语法错误 -->").toString()));
            sb.setLength(0);
            OutputUtilities.xmlIndent(sb, 1);
            element.addElement(new TextElement(sb.append(doNothingUpdateClause.toString()).toString()));
        }

        parent.addElement(element);
    }

    // add selectManuallyByExample() method
    private void addSelectManuallyByExampleMethod(Interface interfaze, IntrospectedTable introspectedTable) {
        Method method = new Method();
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        method.setName("selectManuallyByExample");
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
        returnType.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        method.setReturnType(returnType);

        Parameter selectClause = new Parameter(FullyQualifiedJavaType.getStringInstance(), "selectClause");
        selectClause.addAnnotation("@Param(\"selectClause\")");
        method.addParameter(selectClause);
        Parameter example = new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example");
        example.addAnnotation("@Param(\"example\")");
        method.addParameter(example);

        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        interfaze.addMethod(method);
    }

    // add XML element for selectManuallyByExample()
    private void addSelectManuallyByExampleElement(XmlElement parent, IntrospectedTable introspectedTable) {
        XmlElement element = new XmlElement("select");
        context.getCommentGenerator().addComment(element);

        element.addAttribute(new Attribute("id", "selectManuallyByExample"));
        element.addAttribute(new Attribute("parameterType", "map"));
        element.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));

        element.addElement(new TextElement("select"));

        XmlElement distinct = new XmlElement("if");
        distinct.addAttribute(new Attribute("test", "example.distinct"));
        distinct.addElement(new TextElement("distinct"));
        element.addElement(distinct);

        element.addElement(new TextElement("${selectClause} from " +
                introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        XmlElement example = new XmlElement("if");
        example.addAttribute(new Attribute("test", "_parameter != null"));
        XmlElement includeElement = new XmlElement("include");
        includeElement.addAttribute(new Attribute("refid", introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
        example.addElement(includeElement);
        element.addElement(example);

        XmlElement orderBy = new XmlElement("if");
        orderBy.addAttribute(new Attribute("test", "example.orderByClause != null"));
        orderBy.addElement(new TextElement("order by ${example.orderByClause}"));
        element.addElement(orderBy);

        element.addElement(getLimitOffsetClauseElement("example."));

        parent.addElement(element);
    }

    // add selectManuallyByPrimaryKey() method
    private void addSelectManuallyByPrimaryKeyMethod(Interface interfaze, IntrospectedTable introspectedTable) {
        Method method = new Method();
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        method.setName("selectManuallyByPrimaryKey");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

        Parameter selectClause = new Parameter(FullyQualifiedJavaType.getStringInstance(), "selectClause");
        selectClause.addAnnotation("@Param(\"selectClause\")");
        method.addParameter(selectClause);

        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            Parameter key = new Parameter(new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType()), "key");
            key.addAnnotation("@Param(\"key\")");
            method.addParameter(key);
        } else {
            List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();
            for (IntrospectedColumn introspectedColumn: introspectedColumns) {
                FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
                interfaze.addImportedType(type);

                Parameter parameter = new Parameter(type, introspectedColumn.getJavaProperty());
                parameter.addAnnotation("@Param(\"" + introspectedColumn.getJavaProperty() + "\")");
                method.addParameter(parameter);
            }
        }

        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        interfaze.addMethod(method);
    }

    // add XML element for selectManuallyByPrimaryKey()
    private void addSelectManuallyByPrimaryKeyElement(XmlElement parent, IntrospectedTable introspectedTable) {
        XmlElement element = new XmlElement("select");
        context.getCommentGenerator().addComment(element);

        element.addAttribute(new Attribute("id", "selectManuallyByPrimaryKey"));
        element.addAttribute(new Attribute("parameterType", "map"));
        element.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));

        element.addElement(new TextElement("select ${selectClause} from " +
                introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        boolean addPrefix = introspectedTable.getRules().generatePrimaryKeyClass();
        boolean and = false;
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn introspectedColumn: introspectedTable.getPrimaryKeyColumns()) {
            sb.setLength(0);
            if (and) {
                sb.append("  and ");
            } else {
                sb.append("where ");
                and = true;
            }

            sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            String parameterClause = MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
            if (addPrefix) {
                sb.append(parameterClause.substring(0, 2));
                sb.append("key.");
                sb.append(parameterClause.substring(2));
            } else {
                sb.append(parameterClause);
            }
            element.addElement(new TextElement(sb.toString()));
        }

        parent.addElement(element);
    }

    // add updateManuallyByExample() method
    private void addUpdateManuallyByExampleMethod(Interface interfaze, IntrospectedTable introspectedTable) {
        Method method = new Method();
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        method.setName("updateManuallyByExample");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());

        Parameter updateClause = new Parameter(FullyQualifiedJavaType.getStringInstance(), "updateClause");
        updateClause.addAnnotation("@Param(\"updateClause\")");
        method.addParameter(updateClause);
        Parameter example = new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example");
        example.addAnnotation("@Param(\"example\")");
        method.addParameter(example);

        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        interfaze.addMethod(method);
    }

    // add XML element for updateManuallyByExample()
    private void addUpdateManuallyByExampleElement(XmlElement parent, IntrospectedTable introspectedTable) {
        XmlElement element = new XmlElement("update");
        context.getCommentGenerator().addComment(element);

        element.addAttribute(new Attribute("id", "updateManuallyByExample"));
        element.addAttribute(new Attribute("parameterType", "map"));

        element.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        element.addElement(new TextElement("set ${updateClause}"));

        XmlElement example = new XmlElement("if");
        example.addAttribute(new Attribute("test", "_parameter != null"));
        XmlElement includeElement = new XmlElement("include"); //$NON-NLS-1$
        includeElement.addAttribute(new Attribute("refid", introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
        example.addElement(includeElement);
        element.addElement(example);

        parent.addElement(element);
    }

    // add updateManuallyByPrimaryKey() method
    private void addUpdateManuallyByPrimaryKeyMethod(Interface interfaze, IntrospectedTable introspectedTable) {
        Method method = new Method();
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        method.setName("updateManuallyByPrimaryKey");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());

        Parameter updateClause = new Parameter(FullyQualifiedJavaType.getStringInstance(), "updateClause");
        updateClause.addAnnotation("@Param(\"updateClause\")");
        method.addParameter(updateClause);

        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            Parameter key = new Parameter(new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType()), "key");
            key.addAnnotation("@Param(\"key\")");
            method.addParameter(key);
        } else {
            List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();
            for (IntrospectedColumn introspectedColumn: introspectedColumns) {
                FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
                interfaze.addImportedType(type);

                Parameter parameter = new Parameter(type, introspectedColumn.getJavaProperty());
                parameter.addAnnotation("@Param(\"" + introspectedColumn.getJavaProperty() + "\")");
                method.addParameter(parameter);
            }
        }

        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        interfaze.addMethod(method);
    }

    // add XML element for updateManuallyByPrimaryKey()
    private void addUpdateManuallyByPrimaryKeyElement(XmlElement parent, IntrospectedTable introspectedTable) {
        XmlElement element = new XmlElement("update");
        context.getCommentGenerator().addComment(element);

        element.addAttribute(new Attribute("id", "updateManuallyByPrimaryKey"));
        element.addAttribute(new Attribute("parameterType", "map"));

        element.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        element.addElement(new TextElement("set ${updateClause}"));

        boolean addPrefix = introspectedTable.getRules().generatePrimaryKeyClass();
        boolean and = false;
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn introspectedColumn: introspectedTable
                .getPrimaryKeyColumns()) {
            sb.setLength(0);
            if (and) {
                sb.append("  and ");
            } else {
                sb.append("where ");
                and = true;
            }

            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            String parameterClause = MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
            if (addPrefix) {
                sb.append(parameterClause.substring(0, 2));
                sb.append("key.");
                sb.append(parameterClause.substring(2));
            } else {
                sb.append(parameterClause);
            }
            element.addElement(new TextElement(sb.toString()));
        }

        parent.addElement(element);
    }

    // generate XML element for limit/offset clause
    private XmlElement getLimitOffsetClauseElement(String prefix) {
        if (prefix == null) prefix = "";

        XmlElement element = new XmlElement("if");
        element.addAttribute(new Attribute("test", prefix + "limit != null"));

        XmlElement choose = new XmlElement("choose");

        XmlElement when = new XmlElement("when");
        when.addAttribute(new Attribute("test", prefix + "offset != null"));
        when.addElement(new TextElement("limit ${" + prefix + "offset}, ${" + prefix + "limit}"));

        XmlElement otherwise = new XmlElement("otherwise");
        otherwise.addElement(new TextElement("limit ${" + prefix + "limit}"));

        choose.addElement(when);
        choose.addElement(otherwise);

        element.addElement(choose);
        return element;
    }

    // copied from AbstractXmlElementGenerator, used for selectOrUpdateManually() / selectSelectiveOrUpdateManually()
    // should return an XmlElement for the select key used to automatically generate keys.
    private XmlElement getSelectKeyElement(
            IntrospectedColumn introspectedColumn, GeneratedKey generatedKey) {
        XmlElement element = new XmlElement("selectKey");

        String identityColumnType = introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName();
        element.addAttribute(new Attribute("resultType", identityColumnType));
        // add "record." prefix to `keyProperty` for selectOrUpdateManually() / selectSelectiveOrUpdateManually()
        element.addAttribute(new Attribute("keyProperty", "record." + introspectedColumn.getJavaProperty()));
        element.addAttribute(new Attribute("order", generatedKey.getMyBatis3Order()));
        element.addElement(new TextElement(generatedKey.getRuntimeSqlStatement()));

        return element;
    }
}