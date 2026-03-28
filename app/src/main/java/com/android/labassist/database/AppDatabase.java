package com.android.labassist.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.database.entities.DepartmentEntity;
import com.android.labassist.database.entities.DeviceEntity;
import com.android.labassist.database.entities.LabEntity;
import com.android.labassist.database.entities.NoteEntity;
import com.android.labassist.database.entities.NotificationEntity;
import com.android.labassist.database.entities.StudentEntity;
import com.android.labassist.database.entities.TechnicianEntity;

@Database(
        entities = {LabEntity.class, DeviceEntity.class, ComplaintEntity.class, NoteEntity.class, DepartmentEntity.class, TechnicianEntity.class, StudentEntity.class, NotificationEntity.class},
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
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration(false) // Wipes DB if version changes (good for early dev)
                            .build();
                }
            }
        }
        return instance;
    }
}