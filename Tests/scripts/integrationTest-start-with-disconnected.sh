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

${EMULATOR_DIR}/emulator -avd Device -netspeed full -netdelay none -no-boot-anim &

sleep 15
python preference_file.py ${KEY} ${INT}  false  false ${IP} true 2048

##adb -s ${DEVICE} uninstall ch.luethi.skylinestracker
adb -s ${DEVICE} push ch.luethi.skylinestracker_preferences.xml /data/data/ch.luethi.skylinestracker/shared_prefs/
adb -s ${DEVICE} install -r  ${PROJECT_DIR}/out/SkyLinesTracker.apk
sleep 15

adb -s ${DEVICE} shell am start -W -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP ${IP}
adb -s ${DEVICE} shell svc data enable
adb -s ${DEVICE} shell ls -l  /data/data/ch.luethi.skylinestracker/shared_prefs/ch.luethi.skylinestracker_preferences.xml

sleep 15
sh clickLiveTracking.sh ${DEVICE}
sleep 15

echo "### $(date +"%T") GPS simmluation, LiveTracking checked, NO internet connection"
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test.out &
python gps_simulator.py 127.0.0.1 200 ${KEY} > sim-test.out &
sleep 60

echo "### $(date +"%T") Switch ON internet connection"
adb -s ${DEVICE} shell svc data enable
sleep 200

pkill -f UDP-Receiver.jar
pkill -f gps_simulator.py
sleep 30

echo "#### $(date +"%T") Shuting down everting....................."
adb -s ${DEVICE} shell am force-stop ch.luethi.skylinestracker
adb -s ${DEVICE} emu kill
pkill -f qemu-system-x86_64