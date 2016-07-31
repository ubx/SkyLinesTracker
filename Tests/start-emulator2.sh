#!/bin/bash
#
EMULATOR_DIR="/home/andreas/opt/android-sdk-linux/tools"
PROJECT_DIR="/home/andreas/IdeaProjects/SkyLinesTracker"
TEST_DIR="/home/andreas/IdeaProjects/SkyLinesTracker/Tests"
IP=$(hostname -I | awk '{print $1}')
INT=2
KEY="ABCD1234"

cd $TEST_DIR

python preference_file.py $KEY $INT  "false"  "true" $IP "true"

$EMULATOR_DIR/emulator -avd Device -netspeed full -netdelay none -no-boot-anim &
sleep 30
adb push ch.luethi.skylinestracker_preferences.xml data/data/ch.luethi.skylinestracker/shared_prefs/
adb -s emulator-5554 install -r $PROJECT_DIR/out/SkyLinesTracker.apk
java -jar UDP-Receiver.jar -br &
trap "pkill -f UDP-Receiver.jar; exit" INT TERM EXIT

adb -s emulator-5554 shell am start -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP $IP
adb -s emulator-5554 shell input keyevent 82
read -p "On emulator check 'Live Tracking' and hit return" ok

echo "### $(date +"%T") Normal GPS simluation"
adb -s emulator-5554 shell svc data enable
python gps_simulator.py 60 $KEY

echo "### $(date +"%T") GPS simluation, not Internet connection"
adb -s emulator-5554 shell svc data disable
python gps_simulator.py 60 $KEY

echo "### $(date +"%T") GPS simluation, reconnect Internet"
adb -s emulator-5554 shell svc data enable
python gps_simulator.py 30 $KEY

echo "### $(date +"%T") GPS simluation, not Internet connection"
adb -s emulator-5554 shell svc data disable
python gps_simulator.py 60 $KEY

echo "### $(date +"%T") NO GPS simluation, reconnect Internet"
adb -s emulator-5554 shell svc data enable

sleep 600

echo "### $(date +"%T") Normal GPS simluation"
adb -s emulator-5554 shell svc data enable
python gps_simulator.py 30 $KEY

echo "#### $(date +"%T") Shuting down everting....................."
pkill -f UDP-Receiver.jar
adb -s emulator-5554 shell am force-stop ch.luethi.skylinestracker
adb -s emulator-5554 emu kill
