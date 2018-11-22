package com.deepano.dpnandroidsample;

public class DeepanoApiFactory {

    private static volatile DeepanoApiFactory deepanoApiFactory = null;

    private DeepanoGLView deepanoGLView = null;

    private static int FRAME_WIDTH = 1280;
    private static int FRAMW_HEIGHT = 960;

    private byte[] yData = null;
    private byte[] uData = null;
    private byte[] vData = null;

    private DeepanoApiFactory(DeepanoGLView deepanoGLView){
        this.deepanoGLView = deepanoGLView;
    }

    public static DeepanoApiFactory getApiInstance(DeepanoGLView deepanoGLView){
        if(deepanoApiFactory == null){
            synchronized (DeepanoApiFactory.class){
                if(deepanoApiFactory == null){
                    deepanoApiFactory = new DeepanoApiFactory(deepanoGLView);
                }
            }
        }
        return deepanoApiFactory;
    }

    public native int initDevice(int fd);

    public native int startCamera();

    public native int netProc(String blobPath);

    public void update(byte[] yuvBuffer){

        int size = FRAME_WIDTH * FRAMW_HEIGHT;
        if(yData == null || uData ==null || vData == null){
            yData = new byte[size];
            uData = new byte[size / 4];
            vData = new byte[size / 4];
        }

        System.arraycopy(yuvBuffer,0,yData,0,size);
        System.arraycopy(yuvBuffer,size,uData,0,size / 4);
        System.arraycopy(yuvBuffer,size * 5 / 4,vData,0,size / 4);
        this.deepanoGLView.update(yData,uData,vData);
    }

    public void getCoordinate(CoordBox[] coordinateBuffer){
        this.deepanoGLView.updateCoord(coordinateBuffer);
    }

    static {
        System.loadLibrary("dpAndroidApi");
    }

}
