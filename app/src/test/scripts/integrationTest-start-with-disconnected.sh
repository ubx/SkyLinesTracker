#!/bin/bash
#
echo `pwd`

source `pwd`/helper.sh

trap "pkill -f UDP-Receiver.jar; exit" INT TERM EXIT

python preference_file.py ${KEY} ${INT} false ${IP} true 2048

sh startEmulator.sh ${PROJECT_DIR} ${DEVICE} ${IP} AVD

sh clickLiveTracking.sh ${DEVICE} AVD

echo "### $(date +"%T") GPS simmluation, LiveTracking checked, NO internet connection"
set_internet_connection disable
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test.out &
python gps_simulator.py 127.0.0.1 9999 ${KEY} xxx > sim-test-0.out &
sleep 60

echo "### $(date +"%T") Switch ON internet connection"
set_internet_connection enable
sleep 100

echo "### $(date +"%T") GPS simmluation, NO internet connection"
set_internet_connection disable
sleep 100

echo "### $(date +"%T") No GPS simmluation, internet connection"
pkill -f gps_simulator.py
set_internet_connection enable
sleep 100

echo "### $(date +"%T") GPS simmluation, internet connection"
python gps_simulator.py 127.0.0.1 9999 ${KEY} AVD > sim-test-1.out &
sleep 100

pkill -f gps_simulator.py
sleep 100

pkill -f UDP-Receiver.jar
sleep 20

cat sim-test-0.out sim-test-1.out > sim-test.out

echo "#### $(date +"%T") Shutting down everything....................."
shutdown