package com.genymobile.transfer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Binder;
import android.os.Looper;
import android.view.Display;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.genymobile.transfer.comon.Ln;
import com.genymobile.transfer.control.EventController;
import com.genymobile.transfer.device.Device;
import com.genymobile.transfer.service.TransferService;
import com.genymobile.transfer.video.EncodeConfigure;
import com.genymobile.transfer.video.ScreenConfigure;
import com.genymobile.transfer.video.ScreenEncoder;
import com.genymobile.transfer.video.VideoServer;
import com.genymobile.transfer.wrappers.ServiceManager;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Method;

/*
在安卓设备内通过app_progress运行的文件 dex>jar
 */
public final class Server {

    private Server() {
        // not instantiable
    }


    public static Device device;

    //PC端通过命令的方式启动jar，传递进来的参数包含码率、分辨率等配置信息
    private static void scrcpy(Options options) throws IOException {

        // start first video socket
        VideoServer connection = new VideoServer(device, options);
        // asynchronous
        // start second socket > control socket


        MediaCodec codec = EncodeConfigure.createCodec();
        MediaFormat format = EncodeConfigure.createFormat(options);
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        /*
        这一步很关键，
        创建了一个硬件加速的surface
        并将其绑定到显示图层
        这样解码器就有数据源了
         */
        Surface surface = codec.createInputSurface();


        ScreenConfigure.configureDisplay(options, surface);


        System.out.println("创建屏幕完毕");


        startEventController(device, options, connection);


        ScreenEncoder screenEncoder = new ScreenEncoder();
        screenEncoder.streamScreen(codec, options, connection.getFileDescriptor(), connection.getOutputStream());

//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    private static void startEventController(final Device device, Options options, final VideoServer connection) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new EventController(device, options).control();
                } catch (IOException e) {
                    // this is expected on close
                    Ln.d("Event controller stopped");
                }
            }
        }).start();
    }


    private static final String TAG = "Server";

    public static void main(String... args) throws Exception {

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                System.out.println("uncaughtException: t=" + t + ",e=" + e);
            }
        });
        System.out.println("运行成功 PID=" + android.os.Process.myPid() + " UID=" + android.os.Process.myUid()+" Binder.CallingUid="+Binder.getCallingUid());


        TransferService transferService = new TransferService();


        Looper.prepareMainLooper();
        Looper.loop();

//
//        //构造通信参数实体
//        Options options = Options.createOptionsFromStr(args[0]);
//        startCenterControlSocket();
//
//
//
//        scrcpy(options);
    }
}
