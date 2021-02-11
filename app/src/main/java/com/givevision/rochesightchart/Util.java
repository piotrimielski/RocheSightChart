package com.givevision.rochesightchart;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.givevision.rochesightchart.db.Acuity;
import com.givevision.rochesightchart.db.AcuityRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

/**
 *
 */
public class Util {

    public static final String TAG = "GLUtil";

    public static final boolean DEBUG = true;
    public static final String LOG_TAG_MAIN = "MainActivity";
    public static final String LOG_TAG_KEY = "keyAction";
    public static final String LOG_TAG_RECOGNITION = "Recognition";
    public static final String LOG_TAG_VOICE = "Voice Command";
    public static final String LOG_TAG_DB = "Database";
    public static final String LOG_TAG_RENDERING = "Rendering";
    public static final String LOG_TAG_LEARN = "Learn";
    public static final String LOG_TAG_SPRITE = "Sprite";
    public static final int KEY_NBR = 5; //number of check key touched
    protected static final String PREF_USER_ID = "user Id";
    protected static final String PREF_LEFT_CALIBRATION_X = "left eye calibration x";
    protected static final String PREF_RIGHT_CALIBRATION_X = "right eye calibration x";
    protected static final String PREF_LEFT_CALIBRATION_Y = "left eye calibration y";
    protected static final String PREF_RIGHT_CALIBRATION_Y = "right eye calibration y";
    protected static final String PREF_LEFT_START = "left eye chart start";
    protected static final String PREF_RIGHT_START = "right eye chart start";
    protected static final String PREF_BLIND_EYE = "blind eye"; //0-left, 1-right, 2-both
    protected static final String MyPREFERENCES = "my preferences";
    protected static final String PREF_M_LEFT = "M-Unit left";
    protected static final String PREF_M_RIGHT = "M-Unit right";
    protected static final String PREF_RESULT_OF_4_LEFT = "result of 4 left";
    protected static final String PREF_RESULT_OF_4_RIGHT = "result of 4 right";

    private static SharedPreferences sharedpreferences;
    public static final int KEY_POWER = 100;
    public static final int KEY_TRIGGER = 96;
    public static final int KEY_UP = 19;
    public static final int KEY_DOWN = 20;
    public static final int KEY_LEFT = 21;
    public static final int KEY_RIGHT = 22;
    public static final int KEY_BACK = 97;

    public static final String SSID = "givevision";
    public static final String SSIDPW = "!GV-vat-00";
    public static final String URL = "http://givevision.net/helpdesk/acuity";

    /**
     * check info from server.
     *
     * @return id of OK or -1 as error
     */
    public int readJSONFromServer(String str) {
        JSONObject myJson = null;
        try {
            myJson = new JSONObject(str);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException " + e);
        }

        if (myJson != null) {
            int id = myJson.optInt("appID");
            int code = myJson.optInt("code");
            if (Util.DEBUG) {
                Log.i(TAG, "myJson code= " + code + " id= " + id);
            }
            if (code == 100) {
                return id;
            } else {
                return -1;
            }
        }
        return -1;
    }


    /**
     * create json from row of acuity in database.
     *
     * @return id of OK or -1 as error
     */
    public static String writeJSONToString(Acuity acuity, String imei) {
        JSONObject object = new JSONObject();
        try {

            object.put("appID", acuity.getId());
            object.put("userID", acuity.getUserId());
            object.put("createdAt", getDateTime(acuity.getCreatedAt()));
            object.put("left", acuity.getLeftEye());
            object.put("right", acuity.getRightEye());
            object.put("imei", imei);
            String str = String.valueOf(acuity.getId()) + String.valueOf(acuity.getUserId()) +
                    acuity.getLeftEye() + acuity.getRightEye()+imei;
            if (Util.DEBUG) {
                Log.i(TAG, "myJson str= " + str);
            }
            long check = checksum(str);
            object.put("checksum", check);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException " + e);
        }
        if (Util.DEBUG) {
            Log.i(TAG, "myJson " + object.toString());
        }
        return object.toString();
    }

    public static long checksum(String msg) {
        byte[] arr = msg.getBytes();
        Checksum checksum = new Adler32();
        checksum.update(arr, 0, arr.length);
        long res = checksum.getValue();
        if (Util.DEBUG) {
            Log.i(TAG, " checksum= " + res);
        }
        return res;
    }

    public static String getDateTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    //https://developer.android.com/training/volley
    // Post Request For JSONObject
    /**
     * send post request.
     *  @param ctx activity context
     * @param acuityRepository
     * @param imei phone imei
     * @param acuity object to send to server
     */
    public static void postData(final Context ctx, final AcuityRepository acuityRepository, final String imei, Acuity acuity) {
//        RequestQueue requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        final Context context=ctx;
        String str="";
        JSONObject object = new JSONObject();
        try {
            //input your API parameters
            if(acuity!=null){
                object.put("appID", acuity.getId());
                object.put("userID", acuity.getUserId());
                object.put("contrast", acuity.getContrast());
                object.put("duration", acuity.getDuration());
                object.put("leftFirst", acuity.getLeftEyeFirst());
                object.put("rightFirst", acuity.getRightEyeFirst());
                object.put("left", acuity.getLeftEye());
                object.put("right", acuity.getRightEye());
                object.put("createdAt", getDateTime(acuity.getCreatedAt()));
                object.put("imei",imei);
                str = Integer.toString(acuity.getId()) + Integer.toString(acuity.getUserId()) +
                        acuity.getLeftEye() + acuity.getRightEye()+ imei;

            }else{
                object.put("appID", -1);
                object.put("userID",-1);
                object.put("imei",imei);
                str = -1+imei;
            }

            if (Util.DEBUG) {
                Log.i(TAG, "checksum str= " + str);
            }
            long check = checksum(str);
            object.put("checksum", check);
            if (Util.DEBUG) {
                Log.i(TAG, "object = "+object.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException "+e);
        }
        // Enter the correct url for your api service site
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.POST, URL, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    int appID=response.optInt("appID");
                    int code= response.optInt("code");
                    final int userID= response.optInt("userID");
                    String status= response.optString("status");
                    if (Util.DEBUG) {
                        Log.i(TAG, "myJson code= "+code +" id= "+appID+" status= "+status);
                    }
                    if(code==100){
                        if(getSharedPreferences(context).getInt(PREF_USER_ID,-1)!=userID){
                            upDatePref(context, PREF_USER_ID,userID);
                            if (Util.DEBUG) {
                                Log.i(TAG, "VolleyResponse userID= "+userID);
                            }
                        }
                        //modify in database
                        if(appID>0){
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    if (Util.DEBUG) {
                                        Log.i(TAG, "VolleyResponse upDateInServer userID= "+userID);
                                    }
                                    acuityRepository.upDateInServer(appID);

                                }
                            });
                        }
                    }else{
                        Log.e(TAG, "VolleyResponse code= "+code+" appID= "+appID+" status= "+status);
                        if(code== 211){
                            //exist in database
                            if(appID>0){
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (Util.DEBUG) {
                                            Log.i(TAG, "VolleyResponse upDateInServer userID= "+userID);
                                        }
                                        acuityRepository.upDateInServer(appID);

                                    }
                                });
                            }
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "VolleyError "+error);
                }
            }
        );
        jsonObjectRequest.setTag(LOG_TAG_MAIN);
        RequestQueueSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
//        requestQueue.add(jsonObjectRequest);
    }



    // Get Request For JSONObject
    /**
     * send get request.
     *
     * @param ctx activity context
     * @param imei phone imei
     * @param acuity object to send to server
     */
    protected static void getData( Context ctx,String imei, Acuity acuity){
//        RequestQueue requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        JSONObject object = new JSONObject();
        try {
            //input your API parameters
            object.put("appID", acuity.getId());
            object.put("userID", acuity.getUserId());
            object.put("createdAt", getDateTime(acuity.getCreatedAt()));
            object.put("left", acuity.getLeftEye());
            object.put("right", acuity.getRightEye());
            object.put("imei",imei);
            String str = String.valueOf(acuity.getId()) + String.valueOf(acuity.getUserId()) +
                    acuity.getLeftEye() + acuity.getRightEye()+ imei;
            if (Util.DEBUG) {
                Log.i(TAG, "checksum str= " + str);
            }
            long check = checksum(str);
            object.put("checksum", check);
            if (Util.DEBUG) {
                Log.i(TAG, "object = "+object.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException "+e);
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
           Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
               @Override
               public void onResponse(JSONObject response) {
                   int id=response.optInt("appID");
                   int code= response.optInt("code");
                   if (Util.DEBUG) {
                       Log.i(TAG, "myJson code= "+code +" id= "+id);
                   }
                   if(code==100){

                   }else{

                   }
               }
           }, new Response.ErrorListener() {
               @Override
               public void onErrorResponse(VolleyError error) {
                   Log.e(TAG, "VolleyError "+error);
               }
           }
        );
        jsonObjectRequest.setTag(LOG_TAG_MAIN);
        RequestQueueSingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
//        requestQueue.add(jsonObjectRequest);
    }

    protected static void upDatePref(Context ctx, String key, String value){
        SharedPreferences.Editor editor = ctx.getSharedPreferences(Util.MyPREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }
    protected static void upDatePref(Context ctx, String key, int value){
        SharedPreferences.Editor editor = ctx.getSharedPreferences(Util.MyPREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.commit();
    }
    protected static void upDatePref(Context ctx, String key, float value){
        SharedPreferences.Editor editor = ctx.getSharedPreferences(Util.MyPREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putFloat(key, value);
        editor.commit();
    }
    protected static void upDatePref(Context ctx, String key, boolean value){
        SharedPreferences.Editor editor = ctx.getSharedPreferences(Util.MyPREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    protected static SharedPreferences getSharedPreferences(Context ctx) {
        return ctx.getSharedPreferences(Util.MyPREFERENCES, Context.MODE_PRIVATE);
    }

    public static ArrayList<String> readConfigFile(String path){
        ArrayList<String> arrayOfString=new ArrayList<>();;
        try {
            RandomAccessFile reader = new RandomAccessFile(path, "r");
            reader.seek(0);
            String str=" ";
            while(str!=null){
                str=reader.readLine();
                if(str!=null){
                    Log.i(TAG, "stat "+str);
                    arrayOfString.add(str);
                }
            }
            return arrayOfString;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "readFile FileNotFoundException  "+e);
        } catch (IOException e) {
            Log.e(TAG, "readFile IOException  "+e);
        }
        return null;
    }
}
