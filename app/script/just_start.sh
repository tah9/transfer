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

DEVICE="a964518d"
# 设置端口号
MAIN_PORT=20000

# 获取设备指定端口的进程PID
PID=$(adb -s ${DEVICE} shell ss -plnt | grep ":$MAIN_PORT" | grep -oP '(?<=pid=)\d+' | head -n1)
# 检查是否有匹配到的进程
if [[ -n "$PID" ]]; then
    echo "kill PID: $PID"

    # 杀死进程（这里假设你有足够权限，否则需要加上sudo）
    adb -s ${DEVICE} shell kill -9 "$PID"
else
    echo "No process found listening on port $MAIN_PORT"
fi


# 将dex文件推入设备并改后缀为jar
adb -s ${DEVICE} push ../finish.dex ${targetFold}
adb -s ${DEVICE} shell CLASSPATH=${targetFold} app_process / com.genymobile.transfer.Server
# 0-0-1080-2326
# 注意宽高要传入2的倍数
# pc emulator is server
# pc ip 192.168.43.137
# mix2s ip 192.168.43.1
# mix2s ip 192.168.43.238
