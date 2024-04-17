#!/bin/bash

set JAVA_HOME=/opt/android-studio-for-platform/jbr
cd ../../
./gradlew clean -q
./gradlew build -q #生成class文件
./gradlew createJar #将class文件打包为jar

#使用dx工具将jar转为dex
/home/mo/dependencies/Android/Sdk/build-tools/30.0.2/dx --dex --output=/home/mo/AsfpProjects/transferclient/app/src/main/assets/finish.dex /home/mo/AsfpProjects/transfer/app/build/libs/finish.jar

# 将文件放入客户端项目assets目录下
#./gradlew makeDex -q #打包jar，并使用安卓sdk中的d8工具合成dex

#/home/mo/dependencies/Android/Sdk/build-tools/30.0.3/d8 --output ./ /home/mo/AsfpProjects/transfer/app/build/libs/finish.jar


# 改名
#mv /home/mo/AsfpProjects/transferclient/app/src/main/assets/finish.dex /home/mo/AsfpProjects/transferclient/app/src/main/assets/dex.bak
# 复制
cp /home/mo/AsfpProjects/transferclient/app/src/main/assets/finish.dex /home/mo/AsfpProjects/transfer/app/finish.dex
cd app/script
#. just_start.sh