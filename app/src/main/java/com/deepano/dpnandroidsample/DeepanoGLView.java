package com.deepano.dpnandroidsample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.CollapsibleActionView;
import android.view.WindowManager;

/**
 * Created by xutianyi on 18-11-13.
 */

public class DeepanoGLView extends GLSurfaceView {

    private GLFrameRenderer renderer = null;

    private Paint mPaint = null;

    private float scaleRatio = 0f;
    private float offset = 0f;
    private float f = 0f;

    private CoordBox[] coordinateBuffer = null;

    public DeepanoGLView(Context context) {
        super(context);
        init(context);

    }

    public DeepanoGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void init(Context ctx) {

        setWillNotDraw(false);
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);

        f = (float) dm.heightPixels / dm.widthPixels;
        if(f > (float) 960 / 1280 ){
            scaleRatio = (float) dm.widthPixels / 1280;
            offset = (dm.heightPixels - 960 * scaleRatio) / 2;
        }else {
            scaleRatio = (float) dm.heightPixels / 960;
            offset = (dm.widthPixels - 1280 * scaleRatio) / 2;
        }

        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);

        renderer = new GLFrameRenderer(this, dm.widthPixels, dm.heightPixels);
        setEGLContextClientVersion(2);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        renderer.update(1280, 960);
    }

    public void update(byte[] ydata, byte[] udata, byte[] vdata) {
        Log.e("Deepano", "update");
        renderer.update(1280, 960);
        renderer.update(ydata, udata, vdata);
    }

    public void updateCoord(CoordBox[] coordinateBuffer) {
        this.coordinateBuffer = coordinateBuffer;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("Deepano", "onDraw");

        if (coordinateBuffer != null) {
            for (int index = 0; index < this.coordinateBuffer.length; index++) {

                if(f > (float) 960 / 1280){
                    canvas.drawRect(
                            scaleRatio * coordinateBuffer[index].x1,
                            offset + scaleRatio * coordinateBuffer[index].y1,
                            scaleRatio * coordinateBuffer[index].x2,
                            offset + scaleRatio * coordinateBuffer[index].y2,
                            mPaint);
                }else{
                    canvas.drawRect(
                            offset + scaleRatio * coordinateBuffer[index].x1,
                            scaleRatio * coordinateBuffer[index].y1,
                            offset + scaleRatio * coordinateBuffer[index].x2,
                            scaleRatio * coordinateBuffer[index].y2,
                            mPaint);
                }
            }
        }
    }

    @Override
    public void postInvalidate() {
        super.postInvalidate();
    }
}
