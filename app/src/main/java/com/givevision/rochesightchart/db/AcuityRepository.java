package com.givevision.rochesightchart.db;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.room.Room;

import com.givevision.rochesightchart.Util;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

public class
AcuityRepository {
    public final static String TAG = "AcuityRepository";

    private String DB_NAME = "db_givevision";
    private GiveVisionDatabase gvDatabase;

    public AcuityRepository(Context context) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_DB, "create database");
        }
        gvDatabase = Room.databaseBuilder(context, GiveVisionDatabase.class, DB_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    public void insertAcuity(int userId, int contrast, int duration,
                             String leftEyeFirst, String rightEyeFirst,
                             String leftEye, String rightEye,
                             int leftLogCal, int leftLogTest,
                             int rightLogCal, int rightLogTest, int logRes) {
        Date currentTime = new Date(Calendar.getInstance().getTimeInMillis());

        final Acuity acuity = new Acuity();
        acuity.setUserId(userId);
        acuity.setContrast(contrast);
        acuity.setDuration(duration);
        acuity.setLeftEyeFirst(leftEyeFirst);
        acuity.setRightEyeFirst(rightEyeFirst);
        acuity.setLeftEye(leftEye);
        acuity.setRightEye(rightEye);
        acuity.setInServer(false);
        acuity.setInServer(false);
        acuity.setLeftLogCal(leftLogCal);
        acuity.setLeftLogTest(leftLogTest);
        acuity.setRightLogCal(rightLogCal);
        acuity.setRightLogTest(rightLogTest);
        acuity.setLog(logRes);
        acuity.setCreatedAt(currentTime);
        acuity.setModifiedAt(currentTime);
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_DB, "insert acuity: userId:"+userId+" contrast:"+contrast
                     +" leftEye:"+leftEye+" rightEye:"+rightEye
                    +" leftLogCal:"+leftLogCal+" leftLogTest:"+leftLogTest
                    +" rightLogCal:"+rightLogCal+" rightLogTest:"+rightLogTest
                    +" log (result):"+logRes);
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                gvDatabase.acuityDao().insertAcuity(acuity);
            }
        });
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... voids) {
//                gvDatabase.acuityDao().insertAcuity(acuity);
//                return null;
//            }
//        }.execute();

    }

    private void updateAcuityInDB(final int id, final boolean inDB) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Date currentTime = new Date(Calendar.getInstance().getTimeInMillis());
                final Acuity acuity=  getAcuityById(id);
                acuity.setModifiedAt(currentTime);
                acuity.setInServer(inDB);
                gvDatabase.acuityDao().updateAcuity(acuity);
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_DB, "update acuity");
                }
            }
        });
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... voids) {
//                Date currentTime = new Date(Calendar.getInstance().getTimeInMillis());
//                final Acuity acuity=  getAcuityById(id);
//                acuity.setModifiedAt(currentTime);
//                acuity.setInServer(inDB);
//                gvDatabase.acuityDao().updateAcuity(acuity);
//                if (Util.DEBUG) {
//                    Log.i(Util.LOG_TAG_DB, "update acuity");
//                }
//                return null;
//            }
//        }.execute();
    }

    public List<Acuity> getAllAcuities() {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_DB, "get all acuities");
        }
        return gvDatabase.acuityDao().getAllAcuities();
    }

    public float[] getBestAcuity(int contrast) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_DB, "getBestAcuity");
        }
        float[] bests= {0,0};
        List<Acuity> list=gvDatabase.acuityDao().getBestAcuities(contrast);
        if(list.size()<3){
            bests[0]=-1;
            bests[1]=-1;
        }else{
            for (Acuity acuity : list) {
                if (Util.DEBUG) {
                    Log.i(Util.LOG_TAG_DB, "getBestAcuity acuities= "+acuity.getLeftEye());
                }
                if(acuity.getLeftEye()!=null){
                    String left=acuity.getLeftEye();
                    if(Float.parseFloat(left)>bests[0]){
                        bests[0]=Float.parseFloat(left);
                    }
                }
                if(acuity.getRightEye()!=null){
                    String right=acuity.getRightEye();
                    if(Float.parseFloat(right)>bests[1]){
                        bests[1]=Float.parseFloat(right);
                    }
                }
            }
        }
        return bests;
    }

    public List<Acuity> getAcuitiesByUserId(int userId) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_DB, "get acuities by user id");
        }
        return gvDatabase.acuityDao().getAcuitiesByUserId(userId);
    }

    public Acuity getAcuityById(int id) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_DB, "get acuity by id");
        }
        return gvDatabase.acuityDao().findAcuityById(id);
    }

    public Acuity getAcuityCreatedAt(Date createdAt) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_DB, "get acuity by createdAt");
        }
        return gvDatabase.acuityDao().findAcuityByCreatedDate(createdAt);
    }

    public Acuity getAcuityModifiedAt(Date modifiesAt) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_DB, "get acuity by modifiesAt");
        }
        return gvDatabase.acuityDao().findAcuityByModifiedDate(modifiesAt);
    }

    public List<Acuity> getAcuitiesByInDB(boolean inDB) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_DB, "get acuities by in DataBase");
        }
        return gvDatabase.acuityDao().loadAcuitiesByInDatabase(inDB);
    }

     public void deleteAcuitiesTable() {
         AsyncTask.execute(new Runnable() {
             @Override
             public void run() {
                 gvDatabase.acuityDao().deleteAcuityTable();
                 if (Util.DEBUG) {
                     Log.d(TAG, "PictureRepository:: deleteAcuities Table");
                 }
             }
         });
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... voids) {
//                gvDatabase.acuityDao().deleteAcuityTable();
//                if (Util.DEBUG) {
//                    Log.d(TAG, "PictureRepository:: deleteAcuities Table");
//                }
//                return null;
//            }
//        }.execute();
    }

    public void deleteAcuityById(final int id) {
        final Acuity acuity = getAcuityById(id);
        if (acuity != null) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    gvDatabase.acuityDao().deleteAcuity(acuity);
                    if (Util.DEBUG) {
                        Log.d(TAG, "PictureRepository:: delete Acuity");
                    }
                }
            });
//            new AsyncTask<Void, Void, Void>() {
//                @Override
//                protected Void doInBackground(Void... voids) {
//                    gvDatabase.acuityDao().deleteAcuity(acuity);
//                    if (Util.DEBUG) {
//                        Log.d(TAG, "PictureRepository:: delete Acuity");
//                    }
//                    return null;
//                }
//            }.execute();
        }
    }

    public void newInstallation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // new installation
                List<Acuity> acuities = getAllAcuities();
                if(!acuities.isEmpty()){
                    for(Acuity acuity : acuities) {
                        if(acuity!=null){
                            if (Util.DEBUG) {
                                Log.d(TAG, "newInstallation:: acuity id= " + acuity.getId());
                            }
                        }else{
                            Log.e(TAG, "newInstallation:: acuity empty");
                        }
                    }
                }else{
                    if (Util.DEBUG) {
                        Log.e(TAG, "newInstallation:: acuities empty");
                    }
                }
            }
        } ).start();
        deleteAcuitiesTable();
    }

    public Acuity getLastId(int userId) {
            if (Util.DEBUG) {
                Log.i(Util.LOG_TAG_DB, "get last acuity by user id");
            }
            return gvDatabase.acuityDao().getLastAcuityByUserId(userId);
    }

    public void upDateInServer(int appID) {
        if (Util.DEBUG) {
            Log.i(Util.LOG_TAG_DB, "upDate In Server for appId");
        }
        gvDatabase.acuityDao().upDateInServerByAppId(appID);
    }
}
