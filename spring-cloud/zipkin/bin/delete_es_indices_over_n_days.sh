#!/bin/bash
#脚本的日志文件路径
CLEAN_LOG="./clean_index.log"
#es地址,多个地址用逗号分隔
SERVER_PORTS="http://192.168.6.165:9900,http://192.168.6.166:9900,http://192.168.6.167:9900"
#es账号
ES_UID="elastic"
#es密码
ES_PWD="50VMZ3EUJGU5rQCMcPCJ"
#保留的时间，单位天
DELTIME=7
#删除的历史索引名称前缀
PREFIX_IDX_NAME="zipkin-span-"

echo "-------------------$(date +%F_%T)---------------"  >>${CLEAN_LOG}

DAYS_AGO=$(($DELTIME - 1))
DATE=`date -d "${DAYS_AGO} days ago" +%Y-%m-%d`
t1=`date -d "$DATE" +%s`
OLD_IFS="$IFS"
IFS=","
arr=($SERVER_PORTS)
IFS="$OLD_IFS"
for SERVER_PORT in ${arr[@]}
do

  INDEXS=$(curl -s -u  ${ES_UID}:${ES_PWD}  "${SERVER_PORT}/_cat/indices?v"|awk '{print $3}')

  for del_index in ${INDEXS}
  do
     if [[ $del_index == $PREFIX_IDX_NAME* ]];then
       timeString=${del_index//${PREFIX_IDX_NAME}/}
       t2=`date -d "$timeString" +%s`

       if [ $t1 -gt $t2 ]; then
           delResult=`curl -s -u ${ES_UID}:${ES_PWD}  -XDELETE "${SERVER_PORT}/"${del_index}"?pretty" |sed -n '2p'`
           echo "delete index:$del_index result:$delResult" >>${CLEAN_LOG}
       fi
     fi
  done

done

