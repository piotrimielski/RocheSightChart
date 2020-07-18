package com.givevision.rochesightchart;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLCircleRenderer implements GLSurfaceView.Renderer{
    private static final float X_POSITION = 0.5f;
    private static final float X_POSITION_LEFT = 0.30f;
    private static final float X_POSITION_RIGHT = 0.70f;
    private static final float Y_POSITION = 0.5f;
    private static final int RADIUS=200;
    private static final int STEP=40;

    private static float YELLOW_COLOR[] = {0.976f, 0.694f, 0.015f, 1f};
    private static float BLACK_COLOR[] = {0f, 0f, 0f, 1f};
    private GLCircleSprite mSprite;
    private GLCircleSprite mSpriteRight;
    private GLCircleSprite mSpriteLeft;
    private static int chart=-1;
    private static int eye=0;
    private int pos=-1;

    public GLCircleRenderer(MainActivity mainActivity) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RENDERING, "constructor");
        }
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RENDERING, "onSurfaceCreated");
        }
        GLES20.glClearColor(0, 0, 0, 0);
        mSprite = new GLCircleSprite(YELLOW_COLOR,-1);
        mSpriteLeft = new GLCircleSprite(BLACK_COLOR,0);
        mSpriteRight = new GLCircleSprite(BLACK_COLOR,1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RENDERING, "onSurfaceChanged");
        }
        GLES20.glViewport(0, 0, width, height);

        float mainBlobX = width * X_POSITION;
        float leftBlobX = width * X_POSITION_LEFT;
        float rightBlobX = width * X_POSITION_RIGHT;
        float mainBlobY = height * Y_POSITION;

        mSprite.setCenterX(mainBlobX);
        mSprite.setCenterY(mainBlobY);
        mSprite.setRadius(RADIUS);
        mSpriteLeft.setCenterX(leftBlobX);
        mSpriteLeft.setCenterY(mainBlobY);
        mSpriteLeft.setRadius(RADIUS);
        mSpriteRight.setCenterX(rightBlobX);
        mSpriteRight.setCenterY(mainBlobY);
        mSpriteRight.setRadius(RADIUS);
        mSpriteLeft.setChart(chart);
        mSpriteRight.setChart(chart);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
//        mSprite.draw();
        if(eye==0){
            mSpriteLeft.draw(pos);
        }else if(eye==1){
            mSpriteRight.draw(pos);
        }else {
            mSpriteLeft.draw(pos);
            mSpriteRight.draw(pos);
        }
    }

    public void setChart(int chart, int eye, String learnChart) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RENDERING, "setChart chart= "+
                    chart + " eye= " + eye + " learnChart= "+learnChart);
        }
        this.chart=chart;
        this.eye = eye;
        if(learnChart.contains("up")){
            pos=1;
        }else if(learnChart.contains("right")){
            pos=2;
        }else if(learnChart.contains("down")){
            pos=3;
        }else if(learnChart.contains("left")){
            pos=4;
        }else{
            pos=-1;
        }
        if(mSpriteLeft!=null && mSpriteRight!=null){
            if(chart>=0){
                mSpriteLeft.setRadius(RADIUS-STEP*chart);
                mSpriteRight.setRadius(RADIUS-STEP*chart);
            }else{
                mSpriteLeft.setRadius(RADIUS);
                mSpriteRight.setRadius(RADIUS);
            }
            mSpriteLeft.setChart(chart);
            mSpriteRight.setChart(chart);
        }
    }
}
