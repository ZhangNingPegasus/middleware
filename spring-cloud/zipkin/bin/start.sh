#!/bin/bash
Xms=2G
Xmx=2G
Xmn=1G
KAFKA_BOOTSTRAP_SERVERS="192.168.6.164:9092,192.168.6.165:9092,192.168.6.166:9092"
ES_HOSTS="http://192.168.6.165:9900,http://192.168.6.166:9900,http://192.168.6.167:9900"
ES_USERNAME="elastic"
ES_PASSWORD="50VMZ3EUJGU5rQCMcPCJ"

STORAGE_TYPE="elasticsearch"
COLLECTOR_KAFKA_ENABLED="true"
KAFKA_GROUP_ID="zipkin"
KAFKA_TOPIC="zipkin"
KAFKA_STREAMS="3"
ES_INDEX="zipkin"
ES_INDEX_SHARDS="3"
ES_INDEX_REPLICAS="1"
APPLICATION="zipkin-server"
APPLICATION_JAR="zipkin.jar"
VERSION="2.22.1"
cd $(dirname $0)
cd ..
BASE_PATH=$(pwd)
LOG_DIR="/wyyt/logs/tomcat/springcloud/zipkin/${APPLICATION}"
LOGS_HEAPDUMP="${LOG_DIR}/heapdump"
LOGS_GC="${LOG_DIR}/gc"

STARTUP_LOG="================================================ $(date +'%Y-%m-%m %H:%M:%S') ================================================\n"

if [[ ! -d "${LOG_DIR}" ]]; then
  mkdir -p "${LOG_DIR}"
fi
if [[ ! -d "${LOGS_HEAPDUMP}" ]]; then
  mkdir -p "${LOGS_HEAPDUMP}"
fi
if [[ ! -d "${LOGS_GC}" ]]; then
  mkdir -p "${LOGS_GC}"
fi

JAVA_OPT="-DCOLLECTOR_KAFKA_ENABLED=${COLLECTOR_KAFKA_ENABLED}"
JAVA_OPT="${JAVA_OPT} -DKAFKA_BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS}"
JAVA_OPT="${JAVA_OPT} -DKAFKA_GROUP_ID=${KAFKA_GROUP_ID}"
JAVA_OPT="${JAVA_OPT} -DKAFKA_TOPIC=${KAFKA_TOPIC}"
JAVA_OPT="${JAVA_OPT} -DKAFKA_STREAMS=${KAFKA_STREAMS}"
JAVA_OPT="${JAVA_OPT} -DSTORAGE_TYPE=${STORAGE_TYPE}"
JAVA_OPT="${JAVA_OPT} -DES_HOSTS=${ES_HOSTS}"
JAVA_OPT="${JAVA_OPT} -DES_USERNAME=${ES_USERNAME}"
JAVA_OPT="${JAVA_OPT} -DES_PASSWORD=${ES_PASSWORD}"
JAVA_OPT="${JAVA_OPT} -DES_INDEX=${ES_INDEX}"
JAVA_OPT="${JAVA_OPT} -DES_INDEX_SHARDS=${ES_INDEX_SHARDS}"
JAVA_OPT="${JAVA_OPT} -DES_INDEX_REPLICAS=${ES_INDEX_REPLICAS}"
JAVA_OPT="${JAVA_OPT} -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -XX:-OmitStackTraceInFastThrow "
JAVA_GC="-Xloggc:${LOGS_GC}/gc.log -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=10M "
JAVA_MEM_OPT="-server -Xms${Xms} -Xmx${Xmx} -Xmn${Xmn} -XX:NewRatio=1 -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:MaxDirectMemorySize=512m -XX:+HeapDumpOnOutOfMemoryError "
JAVA_MEM_OPT="${JAVA_MEM_OPT} -XX:HeapDumpPath=${LOGS_HEAPDUMP}/ "
JAVA_PARAMETER_OPT="-Dversion=${VERSION} -Dwork.dir=${BASE_PATH}"

STARTUP_LOG="${STARTUP_LOG}application name: ${APPLICATION}\n"
STARTUP_LOG="${STARTUP_LOG}application jar name: ${APPLICATION_JAR}\n"
STARTUP_LOG="${STARTUP_LOG}application jar version: ${VERSION}\n"
STARTUP_LOG="${STARTUP_LOG}application root path: ${BASE_PATH}\n"
STARTUP_LOG="${STARTUP_LOG}application log path: ${LOG_DIR}\n"
STARTUP_LOG="${STARTUP_LOG}application startup command: java ${JAVA_OPT} ${JAVA_GC} ${JAVA_MEM_OPT} ${JAVA_PARAMETER_OPT} -jar ${BASE_PATH}/${APPLICATION_JAR}\n"

nohup java ${JAVA_OPT} ${JAVA_GC} ${JAVA_MEM_OPT} ${JAVA_PARAMETER_OPT} -jar ${BASE_PATH}/${APPLICATION_JAR} >${LOG_DIR}/zipkin.log &

PID=$(ps -ef | grep "${APPLICATION_JAR}" | grep -v grep | awk '{ print $2 }')
STARTUP_LOG="${STARTUP_LOG}application pid: ${PID}\n"

echo -e ${STARTUP_LOG}
