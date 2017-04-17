#!/bin/bash
module=$1

if [ -n "$DOCKER_IP" ]
then
        cp /etc/hosts /etc/hosts.temp
        sed -i "s/.*$(hostname)/$DOCKER_IP $(hostname)/" /etc/hosts.temp
        cat /etc/hosts.temp > /etc/hosts
fi

cd $(dirname $0)/../apps
webosPath=`pwd`
cd ${module}
m=`pwd`
echo "work_dir:${m}"

bizjar=""
webos=""
CLASSPATH=
# scan apps/xx/lib/mule/*.jar
if [ -d "lib/mule/" ]
then
	for jar in lib/mule/*.jar; do
	    CLASSPATH=$jar:$CLASSPATH
	done
fi
# scan apps/xx/lib/ext/*.jar
if [ -d "lib/ext/" ]
then
	for jar in lib/ext/*.jar; do
	    CLASSPATH=$jar:$CLASSPATH
	done
fi
# scan apps/xx/lib/*.jar
for jar in lib/*.jar; do
    CLASSPATH=$jar:$CLASSPATH
    bizjar=${jar:4}:$bizjar
    [[ $jar =~ "spc-webos" ]] && webos=$jar
done
# scan webos/lib/*.jar
for jar in ../../lib/*.jar; do
	if [[ $jar =~ "spc-webos" ]]
	then
		if [[ ${webos} != "" ]]
		then
			echo "skip:[$jar]"
		else
			CLASSPATH=$CLASSPATH:$jar
			webos=$jar
		fi
	else
		CLASSPATH=$CLASSPATH:$jar
	fi
done

JAVA_MEM_OPTS=" -server -XX:SurvivorRatio=2 -XX:+UseParallelGC "

JavaPNO=`ps -ef | grep java | awk '{print $2 }'`
echo $JavaPNO
if [ "${JavaPNO}" != "" ]
then
    echo "${module} is running, cannot start it!!! "
else
    echo "${module} is starting with [${webos}] ..."
    #echo "$JAVA_MEM_OPTS -Dpdf.fontpath=$webosPath/lib/simsun.ttf -Ddubbo.shutdown.hook=true -cp .:conf:$CLASSPATH"
    exec java $JAVA_MEM_OPTS -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dapp.docker=true -Ddubbo.shutdown.hook=true $JAVA_ARGS -Dbizjar=${bizjar} -cp .:conf:$CLASSPATH com.alibaba.dubbo.container.Main
fi



