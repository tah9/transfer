package com.genymobile.transfer.device;


//import android.content.IOnPrimaryClipChangedListener;

import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
//import android.view.IRotationWatcher;
//import android.view.IDisplayFoldListener;
import android.view.Display;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.genymobile.transfer.Options;
import com.genymobile.transfer.comon.Ln;
import com.genymobile.transfer.control.Point;
import com.genymobile.transfer.control.Position;
import com.genymobile.transfer.wrappers.InputManager;
import com.genymobile.transfer.wrappers.ServiceManager;
import com.genymobile.transfer.wrappers.SurfaceControl;

import java.util.concurrent.atomic.AtomicBoolean;

public class Device {

    public static final int POWER_MODE_OFF = SurfaceControl.POWER_MODE_OFF;
    public static final int POWER_MODE_NORMAL = SurfaceControl.POWER_MODE_NORMAL;

    public static final int INJECT_MODE_ASYNC = InputManager.INJECT_INPUT_EVENT_MODE_ASYNC;
    public static final int INJECT_MODE_WAIT_FOR_RESULT = InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT;
    public static final int INJECT_MODE_WAIT_FOR_FINISH = InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH;

    public static final int LOCK_VIDEO_ORIENTATION_UNLOCKED = -1;
    public static final int LOCK_VIDEO_ORIENTATION_INITIAL = -2;

    public interface RotationListener {
        void onRotationChanged(int rotation);
    }

    public interface FoldListener {
        void onFoldChanged(int displayId, boolean folded);
    }

    public interface ClipboardListener {
        void onClipboardTextChanged(String text);
    }

//    private final int lockVideoOrientation;

    private RotationListener rotationListener;
    private FoldListener foldListener;
    private ClipboardListener clipboardListener;
    private final AtomicBoolean isSettingClipboard = new AtomicBoolean();

    public void setDisplayId(int displayId) {
        this.displayId = displayId;
    }

    /**
     * Logical display identifier
     */
    public int displayId = 0;

    /**
     * The surface flinger layer stack associated with this logical display
     */
//    private final int layerStack;

//    private final boolean supportsInputEvents;

    private DisplayInfo displayInfo;

    public DisplayInfo getDisplayInfo() {
        return displayInfo;
    }

    public void setDisplayInfo(DisplayInfo displayInfo) {
        this.displayInfo = displayInfo;
    }

    public Device(Options options) {
        setDisplayId(options.getTargetDisplayId());
//        this.displayInfo = ServiceManager.getDisplayManager().getDisplayInfo(0);
//        lockVideoOrientation = options.getLockVideoOrientation();

//        screenInfo = ScreenInfo.computeScreenInfo(displayInfo.getRotation(), deviceSize, crop, maxSize, lockVideoOrientation);
//        layerStack = displayInfo.getLayerStack();

//        ServiceManager.getWindowManager().registerRotationWatcher(new IRotationWatcher.Stub() {
//            @Override
//            public void onRotationChanged(int rotation) {
//                synchronized (Device.this) {
//                    screenInfo = screenInfo.withDeviceRotation(rotation);
//
//                    // notify
//                    if (rotationListener != null) {
//                        rotationListener.onRotationChanged(rotation);
//                    }
//                }
//            }
//        }, displayId);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            ServiceManager.getWindowManager().registerDisplayFoldListener(new IDisplayFoldListener.Stub() {
//                @Override
//                public void onDisplayFoldChanged(int displayId, boolean folded) {
//                    if (Device.this.displayId != displayId) {
//                        // Ignore events related to other display ids
//                        return;
//                    }
//
//                    synchronized (Device.this) {
//                        DisplayInfo displayInfo = ServiceManager.getDisplayManager().getDisplayInfo(displayId);
//                        if (displayInfo == null) {
//                            Ln.e("Display " + displayId + " not found\n" + LogUtils.buildDisplayListMessage());
//                            return;
//                        }
//
//                        screenInfo = ScreenInfo.computeScreenInfo(displayInfo.getRotation(), displayInfo.getSize(), options.getCrop(),
//                                options.getMaxSize(), options.getLockVideoOrientation());
//                        // notify
//                        if (foldListener != null) {
//                            foldListener.onFoldChanged(displayId, folded);
//                        }
//                    }
//                }
//            });
//        }

//        if (options.getControl() && options.getClipboardAutosync()) {
//            // If control and autosync are enabled, synchronize Android clipboard to the computer automatically
//            ClipboardManager clipboardManager = ServiceManager.getClipboardManager();
//            if (clipboardManager != null) {
//                clipboardManager.addPrimaryClipChangedListener(new IOnPrimaryClipChangedListener.Stub() {
//                    @Override
//                    public void dispatchPrimaryClipChanged() {
//                        if (isSettingClipboard.get()) {
//                            // This is a notification for the change we are currently applying, ignore it
//                            return;
//                        }
//                        synchronized (Device.this) {
//                            if (clipboardListener != null) {
//                                String text = getClipboardText();
//                                if (text != null) {
//                                    clipboardListener.onClipboardTextChanged(text);
//                                }
//                            }
//                        }
//                    }
//                });
//            } else {
//                Ln.w("No clipboard manager, copy-paste between device and computer will not work");
//            }
//        }

//        if ((displayInfoFlags & DisplayInfo.FLAG_SUPPORTS_PROTECTED_BUFFERS) == 0) {
//            Ln.w("Display doesn't have FLAG_SUPPORTS_PROTECTED_BUFFERS flag, mirroring can be restricted");
//        }
//
//        // main display or any display on Android >= Q
//        supportsInputEvents = displayId == 0 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
//        if (!supportsInputEvents) {
//            Ln.w("Input events are not supported for secondary displays before Android 10");
//        }
    }


//    public int getLayerStack() {
//        return layerStack;
//    }


    public static String getDeviceName() {
        return Build.MODEL;
    }

    public static boolean supportsInputEvents(int displayId) {
        return displayId == 0 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

//    public boolean supportsInputEvents() {
//        return supportsInputEvents;
//    }

    public static boolean injectEvent(InputEvent inputEvent, int displayId) {
        System.out.println(inputEvent);
        try {
//            if (displayId != Display.DEFAULT_DISPLAY)
//                InputManager.setDisplayId(inputEvent, displayId);
//            ServiceManager.getInputManager().injectInputEvent(inputEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return true;
    }


    public static boolean isScreenOn() {
        return ServiceManager.getPowerManager().isScreenOn();
    }

    public synchronized void setRotationListener(RotationListener rotationListener) {
        this.rotationListener = rotationListener;
    }

    public synchronized void setFoldListener(FoldListener foldlistener) {
        this.foldListener = foldlistener;
    }

    public synchronized void setClipboardListener(ClipboardListener clipboardListener) {
        this.clipboardListener = clipboardListener;
    }

    public static void expandNotificationPanel() {
//        ServiceManager.getStatusBarManager().expandNotificationsPanel();
    }

    public static void expandSettingsPanel() {
//        ServiceManager.getStatusBarManager().expandSettingsPanel();
    }

    public static void collapsePanels() {
//        ServiceManager.getStatusBarManager().collapsePanels();
    }

    public static String getClipboardText() {
//        ClipboardManager clipboardManager = ServiceManager.getClipboardManager();
//        if (clipboardManager == null) {
//            return null;
//        }
//        CharSequence s = clipboardManager.getText();
//        if (s == null) {
//            return null;
//        }
        return "s.toString()";
    }

//    public boolean setClipboardText(String text) {
//        ClipboardManager clipboardManager = ServiceManager.getClipboardManager();
//        if (clipboardManager == null) {
//            return false;
//        }
//
//        String currentClipboard = getClipboardText();
//        if (currentClipboard != null && currentClipboard.equals(text)) {
//            // The clipboard already contains the requested text.
//            // Since pasting text from the computer involves setting the device clipboard, it could be set twice on a copy-paste. This would cause
//            // the clipboard listeners to be notified twice, and that would flood the Android keyboard clipboard history. To workaround this
//            // problem, do not explicitly set the clipboard text if it already contains the expected content.
//            return false;
//        }
//
//        isSettingClipboard.set(true);
//        boolean ok = clipboardManager.setText(text);
//        isSettingClipboard.set(false);
//        return ok;
//    }

    /**
     * @param mode one of the {@code POWER_MODE_*} constants
     */
    public static boolean setScreenPowerMode(int mode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Change the power mode for all physical displays
            long[] physicalDisplayIds = SurfaceControl.getPhysicalDisplayIds();
            if (physicalDisplayIds == null) {
                Ln.e("Could not get physical display ids");
                return false;
            }

            boolean allOk = true;
            for (long physicalDisplayId : physicalDisplayIds) {
                IBinder binder = SurfaceControl.getPhysicalDisplayToken(physicalDisplayId);
                allOk &= SurfaceControl.setDisplayPowerMode(binder, mode);
            }
            return allOk;
        }

        // Older Android versions, only 1 display
        IBinder d = SurfaceControl.getBuiltInDisplay();
        if (d == null) {
            Ln.e("Could not get built-in display");
            return false;
        }
        return SurfaceControl.setDisplayPowerMode(d, mode);
    }

//    public static boolean powerOffScreen(int displayId) {
//        if (!isScreenOn()) {
//            return true;
//        }
//        return pressReleaseKeycode(KeyEvent.KEYCODE_POWER, displayId, Device.INJECT_MODE_ASYNC);
//    }

    /**
     * Disable auto-rotation (if enabled), set the screen rotation and re-enable auto-rotation (if it was enabled).
     */
    public static void rotateDevice() {
//        WindowManager wm = ServiceManager.getWindowManager();
//
//        boolean accelerometerRotation = !wm.isRotationFrozen();
//
//        int currentRotation = wm.getRotation();
//        int newRotation = (currentRotation & 1) ^ 1; // 0->1, 1->0, 2->1, 3->0
//        String newRotationString = newRotation == 0 ? "portrait" : "landscape";
//
//        Ln.i("Device rotation requested: " + newRotationString);
//        wm.freezeRotation(newRotation);
//
//        // restore auto-rotate if necessary
//        if (accelerometerRotation) {
//            wm.thawRotation();
//        }
    }
}
