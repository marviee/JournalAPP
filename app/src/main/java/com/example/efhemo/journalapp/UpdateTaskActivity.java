package com.example.efhemo.journalapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.efhemo.journalapp.database.AppDatabase;
import com.example.efhemo.journalapp.database.AppExecutors;
import com.example.efhemo.journalapp.model.TaskEntry;

public class UpdateTaskActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText writeUpdate;
    private EditText titleUpdate;
    Button buttonSave;

    public static final String INTENT_KEY = "intent_key";
    // Extra for the task ID to be received after rotation
    public static final String SAVEINSTANCE_KEY = "instance_key";

    AppDatabase mDb;
    Intent intent;

    Bundle bundle;
    private int itemId;
    private String day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_task);

        //initialize soem view here
        intiliazeViews();
        buttonSave = findViewById(R.id.update_button);
        buttonSave.setOnClickListener(this);

        mDb = AppDatabase.getsInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVEINSTANCE_KEY)) {
            bundle = savedInstanceState.getBundle(SAVEINSTANCE_KEY);
        }

        intent = getIntent();
        bundle = intent.getBundleExtra(INTENT_KEY);

        if (bundle != null) {

            // populate the UI and get some other value
            titleUpdate.setText(bundle.getString("name"));
            writeUpdate.setText(bundle.getString("description"));
            itemId = bundle.getInt("identi");
            day = bundle.getString("day");

        }

    }

    void intiliazeViews (){
        titleUpdate = findViewById(R.id.edit_title_update);
        writeUpdate = findViewById(R.id.edit_write_update);
    }

    //The intent data sent should survive on rotation changes TODO
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle(SAVEINSTANCE_KEY, bundle);
        super.onSaveInstanceState(outState);
    }

    //setup onCLick for update button
    @Override
    public void onClick(View view) {

        String myTitle = titleUpdate.getText().toString();
        String myWrite = writeUpdate.getText().toString();

        if (myTitle.isEmpty() || myWrite.isEmpty()) {
            //Todo: put Stack bar instead of toast
            Toast.makeText(this, "All field must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        final TaskEntry task = new TaskEntry(myTitle, myWrite, day);
        AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                task.setID(itemId);
                mDb.taskDao().updateJournalTask(task);
                finish();
            }
        });


    }


}
