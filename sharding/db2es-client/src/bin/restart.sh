#!/bin/bash

APPLICATION="@project.name@"
APPLICATION_JAR="@build.finalName@.jar"

echo stop ${APPLICATION} Application...
sh stop.sh

echo start ${APPLICATION} Application...
sh start.sh
