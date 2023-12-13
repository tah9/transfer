package com.genymobile.transfer.video;

import android.os.ParcelFileDescriptor;

import com.genymobile.transfer.Options;
import com.genymobile.transfer.comon.IO;
import com.genymobile.transfer.device.Device;
import com.genymobile.transfer.device.Size;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class VideoConnection implements Closeable {

    private final Socket socket;
    private FileDescriptor fileDescriptor;
    private Options options;

    /*
      连接服务器
      并向客户端发送设备名称和宽高
       */
    public VideoConnection(Device device, Options options) throws IOException {
        this.options = options;
        this.socket = connect(options.getHost(), options.getPort());
        this.fileDescriptor = ParcelFileDescriptor.fromSocket(socket).getFileDescriptor();
        Size videoSize = device.getDisplayInfo().getSize();
        send(Device.getDeviceName(), videoSize.getWidth(), videoSize.getHeight());
    }

    private Socket connect(String host, int port) throws IOException {
        return new Socket(host, port);
    }


    public void close() throws IOException {
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private void send(String deviceName, int width, int height) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(/*64 + */4);
//        byte[] deviceNameBytes = new byte[64];
//        byte[] realName = "deviceName".getBytes(StandardCharsets.UTF_8);
//        System.arraycopy(realName,0,deviceNameBytes,0,realName.length);
        int wh = (width << 16) | (height & 0xFFFF);
        buffer.putInt(wh);
//        buffer.put(deviceNameBytes,0,deviceNameBytes.length);
        buffer.flip();
        IO.writeFully(fileDescriptor, buffer);
    }

    public FileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }


}
