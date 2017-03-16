<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:p="http://www.springframework.org/schema/p" xmlns:amq="http://activemq.apache.org/schema/core"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:jms="http://www.springframework.org/schema/jms"
	xmlns:util="http://www.springframework.org/schema/util"
	xsi:schemaLocation="http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.0.xsd 
	http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd
	 http://www.springframework.org/schema/jms http://www.springframework.org/schema/jms/spring-jms-4.0.xsd
     http://activemq.apache.org/schema/core http://activemq.apache.org/schema/core/activemq-core.xsd">
     
<#list brokers as b>
	<bean id="amqCnn${b_index}" class="org.apache.activemq.jms.pool.PooledConnectionFactory"
		destroy-method="stop" p:maxConnections="${_conf['app.mq.cnn.max']!'5'}" 
		p:reconnectOnException="true" p:timeBetweenExpirationCheckMillis="${_conf['app.mq.cnn.timeBetweenExpirationCheckMillis']!'-1'}" 
		p:idleTimeout="${_conf['app.mq.cnn.idleTimeout']!'36000'}" p:maximumActiveSessionPerConnection="${_conf['app.mq.cnn.maximumActiveSessionPerConnection']!'100'}">
		<property name="connectionFactory">
			<bean class="org.apache.activemq.ActiveMQConnectionFactory" p:brokerURL="${b}" />
		</property>
	</bean>
	<jms:listener-container destination-type="queue"
		concurrency="${_conf['app.mq.lsr.concurrency']!'2-20'}" recovery-interval="${_conf['app.mq.lsr.recoveryInterval']!'30000'}"
		receive-timeout="${_conf['app.mq.lsr.receiveTimeout']!'5000'}"
		container-type="default" connection-factory="amqCnn${b_index}" acknowledge="auto">
		<#list reqQ as q>
		<jms:listener destination="${q}?consumer.prefetchSize=1" ref="amqReqJscallML" />
		</#list>
		<#list _statics["spc.webos.mq.MQ"].REQUST_QUEUE as q>
		<jms:listener destination="${q}?consumer.prefetchSize=1" ref="amqReqJscallML" />
		</#list>
		<#list resQ as q>
		<jms:listener destination="${q}?consumer.prefetchSize=2" ref="amqResJscallML" />
		</#list>
		<#list _statics["spc.webos.mq.MQ"].RESPONSE_QUEUE as q>
		<jms:listener destination="${q}?consumer.prefetchSize=2" ref="amqResJscallML" />
		</#list>
		<jms:listener destination="${reqJvmQ}?consumer.prefetchSize=5" ref="amqReqJscallML" />
		<jms:listener destination="${resJvmQ}?consumer.prefetchSize=5" ref="amqResJscallML" />
		<#list queues?keys as q>
		<jms:listener destination="${q}?consumer.prefetchSize=3" ref="${queues[q]}" />
    	</#list>
	</jms:listener-container>
</#list>
	<bean id="amqJms" class="spc.webos.mq.jms.MultiBrokerSendJmsTemplate">
		<property name="multiBrokerJms">
			<util:list>
			<#list brokers as b>
				<bean class="org.springframework.jms.core.JmsTemplate" p:connectionFactory-ref="amqCnn${b_index}" />
			</#list>
			</util:list>
		</property>
	</bean>
	<bean id="amqReqJscallML" class="spc.webos.mq.jms.JSCallMessageListener" p:jms-ref="amqJms" p:request="true" />
	<bean id="amqResJscallML" class="spc.webos.mq.jms.JSCallMessageListener" p:jms-ref="amqJms" p:request="false" />
</beans>