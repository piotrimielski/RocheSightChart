package com.givevision.rochesightchart;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

//https://cmusphinx.github.io/wiki/tutorialandroid/

public class ReconizerActivity extends Activity{
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
    private static final String KEYPHRASE_KWS = "check";

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

    private RecognitionListener mRecognitionListener = new RecognitionListener() {
        /**
         *
         */
        // RecognitionListener implementation
        @Override
        public void onBeginningOfSpeech() {
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_RECOGNITION, "onBeginningOfSpeech SearchName= "+recognizer.getSearchName());
            }
        }
        /**
         * We stop recognizer here to get a final result
         */
        @Override
        public void onEndOfSpeech() {
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_RECOGNITION, "onEndOfSpeech SearchName= "+recognizer.getSearchName());
            }
            if (recognizer.getSearchName().equals(KWS_SEARCH)) {
                switchSearch(KWS_SEARCH);
            }
        }
        /**
         * In partial result we get quick updates about current hypothesis. In
         * keyword spotting mode we can react here, in other modes we need to wait
         * for final result in onResult.
         */
        @Override
        public void onPartialResult(Hypothesis hypothesis) {
            if (hypothesis == null)
                return;

//        if (Util.DEBUG) {
//            Log.i(Util.LOG_TAG_RECOGNITION, "onPartialResult getHypstr= "+hypothesis.getHypstr()
//                    + " getBestScore= " + hypothesis.getBestScore()
//                    + " getProb= " + hypothesis.getProb());
//        }
//        if(hypothesis.getBestScore()<-3000)
//            return;

            String text = hypothesis.getHypstr();
            if (text.contains(KEYPHRASE_KWS)) {
                learning=false;
//            if(isTTS){
//                String caption = getResources().getString(captions.get(CHARTS_SEARCH));
//                say(caption, false);
//            }
                learn.clearResult();
                switchSearch(CHARTS_SEARCH);
            }else if (text.equals(START_SEARCH)){

            }else if (text.equals(NEXT_SEARCH)){
                switchSearch(CHARTS_SEARCH);
            }else if (text.equals(STOP_SEARCH)){

            }else if (text.equals(UP_SEARCH)){
                switchSearch(CHARTS_SEARCH);
            }else if (text.equals(DOWN_SEARCH)){
                switchSearch(CHARTS_SEARCH);
            }else if (text.equals(LEFT_SEARCH)){
                switchSearch(CHARTS_SEARCH);
            }else if (text.equals(RIGHT_SEARCH)){
                switchSearch(CHARTS_SEARCH);
            }
        }
        /**
         * This callback is called when we stop the recognizer.
         */
        @Override
        public void onResult(Hypothesis hypothesis) {
            if (hypothesis != null) {
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_RECOGNITION, "onResult getHypstr= "+hypothesis.getHypstr()
                            + " getBestScore= " + hypothesis.getBestScore()
                            + " getProb= " + hypothesis.getProb());
                    Log.i(Util.LOG_TAG_RECOGNITION, "onResult getHypstr= ");
                }
                String text = hypothesis.getHypstr();

                setText(text);
                if (text.contains(KEYPHRASE_KWS)) {
                    if(isTTS){
                        String caption = getResources().getString(captions.get(CHARTS_SEARCH));
                        say(caption, false);
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            learning=true;
                        }
                    }, 1000);
                }else if(text.equals(START_SEARCH)){
//                learning=false;
//                if (Util.DEBUG) {
//                    Log.i(Util.LOG_TAG_LEARN, "onResult end test result= "+learn.getResult() );
//                    setInfo("start to check");
//                    setText("result is "+learn.getResult() + " %");
//                    say("checking restarted",false);
//                }
//                chart=0;
//                chartPos=0;
//                totalLengthStringArray=learn.getSizeChartsPos(chart);
//                learn.clearResult();
//                myGLRenderer.setChart(chart);
//                learning=true;
                }else if(text.equals(STOP_SEARCH)){
//                learning=false;
//                if (Util.DEBUG) {
//                    Log.i(Util.LOG_TAG_LEARN, "onResult end test result= "+learn.getResult() );
//                    setInfo("ready to restart");
//                    setText("result is "+learn.getResult() + " %");
//                    say("result is "+learn.getResult() + " %",false);
//                }
//                chart=0;
//                chartPos=0;
//                totalLengthStringArray=learn.getSizeChartsPos(chart);
//                learn.clearResult();
//                myGLRenderer.setChart(chart);

                }else if(text.equals(NEXT_SEARCH)){
                    learning=false;
                    chartPos = 0;
                    chart++;
                    if(chart>=totalLengthCharts){
                        learning=false;
                        if (Util.DEBUG) {
                            Log.i(Util.LOG_TAG_LEARN, "onResult next chart= "+chart);
                            setInfo("end of test");
                            setText("");
                            say("\"end of test",false);
                            setText("result is "+learn.getResult() + " %");
                            say("result is "+learn.getResult() + " %",false);

                            chart=0;
                            chartPos=0;
                            totalLengthStringArray=learn.getSizeChartsPos(chart);
                            learn.clearResult();
                            myGLRenderer.setChart(chart);
                        }
                        switchSearch(KWS_SEARCH);
                    }else{
                        if (Util.DEBUG) {
                            Log.i(Util.LOG_TAG_LEARN, "onResult next chart= "+chart);
                            setInfo("test started");
                            setText("");
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
                        switchSearch(CHARTS_SEARCH);
                    }

                }else if (learning && (text.equals(UP_SEARCH) || text.equals(DOWN_SEARCH)
                        || text.equals(UP_SEARCH) || text.equals(RIGHT_SEARCH) || text.equals(LEFT_SEARCH))){
                    learning=false;
                    if (Util.DEBUG) {
                        Log.i(Util.LOG_TAG_LEARN, "onResult learning  totalLengthCharts= " + totalLengthCharts +
                                " chart=" + chart + " totalLengthStringArray=" + totalLengthStringArray + " pos= " + chartPos);
                    }
                    if(chart<=totalLengthCharts-1){
                        if(chartPos<=totalLengthStringArray-1){
                            if (Util.DEBUG) {
                                Log.i(Util.LOG_TAG_LEARN, "onResult setResult= "+text);
                            }
                            learn.setResult(chart, chartPos, text);
                            chartPos++;
                            switchSearch(CHARTS_SEARCH);
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
                                    switchSearch(KWS_SEARCH);
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
                                switchSearch(CHARTS_SEARCH);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        learning=true;
                                    }
                                }, 1000);
                            }
                        }
                    }
                }else{
                    setText("");
                }
            }
        }

        /**
         *
         * @param error
         */
        @Override
        public void onError(Exception error) {
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_RECOGNITION, "onError error= "+error);
            }
            setText(error.getMessage());
            say(error.getMessage(),false);
        }

        /**
         *
         */
        @Override
        public void onTimeout() {
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_RECOGNITION, "onTimeout");
            }
            say("time out",false);
//        switchSearch(KWS_SEARCH);
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

                    while (mTTS.isSpeaking()){
                        new SleepThread(100).start();}
                    startRecognizer();
                }
            }
        });
    }

    /**
     *
     */
    private void startRecognizer(){
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, "startRecognizer");
        }
        new SetupTask(this).execute();
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




    /**
     *
     */
    private class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<ReconizerActivity> activityReference;

        SetupTask(ReconizerActivity activity) {
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

            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
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
                activityReference.get().switchSearch(KWS_SEARCH);
            }
        }
    }

    /**
     *
     * @param searchName
     */
    private void switchSearch(String searchName) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RECOGNITION, "switchSearch searchName= "+searchName);
        }
        recognizer.stop();
        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH)) {
            recognizer.startListening(searchName);
        }else {
            recognizer.startListening(searchName);
//            recognizer.startListening(searchName, 4000);
        }

//        String caption = getResources().getString(captions.get(searchName));
//        setInfo("");
//        setText("");
    }

    /**
     *
     * @param assetDir
     * @throws IOException
     */
    private void setupRecognizer(File assetDir) throws IOException{
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_RECOGNITION, "setupRecognizer assetDir= "+assetDir);
        }

        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetDir, "en-us-ptm"))
                .setDictionary(new File(assetDir, "cmudict-en-us.dict"))

                .setRawLogDir(assetDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(mRecognitionListener);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE_KWS);

        // Create grammar-based searches
        File chartsGrammar = new File(assetDir, "charts.gram");
        recognizer.addGrammarSearch(CHARTS_SEARCH, chartsGrammar);

        setInfo("Recognizer Ready");

//        // Create grammar-based search for digit recognition
//        File digitsGrammar = new File(assetDir, "digits.gram");
//        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);
//
//        // Phonetic search
//        File phoneticModel = new File(assetDir, "en-phone.dmp");
//        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
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
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
        super.onDestroy();
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_MAIN, "onDestroy");
        }

    }


}

