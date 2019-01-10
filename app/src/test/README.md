# Testing the SkyLines Tracker


## Preconditions
* Use a virtual device with Google APIs (fakegps does not run otherwise !)
* Set emulator as root: adb root (https://stackoverflow.com/questions/5095234/how-to-get-root-access-on-android-emulator)
* adb install -r com.blogspot.newapphorizons.fakegps_2018-09-27.apk
* Android Device (emulator): Developer options: ON ->  Allow mock location: ON
* To record clicks: adb shell -> getevent
