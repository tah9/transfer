package com.genymobile.transfer.control;


public final class Pointer {

    public int id;

    public float x;

    public float y;

    public long downTime;

    public Pointer(int id, long downTime) {
        this.id = id;
        this.downTime = downTime;
    }

}
