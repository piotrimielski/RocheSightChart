package com.givevision.rochesightchart.old;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.util.Log;

import com.givevision.rochesightchart.R;
import com.givevision.rochesightchart.Util;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;

public class
MyGLRenderer implements GLSurfaceView.Renderer{
    private Context context;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];
    public volatile float mAngle = 0f;
    private SpriteLeft sprite1;
    private SpriteRight sprite2;
    private float ratio;
    private float mTransX;
    private static int pos=-1;
    private static int eyeToShow=0;

    public MyGLRenderer(Context ctx) {
        this.context = ctx;

        final Handler handler=new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                pos=pos+1;
//                if(pos>5){
//                    pos=1;
//                }
//                handler.postDelayed(this,5000);
//            }
//        },5000);
    }

    public  void setChart(int chart, int eye) {
        pos=chart;
        eyeToShow = eye;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0f, 0f, 0f, 1.0f);
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RENDERING, "onSurfaceCreated init sprite");
        }
        sprite1 = new SpriteLeft(context, 0.15f);
        sprite2 = new SpriteRight(context, 0.15f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);



        ratio = (float) width / height;
        if(ratio<1){
            ratio = (float) height / width;
        }
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RENDERING, "onSurfaceChanged width= "+width+" height="+height);
            Log.i(Util.LOG_TAG_RENDERING, "onSurfaceChanged init mProjectionMatrix ratio= "+ratio);
        }
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        if(ratio<1){
            Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 2, 7);
        }else{
            switch (pos){
                case  -1:
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio*0.5f, ratio*0.5f, -2f, 2f, 2, 7);
                    break;
                case  0:
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio*0.5f, ratio*0.5f, -2f, 2f, 2, 7);
                    break;
                case  1:
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio*0.5f, ratio*0.5f, -3f, 3f, 2, 7);
                    break;
                case 2:
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio*0.5f, ratio*0.5f, -4f, 4f, 2, 7);
                    break;
                case  3:
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio*0.5f, ratio*0.5f, -5f, 5f, 2, 7);
                    break;
                case  4:
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio*0.5f, ratio*0.5f, -6f, 6f, 2, 7);
                    break;
                default:
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio*0.5f, ratio*0.5f, -2f, 2f, 2, 7);
                    break;
            }

        }
    }

    private boolean isDrowed=false;

    @Override
    public void onDrawFrame(GL10 gl) {
        drawPicTexture(null);
    }

    public void setPosX(float transX){
        this.mTransX=transX;
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RENDERING, "drawPicTexture setPosXY:  transX= " + transX);
        }
    }

    int posOld=-2;
    private void drawPicTexture(Bitmap bmp) {
        if (pos!=posOld){
//            sprite1 = new SpriteLeft(context,mTransX);
//            sprite2 = new SpriteRight(context,mTransX);
            if (ratio < 1) {
                Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 2, 7);
            } else {
                if (pos == -1) {
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio * 0.5f, ratio * 0.5f, -0.7f, 0.7f, 2, 7);
                    sprite1.setTexture(R.drawable.testimg);
                    sprite2.setTexture(R.drawable.testimg);
                } else if (pos == 0) {
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio * 0.5f, ratio * 0.5f, -2f, 2f, 2, 7);
                    sprite1.setTexture(R.drawable.chart1);
                    sprite2.setTexture(R.drawable.chart1);
                } else if (pos == 1) {
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio * 0.5f, ratio * 0.5f, -3f, 3f, 2, 7);
                    sprite1.setTexture(R.drawable.chart2);
                    sprite2.setTexture(R.drawable.chart2);
                } else if (pos == 2) {
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio * 0.5f, ratio * 0.5f, -4f, 4f, 2, 7);
                    sprite1.setTexture(R.drawable.chart3);
                    sprite2.setTexture(R.drawable.chart3);
                } else if (pos == 3) {
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio * 0.5f, ratio * 0.5f, -5f, 5f, 2, 7);
                    sprite1.setTexture(R.drawable.chart4);
                    sprite2.setTexture(R.drawable.chart4);
                } else if (pos == 4) {
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio * 0.5f, ratio * 0.5f, -6f, 6f, 2, 7);
                    sprite1.setTexture(R.drawable.chart5);
                    sprite2.setTexture(R.drawable.chart5);
                } else {
                    Matrix.frustumM(mProjectionMatrix, 0, -ratio * 0.5f, ratio * 0.5f, -2f, 2f, 2, 7);
                    if(eyeToShow==0){
                        sprite1.Draw(mMVPMatrix);
                    }else if(eyeToShow==1){
                        sprite2.Draw(mMVPMatrix);
                    }else {
                        sprite1.Draw(mMVPMatrix);
                        sprite2.Draw(mMVPMatrix);
                    }
                }
            }

            //Set the camera position (View Matrix)
            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            //Calculate the projection and view transformation
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

            //Create a rotation transformation for the triangle
            Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);

            //Combine the rotation matrix with the projection and camera view
            Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);

            posOld=pos;
        }else{
            glClear(GL_COLOR_BUFFER_BIT);
            if(eyeToShow==0){
                sprite1.Draw(mMVPMatrix);
            }else if(eyeToShow==1){
                sprite2.Draw(mMVPMatrix);
            }else {
                sprite1.Draw(mMVPMatrix);
                sprite2.Draw(mMVPMatrix);
            }
        }

    }


}
