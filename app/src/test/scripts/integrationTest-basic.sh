#!/bin/bash
#
echo `pwd`

### todo -- better way
cd /home/andreas/IdeaProjects/SkyLinesTracker4/app/src/test/scripts/
echo `pwd`

source `pwd`helper.sh

trap "pkill -f UDP-Receiver.jar; exit" INT TERM EXIT

python preference_file.py ${KEY} ${INT}  false  false ${IP} true 2048

sh startEmulator.sh ${PROJECT_DIR} ${DEVICE} ${IP} ADV

echo "### $(date +"%T") GPS simmluation, LiveTracking NOT checked"
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test-00.out &
python gps_simulator.py 127.0.0.1 1200 ${KEY} ADV > sim-test.out &
sleep 60
pkill -f UDP-Receiver.jar

echo "### $(date +"%T") GPS simmluation, LiveTracking checked"
sh clickLiveTracking.sh ${DEVICE} ADV
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test-01.out &
sleep 60
pkill -f UDP-Receiver.jar

echo "### $(date +"%T") GPS simmluation, LiveTracking NOT checked again"
sh clickLiveTracking.sh ${DEVICE} ADV
java -jar ${TEST_DIR}/UDP-Receiver.jar -br > rcv-test-02.out &
sleep 60
pkill -f UDP-Receiver.jar


sleep 15
pkill -f gps_simulator.py

echo "#### $(date +"%T") Shuting down everting....................."
shutdown