package com.genymobile.transfer.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.IBinder;
import android.system.ErrnoException;
import android.system.Os;
import android.view.Surface;

import com.genymobile.transfer.Server;
import com.genymobile.transfer.comon.FakeContext;
import com.genymobile.transfer.comon.IO;
import com.genymobile.transfer.control.EventController;
import com.genymobile.transfer.device.Device;
import com.genymobile.transfer.wrappers.DisplayManager;
import com.genymobile.transfer.wrappers.ServiceManager;
import com.genymobile.transfer.wrappers.SurfaceControl;

import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScreenEncoder implements Device.RotationListener {

    private static final int DEFAULT_FRAME_RATE = 60; // fps
    private static final int DEFAULT_I_FRAME_INTERVAL = 10; // seconds

    private static final int REPEAT_FRAME_DELAY = 6; // repeat after 6 frames

    private static final int MICROSECONDS_IN_ONE_SECOND = 1_000_000;
    private final AtomicBoolean rotationChanged = new AtomicBoolean();

    private int bitRate;
    private int frameRate;
    private int iFrameInterval;

    public ScreenEncoder(int bitRate, int frameRate, int iFrameInterval) {
        this.bitRate = bitRate;
        this.frameRate = frameRate;
        this.iFrameInterval = iFrameInterval;
    }

    public ScreenEncoder(int bitRate) {
        this(bitRate, DEFAULT_FRAME_RATE, DEFAULT_I_FRAME_INTERVAL);
    }

    @Override
    public void onRotationChanged(int rotation) {
        rotationChanged.set(true);
    }

    public boolean consumeRotationChange() {
        return rotationChanged.getAndSet(false);
    }


    public void streamScreen(Device device, FileDescriptor fileDescriptor, DataOutputStream dataOutputStream) throws IOException {
        MediaFormat format = createFormat(bitRate, frameRate, iFrameInterval);
//        device.setRotationListener(this);
        boolean alive;
        try {
//            do {
            MediaCodec codec = createCodec();
            IBinder display = createDisplay("scrcpy");
            Rect deviceRect = device.getScreenInfo().getDeviceSize().toRect();
            Rect videoRect = device.getScreenInfo().getVideoSize().toRect();
            setSize(format, videoRect.width(), videoRect.height());
            configure(codec, format);
                /*
                这一步很关键，
                创建了一个硬件加速的surface
                并将其绑定到显示图层
                这样解码器就有数据源了
                 */
            Surface surface = codec.createInputSurface();

//            VirtualDisplay virtualScrcpy = DisplayManager.createVirtualDisplay("virtual_scrcpy", 1080, 2160, 403);
//            virtualScrcpy.setSurface(surface);
//            Server.device.setDisplayId(virtualScrcpy.getDisplay().getDisplayId());
            System.out.println("setting finish");
            //有内容了，mediacodec有数据源了
            setDisplaySurface(display, surface, deviceRect, videoRect, device.getScreenInfo().getLayerStack());
            Server.device.setDisplayId(0);
            codec.start();
            try {
                alive = encode(codec, fileDescriptor, dataOutputStream);
            } finally {
                codec.stop();
                destroyDisplay(display);
                codec.release();
                surface.release();
            }
            System.out.println("do finish");

//            } while (alive);
        } finally {
            device.setRotationListener(null);
        }
    }


    /*
    因为把编码器的surface和显示层绑定了
    编码器之间对显示内容进行了编码
    所以下面的encode方法本质上是从解码器中读取编好码的数据然后发送，逻辑层面并没有进行真正的编码
     */
    private boolean encode(MediaCodec codec, FileDescriptor fileDescriptor, DataOutputStream outputStream) throws IOException {
        @SuppressWarnings("checkstyle:MagicNumber")
        boolean eof = false;
        ByteBuffer header =ByteBuffer.allocate(4);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        //main in this encode loop
        while (!eof) {
            //获取可用的输出缓冲区
            int outputBufferId = codec.dequeueOutputBuffer(bufferInfo, -1);
            eof = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
            if (outputBufferId<0){
                continue;
            }
            try {
                //获取输出数据
                ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferId);
                int len = outputBuffer.remaining();
                header.clear();
                header.put((byte) ((len >> 24) & 0xFF));
                header.put((byte) ((len >> 16) & 0xFF));
                header.put((byte) ((len >> 8) & 0xFF));
                header.put((byte) (len & 0xFF));
                header.flip();
                IO.writeFully(fileDescriptor, header);
                System.out.println("len "+len);
                IO.writeFully(fileDescriptor, outputBuffer);
            } finally {
                //codec复用技术，要把数据还给它
                codec.releaseOutputBuffer(outputBufferId, false);
            }
        }

        return !eof;
    }

    private static MediaCodec createCodec() throws IOException {
        return MediaCodec.createEncoderByType("video/avc");
    }

    private static MediaFormat createFormat(int bitRate, int frameRate, int iFrameInterval) throws IOException {
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "video/avc");
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval);
        // display the very first frame, and recover from bad quality when no new frames
//        format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, MICROSECONDS_IN_ONE_SECOND * REPEAT_FRAME_DELAY / frameRate); // µs
        return format;
    }

    private static IBinder createDisplay(String name) {
        // Since Android 12 (preview), secure displays could not be created with shell permissions anymore.
        // On Android 12 preview, SDK_INT is still R (not S), but CODENAME is "S".
        boolean secure = Build.VERSION.SDK_INT < Build.VERSION_CODES.R || (Build.VERSION.SDK_INT == Build.VERSION_CODES.R && !"S".equals(
                Build.VERSION.CODENAME));
        return SurfaceControl.createDisplay(name, secure);
    }

    private static void configure(MediaCodec codec, MediaFormat format) {
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    private static void setSize(MediaFormat format, int width, int height) {
        format.setInteger(MediaFormat.KEY_WIDTH, width);
        format.setInteger(MediaFormat.KEY_HEIGHT, height);
    }

    private static void setDisplaySurface(IBinder display, Surface surface, Rect deviceRect, Rect displayRect, int layerStack) {
        SurfaceControl.openTransaction();
        try {
            SurfaceControl.setDisplaySurface(display, surface);
            SurfaceControl.setDisplayProjection(display, 0, deviceRect, displayRect);
            SurfaceControl.setDisplayLayerStack(display, layerStack);
        } finally {
            SurfaceControl.closeTransaction();
        }
    }

    private static void destroyDisplay(IBinder display) {
        SurfaceControl.destroyDisplay(display);
    }
}
