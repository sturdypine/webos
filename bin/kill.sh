module=$1
jvm=$2
if [ "${jvm}" = "" ]
then
	echo "module & jvm cannot null!!!"
	exit 0
fi

cd $(dirname $0)/..
id="${module}/${jvm}"

JavaPNO=`ps -ef | grep java | grep ${id} | awk '{print $2 }'`
echo $JavaPNO
AC=`ps -ef | grep java | grep ${id} | awk '{print $1 }'`
echo $AC
if [ "${JavaPNO}" != "" ]
then
    echo "${id} jvm is running, will be killed!!!" 
    kill -9 $JavaPNO
    JavaPNO=`ps -ef | grep java | grep ${id} | awk '{print $2 }'`
	if [ "${JavaPNO}" != "" ]
	then
		echo "${id} jvm(${JavaPNO}) is running" 
	else
		echo "no ${id} jvm is running "
	fi
else
    echo "no ${id} jvm is running "
fi


