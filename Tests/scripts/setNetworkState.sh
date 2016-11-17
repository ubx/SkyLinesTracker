#!/usr/bin/env bash

TARGET=${3:-AVD}
STATE=$2

if [ "$STATE" = "ON" ]; then
    ENABLE="enable"
    AP_MODE="0"
    EZ="false"
else
    ENABLE="disable"
    AP_MODE="1"
    EZ="true"
fi

case $TARGET in
AVD)
    adb -s $1 shell svc data $ENABLE
    ;;
S3)
    adb -s $1 shell settings put global airplane_mode_on $AP_MODE
    adb -s $1 shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state $EZ
    ;;

*)
    echo "Unknown TARGET ${TARGET}"
esac