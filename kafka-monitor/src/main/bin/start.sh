#!/bin/bash
JAR_NAME="kafka-monitor"
Xms=2G
Xmx=2G
Xmn=1G
COMMON_LOG_DIR="/wyyt/logs/dubbo/$JAR_NAME"
if [ ! -d $COMMON_LOG_DIR ]; then
  mkdir -p $COMMON_LOG_DIR
fi
LOGS_HEAPDUMP=$COMMON_LOG_DIR/heapdump
if [ ! -d $LOGS_HEAPDUMP ]; then
  mkdir -p $LOGS_HEAPDUMP
fi
cd $(dirname $0) || exit 1
cd ..
DEPLOY_DIR=$(pwd)
LIB_DIR=$DEPLOY_DIR/lib
CONFIG_DIR=$DEPLOY_DIR/config
JAR_FULL_NAME=
files=$(ls $LIB_DIR)
for filename in $files; do
  if [[ "$filename" =~ ^${JAR_NAME}-.* ]]; then
    JAR_FULL_NAME=$filename
    break
  fi
done
jarName=${JAR_FULL_NAME/.jar/}
VERSION=${jarName##*-}
JAR_DIR=$LIB_DIR/$JAR_FULL_NAME

PID=$(ps -ef | grep $JAR_FULL_NAME | grep -v grep | awk '{print $2}')

if [ "$1" = "status" ]; then
  if [ -n "$PID" ]; then
    echo "The $JAR_NAME is running...! PID: $PID"
    exit 0
  else
    echo "The $JAR_NAME is stopped"
    exit 0
  fi
fi

if [ -n "${PID}" ]; then
  echo "ERROR: The $JAR_NAME already started! PID: $PID"
  exit 1
fi

JAVA_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "
JAVA_GC="-Xloggc:$COMMON_LOG_DIR/$JAR_NAME-gc.log -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=10M "

JAVA_MEM_OPTS="-server -Xms${Xms} -Xmx${Xmx} -Xmn${Xmn} -XX:NewRatio=1 -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 "
JAVA_MEM_OPTS="$JAVA_MEM_OPTS -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:MaxDirectMemorySize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$LOGS_HEAPDUMP/ "
JAVA_PARAMETER_OPTS="-Dversion=$VERSION -Dwork.dir=$DEPLOY_DIR"
JAVA_YML_CONFIG="--spring.config.location=$CONFIG_DIR/application.yml"

echo "java $JAVA_OPTS $JAVA_GC $JAVA_MEM_OPTS $JAVA_PARAMETER_OPTS -jar $JAR_DIR $JAVA_YML_CONFIG"
nohup java $JAVA_OPTS $JAVA_GC $JAVA_MEM_OPTS $JAVA_PARAMETER_OPTS -jar $JAR_DIR $JAVA_YML_CONFIG >/dev/null 2>&1 &
echo "Starting the $JAR_NAME"

COUNT=0
while [ $COUNT -lt 1 ]; do
  echo -e ".\c"
  sleep 1
  COUNT=$(ps -ef | grep $JAR_FULL_NAME | grep -v grep | awk '{print $2}' | wc -l)
  if [ $COUNT -gt 0 ]; then
    break
  fi
done

echo -e "\nStart $JAR_NAME successed with PID=$!"
