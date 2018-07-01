package com.example.efhemo.journalapp.database;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.efhemo.journalapp.model.TaskEntry;

import java.util.List;

@Dao //access or write to db
public interface TaskDao {

    @Query("SELECT * FROM journal_table ORDER BY id DESC")
    LiveData<List<TaskEntry>> loadJournalTask();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertJournalTask(TaskEntry entry);


    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateJournalTask(TaskEntry entry);

    @Delete
    void deleteJournalTask(TaskEntry entry);


    @Query("SELECT * FROM journal_table WHERE id = :id")
    LiveData<TaskEntry> loadTaskById(int id);

}
