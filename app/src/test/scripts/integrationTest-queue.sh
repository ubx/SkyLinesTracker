#!/usr/bin/env bash
#

source `pwd`/Tests/scripts/helper.sh

trap "pkill -f UDP-Receiver.jar; exit" INT TERM EXIT

python preference_file.py ${KEY} ${INT}  false  true ${IP} true 2048

sh startEmulator.sh ${PROJECT_DIR} ${DEVICE} ${IP} genymotion

sh clickLiveTracking.sh ${DEVICE} genymotion

echo "### $(date +"%T") GPS simmluation, with Internet connection"
set_internet_connection enable

java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test.out &
python gps_simulator.py 127.0.0.1 1200 ${KEY} ADV > sim-test.out &
sleep 60

echo "### $(date +"%T") Simulate PositionService restart"
$adb -s ${DEVICE} shell am stopservice ch.luethi.skylinestracker/.PositionService
$adb -s ${DEVICE} shell am startservice ch.luethi.skylinestracker/.PositionService

echo "### $(date +"%T") Simulate network connection loss with positionService restart"
set_internet_connection disable

sleep 60
$adb -s ${DEVICE} shell am stopservice ch.luethi.skylinestracker/.PositionService
$adb -s ${DEVICE} shell am startservice ch.luethi.skylinestracker/.PositionService
sleep 30
set_internet_connection enable

sleep 30

echo "### $(date +"%T") Simulate network connection loss"
set_internet_connection disable

sleep 180
set_internet_connection enable

sleep 30

echo "### $(date +"%T") Simulate network connection loss with PositionService restart"
set_internet_connection disable

sleep 260
$adb -s ${DEVICE} shell am stopservice ch.luethi.skylinestracker/.PositionService
$adb -s ${DEVICE} shell am startservice ch.luethi.skylinestracker/.PositionService
set_internet_connection enable


sleep 600
pkill -f UDP-Receiver.jar
pkill -f gps_simulator.py

echo "#### $(date +"%T") Shuting down everting....................."
shutdown