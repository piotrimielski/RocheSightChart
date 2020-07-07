package com.givevision.rochesightchart;

import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glClearColor;

public abstract class  GLRenderer implements GLSurfaceView.Renderer {
    /** Store our model data in a float buffer. */
    private final FloatBuffer mTriangle1Vertices;
//    private final FloatBuffer mTriangle2Vertices;
//    private final FloatBuffer mTriangle3Vertices;

    /** How many bytes per float. */
    private final int mBytesPerFloat = 4;

    private boolean mFirstDraw;
    private boolean mSurfaceCreated;
    private int mWidth;
    private int mHeight;
    private long mLastTime;
    private int mFPS;


    public GLRenderer() {
        mFirstDraw = true;
        mSurfaceCreated = false;
        mWidth = -1;
        mHeight = -1;
        mLastTime = System.currentTimeMillis();
        mFPS = 0;

        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RENDERING, "init triangle1VerticesData");
        }

        final float[] triangle1VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.5f, -0.25f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f};

        mTriangle1Vertices = ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        mTriangle1Vertices.put(triangle1VerticesData).position(0);
    }

    /**
     *This method is called when the surface is first created.
     * It will also be called if we lose our surface context and it is later recreated by the system.
     *
     * @param notUsed
     * @param config
     */
    @Override
    public void onSurfaceCreated(GL10 notUsed, EGLConfig config) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RENDERING, "Surface created.");
        }
        mSurfaceCreated = true;
        mWidth = -1;
        mHeight = -1;
    }

    /**
     * This is called whenever the surface changes; for example, when switching
     * from portrait to landscape. It is also called after the surface has been created.
     *
     * @param notUsed
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(GL10 notUsed, int width, int height) {
        if (!mSurfaceCreated && width == mWidth  && height == mHeight) {
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_RENDERING, "Surface changed but already handled.");
            }
            return;
        }
        if (Util.DEBUG) {
            // Android honeycomb has an option to keep the
            // context.
            String msg = "Surface changed width:" + width + " height:" + height;
            if (mSurfaceCreated) {
                msg += " context lost.";
            } else {
                msg += ".";
            }
            Log.i(Util.LOG_TAG_RENDERING, msg);
        }

        mWidth = width;
        mHeight = height;

        onCreate(mWidth, mHeight, mSurfaceCreated);
        mSurfaceCreated = false;
    }

    /**
     * This is called whenever it’s time to draw a new frame.
     * @param notUsed
     */
    @Override
    public void onDrawFrame(GL10 notUsed) {
        onDrawFrame(mFirstDraw, mTriangle1Vertices);

        if (Util.DEBUG) {
            mFPS++;
            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastTime >= 1000) {
                mFPS = 0;
                mLastTime = currentTime;
            }
        }

        if (mFirstDraw) {
            mFirstDraw = false;
        }
    }

    public int getFPS() {
        return mFPS;
    }

    /**
     * To know if OpenGL is up and running
     *
     * @param width
     * @param height
     * @param contextLost
     */
    public abstract void onCreate(int width, int height, boolean contextLost);

    /**
     * to know when to draw stuff
     *
     * @param firstDraw
     */
    public abstract void onDrawFrame(boolean firstDraw, FloatBuffer mTriangle1Vertices);
}
