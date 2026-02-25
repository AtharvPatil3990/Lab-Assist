package com.android.labassist.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.database.entities.DeviceEntity;
import com.android.labassist.database.entities.LabEntity;

import java.util.List;

@Dao
public interface LabAssistDao {

    // ================= LABS =================
    // REPLACE ensures that if the server updates a lab's name, Room overwrites the old one
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLabs(List<LabEntity> labs);

    @Query("SELECT * FROM labs ORDER BY labName ASC")
    LiveData<List<LabEntity>> getAllLabs();

    // ================= DEVICES =================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDevices(List<DeviceEntity> devices);

    // Dynamic query to populate the "Select Device" dropdown based on chosen Lab
    @Query("SELECT * FROM devices WHERE labId = :labId ORDER BY deviceName ASC")
    LiveData<List<DeviceEntity>> getDevicesForLab(String labId);

    // ================= COMPLAINTS =================

    @Transaction  // THE FIX: A Transaction ensures LiveData only emits ONCE
    default void syncComplaints(List<ComplaintEntity> newComplaints) {
        clearComplaints();
        insertComplaints(newComplaints);
    }
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertComplaints(List<ComplaintEntity> complaints);

    // For Technicians: Get active complaints assigned to them or their labs
    @Query("SELECT * FROM complaints WHERE status != 'RESOLVED' ORDER BY priority DESC, createdAt ASC")
    LiveData<List<ComplaintEntity>> getActiveComplaints();

    // Local optimistic update (Updates UI instantly before server confirms)
    @Query("UPDATE complaints SET status = :newStatus WHERE id = :complaintId")
    void updateComplaintStatus(String complaintId, String newStatus);

    // Clear everything (Call this when user logs out!)
    @Query("DELETE FROM labs")
    void clearLabs();

    @Query("DELETE FROM devices")
    void clearDevices();

    @Query("DELETE FROM complaints")
    void clearComplaints();



    // 📊 DASHBOARD STATS
    @Query("SELECT COUNT(*) FROM complaints")
    LiveData<Integer> getTotalComplaintsCount();

    // Assuming your pending statuses are OPEN and IN_PROGRESS
    @Query("SELECT COUNT(*) FROM complaints WHERE status = 'OPEN' OR status = 'IN_PROGRESS'")
    LiveData<Integer> getPendingComplaintsCount();

    @Query("SELECT COUNT(*) FROM complaints WHERE status = 'RESOLVED'")
    LiveData<Integer> getResolvedComplaintsCount();

//    For LabsEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllLabs(List<LabEntity> labs);

    @Query("DELETE FROM labs")
    void deleteAllLabs();


    // For DeviceEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllDevices(List<DeviceEntity> devices);

    // Optional: Clear all devices
    @Query("DELETE FROM devices")
    void deleteAllDevices();

    @Query("SELECT * FROM labs")
    LiveData<List<LabEntity>> getAllLabsLive();

    @Query("SELECT * FROM devices WHERE labId = :labId")
    LiveData<List<DeviceEntity>> getDevicesForLabLive(String labId);

    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    LiveData<List<ComplaintEntity>> getAllComplaintsHistory();
}