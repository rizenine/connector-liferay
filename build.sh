#!/bin/bash
#

SERVER_PATH=/home/justin/Desktop/Projects/midpoint-3.9
BUILD_PATH=/home/justin/Desktop/Projects/connector-liferay

cd $BUILD_PATH

mvn clean
mvn package

cp $BUILD_PATH/target/connector-liferay-1.0.jar $SERVER_PATH/var/icf-connectors/

$SERVER_PATH/bin/stop.sh
$SERVER_PATH/bin/start.sh

tail -f $SERVER_PATH/var/log/*.log
