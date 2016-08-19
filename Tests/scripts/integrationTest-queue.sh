#!/bin/bash
#
EMULATOR_DIR="/home/andreas/opt/android-sdk-linux/tools"
PROJECT_DIR="/home/andreas/IdeaProjects/SkyLinesTracker"
TEST_DIR="/home/andreas/IdeaProjects/SkyLinesTracker/Tests"
IP=$(hostname -I | awk '{print $1}')
INT=2
KEY="ABCD1234"

cd ${TEST_DIR}/scripts
rm -rf sim-test-*.out
rm -rf rcv-test-*.out
pkill -f UDP-Receiver.jar

trap "pkill -f UDP-Receiver.jar; exit" INT TERM EXIT

${EMULATOR_DIR}/emulator -avd Device -netspeed full -netdelay none -no-boot-anim &

sleep 15
python preference_file.py ${KEY} ${INT}  false  true ${IP} true 2048

adb -s emulator-5554 push ch.luethi.skylinestracker_preferences.xml /data/data/ch.luethi.skylinestracker/shared_prefs/
adb -s emulator-5554 install -r  ${PROJECT_DIR}/out/SkyLinesTracker.apk
#adb -s emulator-5554 shell dumpsys batterystats --reset
#adb -s emulator-5554 shell dumpsys battery set ac 0
#adb -s emulator-5554 shell dumpsys battery set level 80
#adb -s emulator-5554 shell dumpsys battery

sleep 15
adb -s emulator-5554 shell am start -W -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP ${IP}

sh clickLiveTracking.sh

adb -s emulator-5554 shell ls -l  /data/data/ch.luethi.skylinestracker/shared_prefs/ch.luethi.skylinestracker_preferences.xml

echo "### $(date +"%T") GPS simmluation, with Internet connection"
adb -s emulator-5554 shell svc data enable
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test-02.out &
python gps_simulator.py 1200 ${KEY} > sim-test-02.out &
sleep 60

echo "### $(date +"%T") Simulate PositionService restart"
adb -s emulator-5554 shell am stopservice ch.luethi.skylinestracker/.PositionService
adb -s emulator-5554 shell am startservice ch.luethi.skylinestracker/.PositionService

echo "### $(date +"%T") Simulate network connection loss"
adb -s emulator-5554 shell svc data disable
sleep 180
adb -s emulator-5554 shell svc data enable
sleep 30

echo "### $(date +"%T") Simulate network connection loss with PositionService restart"
adb -s emulator-5554 shell svc data disable
sleep 260
adb -s emulator-5554 shell am stopservice ch.luethi.skylinestracker/.PositionService
adb -s emulator-5554 shell am startservice ch.luethi.skylinestracker/.PositionService
adb -s emulator-5554 shell svc data enable

sleep 3700
pkill -f UDP-Receiver.jar
pkill -f gps_simulator.py

echo "#### $(date +"%T") Dumpsys batterystats and bugreport"
#adb -s emulator-5554 shell  dumpsys batterystats > batterystats.txt
#adb -s emulator-5554 shell  bugreport > bugreport.txt


echo "#### $(date +"%T") Shuting down everting....................."
adb -s emulator-5554 shell am force-stop ch.luethi.skylinestracker
adb -s emulator-5554 emu kill
pkill -f qemu-system-x86_64
exit