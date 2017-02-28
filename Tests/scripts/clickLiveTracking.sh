#!/usr/bin/env bash

TARGET=${2:-AVD}

case $TARGET in
AVD)
    adb -s $1 shell sendevent /dev/input/event1 3 57   0
    adb -s $1 shell sendevent /dev/input/event1 3 48   50
    adb -s $1 shell sendevent /dev/input/event1 3 58   129
    adb -s $1 shell sendevent /dev/input/event1 3 53   15186
    adb -s $1 shell sendevent /dev/input/event1 3 54   6438
    adb -s $1 shell sendevent /dev/input/event1 0 0    0
    adb -s $1 shell sendevent /dev/input/event1 3 58   0
    adb -s $1 shell sendevent /dev/input/event1 3 57   4294967295
    adb -s $1 shell sendevent /dev/input/event1 0 0    0
    sleep 15
    ;;
S3)
    adb -s $1 shell input tap 405 192
    sleep 5
    ;;

*)
    echo "Unknown TARGET ${TARGET}"
esac