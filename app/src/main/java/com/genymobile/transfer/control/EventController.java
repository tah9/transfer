package com.genymobile.transfer.control;

import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
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

    public EventController(Device device, Options options) {
        this.device = device;

        try {
            Process process = Runtime.getRuntime().exec("dumpsys display | grep mDisplayId");
            InputStream inputStream = process.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            StringBuffer buffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            HashSet<String> displayIds = new HashSet<>();
            Pattern pattern = Pattern.compile("mDisplayId=(\\d+)");
            Matcher matcher = pattern.matcher(buffer.toString());

            while (matcher.find()) {
                displayIds.add(matcher.group(1));
            }

            int tid = 0;
            for (String id : displayIds) {
                System.out.println("displayId+"+id);
                tid = Math.max(tid,Integer.parseInt(id));
            }
            options.setTargetDisplayId(tid);



            Runtime.getRuntime().exec("am start -n bin.mt.plus/.Main --display "+tid);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        this.device.setDisplayId(options.getTargetDisplayId());
        System.out.println("EventController: touch displayId "+options.getTargetDisplayId());
        System.out.println("EventController: touch displayId "+device.displayId);
        this.options = options;
        initPointers();
        try {
            controlSocket = new Socket(options.getHost(), options.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private void setPointerCoords(Point point) {
        MotionEvent.PointerCoords coords = pointerCoords[0];
        coords.x = point.x;
        coords.y = point.y;
    }

    private void setScroll(int hScroll, int vScroll) {
        MotionEvent.PointerCoords coords = pointerCoords[0];
        coords.setAxisValue(MotionEvent.AXIS_HSCROLL, hScroll);
        coords.setAxisValue(MotionEvent.AXIS_VSCROLL, vScroll);
    }

    private static final String TAG = "EventController";
    public static long displayId;


    private static Position readPosition(DataInputStream dis) {
        try {
            int x = dis.readInt();
            int y = dis.readInt();
            int screenWidth = dis.readInt();
            int screenHeight = dis.readInt();
            return new Position(x, y, screenWidth, screenHeight);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ControlEvent createControl(DataInputStream dis) {
        try {
            int action = dis.readInt();//4 8 16 4 4 4 4
            long pointerId = dis.readLong();
            Position position = readPosition(dis);
            float pressure = dis.readFloat();
            int actionButton = dis.readInt();
            int buttons = dis.readInt();
            int displayId = dis.readInt();
            return ControlEvent.createMotionControlEvent(action, pointerId, position, pressure, actionButton, buttons, displayId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void control() throws IOException {
        // on start, turn screen on
        turnScreenOn();
//        DataOutputStream oos = new DataOutputStream(controlSocket.getOutputStream());
        DataInputStream ois = new DataInputStream(controlSocket.getInputStream());
//
        try {
//            Thread.sleep(3000);
//            oos.writeLong(displayId);
            System.out.println("control: " + Server.device.getDisplayInfo().getDisplayId());

//            oos.flush();
            while (true) {
                int downTime = 0;
                int action = 0;
                int count = 0;
                downTime = ois.readInt();
                action = ois.readInt();
                count = ois.readInt();
                System.out.println("pointerCounter " + count);


                try {
                    for (int i = 0; i < count; i++) {
                        pointerProperties[i].id = ois.readInt();
                        MotionEvent.PointerCoords pointerCoord = pointerCoords[i];
                        pointerCoord.x = ois.readInt();
                        pointerCoord.y = ois.readInt();
                        pointerCoord.pressure = ois.readFloat();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                int buttonState = 0;
                int source = 0;
                try {
                    buttonState = ois.readInt();
                    source = ois.readInt();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try {
                    MotionEvent event = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
                                action, count, pointerProperties, pointerCoords,
                                0, buttonState, 0, 0, DEFAULT_DEVICE_ID,
                                0, source, device.displayId,0, MotionEvent.CLASSIFICATION_NONE);
                    }
                    device.injectEvent(event, Device.INJECT_MODE_ASYNC);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleEvent() throws IOException {
        ControlEvent controlEvent = receiveControlEvent();
        switch (controlEvent.getType()) {
            case ControlEvent.TYPE_KEYCODE:
                injectKeycode(controlEvent.getAction(), controlEvent.getKeycode(), controlEvent.getMetaState());
                break;
            case ControlEvent.TYPE_TEXT:
                injectText(controlEvent.getText());
                break;
            case ControlEvent.TYPE_MOUSE:
//                injectMouse(controlEvent.getAction(), controlEvent.getPointerId(), controlEvent.getPosition(), controlEvent.getPressure(), controlEvent.getActionButton(), controlEvent.getButtons(), controlEvent.getDisplayId());
                break;
            case ControlEvent.TYPE_SCROLL:
                injectScroll(controlEvent.getPosition(), controlEvent.getHScroll(), controlEvent.getVScroll());
                break;
            case ControlEvent.TYPE_COMMAND:
                executeCommand(controlEvent.getAction());
                break;
            default:
                // do nothing
        }
    }

    private boolean injectKeycode(int action, int keycode, int metaState) {
        return injectKeyEvent(action, keycode, 0, metaState);
    }

    private boolean injectChar(char c) {
        String decomposed = KeyComposition.decompose(c);
        char[] chars = decomposed != null ? decomposed.toCharArray() : new char[]{c};
        KeyEvent[] events = charMap.getEvents(chars);
        if (events == null) {
            return false;
        }
        for (KeyEvent event : events) {
            if (!injectEvent(event)) {
                return false;
            }
        }
        return true;
    }

    private boolean injectText(String text) {
        for (char c : text.toCharArray()) {
            if (!injectChar(c)) {
                return false;
            }
        }
        return true;
    }


    private boolean injectMouse(int action, long pointerId, Position position, float pressure, int actionButton, int buttons) {
        long now = SystemClock.uptimeMillis();

        Point point = position.getPoint();
        if (point == null) {
            Ln.w("Ignore touch event, it was generated for a different device size");
            return false;
        }

        int pointerIndex = pointersState.getPointerIndex(pointerId);
        if (pointerIndex == -1) {
            Ln.w("Too many pointers for touch event");
            return false;
        }
        Pointer pointer = pointersState.get(pointerIndex);
        pointer.setPoint(point);
        pointer.setPressure(pressure);

        int source;
        if (pointerId == POINTER_ID_MOUSE || pointerId == POINTER_ID_VIRTUAL_MOUSE) {
            // real mouse event (forced by the client when --forward-on-click)
            pointerProperties[pointerIndex].toolType = MotionEvent.TOOL_TYPE_MOUSE;
            source = InputDevice.SOURCE_MOUSE;
            pointer.setUp(buttons == 0);
        } else {
            // POINTER_ID_GENERIC_FINGER, POINTER_ID_VIRTUAL_FINGER or real touch from device
            pointerProperties[pointerIndex].toolType = MotionEvent.TOOL_TYPE_FINGER;
            source = InputDevice.SOURCE_TOUCHSCREEN;
            // Buttons must not be set for touch events
            buttons = 0;
            pointer.setUp(action == MotionEvent.ACTION_UP);
        }

        int pointerCount = pointersState.update(pointerProperties, pointerCoords);
        if (pointerCount == 1) {
            if (action == MotionEvent.ACTION_DOWN) {
                lastTouchDown = now;
            }
        } else {
            // secondary pointers must use ACTION_POINTER_* ORed with the pointerIndex
            if (action == MotionEvent.ACTION_UP) {
                action = MotionEvent.ACTION_POINTER_UP | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            } else if (action == MotionEvent.ACTION_DOWN) {
                action = MotionEvent.ACTION_POINTER_DOWN | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            }
        }

        /* If the input device is a mouse (on API >= 23):
         *   - the first button pressed must first generate ACTION_DOWN;
         *   - all button pressed (including the first one) must generate ACTION_BUTTON_PRESS;
         *   - all button released (including the last one) must generate ACTION_BUTTON_RELEASE;
         *   - the last button released must in addition generate ACTION_UP.
         *
         * Otherwise, Chrome does not work properly: <https://github.com/Genymobile/scrcpy/issues/3635>
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && source == InputDevice.SOURCE_MOUSE) {
            if (action == MotionEvent.ACTION_DOWN) {
                if (actionButton == buttons) {
                    // First button pressed: ACTION_DOWN
                    MotionEvent downEvent = MotionEvent.obtain(lastTouchDown, now, MotionEvent.ACTION_DOWN, pointerCount, pointerProperties,
                            pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0);
                    if (!device.injectEvent(downEvent, Device.INJECT_MODE_ASYNC)) {
                        return false;
                    }
                }

                // Any button pressed: ACTION_BUTTON_PRESS
                MotionEvent pressEvent = MotionEvent.obtain(lastTouchDown, now, MotionEvent.ACTION_BUTTON_PRESS, pointerCount, pointerProperties,
                        pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0);
                if (!InputManager.setActionButton(pressEvent, actionButton)) {
                    return false;
                }
                if (!device.injectEvent(pressEvent, Device.INJECT_MODE_ASYNC)) {
                    return false;
                }

                return true;
            }

            if (action == MotionEvent.ACTION_UP) {
                // Any button released: ACTION_BUTTON_RELEASE
                MotionEvent releaseEvent = MotionEvent.obtain(lastTouchDown, now, MotionEvent.ACTION_BUTTON_RELEASE, pointerCount, pointerProperties,
                        pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0);
                if (!InputManager.setActionButton(releaseEvent, actionButton)) {
                    return false;
                }
                if (!device.injectEvent(releaseEvent, Device.INJECT_MODE_ASYNC)) {
                    return false;
                }

                if (buttons == 0) {
                    // Last button released: ACTION_UP
                    MotionEvent upEvent = MotionEvent.obtain(lastTouchDown, now, MotionEvent.ACTION_UP, pointerCount, pointerProperties,
                            pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0);
                    if (!device.injectEvent(upEvent, Device.INJECT_MODE_ASYNC)) {
                        return false;
                    }
                }

                return true;
            }
        }

        MotionEvent event = MotionEvent
                .obtain(lastTouchDown, now, action, pointerCount, pointerProperties, pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source,
                        0);
        return device.injectEvent(event, Device.INJECT_MODE_ASYNC);
    }


    private boolean injectScroll(Position position, int hScroll, int vScroll) {
        long now = SystemClock.uptimeMillis();
        Point point = position.getPoint();
        if (point == null) {
            // ignore event
            return false;
        }
        setPointerCoords(point);
        setScroll(hScroll, vScroll);
//        MotionEvent event = MotionEvent.obtain(lastMouseDown, now, MotionEvent.ACTION_SCROLL, 1, pointerProperties, pointerCoords, 0, 0, 1f, 1f, 0,
//                0, InputDevice.SOURCE_MOUSE, 0);
//        return injectEvent(event);
        return false;
    }

    private boolean injectKeyEvent(int action, int keyCode, int repeat, int metaState) {
        long now = SystemClock.uptimeMillis();
        KeyEvent event = new KeyEvent(now, now, action, keyCode, repeat, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0,
                InputDevice.SOURCE_KEYBOARD);
        return injectEvent(event);
    }

    private boolean injectKeycode(int keyCode) {
        return injectKeyEvent(KeyEvent.ACTION_DOWN, keyCode, 0, 0)
                && injectKeyEvent(KeyEvent.ACTION_UP, keyCode, 0, 0);
    }

    private boolean injectEvent(InputEvent event) {
        return device.injectEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

    private boolean turnScreenOn() {
        return device.isScreenOn() || injectKeycode(KeyEvent.KEYCODE_POWER);
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

