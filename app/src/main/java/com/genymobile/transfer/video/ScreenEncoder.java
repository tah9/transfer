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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScreenEncoder implements Device.RotationListener {

    private final AtomicBoolean rotationChanged = new AtomicBoolean();

    @Override
    public void onRotationChanged(int rotation) {
        rotationChanged.set(true);
    }

    public boolean consumeRotationChange() {
        return rotationChanged.getAndSet(false);
    }


    public void streamScreen(MediaCodec codec,Options options, FileDescriptor fileDescriptor, OutputStream outputStream) throws IOException {

//        device.setRotationListener(this);






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

//            byte[] bytes = new byte[size];
//            codec.getOutputBuffer(outputBufferId).get(bytes);
//            outputStream.write(bytes);
//            outputStream.flush();
            //codec复用技术，要把数据还给它
            codec.releaseOutputBuffer(outputBufferId, false);


//            String filePath = "/data/local/tmp/h264_1.txt"; // 指定的文件路径
//            appendBytesToFile(bytes, filePath); // 追加写入数据到文件
//            System.out.println(size);

        }
    }




    // 将 byte 数组追加写入指定路径的文件，头部添加指定字符串并换行
    public void appendBytesToFile(byte[] data, String filePath) {
        FileOutputStream fos = null;
        byte[] byteArray = new byte[10];
        // 将所有字节的二进制内容都设置为 1
        Arrays.fill(byteArray, (byte) 0b11111111);
        try {
            fos = new FileOutputStream(filePath, true); // 设置为 true 表示以追加模式写入文件
            fos.write(byteArray);
            fos.write(byteArray);
            fos.write(byteArray);
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
