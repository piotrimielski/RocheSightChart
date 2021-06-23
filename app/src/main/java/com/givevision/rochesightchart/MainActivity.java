package com.givevision.rochesightchart;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
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
import android.os.Looper;
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
import com.givevision.rochesightchart.bluetooth.BluetoothChecker;
import com.givevision.rochesightchart.bluetooth.BluetoothEvent;
import com.givevision.rochesightchart.db.Acuity;
import com.givevision.rochesightchart.db.AcuityRepository;

import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import static android.os.SystemClock.sleep;
import static com.givevision.rochesightchart.Util.LOG_TAG_KEY;
import static com.givevision.rochesightchart.Util.LOG_TAG_MAIN;
import static com.givevision.rochesightchart.Util.TAG;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

//https://cmusphinx.github.io/wiki/tutorialandroid/

public class MainActivity extends Activity {
    //5% contrast – this is with background 186 and foreground 178 pixel intensities
    private static final int GREY_E = 178;
    private static final int GREY_B = 186;
    //2.5% contrast, with background 186 and foreground 182
    private static final int GREY_E_1 = 182;
    private static final int GREY_B_1 = 186;
    private static final int WHITE = 255;
    private static final int BLACK = 0;
    private static final double VOLUME= 0.7;
    private static final int LIMIT_GREY=7; //5% gray for contrast test
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
    private static final String ACTION_CONTROLLER_CALIBRATION_INFO21 = "controller calibration info21";
    private static final String ACTION_CONTROLLER_TEST_INFO1 = "controller test info1";
//    private static final String ACTION_CONTROLLER_TEST_INFO11 = "controller test info11";
    private static final String ACTION_CONTROLLER_TEST_INFO12 = "controller test info12";
    private static final String ACTION_CONTROLLER_TEST_INFO13 = "controller test info13";
//    private static final String ACTION_CONTROLLER_TEST_INFO2 = "controller test info2";
    private static final String ACTION_CONTROLLER_TEST_INFO3 = "controller test info3";
    private static final String ACTION_CONTROLLER_TEST_INFO4 = "controller test info4";
    private static final String ACTION_CONTROLLER_TEST_INFO5 = "controller test info5";
    private static final String ACTION_EXIT_TEST = "exit test";
    private static final String ACTION_EXIT_TEST1 = "exit test1";
    private static final String  ACTION_EXIT_START_APP="start app in exit";
    private static final String ACTION_END_TEST = "end test";
    private static final String ACTION_LAST_TEST = "last test";

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
    private boolean end=false;
    private boolean testDone;
    private boolean isDoubleTouche;
    private RelativeLayout   relativeLayout  ;
    private boolean isReadyForSpeech;
    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 3;
    private static final int LONG_DELAY = 10000;
    private static final int SHORT_DELAY = 5000;
    private static final int KEY_DELAY = 200;
    private SpeechRecognizer recognizer;
//    private HashMap<String, Integer> captions;

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
    private static float LEFT_EYE_POS_X= (float) 560;//555.0;
    private static float LEFT_EYE_POS_Y=(float) 540.0;
    private static float RIGHT_EYE_POS_X= (float) 1600;//1665.0;
    private static float RIGHT_EYE_POS_Y=(float) 540.0;
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
    private int logRes;
    private int contrastLeftLogCal;
    private int contrastRightLogCal;
    private int contrast_1LeftLogCal;
    private int contrast_1RightLogCal;
    private int noContrastLeftLogCal;
    private int noContrastRightLogCal;
    private int contrastLeftLogTest;
    private int contrastRightLogTest;
    private int contrast_1LeftLogTest;
    private int contrast_1RightLogTest;
    private int noContrastLeftLogTest;
    private int noContrastRightLogTest;

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
    private int eyeCalibration;
    private boolean isJoystick;
    private boolean isNegCalDone;
    private boolean isNegTestDone;
    private ToneGenerator toneH = new ToneGenerator(AudioManager.STREAM_DTMF, 100);
    private ToneGenerator toneL = new ToneGenerator(AudioManager.STREAM_DTMF, 80);
    private ToneGenerator toneF = new ToneGenerator(AudioManager.STREAM_ALARM, 80);
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
    private Handler handler = new Handler(Looper.getMainLooper());
    private Handler handler0 = new Handler(Looper.getMainLooper());
    private Handler handler1 = new Handler(Looper.getMainLooper());
    private Handler handler2 = new Handler(Looper.getMainLooper());

    private AcuityRepository acuityRepository;
    private RequestQueue requestQueue;
    private String imei;
    private boolean isEndOfTest;
    private boolean fakeControls=false;
    private boolean isBackTouch=false;
    private AudioManager audioManager;
    private int volume=0;
    private int volumeOrig=0;

    private BroadcastReceiver mBatteryReceiver;
    private Battery batteryController;
    public BluetoothAdapter mBluetoothAdapter;

    //   test reminder
    private Runnable runnableCode1 = new Runnable() {
        @Override
        public void run() {
            if(!mTTS.isSpeaking() && isTimerStart){
                say(getResources().getString(R.string.tempo30sec), false,false);
                handler2.removeCallbacks(runnableCode2);
                handler2.postDelayed(runnableCode2, 6*LONG_DELAY);
            }
        }
    };
    //   test go next
    private Runnable runnableCode2 = new Runnable() {
        @Override
        public void run() {
            if(!mTTS.isSpeaking() && isTimerStart){
                say(getResources().getString(R.string.tempo1min), false,false);
                handler2.postDelayed(runnableCode2, 6*LONG_DELAY);
            }
        }
    };
    //   first massage reminder
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            if(!isAppStarted){
                if(!mTTS.isSpeaking()) {
                    say(getResources().getString(R.string.charts), false, false);
//                    say(getResources().getString(captions.get(CHARTS_SEARCH)), false, false);
                }
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
                                                +" inServer=  "+ acuity.getInServer()
                                                +" log=  "+ acuity.getLog()
                                                +" logCal=  "+ acuity.getLogCal()
                                                +" logTest=  "+ acuity.getLogTest());
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
        handler2.removeCallbacks(runnableCode2);
        isSecondPeriod=false;;
    }

    /**
     * restart the background tasks
     * @param delay start runnable code 1 with defined delay
     * @return
     */
    void restartTask(int delay) {
//        handler.removeCallbacks(runnableCode);
//        handler0.removeCallbacks(runnableCode0);
//        isTimerStart=false;
//        isSecondPeriod=false;
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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
//        captions = new HashMap<>();
//        captions.put(CHARTS_SEARCH, R.string.charts);
//        captions.put(ACTION_CALIBRATION1, R.string.action_calibration);
//        captions.put(ACTION_CONTROLLER_CALIBRATION_INFO1, R.string.action_controller_calibration_info1);
//        captions.put(ACTION_CONTROLLER_TEST_INFO, R.string.action_controller_test_info);
//        captions.put(ACTION_CONTROLLER_TEST_INFO1, R.string.action_controller_test_info1);
//        captions.put(ACTION_CONTROLLER_TEST_INFO13, R.string.action_controller_test_info13);
//        captions.put(ACTION_CONTROLLER_TEST_INFO12, R.string.action_controller_test_info12);
//        captions.put(ACTION_CONTROLLER_TEST_INFO3, R.string.action_controller_test_info3);
//        captions.put(ACTION_CONTROLLER_TEST_INFO4, R.string.action_controller_test_info4);
//        captions.put(ACTION_CONTROLLER_TEST_INFO5, R.string.action_controller_test_info5);
//        captions.put(ACTION_RESET_USER, R.string.action_reset_user);
//        captions.put(ACTION_RESULT_LEFT, R.string.result_left_info);
//        captions.put(ACTION_RESULT_RIGHT, R.string.result_right_info);
//        captions.put(ACTION_EXIT_TEST, R.string.exit_test);
//        captions.put(ACTION_EXIT_TEST1, R.string.exit_test1);
//        captions.put(ACTION_EXIT_START_APP, R.string.exit_start_app);
//        captions.put(ACTION_END_TEST, R.string.end_test);
//        captions.put(ACTION_LAST_TEST, R.string.action_last_test);
//        captions.put(ACTION_REPEAT_TEST, R.string.action_repeat_test);
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
        mBluetoothAdapter= BluetoothChecker.enableBluetooth();
        if(mBluetoothAdapter==null){
            Log.e(TAG, "MainActivity:: onStart mBluetoothAdapter is null");
        }
        EventBus.getDefault().register(this);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        volumeOrig = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (streamMaxVolume*VOLUME), 0);

        batteryController = new Battery(this);
//        batteryController.updateBatteryLevel(Constants.BATTERY_LEVEL);
//        batteryController.updateBatteryTempMax(Constants.BATTERY_TEMP);
//        registerBattery(true);

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RocheSightChart:tracker");
        wl.acquire();
        if(!isTTS) {
            isTTS=true;
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (Util.DEBUG) {
                                Log.i(LOG_TAG_MAIN, "TextToSpeech status= " + status);
                            }
                            if (status != TextToSpeech.ERROR) {
                                mTTS.setLanguage(Locale.UK);
                                say(getResources().getString(R.string.charts), false,false);
                                handler.removeCallbacks(runnableCode);
                                handler.postDelayed(runnableCode, 2*LONG_DELAY);
                                isAppStarted=false;
                                if(newUser){
                                    if(acuityRepository!=null){
                                        AsyncTask.execute( new Runnable() {
                                            @Override
                                            public void run() {
                                                acuityRepository.newInstallation();
                                            }
                                        });
                                    }
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
        testDone=false;
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
//        registerBattery(false);
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
        end=false;
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

    ArrayList<String> bluetooth_msg;
    @Subscribe
    public void onEvent(BluetoothEvent event) {
        bluetooth_msg= event.getBluetooth_msg();
        if (Util.DEBUG) {
            Log.i(LOG_TAG_MAIN, "bluetooth_msg=" +bluetooth_msg.get(1));
        }
        if(bluetooth_msg.get(1).equals("disconnected")){
            //stop app
            exitFromTest(3);
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
                    +" newUser: "+newUser+ " isProcessing= "+isProcessing
                + " isDoubleTouche="+isDoubleTouche+" step1="+step1
                    + " step2="+step1+" test="+test+ " end="+end
            );
        }
        if(isProcessing && !isDoubleTouche && !step1){
            return true;
        }
        int keyCode=event.getKeyCode();
        int keyAction=event.getAction();
        int keyCodeRead;
        if((step1 || (!step1 && !step2 && !test  && !end)) &&
                (keyCode==Util.KEY_TRIGGER && keyAction==KeyEvent.ACTION_DOWN)){
            isDoubleTouche=true;
        }else if(keyCode==Util.KEY_TRIGGER && keyAction==KeyEvent.ACTION_UP){
            isDoubleTouche=false;
        }
        if(!isDoubleTouche &&
                (keyCode==Util.KEY_UP && keyAction==KeyEvent.ACTION_UP)||
                (keyCode==Util.KEY_DOWN && keyAction==KeyEvent.ACTION_UP)||
                (keyCode==Util.KEY_LEFT && keyAction==KeyEvent.ACTION_UP)||
                (keyCode==Util.KEY_RIGHT && keyAction==KeyEvent.ACTION_UP)){
            isJoystick=true;
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
                    +" isDoubleTouche: "+isDoubleTouche +" newUser: "+newUser
                    + " step1= "+step1+" step2= "+step2+" test= "+test
                    +" end= "+end + " isEndOfTest= "+isEndOfTest);
        }
        //TODO:: removed for test
////        if(keyCode==Util.KEY_TRIGGER && keyEvent == KeyEvent.ACTION_UP && (!step1 && !step2 && !test)){
////            isProcessing=true;
////            say(getResources().getString(captions.get(CHARTS_SEARCH)), false);
////            isProcessing=false;
////        }else
        if(keyCode==Util.KEY_POWER  &&  (keyAction == KeyEvent.ACTION_UP || fakeControls)
                && (!step1 && !step2 && !test  && !end) ) {
            //start app
            Util.upDatePref(this,Util.PREF_LEFT_CALIBRATION_X,LEFT_EYE_POS_X);
            Util.upDatePref(this,Util.PREF_LEFT_CALIBRATION_Y,LEFT_EYE_POS_Y);
            Util.upDatePref(this,Util.PREF_RIGHT_CALIBRATION_X,RIGHT_EYE_POS_X);
            Util.upDatePref(this,Util.PREF_RIGHT_CALIBRATION_Y,RIGHT_EYE_POS_Y);

            isProcessing = true;
            say("",false,false);
            isEndOfTest=false;
            startTest=Calendar.getInstance().getTimeInMillis();
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_POWER");
            }
            handler.removeCallbacks(runnableCode);
            isAppStarted = true;
            myGLRenderer.setChart(-1, -2, "", 0);
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
            if (!newUser) {
                setText("", "");
                step1 = false;
                step2 = true;
                test = false;
                end=false;
                eyeCalibration = -1;
                myGLRenderer.setChart(-1, -2, "", 0);
                myGLRenderer.setCharacter(2); //E character
                float x = Util.getSharedPreferences(this).getFloat(Util.PREF_LEFT_CALIBRATION_X, LEFT_EYE_POS_X);
                float y = Util.getSharedPreferences(this).getFloat(Util.PREF_LEFT_CALIBRATION_Y, LEFT_EYE_POS_Y);
                myGLRenderer.setLeftCenterX(x);
                myGLRenderer.setLeftCenterY(y);
                x = Util.getSharedPreferences(this).getFloat(Util.PREF_RIGHT_CALIBRATION_X, RIGHT_EYE_POS_X);
                y = Util.getSharedPreferences(this).getFloat(Util.PREF_RIGHT_CALIBRATION_Y, RIGHT_EYE_POS_Y);
                myGLRenderer.setRightCenterX(x);
                myGLRenderer.setRightCenterY(y);
                eye = -1;
                myGLRenderer.setCalibrationImage(3);
                contrastActive=0;
                good=0;
                err=0;
                learn.clearResult();
                Util.upDatePref(this,Util.PREF_LEFT0_START,FIRST_CHART_LEFT_EYE_0);
                Util.upDatePref(this,Util.PREF_RIGHT0_START,FIRST_CHART_RIGHT_EYE_0);
                Util.upDatePref(this,Util.PREF_LEFT1_START,FIRST_CHART_LEFT_EYE_1);
                Util.upDatePref(this,Util.PREF_RIGHT1_START,FIRST_CHART_RIGHT_EYE_1);
                Util.upDatePref(this,Util.PREF_LEFT2_START,FIRST_CHART_LEFT_EYE_2);
                Util.upDatePref(this,Util.PREF_RIGHT2_START,FIRST_CHART_RIGHT_EYE_2);
                startCalibration2(0, contrastActive);
            } else {
                //fixe the position calibration1
                Util.upDatePref(this,Util.PREF_LEFT_CALIBRATION_X,LEFT_EYE_POS_X);
                Util.upDatePref(this,Util.PREF_LEFT_CALIBRATION_Y,LEFT_EYE_POS_Y);
                Util.upDatePref(this,Util.PREF_RIGHT_CALIBRATION_X,RIGHT_EYE_POS_X);
                Util.upDatePref(this,Util.PREF_RIGHT_CALIBRATION_Y,RIGHT_EYE_POS_Y);
                Util.upDatePref(this,Util.PREF_LEFT0_START,FIRST_CHART_LEFT_EYE_0);
                Util.upDatePref(this,Util.PREF_RIGHT0_START,FIRST_CHART_RIGHT_EYE_0);
                Util.upDatePref(this,Util.PREF_LEFT1_START,FIRST_CHART_LEFT_EYE_1);
                Util.upDatePref(this,Util.PREF_RIGHT1_START,FIRST_CHART_RIGHT_EYE_1);
                Util.upDatePref(this,Util.PREF_LEFT2_START,FIRST_CHART_LEFT_EYE_2);
                Util.upDatePref(this,Util.PREF_RIGHT2_START,FIRST_CHART_RIGHT_EYE_2);
                step1=false;
                step2=true;
                test=false;


                //removed on 630 version
//                myGLRenderer.setCalibrationImage(2);
//                myGLRenderer.setCharacter(1); //circle with gaps character
//                setText("", "");
//                setInfo("Goggle calibration");
//                eyeCalibration = 0;
//                say(getResources().getString(captions.get(ACTION_CALIBRATION1)), false,false);
//                say(getResources().getString(captions.get(ACTION_CONTROLLER_CALIBRATION_INFO1)), true,false);
//                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO1)), true,false);
//                myGLRenderer.setChart(-1, eyeCalibration, "", learn.getOptotypeOuterDiameter(1));
////                myGLRenderer.setCharacter(2);
////                myGLRenderer.setChart(0,eyeCalibration,learn.getChartPosString(0, 0),
////                        learn.getOptotypePixels(0) );
//                step1 = true;
//                step2 = false;
//                test = false;
//                end=false;
            }
            contrastActive=0;
            good=0;
            err=0;
            learn.clearResult();
            noContrastLeftResult="";
            contrastLeftResult="";
            contrast_1LeftResult="";
            noContrastRightResult="";
            contrastRightResult="";
            contrast_1RightResult="";
            noContrastLeftLogCal=0;
            contrastLeftLogCal=0;
            contrast_1LeftLogCal=0;
            noContrastRightLogCal=0;
            contrastRightLogCal=0;
            contrast_1RightLogCal=0;
            noContrastLeftLogCal=0;
            contrastLeftLogTest=0;
            contrast_1LeftLogTest=0;
            noContrastRightLogTest=0;
            contrastRightLogTest=0;
            contrast_1RightLogTest=0;
            startCalibration2(0, contrastActive);
            isProcessing = false;
            return true;
        }else if(keyCode==Util.KEY_UP  && (keyAction == KeyEvent.ACTION_UP || fakeControls)
                && (step1==false && step2==false && test==false && end==false)){
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
                say(getResources().getString(R.string.action_reset_user), false,false);
                myGLRenderer.setChart(-1, -2, "", 0);
                resetPreferences(newUser);
                step1=false;
                step2=false;
                test=false;
                end=false;
                handler.postDelayed(runnableCode, 2*LONG_DELAY);//
                learn.clearResult();
                isProcessing=false;
                isDoubleTouche=false;
            }
        }if(keyCode==Util.KEY_DOWN  && (keyAction == KeyEvent.ACTION_UP || fakeControls)
                && (step1==false && step2==false && test==false && end==false)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_DOWN");
            }
            //reset app for new user
            if(isDoubleTouche){
                isProcessing = true;
                batteryLevel();
                isProcessing=false;
                isDoubleTouche=false;
            }else{
                isProcessing = true;
                sayResult();
                isProcessing=false;
            }
        }else  if(keyCode==Util.KEY_BACK  && (keyAction == KeyEvent.ACTION_UP || fakeControls)
                && (step1==false && step2==false && test==false  && end==false)){
                isProcessing = true;
                endOfTest(false,false);
                isProcessing = false;
        }else if(keyCode==Util.KEY_BACK && (keyAction == KeyEvent.ACTION_UP || fakeControls)
                && (step1==true || step2==true) && (test==false  && end==false)){
            //end of test in calibration phases
            isProcessing=true;
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_BACK");
            }
            isAppStarted=false;
//            stopTask(true);
            say("test stopped", false,false);
            endOfTest(false,false);
            isProcessing=false;
        }else if(keyCode==Util.KEY_BACK && (keyAction == KeyEvent.ACTION_UP || fakeControls)
                && (step1==false && step2==false && test==true  && end==false)){
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
            //removed v 630
//            if(isDoubleTouche){
//                //blind eye setup
//                if(keyCode==Util.KEY_UP && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
//                    if (Util.DEBUG) {
//                        Log.i(Util.LOG_TAG_KEY, "calibration1 isDoubleTouche KEY_UP");
//                    }
//                    isProcessing=true;
//                    //memorize eye to put no testing
//                    if(eyeCalibration==1 && Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==0){
//                        Util.upDatePref(this, Util.PREF_BLIND_EYE, 2);
//                    }else {
//                        Util.upDatePref(this, Util.PREF_BLIND_EYE, eyeCalibration);
//                    }
//                    if (Util.DEBUG) {
//                        Log.i(Util.LOG_TAG_KEY, "calibration1 isDoubleTouche KEY_UP blind= "+
//                                Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1));
//                    }
//                    isProcessing=false;
//                    isDoubleTouche=false;
//                }
//            }else{
                //start calibration 1
//                isProcessing=true;
//                say("",false,false);
//                greyE_left=GREY_ORG_E;
//                greyE_right=GREY_ORG_E;
//                greyE=GREY_ORG_E;
//                greySquare_left=GREY_ORG_SQUARE;
//                greySquare_right=GREY_ORG_SQUARE;
//                greySquare=GREY_ORG_SQUARE;
//                pixelNbr_left=1;
//                pixelNbr_right=1;
//                pixelNbr=1;
//                calibration1(keyCode, keyAction);
//                isProcessing=false;
//            }
        }else if(step2){
            //calibration2 and start test
            isProcessing=true;
            if(isReady){
                if(contrastActive==0 || contrastActive==1){
                    setText(String.valueOf((int)learn.getOptotypePixels(chart)), "");
                }else if(contrastActive==2){
                    setText(String.valueOf(learn.getContrastScore(chart)), "");
                }
                calibration2(keyCode, keyAction,contrastActive);
            }
            isProcessing=false;
        }else if(test){
            isProcessing=true;
            if(isReady){
                test(keyCode, keyAction);
            }
            isProcessing=false;
        }else if(end){
            isProcessing=true;
            if(keyCode==Util.KEY_POWER
                    && (keyAction == KeyEvent.ACTION_UP || fakeControls)) {
                //error
                endOfTest(true,false);
//                exitFromTest(false);
            }else if(keyCode==Util.KEY_TRIGGER
                    && (keyAction == KeyEvent.ACTION_UP || fakeControls)) {
                //test ok
                endOfTest(true,true);
//                exitFromTest(true);
            }else if(keyCode==Util.KEY_DOWN
                    && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
                sayResult();
            }
            isProcessing=false;
        }
        if((keyCode!=Util.KEY_UP && keyAction==KeyEvent.ACTION_UP)&&
                (keyCode!=Util.KEY_DOWN && keyAction==KeyEvent.ACTION_UP)&&
                (keyCode!=Util.KEY_LEFT && keyAction==KeyEvent.ACTION_UP)&&
                (keyCode!=Util.KEY_RIGHT && keyAction==KeyEvent.ACTION_UP)){
            isJoystick=false;
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
            newUser=true;
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
        Util.upDatePref(this,Util.PREF_LEFT0_START,FIRST_CHART_LEFT_EYE_0);
        Util.upDatePref(this,Util.PREF_RIGHT0_START,FIRST_CHART_RIGHT_EYE_0);
        Util.upDatePref(this,Util.PREF_LEFT1_START,FIRST_CHART_LEFT_EYE_1);
        Util.upDatePref(this,Util.PREF_RIGHT1_START,FIRST_CHART_RIGHT_EYE_1);
        Util.upDatePref(this,Util.PREF_LEFT2_START,FIRST_CHART_LEFT_EYE_2);
        Util.upDatePref(this,Util.PREF_RIGHT2_START,FIRST_CHART_RIGHT_EYE_2);
        if(keyCode==Util.KEY_TRIGGER && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_TRIGGER "
                        +" myGLRenderer.getLeftPositionX()="+myGLRenderer.getLeftPositionX()
                        + " myGLRenderer.getRightPositionX()="+myGLRenderer.getRightPositionX()
                        +" myGLRenderer.getLeftPositionY()="+myGLRenderer.getLeftPositionY()
                        + " myGLRenderer.getRightPositionY()="+myGLRenderer.getRightPositionY());
            }
            if(eyeCalibration==0){
                Util.upDatePref(this,Util.PREF_LEFT_CALIBRATION_X,myGLRenderer.getLeftPositionX());
                Util.upDatePref(this,Util.PREF_LEFT_CALIBRATION_Y,myGLRenderer.getLeftPositionY());
                eyeCalibration=1;
                myGLRenderer.setChart(-1, eyeCalibration, "", learn.getOptotypePixels(1));
                say(getResources().getString(R.string.action_controller_test_info13), false,false);
            }else{
                step1=false;
                step2=true;
                test=false;
                end=false;
                contrastActive=0;
                Util.upDatePref(this,Util.PREF_RIGHT_CALIBRATION_X,myGLRenderer.getRightPositionX());
                Util.upDatePref(this,Util.PREF_RIGHT_CALIBRATION_Y,myGLRenderer.getRightPositionY());
                good=0;
                err=0;
                learn.clearResult();
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
            endOfTest(false,false);
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
        isJoystick=false;
        isNegTestDone=false;
        if(Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==0){
            eyeCalibration=1;
        }else if(Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==2){
            endOfTest(false,false);
            return;
        }else if(eye==1 && Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==1){
            endOfTest(true,true);
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
        end=false;
        setInfo("Chart calibration");
        chart=0;
        if(contrastActive==0 || contrastActive==1){
            setText(String.valueOf((int)learn.getOptotypePixels(chart)), "");
        }else if(contrastActive==2){
            setText(String.valueOf(learn.getContrastScore(chart)), "");
        }

        if(contrastActive ==0){
            if(eyeCalibration==0){
                say(getResources().getString(R.string.action_controller_test_info), false,false);
//                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO)), false,false);
                say(getResources().getString(R.string.action_controller_test_info12), true,false);
                say(getResources().getString(R.string.action_controller_test_info1), true,false);
            }else if(eyeCalibration==1 && Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==0) {
                say(getResources().getString(R.string.action_controller_test_info), false,false);
                say(getResources().getString(R.string.action_controller_test_info12), true,false);
            }else{
                say(getResources().getString(R.string.action_controller_test_info), true,false);
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

        restartTask(3*LONG_DELAY);
    }

    /**
     * calibration of sizes of chart to use
     * @param keyCode key code from controller
     * @param keyAction key code from controller
     * @param contrastActive can be 0 - no contrast, 1 - contrast 5%, 2 - last test
     * @return
     */
    private void calibration2(int keyCode, int keyAction, int contrastActive){
        stopTask(false);
        if(contrastActive==0 || contrastActive==1){
            setText(String.valueOf((int)learn.getOptotypePixels(chart)), "");
        }else if(contrastActive==2){
            setText(String.valueOf(learn.getContrastScore(chart)), "");
        }
        if(keyCode==Util.KEY_TRIGGER && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            say("",false,false);
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "calibration2 KEY_TRIGGER chart= "+chart+
                        " eyeCalibration= "+ eyeCalibration+" contrastActive= "+ contrastActive);
            }
            //stop calibration go to test
//            say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO5)), true, false);
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
            good=0;
            err=0;
            learn.clearResult();
            if(chart>3) {
                chart=chart-4;
            }else{
                chart=0;
            }
            startTest(chart, contrastActive);
        }else if(keyCode==Util.KEY_UP && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            say("",false,false);
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, " calibration2 KEY_UP chart= "+chart+
                        " OptotypePixels= "+ learn.getOptotypePixels(chart)+
                        " contrastActive= "+ contrastActive);
            }
            isReady=false;
            say("up",false,false);
            calibrationResultChart("up",contrastActive);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_DOWN && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            say("",false,false);
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "calibration2 KEY_DOWN chart= "+chart +
                        " OptotypePixels= "+ learn.getOptotypePixels(chart)+
                        " contrastActive= "+ contrastActive);
            }
            isReady=false;
            say("down",false,false);
            calibrationResultChart("down", contrastActive);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_LEFT  && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            say("",false,false);
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "calibration2 KEY_LEFT chart= "+chart +
                        " OptotypePixels= "+ learn.getOptotypePixels(chart)+
                        " contrastActive= "+ contrastActive);
            }
            isReady=false;
            say("left",false,false);
            calibrationResultChart("left", contrastActive);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_RIGHT && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            say("",false,false);
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "calibration2 KEY_RIGHT chart= "+chart +
                        " OptotypePixels= "+ learn.getOptotypePixels(chart)+
                        " contrastActive= "+ contrastActive);
            }
            isReady=false;
            say("right",false,false);
            calibrationResultChart("right", contrastActive);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_BACK  && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            say("",false,false);
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "calibration2 KEY_BACK");
            }
            endOfTest(false,false);
        }else if(keyCode==Util.KEY_POWER  && (keyAction == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "calibration2 KEY_POWER "+
                        " contrastActive= " +contrastActive+
                        " eyeCalibration= "+ eyeCalibration+  " chartPos= "+ chartPos+ " chart= "+chart+
                        " err= "+err +" good= "+good +
                        " isJoystick= "+isJoystick + " isNegTestDone= "+isNegTestDone);
            }
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toneF.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,200);
                    }
                }, 200);
            if(!isNegTestDone){
                isNegTestDone=true;
                if(eyeCalibration==0){
                    if(contrastActive==0){
                        //from test contrast eye=1
                        contrastLeftLogTest++;
                        contrastLeftLogTest=-1*contrastLeftLogTest;
                    }else if(contrastActive==1){
                        //from test contrast_1
                        contrast_1LeftLogTest++;
                        contrast_1LeftLogTest=-1*contrast_1LeftLogTest;
                    }else if(contrastActive==2){
                        //from test no contrast
                        noContrastLeftLogTest++;
                        noContrastLeftLogTest=-1*noContrastLeftLogTest;
                    }
                }else if(eyeCalibration==1){
                    if(contrastActive==0){
                        //from test contrast eye=0
                        contrastLeftLogTest++;
                        contrastLeftLogTest=-1*contrastLeftLogTest;
                    }else if(contrastActive==1){
                        //from test contrast_1
                        contrast_1LeftLogTest++;
                        contrast_1LeftLogTest=-1*contrast_1LeftLogTest;
                    }else if(contrastActive==2){
                        //from test no contrast
                        noContrastLeftLogTest++;
                        noContrastLeftLogTest=-1*noContrastLeftLogTest;
                    }
                }
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_KEY, "test KEY_POWER "+
                            " isNegCalDone= "+isNegCalDone+
                            " noContrastLeftLogCal= " +noContrastLeftLogCal+
                            " contrastLeftLogCal= "+ contrastLeftLogCal+
                            " contrast_1LeftLogCal= "+ contrast_1LeftLogCal+
                            " noContrastRightLogCal= "+noContrastRightLogCal+
                            " contrastRightLogCal= "+contrastRightLogCal +
                            " contrast_1RightLogCal= "+contrast_1RightLogCal);
                }
            }
            if(isJoystick){
                if(eyeCalibration==0){
                    if(contrastActive==0){
                        noContrastLeftLogCal++;
                    }else if(contrastActive==1){
                        contrastLeftLogCal++;
                    }else if(contrastActive==2){
                        contrast_1LeftLogCal++;
                    }
                }else if(eyeCalibration==1){
                    if(contrastActive==0){
                        noContrastRightLogCal++;
                    }else if(contrastActive==1){
                        contrastRightLogCal++;
                    }else if(contrastActive==2){
                        contrast_1RightLogCal++;
                    }
                }
            }

            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "calibration2 KEY_POWER "+
                        " noContrastLeftLogCal= " +noContrastLeftLogCal+
                        " contrastLeftLogCal= "+ contrastLeftLogCal+
                        " contrast_1LeftLogCal= "+ contrast_1LeftLogCal+
                        " noContrastRightLogCal= "+noContrastRightLogCal+
                        " contrastRightLogCal= "+contrastRightLogCal +
                        " contrast_1RightLogCal= "+contrast_1RightLogCal);
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }
        restartTask(3*LONG_DELAY);
    }

    /**
     * start test after calibration
     * @param
     * @param chart default chart after calibration
     * @param contrastActive
     * @return
     */
    private void startTest(int chart, int contrastActive){
        isJoystick=false;
        isNegCalDone=false;
        if(eyeCalibration==0 && contrastActive==0){
            Util.upDatePref(this, Util.PREF_LEFT0_START,chart);
            eye=-1;
        }if(eyeCalibration==0 && contrastActive==1){
            Util.upDatePref(this, Util.PREF_LEFT1_START,chart);
            eye=-1;
        }if(eyeCalibration==0 && contrastActive==2){
            Util.upDatePref(this, Util.PREF_LEFT2_START,chart);
            eye=-1;
        }else if(eyeCalibration==1  && contrastActive==0){
            Util.upDatePref(this, Util.PREF_RIGHT0_START,chart);
            eye=-2;
        }else if(eyeCalibration==1  && contrastActive==1){
            Util.upDatePref(this, Util.PREF_RIGHT1_START,chart);
            eye=-2;
        }else if(eyeCalibration==1  && contrastActive==2){
            Util.upDatePref(this, Util.PREF_RIGHT2_START,chart);
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
        restartTask(3*LONG_DELAY);
    }

    /**
     * acuity test for both eyes
     * @param keyCode key code from controller
     * @param keyEvent key code from controller
     * @return
     */
    private void test(int keyCode, int keyEvent){
        stopTask(false);
        say(" ",false,false);
        setInfo("test running");
        if(contrastActive==0 || contrastActive==1){
            setText(String.valueOf((int)learn.getOptotypePixels(chart)), "");
        }else if(contrastActive==2){
            setText(String.valueOf(learn.getContrastScore(chart)), "");
        }

        if(keyCode==Util.KEY_TRIGGER && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_TRIGGER");
            }
            //start next test
            nextChart(NEXT_CHART_FOR_GOOD, contrastActive);
//            nextChart(NEXT_CHART_FOR_ERROR);
        }else if(keyCode==Util.KEY_UP  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            isReady=false;
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_UP");
            }
            say("up",false,false);
            resultChart("up");
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);

        }else if(keyCode==Util.KEY_DOWN  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            isReady=false;
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_DOWN");
            }
            say("down",false,false);
            resultChart("down");
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_LEFT  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            isReady=false;
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_LEFT");
            }
            say("left",false,false);
            resultChart("left");
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_RIGHT  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            isReady=false;
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "KEY_RIGHT");
            }
            say("right",false,false);
            resultChart("right");
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }else if(keyCode==Util.KEY_POWER  && (keyEvent == KeyEvent.ACTION_UP || fakeControls)){
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "test KEY_POWER "+
                        " contrastActive= " +contrastActive+
                        " eye= "+ eye+  " chartPos= "+ chartPos+ " chart= "+chart+
                        " err= "+err +" good= "+good +
                        " isJoystick= "+isJoystick+ " isNegCalDone= "+isNegCalDone);
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    toneF.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,200);
                }
            }, 200);

            if(!isNegCalDone){
                isNegCalDone=true;
                if(eye==0){
                    if(contrastActive==0){
                        noContrastLeftLogCal++;
                        noContrastLeftLogCal=-1*noContrastLeftLogCal;
                    }else if(contrastActive==1){
                        contrastLeftLogCal++;
                        contrastLeftLogCal=-1*contrastLeftLogCal;
                    }else if(contrastActive==2){
                        contrast_1LeftLogCal++;
                        contrast_1LeftLogCal=-1*contrast_1LeftLogCal;
                    }
                }else if(eye==1){
                    if(contrastActive==0){
                        noContrastRightLogCal++;
                        noContrastRightLogCal=-1*noContrastRightLogCal;
                    }else if(contrastActive==1){
                        contrastRightLogCal++;
                        contrastRightLogCal=-1*contrastRightLogCal;
                    }else if(contrastActive==2){
                        contrast_1RightLogCal++;
                        contrast_1RightLogCal=-1*contrast_1RightLogCal;
                    }
                }
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_KEY, "test KEY_POWER "+
                            " isNegCalDone= "+isNegCalDone+
                            " noContrastLeftLogCal= " +noContrastLeftLogCal+
                            " contrastLeftLogCal= "+ contrastLeftLogCal+
                            " contrast_1LeftLogCal= "+ contrast_1LeftLogCal+
                            " noContrastRightLogCal= "+noContrastRightLogCal+
                            " contrastRightLogCal= "+contrastRightLogCal +
                            " contrast_1RightLogCal= "+contrast_1RightLogCal);
                }
            }
            if(isJoystick){
                if(eye==0){
                    if(contrastActive==0){
                        noContrastLeftLogTest++;
                    }else if(contrastActive==1){
                        contrastLeftLogTest++;
                    }else if(contrastActive==2){
                        contrast_1LeftLogTest++;
                    }
                }else if(eye==1){
                    if(contrastActive==0){
                        noContrastRightLogTest++;
                    }else if(contrastActive==1){
                        contrastRightLogTest++;
                    }else if(contrastActive==2){
                        contrast_1RightLogTest++;
                    }
                }
            }

            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_KEY, "test KEY_POWER "+
                        " noContrastLeftLogTest= " +noContrastLeftLogTest+
                        " contrastLeftLogTest= "+ contrastLeftLogTest+
                        " contrast_1LeftLogTest= "+ contrast_1LeftLogTest+
                        " noContrastRightLogTest= "+noContrastRightLogTest+
                        " contrastRightLogTest= "+contrastRightLogTest +
                        " contrast_1RightLogTest= "+contrast_1RightLogTest);
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    isReady=true;
                }}, KEY_DELAY);
        }
        restartTask(3*LONG_DELAY);
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
                    " contrastActive= " +contrastActive+
                    " eye= "+ eye+  " chartPos= "+ chartPos+ " chart= "+chart+
                    " err= "+err +" good= "+good +
                    " blind= "+Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1));
        }
        if (eye == -1 || eye==-2) {
            if(Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==0){
                //start with right eye
                eye = 1;
                if(contrastActive==0){
                    chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START, FIRST_CHART_RIGHT_EYE_0);
                }else if(contrastActive==1){
                    chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT1_START, FIRST_CHART_RIGHT_EYE_1);
                }else if(contrastActive==2){
                    chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT2_START, FIRST_CHART_RIGHT_EYE_2);
                }
//                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO2)), true, true);
            }else if(Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==2){
                //blind end of test
                endOfTest(false,false);
                return;
            }else{
                //start with left or right eye
                if(eye==-1){
                    eye = 0;
                    if(contrastActive==0){
                        chart = Util.getSharedPreferences(this).getInt(Util.PREF_LEFT0_START, FIRST_CHART_LEFT_EYE_0);
                    }else if(contrastActive==1){
                        chart = Util.getSharedPreferences(this).getInt(Util.PREF_LEFT1_START, FIRST_CHART_LEFT_EYE_1);
                    }else if(contrastActive==2){
                        chart = Util.getSharedPreferences(this).getInt(Util.PREF_LEFT2_START, FIRST_CHART_LEFT_EYE_2);
                    }
                }else{
                    eye = 1;
                    if(contrastActive==0){
                        chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START, FIRST_CHART_RIGHT_EYE_0);
                    }else if(contrastActive==1){
                        chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT1_START, FIRST_CHART_RIGHT_EYE_1);
                    }else if(contrastActive==2){
                        chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT2_START, FIRST_CHART_RIGHT_EYE_2);
                    }
                }
//                say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO1)), true, true);
            }
            chartPos=0;
            totalLengthStringArray = learn.getSizeChartsPos(chart);
            learn.clearResult();
        } else {
            chart++;
            //all charts for the contrast's level was done or error
            if ((contrastActive<2 && (chart >= totalLengthCharts) || contrastActive==2 && (chart>=14)) ||
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
                        //go to next contrast
                        eye=0;
                        this.contrastActive = 2;
                        chart = Util.getSharedPreferences(this).getInt(Util.PREF_LEFT2_START, FIRST_CHART_LEFT_EYE_2);
                        say(getResources().getString(R.string.action_controller_test_info4), true, false);
                        good=0;
                        err=0;
                        totalLengthStringArray = learn.getSizeChartsPos(chart);
                        learn.clearResult();
                        startCalibration2(eye, this.contrastActive);
                        return;
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
                        //go to next contrast
                        if(chart>LIMIT_GREY){
                            eye=0;
                            this.contrastActive = 1;
                            chart = Util.getSharedPreferences(this).getInt(Util.PREF_LEFT1_START, FIRST_CHART_LEFT_EYE_1);
                            say(getResources().getString(R.string.action_controller_test_info3), true, false);
                        }else{
                            eye=1;
                            this.contrastActive = 0;
                            chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START, FIRST_CHART_RIGHT_EYE_0);
                            say(getResources().getString(R.string.action_controller_test_info13), true, false);

                        }
                        good=0;
                        err=0;
                        totalLengthStringArray = learn.getSizeChartsPos(chart);
                        learn.clearResult();
                        startCalibration2(eye, this.contrastActive);
                        return;
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
                        if(Util.getSharedPreferences(this).getInt(Util.PREF_BLIND_EYE, -1)==1){
                            endOfTest(true,true);
                            return;
                        }else{
                            //go to next eye
                            chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START, FIRST_CHART_RIGHT_EYE_0);
                            eye=1;
                            this.contrastActive = 0;
                            say(getResources().getString(R.string.action_controller_test_info13), true, false);
                            totalLengthStringArray = learn.getSizeChartsPos(chart);
                            good=0;
                            err=0;
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
                        eye=1;
                        this.contrastActive = 2;
                        chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT2_START, FIRST_CHART_RIGHT_EYE_2);;
                        say(getResources().getString(R.string.action_controller_test_info4), true, false);
                        good=0;
                        err=0;
                        totalLengthStringArray = learn.getSizeChartsPos(chart);
                        learn.clearResult();
                        startCalibration2(eye, this.contrastActive);
                        return;
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
                        if(chart>LIMIT_GREY){
                            chart = Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT1_START, FIRST_CHART_RIGHT_EYE_1);
                            say(getResources().getString(R.string.action_controller_test_info3), true, false);
                            eye=1;
                            this.contrastActive = 1;
                        }else{
                            this.contrastActive = -1;
                            endOfTest(true,true);
                            return;
                        }
                        good=0;
                        err=0;
                        totalLengthStringArray = learn.getSizeChartsPos(chart);
                        learn.clearResult();
                        startCalibration2(eye, this.contrastActive);
                        return;
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

//                        learn.clearResult();
                        this.contrastActive = -1;
                        endOfTest(true,true);
                        return;
                    }
                }
            }
        }

        setInfo("test started");
        if(contrastActive==0 || contrastActive==1){
            setText(String.valueOf((int)learn.getOptotypePixels(chart)), "");
        }else if(contrastActive==2){
            setText(String.valueOf(learn.getContrastScore(chart)), "");
        }
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
     * @param logRes
     * @return
     */
    private void insertAcuity(int contrast, long startTest, int leftFirst, int rightFirst,
                              String leftResult, String rightResult,
                              int leftLogCal, int leftLogTest,
                              int rightLogCal, int rightLogTest, int logRes) {
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
                    leftResult, rightResult,
                    leftLogCal, leftLogTest,
                    rightLogCal,rightLogTest,logRes);
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
        if((contrastActive<2 && chart>-1 && chart<=totalLengthCharts-1) ||
                (contrastActive==2 && chart>-1 && chart<=13)){
//        if(chart>-1 && chart<=totalLengthCharts-1){
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
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toneL.startTone(ToneGenerator.TONE_DTMF_0,200);
                        }
                    }, 500);

                }else{
                    //if good answer
                    good=good+1;
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toneH.startTone(ToneGenerator.TONE_DTMF_1,200);
                        }
                    }, 500);

                }
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_KEY, "calibrationResultChart response= "+r+
                            " good= "+good+" err= "+ err);
                }
                if(good>=2){//next smaller
                    chart=chart+2;
                    if(contrastActive==0 || contrastActive==1){
                        setText(String.valueOf((int)learn.getOptotypePixels(chart)), "");
                    }else if(contrastActive==2){
                        setText(String.valueOf(learn.getContrastScore(chart)), "");
                    }
//                    totalLengthStringArray = learn.getSizeChartsPos(chart);
                    learn.clearResult();
                    chartPos=0;
                    good=0;
                    err=0;
                    if((contrastActive<2 && chart>=totalLengthCharts) ||
                            (contrastActive==2 && chart>=14)){
//                    if(chart>=totalLengthCharts){
                        if (Util.DEBUG) {
                            Log.i(Util.LOG_TAG_KEY, "calibrationResultChart good, go to test");
                        }
                        if(contrastActive==2){
                            chart=11;
                        }else{
                            chart=totalLengthCharts-3;
                        }
                        totalLengthStringArray = learn.getSizeChartsPos(chart);
//                        say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO5)), true, false);
                        good=0;
                        err=0;
                        learn.clearResult();
                        startTest(chart, contrastActive);
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
//                    say(getResources().getString(captions.get(ACTION_CONTROLLER_TEST_INFO5)), true, false);
                    good=0;
                    err=0;
                    learn.clearResult();
                    if(chart>3) {
                        chart=chart-4;
                    }else{
                        chart=0;
                    }
                    startTest(chart,contrastActive);
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
     * check if test finish
     * @param result string of user's result
     * @param
     * @return
     */
    private void resultChart(String result) {
//        setText(result,"");
        if((contrastActive<2 && chart>-1 && chart<=totalLengthCharts-1) ||
                (contrastActive==2 && chart>-1 && chart<=13)){
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
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toneL.startTone(ToneGenerator.TONE_DTMF_0,200);
                        }
                    }, 500);

                }else{
                    //if good answer
                    good=good+1;
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toneH.startTone(ToneGenerator.TONE_DTMF_1,200);
                        }
                    }, 500);

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
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                toneL.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER,1000);
                            }
                        }, 500);
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
     * @param ok //test finished ok
     * @param exitOK //problem with test
     * @param
     * @return
     */
    private void endOfTest(boolean ok, boolean exitOK) {
        stopTask(false);
        setText("","");
        isTimerStart=false;
        chartPos=-1;
        err=0;
        good=0;
        contrastActive=-1;
        testDone=true;
        myGLRenderer.setChart(-1, -2, "", 0);
        if(exitOK){
            logRes=1;
        }else{
            logRes=-1;
        }
        if (Util.DEBUG) {
            Log.d(LOG_TAG_MAIN, "endOfTest test= "+test+" ok= "+ok+" exitOK= "+exitOK+" eye= "+eye);
        }
        if(test & ok){
            say(getResources().getString(R.string.end_test), false,false);
            end=true;
            test=false;
            step1=false;
            step2=false;
        }else if(end && ok){
            if(contrastLeftResult=="" ){
                contrastLeftResult="0";
            }
            if(contrastRightResult=="" ){
                contrastRightResult="0";
            }

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
            Log.d(LOG_TAG_MAIN, "endOfTest errors:"
                    +" logRes= "+ logRes
                    + " noContrastLeftLogCal= "+ noContrastLeftLogCal
                    + " noContrastLeftLogTest= "+ noContrastLeftLogTest
                    + " contrastRightLogCal= "+ contrastLeftLogCal
                    + " contrastRightLogTest= "+ contrastLeftLogTest
                    + " contrast_1LeftLogCal= "+ contrast_1LeftLogCal
                    + " noContrastLeftLogCal= "+ noContrastRightLogCal
                    + " noContrastLeftLogTest= "+ noContrastRightLogTest
                    + " contrastRightLogCal= "+ contrastRightLogCal
                    + " contrastRightLogTest= "+ contrastRightLogTest
                    + " contrast_1LeftLogCal= "+ contrast_1RightLogCal);
            //insert in database no contrast tests
            if(noContrastLeftResult!="" || noContrastRightResult!=""){
                insertAcuity(0, startTest,
                        Util.getSharedPreferences(this).getInt(Util.PREF_LEFT0_START, FIRST_CHART_LEFT_EYE_0),
                        Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT0_START, FIRST_CHART_RIGHT_EYE_0),
                        noContrastLeftResult, noContrastRightResult,
                        noContrastLeftLogCal,noContrastLeftLogTest,
                        noContrastRightLogCal,noContrastRightLogTest, logRes);
            }
            //insert in database contrasts tests
            if(contrastLeftResult!="" || contrastRightResult!=""){
                insertAcuity(1, startTest,
                        Util.getSharedPreferences(this).getInt(Util.PREF_LEFT1_START, FIRST_CHART_LEFT_EYE_1),
                        Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT1_START, FIRST_CHART_RIGHT_EYE_1),
                        contrastLeftResult, contrastRightResult,
                        contrastLeftLogCal,contrastLeftLogTest,
                        contrastRightLogCal,contrastRightLogTest, logRes);
            }
            if(contrast_1LeftResult!="" || contrast_1RightResult!=""){
                insertAcuity(2, startTest,
                        Util.getSharedPreferences(this).getInt(Util.PREF_LEFT2_START, FIRST_CHART_LEFT_EYE_2),
                        Util.getSharedPreferences(this).getInt(Util.PREF_RIGHT2_START, FIRST_CHART_RIGHT_EYE_2),
                        contrast_1LeftResult, contrast_1RightResult,
                        contrast_1LeftLogCal,contrast_1LeftLogTest,
                        contrast_1RightLogCal,contrast_1RightLogTest,logRes);
            }

            AsyncTask.execute( new Runnable() {
                @Override
                public void run() {
                    bestAcuities0=acuityRepository.getBestAcuity(0);
                    if(bestAcuities0[0]==-1 && bestAcuities0[1]==-1){
                        toRepeatTest=false;
                    }else{
                        if(Math.abs(bestAcuities0[0]-Float.parseFloat(noContrastLeftResult))>4 ||
                                Math.abs(bestAcuities0[1]-Float.parseFloat(noContrastRightResult))>4){
                            toRepeatTest=true;
//                    say(getResources().getString(captions.get(ACTION_REPEAT_TEST)), true,true);
                        }else{
                            toRepeatTest=false;
                        }
                    }
                    if(exitOK){
                        exitFromTest(0);
                    }else{
                        exitFromTest(1);
                    }
                }
            });

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
                        noContrastLeftResult, noContrastRightResult,
                        noContrastLeftLogCal,noContrastLeftLogTest,
                        noContrastRightLogCal,noContrastRightLogTest, logRes);
            }else if(contrastActive==1){
                contrastLeftResult="-2";
                contrastRightResult="-2";
                handler0.removeCallbacks(runnableCode0);
                insertAcuity(0, startTest,
                        Util.getSharedPreferences(context).getInt(Util.PREF_LEFT1_START, FIRST_CHART_LEFT_EYE_1),
                        Util.getSharedPreferences(context).getInt(Util.PREF_RIGHT1_START, FIRST_CHART_RIGHT_EYE_1),
                        contrastLeftResult, contrastRightResult,
                        noContrastLeftLogCal,noContrastLeftLogTest,
                        noContrastRightLogCal,noContrastRightLogTest, logRes);
            }else if(contrastActive==2){
                contrast_1LeftResult="-2";
                contrast_1RightResult="-2";
                handler0.removeCallbacks(runnableCode0);
                insertAcuity(0, startTest,
                        Util.getSharedPreferences(context).getInt(Util.PREF_LEFT2_START, FIRST_CHART_LEFT_EYE_2),
                        Util.getSharedPreferences(context).getInt(Util.PREF_RIGHT2_START, FIRST_CHART_RIGHT_EYE_2),
                        contrast_1LeftResult, contrast_1RightResult,
                        noContrastLeftLogCal,noContrastLeftLogTest,
                        noContrastRightLogCal,noContrastRightLogTest, logRes);
            }

            handler0.postDelayed(runnableCode0, 100);
            exitFromTest(2);
        }
    }

    /**
     * end of test
     * @param exit //0 - test finnished ok, 1 - test had the problem, 2 - stopped by user
     * @param
     * @return
     */
    private void exitFromTest(int exit){
        chart=-1;
        eye=-2;
        if (Util.DEBUG) {
            Log.d(LOG_TAG_MAIN, "endOfTest startedByPackage="+ startedByPackage);}

        if(exit==0){
            say(getResources().getString(R.string.exit_test1), true,false);
        }else if(exit==1){
            say(getResources().getString(R.string.exit_test), true,false);
        }else if(exit==2){
            say(getResources().getString(R.string.exit_test2), true,false);
        }else{
            toRepeatTest=false;
            logRes=-2;
            say(getResources().getString(R.string.exit_test2), true,false);
        }
        if(startedByPackage!=null){
            isProcessing=true;
            if(logRes!=-2){
                say(getResources().getString(R.string.exit_start_app), true,true);
            }
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeOrig, 0);
//                sendBroadcastToActivity(BROADCAST_START_APP_ACTION, START_APP_RESULT, 1);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    startExternalApp(startedByPackage);
                }
            },SHORT_DELAY);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
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
            end=false;
            contrastActive=-1;
            isEndOfTest=false;
            return;
        }else{
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
//                    isProcessing=false;
                    isEndOfTest=true;
                }
            },LONG_DELAY);
        }
        isAppStarted=false;
        step2=false;
        step1=false;
        test=false;
        end=false;
        contrastActive=-1;
    }
//private boolean isSayResult;
    private void sayResult() {
//        isSayResult=true;
        if(!testDone){
            return;
        }
        String noContrastLeft="";
        String noContrastRight="";
        String contrastLeft="";
        String contrastRight="";
        if(noContrastLeftResult=="" || noContrastLeftResult=="0" || noContrastLeftResult=="-1" || noContrastLeftResult=="-2" ){
//                noContrastLeftResult="no reading";
        }else{
            noContrastLeft=learn.getPixelsFromMunit(noContrastLeftResult);
        }
        if(noContrastRightResult==""  || noContrastRightResult=="0" || noContrastRightResult=="-1" || noContrastRightResult=="-2" ){
//                noContrastRightResult="no reading";
        }else{
            noContrastRight=learn.getPixelsFromMunit(noContrastRightResult);
        }
        if(contrastLeftResult=="" || contrastLeftResult=="0" || contrastLeftResult=="-1" || contrastLeftResult=="-2"){
//                contrastLeftResult="no reading";
        }else{
            contrastLeft=learn.getPixelsFromMunit(contrastLeftResult);
        }
        if(contrastRightResult=="" || contrastRightResult=="0" || contrastRightResult=="-1" || contrastRightResult=="-2"){
//                contrastRightResult="no reading";
        }else{
            contrastRight=learn.getPixelsFromMunit(contrastRightResult);
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
        setText("left eye: "+noContrastLeft +" / " +contrastLeft+" / " +contrast_1LeftResult,
                "right eye: "+noContrastRight +" / " +contrastRight+" / " +contrast_1RightResult);
        say(getResources().getString(R.string.result_left_info)+", no contrast "+noContrastLeft +
                " pixels, contrast " +contrastLeft+" pixels, contrast sensitivity " +contrast_1LeftResult+ "log. "+
                getResources().getString(R.string.result_right_info)+", no contrast "+noContrastRight +
                " pixels, contrast " +contrastRight +" pixels, contrast sensitivity " +contrast_1RightResult + "log.", false,false);
//        say(getResources().getString(captions.get(ACTION_RESULT_RIGHT))+", no contrast "+noContrastRightResult +
//                " pixels, contrast " +contrastRightResult +" pixels, contrast sensitivity " +contrast_1RightResult + "log", true,false);
//        isSayResult=false;
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
            if(logRes==-1){
                sendBroadcastToActivity(BROADCAST_START_APP_ACTION, START_APP_RESULT, 4);
            }else if(logRes==-2){
                sendBroadcastToActivity(BROADCAST_START_APP_ACTION, START_APP_RESULT, 6);
            }else{
                sendBroadcastToActivity(BROADCAST_START_APP_ACTION, START_APP_RESULT, 3);
            }
        }else{
            if(logRes==-1){
                sendBroadcastToActivity(BROADCAST_START_APP_ACTION, START_APP_RESULT, 5);
            }else if(logRes==-2){
                sendBroadcastToActivity(BROADCAST_START_APP_ACTION, START_APP_RESULT, 6);
            }else{
                sendBroadcastToActivity(BROADCAST_START_APP_ACTION, START_APP_RESULT, 2);
            }
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

    private void batteryLevel(){
        say(context.getString(R.string.battery_is_en)+" "+batteryController.getCurrentLevel()+" "+context.getString(R.string.p_charged_en),false,false);
    }
    private void registerBattery(boolean register){
        Log.i(TAG, "mBatteryReceiver started register= "+register);
        if(register){
            mBatteryReceiver = new BatteryService();
            if( mBatteryReceiver!=null){
                registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
//		        	LogManagement.Log_d(TAG, "MainActivity:: mBatteryReceiver registered");
            }
        }else{
            if(mBatteryReceiver!=null){
                unregisterReceiver(mBatteryReceiver);
                mBatteryReceiver=null;
//            	LogManagement.Log_d(TAG, "MainActivity:: mBatteryReceiver unregistered");
            }
        }
    }

}

