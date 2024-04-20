package com.genymobile.transfer.service;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.ParcelFileDescriptor;
import android.view.Surface;

import com.genymobile.CustomSocket;
import com.genymobile.transfer.Options;
import com.genymobile.transfer.video.EncodeConfigure;
import com.genymobile.transfer.video.ScreenConfigure;
import com.genymobile.transfer.video.ScreenEncoder;

public class MirrorThread extends Thread {
    private Options options;
    private CustomSocket videoSocket;

    public MirrorThread(Options options, CustomSocket socket) {
        this.options = options;
        this.videoSocket = socket;
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

            //开始传输视频流
            ScreenEncoder screenEncoder = new ScreenEncoder();
            screenEncoder.streamScreen(codec, options, ParcelFileDescriptor.fromSocket(videoSocket.getSocket()).getFileDescriptor(), videoSocket.getSocket().getOutputStream());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
