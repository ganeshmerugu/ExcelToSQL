<#-- combined_tables.ftl -->

<#list tables as table>
-- SQL script to create table ${table.tableName}

CREATE TABLE ${table.tableName} (
<#list table.columns as column>
    ${column} VARCHAR(255)<#if column_has_next>,</#if>
</#list>
);

INSERT INTO ${table.tableName} (
<#list table.columns as column>
    ${column}<#if column_has_next>, </#if>
</#list>
)
VALUES
<#list table.rows as row>
    (
    <#list row as value>
        <#if value??>
            <#if value?is_string>
                '${value}'<#else>${value}</#if>
            <#else>
                NULL
            </#if><#if value_has_next>, </#if>
    </#list>
    )<#if row_has_next>,</#if>
</#list>;

<#if table_has_next>
-- End of table ${table.tableName}

</#if>
</#list>
