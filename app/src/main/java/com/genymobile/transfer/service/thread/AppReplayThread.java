package com.genymobile.transfer.service.thread;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.ParcelFileDescriptor;
import android.view.Surface;

import com.genymobile.transfer.Options;
import com.genymobile.transfer.RunProcess;
import com.genymobile.transfer.video.EncodeConfigure;
import com.genymobile.transfer.video.ScreenConfigure;
import com.genymobile.transfer.video.ScreenEncoder;

import java.net.Socket;

public class AppReplayThread extends Thread {
    private Socket videoSocket;
    private Options options;
    private String packageName;

    public AppReplayThread(Socket videoSocket, Options options, String packageName) {
        this.videoSocket = videoSocket;
        this.options = options;
        this.packageName = packageName;
    }

    @Override
    public void run() {
        super.run();
        //其他设备连接
        try {
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
}
