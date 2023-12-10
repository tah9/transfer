package com.genymobile.transfer.device;

public class ScreenInfo {
    private Size deviceSize;
    private Size videoSize;
    private int layerStack = 0;
    private boolean rotated;

    public ScreenInfo(Size deviceSize, Size videoSize, boolean rotated) {
        this.deviceSize = deviceSize;
        this.videoSize = videoSize;
        this.rotated = rotated;
    }

    public void setDeviceSize(Size deviceSize) {
        this.deviceSize = deviceSize;
    }

    public void setVideoSize(Size videoSize) {
        this.videoSize = videoSize;
    }

    public void setLayerStack(int layerStack) {
        this.layerStack = layerStack;
    }

    public void setRotated(boolean rotated) {
        this.rotated = rotated;
    }

    @Override
    public String toString() {
        return "ScreenInfo{" +
                "deviceSize=" + deviceSize +
                ", videoSize=" + videoSize +
                ", layerStack=" + layerStack +
                ", rotated=" + rotated +
                '}';
    }

    public int getLayerStack() {
        return layerStack;
    }


    public Size getDeviceSize() {
        return deviceSize;
    }

    public Size getVideoSize() {
        return videoSize;
    }

    public ScreenInfo withRotation(int rotation) {
        boolean newRotated = (rotation & 1) != 0;
        if (rotated == newRotated) {
            return this;
        }
        return new ScreenInfo(deviceSize.rotate(), videoSize.rotate(), newRotated);
    }

}
