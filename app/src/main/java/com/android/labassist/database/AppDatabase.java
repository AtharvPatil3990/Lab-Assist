package com.android.labassist.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.database.entities.DeviceEntity;
import com.android.labassist.database.entities.LabEntity;
import com.android.labassist.database.entities.NoteEntity;

@Database(
        entities = {LabEntity.class, DeviceEntity.class, ComplaintEntity.class, NoteEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract LabAssistDao labAssistDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "lab_assist_database")
                            .fallbackToDestructiveMigration(false) // Wipes DB if version changes (good for early dev)
                            .build();
                }
            }
        }
        return instance;
    }
}