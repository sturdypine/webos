<bean id="jedisPool" class="redis.clients.jedis.ShardedJedisPool" destroy-method="destroy">
	<constructor-arg index="0" ref="jedisPoolConfig" />
	<constructor-arg index="1">
		<list>
		<#list hosts as host>
		<#assign idx=host?index_of(":")>
			<bean class="redis.clients.jedis.JedisShardInfo">
				<constructor-arg index="0" value="<#if (idx>0)>${host?substring(0,idx)}<#else>${host}</#if>" />
				<constructor-arg index="1" type="int" value="<#if (idx>0)>${host?substring(idx+1)}<#else>6379</#if>" />
			</bean>
		</#list>
		</list>
	</constructor-arg>
</bean>