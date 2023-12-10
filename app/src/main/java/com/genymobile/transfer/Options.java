package com.genymobile.transfer;

public class Options {
    private int maxSize;
    private int bitRate;
    private int layerStack;

    public int getDisplayId() {
        return displayId;
    }

    public void setDisplayId(int displayId) {
        this.displayId = displayId;
    }

    private int displayId;

    public int getLayerStack() {
        return layerStack;
    }

    public void setLayerStack(int layerStack) {
        this.layerStack = layerStack;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }
}
