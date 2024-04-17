#!/bin/bash

#sudo fuser -k 20002/tcp #kill 20002
#sudo fuser -k 20001/tcp #kill 20002



targetFold="/data/local/tmp/transfer.jar"

#targetFold="/home/mo/AsfpProjects/transferclient/app/src/main/assets/transfer.jar"
#adb -s adb-R5CR20GSWMT-jVgbu4._adb-tls-connect._tcp push ../classes.dex ${targetFold}
#adb -s adb-R5CR20GSWMT-jVgbu4._adb-tls-connect._tcp shell CLASSPATH=${targetFold} app_process / com.genymobile.transfer.Server \
# bitRate=8000000,host=192.168.43.248,port=20002,refreshInterval=10,repeatFrame=100000

#android document say,android emulator and desktop can visit use 10.0.2.2
#forward 将设备端口转发到主机上(arg0 pc port,arg1 device port)
# reverse将主机端口转发到设备上
#adb -s emulator-5556 forward tcp:20002 tcp:20001

# 将dex文件推入设备并改后缀为jar
adb -s emulator-5554 push ../finish.dex ${targetFold}
adb -s emulator-5554 shell su -c CLASSPATH=${targetFold} app_process / com.genymobile.transfer.Server \
 bitRate=8000000,host=10.0.2.2,port=20002,refreshInterval=10,repeatFrame=100000,fps=60,MaxFps=60,displayRegion=0-0-2400-1080
# 0-0-1080-2326
# 注意宽高要传入2的倍数
# pc emulator is server
# pc ip 192.168.43.137
# mix2s ip 192.168.43.1
# mix2s ip 192.168.43.238
