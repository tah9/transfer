package com.genymobile.transfer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.genymobile.CustomSocket;
import com.genymobile.transfer.ITransferInterface;
import com.genymobile.transfer.Options;
import com.genymobile.transfer.RunProcess;
import com.genymobile.transfer.censerver.MessageType;
import com.genymobile.transfer.video.EncodeConfigure;
import com.genymobile.transfer.video.ScreenConfigure;
import com.genymobile.transfer.video.ScreenEncoder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TransferService /*extends Service */ {
    private static final String TAG = "MainService";

    public TransferService() {
        //主服务线程,用来与交互app通信
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket mainServerSocket = new ServerSocket(20000);
                    System.out.println("mainServerSocket启动完成");
                    //只与一个交互app连接
                    CustomSocket appSocket = new CustomSocket(mainServerSocket.accept());
                    System.out.println("一个设备连接到app");
                    //循环接受交互app的信息
                    while (true) {
                        System.out.println("阻塞中");
                        int type = appSocket.getDataInputStream().readInt();
                        System.out.println("应用接力");
                        if (type == MessageType.APP) {
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
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //其他设备连接
                                        try {
                                            Socket videoSocket = serverSocket.accept();
                                            System.out.println("其他设备已连接视频流");

                                            MediaCodec codec = EncodeConfigure.createCodec();
                                            MediaFormat format = EncodeConfigure.createFormat(options);
                                            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

                                            Surface surface = codec.createInputSurface();


                                            int displayId = ScreenConfigure.configureDisplay(options, surface);
                                            options.setTargetDisplayId(displayId);

                                            System.out.println("创建屏幕完毕,displayId=" + displayId);

                                            //在虚拟显示器运行指定应用
                                            //1根据包名查询启动类
                                            String queryCmd = "cmd package resolve-activity --brief -c android.intent.category.LAUNCHER " + packageName + " | tail -n 1";
                                            System.out.println("queryCmd=" + queryCmd);
                                            String queryResult = RunProcess.runProcess(queryCmd);
                                            System.out.println("queryResult=" + queryResult);

                                            //process和adb执行不一样,不能过滤结果,本次使用正则表达式
                                            queryResult = queryResult.split("\n")[1];
                                            System.out.println("split result=" + queryResult);

                                            //2在指定显示器加载指定应用
                                            String amResult = RunProcess.runProcess("am start -n " + queryResult + " --display " + displayId);
                                            System.out.println("amResult=" + amResult);


                                            //开始传输视频流
                                            ScreenEncoder screenEncoder = new ScreenEncoder();
                                            screenEncoder.streamScreen(codec, options, ParcelFileDescriptor.fromSocket(videoSocket).getFileDescriptor(), videoSocket.getOutputStream());

                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }

                                    }
                                }).start();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();
    }

    ITransferInterface.Stub binder = new ITransferInterface.Stub() {
        @Override
        public String appRunOnTargetDisplay(String packageName, String optionsStr) throws RemoteException {
            //本地套接字以及display名称
            String instantName = "transfer@" + System.currentTimeMillis();
            System.out.println("appRunOnTargetDisplay:instantName=" + instantName + ",packageName=" + packageName + ",optionsStr=" + optionsStr);
            Options options = Options.createOptionsFromStr(optionsStr);

            try {
                //本地套接字创建完毕即可通知交互程序转发端口
                LocalServerSocket localServerSocket = new LocalServerSocket(instantName);
                System.out.println("本地套接字创建完成 " + instantName);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //其他设备连接
                        try {
                            LocalSocket videoSocket = localServerSocket.accept();

                            MediaCodec codec = EncodeConfigure.createCodec();
                            MediaFormat format = EncodeConfigure.createFormat(options);
                            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

                            Surface surface = codec.createInputSurface();


                            int displayId = ScreenConfigure.configureDisplay(options, surface);
                            options.setTargetDisplayId(displayId);

                            System.out.println("创建屏幕完毕");

                            //在虚拟显示器运行指定应用
                            //1根据包名查询启动类
                            String queryLauncherClass = RunProcess.runProcess("su -c cmd package resolve-activity --brief -c android.intent.category.LAUNCHER " + packageName + " | tail -n 1");
                            //2在指定显示器加载指定应用
                            Runtime.getRuntime().exec("su -c shell am start -n " + queryLauncherClass + " --display " + displayId);


                            //开始传输视频流
                            ScreenEncoder screenEncoder = new ScreenEncoder();
                            screenEncoder.streamScreen(codec, options, videoSocket.getFileDescriptor(), videoSocket.getOutputStream());

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }
                }).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return instantName;
        }
    };

//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return binder;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.d(TAG, "onCreate: ");
//    }
}
