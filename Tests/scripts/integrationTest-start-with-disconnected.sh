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

sh clickLiveTracking.sh ${DEVICE}

echo "### $(date +"%T") GPS simmluation, LiveTracking checked, NO internet connection"
adb -s ${DEVICE} shell svc data disable
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test.out &
python gps_simulator.py 127.0.0.1 9999 ${KEY} > sim-test-0.out &
sleep 60

echo "### $(date +"%T") Switch ON internet connection"
adb -s ${DEVICE} shell svc data enable
sleep 100

echo "### $(date +"%T") GPS simmluation, NO internet connection"
adb -s ${DEVICE} shell svc data disable
sleep 100

echo "### $(date +"%T") No GPS simmluation, internet connection"
pkill -f gps_simulator.py
adb -s ${DEVICE} shell svc data enable
sleep 100

echo "### $(date +"%T") GPS simmluation, internet connection"
python gps_simulator.py 127.0.0.1 9999 ${KEY} > sim-test-1.out &
sleep 100

pkill -f gps_simulator.py
sleep 100

pkill -f UDP-Receiver.jar
sleep 20

cat sim-test-0.out sim-test-1.out > sim-test.out

echo "#### $(date +"%T") Shuting down everting....................."
adb -s ${DEVICE} shell am force-stop ch.luethi.skylinestracker
adb -s ${DEVICE} emu kill
pkill -f qemu-system-x86_64