package com.example.efhemo.journalapp.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.efhemo.journalapp.model.TaskEntry;

import java.util.List;

public class MainViewModel extends AndroidViewModel {



    private LiveData<List<TaskEntry>> task;


    public MainViewModel(@NonNull Application application) {
        super(application);

        /*LiveData works outside of the main Thread and therefore we dont need the Executors*/
        AppDatabase mDb= AppDatabase.getsInstance(this.getApplication());

        //we want it to survive through rotation changes when accessing data i.e viewModel
        task = mDb.taskDao().loadJournalTask();
    }

    public LiveData<List<TaskEntry>> getTask(){
        return task;
    }

}
