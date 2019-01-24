import telnetlib
from time import sleep
import time
import random
import sys
import os

HOST = "127.0.0.1"
PORT = 5554
TIMEOUT = 10
LAT_SRC = 46.0000000
LAT_DST = 56.0000000
LNG_SRC =  7.0000000
LNG_DST = 10.0000000

HOST = sys.argv[-4]
SECONDS = int(sys.argv[-3])
KEY = sys.argv[-2]
TARGET = sys.argv[-1]

LAT_MAX_STEP = ((max(LAT_DST, LAT_SRC) - min(LAT_DST, LAT_SRC)) / SECONDS)
LNG_MAX_STEP = ((max(LNG_DST, LNG_SRC) - min(LNG_DST, LNG_SRC)) / SECONDS)

DIRECTION_LAT = 1 if LAT_DST - LAT_SRC > 0 else -1
DIRECTION_LNG = 1 if LNG_DST - LNG_SRC > 0 else -1

lat = LAT_SRC
lng = LNG_SRC

if TARGET != "ADV":
    tn = telnetlib.Telnet(HOST, PORT, TIMEOUT)
    tn.set_debuglevel(0)
    tn.read_until(b"OK", 5)


def millies_of_day():
    t = time.gmtime()
    mils_of_dat = (t.tm_sec + (t.tm_min * 60) + (t.tm_hour * 3600)) * 1000
    return mils_of_dat


def write_geo(lng, lat, alt):
    if TARGET == "ADV":
        os.system ("/home/andreas/opt/android-sdk-linux/platform-tools/adb shell am startservice --user 0 -a com.blogspot.newapphorizons.fakegps.UPDATE  -e longitude {0} -e latitude {1}".format(lng, lat))
    else:
        tn.write("geo fix {0} {1} {2}\n".format(lng, lat, alt).encode('ascii'))
    print(("Sim: {0},{1},{2:.5f},{3:.5f},{4}".format(millies_of_day(), KEY, lng, lat, alt)))
    sys.stdout.flush()


def write_gsm(mode):
    tn.write("gsm data " + mode + '\n')
    print(("Sim: gsm data " + mode + '\n'))


def write_sms(msg):
    tn.write("sms send 1234 slt " + msg + '\n')


def auth():
    if TARGET != "ADV":
        tn.write(b"auth 1dzok1p9Lo5UaJ9M" + b'\n')


auth()

for i in range(SECONDS):
    lat += round(random.uniform(0, LAT_MAX_STEP), 7) * DIRECTION_LAT
    lng += round(random.uniform(0, LNG_MAX_STEP), 7) * DIRECTION_LNG
    write_geo(lng, lat, 1000 + i)
    sleep(1)

if TARGET == "ADV":
    tn.write("exit\n")
    print(tn.read_all())

