<#function fieldValue type value>
	<#if type=="String"><#return "\"${value}\""></#if>
    <#if type=="float"><#return "${value}f"></#if>
    <#if type=="double"><#return "${value}d"></#if>
    <#return value>
</#function>
<#function contains list item>
	<#list list as nextInList>
    	<#if nextInList == item><#return true></#if>
    </#list>
    <#return false>
</#function>
<#function convertType type>
	<#if type=="long"><#return "Long"></#if>
	<#if type=="int"><#return "Integer"></#if>
	<#if type=="short"><#return "Short"></#if>
	<#if type=="float"><#return "Float"></#if>
	<#if type=="double"><#return "Double"></#if>
</#function>
<#assign JAVA_BUILDIN_TYPES=["short","int","long","float","double"]>
<#assign blobs=0>
package ${package};

import java.io.*;
import java.math.*;
import java.util.*;
<#if valueObject>import spc.webos.model.ValueObject;</#if>

/**
* genarated by sturdypine.chen
* Email: sturdypine@gmail.com
* description: ${vo.remark?default("")}
*/
public class ${clazzName} <#if vo.parent?exists>extends ${vo.parent}<#else>implements <#if valueObject>ValueObject<#else>java.io.Serializable</#if></#if>
{
	public static final long serialVersionUID = ${currentDt?string("yyyyMMdd")}L;
	// 
<#list fields as field>
	<#assign nullValueRemark="">
	<#assign fValue="">
	<#if field.nullValue?exists && field.nullValue!="" && contains(JAVA_BUILDIN_TYPES,field.javaType)>
		<#assign fValue=fieldValue("${field.javaType}",field.nullValue)>
		<#assign nullValueRemark="("+fValue+" NULL)">
	</#if>
	protected <#if (field.javaType=='AbstractBlob')><#assign blobs=blobs+1>transient </#if>${field.javaType} ${field.name}<#if fValue!=""> = ${fValue}</#if>; // ${field.remark?default("")}${nullValueRemark}<#if field.primary> </#if>
</#list>
<#if voProperties?exists>

	// VOVO
<#list voProperties as field>
<#if !field.select?exists>
<#assign javaType="${field.javaType}">
<#if !field.manyToOne><#assign javaType="List<"+javaType+">"></#if>
	protected ${javaType} ${field.name};<#if field.remark?exists> // ${field.remark}</#if>
</#if>
</#list>
</#if>
<#if voProperties?exists>
	
	// VOSql
	// Note: SqlString, Inegter...Java final class Object 
	// ObjecttoString()
<#list voProperties as field>
<#if field.select?exists>
	protected ${field.javaType} ${field.name}; // SQL_ID = ${field.select}  ${field.remark?default("")}
</#if>
</#list>
</#if>
<#if valueObject>
<#assign seq="null"><#assign type=""><#assign nullValue="">
<#list fields as field><#if field.sequence?exists><#assign seq="${field.name}"><#assign type="${field.javaType}"><#assign nullValue="${field.nullValue?default('')}"></#if></#list>
<#if seq=="null"><#list fields as field><#if field.primary><#assign seq="${field.name}"><#assign type="${field.javaType}"><#assign nullValue="${field.nullValue?default('')}"></#if></#list></#if>
<#--<#if contains(JAVA_BUILDIN_TYPES,type)>
	<#assign type=convertType(type)>
	public static final String SEQ_NAME = "${seq}";
<#else>
	public static final String SEQ_NAME = "${seq}";
</#if>-->
</#if>
<#assign first="1">

<#list vo.staticFields as field>
	${field}
</#list>
	
	public ${clazzName}()
	{
	}
	
	<#assign primary="0">
	<#list fields as field>
		<#if field.primary><#assign primary="1"></#if>
	</#list>
	<#if primary="1">
	<#assign first="1">
	public ${clazzName}(<#list fields as field><#if field.primary><#if first="0"> ,<#else><#assign first="0"></#if>${field.javaType} ${field.name}</#if></#list>)
	{
	<#list fields as field>
	<#if field.primary>
		this.${field.name} = ${field.name};
	</#if>
	</#list>
	}
	</#if>


<#assign first="1">
<#if valueObject>
<#--
	public void setPrimary(<#list fields as field><#if field.primary><#if first="0"> ,<#else><#assign first="0"></#if>${field.javaType} ${field.name}</#if></#list>)
	{
	<#list fields as field>
	<#if field.primary>
		this.${field.name} = ${field.name};
	</#if>
	</#list>
	}
-->
	
	public String primary()
	{
		String delim = "#";
		StringBuffer buf = new StringBuffer();<#assign f="0">
	<#list fields as field>
	<#if field.primary>
		<#if f!="0">buf.append(delim + this.${field.name});<#else>buf.append(this.${field.name});</#if><#assign f="1">
	</#if>
	</#list>
		return buf.toString();
	}
	
	public String table()
	{
		return "${classDesc.table}";
	}
	
	public String[] blobFields()
	{
<#if blobs==0>
		return null;
<#else>
		return new String[] {<#list fields as field><#if (field.javaType=='AbstractBlob')><#assign blobs=blobs-1>"${field.name}"<#if (blobs>0)>, </#if></#if></#list>};
</#if>
	}
	
</#if>
	// set all properties to NULL
	public void setNULL()
	{
	<#list fields as field>
		<#assign fValue="null">
		<#if field.nullValue?exists && field.nullValue!="null" && contains(JAVA_BUILDIN_TYPES,field.javaType)>
			<#assign fValue=fieldValue("${field.javaType}",field.nullValue)>
		</#if>
    	this.${field.name} = ${fValue};
	</#list>
	<#if voProperties?exists>
	<#list voProperties as field>
	    this.${field.name} = null;
	</#list>
	</#if>
	}
	
	public void setRefNull()
	{
<#if voProperties?exists>
<#list voProperties as field>
		this.${field.name} = null;
</#list>
</#if>
	}
	
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof ${clazzName})) return false;
		${clazzName} obj = (${clazzName}) o;
<#list fields as field>
	<#if contains(JAVA_BUILDIN_TYPES,field.javaType)>
		if (${field.name} != obj.${field.name}) return false;
	<#else>
		if (!${field.name}.equals(obj.${field.name})) return false;
	</#if>
</#list>
		return true;
	}
	
	public int compareTo(Object o)
	{
		return -1;
	}

	// set all properties to default value...
	public void init()
	{
	<#list fields as field>
		<#if field.defaultValue?exists>
		<#assign fValue=fieldValue("${field.javaType}",field.defaultValue)>
		this.${field.name} = ${fValue};
		</#if>
	</#list>
	}
	
<#list fields as field>
    public ${field.javaType} get${field.name?cap_first}()
    {
        return ${field.name};
    }
    
    public void set${field.name?cap_first}(${field.javaType} ${field.name})
    {
        this.${field.name} = ${field.name};
    }
    
</#list>
<#if voProperties?exists>
<#list voProperties as field>
<#assign javaType="${field.javaType}">
<#if !field.manyToOne><#assign javaType="List<"+javaType+">"></#if>

	// Note: ObjecttoString()
	public ${javaType} get${field.name?cap_first}()
    {
<#if field.select?exists && javaType="Object">
		return ${field.name} == null ? null : ${field.name}.toString();
<#else>
        return ${field.name};
</#if>
    }
    
    public void set${field.name?cap_first}(${javaType} ${field.name})
    {
        this.${field.name} = ${field.name};
    }
</#list>
</#if>
	
	public void set(${clazzName} vo)
	{
<#list fields as field>
    	this.${field.name} = vo.${field.name};
</#list>
<#if voProperties?exists>
<#list voProperties as field>
		this.${field.name} = vo.${field.name};
</#list>
</#if>
	}
	
	public StringBuffer toJson()
	{
		StringBuffer buf = new StringBuffer();
		try {
			buf.append(${json}(this));
		} catch (Exception e) {
		}
		return buf;
	}

<#if valueObject>
	public void afterLoad()
	{
	}

	public void beforeLoad()
	{
	}
	
	public void destroy()
	{
<#list fields as field><#if (field.javaType=='AbstractBlob')>		if (${field.name} != null) ${field.name}.close();</#if></#list>
	}
</#if>

	public String toString()
	{
		StringBuffer buf = new StringBuffer(128);
		buf.append(getClass().getName() + "(serialVersionUID=" + serialVersionUID + "):");
		buf.append(toJson());
		return buf.toString();
	}
	
	public Object clone()
	{
		${clazzName} obj = new ${clazzName}();
		obj.set(this);
		return obj;
	}
}
