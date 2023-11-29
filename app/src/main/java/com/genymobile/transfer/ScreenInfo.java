package com.genymobile.transfer;

public final class ScreenInfo {
    private final Size deviceSize;
    private final Size videoSize;
    private final boolean rotated;

    public ScreenInfo(Size deviceSize, Size videoSize, boolean rotated) {
        this.deviceSize = deviceSize;
        this.videoSize = videoSize;
        this.rotated = rotated;
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

    @Override
    public String toString() {
        return "ScreenInfo{" +
                "deviceSize=" + deviceSize +
                ", videoSize=" + videoSize +
                ", rotated=" + rotated +
                '}';
    }
}
