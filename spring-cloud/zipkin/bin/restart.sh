#!/bin/bash

APPLICATION="zipkin-server"
APPLICATION_JAR="zipkin.jar"

echo stop ${APPLICATION} Application...
sh shutdown.sh

echo start ${APPLICATION} Application...
sh startup.sh