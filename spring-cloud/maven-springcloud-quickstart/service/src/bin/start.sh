#!/bin/bash
# 修改成Runnable/Callable对象所在的扫描目录, 该包下的Runnable/Callable对象都会被装饰, 最好精细和准确, 减少被装饰的对象数，提高性能,多个用';'分隔
THREAD_PACKAGES="@project.groupId@"
Xms=2G
Xmx=2G
Xmn=1G
APPLICATION="@project.name@"
APPLICATION_JAR="@build.finalName@.jar"
VERSION="@project.version@"
LOG_DIR="/wyyt/logs/tomcat/springcloud/${APPLICATION}"
LOGS_HEAPDUMP="${LOG_DIR}/heapdump"
LOGS_GC="${LOG_DIR}/gc"

cd $(dirname $0)
cd ..
BASE_PATH=$(pwd)
CONFIG_DIR=${BASE_PATH}"/config/"
AGENT_DIR=${BASE_PATH}"/agent/"
AGENT_JAR=""
AGENT_JAR_VERSION=""
for fileName in $(ls ${AGENT_DIR}); do
  if [ ! -d ${fileName} ]; then
    if [ ${fileName##*.} = jar ]; then
      AGENT_JAR=$fileName
      AGENT_JAR_FROM=$(echo "$AGENT_JAR" | awk -F ''-'' '{printf "%d", length($0)-length($NF)}')
      AGENT_JAR_TO=$(echo "$AGENT_JAR" | awk -F ''.'' '{printf "%d", length($0)-length($NF)}')
      AGENT_JAR_SUB_LEN=$((AGENT_JAR_TO - AGENT_JAR_FROM - 1))
      AGENT_JAR_VERSION=${AGENT_JAR:$AGENT_JAR_FROM:$AGENT_JAR_SUB_LEN}
    fi
  fi
done

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

JAVA_AGENT=""
if [ -f "${AGENT_DIR}${AGENT_JAR}" ]; then
  JAVA_AGENT="-javaagent:${AGENT_DIR}${AGENT_JAR} "
  JAVA_AGENT="${JAVA_AGENT} -Dthread.scan.packages=\"${THREAD_PACKAGES}\""
fi

JAVA_OPT="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -XX:-OmitStackTraceInFastThrow "
JAVA_GC="-Xloggc:${LOGS_GC}/gc.log -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=10M "
JAVA_MEM_OPT="-server -Xms${Xms} -Xmx${Xmx} -Xmn${Xmn} -XX:NewRatio=1 -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:MaxDirectMemorySize=512m -XX:+HeapDumpOnOutOfMemoryError "
JAVA_MEM_OPT="${JAVA_MEM_OPT} -XX:HeapDumpPath=${LOGS_HEAPDUMP}/ "
JAVA_PARAMETER_OPT="-Dversion=${VERSION} -Dagent.jar.version=${AGENT_JAR_VERSION} -Dwork.dir=${BASE_PATH}"

STARTUP_LOG="${STARTUP_LOG}application name: ${APPLICATION}\n"
STARTUP_LOG="${STARTUP_LOG}application jar name: ${APPLICATION_JAR}\n"
STARTUP_LOG="${STARTUP_LOG}application jar version: ${VERSION}\n"
STARTUP_LOG="${STARTUP_LOG}application root path: ${BASE_PATH}\n"
STARTUP_LOG="${STARTUP_LOG}application config path: ${CONFIG_DIR}\n"
STARTUP_LOG="${STARTUP_LOG}application startup command: java ${JAVA_AGENT} ${JAVA_OPT} ${JAVA_GC} ${JAVA_MEM_OPT} ${JAVA_PARAMETER_OPT} -jar ${BASE_PATH}/boot/${APPLICATION_JAR} --spring.config.location=${CONFIG_DIR}\n"

nohup java ${JAVA_AGENT} ${JAVA_OPT} ${JAVA_GC} ${JAVA_MEM_OPT} ${JAVA_PARAMETER_OPT} -jar ${BASE_PATH}/boot/${APPLICATION_JAR} --spring.config.location=${CONFIG_DIR} >/dev/null 2>&1 &

PID=$(ps -ef | grep "${APPLICATION_JAR}" | grep -v grep | awk '{ print $2 }')
STARTUP_LOG="${STARTUP_LOG}application pid: ${PID}\n"

echo -e ${STARTUP_LOG}
