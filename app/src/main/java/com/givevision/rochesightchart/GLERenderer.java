package com.givevision.rochesightchart;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLERenderer implements GLSurfaceView.Renderer{
    private static final float X_POSITION = 0.5f;
    private static final float X_POSITION_LEFT = 0.30f;
    private static final float X_POSITION_RIGHT = 0.70f;
    private static final float Y_POSITION = 0.5f;
    private static final int ORG_RADIUS = 200;
    private static final int CORRECTION = 2;
    private int radius;
    private static float YELLOW_COLOR[] = {0.976f, 0.694f, 0.015f, 1f};
    private static float GREEN_COLOR[] = {0.0f, 1f, 0.0f, 1f};
    private static float BlEU_COLOR[] = {0.0f, 0.0f, 1f, 1f};
    private static float GREY_COLOR[] = {0.5f, 0.5f, 0.5f, 1f};
    private static float BLACK_COLOR[] = {0f, 0f, 0f, 1f};
    private float mainBlobX;
    private float leftBlobX;
    private float rightBlobX;
    private float mainBlobY;
    private GLESprite mSprite;
    private GLESprite mSpriteRight;
    private GLESprite mSpriteLeft;
    private static int chart=-1;
    private static int eye=0;
    private int pos=-1;
    private int mCharacter=1;

    public GLERenderer(MainActivity mainActivity) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RENDERING, "constructor");
        }
        radius=ORG_RADIUS;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RENDERING, "onSurfaceCreated");
        }
        GLES20.glClearColor(.1f, .1f, .1f, 1);
        mSprite = new GLESprite(YELLOW_COLOR,-1, 0f);
        mSpriteLeft = new GLESprite(GREEN_COLOR,0,0f);
        mSpriteRight = new GLESprite(BlEU_COLOR,1,0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RENDERING, "onSurfaceChanged");
        }
        GLES20.glViewport(0, 0, width, height);

        mainBlobX = width * X_POSITION;
        leftBlobX = width * X_POSITION_LEFT;
        rightBlobX = width * X_POSITION_RIGHT;
        mainBlobY = height * Y_POSITION;

        mSprite.setCenterX(mainBlobX);
        mSprite.setCenterY(mainBlobY);
        mSprite.setRadius(radius);
        mSpriteLeft.setCenterX(leftBlobX);
        mSpriteLeft.setCenterY(mainBlobY);
        mSpriteLeft.setRadius(radius);
        mSpriteRight.setCenterX(rightBlobX);
        mSpriteRight.setCenterY(mainBlobY);
        mSpriteRight.setRadius(radius);
        mSpriteLeft.setChart(chart);
        mSpriteRight.setChart(chart);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
//        mSprite.draw();
        if(eye==0){
            mSpriteLeft.draw(pos, mCharacter);
        }else if(eye==1){
            mSpriteRight.draw(pos, mCharacter);
        }else if(eye==-1){
            mSpriteLeft.draw(pos, mCharacter);
            mSpriteRight.draw(pos, mCharacter);
        }else {

        }
    }

    public void setChart(int chart, int eye, String learnChart, float optotypeOuterDiameter) {
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
        }else if(learnChart.contains("all")){
            pos=5;
        }else{
            pos=-1;
        }
        if(chart>-1){
            radius = (int) optotypeOuterDiameter / 2 * CORRECTION;
        }else{
            radius=ORG_RADIUS;
        }
        if(mSpriteLeft!=null && mSpriteRight!=null){
            mSpriteLeft.setRadius(radius);
            mSpriteRight.setRadius(radius);
            mSpriteLeft.setChart(chart);
            mSpriteRight.setChart(chart);
        }
    }

    public void setLeftPosition(float delta) {
        mSpriteLeft.setCenterX(mSpriteLeft.getCenterX()-delta);
    }

    public void setRightPosition(float delta) {
        mSpriteRight.setCenterX(mSpriteRight.getCenterX()+delta);
    }

    public void resetPosition() {
        mSpriteLeft.setCenterX(leftBlobX);
        mSpriteLeft.setCenterY(mainBlobY);
        mSpriteRight.setCenterX(rightBlobX);
        mSpriteRight.setCenterY(mainBlobY);
    }

    public void setCalibrationImage(int i) {
        if(i==1){
            mSpriteLeft.setColor(GREEN_COLOR);
            mSpriteRight.setColor(BlEU_COLOR);
        }else if(i==2){
            mSpriteLeft.setColor(GREY_COLOR);
            mSpriteRight.setColor(GREY_COLOR);
        }else{
            mSpriteLeft.setColor(BLACK_COLOR);
            mSpriteRight.setColor(BLACK_COLOR);
        }

    }

    public void setCharacter(int i) {
        mCharacter=i;
    }

    public float getLeftPosition() {
        return mSpriteLeft.getCenterX();
    }
    public float getRightPosition() {
        return mSpriteRight.getCenterX();
    }
}
