package com.genymobile.transfer;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class DesktopConnection implements Closeable {

    private static final int DEVICE_NAME_FIELD_LENGTH = 64;

    private static final String SOCKET_NAME = "scrcpy";

    private final LocalSocket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;

    private final ControlEventReader reader = new ControlEventReader();

    private DesktopConnection(LocalSocket socket) throws IOException {
        this.socket = socket;
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }


    private static LocalSocket connect(String abstractName) throws IOException {
        System.out.println("连接前");
        LocalSocket localSocket = new LocalSocket();
        localSocket.connect(new LocalSocketAddress(abstractName, LocalSocketAddress.Namespace.ABSTRACT));
        return localSocket;
    }

    /*
    连接服务器
    并向客户端发送设备名称和宽高
     */
    public static DesktopConnection open(Device device) throws IOException {
        LocalSocket socket = connect(SOCKET_NAME);

        DesktopConnection connection = new DesktopConnection(socket);
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
        byte[] buffer = new byte[DEVICE_NAME_FIELD_LENGTH + 4];

        byte[] deviceNameBytes = deviceName.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(DEVICE_NAME_FIELD_LENGTH - 1, deviceNameBytes.length);
        System.arraycopy(deviceNameBytes, 0, buffer, 0, len);
        // byte[] are always 0-initialized in java, no need to set '\0' explicitly

        buffer[DEVICE_NAME_FIELD_LENGTH] = (byte) (width >> 8);
        buffer[DEVICE_NAME_FIELD_LENGTH + 1] = (byte) width;
        buffer[DEVICE_NAME_FIELD_LENGTH + 2] = (byte) (height >> 8);
        buffer[DEVICE_NAME_FIELD_LENGTH + 3] = (byte) height;
//        dataOutputStream.writeUTF(deviceName);
//        dataOutputStream.writeInt();
        dataOutputStream.write(buffer, 0, buffer.length);
    }

    public OutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public ControlEvent receiveControlEvent() throws IOException {
        ControlEvent event = reader.next();
        while (event == null) {
            reader.readFrom(dataInputStream);
            event = reader.next();
        }
        return event;
    }
}
