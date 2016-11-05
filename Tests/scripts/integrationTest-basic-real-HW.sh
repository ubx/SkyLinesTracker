#!/bin/bash
#
##DEVICE="0123456789ABCDEF" # Weiko
DEVICE="4df0279066905fc7" # S7

PROJECT_DIR="/home/andreas/IdeaProjects/SkyLinesTracker"
TEST_DIR="/home/andreas/IdeaProjects/SkyLinesTracker/Tests"
IP=$(hostname -I | awk '{print $1}')
#IP="ubx.internet-box.ch"
INT=5
KEY="67FCFE73"

cd ${TEST_DIR}/scripts
rm -rf sim-test*.out
rm -rf rcv-test*.out
pkill -f UDP-Receiver.jar
pkill -f gps_simulator.py

trap "pkill -f UDP-Receiver.jar; exit" INT TERM EXIT

python preference_file.py ${KEY} ${INT}  false  false ${IP} true 2048

adb -s ${DEVICE} install -r  ${PROJECT_DIR}/out/SkyLinesTracker.apk
adb -s ${DEVICE} push ch.luethi.skylinestracker_preferences.xml /data/data/ch.luethi.skylinestracker/shared_prefs/
sleep 15

adb -s ${DEVICE} shell am start -W -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP ${IP}
adb -s ${DEVICE} shell ls -l  /data/data/ch.luethi.skylinestracker/shared_prefs/ch.luethi.skylinestracker_preferences.xml
adb -s ${DEVICE} shell setprop persist.sys.timezone UTC

sleep 15

echo "### $(date +"%T") GPS simmluation, LiveTracking NOT checked"
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test-00.out &
python gps_simulator.py 127.0.0.1 1200 ${KEY} ADB > sim-test.out &
sleep 60
pkill -f UDP-Receiver.jar

echo "### $(date +"%T") GPS simmluation, LiveTracking checked"
sh clickLiveTracking-S7.sh ${DEVICE}
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test-01.out &
sleep 60
pkill -f UDP-Receiver.jar

echo "### $(date +"%T") GPS simmluation, LiveTracking NOT checked again"
sh clickLiveTracking-S7.sh ${DEVICE}
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test-02.out &
sleep 60
pkill -f UDP-Receiver.jar


sleep 15
pkill -f gps_simulator.py

echo "#### $(date +"%T") Shuting down everting....................."
adb -s ${DEVICE} shell am force-stop ch.luethi.skylinestracker
