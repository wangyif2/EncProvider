#!/bin/sh

rm -f ./contacts.db
~/tools/android-sdk-linux/platform-tools/adb uninstall se.yifan.android.encprovider
~/tools/android-sdk-linux/platform-tools/adb logcat -c