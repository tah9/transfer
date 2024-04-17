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

import com.genymobile.transfer.ITransferInterface;
import com.genymobile.transfer.Options;
import com.genymobile.transfer.RunProcess;
import com.genymobile.transfer.video.EncodeConfigure;
import com.genymobile.transfer.video.ScreenConfigure;
import com.genymobile.transfer.video.ScreenEncoder;

import java.io.IOException;
import java.net.Socket;

public class TransferService extends Service {
    private static final String TAG = "MainService";

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
    }
}
