#!/bin/bash
#
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

python preference_file.py ${KEY} ${INT}  false  false ${IP} true 2048

sh startEmulator.sh ${PROJECT_DIR} ${DEVICE} ${IP}

echo "### $(date +"%T") GPS simmluation, LiveTracking NOT checked"
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test-00.out &
python gps_simulator.py 127.0.0.1 1200 ${KEY} TEL > sim-test.out &
sleep 60
pkill -f UDP-Receiver.jar

echo "### $(date +"%T") GPS simmluation, LiveTracking checked"
sh clickLiveTracking.sh ${DEVICE}
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test-01.out &
sleep 60
pkill -f UDP-Receiver.jar

echo "### $(date +"%T") GPS simmluation, LiveTracking NOT checked again"
sh clickLiveTracking.sh ${DEVICE}
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test-02.out &
sleep 60
pkill -f UDP-Receiver.jar


sleep 15
pkill -f gps_simulator.py

echo "#### $(date +"%T") Shuting down everting....................."
adb -s ${DEVICE} shell am force-stop ch.luethi.skylinestracker
adb -s ${DEVICE} emu kill
sh stopEmulator.sh