# Testing the SkyLines Tracker


## Preconditions
* Use a virtual device with Google APIs (fakegps does not run otherwise !)
* Set emulator as root: adb root (https://stackoverflow.com/questions/5095234/how-to-get-root-access-on-android-emulator)
* adb install -r com.blogspot.newapphorizons.fakegps_2018-09-27.apk
* run SkyLines app th efirst time
* adb -s emulator-5554 push ch.luethi.skylinestracker_preferences.xml /data/data/ch.luethi.skylinestracker/shared_prefs/
* Android Device (emulator): Developer options: ON ->  Allow mock location: ON
* To record clicks: adb shell -> getevent

##ToDos
* Find a more elegant method for testing
