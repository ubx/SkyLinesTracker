#!/usr/bin/env bash


TARGET=${4:-AVD}
EMULATOR_DIR="/home/andreas/opt/android-sdk-linux2/tools"

case $TARGET in
AVD)
    ${EMULATOR_DIR}/emulator -avd Device -netspeed full -netdelay none -no-boot-anim -gpu off &
    sleep 30
    adb -s $2 push ch.luethi.skylinestracker_preferences.xml /data/data/ch.luethi.skylinestracker/shared_prefs/
    adb -s $2 install -r  $1/out/SkyLinesTracker.apk
    adb -s $2 shell am start -W -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP $3
    adb -s $2 shell ls -l  /data/data/ch.luethi.skylinestracker/shared_prefs/ch.luethi.skylinestracker_preferences.xml
    adb -s $2 shell setprop persist.sys.timezone UTC
    adb -s $2 shell svc data enable
    ;;
S3)
    adb -s $2 install -r  $1/out/SkyLinesTracker.apk
    adb -s $2 push ch.luethi.skylinestracker_preferences.xml /data/data/ch.luethi.skylinestracker/shared_prefs/
    sleep 15
    adb -s $2 shell am start -W -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP $3
    adb -s $2 shell ls -l  /data/data/ch.luethi.skylinestracker/shared_prefs/ch.luethi.skylinestracker_preferences.xml
    adb -s $2 shell setprop persist.sys.timezone UTC
    ;;

*)
    echo "Unknown TARGET ${TARGET}"
esac