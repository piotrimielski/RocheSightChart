package com.givevision.rochesightchart.old;

import android.content.Context;
import android.opengl.GLES20;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static com.givevision.rochesightchart.old.Shaders.fragmentShaderCode;
import static com.givevision.rochesightchart.old.Shaders.loadTexture;
import static com.givevision.rochesightchart.old.Shaders.vertexShaderCode;

public  class SpriteRight {
	//Reference to Activity Context
	private final Context mActivityContext;

	//Added for Textures
	private final FloatBuffer mTextureCoordinates;
	private int mTextureUniformHandle;
	private int mTextureCoordinateHandle;
	private final int mTextureCoordinateDataSize = 2;
	private int mTextureDataHandle;



	private final int shaderProgram;    
	private final FloatBuffer vertexBuffer;
	private final ShortBuffer drawListBuffer;
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;
	
	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 2;


	private short drawOrder[] = { 0, 1, 2, 1,3,2 }; //Order to draw vertices
	private final int vertexStride = COORDS_PER_VERTEX * 4; //Bytes per vertex

	// Set color with red, green, blue and alpha (opacity) values
	float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

	public SpriteRight(final Context activityContext, float transX){
	    this.mActivityContext = activityContext;
		float transXL=0.0f;
		float transXR=0.0f;
		if(transX>0){
			transXL=transX;
			transXR=transX;
		}else{
			transXR=transX;
		}
		float spriteCoords[] = {
				0.0f-transXR, -0.65f, // bottom right
				-1.0f-transXL, -0.65f, // bottom left
				0.0f-transXR, 0.65f, // top right
				-1.0f-transXL, 0.65f //top left
		};
	    //Initialize Vertex Byte Buffer for Shape Coordinates / # of coordinate values * 4 bytes per float
	    ByteBuffer bb = ByteBuffer.allocateDirect(spriteCoords.length * 4); 
	    //Use the Device's Native Byte Order
	    bb.order(ByteOrder.nativeOrder());
	    //Create a floating point buffer from the ByteBuffer
	    vertexBuffer = bb.asFloatBuffer();
	    //Add the coordinates to the FloatBuffer
	    vertexBuffer.put(spriteCoords);
	    //Set the Buffer to Read the first coordinate
	    vertexBuffer.position(0);
	    
	    // S, T (or X, Y)
	    // Texture coordinate data.
	    // Because images have a Y axis pointing downward (values increase as you move down the image) while
	    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
	    // What's more is that the texture coordinates are the same for every face.
	    final float[] textureCoordinateData =	 {
				0, 1,
				1, 1,
	    		0, 0,
				1, 0
	    } ;


	    mTextureCoordinates = ByteBuffer.allocateDirect(textureCoordinateData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	    mTextureCoordinates.put(textureCoordinateData).position(0);

	    //Initialize byte buffer for the draw list
	    ByteBuffer dlb = ByteBuffer.allocateDirect(spriteCoords.length * 2);
	    dlb.order(ByteOrder.nativeOrder());
	    drawListBuffer = dlb.asShortBuffer();
	    drawListBuffer.put(drawOrder);
	    drawListBuffer.position(0);

	    int vertexShader = Shaders.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
	    int fragmentShader = Shaders.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

	    shaderProgram = GLES20.glCreateProgram();
	    GLES20.glAttachShader(shaderProgram, vertexShader);
	    GLES20.glAttachShader(shaderProgram, fragmentShader);

	    //Texture Code
	    GLES20.glBindAttribLocation(shaderProgram, 0, "a_TexCoordinate");

	    GLES20.glLinkProgram(shaderProgram);

	  //Load the texture
//	    mTextureDataHandle = loadTexture(mActivityContext, 0);
	    
	}

	public void setTexture(final int resourceId){
		//		 mTextureDataHandle = loadTextureWithPic(bmpScreen);
		mTextureDataHandle = loadTexture(mActivityContext,null,resourceId);

	}

	public void Draw(float[] mvpMatrix){
//		 mTextureDataHandle = loadTextureWithPic(bmpScreen);
//		mTextureDataHandle = loadTexture(mActivityContext,bmpScreen,resourceId);

	    //Add program to OpenGL ES Environment
	    GLES20.glUseProgram(shaderProgram);

	    //Get handle to vertex shader's vPosition member
	    mPositionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");

	    //Enable a handle to the triangle vertices
	    GLES20.glEnableVertexAttribArray(mPositionHandle);

	    //Prepare the triangle coordinate data
	    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

	    //Get Handle to Fragment Shader's vColor member
	    mColorHandle = GLES20.glGetUniformLocation(shaderProgram, "vColor");

	    //Set the Color for drawing the triangle
	    GLES20.glUniform4fv(mColorHandle, 1, color, 0);

	    //Set Texture Handles and bind Texture
	    mTextureUniformHandle = GLES20.glGetAttribLocation(shaderProgram, "u_Texture");
	    mTextureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "a_TexCoordinate");

	    //Set the active texture unit to texture unit 0.
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

	    //Bind the texture to this unit.
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

	    //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
	    GLES20.glUniform1i(mTextureUniformHandle, 0); 

	    //Pass in the texture coordinate information
	    mTextureCoordinates.position(0);
	    GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mTextureCoordinates);
	    GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

	    //Get Handle to Shape's Transformation Matrix
	    mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");

	    //Apply the projection and view transformation
	    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

	    //Draw the triangle
	    GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

	    //Disable Vertex Array
	    GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glFlush();
	}


}