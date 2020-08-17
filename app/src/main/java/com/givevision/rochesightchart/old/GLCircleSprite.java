package com.givevision.rochesightchart.old;

import android.opengl.GLES20;
import android.util.Log;

import com.givevision.rochesightchart.BuildConfig;
import com.givevision.rochesightchart.Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class GLCircleSprite {
    private float[] mColor;
    private int mProgram;

    private float mCenterX;
    private float mCenterY;
    private float mRadius;

    private static final int COORDS_PER_VERTEX = 2;
    private static float VERTEX_COORDINATES[] = {
            -1f,   1f,   // top left
            -1f,  -1f,   // bottom left
            1f,  -1f,   // bottom right
            1f,   1f,   // top right
    };

    float VERTEX_LEFT_COORDINATES[]= {
            -1f,   1f,   // top left
            -1f,  -1f,   // bottom left
            -0.002f,  -1f,   // bottom right
            -0.002f,   1f,   // top right
    };

    private static float VERTEX_RIGHT_COORDINATES[] = {
            0.002f,   1f,   // top left
            0.002f,  -1f,   // bottom left
            1f,  -1f,   // bottom right
            1f,   1f,   // top right
    };

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;

    private final short mDrawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices
    private int mChart;

    public GLCircleSprite(float[] color, int eye) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_SPRITE, "constructor");
        }
        mColor = color;

        String vertexShaderSource = "" +
                "attribute vec2 aPosition; \n" +
                "void main() \n" +
                "{ \n" +
                "   gl_Position = vec4(aPosition, 0., 1.); \n" +
                "} \n";

        String fragmentShaderSource = "" +
                "precision highp float;\n" +
                "uniform vec2 aCirclePosition;\n" +
                "uniform float aRadius; \n" +
                "uniform vec4 aColor; \n" +
                "uniform int aChartPos; \n" +
                "uniform float aChart; \n" +
                "const float threshold = 0.005;\n" +
                "void main() \n" +
                "{ \n" +
                "   float d, dist, pos_y, pos_x, hole, gape;\n" +
                "   dist = distance(aCirclePosition, gl_FragCoord.xy);\n" +
                "   pos_y = aCirclePosition.y - gl_FragCoord.y;\n" +
                "   pos_x = aCirclePosition.x - gl_FragCoord.x;\n" +
                "   gape = 2. * aRadius / 5.; \n" +
                "   if(dist == 0.){\n" +
                "       dist = 1.;\n" +
                "   }\n"+
                "   d = aRadius / dist;\n" +
                "   if(dist < aRadius - gape){\n" +
                "     gl_FragColor = vec4(0.79, 0.81, 0.6, 1.);\n" +
                "   }else if(dist <= aRadius){\n" +
                "      if(aChart == -1.){\n" +
                "        gl_FragColor = vec4(0.5, 0.5, 0.5, 1.); \n" +
                "      }else if(aChartPos==2 && pos_y<gape/2. && pos_y>-gape/2. && pos_x<0.){\n" +
                "        gl_FragColor = vec4(0.79, 0.81, 0.6, 1.);\n" +
                "      }else if(aChartPos==4 && pos_y<gape/2. && pos_y>-gape/2. && pos_x>0.){\n" +
                "        gl_FragColor = vec4(0.79, 0.81, 0.6, 1.);\n" +
                "      }else if(aChartPos==1 && pos_x<gape/2. && pos_x>-gape/2. && pos_y<0.){\n" +
                "        gl_FragColor = vec4(0.79, 0.81, 0.6, 1.);\n" +
                "      }else if(aChartPos==3 && pos_x<gape/2. && pos_x>-gape/2. && pos_y>0.){\n" +
                "        gl_FragColor = vec4(0.79, 0.81, 0.6, 1.);\n" +
                "      }else if(aChartPos==5){\n" +
                "           if( pos_y<gape/5. && pos_y>-gape/5. && pos_x<0.){\n" +
                "               gl_FragColor = vec4(0.79, 0.81, 0.6, 1.);\n" +
                "           }else if( pos_y<gape/5. && pos_y>-gape/5. && pos_x>0.){\n" +
                "               gl_FragColor = vec4(0.79, 0.81, 0.6, 1.);\n" +
                "           }else if( pos_x<gape/5. && pos_x>-gape/5. && pos_y<0.){\n" +
                "               gl_FragColor = vec4(0.79, 0.81, 0.6, 1.);\n" +
                "           }else if( pos_x<gape/5. && pos_x>-gape/5. && pos_y>0.){\n" +
                "               gl_FragColor = vec4(0.79, 0.81, 0.6, 1.);\n" +
                "           }\n"+
                "      }else{\n"+
                "        gl_FragColor = aColor;\n" +
                "      }\n"+
//                "   }else if(d >= 1. - threshold) {\n" +
//                "        float a = (d - (1. - threshold)) / threshold;\n" +
//                "        gl_FragColor = vec4(aColor.r, aColor.g, aColor.b, a); \n" +
                "    }else{\n" +
                "        gl_FragColor = vec4(0.79, 0.81, 0.6, 1.);\n" +//vec4(0.79, 0.81, 0.6, 0.)
                "    }\n"+
                "} \n";

        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);
        mProgram = linkProgram(vertexShader, fragmentShader);
        if (BuildConfig.DEBUG) {
            validateProgram(mProgram);
        }

        if(eye==0){
            ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(VERTEX_LEFT_COORDINATES.length * 4);
            vertexByteBuffer.order(ByteOrder.nativeOrder());
            mVertexBuffer = vertexByteBuffer.asFloatBuffer();
            mVertexBuffer.put(VERTEX_LEFT_COORDINATES);
            mVertexBuffer.position(0);
        }else if(eye==1){
            ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(VERTEX_RIGHT_COORDINATES.length * 4);
            vertexByteBuffer.order(ByteOrder.nativeOrder());
            mVertexBuffer = vertexByteBuffer.asFloatBuffer();
            mVertexBuffer.put(VERTEX_RIGHT_COORDINATES);
            mVertexBuffer.position(0);
        }else{
            ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(VERTEX_COORDINATES.length * 4);
            vertexByteBuffer.order(ByteOrder.nativeOrder());
            mVertexBuffer = vertexByteBuffer.asFloatBuffer();
            mVertexBuffer.put(VERTEX_COORDINATES);
            mVertexBuffer.position(0);
        }


        ByteBuffer drawByteBuffer = ByteBuffer.allocateDirect(mDrawOrder.length * 2);
        drawByteBuffer.order(ByteOrder.nativeOrder());
        mDrawListBuffer = drawByteBuffer.asShortBuffer();
        mDrawListBuffer.put(mDrawOrder);
        mDrawListBuffer.position(0);
    }

    private static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_SPRITE, "linkProgram");
        }
        final int programObjectId = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (programObjectId == 0) {
            return 0;
        }

        GLES20.glAttachShader(programObjectId, vertexShaderId);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(programObjectId, fragmentShaderId);
        checkGlError("glAttachShader");

        GLES20.glLinkProgram(programObjectId);

        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);

        if (linkStatus[0] == 0) {
            // If it failed, delete the program object. glDeleteProgram(programObjectId);
            if (Util.DEBUG) {
                Log.e(Util.LOG_TAG_RENDERING, "Could not link program: ");
                Log.e(Util.LOG_TAG_RENDERING, GLES20.glGetProgramInfoLog(programObjectId));
            }
            GLES20.glDeleteProgram(programObjectId);
            return 0;
        }
        return programObjectId;
    }

    private static boolean validateProgram(int programObjectId) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_SPRITE, "validateProgram");
        }
        GLES20.glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        return validateStatus[0] != 0;
    }

    private static int compileVertexShader(String shaderCode) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_SPRITE, "compileVertexShader");
        }
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    private static int compileFragmentShader(String shaderCode) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_SPRITE, "compileFragmentShader");
        }
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderCode) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_SPRITE, "compileShader");
        }
        final int shaderObjectId = GLES20.glCreateShader(type);
        checkGlError("glCreateShader type=" + type);
        if (shaderObjectId == 0) {
            return 0;
        }
        GLES20.glShaderSource(shaderObjectId, shaderCode);
        GLES20.glCompileShader(shaderObjectId);

        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shaderObjectId);
            return 0;
        }
        return shaderObjectId;
    }

    public void draw(int pos) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glUseProgram(mProgram);

        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        int vertexStride = COORDS_PER_VERTEX * 4;
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

        GLES20.glUniform4fv(GLES20.glGetUniformLocation(mProgram, "aColor"), 1, mColor, 0);
        GLES20.glUniform2f(GLES20.glGetUniformLocation(mProgram, "aCirclePosition"), mCenterX, mCenterY);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(mProgram, "aRadius"), mRadius);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgram, "aChartPos"), pos);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(mProgram, "aChart"), (float)mChart);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mDrawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public float getCenterX() {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_SPRITE, "getCenterX");
        }
        return mCenterX;
    }

    public void setCenterX(float centerX) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_SPRITE, "setCenterX");
        }
        mCenterX = centerX;
    }

    public float getCenterY() {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_SPRITE, "getCenterY");
        }
        return mCenterY;
    }

    public void setCenterY(float centerY) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_SPRITE, "setCenterY");
        }
        mCenterY = centerY;
    }

    public float getRadius() {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_SPRITE, "getRadius");
        }
        return mRadius;
    }

    public void setRadius(float radius) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_SPRITE, "setRadius");
        }
        this.mRadius = radius;
    }

    /**
     * Checks to see if a GLES error has been raised.
     */
    static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            if (Util.DEBUG) {
                Log.e(Util.LOG_TAG_SPRITE, glOperation + ": glError 0x " + Integer.toHexString(error));
            }
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public void setChart(int chart) {
        this.mChart=chart;
    }
}
