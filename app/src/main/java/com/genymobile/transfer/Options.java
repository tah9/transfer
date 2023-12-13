package com.genymobile.transfer;

import android.graphics.Rect;


public class Options {

    private int dpi=401;
    private String host="192.168.43.248";
    private int port=20001;
    private int targetDisplayId = 0;
    private int bitRate=8_000_000;
    private int fps=60;
    private int refreshInterval=3;
    private int layerStack = 0;
    private String displayName="oi";
    private boolean mirror=true;//true mirror,false expand
    private Rect displayRegion;
    private Rect cropRegion;
    private int orientation = 0;

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTargetDisplayId() {
        return targetDisplayId;
    }

    public void setTargetDisplayId(int targetDisplayId) {
        this.targetDisplayId = targetDisplayId;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public int getLayerStack() {
        return layerStack;
    }

    public void setLayerStack(int layerStack) {
        this.layerStack = layerStack;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isMirror() {
        return mirror;
    }

    public void setMirror(boolean mirror) {
        this.mirror = mirror;
    }

    public Rect getDisplayRegion() {
        return displayRegion;
    }

    public void setDisplayRegion(Rect displayRegion) {
        this.displayRegion = displayRegion;
    }

    public Rect getCropRegion() {
        return cropRegion;
    }

    public void setCropRegion(Rect cropRegion) {
        this.cropRegion = cropRegion;
    }

    public int getDpi() {
        return dpi;
    }

    public void setDpi(int dpi) {
        this.dpi = dpi;
    }
}
