package com.genymobile.transfer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
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


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LocalServerSocket localServerSocket = new LocalServerSocket("123123123");
                    LocalSocket accept = localServerSocket.accept();
                    DataInputStream dataInputStream = new DataInputStream(accept.getInputStream());
                    System.out.println("readUTF "+dataInputStream.readUTF());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();


        TransferService transferService = new TransferService();
        ServiceManager.addService("TransferService", transferService.onBind(null), true);
        System.out.println("222TransferService 服务添加成功");


//        IInterface calculateService = ServiceManager.getService("CalculateService", "com.genymobile.transfer.ICalculateInterface");
//        Method method = calculateService.getClass().getMethod("addition", int.class, int.class);


//        int result = (int) method.invoke(null, 7, 7);
//        System.out.println("result=" + result);
//        FakeContext.get().startService(new Intent(FakeContext.get(), CalculateService.class));

        int pid = android.os.Process.myPid();
        System.out.println("jar运行成功 PID=" + pid);
//        try {
//            // 获取DisplayManagerGlobal类的Class对象
//            Class<?> displayManagerGlobalClass = Class.forName("android.hardware.display.DisplayManagerGlobal");
//
//// 获取getInstance()方法
//            Method getInstanceMethod = displayManagerGlobalClass.getMethod("getInstance");
//
//// 调用getInstance()方法获取DisplayManagerGlobal的实例
//            Object displayManagerGlobalInstance = getInstanceMethod.invoke(null);
//
//// 确保getInstance()返回的对象类型是你期望的类，如果不是，请替换为正确的类型
//            // 获取getMethod方法（无参版本）
//            Method method = displayManagerGlobalClass.getMethod("getDisplayIds", boolean.class);
//
//// 调用无参方法
//            int[] displayIds = (int[]) method.invoke(displayManagerGlobalInstance, false);
//            System.out.println("display size=" + displayIds.length);
//            // 获取getRealDisplay()方法
//            Method getRealDisplayMethod = displayManagerGlobalClass.getMethod("getRealDisplay", int.class);
//            for (int displayId : displayIds) {
//                if (displayId == 0) continue;
//                System.out.println("displayId=" + displayId);
//                // 调用getRealDisplay()方法
//                Display display = (Display) getRealDisplayMethod.invoke(displayManagerGlobalInstance, displayId);
//                System.out.println(display.toString());
//            }
//            // 现在displayIds包含了所需的结果
//
//        } catch (Exception e) {
//            System.out.println(e.toString());
//            throw new RuntimeException(e);
//        }


        Looper.prepareMainLooper();
        Looper.loop();


//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread t, Throwable e) {
//                Ln.e("Exception on thread " + t, e);
//            }
//        });
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
