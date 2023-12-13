#!/bin/bash

#adb -s emulator-5556 forward tcp:20002 tcp:20001
targetFold="/data/local/tmp/transfer.jar"
adb -s 192.168.43.1:5555 push ../classes.dex ${targetFold}
adb -s 192.168.43.1:5555 shell CLASSPATH=${targetFold} app_process / com.genymobile.transfer.Server \
 5000 64000000
