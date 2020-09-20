package com.givevision.rochesightchart;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class AppTransfer {
	public static final String TAG = "AppTransfer";

	private int appID;
	private int userID;
	private String resultLeft;
	private String resultRight;
	private int checksum;
	private String date;
	
	public void setAppID(int appID){
		this.appID=appID;
	}
	public int getAppID(){
		return appID;
	}

	public void setUserID(int userID){
		this.userID=userID;
	}
	public int getUserID(){
		return userID;
	}

	public void setResultLeft(String resultLeft){
		this.resultLeft=resultLeft;
	}
	public String getResultLeft(){
		return resultLeft;
	}

	public void setResultRight(String resultRight){
		this.resultRight=resultRight;
	}
	public String getResultRight(){
		return resultRight;
	}

	public void setDate(String str){
		date=str;
	}
	public String getDate(){
		return date;
	}
	
	public  boolean readJSONFromServer(String str){
		JSONObject myJson = null;
		try {
			myJson = new JSONObject(str);
		} catch (JSONException e) {
			Log.e(TAG, "JSONException "+e);
		}
		   
		if(myJson!=null){
			setAppID(myJson.optInt("appID"));
			setUserID(myJson.optInt("userID"));
//			setMessage(myJson.optString("message"));
//			setType(myJson.optString("type"));
		}
		return false;
	}
	
	
	
	public static String writeJSONToString(String action, String message, String type) {
		JSONObject object = new JSONObject();
		try {
		   	object.put("action", action);
		   	object.put("message", message);
		   	object.put("type", type);
		   	object.put("date", getDateTime());
		     
		} catch (JSONException e) {
		   	Log.e(TAG, "JSONException "+e);
		}
		if (Util.DEBUG) {
			Log.i(TAG, "myJson "+object);
		}
		return object.toString();
	}

	public static long checksum(String msg){
		byte[] arr = msg.getBytes();
		Checksum checksum = new Adler32();
		checksum.update(arr, 0, arr.length);
		long res = checksum.getValue();
		if (Util.DEBUG) {
			Log.i(TAG, " checksum= "+res);
		}
		return res;
	}
	
	public static String[] stringToArray(String str, String sep){
		
		String[] arr=str.split(sep);
		
		return arr;
	}
	
	int width;
	int height; 
	byte[] byteArray;
	Bitmap bitmap;
	public byte[] BitmapToByte(Bitmap _bitmap){
		width = _bitmap.getWidth();
        height = _bitmap.getHeight();

        int size = _bitmap.getRowBytes() * _bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        _bitmap.copyPixelsToBuffer(byteBuffer);
        byteArray = byteBuffer.array();
        
        return byteArray;
	}
	
	public Bitmap ByteToBitmap(){
		Bitmap.Config configBmp = Bitmap.Config.valueOf(bitmap.getConfig().name());
        Bitmap bitmap_tmp = Bitmap.createBitmap(width, height, configBmp);
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        bitmap_tmp.copyPixelsFromBuffer(buffer);
		
        return bitmap_tmp;
	}
	
	 public static String getDateTime() {
	        SimpleDateFormat dateFormat = new SimpleDateFormat(
	                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	        Date date = new Date();
	        return dateFormat.format(date);
	}
}
