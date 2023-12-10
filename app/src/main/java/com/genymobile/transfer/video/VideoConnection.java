package com.genymobile.transfer.video;

import android.os.ParcelFileDescriptor;

import com.genymobile.transfer.comon.NetworkConfig;
import com.genymobile.transfer.device.Device;
import com.genymobile.transfer.device.Size;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Socket;

public final class VideoConnection implements Closeable {

    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;


    private VideoConnection(Socket socket) throws IOException {
        this.socket = socket;
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    private static Socket connect(String host, int port) throws IOException {
        return new Socket(host, port);
    }

    /*
    连接服务器
    并向客户端发送设备名称和宽高
     */
    public static VideoConnection open(Device device) throws IOException {
        Socket socket = connect(NetworkConfig.SOCKET_HOST, NetworkConfig.SOCKET_PORT);
        VideoConnection connection = new VideoConnection(socket);
        Size videoSize = device.getScreenInfo().getVideoSize();
        connection.send(Device.getDeviceName(), videoSize.getWidth(), videoSize.getHeight());
        return connection;
    }

    public void close() throws IOException {
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private void send(String deviceName, int width, int height) throws IOException {
        int wh = (width << 16) | (height & 0xFFFF);
        dataOutputStream.writeUTF(deviceName);
        dataOutputStream.writeInt(wh);
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }
    public FileDescriptor getFileDescriptor(){
        return ParcelFileDescriptor.fromSocket(socket).getFileDescriptor();
    }


}
