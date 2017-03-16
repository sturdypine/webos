<classes>
<#assign table="">
<#list talbecolumns as row>
<#if table!=row[0]><#if table!=''>	</class>
	
</#if>	<class name="${pakage?default('')}" table="${row[0]}"></#if>
<#assign table=row[0]><#assign type=row[2]?lower_case>
		<property name="${row[1]?lower_case}" <#if (type?index_of("dec")>=0)>javaType="BigDecimal"<#elseif (type?index_of("bigint")>=0)>javaType="Long"<#elseif (type?index_of("int")>=0)>javaType="Integer"</#if>/>
</#list>
</classes>