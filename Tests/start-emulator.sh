#!/bin/sh
#
/home/andreas/opt/android-sdk-linux/tools/emulator -avd Device -netspeed full -netdelay none -no-boot-anim &
adb -s emulator-5554 install -r /home/andreas/IdeaProjects/SkyLinesTracker/SkyLinesTracker.apk
adb push /home/andreas/IdeaProjects/SkyLinesTracker/Tests/ch.luethi.skylinestracker_preferences.xml data/data/ch.luethi.skylinestracker/shared_prefs/
adb -s emulator-5554 shell ps
export ip=$(hostname -I | awk '{print $1}')
adb shell input keyevent 82
adb -s emulator-5554 shell am start -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP ${ip}
python /home/andreas/IdeaProjects/SkyLinesTracker/Tests/integration.py