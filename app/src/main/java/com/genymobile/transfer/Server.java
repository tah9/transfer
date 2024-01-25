package com.genymobile.transfer;

import android.graphics.Rect;

import com.genymobile.transfer.comon.Ln;
import com.genymobile.transfer.control.EventController;
import com.genymobile.transfer.device.Device;
import com.genymobile.transfer.device.Size;
import com.genymobile.transfer.video.ScreenEncoder;
import com.genymobile.transfer.video.VideoServer;

import java.io.IOException;

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
        System.out.println("jar运行成功");

        int pid = android.os.Process.myPid();
        System.out.println("PID: " + pid);
        // start first video socket
        VideoServer connection = new VideoServer(device, options);
        ScreenEncoder screenEncoder = new ScreenEncoder();
        // asynchronous
        // start second socket > control socket
//        startEventController(device, options, connection);
        screenEncoder.streamScreen(options, connection.getFileDescriptor(), connection.getOutputStream());

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

    @SuppressWarnings("checkstyle:MagicNumber")
    private static Options createOptions(String... args) {
        Options options = new Options();
        options.setOptionsFromString(options, args[0]);

        device = new Device(options);
        Size size = device.getDisplayInfo().getSize();
        Rect rect = new Rect(0, 0, size.getWidth(), size.getHeight());
        options.setDisplayRegion(rect);
        options.setCropRegion(rect);

        if (options.getCropRegion() == null) {
            options.setCropRegion(rect);
        }

        System.out.println(options.string());
        return options;
    }

    public static void main(String... args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Ln.e("Exception on thread " + t, e);
            }
        });

        Options options = createOptions(args);

        scrcpy(options);
    }
}
