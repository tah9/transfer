#!/bin/bash


targetFold="/data/local/tmp/transfer.jar"
#adb -s adb-R5CR20GSWMT-jVgbu4._adb-tls-connect._tcp push ../classes.dex ${targetFold}
#adb -s adb-R5CR20GSWMT-jVgbu4._adb-tls-connect._tcp shell CLASSPATH=${targetFold} app_process / com.genymobile.transfer.Server \
# bitRate=8000000,host=192.168.43.248,port=20002,refreshInterval=10,repeatFrame=100000

#android document say,android emulator and desktop can visit use 10.0.2.2
#forward 将设备端口转发到主机上， reverse将主机端口转发到设备上，以下文为例，主机使用20001端口
adb -s emulator-5554 forward tcp:20002 tcp:20001
#
adb -s emulator-5554 push ../classes.dex ${targetFold}
adb -s emulator-5554 shell CLASSPATH=${targetFold} app_process / com.genymobile.transfer.Server \
 bitRate=8000000,host=10.0.2.2,port=20002,refreshInterval=1,repeatFrame=1000
