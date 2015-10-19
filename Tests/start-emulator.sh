#!/bin/bash
#
EMULATOR_DIR="/home/andreas/opt/android-sdk-linux/tools"
PROJECT_DIR="/home/andreas/IdeaProjects/SkyLinesTracker"
TEST_DIR="/home/andreas/IdeaProjects/SkyLinesTracker/Tests"
#
$EMULATOR_DIR/emulator -avd Device -netspeed full -netdelay none -no-boot-anim &
adb -s emulator-5554 install -r $PROJECT_DIR/SkyLinesTracker.apk
adb push $TEST_DIR/ch.luethi.skylinestracker_preferences.xml data/data/ch.luethi.skylinestracker/shared_prefs/
adb -s emulator-5554 shell ps

###nc -l -u 5597 | xxd -b &

adb shell input keyevent 82
export ip=$(hostname -I | awk '{print $1}')
adb -s emulator-5554 shell am start -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP ${ip}
####python $TEST_DIR/integration.py
echo 'pos={11,22,530} motion={20,120,3} turnrate=10 tzdiff=0' | nc ktrax-sim.kisstech.ch 48888 | nc localhost 5554
