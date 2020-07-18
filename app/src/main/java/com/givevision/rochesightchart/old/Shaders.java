package com.givevision.rochesightchart.old;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.givevision.rochesightchart.Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Shaders {

    private static final int SIZEOF_FLOAT = 4;

    static final String vertexShaderCode =
            "attribute vec2 a_TexCoordinate;" +
            "varying vec2 v_TexCoordinate;" +
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = vPosition * uMVPMatrix;" +
            "  v_TexCoordinate = a_TexCoordinate;" +
            "}";

    static final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "uniform sampler2D u_Texture;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main() {" +
                    "gl_FragColor = (vColor * texture2D(u_Texture, v_TexCoordinate));" +
                    "}";

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;
    static float spriteCoords[] = { -0.5f,  0.5f,   // top left
            -0.5f, -0.5f,   // bottom left
            0.5f, -0.5f,   // bottom right
            0.5f,  0.5f }; //top right

    static short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; //Order to draw vertices
    static final int vertexStride = COORDS_PER_VERTEX * 4; //Bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    static float myColor[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    // S, T (or X, Y)
    // Texture coordinate data.
    // Because images have a Y axis pointing downward (values increase as you move down the image) while
    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
    // What's more is that the texture coordinates are the same for every face.
    static final float[] cubeTextureCoordinateData =
            {
                    -0.5f,  0.5f,
                    -0.5f, -0.5f,
                    0.5f, -0.5f,
                    0.5f,  0.5f
            };


    /**
     * Creates a texture from raw data.
     *
     * @param data Image data, in a "direct" ByteBuffer.
     * @param width Texture width, in pixels (not bytes).
     * @param height Texture height, in pixels.
     * @param format Image data format (use constant appropriate for glTexImage2D(), e.g. GL_RGBA).
     * @return Handle to texture.
     */
    public static int createImageTexture(ByteBuffer data, int width, int height, int format) {
        int[] textureHandles = new int[1];
        int textureHandle;

        GLES20.glGenTextures(1, textureHandles, 0);
        textureHandle = textureHandles[0];
        checkGlError("glGenTextures");

        // Bind the texture handle to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);

        // Configure min/mag filtering, i.e. what scaling method do we use if what we're rendering
        // is smaller or larger than the source image.
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        checkGlError("loadImageTexture");

        // Load the data from the buffer into the texture handle.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, /*level*/ 0, format,
                width, height, /*border*/ 0, format, GLES20.GL_UNSIGNED_BYTE, data);
        checkGlError("loadImageTexture");

        return textureHandle;
    }

    /**
     * Allocates a direct float buffer, and populates it with the float array data.
     */
    public static FloatBuffer createFloatBuffer(float[] coords) {
        // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZEOF_FLOAT);
        //Use the Device's Native Byte Order
        bb.order(ByteOrder.nativeOrder());
        //Create a floating point buffer from the ByteBuffer
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }

    /**
     *
     * @param bmpScreen
     * @return
     */
    public int loadTextureWithPic(Bitmap bmpScreen) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);
        checkGlError("glGenTextures");

        if (textureHandle[0] != 0){
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            checkGlError("loadImageTexture");

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmpScreen, 0);
            checkGlError("loadImageTexture");
            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bmpScreen.recycle();
        }
        if (textureHandle[0] == 0){
            throw new RuntimeException("Error loading texture.");
        }
//		mTextureDataHandle=textureHandle[0];
        return textureHandle[0];
    }

    /**
     *
     * @param context
     * @param bmpScreen
     * @param resourceId
     * @return
     */
    public static int loadTexture(final Context context, final Bitmap bmpScreen, final int resourceId){
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);
        checkGlError("glGenTextures");
        if (textureHandle[0] != 0){
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            // Read in the resource
            Bitmap bitmap;
            if(bmpScreen==null){
                bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
//                bitmap=inversBitmap(bitmap);
            }else{
                Bitmap croppedBitmap = Bitmap.createBitmap(bmpScreen, 0, 0,bmpScreen.getWidth()/2*3,bmpScreen.getHeight());
                bitmap=croppedBitmap.copy(Bitmap.Config.ARGB_8888,false);
//		     String path=Environment.getExternalStorageDirectory()+"/photo.jpg";
//		     Log.v("sprite","path "+path);
//		     Bitmap bitmap = BitmapFactory.decodeFile(path, options);
//		     bitmap=inversBitmap(bitmap);
            }

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            checkGlError("bindTexture");
            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            checkGlError("setFilteringTexture");
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            checkGlError("loadImageTexture");
            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0){
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    /**
     *
     * @param image
     * @return
     */
    private  static Bitmap inversBitmap(Bitmap image){
        int height=image.getHeight();
        int width=image.getWidth();

        Bitmap srcBitmap=Bitmap.createBitmap(width, height, image.getConfig());

        for (int y=width-1;y>=0;y--)
            for(int x=0;x<height;x++)
                srcBitmap.setPixel(y,height-x-1,image.getPixel(y,x));
        return srcBitmap;

    }

    /**
     * Creates a texture from drawable.
     *
     * @param context
     * @param resourceId
     * @return Handle to texture.
     */
    public static int loadTexture(final Context context, final int resourceId){
        final int[] textureHandles = new int[1];
        int textureHandle;

        GLES20.glGenTextures(1, textureHandles, 0);
        textureHandle = textureHandles[0];
        checkGlError("glGenTextures");
        if (textureHandle != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            checkGlError("loadImageTexture");

            // Load the bitmap into the bound texture.
            // Load the data from the buffer into the texture handle.
            //GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, /*level*/ 0, format,
            //        width, height, /*border*/ 0, format, GLES20.GL_UNSIGNED_BYTE, data);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            checkGlError("loadImageTexture");
            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle == 0){
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle;
    }

    /**
     * Compiles the provided shader source
     *
     * @param type
     * @param shaderCode
     * @return A handle to the shader, or 0 on failure.
     */
    static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        checkGlError("glCreateShader type=" + type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            if (Util.DEBUG) {
                Log.e(Util.LOG_TAG_RENDERING, "Could not compile shader " + type + ":");
                Log.e(Util.LOG_TAG_RENDERING, " " + GLES20.glGetShaderInfoLog(shader));
            }
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        return shader;
    }

    /**
     * Creates a new program from the supplied vertex and fragment shaders.
     *
     * @return A handle to the program, or 0 on failure.
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) {
            Log.e(Util.LOG_TAG_RENDERING, "Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            if (Util.DEBUG) {
                Log.e(Util.LOG_TAG_RENDERING, "Could not link program: ");
                Log.e(Util.LOG_TAG_RENDERING, GLES20.glGetProgramInfoLog(program));
            }
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    /**
     * Checks to see if a GLES error has been raised.
     */
    static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            if (Util.DEBUG) {
                Log.e(Util.LOG_TAG_RENDERING, glOperation + ": glError 0x " + Integer.toHexString(error));
            }
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Checks to see if the location we obtained is valid.  GLES returns -1 if a label
     * could not be found, but does not set the GL error.
     * <p>
     * Throws a RuntimeException if the location is invalid.
     */
    public static void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }

}
