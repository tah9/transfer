#!/bin/bash

set JAVA_HOME=/opt/android-studio-for-platform/jbr
cd ../
./gradlew clean -q
./gradlew build -q #生成class文件
./gradlew makeDex -q #打包jar，并使用安卓sdk中的d8工具合成dex
cd script
. just_start.sh