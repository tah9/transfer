package com.genymobile.transfer.video;

import android.content.Context;
import android.hardware.display.VirtualDisplay;
import android.os.IBinder;
import android.view.Display;
import android.view.Surface;

import com.genymobile.transfer.Options;
import com.genymobile.transfer.wrappers.DisplayManager;
import com.genymobile.transfer.wrappers.SurfaceControl;

public class ScreenConfigure {
    public static int configureDisplay(Options options, Surface surface) {
        /*
        镜像显示
         */
        if (options.isMirror()) {
            IBinder display = SurfaceControl.createDisplay(options.getDisplayName());

            SurfaceControl.setDisplaySurface(display,
                    options.getOrientation(),
                    surface,
                    options.getDisplayRegion(), options.getCropRegion(), options.getLayerStack());
            return 0;
        } else {
            VirtualDisplay virtualScrcpy = DisplayManager.createVirtualDisplay(options.getDisplayName(),
                    options.getDisplayRegion().width(),
                    options.getDisplayRegion().height(),
                    options.getDpi());
            virtualScrcpy.setSurface(surface);
            return virtualScrcpy.getDisplay().getDisplayId();
        }
    }
}
