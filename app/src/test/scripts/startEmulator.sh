#!/usr/bin/env bash
adb=/home/andreas/opt/android-sdk-linux/platform-tools/adb

TARGET=${4:-AVD}
EMULATOR_DIR="/home/andreas/opt/android-sdk-linux/emulator"
ADV_DEVICE="Device-API-28"

case $TARGET in
AVD)
    ${EMULATOR_DIR}/emulator -avd ${ADV_DEVICE}  -netspeed full -netdelay none &
    sleep 30
    $adb -s $2 root
    $adb -s $2 install -r com.blogspot.newapphorizons.fakegps_2018-12-04.apk
    $adb -s $2 push ch.luethi.skylinestracker_preferences.xml /data/data/ch.luethi.skylinestracker/shared_prefs/
    $adb -s $2 install -r  $1/app/build/outputs/apk/debug/SkyLinesTracker-debug.apk
    $adb -s $2 shell am start -W -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP $3
    $adb -s $2 shell ls -l  /data/data/ch.luethi.skylinestracker/shared_prefs/ch.luethi.skylinestracker_preferences.xml
    $adb -s $2 shell setprop persist.sys.timezone UTC
    $adb -s $2 shell svc data enable
    ;;
S3)
    $adb -s $2 install -r  $1/out/SkyLinesTracker.apk
    $adb -s $2 push ch.luethi.skylinestracker_preferences.xml /data/data/ch.luethi.skylinestracker/shared_prefs/
    sleep 15
    $adb -s $2 shell am start -W -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP $3
    $adb -s $2 shell ls -l  /data/data/ch.luethi.skylinestracker/shared_prefs/ch.luethi.skylinestracker_preferences.xml
    $adb -s $2 shell setprop persist.sys.timezone UTC
    ;;
genymotion)
    $adb -s $2 push ch.luethi.skylinestracker_preferences.xml /data/data/ch.luethi.skylinestracker/shared_prefs/
    $adb -s $2 install -r  $1/out/SkyLinesTracker.apk
    $adb -s $2 shell am start -W -n ch.luethi.skylinestracker/ch.luethi.skylinestracker.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -e ISTESTING true -e TESTING_IP $3
    $adb -s $2 shell ls -l  /data/data/ch.luethi.skylinestracker/shared_prefs/ch.luethi.skylinestracker_preferences.xml
    $adb -s $2 shell setprop persist.sys.timezone UTC
    $adb -s $2 shell svc data enable
    ;;

*)
    echo "Unknown TARGET ${TARGET}"
esac