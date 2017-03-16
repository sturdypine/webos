<#function contains list item>
	<#list list as nextInList>
    	<#if nextInList == item><#return true></#if>
    </#list>
    <#return false>
</#function>

<#macro genWhere field operator=0>
<#if field.prepare>
${"<#if _VO_."}${field.name}?exists>and ${field.column}=:${field.name}${"</#if>"}
<#elseif contains(JAVA_BUILDIN_TYPES,"${field.javaType}")>
${"<#if _VO_."}${field.name}!=${field.nullValue}>and ${field.column}=${"$"}{_VO_.${field.name}?c}${"</#if>"}
<#elseif contains(JAVA_NUMBER_TYPES,"${field.javaType}")>
${"<#if _VO_."}${field.name}?exists>and ${field.column}=${"$"}{_VO_.${field.name}?c}<#if operator!=0>${"<#else>"}and ${field.column} is null</#if>${"</#if>"}
<#elseif ("IBlob"!="${field.javaType}")>
<#if operator==0>${"<#if _VO_."}${field.name}?exists></#if>and ${field.column}='${"$"}{_VO_.${field.name}?default('')}'<#if operator==0>${"</#if>"}</#if>
</#if>
</#macro>

<#macro genSelect properties>
<#list properties as field>
${"<#if"} !_ASSIGNED_FIELDS_?exists||contains(_ASSIGNED_FIELDS_,"${field.name}")>${"<#if"} _FIRST_=="1">${"<#assign _FIRST_="}"0">${"<#else>"},${"</#if>"}${getOutFieldName(field)} ${field.name}${"</#if>"}
</#list>
<#if !_ASSIGNED_FIELDS_?exists && classDesc.parent?exists>
</#if>
</#macro>

<#function getOutFieldName _VO_ type="select">
	<#if type="select"&&_VO_.select?exists && _VO_.select!="">
		<#return _VO_.select>
	</#if>
	<#if type="update"&&_VO_.update?exists && _VO_.update!="">
		<#return _VO_.update>
	</#if>
	<#if type="insert"&&_VO_.insert?exists && _VO_.insert!="">
		<#return _VO_.insert>
	</#if>
	<#return _VO_.column>
</#function>

<#function genSQL _VO_ operator tableName="">
	<#if operator=0&&_VO_.select?exists&&_VO_.select!="">
		<#return _VO_.select>
	<#elseif operator=2&&_VO_.update?exists&&_VO_.update!="">
		<#return _VO_.update>
	<#elseif operator=3&&_VO_.insert?exists&&_VO_.insert!="">
		<#return _VO_.insert>
	</#if>
	<#if operator=3&&_VO_.sequence?exists&&_VO_.sequence="true">
		<#return "$"+"{_SEQUENCE_?c}">
<#--
		<#return "(select <#if _DB_TYPE_=\"SQLSERVER\">ISNULL(max("+"${_VO_.column}"+")+1,1)"
		+"<#elseif _DB_TYPE_=\"DB2\">values(max("+"${_VO_.column}"+")+1,1)"
		+"<#elseif  _DB_TYPE_=\"ORACLE\"></#if> from "+tableName+")"
		>
-->
	</#if>
	<#if operator=3&&_VO_.uuid> <#-- uuid, 900_20160109 uuid is string -->
		<#return "'$"+"{_UUID_.uuid()}'" >
		<#--<#if !contains(JAVA_BUILDIN_TYPES,_VO_.javaType)&&!contains(JAVA_NUMBER_TYPES,_VO_.javaType)><#-- no number.. -->
		<#--
			<#return "'$"+"{_UUID_.generate(_VO_,"+"${_VO_.name}"+")}'" >
		<#else>
			<#return "$"+"{_UUID_.generate(_VO_,"+"${_VO_.name}"+")}" >
		</#if>-->
	</#if>
	<#if _VO_.prepare>
		<#return "<#if _VO_."+"${_VO_.name}"+"?exists" + ">:"+"${_VO_.name}"+"<#else>NULL</#if>" >
	</#if>
	<#if !contains(JAVA_BUILDIN_TYPES,_VO_.javaType)&&!contains(JAVA_NUMBER_TYPES,_VO_.javaType)><#-- no number.. -->
		<#return "<#if _VO_." + "${_VO_.name}" + "?exists>'$"+"{_VO_." + "${_VO_.name}" + "}'<#else>NULL</#if>" >
	</#if>
	<#if _VO_.nullValue?exists&&_VO_.nullValue!="">
		<#return "<#if _VO_."+"${_VO_.name}"+"?exists&&_VO_." + "${_VO_.name}" + "!="
					+ "${_VO_.nullValue}" + ">$"+"{_VO_." + "${_VO_.name}"
					+ "?c}<#else>NULL</#if>" >
	</#if>
	<#return "<#if _VO_." + "${_VO_.name}" + "?exists>$"+"{_VO_." + "${_VO_.name}"
				+ "?c}<#else>NULL</#if>" >
</#function>

<#assign JAVA_BUILDIN_TYPES=["short","int","long","float","double"]>
<#assign JAVA_NUMBER_TYPES=["Short","Integer","Long","Float","Double","BigDecimal","BigInteger"]>
<#assign JDBC_TYPE_NUMERIC="NUMERIC">
<#assign JDBC_TYPE_CHAR="CHAR">
<#assign JDBC_TYPE_DATE="DATE">
<#assign JDBC_TYPE_TIME="TIME">
<#assign JDBC_TYPE_TIMESTAMP="TIMESTAMP">
<#assign first="1">
${classDesc.declare?default('')} 
<#if sqlType=="0">
${"<#assign _FIRST_="}"1">${"<#"}function contains l i>${"<#"}list l as n>${"<#"}if n?lower_case==i?lower_case>${"<#"}return true>${"</#"}if>${"</#"}list>${"<#"}return false>${"</#"}function>select
<#list properties as field>
<#if ("IBlob"=="${field.javaType}")>
${"<#if"} (_FILE_ALL_?exists||(_FILE_FIELDS_?exists&&contains(_FILE_FIELDS_,"${field.name}")))>${"<#if"} _FIRST_=="1">${"<#assign _FIRST_="}"0">${"<#else>"},${"</#if>"}<#if (getOutFieldName(field)?upper_case==field.name?upper_case)>${getOutFieldName(field)}<#else>${getOutFieldName(field)} ${field.name}</#if>${"</#if>"}
<#else>
${"<#if"} !_ASSIGNED_FIELDS_?exists||contains(_ASSIGNED_FIELDS_,"${field.name}")>${"<#if"} _FIRST_=="1">${"<#assign _FIRST_="}"0">${"<#else>"},${"</#if>"}<#if (getOutFieldName(field)?upper_case==field.name?upper_case)>${getOutFieldName(field)}<#else>${getOutFieldName(field)} ${field.name}</#if>${"</#if>"}
</#if>
</#list>
from ${classDesc.table} where 1=1 <#list properties as field><@genWhere field/></#list> ${"$"}{_SELECT_TAIL_?default("")}
<#elseif sqlType=="2">
${"<#assign _FIRST_="}"1">${"<#"}function contains l i>${"<#"}list l as n>${"<#"}if n?lower_case==i?lower_case>${"<#"}return true>${"</#"}if>${"</#"}list>${"<#"}return false>${"</#"}function>update ${classDesc.table} set
<#list properties as field>
<#if !field.updatable><#-- 900_20151209 updateable -->
<#elseif field.version><#-- 810_20151201 ol-->
${"<#if"} (_VO_.${field.name}?exists)>${"<#if"} _FIRST_=="1">${"<#assign _FIRST_="}"0">${"<#else>"},${"</#if>"}${field.column}=${field.column}+1${"</#if>"}
<#elseif !field.primary && !field.sequence?exists>
<#if contains(JAVA_BUILDIN_TYPES,"${field.javaType}")>
${"<#if"} (!_ASSIGNED_FIELDS_?exists||!contains(_ASSIGNED_FIELDS_,"${field.name}")) && (_UPDATE_NULL_?exists||_VO_.${field.name}!=${field.nullValue})>${"<#if"} _FIRST_=="1">${"<#assign _FIRST_="}"0">${"<#else>"},${"</#if>"}${field.column}=${genSQL(field,2)}${"</#if>"}
<#elseif contains(JAVA_NUMBER_TYPES,"${field.javaType}")>
${"<#if"} (!_ASSIGNED_FIELDS_?exists||!contains(_ASSIGNED_FIELDS_,"${field.name}")) && (_UPDATE_NULL_?exists||_VO_.${field.name}?exists)>${"<#if"} _FIRST_=="1">${"<#assign _FIRST_="}"0">${"<#else>"},${"</#if>"}${field.column}=${genSQL(field,2)}${"</#if>"}
<#elseif field.prepare>
${"<#if"} (_VO_.${field.name}?exists)>${"<#if"} _FIRST_=="1">${"<#assign _FIRST_="}"0">${"<#else>"},${"</#if>"}${field.column}=:${field.name}${"</#if>"}
<#else>
${"<#if"} (!_ASSIGNED_FIELDS_?exists||!contains(_ASSIGNED_FIELDS_,"${field.name}")) && (_UPDATE_NULL_?exists||_VO_.${field.name}?exists)>${"<#if"} _FIRST_=="1">${"<#assign _FIRST_="}"0">${"<#else>"},${"</#if>"}${field.column}=${genSQL(field,2)}${"</#if>"}
</#if>
</#if>
</#list>
where 1=1
<#list properties as field>
<#if field.version>
<@genWhere field,1/>
<#elseif field.primary||(field.sequence?exists)>
${"<#if"} (!_ASSIGNED_FIELDS_?exists||contains(_ASSIGNED_FIELDS_,"${field.name}"))><@genWhere field,1/>${"</#if>"}
<#else>
${"<#if"} (_ASSIGNED_FIELDS_?exists&&contains(_ASSIGNED_FIELDS_,"${field.name}"))><@genWhere field,1/>${"</#if>"}
</#if>
</#list> ${"$"}{_UPDATE_TAIL_!("")}
<#elseif sqlType=="1">
${"<#"}function contains l i>${"<#"}list l as n>${"<#"}if n?lower_case==i?lower_case>${"<#"}return true>${"</#"}if>${"</#"}list>${"<#"}return false>${"</#"}function>delete from ${classDesc.table}
where 1=1
<#list properties as field><@genWhere field/></#list> ${"$"}{_DELETE_TAIL_!("")}
<#else>
insert into ${classDesc.table} (<#list properties as field><#if !((_DB_TYPE_=='DB2'||_DB_TYPE_=='DERBY')&&field.sequence?exists&&field.sequence="AUTO")><#if field_index!=0>,</#if>${field.column}</#if></#list>) <#if (classDesc.insertMode==1)>select <#else>values(</#if><#list properties as field><#if !((_DB_TYPE_=='DB2'||_DB_TYPE_=='DERBY')&&field.sequence?exists&&field.sequence="AUTO")><#if field_index!=0>,</#if><#if field.version>1<#else>${genSQL(field,3,classDesc.table)}</#if></#if></#list><#if (classDesc.insertMode==1)>${"<#if"} _DB_TYPE_=='ORACLE'> from dual${"<#elseif"} _DB_TYPE_=='DB2'> from SYSIBM.SYSDUMMY1${"</#if>"}<#else>)</#if>
</#if>