package com.genymobile.transfer.censerver;

import com.genymobile.transfer.censerver.thread.ReceiveThread;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
    server端主要职责是将视频流发送给其他设备。
    服务端持续监听30001端口
    （母端监听端口，待公端连接后，仅用该端口进行指令通信，母端视频流向公端使用协商出的端口）
    根据指令构建一个socket,用来开启、销毁屏幕，在指定屏幕运行指定应用等其他操作。
 */
public class FemaleSocket {

    public FemaleSocket() {
        init();
    }

    private void init() {
        try {
            ServerSocket communicationSocket = new ServerSocket(30001);
            System.out.println("母端已运行");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {

                        try {

                            Socket communicationItemSocket = communicationSocket.accept();
                            DataOutputStream dataOutputStream = new DataOutputStream(communicationItemSocket.getOutputStream());
                            DataInputStream dataInputStream = new DataInputStream(communicationItemSocket.getInputStream());
                            //接受行为当然要开启新的线程，否则会被阻塞
                            new ReceiveThread(dataInputStream, dataOutputStream).start();


                        } catch (IOException e) {
                            System.out.println("communicationItemSocket=" + e);
                            System.exit(0);
                        } finally {
                            //创建的虚拟屏幕什么的记得在这里销毁

                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
