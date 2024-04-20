package com.genymobile.transfer.control;

import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.genymobile.transfer.Options;
import com.genymobile.transfer.Server;
import com.genymobile.transfer.comon.Ln;
import com.genymobile.transfer.device.Device;
import com.genymobile.transfer.wrappers.InputManager;
import com.genymobile.transfer.wrappers.ServiceManager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EventController {
    private static final int POINTER_ID_MOUSE = -1;
    private static final int POINTER_ID_VIRTUAL_MOUSE = -3;
    private final Device device;
    private final KeyCharacterMap charMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
    private final Socket controlSocket;
    private Options options;
    private long lastTouchDown;
    private static final int DEFAULT_DEVICE_ID = 0;
    private final PointersState pointersState = new PointersState();
    private final MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[PointersState.MAX_POINTERS];
    private final MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[PointersState.MAX_POINTERS];

    public EventController(Device device, Options options, Socket socket) {
        this.device = device;
        this.options = options;

//
//        this.device.setDisplayId(options.getTargetDisplayId());
//        System.out.println("EventController: touch displayId "+options.getTargetDisplayId());
//        System.out.println("EventController: touch displayId "+device.displayId);
//        this.options = options;
        initPointers();
        this.controlSocket = socket;
    }

    private void initPointers() {
        for (int i = 0; i < PointersState.MAX_POINTERS; ++i) {
            MotionEvent.PointerProperties props = new MotionEvent.PointerProperties();
            props.toolType = MotionEvent.TOOL_TYPE_FINGER;

            MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
            coords.orientation = 0;
            coords.size = 0;

            pointerProperties[i] = props;
            pointerCoords[i] = coords;
        }
    }


    private final ControlEventReader reader = new ControlEventReader();

    public ControlEvent receiveControlEvent() throws IOException {
        ControlEvent event = reader.next();
        while (event == null) {
            reader.readFrom(controlSocket.getInputStream());
            event = reader.next();
        }
        return event;
    }


    private static final String TAG = "EventController";


    public void control() throws IOException {
        // on start, turn screen on
        turnScreenOn();
//        DataOutputStream oos = new DataOutputStream(controlSocket.getOutputStream());
        DataInputStream ois = new DataInputStream(controlSocket.getInputStream());

        InputManager inputManager = ServiceManager.getInputManager();

        while (true) {
            int downTime = 0;
            int action = 0;
            int count = 0;
            downTime = ois.readInt();
            action = ois.readInt();
            count = ois.readInt();
            System.out.println("pointerCounter " + count);


            for (int i = 0; i < count; i++) {
                pointerProperties[i].id = ois.readInt();
                MotionEvent.PointerCoords pointerCoord = pointerCoords[i];
                pointerCoord.x = ois.readInt();
                pointerCoord.y = ois.readInt();
                pointerCoord.pressure = ois.readFloat();
            }


            int buttonState = 0;
            int source = 0;
            buttonState = ois.readInt();
            source = ois.readInt();

            try {
                MotionEvent event = MotionEvent.obtain(
                        downTime, SystemClock.uptimeMillis(), action, count,
                        pointerProperties, pointerCoords,
                        0, 0, 1f, 1f, 0, 0,
                        InputDevice.SOURCE_TOUCHSCREEN, 0);


                if (options.getTargetDisplayId() != Display.DEFAULT_DISPLAY) {
                    InputManager.setDisplayId(event, options.getTargetDisplayId());
                }
                inputManager.injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);

            } catch (Exception e) {
                System.out.println("control error=" + e);
            }
        }
    }


    private boolean turnScreenOn() {
        return device.isScreenOn();
    }

    private boolean executeCommand(int action) {
        switch (action) {
            case ControlEvent.COMMAND_SCREEN_ON:
                return turnScreenOn();
            default:
                Ln.w("Unsupported command: " + action);
        }
        return false;
    }
}

