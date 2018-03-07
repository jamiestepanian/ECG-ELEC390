package com.elec390.teamb.ecg;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Room entity
 */

@Entity(tableName = "session")
public class SessionEntity {
    @PrimaryKey (autoGenerate = true)
    @ColumnInfo(name = "sessionid")
    public int sId;

    @ColumnInfo(name = "sessionstart")
    @TypeConverters({DateTypeConverter.class})
    @NonNull
    public Date mSessionStart;

    @ColumnInfo(name = "sessionend")
    @TypeConverters({DateTypeConverter.class})
    @NonNull
    public Date mSessionEnd;

    @ColumnInfo(name = "sessioncomments")
    public String mSessionCommentsFileName;

    @ColumnInfo(name = "sessiondata")
    public String mSessionDataFileName;

    public SessionEntity(Date mSessionStart, Date mSessionEnd,
                         String mSessionCommentsFileName, String mSessionDataFileName) {
        this.mSessionStart = mSessionStart;
        this.mSessionEnd = mSessionEnd;
        this.mSessionCommentsFileName = mSessionCommentsFileName;
        this.mSessionDataFileName = mSessionDataFileName;
    }
}