package com.givevision.rochesightchart;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.ToneGenerator;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;


//https://cmusphinx.github.io/wiki/tutorialandroid/

public class MainActivity extends Activity implements  RecognitionListener  {
    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String CHARTS_SEARCH = "charts";
    private static final String KEYPHRASE_KWS = "check";
    private static final String NEXT_SEARCH = "next";
    private static final String STOP_SEARCH = "stop";
    private static final String UP_SEARCH = "up";
    private static final String DOWN_SEARCH = "down";
    private static final String LEFT_SEARCH = "left";
    private static final String RIGHT_SEARCH = "right";
    private static final String ACCEPT_CALIBRATION_SEARCH = "accept";
    private static final String ACTION_CALIBRATION = "calibration";
    private static final String ACTION_CONTROLLER_CALIBRATION_INFO = "controller calibration info";
    private static final String ACTION_VOICE_CALIBRATION_INFO = "voice calibration info";
    private static final String ACTION_CALIBRATION_CHECK = "calibration check";
    private static final String ACTION_TEST = "test";
    private static final String ACTION_CONTROLLER_TEST_INFO = "controller test info";
    private static final String ACTION_VOICE_TEST_INFO = "voice test info";
    private static final String ACTION_CONTROLLER = "controller";
    private static final String ACTION_VOICE = "voice";

    private boolean step1=false;
    private boolean step2=false;
    private RelativeLayout   relativeLayout  ;
    private boolean isReadyForSpeech;
    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;

    private GLSurfaceView mGLView;
    private TextView mTextInfo1;
    private TextView mTextInfo2;
    private TextView mTextInfo3;
    private TextView mTextInfo4;
    private TextView mTextInfo5;
    private TextView mTextInfo6;

    private int offset1=0;
    private int offset2=-100;

    private int offset3=0;
    private int offset4=-100;

    private TextToSpeech mTTS = null;
    private final int ACT_CHECK_TTS_DATA = 1000;
    private boolean isTTS=false;

    private GLCircleRenderer myGLRenderer;
    private LearnMachine learn;
    private int totalLengthCharts=0;
    private int totalLengthStringArray=0;
    private  static int FIRST_CHART_LEFT_EYE=0;
    private  static int FIRST_CHART_RIGHT_EYE=0;
    private static final String PREF_LEFT_START = "left eye chart start";
    private static final String PREF_RIGHT_START = "right eye chart start";
    private int chart=0;
    private int chartPos=-1;
    private boolean learning=false;
    private boolean isDone=false;
    private int eye=-1; //0-left 1-right -1-double
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private boolean isProcessing;
    private boolean isVoiceProcessing;
    private boolean isBluetooth;

    // Create the Handler object (on the main thread by default)
    private Handler handler = new Handler();
    // Define the code block to be executed
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            if(isDone) {
                startSpeechRecognizer();
            }else{
                handler.postDelayed(this, 1000);
            }
        }
    };


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


        //full screen definition
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//                        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
//	    		 | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
//	    		 | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//	    		 | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//        );

        relativeLayout = new RelativeLayout(this);
        relativeLayout.setId(AndroidUtils.generateViewId());
        relativeLayout.setKeepScreenOn(true);
        relativeLayout.setBackgroundColor(Color.BLACK);; //or whatever your image is
        setContentView(relativeLayout);

        //initialisation openGLES2
        if (hasGLES20()) {
            mGLView = new GLSurfaceView(this);
            mGLView.setEGLContextClientVersion(2);
            mGLView.setPreserveEGLContextOnPause(true);
            myGLRenderer=new GLCircleRenderer(this);
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

        mTextInfo1= new TextView(this);
        mTextInfo1.setId(AndroidUtils.generateViewId());
        mTextInfo1.setText("nothing");
        mTextInfo1.setTextColor(Color.DKGRAY);
        mTextInfo1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(width/2, 50);
        params1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params1.setMargins(width/4+offset1, 100, 0, 0);
        mTextInfo1.setLayoutParams(params1);
        relativeLayout.addView(mTextInfo1);

        mTextInfo2= new TextView(this);
        mTextInfo2.setId(AndroidUtils.generateViewId());
        mTextInfo2.setText("nothing");
        mTextInfo2.setTextColor(Color.DKGRAY);
        mTextInfo2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(width/2, 50);
        params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params2.setMargins(width*3/4+offset2, 100, 0, 0);
        mTextInfo2.setLayoutParams(params2);
        relativeLayout.addView(mTextInfo2);

        mTextInfo3 = new TextView(this);
        mTextInfo3.setId(AndroidUtils.generateViewId());
        mTextInfo3.setTextColor(Color.DKGRAY);
        mTextInfo3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(width/2, 50);
        params3.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params3.setMargins(width/4+offset3, 200, 0, 0);
        mTextInfo3.setLayoutParams(params3);
        relativeLayout.addView(mTextInfo3);

        mTextInfo4 = new TextView(this);
        mTextInfo4.setId(AndroidUtils.generateViewId());
        mTextInfo4.setTextColor(Color.DKGRAY);
        mTextInfo4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        RelativeLayout.LayoutParams params4 = new RelativeLayout.LayoutParams(width/2, 50);
        params4.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params4.setMargins(width*3/4+offset4, 200, 0, 0);
        mTextInfo4.setLayoutParams(params4);
        relativeLayout.addView(mTextInfo4);

        mTextInfo5 = new TextView(this);
        mTextInfo5.setId(AndroidUtils.generateViewId());
        mTextInfo5.setTextColor(Color.DKGRAY);
        mTextInfo5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        RelativeLayout.LayoutParams params5 = new RelativeLayout.LayoutParams(width/2, 50);
        params5.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params5.setMargins(width/4+offset3, 250, 0, 0);
        mTextInfo5.setLayoutParams(params5);
        relativeLayout.addView(mTextInfo5);

        mTextInfo6 = new TextView(this);
        mTextInfo6.setId(AndroidUtils.generateViewId());
        mTextInfo6.setTextColor(Color.DKGRAY);
        mTextInfo6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        RelativeLayout.LayoutParams params6 = new RelativeLayout.LayoutParams(width/2, 50);
        params6.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params6.setMargins(width*3/4+offset4, 250, 0, 0);
        mTextInfo6.setLayoutParams(params6);
        relativeLayout.addView(mTextInfo6);

        mGLView.setId(AndroidUtils.generateViewId());
//        setContentView(mGLView);
        relativeLayout.addView(mGLView);

        setInfo("Preparing the recognizer");
        // Prepare the data for UI
        captions = new HashMap<>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(CHARTS_SEARCH, R.string.charts);
        captions.put(ACTION_CALIBRATION, R.string.action_calibration);
        captions.put(ACTION_CONTROLLER_CALIBRATION_INFO, R.string.action_controller_calibration_info);
        captions.put(ACTION_VOICE_CALIBRATION_INFO, R.string.action_voice_calibration_info);
        captions.put(ACTION_CALIBRATION_CHECK, R.string.action_calibration_check);
        captions.put(ACTION_CONTROLLER, R.string.action_controller);
        captions.put(ACTION_VOICE, R.string.action_voice);
        captions.put(ACTION_TEST, R.string.action_test);
        captions.put(ACTION_CONTROLLER_TEST_INFO, R.string.action_controller_test_info);
        captions.put(ACTION_VOICE_TEST_INFO, R.string.action_voice_test_info);
        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return;
        }

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getBaseContext());
        mSpeechRecognizer.setRecognitionListener(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getBaseContext().getPackageName());
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
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
        if (mGLView != null) {
            mGLView.onResume();
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_MAIN, "onResume mGLView.onResume done");
            }
        }

        learn =new LearnMachine(this);
        totalLengthCharts=learn.getSizeCharts();
        chart=-1;
        chartPos=-1;
        eye=-1;
        myGLRenderer.setChart(chart, eye, "", learn.getOptotypeOuterDiameter(0));
//        if(chartPos==-1){
//            myGLRenderer.setChart(-1, eye, "", learn.getOptotypeOuterDiameter(0));
//        }else{
//            totalLengthStringArray=learn.getSizeChartsPos(chart);
//            myGLRenderer.setChart(-1, eye, learn.getChartPosString(chart, chartPos),
//                    learn.getOptotypeOuterDiameter(chart));
//        }

        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "chart= "+chart);
        }
        if(!isDone) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (Util.DEBUG) {
                                Log.i(Util.LOG_TAG_MAIN, "TextToSpeech status= " + status);
                            }
                            if (status != TextToSpeech.ERROR) {
                                mTTS.setLanguage(Locale.UK);
                                isTTS = true;

                                say(getResources().getString(captions.get(CHARTS_SEARCH)), false);
                                say(getResources().getString(captions.get(KWS_SEARCH)), true);
                                //        startListeningWithoutDialog();
                                isDone=true;
                                startSpeechRecognizer();
                            }
                        }
                    });
                }}, 500);
            // Create the Handler object (on the main thread by default)
//            Handler handler = new Handler();
//
//            // Start the initial runnable task by posting through the handler
//            handler.post(runnableCode);
//            new Handler().postDelayed(new Runnable() {
//                public void run() {
//                    startSpeechRecognizer();
//                }}, 5000);

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
        if (mGLView != null) {
            mGLView.onPause();
        }
    }

    /**
     *
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, "onDestroy");
        }
        if(mTTS !=null){
            mTTS.stop();
            mTTS.shutdown();
            mTTS=null;
            isTTS=false;
            isDone=false;
        }
        stopSpeechRecognizer();
    }
    /**
     *
     * @param event
     * @return executed
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(isProcessing){
            return true;
        }
        int keyCode=event.getKeyCode();
        int keyEvent=event.getAction();
        if(keyEvent == KeyEvent.ACTION_UP){
            isProcessing=true;
        }

        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, "keyCode: select: "+event.getKeyCode()+" action: "+event.getAction());
            Log.i(Util.LOG_TAG_MAIN, "keyCode: isProcessing: "+isProcessing+" isBluetooth: "+isBluetooth);
        }
        if(keyCode==Util.KEY_POWER  && keyEvent == KeyEvent.ACTION_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_POWER");
            }
            if(!isBluetooth){
                isBluetooth=true;
                stopSpeechRecognizer();
                learn.clearResult();
                setText("", "");
                myGLRenderer.setChart(-1, -1, "", learn.getOptotypeOuterDiameter(0));
                say(getResources().getString(captions.get(ACTION_CONTROLLER)), false);
                say(getResources().getString(captions.get(ACTION_CALIBRATION)), true);
                say(getResources().getString(captions.get(ACTION_CONTROLLER_CALIBRATION_INFO)), true);
                say("left eye",true);
                chart=0;
                eye=0;
                myGLRenderer.setChart(chart,eye,"all", learn.getOptotypeOuterDiameter(chart) );
                step1=true;
                step2=false;
            }else{
                //voice command
                isBluetooth=false;
                isVoiceProcessing=false;
                setText("", "");
                myGLRenderer.setChart(-1, -1, "", learn.getOptotypeOuterDiameter(0));
                startSpeechRecognizer();
                say(getResources().getString(captions.get(ACTION_VOICE)), false);
                say(getResources().getString(captions.get(KWS_SEARCH)), true);
                step1=false;
                step2=false;
                chart=-1;
                eye=-1;
                learn.clearResult();
            }
        }
        if(!isBluetooth){
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    isProcessing = false;
                }}, 2000);
            return true;
        }else{
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    isVoiceProcessing = false;
                }}, 2000);
        }
        if(step1){
            calibration(keyCode, keyEvent);
        }else if(step2){
            test(keyCode, keyEvent);
        }
        return true;
    }

    private void calibration(int keyCode, int keyEvent){
        if(keyCode==Util.KEY_TRIGGER && keyEvent == KeyEvent.ACTION_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_TRIGGER");
            }
            if(eye==0){
                if(chart>0 && chart<totalLengthCharts-1){
                    learn.upDatePref(PREF_LEFT_START,chart-1);
                }else if(chart>=totalLengthCharts-1){
                    learn.upDatePref(PREF_LEFT_START,chart-2);
                }else{
                    learn.upDatePref(PREF_LEFT_START,chart);
                }
                eye=1;
                chart=0;
                say("right eye",false);
                myGLRenderer.setChart(chart,eye,"all", learn.getOptotypeOuterDiameter(chart) );
            }else if(eye==1){
                if(chart>0 && chart<totalLengthCharts-1){
                    learn.upDatePref(PREF_RIGHT_START,chart-1);
                }else if(chart>=totalLengthCharts-1){
                    learn.upDatePref(PREF_RIGHT_START,chart-2);
                }else{
                    learn.upDatePref(PREF_RIGHT_START,chart);
                }
                eye=-1;
                step1=false;
                myGLRenderer.setChart(-1, eye, "", learn.getOptotypeOuterDiameter(0));
                say(getResources().getString(captions.get(ACTION_TEST)), true);
                if(isBluetooth){
                    say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO)), true);
                }else{
                    say(getResources().getString(captions.get(ACTION_VOICE_TEST_INFO)), true);
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        step2=true;
                        nextChart();
                    }}, 2000);
            }
        }else if(keyCode==Util.KEY_UP  && keyEvent == KeyEvent.ACTION_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_UP");
            }
            chart=chart-2;
            if(chart <0){
                chart=0;
            }
            myGLRenderer.setChart(chart,eye,"all", learn.getOptotypeOuterDiameter(chart) );
            say(getResources().getString(captions.get(ACTION_CALIBRATION_CHECK)), false);
        }else if(keyCode==Util.KEY_DOWN  && keyEvent == KeyEvent.ACTION_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_DOWN");
            }
            chart=chart+2;
            if(chart > totalLengthCharts-1){
                chart=totalLengthCharts-1;
            }
            myGLRenderer.setChart(chart,eye,"all", learn.getOptotypeOuterDiameter(chart) );
            say(getResources().getString(captions.get(ACTION_CALIBRATION_CHECK)), false);
        }else if(keyCode==Util.KEY_LEFT  && keyEvent == KeyEvent.ACTION_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_LEFT");
            }

        }else if(keyCode==Util.KEY_RIGHT  && keyEvent == KeyEvent.ACTION_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_RIGHT");
            }

        }else if(keyCode==Util.KEY_BACK  && keyEvent == KeyEvent.ACTION_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_BACK");
            }

        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                isProcessing=false;
                isVoiceProcessing=false;
                if(mSpeechRecognizer!=null && !isBluetooth) {
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                }
            }}, 2000);

    }

    private void test(int keyCode, int keyEvent){
        if(keyCode==Util.KEY_TRIGGER && keyEvent == KeyEvent.ACTION_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_TRIGGER");
            }
            //start checking or next chart
            nextChart();
        }else if(keyCode==Util.KEY_UP  && keyEvent == KeyEvent.ACTION_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_UP");
            }
            resultChart("up");
        }else if(keyCode==Util.KEY_DOWN  && keyEvent == KeyEvent.ACTION_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_DOWN");
            }
            resultChart("down");
        }else if(keyCode==Util.KEY_LEFT  && keyEvent == KeyEvent.ACTION_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_LEFT");
            }
            resultChart("left");
        }else if(keyCode==Util.KEY_RIGHT  && keyEvent == KeyEvent.ACTION_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_RIGHT");
            }
            resultChart("right");
        }else if(keyCode==Util.KEY_BACK  && keyEvent == KeyEvent.ACTION_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_BACK");
            }
            endOfTest();
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                isProcessing=false;
                isVoiceProcessing=false;
                if(mSpeechRecognizer!=null && !isBluetooth) {
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                }
            }}, 2000);
    }

    private void nextChart() {
        if(eye==-1){
            eye=0;
            chart=learn.getSharedPreferences().getInt(PREF_LEFT_START,FIRST_CHART_LEFT_EYE);
            say("left eye",false);
            learn.clearResult();
        }else{
            if(learn.isResultOk(chart,eye)){
                chart++;
            }else{
                if(eye==0){
                    chart= totalLengthCharts;
                }else{
                    endOfTest();
                    return;
                }
            }
        }
        chartPos = 0;
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "totalLengthCharts= " + totalLengthCharts +
                    " chart=" + chart + " totalLengthStringArray=" + totalLengthStringArray + " pos= " + chartPos);
        }
        setInfo("test started");
        if(chart>=totalLengthCharts){
            if(eye<1){
                chart=learn.getSharedPreferences().getInt(PREF_RIGHT_START,FIRST_CHART_RIGHT_EYE);
                chartPos=0;
                eye=1;
                totalLengthStringArray=learn.getSizeChartsPos(chart);
                myGLRenderer.setChart(chart,eye,learn.getChartPosString(chart,chartPos),
                        learn.getOptotypeOuterDiameter(chart) );

                say("right eye ",false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        if(mSpeechRecognizer!=null && !isBluetooth && !isReadyForSpeech) {
//                            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
//                        }
//                        setText("","");
                    }
                }, 2000);
            }else {
                endOfTest();
            }
        }else{
            if((chart==learn.getSharedPreferences().getInt(PREF_LEFT_START,FIRST_CHART_LEFT_EYE) && eye==0)||
                    (chart==learn.getSharedPreferences().getInt(PREF_LEFT_START,FIRST_CHART_RIGHT_EYE) && eye==1)){
//                say("",false);
            }else{
                say("next chart",false);
            }

            totalLengthStringArray=learn.getSizeChartsPos(chart);
            myGLRenderer.setChart(chart, eye, learn.getChartPosString(chart, chartPos),
                    learn.getOptotypeOuterDiameter(chart));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    if(mSpeechRecognizer!=null && !isBluetooth && !isReadyForSpeech) {
//                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
//                    }
//                    setText("","");
                }
            }, 2000);
        }
    }

    private void resultChart(String result) {
        if(chart==-1){
            return;
        }
        setText(result,"");
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_LEARN, "totalLengthCharts= " + totalLengthCharts +
                    " chart=" + chart + " totalLengthStringArray=" + totalLengthStringArray + " pos= " + chartPos);
        }
        if(chart<=totalLengthCharts-1){
            if(chartPos<=totalLengthStringArray-1){
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "setResult= "+result);
                }
                learn.setResult(chart, chartPos, result,eye);
                chartPos++;
                if(chartPos==totalLengthStringArray){
                    nextChart();
                }else{
                    say("next",false);
                }
                if(chartPos==-1){
                    myGLRenderer.setChart(-1, eye, "", learn.getOptotypeOuterDiameter(0));
                }else{
                    myGLRenderer.setChart(chart, eye, learn.getChartPosString(chart, chartPos), learn.getOptotypeOuterDiameter(chart));
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        if(mSpeechRecognizer!=null && !isBluetooth && !isReadyForSpeech) {
//                            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
//                        }
                        setText("","");
                    }
                }, 1000);
            }else{
                nextChart();
            }
        }
    }

    private void endOfTest() {
        chart=-1;
        chartPos=-1;
        eye=-1;
        myGLRenderer.setChart(chart, eye, "", learn.getOptotypeOuterDiameter(0));
        say("end of test",false);
        say("for the left eye result is "+learn.getResult(0) ,false);

        say("for the right eye result is "+learn.getResult(1),false);
        setInfo("end of test");
        setText("left eye "+learn.getResult(0),"right eye "+learn.getResult(1));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mSpeechRecognizer!=null && !isBluetooth && !isReadyForSpeech) {
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                }
            }
        }, 4000);
    }

    /**
     *
     */
    private void startSpeechRecognizer(){
//        stopSpeechRecognizer();
        if(mSpeechRecognizer ==null) {
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getBaseContext());
            mSpeechRecognizer.setRecognitionListener(this);
            mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getBaseContext().getPackageName());
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        }
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        chart=-1;
        eye=-1;
    }

    /**
     *
     */
    private void stopSpeechRecognizer(){
        if(mSpeechRecognizer !=null) {
            mSpeechRecognizer.stopListening();
            mSpeechRecognizer = null;
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
            while (mTTS.isSpeaking()){
                new SleepThread(100).start();
            }
        }
    }

    /**
     *
     * @param txt
     */
    private void setText(String txt, String txt1){
        mTextInfo3.setText(txt);
        mTextInfo3.bringToFront();
        mTextInfo4.setText(txt);
        mTextInfo4.bringToFront();
        mTextInfo5.setText(txt1);
        mTextInfo5.bringToFront();
        mTextInfo6.setText(txt1);
        mTextInfo6.bringToFront();
    }

    /**
     *
     * @param txt
     */
    private void setInfo(String txt){
        mTextInfo1.setText(txt);
        mTextInfo1.bringToFront();
        mTextInfo2.setText(txt);
        mTextInfo2.bringToFront();
    }

    public static boolean stringContainsItemFromList(String inputStr, ArrayList<String> items) {
        for(int i=0; i<items.size();i++){
            if(items.get(i).contains(inputStr)){
                return true;
            }
        }
        return false;
    }


    @Override
    public void onReadyForSpeech(Bundle params) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_VOICE, " onReadyForSpeech");        }
        isReadyForSpeech=true;
    }

    @Override
    public void onBeginningOfSpeech() {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_VOICE, " onBeginningOfSpeech");        }

    }

    @Override
    public void onRmsChanged(float rmsdB) {
//        if (Util.DEBUG) {
//            Log.i(Util.LOG_TAG_MAIN, " onRmsChanged");        }

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_VOICE, " onBufferReceived");        }

    }

    @Override
    public void onEndOfSpeech() {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_VOICE, " onEndOfSpeech");        }
        isReadyForSpeech=false;
    }

    @Override
    public void onError(int errorCode) {
        isReadyForSpeech=false;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_VOICE, " ERROR_AUDIO");        }
                say("ERROR AUDIO",false);
                break;

            case SpeechRecognizer.ERROR_CLIENT:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_VOICE, " ERROR_CLIENT");        }
//                say("ERROR CLIENT",false);
                break;

            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_VOICE, " ERROR_INSUFFICIENT_PERMISSIONS");        }
                say("ERROR PERMISSIONS",false);
                break;

            case SpeechRecognizer.ERROR_NETWORK:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_VOICE, " ERROR_NETWORK");        }
//                say("ERROR NETWORK",false);
                if(mSpeechRecognizer!=null && !isBluetooth) {
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                }
                break;

            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_VOICE, " ERROR_NETWORK_TIMEOUT");        }
//                say("ERROR NETWORK TIMEOUT",false);
                if(mSpeechRecognizer!=null && !isBluetooth) {
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                }
                break;

            case SpeechRecognizer.ERROR_NO_MATCH:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_VOICE, " ERROR_NO_MATCH");        }
//                say("NO MATCH try again",false);
                if(mSpeechRecognizer!=null && !isBluetooth) {
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                }
                break;

            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_VOICE, " ERROR_RECOGNIZER_BUSY");        }
//                say("I'm busy wait",false);
                break;

            case SpeechRecognizer.ERROR_SERVER:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_VOICE, " ERROR_SERVER");        }
//                say("ERROR SERVER",false);
                if(mSpeechRecognizer!=null && !isBluetooth) {
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                }
                break;

            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_VOICE, " ERROR_SPEECH_TIMEOUT");        }
                if(mSpeechRecognizer!=null && !isBluetooth) {
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                }
                break;

            default:
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_VOICE, " unknown");        }
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
            Log.i(Util.LOG_TAG_VOICE, " onResults "+matches.toString());
            Log.i(Util.LOG_TAG_VOICE, " onResults steap1= "+step1+ " step2= "+step2);
            Log.i(Util.LOG_TAG_VOICE, " onResults isVoiceProcessing= "+isVoiceProcessing+
                    " chart= "+chart+ " eye= "+eye);
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                isProcessing=false;
                isVoiceProcessing=false;
            }}, 2000);
        if(isVoiceProcessing){
            return;
        }
        isVoiceProcessing=true;
        if(stringContainsItemFromList(KEYPHRASE_KWS,matches)){
            chart=-1;
            eye=-1;
            learn.clearResult();
            setText("", "");
            myGLRenderer.setChart(-1, -1, "", learn.getOptotypeOuterDiameter(0));
            say(getResources().getString(captions.get(ACTION_CALIBRATION)), false);
            say(getResources().getString(captions.get(ACTION_VOICE_CALIBRATION_INFO)), true);
            say("left eye",true);
            chart=0;
            eye=0;
            myGLRenderer.setChart(chart,eye,"all", learn.getOptotypeOuterDiameter(chart) );
            step1=true;
            step2=false;
        }
        if(step1){
            if(stringContainsItemFromList(ACCEPT_CALIBRATION_SEARCH,matches)){
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "onResult "+ACCEPT_CALIBRATION_SEARCH);
                }
                calibration(Util.KEY_TRIGGER, KeyEvent.ACTION_UP);
            }else if(stringContainsItemFromList(UP_SEARCH,matches)){
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "onResult "+UP_SEARCH);
                }
                calibration(Util.KEY_UP, KeyEvent.ACTION_UP);
            }else if(stringContainsItemFromList(DOWN_SEARCH,matches)){
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "onResult "+DOWN_SEARCH);
                }
                calibration(Util.KEY_DOWN, KeyEvent.ACTION_UP);
            }else{
//                setText("","");
//                say("try again",false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if(mSpeechRecognizer!=null && !isBluetooth) {
                            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        }
                    }}, 1000);
            }

        }else if(step2){
            if(stringContainsItemFromList(UP_SEARCH,matches)){
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "onResult "+UP_SEARCH);
                }
                test(Util.KEY_UP, KeyEvent.ACTION_UP);
            }else if(stringContainsItemFromList(DOWN_SEARCH,matches)){
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "onResult "+DOWN_SEARCH);
                }
                test(Util.KEY_DOWN, KeyEvent.ACTION_UP);
            }else if(stringContainsItemFromList(LEFT_SEARCH,matches)){
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "onResult "+LEFT_SEARCH);
                }
                test(Util.KEY_LEFT, KeyEvent.ACTION_UP);
            }else if(stringContainsItemFromList(RIGHT_SEARCH,matches)){
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "onResult "+RIGHT_SEARCH);
                }
                test(Util.KEY_RIGHT, KeyEvent.ACTION_UP);
            }else if(stringContainsItemFromList(NEXT_SEARCH,matches)){
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "onResult "+NEXT_SEARCH);
                }
                test(Util.KEY_TRIGGER, KeyEvent.ACTION_UP);
            }else if(stringContainsItemFromList(STOP_SEARCH,matches)){
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_LEARN, "onResult "+STOP_SEARCH);
                }
                test(Util.KEY_BACK, KeyEvent.ACTION_UP);
            }else{
//                setText("","");
//                say("try again",false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if(mSpeechRecognizer!=null && !isBluetooth) {
                            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        }
                    }}, 1000);
            }

        }else{
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if(mSpeechRecognizer!=null && !isBluetooth) {
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                    }
                }}, 2000);
        }



    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> matches = partialResults
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_VOICE, " onPartialResults "+matches.toString());
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_VOICE, " onEvent");        }

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
         * @return exception
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
                activityReference.get().setText("Failed to init recognizer " + result,"");
            } else {

            }
        }
    }





    /**
     *
     * @return gleVersion
     */
    private boolean hasGLES20() {
        ActivityManager am = (ActivityManager)
                getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return info.reqGlEsVersion >= 0x20000;
    }
}

