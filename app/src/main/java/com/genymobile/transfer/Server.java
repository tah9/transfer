package com.genymobile.transfer;

import android.graphics.Rect;

import com.genymobile.transfer.comon.Ln;
import com.genymobile.transfer.control.EventController;
import com.genymobile.transfer.device.Device;
import com.genymobile.transfer.device.DisplayInfo;
import com.genymobile.transfer.device.Size;
import com.genymobile.transfer.video.ScreenEncoder;
import com.genymobile.transfer.video.VideoConnection;

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

        device = new Device(options);
        Size size = device.getDisplayInfo().getSize();
        Rect rect = new Rect(0, 0, size.getWidth(),size.getHeight());
        options.setDisplayRegion(rect);
        options.setCropRegion(rect);

        // start first video socket
        try (VideoConnection connection = new VideoConnection(device,options)) {
            ScreenEncoder screenEncoder = new ScreenEncoder();
            // asynchronous
            // start second socket > control socket
            startEventController(device, options,connection);

            try {
                // synchronous
                screenEncoder.streamScreen(options, connection.getFileDescriptor());
            } catch (IOException e) {
                // this is expected on close
                Ln.d("Screen streaming stopped");
            }
        }
    }

    private static void startEventController(final Device device,Options options,final VideoConnection connection) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new EventController(device,options).control();
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
