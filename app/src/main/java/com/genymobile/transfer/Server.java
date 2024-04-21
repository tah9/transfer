package com.genymobile.transfer;

import android.os.Binder;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.genymobile.transfer.service.TransferService;

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
