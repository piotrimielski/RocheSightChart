package com.givevision.rochesightchart.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "acuities", indices = {@Index("user_id")})
public class Acuity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "contrast")
    private int contrast; //modified boolean to int in version 2. (0-1-2)

    @ColumnInfo(name = "duration")
    private int duration; //added in version 3.

    @ColumnInfo(name = "left_eye_first")
    private String leftEyeFirst; //added in version 3.

    @ColumnInfo(name = "right_eye_first")
    private String rightEyeFirst; //added in version 3.

    @ColumnInfo(name = "left_eye")
    private String leftEye;

    @ColumnInfo(name = "right_eye")
    private String rightEye;

    @ColumnInfo(name = "in_server")
    private boolean inServer;

    @ColumnInfo(name = "log")
    private int log; //added in version 4.

    @ColumnInfo(name = "log_calibration")
    private int logCal; //added in version 5.

    @ColumnInfo(name = "log_test")
    private int logTest; //added in version 5.

    @ColumnInfo(name = "left_log")
    private int leftLog; //added in version 6.

    @ColumnInfo(name = "left_log_calibration")
    private int leftLogCal; //added in version 6.

    @ColumnInfo(name = "left_log_test")
    private int leftLogTest; //added in version 6.

    @ColumnInfo(name = "right_log")
    private int rightLog; //added in version 6.

    @ColumnInfo(name = "right_log_calibration")
    private int rightLogCal; //added in version 6.

    @ColumnInfo(name = "right_log_test")
    private int rightLogTest; //added in version 6.

    @ColumnInfo(name = "created_at")
    @TypeConverters({TimestampConverter.class})
    private Date createdAt;


    @ColumnInfo(name = "modified_at")
    @TypeConverters({TimestampConverter.class})
    private Date modifiedAt;


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getContrast() {
        return contrast;
    }
    public void setContrast(int contrast) {
        this.contrast = contrast;
    }

    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLeftEyeFirst() {
        return leftEyeFirst;
    }
    public void setLeftEyeFirst(String leftEyeFirst) {
        this.leftEyeFirst = leftEyeFirst;
    }

    public String getRightEyeFirst() {return rightEyeFirst;}
    public void setRightEyeFirst(String rightEyeFirst) {
        this.rightEyeFirst = rightEyeFirst;
    }

    public String getLeftEye() {
        return leftEye;
    }
    public void setLeftEye(String leftEye) {
        this.leftEye = leftEye;
    }

    public String getRightEye() {return rightEye;}
    public void setRightEye(String rightEye) {
        this.rightEye = rightEye;
    }

    public boolean getInServer() {
        return inServer;
    }
    public void setInServer(boolean inServer) {
        this.inServer = inServer;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public int getLog() {return log;}
    public void setLog(int log) {
        this.log = log;
    }
    public int getLogCal() {return logCal;}
    public void setLogCal(int logCal) {
        this.logCal = logCal;
    }
    public int getLogTest() {return logTest;}
    public void setLogTest(int logCalTest) {
        this.logTest = logCalTest;
    }
    public int getLeftLog() {return leftLog;}
    public void setLeftLog(int leftLog) {
        this.leftLog = leftLog;
    }
    public int getLeftLogCal() {return leftLogCal;}
    public void setLeftLogCal(int leftLogCal) {
        this.leftLogCal = leftLogCal;
    }
    public int getLeftLogTest() {return leftLogTest;}
    public void setLeftLogTest(int leftLogTest) {
        this.leftLogTest = leftLogTest;
    }
    public int getRightLog() {return rightLog;}
    public void setRightLog(int rightLog) {
        this.rightLog = rightLog;
    }
    public int getRightLogCal() {return rightLogCal;}
    public void setRightLogCal(int rightLogCal) {
        this.rightLogCal = rightLogCal;
    }
    public int getRightLogTest() {return rightLogTest;}
    public void setRightLogTest(int rightLogTest) {
        this.rightLogTest = rightLogTest;
    }
}
