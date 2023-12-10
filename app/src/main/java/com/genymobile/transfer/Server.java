package com.genymobile.transfer;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Surface;

import com.genymobile.transfer.comon.FakeContext;
import com.genymobile.transfer.comon.Ln;
import com.genymobile.transfer.control.EventController;
import com.genymobile.transfer.device.Device;
import com.genymobile.transfer.video.ScreenEncoder;
import com.genymobile.transfer.video.VideoConnection;

import java.io.IOException;
import java.net.Socket;

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

        // start first video socket
        try (VideoConnection connection = VideoConnection.open(device)) {
            ScreenEncoder screenEncoder = new ScreenEncoder(options.getBitRate());
            // asynchronous
            // start second socket > control socket
            startEventController(device, connection);

            try {
                // synchronous
                screenEncoder.streamScreen(device, connection.getFileDescriptor(),connection.getDataOutputStream());
            } catch (IOException e) {
                // this is expected on close
                Ln.d("Screen streaming stopped");
            }
        }
    }

    private static void startEventController(final Device device, final VideoConnection connection) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new EventController(device).control();
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
        if (args.length < 1) {
            return options;
        }
        int maxSize = Integer.parseInt(args[0]) & ~7; // multiple of 8
        options.setMaxSize(maxSize);

        if (args.length < 2) {
            return options;
        }
        int bitRate = Integer.parseInt(args[1]);
        options.setBitRate(bitRate);

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
