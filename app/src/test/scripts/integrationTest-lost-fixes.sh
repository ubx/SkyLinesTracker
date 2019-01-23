#!/bin/bash
#
echo `pwd`

source `pwd`/helper.sh

trap "pkill -f UDP-Receiver.jar; exit" INT TERM EXIT

python preference_file.py ${KEY} ${INT} false ${IP} true 50

sh startEmulator.sh ${PROJECT_DIR} ${DEVICE} ${IP} AVD

sh clickLiveTracking.sh ${DEVICE} AVD

echo "### $(date +"%T") GPS simmluation, LiveTracking checked, NO internet connection"
$adb -s ${DEVICE} shell svc data disable
$adb -s ${DEVICE} shell svc wifi disable
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test.out &
python gps_simulator.py 127.0.0.1 400 ${KEY} AVD > sim-test.out &
sleep 100

echo "### $(date +"%T") Switch ON internet connection after 100 seconds"
$adb -s ${DEVICE} shell svc data enable
$adb -s ${DEVICE} shell svc wifi enable
sleep 30

pkill -f UDP-Receiver.jar
sleep 400
pkill -f gps_simulator.py
sleep 30

echo "#### $(date +"%T") Shutting down everything....................."
$adb -s ${DEVICE} shell am force-stop ch.luethi.skylinestracker
$adb -s ${DEVICE} emu kill
sh stopEmulator.sh