package com.genymobile.transfer.video;

import android.graphics.Rect;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.Surface;

import com.genymobile.transfer.Options;
import com.genymobile.transfer.Server;
import com.genymobile.transfer.comon.IO;
import com.genymobile.transfer.device.Device;
import com.genymobile.transfer.wrappers.SurfaceControl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScreenEncoder implements Device.RotationListener {

    private final AtomicBoolean rotationChanged = new AtomicBoolean();

    @Override
    public void onRotationChanged(int rotation) {
        rotationChanged.set(true);
    }

    public boolean consumeRotationChange() {
        return rotationChanged.getAndSet(false);
    }


    public void streamScreen(Options options, FileDescriptor fileDescriptor, OutputStream outputStream) throws IOException {

//        device.setRotationListener(this);
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
        codec.start();

//                alive = encode(codec, fileDescriptor, dataOutputStream);
        encode(codec, fileDescriptor, outputStream);
//        codec.stop();
////        SurfaceControl.destroyDisplay(display);
//        codec.release();
//        surface.release();
    }

    /*
    因为把编码器的surface和显示层绑定了
    编码器之间对显示内容进行了编码
    所以下面的encode方法本质上是从解码器中读取编好码的数据然后发送，逻辑层面并没有进行真正的编码
     */
    private void encode(MediaCodec codec, FileDescriptor fileDescriptor, OutputStream outputStream) throws IOException {
        boolean eof = false;
        ByteBuffer header = ByteBuffer.allocate(12);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        //main in this encode loop
        while (true) {
            int outputBufferId = codec.dequeueOutputBuffer(bufferInfo, -1);
            if (outputBufferId < 0) {
                continue;
            }
            int size = bufferInfo.size;
            header.clear();
            header.putInt(size);
            header.putLong(bufferInfo.presentationTimeUs);
            header.position(0);
            IO.writeFully(fileDescriptor, header);
            IO.writeFully(fileDescriptor, codec.getOutputBuffer(outputBufferId));
            //codec复用技术，要把数据还给它
            codec.releaseOutputBuffer(outputBufferId, false);
        }
    }
}
