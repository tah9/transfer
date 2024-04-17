package com.genymobile.transfer.censerver.thread;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.ParcelFileDescriptor;
import android.view.Surface;

import com.genymobile.transfer.Options;
import com.genymobile.transfer.RunProcess;
import com.genymobile.transfer.censerver.MessageType;
import com.genymobile.transfer.video.EncodeConfigure;
import com.genymobile.transfer.video.ScreenConfigure;
import com.genymobile.transfer.video.ScreenEncoder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class IOThread extends Thread {
    ServerSocket dsyncSocket;
    Options options;
    String packageName;

    public IOThread(ServerSocket dsyncSocket, Options options, String packageName) {
        this.dsyncSocket = dsyncSocket;
        this.options = options;
        this.packageName = packageName;
    }

    @Override
    public void run() {
        try {

            //创建虚拟显示器
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


            int displayId = ScreenConfigure.configureDisplay(options, surface);
            options.setTargetDisplayId(displayId);

            System.out.println("创建屏幕完毕");

            //在虚拟显示器运行指定应用
            //1根据包名查询启动类
            String queryLauncherClass = RunProcess.runProcess("su -c cmd package resolve-activity --brief -c android.intent.category.LAUNCHER " + packageName + " | tail -n 1");
            //2在指定显示器加载指定应用
            Runtime.getRuntime().exec("su -c shell am start -n " + queryLauncherClass + " --display " + displayId);

            //等待公端连接视频流socket
            Socket connection = dsyncSocket.accept();

            //开始传输视频流
            ScreenEncoder screenEncoder = new ScreenEncoder();
            screenEncoder.streamScreen(codec, options, ParcelFileDescriptor.fromSocket(connection).getFileDescriptor(), connection.getOutputStream());


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
