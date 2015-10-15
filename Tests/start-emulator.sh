#!/bin/bash
#
/home/andreas/opt/android-sdk-linux/tools/emulator -avd Device -netspeed full -netdelay none -no-boot-anim &
adb -s emulator-5554 install -r /home/andreas/IdeaProjects/SkyLinesTracker/SkyLinesTracker.apk
adb push /home/andreas/IdeaProjects/SkyLinesTracker/Tests/ch.luethi.skylinestracker_preferences.xml data/data/ch.luethi.skylinestracker/shared_prefs/
adb -s emulator-5554 shell ps

###nc -l -u 5597 | xxd -b &

adb shell input keyevent 82
export ip=$(hostname -I | awk '{print $1}')
adb -s emulator-5554 shell am start -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP ${ip}
####python /home/andreas/IdeaProjects/SkyLinesTracker/Tests/integration.py
echo 'pos={11,22,530} motion={20,120,3} turnrate=10 tzdiff=0' | nc ktrax-sim.kisstech.ch 48888 | nc localhost 5554
