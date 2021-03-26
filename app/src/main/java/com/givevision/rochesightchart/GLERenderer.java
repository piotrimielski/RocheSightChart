package com.givevision.rochesightchart;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLERenderer implements GLSurfaceView.Renderer{
    private static final float X_POSITION = 0.5f;
    private static final float X_POSITION_LEFT = 0.25f;//was 30
    private static final float X_POSITION_RIGHT = 0.75f;//was 70
    private static final int MAX_XY_DELTA = 200;//in pixel
    private static final float Y_POSITION = 0.5f;
    private static final int ORG_RADIUS = 150;
    private static final float CORRECTION = 4f; //pixels /mm if phone 25mm from lens 1.875
    private int radius;
    private static float YELLOW_COLOR[] = {0.976f, 0.694f, 0.015f, 1f};
    private static float GREEN_COLOR[] = {0.0f, 1f, 0.0f, 1f};
    private static float BlEU_COLOR[] = {0.0f, 0.0f, 1f, 1f};
    private static float GREY_COLOR[] = {0.5f, 0.5f, 0.5f, 1f};
    private static float BLACK_COLOR[] = {0f, 0f, 0f, 1f};
//    private float mainBlobX;
    private float leftBlobX;
    private float rightBlobX;
    private float leftBlobY;
    private float rightBlobY;
//    private float mainBlobY;
//    private GLESprite mSprite;
    private GLESprite mSpriteRight;
    private GLESprite mSpriteLeft;
    private  int chart=-1;
    private  int eye=0;
    private  int greyE=125;
    private  int greySquare=126;
    private int pos=-1;
    private int mCharacter=1;

    public GLERenderer(MainActivity mainActivity) {
        if (Util.DEBUG_RENDERER) {
            Log.i(Util.LOG_TAG_RENDERING, "constructor");
        }
        radius=ORG_RADIUS;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        if (Util.DEBUG_RENDERER) {
            Log.i(Util.LOG_TAG_RENDERING, "onSurfaceCreated");
        }
        GLES20.glClearColor(.1f, .1f, .1f, 1);
//        mSprite = new GLESprite(YELLOW_COLOR,-1, 0f);
        mSpriteLeft = new GLESprite(YELLOW_COLOR,0,0f);
        mSpriteRight = new GLESprite(YELLOW_COLOR,1,0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (Util.DEBUG_RENDERER) {
            Log.i(Util.LOG_TAG_RENDERING, "onSurfaceChanged width= ");
        }
        GLES20.glViewport(0, 0, width, height);

//        mainBlobX = width * X_POSITION;
        leftBlobX = width * X_POSITION_LEFT;
        rightBlobX = width * X_POSITION_RIGHT;
        leftBlobY = height * Y_POSITION;
        rightBlobY = height * Y_POSITION;

//        mSprite.setCenterX(mainBlobX);
//        mSprite.setCenterY(mainBlobY);
//        mSprite.setRadius(radius);
        mSpriteLeft.setCenterX(leftBlobX);
        mSpriteLeft.setCenterY(leftBlobY);
        mSpriteLeft.setRadius(radius);
        mSpriteRight.setCenterX(rightBlobX);
        mSpriteRight.setCenterY(rightBlobY);
        mSpriteRight.setRadius(radius);
        mSpriteLeft.setChart(chart);
        mSpriteRight.setChart(chart);
        mSpriteLeft.setGrey(greyE,greySquare);
        mSpriteRight.setGrey(greyE,greySquare);
        if (Util.DEBUG_RENDERER) {
            Log.i(Util.LOG_TAG_RENDERING, "onSurfaceChanged width= "+width+ " height= "+height+
                    " leftBlobX= " + leftBlobX + " leftBlobY= " + leftBlobY +
                    " rightBlobX= " + rightBlobX+ " rightBlobY= "+rightBlobY);
        }
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

    public void setGrey(int eye, int e, int s){
        if (Util.DEBUG_RENDERER) {
            Log.i(Util.LOG_TAG_RENDERING, "setGrey E= "+
                    e + " square= " + s + " eye= " + eye);
        }
        if(mSpriteLeft!=null && mSpriteRight!=null) {
            if (eye == 0) {
                mSpriteLeft.setGrey(e, s);
            } else {
                mSpriteRight.setGrey(e, s);
            }
        }
    }

    public void setChart(int c, int e, String learnChart, float optotypeOuterDiameter) {
        if (Util.DEBUG_RENDERER) {
            Log.i(Util.LOG_TAG_RENDERING, "setChart chart= "+
                    c + " eye= " + e + " learnChart= "+learnChart);
        }
        chart=c;
        eye = e;
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
            if(pos==5){
                radius = (int) (optotypeOuterDiameter * CORRECTION);
            }else{
                radius=540;
            }
        }else if(chart==-1){
            radius=ORG_RADIUS;
        }else{
            radius=540;
        }

        if (Util.DEBUG_RENDERER) {
            Log.i(Util.LOG_TAG_RENDERING, "setChart Sprite chart= "+
                    c + " radius= " + radius + " optotypeOuterDiameter= "+(int) optotypeOuterDiameter);
        }
        if(mSpriteLeft!=null && mSpriteRight!=null){
            mSpriteLeft.setRadius(radius);
            mSpriteRight.setRadius(radius);
            mSpriteLeft.setChart(chart);
            mSpriteRight.setChart(chart);
            mSpriteLeft.setPixelNbr((int) optotypeOuterDiameter);
            mSpriteRight.setPixelNbr((int) optotypeOuterDiameter);
        }
    }

    public void setLeftPositionX(float delta) {
        int d= (int) (leftBlobX-mSpriteLeft.getCenterX());
        if (Util.DEBUG_RENDERER) {
            Log.i(Util.LOG_TAG_RENDERING, "setLeftPositionX d= "+d+" delta= "+delta);
        }
        if(delta>0){
            if(d<MAX_XY_DELTA) {
                mSpriteLeft.setCenterX(mSpriteLeft.getCenterX() - delta);
            }
        }else{
            if(d>=-MAX_XY_DELTA && d<=MAX_XY_DELTA) {
                mSpriteLeft.setCenterX(mSpriteLeft.getCenterX() - delta);
            }
        }
    }

    public void setRightPositionX(float delta) {
        int d= (int) (rightBlobX-mSpriteRight.getCenterX());
        if (Util.DEBUG_RENDERER) {
            Log.i(Util.LOG_TAG_RENDERING, "setRightPositionX d= "+d+" delta= "+delta);
        }
        if(delta<0){
            if(d<MAX_XY_DELTA) {
                mSpriteRight.setCenterX(mSpriteRight.getCenterX() + delta);
            }
        }else{
            if(d>=-MAX_XY_DELTA && d<=MAX_XY_DELTA) {
                mSpriteRight.setCenterX(mSpriteRight.getCenterX() + delta);
            }
        }
    }

    public void setLeftPositionY(float delta) {
        int d= (int) (leftBlobY-mSpriteLeft.getCenterY());
        if (Util.DEBUG_RENDERER) {
            Log.i(Util.LOG_TAG_RENDERING, "setLeftPositionY d= "+d+" delta= "+delta);
        }
        if(delta>0){
            if(d<MAX_XY_DELTA) {
                mSpriteLeft.setCenterY(mSpriteLeft.getCenterY() - delta);
            }
        }else{
            if(d>=-MAX_XY_DELTA && d<=MAX_XY_DELTA) {
                mSpriteLeft.setCenterY(mSpriteLeft.getCenterY() - delta);
            }
        }
    }

    public void setRightPositionY(float delta) {
        int d= (int) (rightBlobY-mSpriteRight.getCenterY());
        if (Util.DEBUG_RENDERER) {
            Log.i(Util.LOG_TAG_RENDERING, "setRightPositionY d= "+d+" delta= "+delta);
        }
        if(delta<0){
            if(d<MAX_XY_DELTA) {
                mSpriteRight.setCenterY(mSpriteRight.getCenterY() + delta);
            }
        }else{
            if(d>=-MAX_XY_DELTA && d<=MAX_XY_DELTA) {
                mSpriteRight.setCenterY(mSpriteRight.getCenterY() + delta);
            }
        }
    }

    public void resetUser(boolean newUser) {
        if(newUser){
            mSpriteLeft.setCenterX(leftBlobX);
            mSpriteLeft.setCenterY(leftBlobY);
            mSpriteRight.setCenterX(rightBlobX);
            mSpriteRight.setCenterY(rightBlobY);
        }
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

    public float getLeftPositionX() {
        return mSpriteLeft.getCenterX();
    }
    public float getRightPositionX() {
        return mSpriteRight.getCenterX();
    }

    public float getLeftPositionY() {
        return mSpriteLeft.getCenterY();
    }
    public float getRightPositionY() {
        return mSpriteRight.getCenterY();
    }
    public void setLeftCenterX(float x) {
        mSpriteLeft.setCenterX(x);
    }
    public void setRightCenterX(float x) {
        mSpriteRight.setCenterX(x);
    }

    public void setLeftCenterY(float y) {
        mSpriteLeft.setCenterY(y);
    }
    public void setRightCenterY(float y) {
        mSpriteRight.setCenterY(y);
    }

    public void setPixelNbr(int eyeCalibration, int pixelNbr) {
        if(eyeCalibration==0){
            mSpriteLeft.setPixelNbr(pixelNbr);
        }else{
            mSpriteRight.setPixelNbr(pixelNbr);
        }
    }
}
