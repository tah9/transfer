package com.genymobile.transfer.censerver.thread;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import com.genymobile.transfer.Options;
import com.genymobile.transfer.RunProcess;
import com.genymobile.transfer.censerver.MessageType;
import com.genymobile.transfer.video.EncodeConfigure;
import com.genymobile.transfer.video.ScreenConfigure;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiveThread extends Thread {

    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;

    public ReceiveThread(DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    @Override
    public void run() {
        try {
            System.out.println("被设备连接，可接受指令");
            //已连接，阻塞在此处，保持连接状态同时监听公端行为
            char type = dataInputStream.readChar();
            //应用接力
            if (type == MessageType.APP) {
                //接受option参数
                String optionStr = dataInputStream.readUTF();
                //接受应用包名
                String packageName = dataInputStream.readUTF();

                Options options = Options.createOptionsFromStr(optionStr);


                System.out.println("在指定显示器加载指定应用成功");


                //将分配的可用端口发送给公设备
                //每个设备连接到母端后，母端给其分配一个端口和创建socket用于传输视频流
                ServerSocket dsyncSocket = new ServerSocket(0);
                dataOutputStream.writeChar(MessageType.PORT);
                dataOutputStream.writeInt(dsyncSocket.getLocalPort());
                dataOutputStream.flush();

                //使用动态端口socket传输数据
               new IOThread(dsyncSocket,options,packageName).start();


            }
        } catch (Exception e) {

        }
    }
}
