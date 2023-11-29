#!/bin/bash
set JAVA_HOME=/opt/android-studio-for-platform/jbr
./gradlew clean -q
./gradlew build -q #生成class文件
./gradlew makeDex -q #打包jar，并使用安卓sdk中的d8工具合成dex
adb push ./app/classes.dex /data/local/tmp/scrcpy-server.jar
adb shell CLASSPATH=/data/local/tmp/scrcpy-server.jar app_process / com.genymobile.transfer.Server 1080 8000
