#!/bin/bash

#adb -s emulator-5556 forward tcp:20002 tcp:20001
targetfold="/data/local/tmp/"
#adb -s 192.168.43.248:44037 push ../app/classes.dex ${targetfold}scrcpy-server.jar
#adb -s 192.168.43.248:44037 shell CLASSPATH=${targetfold}scrcpy-server.jar app_process / com.genymobile.transfer.Server 5000 8000 0
adb -s 192.168.43.1:5555 push ../app/classes.dex ${targetfold}scrcpy-server.jar
adb -s 192.168.43.1:5555 shell CLASSPATH=${targetfold}scrcpy-server.jar app_process / com.genymobile.transfer.Server 5000 8000000 0
