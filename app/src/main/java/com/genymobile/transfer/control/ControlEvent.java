package com.genymobile.transfer.control;

import android.view.MotionEvent;

/**
 * Union of all supported event types, identified by their {@code type}.
 */
public final class ControlEvent {

    public static final int TYPE_KEYCODE = 0;
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_MOUSE = 2;
    public static final int TYPE_TOUCH = 9;
    public static final int TYPE_SCROLL = 3;
    public static final int TYPE_COMMAND = 4;
    public static final int COMMAND_SCREEN_ON = 0;

    private int type;
    private String text;
    private  int displayId = 0;

    private int metaState; // KeyEvent.META_*
    private int action; // KeyEvent.ACTION_* or MotionEvent.ACTION_* or COMMAND_*
    private int keycode; // KeyEvent.KEYCODE_*
    private int buttons; // MotionEvent.BUTTON_*
    private int actionButton; // MotionEvent.BUTTON_*

    private Position position;
    private int hScroll;
    private int vScroll;
    private long pointerId;
    private float pressure;
    public MotionEvent motionEvent;
    private ControlEvent() {
    }

    public static ControlEvent createKeycodeControlEvent(int action, int keycode, int metaState) {
        ControlEvent event = new ControlEvent();
        event.type = TYPE_KEYCODE;
        event.action = action;
        event.keycode = keycode;
        event.metaState = metaState;
        return event;
    }

    public static ControlEvent createTextControlEvent(String text) {
        ControlEvent event = new ControlEvent();
        event.type = TYPE_TEXT;
        event.text = text;
        return event;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setMetaState(int metaState) {
        this.metaState = metaState;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setKeycode(int keycode) {
        this.keycode = keycode;
    }

    public void setButtons(int buttons) {
        this.buttons = buttons;
    }

    public int getActionButton() {
        return actionButton;
    }

    public void setActionButton(int actionButton) {
        this.actionButton = actionButton;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int gethScroll() {
        return hScroll;
    }

    public void sethScroll(int hScroll) {
        this.hScroll = hScroll;
    }

    public int getvScroll() {
        return vScroll;
    }

    public void setvScroll(int vScroll) {
        this.vScroll = vScroll;
    }

    public long getPointerId() {
        return pointerId;
    }

    public void setPointerId(long pointerId) {
        this.pointerId = pointerId;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public MotionEvent getMotionEvent() {
        return motionEvent;
    }

    public void setMotionEvent(MotionEvent motionEvent) {
        this.motionEvent = motionEvent;
    }

    public static ControlEvent createMotionControlEvent(int action, long pointerId, Position position, float pressure, int actionButton,
                                                        int buttons,int displayId) {
        ControlEvent msg = new ControlEvent();
        msg.type = TYPE_MOUSE;
        msg.action = action;
        msg.pointerId = pointerId;
        msg.pressure = pressure;
        msg.position = position;
        msg.actionButton = actionButton;
        msg.buttons = buttons;
        msg.displayId=displayId;
        return msg;
    }

    public static ControlEvent createScrollControlEvent(Position position, int hScroll, int vScroll) {
        ControlEvent event = new ControlEvent();
        event.type = TYPE_SCROLL;
        event.position = position;
        event.hScroll = hScroll;
        event.vScroll = vScroll;
        return event;
    }

    public static ControlEvent createCommandControlEvent(int action) {
        ControlEvent event = new ControlEvent();
        event.type = TYPE_COMMAND;
        event.action = action;
        return event;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getMetaState() {
        return metaState;
    }

    public int getAction() {
        return action;
    }

    public int getKeycode() {
        return keycode;
    }

    public int getButtons() {
        return buttons;
    }
    public int getDisplayId(){
        return displayId;
    }

    public Position getPosition() {
        return position;
    }

    public int getHScroll() {
        return hScroll;
    }

    public int getVScroll() {
        return vScroll;
    }
}
