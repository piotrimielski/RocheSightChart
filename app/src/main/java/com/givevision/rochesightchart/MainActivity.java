package com.givevision.rochesightchart;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
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

import com.android.volley.RequestQueue;
import com.givevision.rochesightchart.db.Acuity;
import com.givevision.rochesightchart.db.AcuityRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import static android.os.SystemClock.sleep;
import static com.givevision.rochesightchart.Util.LOG_TAG_MAIN;
import static com.givevision.rochesightchart.Util.TAG;


//https://cmusphinx.github.io/wiki/tutorialandroid/

public class MainActivity extends Activity {
    private static  int GREY_E = 250;
    private static  int GREY_B = 255;
    private static Context context;
    public static final String CONF_PATH = Environment.getExternalStorageDirectory()+ "/GiveVision/contrast.txt";
    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String CHARTS_SEARCH = "charts";
    private static final String ACTION_CALIBRATION1 = "calibration";
    private static final String ACTION_CALIBRATION2 = "sizes";
    private static final String ACTION_CONTROLLER_CALIBRATION_INFO1 = "controller calibration info1";
    private static final String ACTION_CONTROLLER_CALIBRATION_INFO2 = "controller calibration info2";
    private static final String ACTION_TEST = "test";
    private static final String ACTION_TEST_REMINDER = "test reminder";
    private static final String ACTION_RESET_USER = "reset user";
    private static final String ACTION_RESULT_LEFT = "result left";
    private static final String ACTION_RESULT_RIGHT = "result right";
    private static final String ACTION_CONTROLLER_TEST_INFO = "controller test info";
    private static final String ACTION_VOICE_TEST_INFO = "voice test info";
    private static final String ACTION_CONTROLLER_CALIBRATION_INFO11 = "controller calibration info11";
    private static final String ACTION_CONTROLLER_CALIBRATION_INFO21 = "controller calibration info21";
    private static final String ACTION_CONTROLLER_TEST_INFO1 = "controller test info1";
    private static final String ACTION_CONTROLLER_TEST_INFO2 = "controller test info2";
    private static final String ACTION_CALIBRATION_CHECK = "calibration check";
    private static final String ACTION_CONTROLLER = "controller";
    private static final String ACTION_VOICE = "voice";

    private boolean step1=false;
    private boolean step2=false;
    private boolean test=false;
    private RelativeLayout   relativeLayout  ;
    private boolean isReadyForSpeech;
    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 3;
    private static final int LONG_DELAY = 10000;
    private static final int SHORT_DELAY = 5000;
    private static final int KEY_DELAY = 800;
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

    private GLERenderer myGLRenderer;
    private LearnMachine learn;
    private int totalLengthCharts=0;
    private int totalLengthStringArray=0;
    private  static int FIRST_CHART_LEFT_EYE=0;
    private  static int FIRST_CHART_RIGHT_EYE=0;
    private  static int GREY_ORG_E=125;
    private  static int GREY_ORG_SQUARE=126;

    private static final String BROADCAST_START_APP_ACTION="start app action";
    private static final String START_APP_RESULT = "app response" ;

    private int chart=0;
    private int chartPos=-1;
    private boolean learning=false;
    private int eye=-1; //0-left 1-right -1-double
    private boolean isContrast=false;
    private String contrastLeftResult;
    private String contrastRightResult;
    private String noContrastLeftResult;
    private String noContrastRightResult;
    private int greyE;
    private int greySquare;
    private int pixelNbr=1;
    private int greyE_left;
    private int greySquare_left;
    private int pixelNbr_left=1;
    private int greyE_right;
    private int greySquare_right;
    private int pixelNbr_right=1;
    //    private SpeechRecognizer mSpeechRecognizer;
//    private Intent mSpeechRecognizerIntent;
    private boolean isProcessing;
    private boolean isReady=true;
    private boolean isAppStarted=false;
    private boolean isTimerStart;
    private boolean isSecondPeriod;
    private int eyeCalibration=-1;
    private ToneGenerator toneH = new ToneGenerator(AudioManager.STREAM_DTMF, 100);
    private ToneGenerator toneL = new ToneGenerator(AudioManager.STREAM_DTMF, 80);
    private PowerManager pm;
    private PowerManager.WakeLock wl;
    private boolean newUser=true;
    private int err=0;
    private int good=0;
    private String startedByPackage="";
    //    // Create the Handler object (on the main thread by default)
    private Handler handler = new Handler();
    private Handler handler0 = new Handler();
    private Handler handler1 = new Handler();
    private Handler handler2 = new Handler();

    private AcuityRepository acuityRepository;
    private RequestQueue requestQueue;
    private String imei;
    private boolean fakeControls=false;
    private boolean isBackTouch=false;

    //   test reminder
    private Runnable runnableCode1 = new Runnable() {
        @Override
        public void run() {
            if(isTimerStart) {
                isTimerStart=false;
                say(getResources().getString(captions.get(ACTION_TEST_REMINDER)), false,false);
                handler2.removeCallbacks(runnableCode2);
                handler2.postDelayed(runnableCode2, SHORT_DELAY);
                isSecondPeriod=true;
            }
        }
    };
    //   test go next
    private Runnable runnableCode2 = new Runnable() {
        @Override
        public void run() {
            if(isSecondPeriod){
                stopTask(false);
                nextChart();
//                resultChart("error");
            }
        }
    };
    //   first massage reminder
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            if(!isAppStarted){
                say(getResources().getString(captions.get(CHARTS_SEARCH)), false,false);
                handler.removeCallbacks(runnableCode);
                handler.postDelayed(runnableCode, 2*LONG_DELAY);
            }else{
                handler.removeCallbacks(runnableCode);
            }
        }
    };
    //   internet connaction test
    private Runnable runnableCode0 = new Runnable() {
        @Override
        public void run() {
            handler0.removeCallbacks(runnableCode0);
            if(!isInternetAvailable(context)){
                boolean isWifi=connectWiFi(context,Util.SSID,Util.SSIDPW);
                if (Util.DEBUG) {
                    Log.i(LOG_TAG_MAIN, " isWifi= "+isWifi);
                }
                handler0.postDelayed(runnableCode0, LONG_DELAY);
            }else{
                if (Util.DEBUG) {
                    Log.i(LOG_TAG_MAIN, "runnableCode0 userId= "+
                            Util.getSharedPreferences(context).getInt(Util.PREF_USER_ID,-1));
                }
                // Add a request
                if(Util.getSharedPreferences(context).getInt(Util.PREF_USER_ID,-1)==-1){
                    Util.postData(context, acuityRepository, imei,null);
                }else{
                    AsyncTask.execute( new Runnable() {
                        @Override
                        public void run() {
                            Object obj = new Object();
                            List<Acuity> acuities=acuityRepository.getAllAcuities();
                            for(int i=0; i<acuities.size();i++){
                                Acuity acuity=acuities.get(i);
                                if(!acuity.getInServer()){
                                    Util.postData(context, acuityRepository, imei,acuity);
                                    if (Util.DEBUG) {
                                        Log.i(LOG_TAG_MAIN, "acuity found: "
                                                +" id=  "+ acuity.getId()
                                                +" userId=  "+ acuity.getUserId()
                                                +" contrast=  "+ acuity.getContrast()
                                                +" leftEye=  "+ acuity.getLeftEye()
                                                +" rightEye=  "+ acuity.getRightEye()
                                                +" createdAt=  "+ acuity.getCreatedAt()
                                                +" modifiedAt=  "+ acuity.getModifiedAt()
                                                +" inServer=  "+ acuity.getInServer());
                                    }
//                                    new SleepThread(100).start();
                                }
                            }
                        }
                    });
                    if(startedByPackage==null){
                        handler0.postDelayed(runnableCode0, LONG_DELAY);
                    }
                }
            }
            if (Util.DEBUG) {
                Log.i(LOG_TAG_MAIN, " isOnline= "+isInternetAvailable(context));
            }
        }
    };

    /**
     * stop the background tasks
     * @param
     * @return
     */
    void stopTask(boolean withRunnableCode0) {
        if(withRunnableCode0){
            handler.removeCallbacks(runnableCode);
            handler0.removeCallbacks(runnableCode0);
        }
        isTimerStart=false;
        handler1.removeCallbacks(runnableCode1);
        isSecondPeriod=false;
        handler2.removeCallbacks(runnableCode2);
    }

    /**
     * restart the background tasks
     * @param delay start runnable code 1 with defined delay
     * @return
     */
    void restardTask(int delay) {
        handler.removeCallbacks(runnableCode);
        handler0.removeCallbacks(runnableCode0);
        isTimerStart=false;
        isSecondPeriod=false;
        handler1.removeCallbacks(runnableCode1);
        handler1.postDelayed(runnableCode1, delay);
        isTimerStart=true;
    }

    /**
     * configuration changed manual management
     * @param newConfig configuration information
     * @return
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Util.DEBUG) {
            Log.i(LOG_TAG_MAIN, "onConfigurationChanged newConfig "+newConfig.toString());
        }
    }

    private void initControls() {
        if (fakeControls) {
            findViewById(R.id.controls).setVisibility(View.VISIBLE);
            findViewById(R.id.up).setOnClickListener(view -> mainDispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, Util.KEY_UP)));
            findViewById(R.id.down).setOnClickListener(view -> mainDispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, Util.KEY_DOWN)));
            findViewById(R.id.trigger).setOnClickListener(view -> mainDispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, Util.KEY_TRIGGER)));
            findViewById(R.id.left).setOnClickListener(view -> mainDispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, Util.KEY_LEFT)));
            findViewById(R.id.right).setOnClickListener(view -> mainDispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, Util.KEY_RIGHT)));
            findViewById(R.id.power).setOnClickListener(view -> mainDispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, Util.KEY_POWER)));
            findViewById(R.id.back).setOnClickListener(view -> mainDispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, Util.KEY_BACK)));
        } else {
            findViewById(R.id.controls).setVisibility(View.GONE);
        }

    }

    /**
     * create the activity with saved instance information
     * @param savedInstanceState
     * @return
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getBaseContext();
        if (Util.DEBUG) {
            Log.i(LOG_TAG_MAIN, "onCreate");
        }
        Intent intent = getIntent();
        startedByPackage=intent.getStringExtra("startedByPackage");
        if (Util.DEBUG) {
            Log.i(LOG_TAG_MAIN, "onCreate startedByPackage "+startedByPackage);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //full screen definition
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        int ui = getWindow().getDecorView().getSystemUiVisibility();
        ui = ui | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        final Window win = getWindow();
        win.getDecorView().setSystemUiVisibility(ui);
        win.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        win.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        win.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        |WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        |WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        |WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        |WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        );
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

        setContentView(R.layout.activity_main);
        relativeLayout = (RelativeLayout) findViewById(R.id.surfaceContainer);
        relativeLayout.setId(AndroidUtils.generateViewId());
        relativeLayout.setKeepScreenOn(true);
        relativeLayout.setBackgroundColor(Color.BLACK);

        //initialisation openGLES2
        if (hasGLES20()) {
            mGLView = new GLSurfaceView(this);
            mGLView.setEGLContextClientVersion(2);
            mGLView.setPreserveEGLContextOnPause(true);
            myGLRenderer=new GLERenderer(this);
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
            Log.i(LOG_TAG_MAIN, "onCreate width= "+width + " height= "+height);
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
        initControls();

        setInfo("Preparing the test");
        // Prepare the data for UI
        captions = new HashMap<>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(CHARTS_SEARCH, R.string.charts);
        captions.put(ACTION_CALIBRATION1, R.string.action_calibration);
        captions.put(ACTION_CALIBRATION2, R.string.action_controller_size);
        captions.put(ACTION_CONTROLLER_CALIBRATION_INFO1, R.string.action_controller_calibration_info1);
        captions.put(ACTION_CONTROLLER_CALIBRATION_INFO11, R.string.action_controller_calibration_info11);
        captions.put(ACTION_CONTROLLER_CALIBRATION_INFO2, R.string.action_controller_size_info2);
        captions.put(ACTION_CONTROLLER_CALIBRATION_INFO21, R.string.action_controller_size_info21);
        captions.put(ACTION_TEST, R.string.action_test);
        captions.put(ACTION_TEST_REMINDER, R.string.action_test_reminder);
        captions.put(ACTION_CONTROLLER_TEST_INFO, R.string.action_controller_test_info);
        captions.put(ACTION_CONTROLLER_TEST_INFO1, R.string.action_controller_test_info1);
        captions.put(ACTION_CONTROLLER_TEST_INFO2, R.string.action_controller_test_info2);
        captions.put(ACTION_VOICE_TEST_INFO, R.string.action_voice_test_info);
        captions.put(ACTION_RESET_USER, R.string.action_reset_user);
        captions.put(ACTION_RESULT_LEFT, R.string.result_left_info);
        captions.put(ACTION_RESULT_RIGHT, R.string.result_right_info);
//        captions.put(ACTION_CALIBRATION_CHECK, R.string.action_calibration_check);
//        captions.put(ACTION_CONTROLLER, R.string.action_controller);
//        captions.put(ACTION_VOICE, R.string.action_voice);
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
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSIONS_REQUEST_READ_PHONE_STATE);
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        imei=telephonyManager.getDeviceId();

        acuityRepository = new AcuityRepository(context);
        //Get a RequestQueue For Handle Network Request
        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
    }


    /**
     * start activity
     * @param
     * @return
     */
    @Override
    protected void onStart () {
        super.onStart();
        if (Util.DEBUG) {
            Log.i(LOG_TAG_MAIN, "onStart");
        }
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RocheSightChart:tracker");
        wl.acquire();
        if(!isTTS) {
            isTTS=true;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (Util.DEBUG) {
                                Log.i(LOG_TAG_MAIN, "TextToSpeech status= " + status);
                            }
                            if (status != TextToSpeech.ERROR) {
                                mTTS.setLanguage(Locale.UK);
                                say(getResources().getString(captions.get(CHARTS_SEARCH)), false,false);
                                handler.removeCallbacks(runnableCode);
                                handler.postDelayed(runnableCode, 2*LONG_DELAY);
                                isAppStarted=false;
                                if(newUser){
                                    AsyncTask.execute( new Runnable() {
                                        @Override
                                        public void run() {
                                            acuityRepository.newInstallation();
                                        }
                                    });
                                }else{
                                    AsyncTask.execute( new Runnable() {
                                        @Override
                                        public void run() {
                                            List<Acuity> acuities=acuityRepository.getAllAcuities();
                                            Log.i(LOG_TAG_MAIN, "AsyncTask acuities: "+acuities.size());
                                            for(int i=0; i<acuities.size();i++){
                                                Acuity acuity=acuities.get(i);
                                                if (Util.DEBUG) {
                                                    Log.i(LOG_TAG_MAIN, "AsyncTask acuity: "
                                                            +" id=  "+ acuity.getId()
                                                            +" contrast=  "+ acuity.getContrast()
                                                            +" userId=  "+ acuity.getUserId()
                                                            +" leftEye=  "+ acuity.getLeftEye()
                                                            +" rightEye=  "+ acuity.getRightEye()
                                                            +" createdAt=  "+ acuity.getCreatedAt()
                                                            +" modifiedAt=  "+ acuity.getModifiedAt()
                                                            +" inServer=  "+ acuity.getInServer());
                                                }
                                            }
//                                            exit(0);
                                        }
                                    });
                                }
                            }else{
                                isTTS=false;
                            }
                        }
                    });
                }}, 500);
        }
    }

    /**
     * resume activity
     * @param
     * @return
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (Util.DEBUG) {
            Log.i(LOG_TAG_MAIN, "onResume");
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_START_APP_ACTION);
        /*
         * The activity must call the GL surface view's
         * onResume() on activity onResume().
         */
        if (mGLView != null) {
            mGLView.onResume();
            if (Util.DEBUG) {
                Log.i(LOG_TAG_MAIN, "onResume mGLView.onResume done");
            }
        }
        myGLRenderer.setChart(-1, -2, "", 0);
        learn =new LearnMachine(this);
        totalLengthCharts=learn.getSizeCharts();
        chart=-1;
        chartPos=-1;
        eye=-1;
        if(Util.getSharedPreferences(context).getInt(Util.PREF_RIGHT_START,100)==100){
            newUser=true;
        }else{
            newUser=false;
        }


        handler0.removeCallbacks(runnableCode0);
        handler0.postDelayed(runnableCode0, KEY_DELAY);

        //read sequences of chart from 1 to 17
        ArrayList<String> lists= Util.readConfigFile(CONF_PATH);
        if(lists!=null){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_MAIN, "seq= "+lists.size());
            }
            String str=lists.get(0);
            Log.i(Util.LOG_TAG_MAIN, "str= "+str);
            String[] arrayOfString=str.split(",");
            GREY_E= Integer.parseInt(arrayOfString[0]);
            GREY_B= Integer.parseInt(arrayOfString[1]);
            ArrayList<Integer> list =new ArrayList<Integer>();
        }else{
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_MAIN, "contrast static");
            }
        }
    }

    /**
     * pause activity
     * @param
     * @return
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (Util.DEBUG) {
            Log.i(LOG_TAG_MAIN, "onPause");
        }
        /*
         * The activity must call the GL surface view's
         * onPause() on activity onPause().
         */
        if (mGLView != null) {
            mGLView.onPause();
        }
        say("",false,false);
    }

    /**
     * stop activity
     * @param
     * @return
     */
    @Override
    protected void onStop () {
        super.onStop();
        if (Util.DEBUG) {
            Log.i(LOG_TAG_MAIN, "onStop");
        }
        if (requestQueue != null) {
            requestQueue.cancelAll(Util.LOG_TAG_MAIN);
        }
//        if(startedByPackage==null){
//            WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
//            if (wifiManager.isWifiEnabled()) {
//                wifiManager.setWifiEnabled(false);
//            }
//        }else{
//
//        }

        handler.removeCallbacks(runnableCode);
        handler0.removeCallbacks(runnableCode0);
        handler1.removeCallbacks(runnableCode1);
        handler2.removeCallbacks(runnableCode2);

        if(mTTS !=null){
            mTTS.stop();
            mTTS.shutdown();
            mTTS=null;
            isTTS=false;
        }
        step1=false;
        step2=false;
        test=false;
        isContrast=false;
        wl.release();
    }

    /**
     * destroy activity
     * @param
     * @return
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Util.DEBUG) {
            Log.i(LOG_TAG_MAIN, "onDestroy");
        }

    }

    List<Integer> listOfTouchs = new ArrayList<Integer>();
    boolean isTouchProcessing;
    /**
     * manage key events
     * @param event
     * @return boolean event validation
     */
    @Override
    public boolean
    dispatchKeyEvent(KeyEvent event) {
        if (Util.DEBUG) {
            Log.i(LOG_TAG_MAIN, "dispatchKeyEvent keyCode: select: "+event.getKeyCode()+" action: "+event.getAction()
                    +" newUser: "+newUser+ " isProcessing= "+isProcessing);
        }
        if(isProcessing){
            return true;
        }
        int keyCode=event.getKeyCode();
        int keyEvent=event.getAction();
        int keyCodeRead;
        if(test && (keyCode==Util.KEY_RIGHT || keyCode==Util.KEY_LEFT || keyCode==Util.KEY_UP || keyCode==Util.KEY_DOWN)){
            if(!isTouchProcessing && listOfTouchs.size()>=Util.KEY_NBR){
                isTouchProcessing=true;
                int up=Collections.frequency(listOfTouchs,Util.KEY_UP);
                int down= Collections.frequency(listOfTouchs,Util.KEY_DOWN);
                int left=Collections.frequency(listOfTouchs,Util.KEY_LEFT);
                int right=Collections.frequency(listOfTouchs,Util.KEY_RIGHT);
                if(up>down && up>left && up>right){
                    keyCodeRead=up;
                }else if(down>up && down>left && down>right){
                    keyCodeRead=down;
                }else if(left>up && left>down && left>right){
                    keyCodeRead=left;
                }else if(right>up && right>down && right>left){
                    keyCodeRead=right;
                    isTouchProcessing=false;
                    return mainDispatchKeyEvent(new KeyEvent(keyCodeRead,KeyEvent.ACTION_DOWN));
                }else{
                    if(!isTouchProcessing){
                        listOfTouchs.add(keyCode);
                    }
                    return true;
                }
            }
        }

        return mainDispatchKeyEvent(event);
    }

    private boolean mainDispatchKeyEvent(KeyEvent event) {
        int keyCode=event.getKeyCode();
        int keyEvent=event.getAction();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                isProcessing=false;
            }}, 1000);
        if (Util.DEBUG) {
            Log.i(LOG_TAG_MAIN, "keyCode: select: "+event.getKeyCode()+" action: "+event.getAction()
                    +" newUser: "+newUser);
        }
        //TODO:: removed for test
////        if(keyCode==Util.KEY_TRIGGER && keyEvent == KeyEvent.ACTION_UP && (!step1 && !step2 && !test)){
////            isProcessing=true;
////            say(getResources().getString(captions.get(CHARTS_SEARCH)), false);
////            isProcessing=false;
////        }else
        if(keyCode==Util.KEY_POWER  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)) {
            say("",false,false);

            //start app
            if (step1 || step2 || test) {
                return true;
            }
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_POWER");
            }
            handler.removeCallbacks(runnableCode);
            err = 0;
            good = 0;
            isAppStarted = true;
            isProcessing = true;
            myGLRenderer.setChart(-1, -2, "", 0);
            learn.clearResult();
            myGLRenderer.resetUser(newUser);
            resetPreferences(newUser);
            greyE_left=GREY_ORG_E;
            greyE_right=GREY_ORG_E;
            greyE=GREY_ORG_E;
            greySquare_left=GREY_ORG_SQUARE;
            greySquare_right=GREY_ORG_SQUARE;
            greySquare=GREY_ORG_SQUARE;
            pixelNbr_left=1;
            pixelNbr_right=1;
            pixelNbr=1;
            noContrastLeftResult="";
            contrastLeftResult="";
            noContrastRightResult="";
            contrastRightResult="";
            if (!newUser) {
                setText("", "");
                step1 = false;
                step2 = false;
                test = true;
                eyeCalibration = -1;
                myGLRenderer.setChart(-1, -2, "", 0);
                setInfo("Test running");
                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO)), false,false);
                myGLRenderer.setCharacter(2); //E character
                float x = Util.getSharedPreferences(context).getFloat(Util.PREF_LEFT_CALIBRATION_X, 0f);
                float y = Util.getSharedPreferences(context).getFloat(Util.PREF_LEFT_CALIBRATION_Y, 0f);
                myGLRenderer.setLeftCenterX(x);
                myGLRenderer.setLeftCenterY(y);
                x = Util.getSharedPreferences(context).getFloat(Util.PREF_RIGHT_CALIBRATION_X, 0f);
                y = Util.getSharedPreferences(context).getFloat(Util.PREF_RIGHT_CALIBRATION_Y, 0f);
                myGLRenderer.setRightCenterX(x);
                myGLRenderer.setRightCenterY(y);
                eye = -1;
                myGLRenderer.setCalibrationImage(3);
                nextChart();
                restardTask(LONG_DELAY);
            } else {
                myGLRenderer.setCalibrationImage(2);
                myGLRenderer.setCharacter(1); //circle with gaps character
                setText("", "");
                setInfo("Goggle calibration");
                eyeCalibration = 0;
                say(getResources().getString(captions.get(ACTION_CALIBRATION1)), false,false);
                say(getResources().getString(captions.get(ACTION_CONTROLLER_CALIBRATION_INFO1)), true,false);
                myGLRenderer.setChart(-1, eyeCalibration, "", learn.getOptotypeOuterDiameter(1));
                say("left eye", true,false);
                step1 = true;
                step2 = false;
                test = false;
            }
            isProcessing = false;
        }  else if(keyCode==Util.KEY_BACK  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)
                && (step1==false && step2==false && test==false)){
            say("",false,false);
            isAppStarted=false;
            stopTask(true);
            isProcessing=true;
            newUser=true;
            acuityRepository.newInstallation();
            say(getResources().getString(captions.get(ACTION_RESET_USER)), false,false);
            myGLRenderer.setChart(-1, -2, "", 0);
            resetPreferences(newUser);
            step1=false;
            step2=false;
            test=false;
            handler.postDelayed(runnableCode, 2*LONG_DELAY);
            isProcessing=false;
        }else if(keyCode==Util.KEY_BACK  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            isProcessing=true;
            isAppStarted=false;
            stopTask(true);
            say("test stopped", false,false);
            endOfTest(false);
//            step1=false;
//            step2=false;
//            test=false;
//            learn.clearResult();
//            if(startedByPackage!=null) {
//                stopTask(true);
//                say("we will start SightPlus now", true, false);
//            }
//            startExternalApp(startedByPackage);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                },SHORT_DELAY);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        finish();
//                        System.exit(0);
//                    }
//                },LONG_DELAY);
//            }else {
//                myGLRenderer.setChart(-1, -2, "", 0);
//                handler.postDelayed(runnableCode, 2 * LONG_DELAY);
//                isProcessing = false;
//            }
        }else if(step1 ){
            say("",false,false);
            isProcessing=true;
            greyE_left=GREY_ORG_E;
            greyE_right=GREY_ORG_E;
            greyE=GREY_ORG_E;
            greySquare_left=GREY_ORG_SQUARE;
            greySquare_right=GREY_ORG_SQUARE;
            greySquare=GREY_ORG_SQUARE;
            pixelNbr_left=1;
            pixelNbr_right=1;
            pixelNbr=1;
            calibration1(keyCode, keyEvent);
            isProcessing=false;
        }else if(step2){
            isProcessing=true;
            calibration2(keyCode, keyEvent);
            isProcessing=false;
        }else if(test){
            isProcessing=true;
            if(isReady){
                test(keyCode, keyEvent);
            }
            isProcessing=false;
        }

        return true;
    }

    /**
     * reset or install preferences
     * @param newUser create preferences if new user
     * @return
     */
    private void resetPreferences(boolean newUser) {
        if(newUser){
            Util.upDatePref(this, Util.PREF_LEFT_CALIBRATION_X,0f);
            Util.upDatePref(this,Util.PREF_LEFT_CALIBRATION_Y,0f);
            Util.upDatePref(this,Util.PREF_RIGHT_CALIBRATION_X,0f);
            Util.upDatePref(this,Util.PREF_RIGHT_CALIBRATION_Y,0f);
            Util.upDatePref(this,Util.PREF_LEFT_START,100);
            Util.upDatePref(this,Util.PREF_RIGHT_START,100);
            Util.upDatePref(this,Util.PREF_USER_ID,-1);
        }
        noContrastLeftResult="";
        contrastLeftResult="";
        noContrastRightResult="";
        contrastRightResult="";
        //learn.clearResult();
    }

    /**
     * calibration of position of circle
     * @param keyCode key code from controller
     * @param keyEvent key code from controller
     * @return
     */
    private void calibration1(int keyCode, int keyEvent){
        if(keyCode==Util.KEY_TRIGGER && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_TRIGGER");
            }
            if(eyeCalibration==0){
                Util.upDatePref(this,Util.PREF_LEFT_CALIBRATION_X,myGLRenderer.getLeftPositionX());
                Util.upDatePref(this,Util.PREF_LEFT_CALIBRATION_Y,myGLRenderer.getLeftPositionY());
                eyeCalibration=1;
                myGLRenderer.setChart(-1, eyeCalibration, "", learn.getOptotypePixels(1));
                say(getResources().getString(captions.get(ACTION_CONTROLLER_CALIBRATION_INFO11)), true,false);
            }else{
                step1=false;
                step2=true;
                Util.upDatePref(this,Util.PREF_RIGHT_CALIBRATION_X,myGLRenderer.getRightPositionX());
                Util.upDatePref(this,Util.PREF_RIGHT_CALIBRATION_Y,myGLRenderer.getRightPositionY());
                eyeCalibration=0;
                myGLRenderer.setGrey(eyeCalibration,greyE,greySquare);
                myGLRenderer.setChart(-1, -2, "", 0);
                setInfo("Chart calibration");
                say(getResources().getString(captions.get(ACTION_CALIBRATION2)), false,false);
                say(getResources().getString(captions.get(ACTION_CONTROLLER_CALIBRATION_INFO2)), true,false);
                myGLRenderer.setCalibrationImage(1);
                eye=-1;
                chart=0;//totalLengthCharts*2/3;
                myGLRenderer.setChart(chart,eyeCalibration,"all", learn.getOptotypePixels(chart) );
                say("left eye", true,false);
            }

        }else if(keyCode==Util.KEY_UP){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_UP");
            }
            if(eyeCalibration==0){
                myGLRenderer.setLeftPositionY(-5f);
            }else if(eyeCalibration==1){
                myGLRenderer.setRightPositionY(5f);
            }else{
                myGLRenderer.setLeftPositionY(5f);
                myGLRenderer.setRightPositionY(5f);
            }
        }else if(keyCode==Util.KEY_DOWN){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_DOWN");
            }
            if(eyeCalibration==0){
                myGLRenderer.setLeftPositionY(5f);
            }else if(eyeCalibration==1){
                myGLRenderer.setRightPositionY(-5f);
            }else{
                myGLRenderer.setLeftPositionY(-5f);
                myGLRenderer.setRightPositionY(-5f);
            }
        }else if(keyCode==Util.KEY_LEFT){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_LEFT");
            }
            if(eyeCalibration==0){
                myGLRenderer.setLeftPositionX(5f);
            }else if(eyeCalibration==1){
                myGLRenderer.setRightPositionX(-5f);
            }else{
                myGLRenderer.setLeftPositionX(-5f);
                myGLRenderer.setRightPositionX(-5f);
            }
        }else if(keyCode==Util.KEY_RIGHT){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_RIGHT");
            }
            if(eyeCalibration==0){
                myGLRenderer.setLeftPositionX(-5f);
            }else if(eyeCalibration==1){
                myGLRenderer.setRightPositionX(5f);
            }else{
                myGLRenderer.setLeftPositionX(5f);
                myGLRenderer.setRightPositionX(5f);
            }
        }else if(keyCode==Util.KEY_BACK  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_BACK");
            }
            endOfTest(false);
        }
    }
    /**
     * calibration of sizes of chart to use
     * @param keyCode key code from controller
     * @param keyEvent key code from controller
     * @return
     */
    private void calibration2(int keyCode, int keyEvent){
        if(keyCode==Util.KEY_TRIGGER && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_TRIGGER chart= "+chart);
            }
            if(eyeCalibration==0){
                if(chart>2 && chart<totalLengthCharts-1){
                    chart=chart-2;
                }else if(chart>=totalLengthCharts-1){
                    chart=totalLengthCharts-4;
                }else{
                    chart=0;
                }
                Util.upDatePref(this, Util.PREF_LEFT_START,chart);
                eyeCalibration=1;
                chart=0;//totalLengthCharts*2/3;
                myGLRenderer.setChart(chart,eyeCalibration,"all", learn.getOptotypePixels(chart) );
                say(getResources().getString(captions.get(ACTION_CONTROLLER_CALIBRATION_INFO21)), true,false);
            }else{
                step2=false;
                test=true;
                eyeCalibration=-1;
                myGLRenderer.setChart(-1, -2, "", 0);
                setInfo("Test running");
                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO)), false,false);
                myGLRenderer.setCharacter(2);
                if(chart>2 && chart<totalLengthCharts-1){
                    chart=chart-2;
                }else if(chart>=totalLengthCharts-1){
                    chart=totalLengthCharts-4;
                }else{
                    chart=0;
                }
                Util.upDatePref(this, Util.PREF_RIGHT_START,chart);
                newUser=false;
                eye=-1;
                myGLRenderer.setCalibrationImage(3);
                nextChart();
                restardTask(LONG_DELAY);
            }
        }else if(keyCode==Util.KEY_UP && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_UP chart= "+chart+ " OptotypePixels= "+ learn.getOptotypePixels(chart));
            }
            chart=chart-1;
            if(chart <0){
                chart=0;
            }
            if(eyeCalibration==0 || eyeCalibration==1){
                myGLRenderer.setChart(chart,eyeCalibration,"all", learn.getOptotypePixels(chart) );
            }else{
                myGLRenderer.setChart(chart,eye,"all", learn.getOptotypePixels(chart) );
            }
//            say(getResources().getString(captions.get(ACTION_CALIBRATION_CHECK)), false);
        }else if(keyCode==Util.KEY_DOWN && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_DOWN chart= "+chart + " OptotypePixels= "+ learn.getOptotypePixels(chart));
            }
            chart=chart+1;
            if(chart > totalLengthCharts-1){
                chart=totalLengthCharts-1;
            }
            if(eyeCalibration==0 || eyeCalibration==1){
                myGLRenderer.setChart(chart,eyeCalibration,"all", learn.getOptotypePixels(chart) );
            }else{
                myGLRenderer.setChart(chart,eye,"all", learn.getOptotypePixels(chart) );
            }
//            say(getResources().getString(captions.get(ACTION_CALIBRATION_CHECK)), false);
        }else if(keyCode==Util.KEY_LEFT  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_LEFT");
            }

        }else if(keyCode==Util.KEY_RIGHT && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_RIGHT");
            }

        }else if(keyCode==Util.KEY_BACK  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_BACK");
            }
            endOfTest(false);
        }
    }

    /**
     * acuity test for both eyes
     * @param keyCode key code from controller
     * @param keyEvent key code from controller
     * @return
     */
    private void test(int keyCode, int keyEvent){
        say(" ",false,false);
        if(keyCode==Util.KEY_TRIGGER && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_TRIGGER");
            }
            //start checking or next chart
//            nextChart();
        }else if(keyCode==Util.KEY_UP  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            isReady=false;
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_UP");
            }
            resultChart("up");
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);

        }else if(keyCode==Util.KEY_DOWN  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            isReady=false;
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_DOWN");
            }
            resultChart("down");
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_LEFT  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            isReady=false;
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_LEFT");
            }
            resultChart("left");
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_RIGHT  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            isReady=false;
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_RIGHT");
            }
            resultChart("right");
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_BACK  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            isReady=false;
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_BACK");
            }
            endOfTest(true);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }
    }
    /**
     * new chart sizes or stop test
     * @param
     * @param
     * @return
     */
    private void nextChart() {
        chartPos=0;
        err=0;
        good=0;
        if(eye==-1){
            //start new test with left eye
            eye=0;
            isContrast=false;
            chart=Util.getSharedPreferences(context).getInt(Util.PREF_LEFT_START,FIRST_CHART_LEFT_EYE);
            totalLengthStringArray=learn.getSizeChartsPos(chart);
            if(!isContrast){
                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO1)), true,false);
            }
            learn.clearResult();
        }else{
            if(learn.isResultOk(chart,eye)){
                chart++;
                if(chart>=totalLengthCharts){
                    //go to next eye or is not contrast test go to contrast test
                    if(!isContrast){
                        //first test with contrast done
                        //go to contrast test for left or right eye
                        //memorise no contrast test
                        if (eye == 0) {
                            noContrastLeftResult=learn.getEyeResult(eye);
                            chart=Util.getSharedPreferences(context).getInt(Util.PREF_LEFT_START,FIRST_CHART_LEFT_EYE);
                        }else if(eye==1){
                            noContrastRightResult=learn.getEyeResult(eye);
                            chart=Util.getSharedPreferences(context).getInt(Util.PREF_RIGHT_START,FIRST_CHART_RIGHT_EYE);
                            if (Util.DEBUG) {
                                Log.d(LOG_TAG_MAIN, "Result noContrastLeftResult= " + noContrastLeftResult
                                        + " contrastLeftResult= " + contrastLeftResult);
                            }
                            //insert in database no contrast tests
                            AsyncTask.execute( new Runnable() {
                                @Override
                                public void run() {
                                    handler0.removeCallbacks(runnableCode0);
                                    int userId=Util.getSharedPreferences(context).getInt(Util.PREF_USER_ID,-1);
                                    if(userId>-1){
                                        acuityRepository.insertAcuity(userId, false,noContrastLeftResult,noContrastRightResult);
                                    }
                                    handler0.postDelayed(runnableCode0, 100);
                                }
                            });
                        }
                        totalLengthStringArray=learn.getSizeChartsPos(chart);
                        learn.clearResult();
                        isContrast=true;
                    }else{
                        //second test with contrast done
                        //memorise no contrast test
                        //go to contrast test for right eye or end test
                        if (eye == 0) {
                            contrastLeftResult=learn.getEyeResult(eye);
                            isContrast=false;
                        }else if(eye==1){
                            contrastRightResult=learn.getEyeResult(eye);
                            //insert in database contrast tests
                            AsyncTask.execute( new Runnable() {
                                @Override
                                public void run() {
                                    handler0.removeCallbacks(runnableCode0);
                                    int userId=Util.getSharedPreferences(context).getInt(Util.PREF_USER_ID,-1);
                                    if(userId>-1){
                                        acuityRepository.insertAcuity(userId, true,contrastLeftResult,contrastRightResult);
                                    }
                                    handler0.postDelayed(runnableCode0, 100);
                                }
                            });
                            endOfTest(true);
                            return;
                        }
                        chart= totalLengthCharts;
                    }
                }
            }else{
                //go to next eye or is not contrast test go to contrast test
                if(!isContrast){
                    //first test with contrast done
                    //go to contrast test for left or right eye
                    //memorise no contrast test
                    if (eye == 0) {
                        noContrastLeftResult=learn.getEyeResult(eye);
                        chart=Util.getSharedPreferences(context).getInt(Util.PREF_LEFT_START,FIRST_CHART_LEFT_EYE);
                    }else if(eye==1){
                        noContrastRightResult=learn.getEyeResult(eye);
                        chart=Util.getSharedPreferences(context).getInt(Util.PREF_RIGHT_START,FIRST_CHART_RIGHT_EYE);
                        if (Util.DEBUG) {
                            Log.d(LOG_TAG_MAIN, "Result noContrastLeftResult= " + noContrastLeftResult
                                    + " contrastLeftResult= " + contrastLeftResult);
                        }
                        //insert in database no contrast tests
                        AsyncTask.execute( new Runnable() {
                            @Override
                            public void run() {
                                handler0.removeCallbacks(runnableCode0);
                                int userId=Util.getSharedPreferences(context).getInt(Util.PREF_USER_ID,-1);
                                if(userId>-1){
                                    acuityRepository.insertAcuity(userId, false,noContrastLeftResult,noContrastRightResult);
                                }
                                handler0.postDelayed(runnableCode0, 100);
                            }
                        });
                    }
                    totalLengthStringArray=learn.getSizeChartsPos(chart);
                    learn.clearResult();
                    isContrast=true;
                }else{
                    //second test with contrast done
                    //memorise no contrast test
                    //go to contrast test for right eye or end test
                    if (eye == 0) {
                        contrastLeftResult=learn.getEyeResult(eye);
                        isContrast=false;
                    }else if(eye==1){
                        contrastRightResult=learn.getEyeResult(eye);
                        //insert in database contrast tests
                        AsyncTask.execute( new Runnable() {
                            @Override
                            public void run() {
                                handler0.removeCallbacks(runnableCode0);
                                int userId=Util.getSharedPreferences(context).getInt(Util.PREF_USER_ID,-1);
                                if(userId>-1){
                                    acuityRepository.insertAcuity(userId, true,contrastLeftResult,contrastRightResult);
                                }
                                handler0.postDelayed(runnableCode0, 100);
                            }
                        });
                        endOfTest(true);
                        return;
                    }
                    chart= totalLengthCharts;
                }
            }
        }

        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_KEY, "eye= " + eye +" totalLengthCharts= " + totalLengthCharts +
                    " chart=" + chart + " totalLengthStringArray=" + totalLengthStringArray );
        }
        setInfo("test started");
        if(chart>=totalLengthCharts){
            if(eye<1){
                //go to right eye
                chart=Util.getSharedPreferences(context).getInt(Util.PREF_RIGHT_START,FIRST_CHART_RIGHT_EYE);
                eye=1;
                setText("","");
                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO2)), true,false);
            }
//            else {
//                contrastRightResult=learn.getEyeResult(eye);
//                //insert in database contrast tests
//                AsyncTask.execute( new Runnable() {
//                    @Override
//                    public void run() {
//                        handler0.removeCallbacks(runnableCode0);
//                        int userId=Util.getSharedPreferences(context).getInt(Util.PREF_USER_ID,-1);
//                        if(userId>-1){
//                            acuityRepository.insertAcuity(userId, true,contrastLeftResult,contrastRightResult);
//                        }
//                        handler0.postDelayed(runnableCode0, 0);
//                    }
//                });
//                setText("","");
//                endOfTest();
//                return;
//            }
        }
        //new chart test
//      toneL.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        setText("","");
        totalLengthStringArray=learn.getSizeChartsPos(chart);
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_KEY, "eye= " + eye +" chart=" + chart);
        }
        greyE = GREY_E;//(int) learn.getOptotypeEgrey(chart);
        greySquare = GREY_B; //(int) learn.getOptotypeSquaregrey(chart);
        if(isContrast){
            myGLRenderer.setGrey(eye,greyE,greySquare);
        }else {
            myGLRenderer.setGrey(eye,0,255);
        }
        myGLRenderer.setChart(chart, eye, learn.getChartPosString(chart, chartPos), learn.getOptotypePixels(chart));
        if(test){
             restardTask(LONG_DELAY);
        }
    }

    /**
     * check if test finish
     * @param result string of user's result
     * @param
     * @return
     */
    private void resultChart(String result) {
        if(chart==-1){
            return;
        }
        stopTask(true);
//        setText(result,"");
        if(chart>-1 && chart<=totalLengthCharts-1){
            if(chartPos<=totalLengthStringArray-1){
                //test in the position
//                int r=learn.setResult(chart, chartPos, result,eye);
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_KEY, "result= "+result+" chart= "+chart+
                            " chartPos= "+chartPos+" totalLengthStringArray= "+totalLengthStringArray);
                }
                //result card
                if(learn.setResult(chart, chartPos, result,eye)<1){
                    //if error (-1 or 0)
                    err=err+1;
                    toneL.startTone(ToneGenerator.TONE_DTMF_0,200);
                }else{
                    //if good answer
                    good=good+1;
                    toneH.startTone(ToneGenerator.TONE_DTMF_1,200);
                }
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_KEY, "good= "+good+"err= "+err+" result= "+result+" chart= "+chart+
                            " chartPos= "+chartPos+" totalLengthStringArray= "+totalLengthStringArray);
                }
                if(err>=2 || good>=3){
                    myGLRenderer.setChart(-1, -2, "", 0);
                    sleep(1000);
                    nextChart();
                    return;
                }else{
                    chartPos++;
                    myGLRenderer.setChart(-1, -2, "", 0);
                    sleep(1000);
                    //end of card in this sizes
                    if(chartPos>totalLengthStringArray-1){
                        nextChart();
                        return;
                    }
                    //chartPos was -2 before this procedure
                    if(chartPos==-1){
                        //no test
                        myGLRenderer.setChart(-1, eye, "", learn.getOptotypePixels(0));
                    }else{
                        //test
                        myGLRenderer.setChart(chart, eye, learn.getChartPosString(chart, chartPos), learn.getOptotypePixels(chart));
                    }
                }
            }else{
                //go next level the chart or stop
                myGLRenderer.setChart(-1, -2, "", 0);
                sleep(1000);
                nextChart();
                return;
            }
            if(test){
                restardTask(LONG_DELAY);
            }
        }
    }
    /**
     * end of test
     * @param
     * @param
     * @param ok
     * @return
     */
    private void endOfTest(boolean ok) {
        stopTask(false);
        setText("","");
        isTimerStart=false;
        chartPos=-1;
        err=0;
        good=0;
        isContrast=false;
        myGLRenderer.setChart(-1, -2, "", 0);
        setInfo("end of test");
        say("end of test",false,false);
        if (Util.DEBUG) {
            Log.d(LOG_TAG_MAIN, "test end test= "+test+" ok= "+ok+" eye= "+eye
                    +" left:  noContrastLeftResult= "+ noContrastLeftResult+ " contrastLeftResult= "+ contrastLeftResult
                    + " right:  noContrastRightResult= "+ noContrastRightResult+ " contrastRightResult= "+ contrastRightResult);}
        if(test && ok){
            if(noContrastLeftResult==""){
//                noContrastLeftResult="no reading";
            }else{
                noContrastLeftResult=learn.getPixelsFromMunit(noContrastLeftResult);
            }
            if(noContrastRightResult==""){
//                noContrastRightResult="no reading";
            }else{
                noContrastRightResult=learn.getPixelsFromMunit(noContrastRightResult);
            }
            if(contrastLeftResult==""){
//                contrastLeftResult="no reading";
            }else{
                contrastLeftResult=learn.getPixelsFromMunit(contrastLeftResult);
            }
            if(contrastRightResult==""){
//                contrastRightResult="no reading";
            }else{
                contrastRightResult=learn.getPixelsFromMunit(contrastRightResult);
            }

            setText("left eye: "+noContrastLeftResult +" / " +contrastLeftResult,
                    "right eye: "+noContrastRightResult +" / " +contrastRightResult);
            say(getResources().getString(captions.get(ACTION_RESULT_LEFT))+" "+noContrastLeftResult +" and " +contrastLeftResult, true,false);
            say(getResources().getString(captions.get(ACTION_RESULT_RIGHT))+" "+noContrastRightResult +" and " +contrastRightResult, true,false);
            noContrastLeftResult="";
            contrastLeftResult="";
            noContrastRightResult="";
            contrastRightResult="";
        }else{
//            setText("","");
//            setInfo("Preparing the test");
            if(noContrastLeftResult==""){
                noContrastLeftResult="-1";
            }
            if(noContrastRightResult==""){
                noContrastRightResult="-1";
            }
            if(contrastLeftResult==""){
                contrastLeftResult="-1";
            }
            if(contrastRightResult==""){
                contrastRightResult="-1";
            }
            if (Util.DEBUG) {
                Log.d(LOG_TAG_MAIN, "test end test= "+test+" ok= "+ok
                    +" left:  noContrastLeftResult= "+ noContrastLeftResult+ " contrastLeftResult= "+ contrastLeftResult
                    +" right:  noContrastRightResult= "+ noContrastRightResult+ " contrastRightResult= "+ contrastRightResult);}

        //insert in database contrast tests
            AsyncTask.execute( new Runnable() {
                @Override
                public void run() {
                    handler0.removeCallbacks(runnableCode0);
                    int userId=Util.getSharedPreferences(context).getInt(Util.PREF_USER_ID,-1);
                    if(userId>-1){
                        acuityRepository.insertAcuity(userId, false,noContrastLeftResult,noContrastRightResult);
                        acuityRepository.insertAcuity(userId, true,contrastLeftResult,contrastRightResult);
                    }
                    handler0.postDelayed(runnableCode0, 100);
                }
            });
        }
        chart=-1;
        eye=-2;
        if (Util.DEBUG) {
            Log.d(LOG_TAG_MAIN, "startedByPackage="+ startedByPackage);}
        if(startedByPackage!=null){
            say("we will start SightPlus now",true,true);
//                sendBroadcastToActivity(BROADCAST_START_APP_ACTION, START_APP_RESULT, 1);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startExternalApp(startedByPackage);
                }
            },SHORT_DELAY);

        }
        handler.removeCallbacks(runnableCode);
        handler.postDelayed(runnableCode, LONG_DELAY+LONG_DELAY);
        isAppStarted=false;
        step2=false;
        step1=false;
        test=false;
        isContrast=false;
    }

    private void insertAcuity() {
        AsyncTask.execute( new Runnable() {
            @Override
            public void run() {
                if (Util.DEBUG) {
                    Log.d(LOG_TAG_MAIN, "Result noContrastLeftResult= " + noContrastLeftResult
                            + " contrastLeftResult= " + contrastLeftResult);
                    Log.d(LOG_TAG_MAIN, "Result noContrastRightResult= " + noContrastRightResult
                            + " contrastRightResult= " + contrastRightResult);
                }
                handler0.removeCallbacks(runnableCode0);
                int userId=Util.getSharedPreferences(context).getInt(Util.PREF_USER_ID,-1);
                if(userId>-1){
                    acuityRepository.insertAcuity(userId, false,noContrastLeftResult,noContrastRightResult);
                    acuityRepository.insertAcuity(userId, true,contrastLeftResult,contrastRightResult);
                }
                handler0.postDelayed(runnableCode0, SHORT_DELAY);
            }
        });
    }

    /**
     * speak service
     * @param toSpeak
     * @param queue
     * @return
     */
    private void say(String toSpeak, boolean queue, boolean block){
        if (Util.DEBUG) {
            Log.i(LOG_TAG_MAIN, " say toSpeak= "+toSpeak);        }
        if(mTTS!=null){
            if(block){
                while (mTTS.isSpeaking()){
                    new SleepThread(100).start();
                }
            }
            String utteranceId=this.hashCode() + "";
            if(queue) {
                mTTS.speak(toSpeak, TextToSpeech.QUEUE_ADD, null, utteranceId);
            }else {
                mTTS.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
            }
            if(block){
                while (mTTS.isSpeaking()){
                    new SleepThread(100).start();
                }
            }
        }
    }

    /**
     * set text on the screen
     * @param txt left eye
     * @param txt1 right eye
     * @return
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
     * set info text
     * @param txt
     * @return
     */
    private void setInfo(String txt){
        mTextInfo1.setText(txt);
        mTextInfo1.bringToFront();
        mTextInfo2.setText(txt);
        mTextInfo2.bringToFront();
    }
    /**
     * test if gle version is validate
     * @param
     * @return gleVersion
     */
    private boolean hasGLES20() {
        ActivityManager am = (ActivityManager)
                getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return info.reqGlEsVersion >= 0x20000;
    }

    /**
     * configuration AP
     * @param context
     * @param ssid
     * @param ssidPassword
     * @return boolean
     */
    public static boolean connectWiFi(Context context, String ssid, String ssidPassword) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + ssid + "\"";   // Please note the quotes. String should contain ssid in quotes
        conf.status = WifiConfiguration.Status.ENABLED;
        conf.priority = 40;
        //  WPA network you need to add passphrase like this:
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.preSharedKey = "\"" + ssidPassword + "\"";

        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        int networkId = wifiManager.addNetwork(conf);
        if (Util.DEBUG) {
            Log.v(LOG_TAG_MAIN, "networkId " + networkId);   }

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * check if is connected and internet is available
     * @param context
     * @return boolean
     */
    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork == null) return false;

        if (Util.DEBUG) {
            Log.d(LOG_TAG_MAIN, "activeNetwork= " + activeNetwork.getExtraInfo() +
                    " state= "+ activeNetwork.getState()+ " type= "+ activeNetwork.getType());
        }

        switch (activeNetwork.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                if ((activeNetwork.getState() == NetworkInfo.State.CONNECTED ||
                        activeNetwork.getState() == NetworkInfo.State.CONNECTING) &&
                        isInternet())
                    return true;
                break;
            case ConnectivityManager.TYPE_MOBILE:
                if ((activeNetwork.getState() == NetworkInfo.State.CONNECTED ||
                        activeNetwork.getState() == NetworkInfo.State.CONNECTING) &&
                        isInternet())
                    return true;
                break;
            default:
                return false;
        }
        return false;
    }

    /**
     * check if internet is available
     * @param
     * @return boolean
     */
    private static boolean isInternet() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            if (Util.DEBUG) {
                Log.d(LOG_TAG_MAIN, "exitValue " + exitValue);}
            return(exitValue==0);
        } catch (IOException e)          {
            Log.e(LOG_TAG_MAIN, "IOException " + e.getCause());
        } catch (InterruptedException e) {
            Log.e(LOG_TAG_MAIN, "IOException " + e.getCause());
        }
        return false;
    }

    private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void startExternalApp(String app ) {
        sendBroadcastToActivity(BROADCAST_START_APP_ACTION, START_APP_RESULT, 2);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                System.exit(-1);
            }
        },SHORT_DELAY);

//        boolean isInstalled = isPackageInstalled(app, this.getPackageManager());
//        if(isInstalled){
////			memorizeState();
//            PackageManager pm = getPackageManager();
//            Intent intent = pm.getLaunchIntentForPackage(startedByPackage);
//            intent.putExtra("startedByPackage","com.givevision.rochesightchart" );
//            startActivity(intent);
//            startedByPackage =null;
//            finish();
//            System.exit(0);
//        }else {
//            Log.e(LOG_TAG_MAIN, "application to start not found");
//        }
    }

    public void sendBroadcastToActivity(String action, String code, int result) {
        Intent new_intent = new Intent();
        new_intent.setAction(action);
        new_intent.putExtra(code,result);
        sendBroadcast(new_intent);
        Log.i(TAG, "Broadcast sent to Activity action= "+action+ " code= "+code+ " result= "+result);
    }
}

