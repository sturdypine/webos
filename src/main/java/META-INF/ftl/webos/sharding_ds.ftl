<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:jee="http://www.springframework.org/schema/jee" xmlns:aop="http://www.springframework.org/schema/aop"
	xmlns:tx="http://www.springframework.org/schema/tx" xmlns:util="http://www.springframework.org/schema/util"
	xsi:schemaLocation="
	http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.0.xsd
	http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
	http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd
	http://www.springframework.org/schema/jee http://www.springframework.org/schema/jee/spring-jee-3.0.xsd
	http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-3.0.xsd
	http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-3.0.xsd">
	
<#assign numKey="${db}.jdbc.shard.num">
<#assign num=_conf[numKey]!-1>
<#list 0..num as i>
<#assign dbType="${db}.jdbc.shard.${i}.dbType">
<#assign url="${db}.jdbc.shard.${i}.url">
<#assign driver="${db}.jdbc.shard.${i}.driver">
<#assign username="${db}.jdbc.shard.${i}.username">
<#assign password="${db}.jdbc.shard.${i}.password">
<#assign maxActive="${db}.jdbc.shard.${i}.maxActive">
<#if _conf[password]?exists>
			<bean  id="${db}${i}DS" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close"
				p:url="${_conf[url]?replace("&","&amp;")}"
				p:driverClassName="${_conf[driver]!''}"
				p:dbType="${_conf[dbType]!''}"
				p:maxActive="${_conf[maxActive]!'10'}"
				p:username="${_conf[username]}" p:password="${_conf[password]}"
				p:name="${db?upper_case}_${i}" />
			<bean id="${db}${i}JT" class="spc.webos.persistence.jdbc.XJdbcTemplate"
				p:dataSource-ref="${db}${i}DS" />
</#if>
</#list>
</beans>
