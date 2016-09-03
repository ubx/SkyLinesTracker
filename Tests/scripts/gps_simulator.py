import telnetlib
from time import sleep
import time
import random
import sys

HOST = "127.0.0.1"
PORT = 5554
TIMEOUT = 10
LAT_SRC = 52.5243700
LNG_SRC = 13.4105300
LAT_DST = 53.5753200
LNG_DST = 10.0153400

HOST  = sys.argv[-3]
SECONDS = int(sys.argv[-2])
KEY = sys.argv[-1]

LAT_MAX_STEP = ((max(LAT_DST, LAT_SRC) - min(LAT_DST, LAT_SRC)) / SECONDS) * 2
LNG_MAX_STEP = ((max(LNG_DST, LNG_SRC) - min(LNG_DST, LNG_SRC)) / SECONDS) * 2

DIRECTION_LAT = 1 if LAT_DST - LAT_SRC > 0 else -1
DIRECTION_LNG = 1 if LNG_DST - LNG_SRC > 0 else -1

lat = LAT_SRC
lng = LNG_SRC

tn = telnetlib.Telnet(HOST, PORT, TIMEOUT)
tn.set_debuglevel(0)
tn.read_until("OK", 5)

def millies_of_day():
    t = time.gmtime()
    mils_of_dat = (t.tm_sec + (t.tm_min * 60) + (t.tm_hour * 3600)) * 1000
    return mils_of_dat

def write_geo(lng, lat, alt):
    tn.write("geo fix {0} {1} {2}\n".format(lng, lat, alt))
    print("Sim: {0},{1},{2:.5f},{3:.5f},{4}".format(millies_of_day(), KEY, lng, lat, alt))


def write_gsm(mode):
    tn.write("gsm data " + mode + '\n')
    print("Sim: gsm data " + mode + '\n')


def write_sms(msg):
    tn.write("sms send 1234 slt " + msg + '\n')

def auth():
    tn.write("auth 1dzok1p9Lo5UaJ9M" + '\n')

auth()

for i in range(SECONDS):
    lat += round(random.uniform(0, LAT_MAX_STEP), 7) * DIRECTION_LAT
    lng += round(random.uniform(0, LNG_MAX_STEP), 7) * DIRECTION_LNG
    write_geo(lng, lat, 1000+i)
    sleep(1)

write_geo(LNG_DST, LAT_DST, 100)

tn.write("exit\n")

print tn.read_all()
