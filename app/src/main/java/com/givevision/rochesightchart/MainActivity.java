package com.givevision.rochesightchart;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


//https://cmusphinx.github.io/wiki/tutorialandroid/

public class MainActivity extends Activity implements  RecognitionListener  {
    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String CHARTS_SEARCH = "charts";
    private static final String NEXT_SEARCH = "next";
    private static final String START_SEARCH = "start";
    private static final String STOP_SEARCH = "stop";
    private static final String UP_SEARCH = "up";
    private static final String DOWN_SEARCH = "down";
    private static final String LEFT_SEARCH = "left";
    private static final String RIGHT_SEARCH = "right";
//    private static final String DIGITS_SEARCH = "digits";
//    private static final String MENU_SEARCH = "menu";
//    private static final String PHONE_SEARCH = "phones";
//    private static final String FORECAST_SEARCH = "forecast";
    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE_KWS = "check my eyes";
    private boolean isStarting=false;

    private RelativeLayout   relativeLayout  ;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;

    private GLSurfaceView mSurfaceView;
    private GLSurfaceView mGLView;
    private TextView mTextInfo1;
    private TextView mTextInfo2;
    private TextView mTextInfo3;
    private TextView mTextInfo4;

    private int offset1=220;
    private int offset2=-100;

    private int offset3=100;
    private int offset4=-220;

    private TextToSpeech mTTS = null;
    private final int ACT_CHECK_TTS_DATA = 1000;
    private boolean isTTS=false;

    private MyGLRenderer myGLRenderer;
    private LearnMachine learn;
    private int totalLengthCharts=0;
    private int totalLengthStringArray=0;
    private int chart=0;
    private int chartPos=0;
    private boolean learning=false;

    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, "onCreate");
        }

        // Prepare the data for UI
        captions = new HashMap<>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(CHARTS_SEARCH, R.string.charts);
//        captions.put(MENU_SEARCH, R.string.menu_caption);
//        captions.put(DIGITS_SEARCH, R.string.digits_caption);
//        captions.put(PHONE_SEARCH, R.string.phone_caption);
//        captions.put(FORECAST_SEARCH, R.string.forecast_caption);

        //full screen definition
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        relativeLayout = new RelativeLayout(this);
        relativeLayout.setId(AndroidUtils.generateViewId());
        relativeLayout.setKeepScreenOn(true);
        relativeLayout.setBackgroundColor(Color.DKGRAY);; //or whatever your image is

        //initialisation openGLES2
        if (hasGLES20()) {
            mGLView = new GLSurfaceView(this);
            mGLView.setEGLContextClientVersion(2);
            mGLView.setPreserveEGLContextOnPause(true);
            myGLRenderer=new MyGLRenderer(this);
            mGLView.setRenderer(myGLRenderer);
        } else {
            // Time to get a new phone, OpenGL ES 2.0 not
            // supported.
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, "onCreate width= "+width + " height= "+height);
        }

        mGLView.setId(AndroidUtils.generateViewId());
//        setContentView(mGLView);
        relativeLayout.addView(mGLView);

        mTextInfo1= new TextView(this);
        mTextInfo1.setText("nothing");
        mTextInfo1.setTextColor(Color.WHITE);
        mTextInfo1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(width/2, 50);
        params1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params1.setMargins(width/4+offset1, 300, 0, 0);
        mTextInfo1.setLayoutParams(params1);
        relativeLayout.addView(mTextInfo1);

        mTextInfo2= new TextView(this);
        mTextInfo2.setText("nothing");
        mTextInfo2.setTextColor(Color.WHITE);
        mTextInfo2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(width/2, 50);
        params2.addRule(RelativeLayout.RIGHT_OF, mTextInfo2.getId());
        params2.setMargins(width*3/4+offset2, 300, 0, 0);
        mTextInfo2.setLayoutParams(params2);
        relativeLayout.addView(mTextInfo2);

        mTextInfo3 = new TextView(this);

        mTextInfo3.setTextColor(Color.WHITE);
        mTextInfo3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(width/2, 50);
        params3.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params3.setMargins(width/4+offset3, 350, 0, 0);
        mTextInfo3.setLayoutParams(params3);
        relativeLayout.addView(mTextInfo3);

        mTextInfo4 = new TextView(this);
        mTextInfo4.setTextColor(Color.WHITE);
        mTextInfo4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        RelativeLayout.LayoutParams params4 = new RelativeLayout.LayoutParams(width/2, 50);
        params4.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params4.setMargins(width*3/4+offset4, 350, 0, 0);
        mTextInfo4.setLayoutParams(params4);
        relativeLayout.addView(mTextInfo4);

        setContentView(relativeLayout);

        setInfo("Preparing the recognizer");

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return;
        }

        learn =new LearnMachine();
        totalLengthCharts=learn.getSizeCharts();
        totalLengthStringArray=learn.getSizeChartsPos(chart);
        myGLRenderer.setChart(chart);

        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "chart= "+chart);        }

        //        startListeningWithoutDialog();
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getBaseContext());
        mSpeechRecognizer.setRecognitionListener(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getBaseContext().getPackageName());
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);


        mTTS =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_MAIN, "TextToSpeech status= "+status);
                }
                if(status != TextToSpeech.ERROR) {
                    mTTS.setLanguage(Locale.UK);
                    isTTS=true;
                    String caption = getResources().getString(captions.get(KWS_SEARCH));
                    say(caption,false);

                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
//
                }
            }
        });
    }


    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, "onResume");
        }
        /*
         * The activity must call the GL surface view's
         * onResume() on activity onResume().
         */
        if (mSurfaceView != null) {
            mSurfaceView.onResume();
        }
    }

    /**
     *
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, "onPause");
        }
        /*
         * The activity must call the GL surface view's
         * onPause() on activity onPause().
         */
        if (mSurfaceView != null) {
            mSurfaceView.onPause();
        }
    }

    /**
     *
     */
    @Override
    public void onDestroy() {
        if(mTTS !=null){
            mTTS.stop();
            mTTS.shutdown();
            mTTS=null;
            isTTS=false;


        }
        mSpeechRecognizer.stopListening();

        super.onDestroy();
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, "onDestroy");
        }

    }


    /**
     *
     * @param toSpeak
     * @param queue
     */
    private void say(String toSpeak, boolean queue){
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, " say toSpeak= "+toSpeak);        }
        if(mTTS!=null){
            while (mTTS.isSpeaking()){
                new SleepThread(100).start();
            }
            String utteranceId=this.hashCode() + "";
            if(queue)
                mTTS.speak(toSpeak, TextToSpeech.QUEUE_ADD, null, utteranceId);
            else
                mTTS.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        }
        while (mTTS.isSpeaking()){
            new SleepThread(100).start();
        }
    }

    /**
     *
     * @param txt
     */
    private void setText(String txt){
        mTextInfo3.setText(txt);
        mTextInfo4.setText(txt);
    }

    /**
     *
     * @param txt
     */
    private void setInfo(String txt){
        mTextInfo1.setText(txt);
        mTextInfo2.setText(txt);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, " onReadyForSpeech");        }

    }

    @Override
    public void onBeginningOfSpeech() {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, " onBeginningOfSpeech");        }

    }

    @Override
    public void onRmsChanged(float rmsdB) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, " onRmsChanged");        }

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, " onBufferReceived");        }

    }

    @Override
    public void onEndOfSpeech() {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, " onEndOfSpeech");        }

    }

    @Override
    public void onError(int errorCode) {

        switch (errorCode) {

            case SpeechRecognizer.ERROR_AUDIO:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_MAIN, " ERROR_AUDIO");        }
                say("ERROR AUDIO",false);
                break;

            case SpeechRecognizer.ERROR_CLIENT:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_MAIN, " ERROR_CLIENT");        }
                say("ERROR CLIENT",false);
                break;

            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_MAIN, " ERROR_INSUFFICIENT_PERMISSIONS");        }
                say("ERROR PERMISSIONS",false);
                break;

            case SpeechRecognizer.ERROR_NETWORK:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_MAIN, " ERROR_NETWORK");        }
                say("ERROR NETWORK",false);
                break;

            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_MAIN, " ERROR_NETWORK_TIMEOUT");        }
                say("ERROR NETWORK TIMEOUT",false);
                break;

            case SpeechRecognizer.ERROR_NO_MATCH:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_MAIN, " ERROR_NO_MATCH");        }
                say("NO MATCH try again",false);
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;

            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_MAIN, " ERROR_RECOGNIZER_BUSY");        }
                say("try again",false);
                break;

            case SpeechRecognizer.ERROR_SERVER:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_MAIN, " ERROR_SERVER");        }
                say("ERROR SERVER",false);
                break;

            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_MAIN, " ERROR_SPEECH_TIMEOUT");        }
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;

            default:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_MAIN, " unknown");        }
                say("ERROR unknown",false);
                break;
        }

    }

    @Override
    public void onResults(Bundle results) {
        String result="";
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, " onResults "+matches.toString());        }

        if(matches.contains(KEYPHRASE_KWS)){
            chart=0;
            chartPos=0;
            learn.clearResult();
            totalLengthStringArray=learn.getSizeChartsPos(chart);
            if(isTTS){
                String caption = getResources().getString(captions.get(CHARTS_SEARCH));
                say(caption, false);
            }
            setInfo("ready to test");
            setText("");
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    learning=true;
                    isStarting=true;
                }
            }, 1000);
        }else if(matches.contains(NEXT_SEARCH)){
            if(!isStarting)
                return;
            learning=false;
            chartPos = 0;
            chart++;
            if(chart>=totalLengthCharts){
                learning=false;
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "onResult next chart= "+chart);
                    setInfo("end of test");
                    setText(result);
                    say("end of test",false);
                    setText("result is "+learn.getResult() + " %");
                    say("result is "+learn.getResult() + " %",false);

                    chart=0;
                    chartPos=0;
                    totalLengthStringArray=learn.getSizeChartsPos(chart);
                    myGLRenderer.setChart(chart);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        learning=true;
                    }
                }, 1000);
            }else{
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "onResult next chart= "+chart);
                    setInfo("test started");
                    setText(result);
                    say("next chart",false);
                }
                totalLengthStringArray=learn.getSizeChartsPos(chart);
                myGLRenderer.setChart(chart);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        learning=true;
                    }
                }, 1000);
            }
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        }else if(learning && (matches.contains(UP_SEARCH) || matches.contains(DOWN_SEARCH)
        || matches.contains(LEFT_SEARCH) || matches.contains(RIGHT_SEARCH))){

            if(matches.contains(UP_SEARCH))
                result="up";
            else if(matches.contains(DOWN_SEARCH))
                result="down";
            else if(matches.contains(LEFT_SEARCH))
                result="left";
            else if(matches.contains(RIGHT_SEARCH))
                result="right";

            setText(result);

            learning=false;
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_LEARN, "onResult learning  totalLengthCharts= " + totalLengthCharts +
                        " chart=" + chart + " totalLengthStringArray=" + totalLengthStringArray + " pos= " + chartPos);
            }
            if(chart<=totalLengthCharts-1){
                if(chartPos<=totalLengthStringArray-1){
                    if (Util.DEBUG) {
                        Log.i(Util.LOG_TAG_LEARN, "onResult setResult= "+result);
                    }
                    learn.setResult(chart, chartPos, result);
                    chartPos++;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            learning=true;
                        }
                    }, 1000);
                }
                if(chartPos>totalLengthStringArray-1){
                    say("next chart",false);
                    chart++;
                    chartPos=0;
                    if(chart>=totalLengthCharts){
                        learning=false;
                        if (Util.DEBUG) {
                            Log.i(Util.LOG_TAG_LEARN, "onResult next chart= "+chart);
                            setInfo("end of test");
                            say("\"end of test",false);
                            setText("result is "+learn.getResult() + " %");
                            say("result is "+learn.getResult() + " %",false);

                            chart=0;
                            chartPos=0;
                            totalLengthStringArray=learn.getSizeChartsPos(chart);
                            learn.clearResult();
                            myGLRenderer.setChart(chart);
                        }
                    }else{
                        if (Util.DEBUG) {
                            Log.i(Util.LOG_TAG_LEARN, "onResult next chart= "+chart);
                            setInfo("test started");
                            say("next chart",false);
                            setText("");
                        }
                        totalLengthStringArray=learn.getSizeChartsPos(chart);
                        myGLRenderer.setChart(chart);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                learning=true;
                            }
                        }, 1000);
                    }
                }
            }
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        }else{
            setText(result);
            say("try again",false);
            if(learning){
                learning=false;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        learning=true;
                    }
                }, 1000);
            }
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        }


    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, " onPartialResults");        }

    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, " onEvent");        }

    }


    /**
     *
     */
    private class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<MainActivity> activityReference;

        SetupTask(MainActivity activity) {
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_RECOGNITION, "SetupTask");
            }
            this.activityReference = new WeakReference<>(activity);
        }

        /**
         *
         * @param params
         * @return
         */
        @Override
        protected Exception doInBackground(Void... params) {
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_RECOGNITION, "doInBackground");
            }


            return null;
        }

        /**
         *
         * @param result
         */
        @Override
        protected void onPostExecute(Exception result) {
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_RECOGNITION, "onPostExecute result= "+result );
            }
            if (result != null) {
                activityReference.get().setText("Failed to init recognizer " + result);
            } else {

            }
        }
    }





    /**
     *
     * @return
     */
    private boolean hasGLES20() {
        ActivityManager am = (ActivityManager)
                getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return info.reqGlEsVersion >= 0x20000;
    }



}

