#!/bin/bash
#
EMULATOR_DIR="/home/andreas/opt/android-sdk-linux/tools"
DEVICE="emulator-5554"
PROJECT_DIR="/home/andreas/IdeaProjects/SkyLinesTracker"
TEST_DIR="/home/andreas/IdeaProjects/SkyLinesTracker/Tests"
IP=$(hostname -I | awk '{print $1}')
INT=2
KEY="ABCD1234"

cd ${TEST_DIR}/scripts
rm -rf sim-test*.out
rm -rf rcv-test*.out
pkill -f UDP-Receiver.jar

trap "pkill -f UDP-Receiver.jar; exit" INT TERM EXIT

python preference_file.py ${KEY} ${INT}  false  true ${IP} true 2048

sh startEmulator.sh ${PROJECT_DIR} ${DEVICE} ${IP}

sh clickLiveTracking.sh ${DEVICE}

echo "### $(date +"%T") GPS simmluation, with Internet connection"
adb -s ${DEVICE} shell svc data enable
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test.out &
python gps_simulator.py 127.0.0.1 1200 ${KEY} > sim-test.out &
sleep 60

echo "### $(date +"%T") Simulate PositionService restart"
adb -s ${DEVICE} shell am stopservice ch.luethi.skylinestracker/.PositionService
adb -s ${DEVICE} shell am startservice ch.luethi.skylinestracker/.PositionService

echo "### $(date +"%T") Simulate network connection loss with positionService restart"
adb -s ${DEVICE} shell svc data disable
sleep 60
adb -s ${DEVICE} shell am stopservice ch.luethi.skylinestracker/.PositionService
adb -s ${DEVICE} shell am startservice ch.luethi.skylinestracker/.PositionService
sleep 30
adb -s ${DEVICE} shell svc data enable
sleep 30

echo "### $(date +"%T") Simulate network connection loss"
adb -s ${DEVICE} shell svc data disable
sleep 180
adb -s ${DEVICE} shell svc data enable
sleep 30

echo "### $(date +"%T") Simulate network connection loss with PositionService restart"
adb -s ${DEVICE} shell svc data disable
sleep 260
adb -s ${DEVICE} shell am stopservice ch.luethi.skylinestracker/.PositionService
adb -s ${DEVICE} shell am startservice ch.luethi.skylinestracker/.PositionService
adb -s ${DEVICE} shell svc data enable

sleep 600
pkill -f UDP-Receiver.jar
pkill -f gps_simulator.py

echo "#### $(date +"%T") Shuting down everting....................."
adb -s ${DEVICE} shell am force-stop ch.luethi.skylinestracker
adb -s ${DEVICE} emu kill
pkill -f qemu-system-x86_64