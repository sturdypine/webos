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
	
	<bean id="${beanJT}" class="spc.webos.persistence.jdbc.SlaveJdbcTemplate" init-method="init" p:name="${db}">
		<property name="ds">
			<util:list>
<#assign numKey="jdbc.${db}.slave.num">
<#assign num=_conf[numKey]!-1>
<#list 0..num as i>
<#assign dbType="jdbc.${db}.slave.${i}.dbType">
<#assign url="jdbc.${db}.slave.${i}.url">
<#assign driver="jdbc.${db}.slave.${i}.driver">
<#assign username="jdbc.${db}.slave.${i}.username">
<#assign password="jdbc.${db}.slave.${i}.password">
<#assign maxActive="jdbc.${db}.slave.${i}.maxActive">
<#if _conf[password]?exists>
				<bean  class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close"
					p:url="${_conf[url]?replace("&","&amp;")}"
					p:driverClassName="${_conf[driver]!''}"
					p:dbType="${_conf[dbType]!''}"
					p:maxActive="${_conf[maxActive]!'20'}"
					p:username="${_conf[username]}" p:password="${_conf[password]}"
					p:name="${db?upper_case}-${i}" />
</#if>
</#list>
			</util:list>
		</property>
	</bean>
</beans>
