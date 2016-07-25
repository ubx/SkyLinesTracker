#!/bin/bash
#
EMULATOR_DIR="/home/andreas/opt/android-sdk-linux/tools"
PROJECT_DIR="/home/andreas/IdeaProjects/SkyLinesTracker"
TEST_DIR="/home/andreas/IdeaProjects/SkyLinesTracker/Tests"
IP=$(hostname -I | awk '{print $1}')
INT=2

#
cd $TEST_DIR

KEY="ABCD1234"
python preference_file.py $KEY $INT  "false"  "true" $IP "false"

$EMULATOR_DIR/emulator -avd Device -netspeed full -netdelay none -no-boot-anim &
sleep 30
adb push ch.luethi.skylinestracker_preferences.xml data/data/ch.luethi.skylinestracker/shared_prefs/
adb -s emulator-5554 install -r $PROJECT_DIR/out/SkyLinesTracker.apk


###adb -s emulator-5554 shell ps

###nc -l -u 5597 | xxd -b &

adb -s emulator-5554 shell input keyevent 82
adb -s emulator-5554 shell am start -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP $IP
##java -jar UDP-Receiver.jar -br &
python gps_simulator.py $KEY
### echo 'pos={11,22,530} motion={20,120,3} turnrate=10 tzdiff=0' | nc ktrax-sim.kisstech.ch 48888 | nc localhost 5554


###sleep 1000000

adb -s emulator-5554 shell am force-stop ch.luethi.skylinestracker
adb -s emulator-5554 emu kill

sleep 10

#####################################################################################################################33
KEY="ABCD5678"
python preference_file.py $KEY  $INT  "false"  "true" $IP "true"
$EMULATOR_DIR/emulator -avd Device -netspeed full -netdelay none -no-boot-anim &
sleep 30
adb push ch.luethi.skylinestracker_preferences.xml data/data/ch.luethi.skylinestracker/shared_prefs/
adb -s emulator-5554 install -r $PROJECT_DIR/SkyLinesTracker.apk

adb -s emulator-5554 shell input keyevent 82
adb -s emulator-5554 shell am start -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP $IP
python gps_simulator.py $KEY



adb -s emulator-5554 shell am force-stop ch.luethi.skylinestracker

