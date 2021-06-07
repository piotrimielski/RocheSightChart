package com.givevision.rochesightchart.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * @author Piotr
 */
@Database(entities = {Acuity.class}, version = 6, exportSchema = false)
@TypeConverters({TimestampConverter.class})
public abstract class GiveVisionDatabase extends RoomDatabase {
    public abstract AcuityDao acuityDao();

}
