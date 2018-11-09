package com.deepano.dpnandroidsample;

public class DeepanoApiFactory {

    //public static native void setDpnLog(int logSwitch);

    public static native int initDevice(int fd);

    public static native int startCamera();

    public static native int getFrameBuffer(byte[] frameBuffer);

    public static native int netProc(String blobPath);

    static {
        System.loadLibrary("dpAndroidApi");
    }
}
