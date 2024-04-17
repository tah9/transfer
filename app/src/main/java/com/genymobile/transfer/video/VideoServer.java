package com.genymobile.transfer.video;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import com.genymobile.transfer.Options;
import com.genymobile.transfer.comon.IO;
import com.genymobile.transfer.device.Device;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public final class VideoServer {

    private final Socket socket;
    private FileDescriptor fileDescriptor;
    private Options options;

    public OutputStream getOutputStream(){
        try {
            return socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /*
      连接服务器
      并向客户端发送设备名称和宽高
    */
    public VideoServer(Device device, Options options) throws IOException {
        this.options = options;
//        ServerSocket serverSocket = new ServerSocket(20001);
//        this.socket = serverSocket.accept();
        this.socket = new Socket(options.getHost(), options.getPort());
//        this.socket = connect(options.getHost(), options.getPort());
        this.fileDescriptor = ParcelFileDescriptor.fromSocket(socket).getFileDescriptor();
//        Size videoSize = device.getDisplayInfo().getSize();
//        send(Device.getDeviceName(), videoSize.getWidth(), videoSize.getHeight());
    }

    private Socket connect(String host, int port) throws IOException {
        return new Socket(host, port);
    }


//    public void close() throws IOException {
//        socket.shutdownInput();
//        socket.shutdownOutput();
//        socket.close();
//    }

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
