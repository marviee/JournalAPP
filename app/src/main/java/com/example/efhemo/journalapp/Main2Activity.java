package com.example.efhemo.journalapp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;

import com.example.efhemo.journalapp.database.AppDatabase;
import com.example.efhemo.journalapp.database.AppExecutors;
import com.example.efhemo.journalapp.database.MainViewModel;
import com.example.efhemo.journalapp.model.TaskEntry;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class Main2Activity extends AppCompatActivity implements RCAdapter.OneItemClickListener {

    private static final String LOG_TAG = Main2Activity.class.getSimpleName();
    public static final String IDENTIFY = "identi";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String DAY = "day";


    RecyclerView recyclerView;
    RCAdapter adapter;
    LiveData<List<TaskEntry>> entryList;
    AppDatabase mDb;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        myToolbar.setTitle(getString(R.string.journal_title));

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // attaching bottom sheet behaviour - hide / show on scroll
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) navigation.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationBehavior());

        //recyclerview setup
        recyclerView = findViewById(R.id.rc_view);
        adapter = new RCAdapter(this, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        /*Start Log out configuration*/
        mAuth = FirebaseAuth.getInstance();

        //setUp listener to obderve if log out or log in
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(Main2Activity.this, MainActivity.class));
                }
            }
        };

        //Instantiate database
        mDb = AppDatabase.getsInstance(getApplicationContext());


        /*Item Touch*/
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        recyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {

                // Implement swipe to delete
                AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<TaskEntry> tasks = adapter.getTasks();
                        mDb.taskDao().deleteJournalTask(tasks.get(position));
                    }
                });

            }
        }).attachToRecyclerView(recyclerView); /*itemTouch end*/


        retrieveTask();
    }

    /*
     * press back button twice will close the application
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    //get data from database
    private void retrieveTask() {

        //VIEWMODEL works in the worker thread/ OBSERVES DATA CHANGES
        MainViewModel mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        entryList = mainViewModel.getTask();
        entryList.observe(this, new Observer<List<TaskEntry>>() {
            @Override
            public void onChanged(@Nullable List<TaskEntry> popularEntries) {
                adapter.setTasks(popularEntries);
            }
        });
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    /*
     * package intent for each item*/
    @Override
    public void onOneItemClickListener(int itemId, String title, String desc, String date) {

        Intent intent = new Intent(Main2Activity.this, UpdateTaskActivity.class);

        //package those field in  bundle for intent
        Bundle bundle = new Bundle();
        bundle.putInt(IDENTIFY, itemId);
        bundle.putString(NAME, title);
        bundle.putString(DESCRIPTION, desc);
        bundle.putString(DAY, date);

        intent.putExtra(UpdateTaskActivity.INTENT_KEY, bundle);

        startActivity(intent);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.add_journal:
                    Intent intent = new Intent(getBaseContext(), AddJournalActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.cloud:
                    Intent intent1 = new Intent(Main2Activity.this, CloudActivity.class);
                    startActivity(intent1);
                    return true;
                case R.id.log_out:
                    signOut(); // listener will listen to startup for log out activity
                    return true;
            }
            return false;
        }
    };

}
