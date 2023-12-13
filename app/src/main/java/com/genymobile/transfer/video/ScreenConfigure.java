package com.genymobile.transfer.video;

import android.hardware.display.VirtualDisplay;
import android.os.IBinder;
import android.view.Surface;

import com.genymobile.transfer.Options;
import com.genymobile.transfer.wrappers.DisplayManager;
import com.genymobile.transfer.wrappers.SurfaceControl;

public class ScreenConfigure {
    public static void configureDisplay(Options options, Surface surface) {
        if (options.isMirror()) {
            IBinder display = SurfaceControl.createDisplay(options.getDisplayName());
            options.setTargetDisplayId(0);

            SurfaceControl.setDisplaySurface(display,
                    options.getOrientation(),
                    surface,
                    options.getDisplayRegion(), options.getCropRegion(), options.getLayerStack());

        } else {
            VirtualDisplay virtualScrcpy = DisplayManager.createVirtualDisplay(options.getDisplayName(), options.getDisplayRegion().width(), options.getDisplayRegion().height(), options.getDpi());
            options.setTargetDisplayId(virtualScrcpy.getDisplay().getDisplayId());
            virtualScrcpy.setSurface(surface);
        }
    }
}
