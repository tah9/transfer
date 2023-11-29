#!/bin/bash
#./gradlew clean
#./gradlew assembleDebug --no-build-cache dexMerger
adb push ./app/build/intermediates/dex/debug/mergeExtDexDebug/classes.dex /data/local/tmp/scrcpy-server.jar
adb shell CLASSPATH=/data/local/tmp/scrcpy-server.jar app_process / com.genymobile.transfer.Server
