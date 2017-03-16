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

	<#assign dbType="jdbc.${db}.master.dbType">
	<#assign url="jdbc.${db}.master.url">
	<#assign driver="jdbc.${db}.master.driver">
	<#assign username="jdbc.${db}.master.read.username">
	<#assign password="jdbc.${db}.master.read.password">
	<#assign maxActive="jdbc.${db}.master.read.maxActive">
	<#if _conf[password]?exists>
	<bean id="${beanJT}" class="spc.webos.persistence.jdbc.XJdbcTemplate">
		<property name="dataSource">
			<bean class="com.alibaba.druid.pool.DruidDataSource"
			init-method="init" destroy-method="close"
			p:url="${_conf[url]?replace("&","&amp;")}"
			p:driverClassName="${_conf[driver]!''}"
			p:dbType="${_conf[dbType]!''}"
			p:maxActive="${_conf[maxActive]!'20'}"
			p:username="${_conf[username]}" p:password="${_conf[password]}"
			p:name="${db?upper_case}-MR" />
		</property>
	</bean>
	</#if>
</beans>
