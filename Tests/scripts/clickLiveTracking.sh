#!/usr/bin/env bash

adb -s emulator-5554 shell sendevent /dev/input/event1 3  57   0
adb -s emulator-5554 shell sendevent /dev/input/event1 3  48   1
adb -s emulator-5554 shell sendevent /dev/input/event1 3  58   129
adb -s emulator-5554 shell sendevent /dev/input/event1 3  53   3693
adb -s emulator-5554 shell sendevent /dev/input/event1 3  54   8735
adb -s emulator-5554 shell sendevent /dev/input/event1 3   0   0
adb -s emulator-5554 shell sendevent /dev/input/event1 3  53   3762
adb -s emulator-5554 shell sendevent /dev/input/event1 3   0   0
adb -s emulator-5554 shell sendevent /dev/input/event1 3  53   3830
adb -s emulator-5554 shell sendevent /dev/input/event1 0   0   0
adb -s emulator-5554 shell sendevent /dev/input/event1 3  53   3899
adb -s emulator-5554 shell sendevent /dev/input/event1 0   0   0
adb -s emulator-5554 shell sendevent /dev/input/event1 3  57   4294967295
adb -s emulator-5554 shell sendevent /dev/input/event1 3  58   0
adb -s emulator-5554 shell sendevent /dev/input/event1 0   0   0

