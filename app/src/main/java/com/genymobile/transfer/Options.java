package com.genymobile.transfer;

import android.graphics.Rect;

import java.lang.reflect.Field;


public class Options {

    private int repeatFrame = 100_000;//us
    private int dpi = 420;
    private String host = "10.0.2.2";
    private int port = 20002;
    private int targetDisplayId = 0;
    private int bitRate = 8_000_000;
    private int fps = 60;
    private int refreshInterval = 3;
    private int layerStack = 0;
    private String displayName = "_dis_";
    private boolean mirror = false;//true mirror,false expand
    private Rect displayRegion;
    private Rect cropRegion;
    private int orientation = 0;
    private int MaxFps = 60;

    public int getMaxFps() {
        return MaxFps;
    }

    public void setMaxFps(int maxFps) {
        MaxFps = maxFps;
    }


    //bitRate=8000000,host=10.0.2.2,port=20002,refreshInterval=10,repeatFrame=100000,fps=60,MaxFps=60,displayRegion=0-0-2400-1080
    public static Options createOptionsFromStr(String input) {
        Options options = new Options();
        String[] keyValuePairs = input.split(",");
        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                try {
                    Field field = options.getClass().getDeclaredField(key);
                    field.setAccessible(true);
                    if (field.getType().equals(int.class)) {
                        field.setInt(options, Integer.parseInt(value));
                    } else if (field.getType().equals(String.class)) {
                        field.set(options, value);
                    } else if (field.getType().equals(boolean.class)) {
                        field.setBoolean(options, Boolean.parseBoolean(value));
                    } else if (field.getType().equals(Rect.class)) {
                        //displayRegion=0-0-2400-1080
                        //注意,需要确保宽高不能为奇数
                        System.out.println("rect " + value);
                        String[] s = value.split("-");
                        field.set(options, new Rect(
                                Integer.parseInt(s[0]),
                                Integer.parseInt(s[1]),
                                Integer.parseInt(s[2]) % 2 != 0 ? Integer.parseInt(s[2]) - 1 : Integer.parseInt(s[2]),
                                Integer.parseInt(s[3]) % 2 != 0 ? Integer.parseInt(s[3]) - 1 : Integer.parseInt(s[3])
                        ));
                    }
                    // 可以根据需要添加其他数据类型的处理
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if (options.cropRegion==null){
            options.setCropRegion(options.getDisplayRegion());
        }
        System.out.println("createOptionsFromStr="+options.string());
        return options;
    }

    public String string() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getClass().getSimpleName()).append(" [");

        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true); // 让我们能够访问私有字段
            try {
                String fieldName = field.getName();
                Object value = field.get(this); // 获取字段的值
                stringBuilder.append(fieldName).append("=").append(value).append(", ");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        // 删除最后一个逗号和空格
        if (fields.length > 0) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        }

        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public int getRepeatFrame() {
        return repeatFrame;
    }

    public void setRepeatFrame(int repeatFrame) {
        this.repeatFrame = repeatFrame;
    }

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
