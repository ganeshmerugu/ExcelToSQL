<#-- create_table.ftl -->

-- SQL script to create a table and insert data in bulk

CREATE TABLE ${tableName} (
<#list columns as column>
    ${column} VARCHAR(255)<#if column_has_next>,</#if>
</#list>
);

INSERT INTO ${tableName} (
<#list columns as column>
    ${column}<#if column_has_next>, </#if>
</#list>
)
VALUES
<#list rows as row>
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
