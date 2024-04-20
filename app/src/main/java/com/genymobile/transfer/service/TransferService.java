package com.genymobile.transfer.service;

import com.genymobile.CustomSocket;
import com.genymobile.transfer.Options;
import com.genymobile.transfer.control.EventController;
import com.genymobile.transfer.device.Device;
import com.genymobile.transfer.device.DisplayInfo;
import com.genymobile.transfer.device.Size;
import com.genymobile.transfer.wrappers.DisplayManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TransferService {
    private static final String TAG = "MainService";

    public TransferService() {
        //主服务线程,用来与交互app通信
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket mainServerSocket = new ServerSocket(20000);
                    System.out.println("mainServerSocket启动完成");

                    while (true) {
                        try {
                            //只与一个交互app连接
                            CustomSocket appSocket = new CustomSocket(mainServerSocket.accept());
                            System.out.println("交互app已连接");
                            //循环接受交互app的信息
                            while (true) {
                                System.out.println("阻塞中");
                                int type = appSocket.getDataInputStream().readInt();
                                System.out.println("type=" + type);
                                if (type == MessageType.APP) {
                                    System.out.println("应用接力");
                                    String packageName = appSocket.getDataInputStream().readUTF();
                                    String optStr = appSocket.getDataInputStream().readUTF();
                                    Options options = Options.createOptionsFromStr(optStr);
                                    System.out.println("packageName=" + packageName);
                                    System.out.println("optStr=" + optStr);
                                    try {
                                        //为这个应用创建socket,动态分配端口
                                        ServerSocket serverSocket = new ServerSocket(0);
                                        int dynamicPort = serverSocket.getLocalPort();
                                        //通知交互app,让其通知其他设备使用此端口
                                        appSocket.getDataOutputStream().writeInt(dynamicPort);
                                        System.out.println("套接字创建完成 dynamicPort=" + dynamicPort);

                                        Socket videoSocket = serverSocket.accept();
                                        System.out.println("其他设备已连接视频流");
                                        new AppReplayThread(videoSocket, options, packageName).start();

                                        Socket controlSocket = serverSocket.accept();
                                        System.out.println("其他设备已连接控制流");
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
//                                        int[] displayIds = DisplayManager.create().getDisplayIds();
                                                Device device = new Device(options);
                                                System.out.println("device created displayId=" + options.getTargetDisplayId());
                                                try {
                                                    new EventController(device, options, controlSocket).control();
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }).start();

                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else if (type == MessageType.MIRROR) {
                                    System.out.println("屏幕镜像");

                                    DisplayInfo displayInfo = DisplayManager.create().getDisplayInfo(0);
                                    Size size = displayInfo.getSize();

                                    String optStr = "dpi=" + displayInfo.getDpi() + ",displayRegion=0-0-" + size.getWidth() + "-" + size.getHeight();
                                    Options options = Options.createOptionsFromStr(optStr);
                                    options.setLayerStack(displayInfo.getLayerStack());

                                    //为这个应用创建socket,动态分配端口
                                    ServerSocket serverSocket = new ServerSocket(0);
                                    int dynamicPort = serverSocket.getLocalPort();
                                    //通知交互app,让其通知其他设备使用此端口
                                    appSocket.getDataOutputStream().writeInt(dynamicPort);
                                    System.out.println("套接字创建完成 dynamicPort=" + dynamicPort);

                                    options.setMirror(true);

                                    Socket videoSocket = serverSocket.accept();
                                    System.out.println("其他设备已连接视频流");
                                    new MirrorThread(options, new CustomSocket(videoSocket)).start();

                                    Socket controlSocket = serverSocket.accept();
                                    System.out.println("其他设备已连接控制流");

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
//                                        int[] displayIds = DisplayManager.create().getDisplayIds();
                                            Device device = new Device(options);
                                            System.out.println("device created displayId=" + options.getTargetDisplayId());
                                            try {
                                                new EventController(device, options, controlSocket).control();
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }).start();
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("交互app连接错误 error=" + e);
                        }
                    }

                } catch (IOException e) {
                    System.out.println("mainSocket error=" + e);
//                    throw new RuntimeException(e);
                }

            }
        }).start();
    }

}
