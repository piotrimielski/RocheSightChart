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
    private boolean contrast;

    @ColumnInfo(name = "left_eye")
    private String leftEye;

    @ColumnInfo(name = "right_eye")
    private String rightEye;

    @ColumnInfo(name = "in_server")
    private boolean inServer;

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

    public boolean getContrast() {
        return contrast;
    }

    public void setContrast(boolean contrast) {
        this.contrast = contrast;
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
}
