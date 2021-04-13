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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import static android.os.SystemClock.sleep;
import static com.givevision.rochesightchart.Util.LOG_TAG_KEY;
import static com.givevision.rochesightchart.Util.LOG_TAG_MAIN;
import static com.givevision.rochesightchart.Util.TAG;


//https://cmusphinx.github.io/wiki/tutorialandroid/

public class MainActivity extends Activity {
    //5% contrast – this is with background 186 and foreground 178 pixel intensities
    private static  int GREY_E = 178;
    private static  int GREY_B = 186;
    //2.5% contrast, with background 186 and foreground 182
    private static  int GREY_E_1 = 182;
    private static  int GREY_B_1 = 186;
    private static  int WHITE = 255;
    private static  int BLACK = 0;
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
    private static final String ACTION_CONTROLLER_TEST_INFO11 = "controller test info11";
    private static final String ACTION_CONTROLLER_TEST_INFO12 = "controller test info12";
    private static final String ACTION_CONTROLLER_TEST_INFO2 = "controller test info2";
    private static final String ACTION_CONTROLLER_TEST_INFO3 = "controller test info3";
    private static final String ACTION_CONTROLLER_TEST_INFO4 = "controller test info4";
    private static final String ACTION_CONTROLLER_TEST_INFO5 = "controller test info5";
    private static final String ACTION_CALIBRATION_CHECK = "calibration check";
    private static final String ACTION_CONTROLLER = "controller";
    private static final String ACTION_VOICE = "voice";
    private static final String ACTION_REPEAT_TEST = "repeat test";
    private static final int NEXT_CHART_FOR_MANUAL_STOP=-1;
    private static final int NEXT_CHART_FOR_ERROR=0;
    private static final int NEXT_CHART_FOR_GOOD=1;
    private static final int NEXT_CHART_FOR_NO_READING=2;
    private static final int NEXT_CHART_FOR_NEW_TEST=3;
    private static final int NEXT_CHART_FOR_NEW_LEVEL=4;

    private boolean step1=false;
    private boolean step2=false;
    private boolean test=false;
    private boolean isDoubleTouche;
    private RelativeLayout   relativeLayout  ;
    private boolean isReadyForSpeech;
    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 3;
    private static final int LONG_DELAY = 10000;
    private static final int SHORT_DELAY = 5000;
    private static final int KEY_DELAY = 500;
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
    private  static int FIRST_CHART_LEFT_EYE_0=0;
    private  static int FIRST_CHART_RIGHT_EYE_0=0;
    private  static int FIRST_CHART_LEFT_EYE_1=0;
    private  static int FIRST_CHART_RIGHT_EYE_1=0;
    private  static int FIRST_CHART_LEFT_EYE_2=0;
    private  static int FIRST_CHART_RIGHT_EYE_2=0;
    private  static int GREY_ORG_E=125;
    private  static int GREY_ORG_SQUARE=126;
    private static int CHART_OFFSET=0;
    private static final String BROADCAST_START_APP_ACTION="start app action";
    private static final String START_APP_RESULT = "app response" ;

    private long startTest=0;
    private int chart=0;
    private int chartPos=-1;
    private boolean learning=false;
    private int eye=-1; //0-left 1-right -1-double
    private int contrastActive; //-1- nothing 0- noContrast 1-contras 5% 2-contrast 2.5%
    private String contrastLeftResult;
    private String contrastRightResult;
    private String contrast_1LeftResult;
    private String contrast_1RightResult;
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
    private float[] bestAcuities0;
    private float[] bestAcuities1;
    private float[] bestAcuities2;
    private boolean toRepeatTest;
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
//            if(isTimerStart) {
//                isTimerStart=false;
                say(getResources().getString(captions.get(ACTION_TEST_REMINDER)), false,false);
//                handler2.removeCallbacks(runnableCode2);
//                handler2.postDelayed(runnableCode2, SHORT_DELAY);
//                isSecondPeriod=true;
//            }
            handler1.postDelayed(runnableCode1, LONG_DELAY);
        }
    };
    //   test go next
//    private Runnable runnableCode2 = new Runnable() {
//        @Override
//        public void run() {
//            if(isSecondPeriod){
////                stopTask(false);
//                good=0;
//                err=0;
//                nextChart(NEXT_CHART_FOR_ERROR);
////                resultChart("error");
//            }
//        }
//    };
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
                                        Log.i(LOG_TAG_MAIN, "runnableCode0 acuity found: "
                                                +" id=  "+ acuity.getId()
                                                +" userId=  "+ acuity.getUserId()
                                                +" contrast=  "+ acuity.getContrast()
                                                +" duration=  "+ acuity.getDuration()
                                                +" leftEyeFirst=  "+ acuity.getLeftEyeFirst()
                                                +" rightEyeFirst=  "+ acuity.getRightEyeFirst()
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
                Log.i(LOG_TAG_MAIN, "runnableCode0 isOnline= "+isInternetAvailable(context));
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
//        handler2.removeCallbacks(runnableCode2);
    }

    /**
     * restart the background tasks
     * @param delay start runnable code 1 with defined delay
     * @return
     */
    void restartTask(int delay) {
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
            findViewById(R.id.up).setOnClickListener(view -> mainDispatchKeyEvent(KeyEvent.ACTION_DOWN, Util.KEY_UP));
            findViewById(R.id.down).setOnClickListener(view -> mainDispatchKeyEvent(KeyEvent.ACTION_DOWN, Util.KEY_DOWN));
            findViewById(R.id.trigger).setOnClickListener(view -> mainDispatchKeyEvent(KeyEvent.ACTION_UP, Util.KEY_TRIGGER));
            findViewById(R.id.left).setOnClickListener(view -> mainDispatchKeyEvent(KeyEvent.ACTION_DOWN, Util.KEY_LEFT));
            findViewById(R.id.right).setOnClickListener(view -> mainDispatchKeyEvent(KeyEvent.ACTION_DOWN, Util.KEY_RIGHT));
            findViewById(R.id.power).setOnClickListener(view -> mainDispatchKeyEvent(KeyEvent.ACTION_UP, Util.KEY_POWER));
            findViewById(R.id.back).setOnClickListener(view -> mainDispatchKeyEvent(KeyEvent.ACTION_UP, Util.KEY_BACK));
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
        captions.put(ACTION_CONTROLLER_TEST_INFO11, R.string.action_controller_test_info11);
        captions.put(ACTION_CONTROLLER_TEST_INFO12, R.string.action_controller_test_info12);
        captions.put(ACTION_CONTROLLER_TEST_INFO2, R.string.action_controller_test_info2);
        captions.put(ACTION_CONTROLLER_TEST_INFO3, R.string.action_controller_test_info3);
        captions.put(ACTION_CONTROLLER_TEST_INFO4, R.string.action_controller_test_info4);
        captions.put(ACTION_CONTROLLER_TEST_INFO5, R.string.action_controller_test_info5);
        captions.put(ACTION_VOICE_TEST_INFO, R.string.action_voice_test_info);
        captions.put(ACTION_RESET_USER, R.string.action_reset_user);
        captions.put(ACTION_RESULT_LEFT, R.string.result_left_info);
        captions.put(ACTION_RESULT_RIGHT, R.string.result_right_info);
        captions.put(ACTION_REPEAT_TEST, R.string.action_repeat_test);
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
        isProcessing=false;
        chart=-1;
        chartPos=-1;
        eye=-1;
        if(Util.getSharedPreferences(this).getInt(Util.PREF_LEFT0_START,100)==100 ||
                Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START,100)==100){
            newUser=true;
        }else{
            newUser=false;
        }
        Util.upDatePref(this,Util.PREF_LEFT0_START,FIRST_CHART_LEFT_EYE_0);
        Util.upDatePref(this,Util.PREF_RIGHT0_START,FIRST_CHART_RIGHT_EYE_0);
        Util.upDatePref(this,Util.PREF_LEFT1_START,FIRST_CHART_LEFT_EYE_1);
        Util.upDatePref(this,Util.PREF_RIGHT1_START,FIRST_CHART_RIGHT_EYE_1);
        Util.upDatePref(this,Util.PREF_LEFT2_START,FIRST_CHART_LEFT_EYE_2);
        Util.upDatePref(this,Util.PREF_RIGHT2_START,FIRST_CHART_RIGHT_EYE_2);

        handler0.removeCallbacks(runnableCode0);
        handler0.postDelayed(runnableCode0, KEY_DELAY);

        //read CONTRAST LEVEL from file
//        ArrayList<String> lists= Util.readConfigFile(CONF_PATH);
//        if(lists!=null){
//            if (Util.DEBUG) {
//                Log.i(Util.LOG_TAG_MAIN, "seq= "+lists.size());
//            }
//            String str=lists.get(0);
//            Log.i(Util.LOG_TAG_MAIN, "str= "+str);
//            String[] arrayOfString=str.split(",");
//            GREY_E= Integer.parseInt(arrayOfString[0]);
//            GREY_B= Integer.parseInt(arrayOfString[1]);
//            ArrayList<Integer> list =new ArrayList<Integer>();
//        }else{
//            if (Util.DEBUG) {
//                Log.i(Util.LOG_TAG_MAIN, "contrast static");
//            }
//        }
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
//        handler2.removeCallbacks(runnableCode2);

        if(mTTS !=null){
            mTTS.stop();
            mTTS.shutdown();
            mTTS=null;
            isTTS=false;
        }
        step1=false;
        step2=false;
        test=false;
        contrastActive=0;
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
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (Util.DEBUG) {
            Log.i(LOG_TAG_KEY, "dispatchKeyEvent keyCode: select: "+event.getKeyCode()+" action: "+event.getAction()
                    +" newUser: "+newUser+ " isProcessing= "+isProcessing);
        }
        if(isProcessing && !isDoubleTouche && !step1){
            return true;
        }
        int keyCode=event.getKeyCode();
        int keyAction=event.getAction();
        int keyCodeRead;
        if((step1 || (!step1 && !step2 && !test)) &&
                (keyCode==Util.KEY_TRIGGER && keyAction==KeyEvent.ACTION_DOWN)){
            isDoubleTouche=true;
        }else if(keyCode==Util.KEY_TRIGGER && keyAction==KeyEvent.ACTION_UP){
            isDoubleTouche=false;
        }

        //TODO:: test for good button touched
//        if(test && (keyCode==Util.KEY_RIGHT || keyCode==Util.KEY_LEFT || keyCode==Util.KEY_UP || keyCode==Util.KEY_DOWN)){
//            if(!isTouchProcessing && listOfTouchs.size()>=Util.KEY_NBR){
//                isTouchProcessing=true;
//                int up=Collections.frequency(listOfTouchs,Util.KEY_UP);
//                int down= Collections.frequency(listOfTouchs,Util.KEY_DOWN);
//                int left=Collections.frequency(listOfTouchs,Util.KEY_LEFT);
//                int right=Collections.frequency(listOfTouchs,Util.KEY_RIGHT);
//                if(up>down && up>left && up>right){
//                    keyCodeRead=up;
//                }else if(down>up && down>left && down>right){
//                    keyCodeRead=down;
//                }else if(left>up && left>down && left>right){
//                    keyCodeRead=left;
//                }else if(right>up && right>down && right>left){
//                    keyCodeRead=right;
//                    isTouchProcessing=false;
//                    return mainDispatchKeyEvent(new KeyEvent(keyCodeRead,KeyEvent.ACTION_DOWN));
//                }else{
//                    if(!isTouchProcessing){
//                        listOfTouchs.add(keyCode);
//                    }
//                    return true;
//                }
//            }
//        }

        return mainDispatchKeyEvent(keyCode,keyAction);
    }

    /**
     * controller button action
     * @param keyCode
     * @param keyAction
     * @return boolean true for key acceptation
     */
    private boolean mainDispatchKeyEvent(int keyCode, int keyAction) {

        if (Util.DEBUG) {
            Log.i(LOG_TAG_KEY, "mainDispatchKeyEvent keyCode: select: "+keyCode+" action: "+keyAction
                    +" isDoubleTouche: "+isDoubleTouche +" newUser: "+newUser+
                    " step1= "+step1+" step2= "+step2+" test= "+test);
        }
        //TODO:: removed for test
////        if(keyCode==Util.KEY_TRIGGER && keyEvent == KeyEvent.ACTION_UP && (!step1 && !step2 && !test)){
////            isProcessing=true;
////            say(getResources().getString(captions.get(CHARTS_SEARCH)), false);
////            isProcessing=false;
////        }else
        if(!step1 && !step2 && !test && keyCode==Util.KEY_POWER  && (keyAction == KeyEvent.ACTION_UP || fakeControls)) {
            //start app
            isProcessing = true;
            say("",false,false);

            startTest=Calendar.getInstance().getTimeInMillis();
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_POWER");
            }
            handler.removeCallbacks(runnableCode);
            err = 0;
            good = 0;
            isAppStarted = true;
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
            contrast_1LeftResult="";
            noContrastRightResult="";
            contrastRightResult="";
            contrast_1RightResult="";
            if (!newUser) {
                setText("", "");
                step1 = false;
                step2 = true;
                test = false;
                eyeCalibration = -1;
                myGLRenderer.setChart(-1, -2, "", 0);
                myGLRenderer.setCharacter(2); //E character
                float x = Util.getSharedPreferences(this).getFloat(Util.PREF_LEFT_CALIBRATION_X, 0f);
                float y = Util.getSharedPreferences(this).getFloat(Util.PREF_LEFT_CALIBRATION_Y, 0f);
                myGLRenderer.setLeftCenterX(x);
                myGLRenderer.setLeftCenterY(y);
                x = Util.getSharedPreferences(this).getFloat(Util.PREF_RIGHT_CALIBRATION_X, 0f);
                y = Util.getSharedPreferences(this).getFloat(Util.PREF_RIGHT_CALIBRATION_Y, 0f);
                myGLRenderer.setRightCenterX(x);
                myGLRenderer.setRightCenterY(y);
                eye = -1;
                myGLRenderer.setCalibrationImage(3);
                contrastActive=0;
                startCalibration2(0, contrastActive);
            } else {
                myGLRenderer.setCalibrationImage(2);
                myGLRenderer.setCharacter(1); //circle with gaps character
                setText("", "");
                setInfo("Goggle calibration");
                eyeCalibration = 0;
                say(getResources().getString(captions.get(ACTION_CALIBRATION1)), false,false);
                say(getResources().getString(captions.get(ACTION_CONTROLLER_CALIBRATION_INFO11)), true,false);
                say(getResources().getString(captions.get(ACTION_CONTROLLER_CALIBRATION_INFO1)), true,false);
                myGLRenderer.setChart(-1, eyeCalibration, "", learn.getOptotypeOuterDiameter(1));
//                myGLRenderer.setCharacter(2);
//                myGLRenderer.setChart(0,eyeCalibration,learn.getChartPosString(0, 0),
//                        learn.getOptotypePixels(0) );
                step1 = true;
                step2 = false;
                test = false;
            }
            isProcessing = false;
        }else if(keyCode==Util.KEY_UP  && (keyAction == KeyEvent.ACTION_UP || fakeControls)
                && (step1==false && step2==false && test==false)){
            //reset app for new user
            if(isDoubleTouche){
                isProcessing = true;
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_KEY, "KEY_UP");
                }
                say("",false,false);
                isAppStarted=false;
//                stopTask(true);
                isProcessing=true;
                newUser=true;
                acuityRepository.newInstallation();
                say(getResources().getString(captions.get(ACTION_RESET_USER)), false,false);
                myGLRenderer.setChart(-1, -2, "", 0);
                resetPreferences(newUser);
                step1=false;
                step2=false;
                test=false;
                handler.postDelayed(runnableCode, 2*LONG_DELAY);//
                learn.clearResult();
                isProcessing=false;
                isDoubleTouche=false;
            }
        }else if(keyCode==Util.KEY_BACK  && (keyAction == KeyEvent.ACTION_UP || fakeControls)
                && (step1==false && step2==false && test==false)){
                isProcessing = true;
                endOfTest(false);
                isProcessing = false;
        }else if(keyCode==Util.KEY_BACK && (step1==true || step2==true)
                && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            //end of test in calibration phases
            isProcessing=true;
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_BACK");
            }
            isAppStarted=false;
//            stopTask(true);
            say("test stopped", false,false);
            endOfTest(false);
            isProcessing=false;
        }else if(keyCode==Util.KEY_BACK && (test==true)
                && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            //user go next level test
            isProcessing=true;
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_BACK");
            }
//            isProcessing=true;
            isAppStarted=false;
//            stopTask(true);
            say("test stopped", false,false);
            nextChart(NEXT_CHART_FOR_MANUAL_STOP, contrastActive);
            isProcessing=false;
//            endOfTest(false);
        }else if(step1 ){
            if(isDoubleTouche){
                //blind eye setup
                if(keyCode==Util.KEY_UP && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
                    if (Util.DEBUG) {
                        Log.i(Util.LOG_TAG_KEY, "calibration1 isDoubleTouche KEY_UP");
                    }
                    isProcessing=true;
                    //memorize eye to put no testing
                    if(eyeCalibration==1 && Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==0){
                        Util.upDatePref(this, Util.PREF_BLIND_EYE, 2);
                    }else {
                        Util.upDatePref(this, Util.PREF_BLIND_EYE, eyeCalibration);
                    }
                    if (Util.DEBUG) {
                        Log.i(Util.LOG_TAG_KEY, "calibration1 isDoubleTouche KEY_UP blind= "+
                                Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1));
                    }
                    isProcessing=false;
                    isDoubleTouche=false;
                }
            }else{
                //start calibration 1
                isProcessing=true;
                say("",false,false);
//            isProcessing=true;
                greyE_left=GREY_ORG_E;
                greyE_right=GREY_ORG_E;
                greyE=GREY_ORG_E;
                greySquare_left=GREY_ORG_SQUARE;
                greySquare_right=GREY_ORG_SQUARE;
                greySquare=GREY_ORG_SQUARE;
                pixelNbr_left=1;
                pixelNbr_right=1;
                pixelNbr=1;
                calibration1(keyCode, keyAction);
                isProcessing=false;
            }
        }else if(step2){
            //calibration2 and start test
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "calibration2");
            }
            isProcessing=true;
            if(isReady){
                calibration2(keyCode, keyAction,contrastActive);
            }
            isProcessing=false;
        }else if(test){
            isProcessing=true;
            if(isReady){
                test(keyCode, keyAction);
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
            Util.upDatePref(this,Util.PREF_LEFT0_START,100);
            Util.upDatePref(this,Util.PREF_RIGHT0_START,100);
            Util.upDatePref(this,Util.PREF_LEFT1_START,100);
            Util.upDatePref(this,Util.PREF_RIGHT1_START,100);
            Util.upDatePref(this,Util.PREF_LEFT2_START,100);
            Util.upDatePref(this,Util.PREF_RIGHT2_START,100);
            Util.upDatePref(this,Util.PREF_USER_ID,-1);
            Util.upDatePref(this, Util.PREF_BLIND_EYE,-1);
            Util.postData(context, acuityRepository, imei,null);
        }
        noContrastLeftResult="";
        contrastLeftResult="";
        contrast_1LeftResult="";
        noContrastRightResult="";
        contrastRightResult="";
        contrast_1RightResult="";
        //learn.clearResult();
    }

    /**
     * calibration of position of circle
     * @param keyCode key code from controller
     * @param keyAction key code from controller
     * @return
     */
    private void calibration1(int keyCode, int keyAction){
        if(keyCode==Util.KEY_TRIGGER && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_TRIGGER");
            }
            if(eyeCalibration==0){
                Util.upDatePref(this,Util.PREF_LEFT_CALIBRATION_X,myGLRenderer.getLeftPositionX());
                Util.upDatePref(this,Util.PREF_LEFT_CALIBRATION_Y,myGLRenderer.getLeftPositionY());
                eyeCalibration=1;
                myGLRenderer.setChart(-1, eyeCalibration, "", learn.getOptotypePixels(1));
                say(getResources().getString(captions.get(ACTION_CONTROLLER_CALIBRATION_INFO21)), false,false);
            }else{
                step1=false;
                step2=true;
                test=false;
                contrastActive=0;
                Util.upDatePref(this,Util.PREF_RIGHT_CALIBRATION_X,myGLRenderer.getRightPositionX());
                Util.upDatePref(this,Util.PREF_RIGHT_CALIBRATION_Y,myGLRenderer.getRightPositionY());
                startCalibration2(0, contrastActive);
//                say("left eye", true,false);
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
        }else if(keyCode==Util.KEY_BACK  && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_BACK");
            }
            endOfTest(false);
        }
    }

    /**
     * setup calibration2 for different contrast and eye
     * @param eye to do calibration
     * @param contrastActive contrast to setup
     * @return
     */
    private void startCalibration2(int eye, int contrastActive) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_KEY, "startCalibration2 eyeCalibration= "+eye+
                   " blind eye= "+ Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)+
                    " contrastActive= "+ contrastActive);
        }
        if(Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==0){
            eyeCalibration=1;
        }else if(Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==2){
            endOfTest(false);
            return;
        }else if(eye==1 && Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==1){
            endOfTest(true);
            return;
        }else{
            eyeCalibration=eye;
        }
//        isNoContrast=true;
//        isContrast=false;
//        isContrast_1=false;
        if(contrastActive ==0){
            greyE = BLACK;//(int) learn.getOptotypeEgrey(chart);
            greySquare = WHITE; //(int) learn.getOptotypeSquaregrey(chart);

        }else if(contrastActive ==1){
            greyE = GREY_E;
            greySquare = GREY_B;
        }else if(contrastActive ==2){
            greyE = (int)learn.getOptotypeEgrey(0);
            greySquare = (int)learn.getOptotypeSquaregrey(0);;
        }

        myGLRenderer.setGrey(eyeCalibration,greyE,greySquare);
        myGLRenderer.setChart(-1, -2, "", 0);
        step1=false;
        step2=true;
        test=false;
        setInfo("Chart calibration");
        if(contrastActive ==0){
            if(eyeCalibration==0){
                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO)), false,false);
                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO1)), true,false);
                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO12)), true,false);
            }else if(eyeCalibration==1 && Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==0) {
                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO)), false,false);
                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO11)), true,false);
                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO12)), true,false);
            }else{
                say(getResources().getString(captions.get(ACTION_CONTROLLER_CALIBRATION_INFO21)), false,false);
                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO)), true,false);
            }
        }

        myGLRenderer.setCalibrationImage(1);
        learn.clearResult();
        chart=0;
        myGLRenderer.setCharacter(2);
        chartPos=0;
        totalLengthStringArray = learn.getSizeChartsPos(chart);
        myGLRenderer.setChart(chart,eyeCalibration,learn.getChartPosString(chart, chartPos),
                learn.getOptotypePixels(chart) );
    }

    /**
     * calibration of sizes of chart to use
     * @param keyCode key code from controller
     * @param keyAction key code from controller
     * @param contrastActive can be 0 - no contrast, 1 - contrast 5%, 2 - last test
     * @return
     */
    private void calibration2(int keyCode, int keyAction, int contrastActive){
        say("",false,false);
        if(keyCode==Util.KEY_TRIGGER && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "calibration2 KEY_TRIGGER chart= "+chart+
                        " eyeCalibration= "+ eyeCalibration);
            }
            //stop calibration go to test
            good=0;
            err=0;
            learn.clearResult();
            say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO5)), true, false);
            if (contrastActive == 0) {
                if(eyeCalibration==0){
                    if(Util.getSharedPreferences(this).getInt(Util.PREF_LEFT0_START, 100)==100){
                        Util.upDatePref(this,Util.PREF_LEFT0_START,0);
                    }
                }else if(eyeCalibration==1){
                    if(Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START, 100)==100){
                        Util.upDatePref(this,Util.PREF_RIGHT0_START,0);
                    }
                }
            }else if (contrastActive == 1) {
                if(eyeCalibration==0){
                    if(Util.getSharedPreferences(this).getInt(Util.PREF_LEFT1_START, 100)==100){
                        Util.upDatePref(this,Util.PREF_LEFT1_START,0);
                    }
                }else if(eyeCalibration==1){
                    if(Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT1_START, 100)==100){
                        Util.upDatePref(this,Util.PREF_RIGHT1_START,0);
                    }
                }
            }else if (contrastActive == 2) {
                if(eyeCalibration==0){
                    if(Util.getSharedPreferences(this).getInt(Util.PREF_LEFT2_START, 100)==100){
                        Util.upDatePref(this,Util.PREF_LEFT2_START,0);
                    }
                }else if(eyeCalibration==1){
                    if(Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT2_START, 100)==100){
                        Util.upDatePref(this,Util.PREF_RIGHT2_START,0);
                    }
                }
            }

            if(chart>1) {
                chart=chart-2;
                goToTest(chart, contrastActive);
            }else{
                goToTest(0, contrastActive);
            }
        }else if(keyCode==Util.KEY_UP && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, " calibration2 KEY_UP chart= "+chart+ " OptotypePixels= "+ learn.getOptotypePixels(chart));
            }
            isReady=false;
            calibrationResultChart("up",contrastActive);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_DOWN && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "calibration2 KEY_DOWN chart= "+chart + " OptotypePixels= "+ learn.getOptotypePixels(chart));
            }
            isReady=false;
            calibrationResultChart("down", contrastActive);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_LEFT  && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_LEFT");
            }
            isReady=false;
            calibrationResultChart("left", contrastActive);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_RIGHT && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_RIGHT");
            }
            isReady=false;
            calibrationResultChart("right", contrastActive);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_BACK  && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
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
            //start next test
            //TODO:: for test 2%
            nextChart(NEXT_CHART_FOR_GOOD, contrastActive);
//            nextChart(NEXT_CHART_FOR_ERROR);
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
        }
    }
    /**
     * new chart sizes or stop test
     * @param
     * @param
     * @param nextChartFor
     * @param contrastActive
     * @return
     */
    private void nextChart(int nextChartFor, int contrastActive) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_KEY, "nextChart nextChartFor= "+nextChartFor+
                    "contrastActive= " +contrastActive+
                    " eye= "+ eye+  " chartPos= "+ chartPos+ " chart= "+chart+
                    " err= "+err +" good= "+good +
                    " blind= "+Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1));
        }
        if (eye == -1 || eye==-2) {
            if(Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==0){
                //start with right eye
                eye = 1;
                chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START, FIRST_CHART_RIGHT_EYE_0);
//                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO2)), true, true);
            }else if(Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==2){
                //blind end of test
                endOfTest(false);
                return;
            }else{
                //start with left or right eye
                if(eye==-1){
                    chart = Util.getSharedPreferences(this).getInt(Util.PREF_LEFT0_START, FIRST_CHART_LEFT_EYE_0);
                    eye = 0;
                }else{
                    chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START, FIRST_CHART_RIGHT_EYE_0);
                    eye = 1;
                }
//                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO1)), true, true);
            }
            chartPos=0;
            totalLengthStringArray = learn.getSizeChartsPos(chart);
            learn.clearResult();
        } else {
            chart++;
            //all charts for the contrast's level was done or error
            if (chart >= totalLengthCharts ||
                    !learn.isResultOk(chart-1, eye) ||
                    nextChartFor==NEXT_CHART_FOR_NEW_LEVEL) {
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_KEY, "nextChart go to next ChartFor= "+nextChartFor+
                            " eye= "+ eye+
                            " chart= "+chart+  " chartPos= "+ chartPos+ " totalLengthCharts= "+totalLengthCharts+
                            " contrastActive= "+ contrastActive);
                }
                if (eye == 0) {
                    if (contrastActive ==0) {
                        //first no contrast test done
                        //memorise no contrast test
                        if(nextChartFor==NEXT_CHART_FOR_MANUAL_STOP){
                            noContrastLeftResult = "-1" ;
                        }else if(nextChartFor==NEXT_CHART_FOR_NO_READING){
                            noContrastLeftResult = "0" ;
                        }else if(nextChartFor==NEXT_CHART_FOR_ERROR || nextChartFor==NEXT_CHART_FOR_GOOD){
                            noContrastLeftResult = learn.getEyeResult(eye);
                        }else if(nextChartFor==NEXT_CHART_FOR_NEW_LEVEL){

                        }else if(nextChartFor==NEXT_CHART_FOR_NEW_TEST){

                        }
                        if(nextChartFor==NEXT_CHART_FOR_NO_READING){
                            //go to next eye
                            eye=1;
                            this.contrastActive = 0;
                            chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START, FIRST_CHART_RIGHT_EYE_0);
                            say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO2)), true, false);
                        }else{
                            //go to next contrast
                            eye=0;
                            this.contrastActive = 1;
                            chart = Util.getSharedPreferences(this).getInt(Util.PREF_LEFT1_START, FIRST_CHART_LEFT_EYE_1);
                            say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO3)), true, false);
                        }
                        startCalibration2(eye, this.contrastActive);
                        return;
//                        totalLengthStringArray = learn.getSizeChartsPos(chart);
//                        learn.clearResult();
                    }else if (contrastActive ==1){
                        //contrast test done
                        //memorise contrast test
                        if(nextChartFor==NEXT_CHART_FOR_MANUAL_STOP){
                            contrastLeftResult = "-1" ;
                        }else if(nextChartFor==NEXT_CHART_FOR_NO_READING){
                            contrastLeftResult = "0" ;
                        }else if(nextChartFor==NEXT_CHART_FOR_ERROR || nextChartFor==NEXT_CHART_FOR_GOOD){
                            contrastLeftResult = learn.getEyeResult(eye);
                        }else if(nextChartFor==NEXT_CHART_FOR_NEW_LEVEL){

                        }else if(nextChartFor==NEXT_CHART_FOR_NEW_TEST){

                        }
                        if(nextChartFor==NEXT_CHART_FOR_GOOD){
                            //go to next contrast level
                            chart = Util.getSharedPreferences(this).getInt(Util.PREF_LEFT2_START, FIRST_CHART_LEFT_EYE_2);
                            say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO4)), true, false);
                            eye=0;
                            this.contrastActive = 2;
                        }else{
                            if(Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==1){
                                endOfTest(true);
                                return;
                            }else{
                                //go to next eye
//                                chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START, FIRST_CHART_RIGHT_EYE_0);
//                                eye=1;
//                                this.contrastActive = 0;
////                                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO2)), true, false);
                                //go to next contrast level
                                chart = Util.getSharedPreferences(this).getInt(Util.PREF_LEFT2_START, FIRST_CHART_LEFT_EYE_2);
                                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO4)), true, false);
                                eye=0;
                                this.contrastActive = 2;
                            }
                        }
                        startCalibration2(eye, this.contrastActive);
                        return;
//                        totalLengthStringArray = learn.getSizeChartsPos(chart);
//                        learn.clearResult();
                    }else if (contrastActive ==2){
                        //contrast_1 test done
                        //memorise contrast_1 test
                        if(nextChartFor==NEXT_CHART_FOR_MANUAL_STOP){
                            contrast_1LeftResult = "-1" ;
                        }else if(nextChartFor==NEXT_CHART_FOR_NO_READING){
                            contrast_1LeftResult = "0" ;
                        }else if(nextChartFor==NEXT_CHART_FOR_ERROR || nextChartFor==NEXT_CHART_FOR_GOOD){
                            contrast_1LeftResult = learn.getEyeContrastResult(eye);
                        }else if(nextChartFor==NEXT_CHART_FOR_NEW_LEVEL){

                        }else if(nextChartFor==NEXT_CHART_FOR_NEW_TEST){

                        }
                        if(Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==1){
                            endOfTest(true);
                            return;
                        }else{
                            //go to next eye
                            chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START, FIRST_CHART_RIGHT_EYE_0);
                            eye=1;
                            this.contrastActive = 0;
                            say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO2)), true, false);
                            totalLengthStringArray = learn.getSizeChartsPos(chart);
                            learn.clearResult();
                            startCalibration2(eye, this.contrastActive);
                            return;
                        }
                    }
                }else if (eye == 1) {
                    if (contrastActive ==0) {
                        if(nextChartFor==NEXT_CHART_FOR_MANUAL_STOP){
                            noContrastRightResult = "-1" ;
                        }else if(nextChartFor==NEXT_CHART_FOR_NO_READING){
                            noContrastRightResult = "0" ;
                        }else if(nextChartFor==NEXT_CHART_FOR_ERROR || nextChartFor==NEXT_CHART_FOR_GOOD){
                            noContrastRightResult = learn.getEyeResult(eye);
                        }else if(nextChartFor==NEXT_CHART_FOR_NEW_LEVEL){

                        }else if(nextChartFor==NEXT_CHART_FOR_NEW_TEST){

                        }
                            //go to next contrast level
                        if(nextChartFor==NEXT_CHART_FOR_NO_READING){
                            learn.clearResult();
                            this.contrastActive = -1;
                            endOfTest(true);
                            return;
                        }else{
                            eye=1;
                            this.contrastActive = 1;
                            chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT1_START, FIRST_CHART_RIGHT_EYE_1);;
                            say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO3)), true, false);
                            startCalibration2(eye, this.contrastActive);
                            return;
                        }
//                        totalLengthStringArray = learn.getSizeChartsPos(chart);
//                        learn.clearResult();
                    }else if (contrastActive ==1){
                        if(nextChartFor==NEXT_CHART_FOR_MANUAL_STOP){
                            contrastRightResult = "-1" ;
                        }else if(nextChartFor==NEXT_CHART_FOR_NO_READING){
                            contrastRightResult = "0" ;
                        }else if(nextChartFor==NEXT_CHART_FOR_ERROR || nextChartFor==NEXT_CHART_FOR_GOOD){
                            contrastRightResult = learn.getEyeResult(eye);
                        }else if(nextChartFor==NEXT_CHART_FOR_NEW_LEVEL){

                        }else if(nextChartFor==NEXT_CHART_FOR_NEW_TEST){

                        }
                        if(nextChartFor==NEXT_CHART_FOR_GOOD){
                            //go to next contrast level
                            eye=1;
                            this.contrastActive = 2;
                            chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT2_START, FIRST_CHART_RIGHT_EYE_2);
                            say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO4)), true, false);
                            startCalibration2(eye, this.contrastActive);
                            return;
                        }else{
//                            learn.clearResult();
//                            this.contrastActive = -1;
//                            endOfTest(true);
                            chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT2_START, FIRST_CHART_RIGHT_EYE_2);
                            say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO4)), true, false);
                            eye=1;
                            this.contrastActive = 2;
                            return;
                        }
                    }else if (contrastActive ==2){
                        if(nextChartFor==NEXT_CHART_FOR_MANUAL_STOP){
                            contrast_1RightResult = "-1" ;
                        }else if(nextChartFor==NEXT_CHART_FOR_NO_READING){
                            contrast_1RightResult = "0" ;
                        }else if(nextChartFor==NEXT_CHART_FOR_ERROR || nextChartFor==NEXT_CHART_FOR_GOOD){
                            contrast_1RightResult = learn.getEyeContrastResult(eye);
                        }else if(nextChartFor==NEXT_CHART_FOR_NEW_LEVEL){

                        }else if(nextChartFor==NEXT_CHART_FOR_NEW_TEST){

                        }

//                        learn.clearResult();
                        this.contrastActive = -1;
                        endOfTest(true);
                        return;
                    }
                }
            }
        }

        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_KEY, "nextChart contrastActive= " + this.contrastActive);
        }
        setInfo("test started");
        chartPos=0;
        good=0;
        err=0;
        if (this.contrastActive ==0) {
            greyE = BLACK;//(int) learn.getOptotypeEgrey(chart);
            greySquare = WHITE; //(int) learn.getOptotypeSquaregrey(chart);
        } else if (this.contrastActive ==1) {
            greyE = GREY_E;
            greySquare = GREY_B;
        } else if (this.contrastActive ==2){
            greyE = (int)learn.getOptotypeEgrey(chart);
            greySquare = (int)learn.getOptotypeSquaregrey(chart);;
        }
        setText("","");
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_KEY, "nextChart myGLRenderer eye= " + eye
                    +" chart=" + chart +" chartPos=" + chartPos+ " this.contrastActive= "+this.contrastActive);
        }
        breakBetween(eye);
        myGLRenderer.setGrey(eye,greyE,greySquare);
        if (this.contrastActive ==2){
            myGLRenderer.setChart(chart, eye, learn.getChartPosString(chart, chartPos), learn.getOptotypePixels(0));
        }else{
            myGLRenderer.setChart(chart, eye, learn.getChartPosString(chart, chartPos), learn.getOptotypePixels(chart));
        }

//        if(test){
//             restartTask(LONG_DELAY);
//        }
    }

    /**
     * insert acuity to local database
     * @param contrast (0-nocontrast, 1-contrast 5%, 2-contrast 2.5%)
     * @param startTest time used for finish the test
     * @param leftFirst numbre of pixels in first chart
     * @param rightFirst numbre of pixels in first chart
     * @param leftResult numbre of pixels, 0 no reading, -1 stop manually test, -2 stop manually
     * @param rightResult numbre of pixels, 0 no reading, -1 stop manually test, -2 stop manually
     * @return
     */
    private void insertAcuity(int contrast, long startTest, int leftFirst, int rightFirst,
                              String leftResult, String rightResult) {
        handler0.removeCallbacks(runnableCode0);
        int duration= (int) ((Calendar.getInstance().getTimeInMillis()-startTest)/1000);
        int userId = Util.getSharedPreferences(this).getInt(Util.PREF_USER_ID, -1);
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_KEY, "insertAcuity userId= " + userId+ " duration= "+duration);
        }
        if (userId > -1) {
            acuityRepository.insertAcuity(userId,
                    contrast, duration,
                    String.valueOf((int)learn.getOptotypePixels(leftFirst)),
                    String.valueOf((int)learn.getOptotypePixels(rightFirst)),
                    leftResult, rightResult);
        }
        handler0.postDelayed(runnableCode0, 100);
    }

    /**
     * check if test finish
     * @param
     * @param result string of user's result
     * @param contrastActive
     * @return
     */
    private void calibrationResultChart(String result, int contrastActive) {
//        setText(result,"");
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_KEY, "calibrationResultChart eyeCalibration= "+eyeCalibration+" result= "+result+
                    " chart= "+chart+ " chartPos= " + chartPos+
                    " totalLengthCharts= "+totalLengthCharts+
                    " totalLengthStringArray= "+totalLengthStringArray);
        }
        if(chart>-1 && chart<=totalLengthCharts-1){
//            stopTask(true);
            if(chartPos>-1 && chartPos<=totalLengthStringArray-1){
                //test in the position
                int r=learn.setResult(chart, chartPos, result,eyeCalibration);
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_KEY, "calibrationResultChart response= "+r);
                }

                //result card
                if(r<1){
                    //if error (-1 if !eye(0,1) or 0)
                    err=err+1;
                    toneL.startTone(ToneGenerator.TONE_DTMF_0,200);
                }else{
                    //if good answer
                    good=good+1;
                    toneH.startTone(ToneGenerator.TONE_DTMF_1,200);
                }
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_KEY, "calibrationResultChart response= "+r+
                            " good= "+good+" err= "+ err);
                }
                if(good>=2){//next smaller
                    chart=chart+2;
//                    totalLengthStringArray = learn.getSizeChartsPos(chart);
                    learn.clearResult();
                    chartPos=0;
                    good=0;
                    err=0;
                    if(chart>=totalLengthCharts){
                        if (Util.DEBUG) {
                            Log.i(Util.LOG_TAG_KEY, "calibrationResultChart good, go to test");
                        }
                        chart=chart-2;
                        totalLengthStringArray = learn.getSizeChartsPos(chart);
                        say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO5)), true, false);
                        goToTest(chart, contrastActive);
                        return;
                    }
                }else if(good==1 && err==1){ //repeat same calibration
                    if (Util.DEBUG) {
                        Log.i(Util.LOG_TAG_KEY, "calibrationResultChart wrong and good repeat again one time");
                    }
                }else if(err>=2){ //stop calibration calibration sizes = last good
                    if (Util.DEBUG) {
                        Log.i(Util.LOG_TAG_KEY, "calibrationResultChart wrong, go to test");
                    }
                    good=0;
                    err=0;
                    learn.clearResult();
                    say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO5)), true, false);
                    if(chart>1) {
                        chart=chart-2;
                        goToTest(chart,contrastActive);
                    }else{
                        goToTest(0, contrastActive);
                    }
                    return;
                }else{
                    chartPos++;
                }
                breakBetween(eyeCalibration);
                myGLRenderer.setCalibrationImage(3);
                if(contrastActive==0 || contrastActive==1){
                    myGLRenderer.setGrey(eyeCalibration,greyE,greySquare);
                    myGLRenderer.setChart(chart, eyeCalibration, learn.getChartPosString(chart, chartPos), learn.getOptotypePixels(chart));
                }else{
                    greyE=(int)learn.getOptotypeEgrey(chart);
                    greySquare=(int)learn.getOptotypeSquaregrey(chart);
                    myGLRenderer.setGrey(eyeCalibration,greyE,greySquare);
                    myGLRenderer.setChart(chart, eyeCalibration, learn.getChartPosString(chart, chartPos), learn.getOptotypePixels(0));
                }
            }else{
                if (Util.DEBUG) {
                    Log.e(Util.LOG_TAG_KEY, "calibrationResultChart outside the sizes of chart");
                }
                myGLRenderer.setChart(-1, -2, "", 0);
            }
        }else{
            if (Util.DEBUG) {
                Log.e(Util.LOG_TAG_KEY, "calibrationResultChart outside the chart");
            }
            myGLRenderer.setChart(-1, -2, "", 0);
        }
    }

    private void breakBetween(int e) {
        myGLRenderer.setGrey(e,GREY_ORG_E,GREY_ORG_SQUARE);
        myGLRenderer.setChart(-2, e, "", 0);
        sleep(500);
    }

    /**
     * start test after calibration
     * @param
     * @param chart default chart after calibration
     * @param contrastActive
     * @return
     */
    private void goToTest(int chart, int contrastActive){
        if(eyeCalibration==0 && contrastActive==0){
            if(chart<2){
                Util.upDatePref(this, Util.PREF_LEFT0_START,chart);
            }else{
                Util.upDatePref(this, Util.PREF_LEFT0_START,chart-2);
            }
            eye=-1;
        }if(eyeCalibration==0 && contrastActive==1){
            if(chart<2){
                Util.upDatePref(this, Util.PREF_LEFT1_START,chart);
            }else{
                Util.upDatePref(this, Util.PREF_LEFT1_START,chart-2);
            }
            eye=-1;
        }if(eyeCalibration==0 && contrastActive==2){
            if(chart<2){
                Util.upDatePref(this, Util.PREF_LEFT2_START,chart);
            }else{
                Util.upDatePref(this, Util.PREF_LEFT2_START,chart-2);
            }
            eye=-1;
        }else if(eyeCalibration==1  && contrastActive==0){
            if(chart<2){
                Util.upDatePref(this, Util.PREF_RIGHT0_START,chart);
            }else{
                Util.upDatePref(this, Util.PREF_RIGHT0_START,chart-2);
            }
            eye=-2;
        }else if(eyeCalibration==1  && contrastActive==1){
            if(chart<2){
                Util.upDatePref(this, Util.PREF_RIGHT1_START,chart);
            }else{
                Util.upDatePref(this, Util.PREF_RIGHT1_START,chart-2);
            }
            eye=-2;
        }else if(eyeCalibration==1  && contrastActive==2){
            if(chart<2){
                Util.upDatePref(this, Util.PREF_RIGHT2_START,chart);
            }else{
                Util.upDatePref(this, Util.PREF_RIGHT2_START,chart-2);
            }
            eye=-2;
        }

        step2=false;
        test=true;
        eyeCalibration=-1;
        myGLRenderer.setChart(-1, -2, "", 0);
        setInfo("Test running");
//        say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO)), false,false);
//        myGLRenderer.setCharacter(2);
        newUser=false;
        myGLRenderer.setCalibrationImage(3);
        nextChart(NEXT_CHART_FOR_NEW_TEST,contrastActive);
    }

    /**
     * check if test finish
     * @param result string of user's result
     * @param
     * @return
     */
    private void resultChart(String result) {
//        setText(result,"");
        if(chart>-1 && chart<=totalLengthCharts-1){
//            stopTask(true);
            if(chartPos>-1 && chartPos<=totalLengthStringArray-1){
                //test in the position
//                int r=learn.setResult(chart, chartPos, result,eye);
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_KEY, "resultChart result= "+result+" chart= "+chart+
                            " chartPos= "+chartPos+" totalLengthStringArray= "+totalLengthStringArray);
                }
                //result card
                if(learn.setResult(chart, chartPos, result,eye)<1){
                    //if error (-1 if !eye(0,1) or 0)
                    err=err+1;
                    toneL.startTone(ToneGenerator.TONE_DTMF_0,200);
                }else{
                    //if good answer
                    good=good+1;
                    toneH.startTone(ToneGenerator.TONE_DTMF_1,200);
                }
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_KEY, "resultChart good= "+good+" err= "+
                            err+" result= "+result+" chart= "+chart+
                            " chartPos= "+chartPos+" totalLengthStringArray= "+totalLengthStringArray);
                }
                if(err>=2 || good>=4){
                    if (Util.DEBUG) {
                        Log.i(Util.LOG_TAG_KEY, "resultChart if 1");
                    }
//                    myGLRenderer.setChart(chart, eye, "", 0);
//                    sleep(1000);
                    if(err>=2) {
                        nextChart(NEXT_CHART_FOR_ERROR, contrastActive);
                    }else{
                        nextChart(NEXT_CHART_FOR_GOOD, contrastActive);
                    }
                    return;
                }else if(err==1 && good==3){
                    //repeat same level
                    chartPos=learn.getPosWrongResult(eye,chart);
                    if (Util.DEBUG) {
                        Log.i(Util.LOG_TAG_KEY, "resultChart if 2 repeat chartPos="+chartPos);
                    }
                    breakBetween(eye);
                    myGLRenderer.setGrey(eye,greyE,greySquare);
                    if(contrastActive==2){
                        myGLRenderer.setChart(chart, eye, learn.getChartPosString(chart, chartPos), learn.getOptotypePixels(0));
                    }else{
                        myGLRenderer.setChart(chart, eye, learn.getChartPosString(chart, chartPos), learn.getOptotypePixels(chart));
                    }
                }else{
                    //go to next level
                    if (Util.DEBUG) {
                        Log.i(Util.LOG_TAG_KEY, "resultChart if 3");
                    }
                    chartPos++;
                    //myGLRenderer.setChart(chart, eye, "", 0);
//                    sleep(1000);
                    //end of card in this sizes
                    if(chartPos>totalLengthStringArray-1){
                        if (Util.DEBUG) {
                            Log.i(Util.LOG_TAG_KEY, "resultChart NEXT_CHART_FOR_NEW_LEVEL chartPos= "+chartPos);
                        }
                        nextChart(NEXT_CHART_FOR_NEW_LEVEL, contrastActive);
                        return;
                    }else{
                        //chartPos was -2 before this procedure
                        if (Util.DEBUG) {
                            Log.i(Util.LOG_TAG_KEY, "resultChart myGLRenderer chartPos= "+chartPos);
                        }
                        if(chartPos<0){
                            //no test
                            myGLRenderer.setChart(-1, eye, "", learn.getOptotypePixels(0));
                        }else{
                            //test
                            breakBetween(eye);
                            myGLRenderer.setGrey(eye,greyE,greySquare);
                            if(contrastActive==2){
                                myGLRenderer.setChart(chart, eye, learn.getChartPosString(chart, chartPos), learn.getOptotypePixels(0));
                            }else {
                                myGLRenderer.setChart(chart, eye, learn.getChartPosString(chart, chartPos), learn.getOptotypePixels(chart));
                            }
                        }
                    }
                }
            }else{
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_KEY, "resultChart NEXT_CHART_FOR_NEW_LEVEL 2");
                }
                //go next level the chart or stop
                myGLRenderer.setChart(-1, -2, "", 0);
//                sleep(1000);
                nextChart(NEXT_CHART_FOR_NEW_LEVEL, contrastActive);
                return;
            }
//            if(test){
//                restartTask(LONG_DELAY);
//            }
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
        contrastActive=-1;
        myGLRenderer.setChart(-1, -2, "", 0);
        setInfo("end of test");
        say("end of test",false,false);
        if (Util.DEBUG) {
            Log.d(LOG_TAG_MAIN, "endOfTest test= "+test+" ok= "+ok+" eye= "+eye);
            Log.d(LOG_TAG_MAIN, "endOfTest left:"
                    + " noContrastLeftStart= "+ Util.getSharedPreferences(this).getInt(Util.PREF_LEFT0_START, FIRST_CHART_LEFT_EYE_0)
                    +" noContrastLeftResult= "+ noContrastLeftResult
                    + " contrastLeftStart= "+ Util.getSharedPreferences(this).getInt(Util.PREF_LEFT1_START, FIRST_CHART_LEFT_EYE_1)
                    + " contrastLeftResult= "+ contrastLeftResult
                    + " contrast_1LeftStart= "+ Util.getSharedPreferences(this).getInt(Util.PREF_LEFT2_START, FIRST_CHART_LEFT_EYE_2)
                    + " contrast_1LeftResult= "+ contrast_1LeftResult);
            Log.d(LOG_TAG_MAIN, "endOfTest right:"
                    + " noContrastRightStart= "+ Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START, FIRST_CHART_RIGHT_EYE_0)
                    +" noContrastRightResult= "+ noContrastRightResult
                    + " contrastRightStart= "+ Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT1_START, FIRST_CHART_RIGHT_EYE_1)
                    + " contrastRightResult= "+ contrastRightResult
                    + " contrast_1RightStart= "+ Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT2_START, FIRST_CHART_RIGHT_EYE_2)
                    + " contrast_1RightResult= "+ contrast_1RightResult);
        }
        if(test && ok){
            //insert in database no contrast tests
            if(noContrastLeftResult!="" || noContrastRightResult!=""){
                insertAcuity(0, startTest,
                        Util.getSharedPreferences(this).getInt(Util.PREF_LEFT0_START, FIRST_CHART_LEFT_EYE_0),
                        Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START, FIRST_CHART_RIGHT_EYE_0),
                        noContrastLeftResult, noContrastRightResult);
            }
            //insert in database contrasts tests
            if(contrastLeftResult!="" || contrastRightResult!=""){
                insertAcuity(1, startTest,
                        Util.getSharedPreferences(this).getInt(Util.PREF_LEFT1_START, FIRST_CHART_LEFT_EYE_1),
                        Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT1_START, FIRST_CHART_RIGHT_EYE_1),
                        contrastLeftResult, contrastRightResult);
            }
            if(contrast_1LeftResult!="" || contrast_1RightResult!=""){
                insertAcuity(2, startTest,
                        Util.getSharedPreferences(this).getInt(Util.PREF_LEFT2_START, FIRST_CHART_LEFT_EYE_2),
                        Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT2_START, FIRST_CHART_RIGHT_EYE_2),
                        contrast_1LeftResult, contrast_1RightResult);
            }

            if(noContrastLeftResult=="" || noContrastLeftResult=="0" || noContrastLeftResult=="-1" || noContrastLeftResult=="-2" ){
//                noContrastLeftResult="no reading";
            }else{
                noContrastLeftResult=learn.getPixelsFromMunit(noContrastLeftResult);
            }
            if(noContrastRightResult==""  || noContrastRightResult=="0" || noContrastRightResult=="-1" || noContrastRightResult=="-2" ){
//                noContrastRightResult="no reading";
            }else{
                noContrastRightResult=learn.getPixelsFromMunit(noContrastRightResult);
            }
            if(contrastLeftResult=="" || contrastLeftResult=="0" || contrastLeftResult=="-1" || contrastLeftResult=="-2"){
//                contrastLeftResult="no reading";
            }else{
                contrastLeftResult=learn.getPixelsFromMunit(contrastLeftResult);
            }
            if(contrastRightResult=="" || contrastRightResult=="0" || contrastRightResult=="-1" || contrastRightResult=="-2"){
//                contrastRightResult="no reading";
            }else{
                contrastRightResult=learn.getPixelsFromMunit(contrastRightResult);
            }
            if(contrast_1LeftResult=="" || contrast_1LeftResult=="0" || contrast_1LeftResult=="-1" || contrast_1LeftResult=="-2"){
//                contrastLeftResult="no reading";
            }else{
//                contrast_1LeftResult=learn.getEyeContrastResult(0);
            }
            if(contrast_1RightResult=="" || contrast_1RightResult=="0" || contrast_1RightResult=="-1" || contrast_1RightResult=="-2"){
//                contrastRightResult="no reading";
            }else{
//                contrast_1RightResult=learn.getEyeContrastResult(1);
            }

            setText("left eye: "+noContrastLeftResult +" / " +contrastLeftResult+" / " +contrast_1LeftResult,
                    "right eye: "+noContrastRightResult +" / " +contrastRightResult+" / " +contrast_1RightResult);
            say(getResources().getString(captions.get(ACTION_RESULT_LEFT))+" "+noContrastLeftResult +
                    ", " +contrastLeftResult+", " +contrast_1LeftResult, true,false);
            say(getResources().getString(captions.get(ACTION_RESULT_RIGHT))+" "+noContrastRightResult +
                    ", " +contrastRightResult +", " +contrast_1RightResult, true,false);
            AsyncTask.execute( new Runnable() {
                @Override
                public void run() {
                    bestAcuities0=acuityRepository.getBestAcuity(0);
                }
            });
            while(bestAcuities0==null){

            }
            if(bestAcuities0[0]==-1 && bestAcuities0[1]==-1){
                toRepeatTest=false;
            }else{
                if(Math.abs(bestAcuities0[0]-Float.parseFloat(noContrastLeftResult))>4 ||
                        Math.abs(bestAcuities0[1]-Float.parseFloat(noContrastRightResult))>4){
                    toRepeatTest=true;
                    say(getResources().getString(captions.get(ACTION_REPEAT_TEST)), true,true);
                }else{
                    toRepeatTest=false;
                }
            }

//            noContrastLeftResult="";
//            contrastLeftResult="";
//            noContrastRightResult="";
//            contrastRightResult="";
        }else{
            setText("","");
            setInfo("Preparing the test");
            if (Util.DEBUG) {
                Log.d(LOG_TAG_MAIN, "test end test= "+test+" ok= "+ok
                        +" left:  noContrastLeftResult= "+ noContrastLeftResult+ " contrastLeftResult= "+ contrastLeftResult
                        + " contrast1LeftResult= "+ contrast_1LeftResult
                        +" right:  noContrastRightResult= "+ noContrastRightResult+ " contrastRightResult= "+ contrastRightResult
                        + " contrast1RightResult= "+ contrast_1RightResult);}
            //insert in database contrast tests
            if(contrastActive==0){
                noContrastLeftResult="-2";
                noContrastRightResult="-2";
                handler0.removeCallbacks(runnableCode0);
                insertAcuity(0, startTest,
                        Util.getSharedPreferences(context).getInt(Util.PREF_LEFT0_START, FIRST_CHART_LEFT_EYE_0),
                        Util.getSharedPreferences(context).getInt(Util.PREF_RIGHT0_START, FIRST_CHART_RIGHT_EYE_0),
                        noContrastLeftResult, noContrastRightResult);
            }else if(contrastActive==1){
                contrastLeftResult="-2";
                contrastRightResult="-2";
                handler0.removeCallbacks(runnableCode0);
                insertAcuity(0, startTest,
                        Util.getSharedPreferences(context).getInt(Util.PREF_LEFT1_START, FIRST_CHART_LEFT_EYE_1),
                        Util.getSharedPreferences(context).getInt(Util.PREF_RIGHT1_START, FIRST_CHART_RIGHT_EYE_1),
                        contrastLeftResult, contrastRightResult);
            }else if(contrastActive==2){
                contrast_1LeftResult="-2";
                contrast_1RightResult="-2";
                handler0.removeCallbacks(runnableCode0);
                insertAcuity(0, startTest,
                        Util.getSharedPreferences(context).getInt(Util.PREF_LEFT2_START, FIRST_CHART_LEFT_EYE_2),
                        Util.getSharedPreferences(context).getInt(Util.PREF_RIGHT2_START, FIRST_CHART_RIGHT_EYE_2),
                        contrast_1LeftResult, contrast_1RightResult);
            }
            handler0.postDelayed(runnableCode0, 100);
        }
        chart=-1;
        eye=-2;
        if (Util.DEBUG) {
            Log.d(LOG_TAG_MAIN, "endOfTest startedByPackage="+ startedByPackage);}

        if(startedByPackage!=null){
            isProcessing=true;
            say("we will start SightPlus now",true,true);
//                sendBroadcastToActivity(BROADCAST_START_APP_ACTION, START_APP_RESULT, 1);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startExternalApp(startedByPackage);
                }
            },SHORT_DELAY);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                isProcessing=false;
                    finish();
                    System.exit(-1);
                }
            },LONG_DELAY);
            isAppStarted=false;
            step2=false;
            step1=false;
            test=false;
            contrastActive=-1;
            return;
        }
        isAppStarted=false;
        step2=false;
        step1=false;
        test=false;
        contrastActive=-1;
    }

//    private void insertAcuity(int nextChartFor, int i, String contrastLeftResult, String contrastRightResult) {
//        AsyncTask.execute( new Runnable() {
//            @Override
//            public void run() {
//                if (Util.DEBUG) {
//                    Log.d(LOG_TAG_MAIN, "Result noContrastLeftResult= " + noContrastLeftResult
//                            + " contrastLeftResult= " + MainActivity.this.contrastLeftResult);
//                    Log.d(LOG_TAG_MAIN, "Result noContrastRightResult= " + noContrastRightResult
//                            + " contrastRightResult= " + MainActivity.this.contrastRightResult);
//                }
//                handler0.removeCallbacks(runnableCode0);
//                int userId=Util.getSharedPreferences(context).getInt(Util.PREF_USER_ID,-1);
//                if(userId>-1){
//                    acuityRepository.insertAcuity(userId, 0,noContrastLeftResult,noContrastRightResult);
//                    acuityRepository.insertAcuity(userId, 1, MainActivity.this.contrastLeftResult, MainActivity.this.contrastRightResult);
//                    acuityRepository.insertAcuity(userId, 2,contrast_1LeftResult,contrast_1RightResult);
//                }
//                handler0.postDelayed(runnableCode0, SHORT_DELAY);
//            }
//        });
//    }

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

        if(toRepeatTest){
            sendBroadcastToActivity(BROADCAST_START_APP_ACTION, START_APP_RESULT, 3);
        }else{
            sendBroadcastToActivity(BROADCAST_START_APP_ACTION, START_APP_RESULT, 2);
        }




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

