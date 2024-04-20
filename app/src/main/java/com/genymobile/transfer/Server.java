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
import com.genymobile.transfer.device.DisplayInfo;
import com.genymobile.transfer.service.TransferService;
import com.genymobile.transfer.video.EncodeConfigure;
import com.genymobile.transfer.video.ScreenConfigure;
import com.genymobile.transfer.video.ScreenEncoder;
import com.genymobile.transfer.video.VideoServer;
import com.genymobile.transfer.wrappers.DisplayManager;
import com.genymobile.transfer.wrappers.ServiceManager;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Method;

/*
在安卓设备内通过app_progress运行的文件 dex>jar
 */
public final class Server {

    private static final String TAG = "Server";

    public static void main(String... args) throws Exception {

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                System.out.println("uncaughtException: t=" + t + ",e=" + e);
            }
        });
        System.out.println("运行成功 PID=" + android.os.Process.myPid() + " UID=" + android.os.Process.myUid() + " Binder.CallingUid=" + Binder.getCallingUid());



        TransferService transferService = new TransferService();


        Looper.prepareMainLooper();
        Looper.loop();

    }
}
