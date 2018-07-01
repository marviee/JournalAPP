package com.example.efhemo.journalapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.efhemo.journalapp.model.TaskEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class CloudActivity extends AppCompatActivity {

    // Access a Cloud Firestore instance from your Activity
    private static final String LOG_TAG = CloudActivity.class.getSimpleName();
    RCAdapterCloud rcAdapter;

    List<TaskEntry> onLineList = new ArrayList<>();
    private ListenerRegistration firestoreListener;


    private FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser myUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);

        //toolbar setup
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_cloud);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle(getString(R.string.cloud_title));

        //recyclerView setup
        rcAdapter = new RCAdapterCloud(this );
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.online_data_rc);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(rcAdapter);

        //START initialize_auth
        mAuth = FirebaseAuth.getInstance();
        myUser= FirebaseAuth.getInstance().getCurrentUser();

        //register listener for firebase authentication
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(CloudActivity.this, "you need to sign in", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CloudActivity.this, MainActivity.class));
                }
            }
        };

        /*item Touch*/
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        recyclerView.addItemDecoration(decoration);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT |ItemTouchHelper.UP |ItemTouchHelper.DOWN ) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                List<TaskEntry> tasks = rcAdapter.getTasks();

                //swipe to delete item in the cloud
                deleteCloudItem(position, tasks.get(position).getID() );

            }
        }).attachToRecyclerView(recyclerView);
    }

    //setup to delete item selected for delete according to element ID
    private void deleteCloudItem(final int pos, int taskId){
        FirebaseFirestore.getInstance().collection(myUser.getEmail()+"")
                .document(taskId+"")
                .delete().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onLineList.remove(pos);
                Toast.makeText(getApplication(), "To be deleted", Toast.LENGTH_SHORT).show();

            }
        });
    }

    /*
    * authentication listener must be implemented in the onStart to observe changes
     */
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        firestoreListener = FirebaseFirestore.getInstance()
                .collection(myUser.getEmail()+"").
                        addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(e != null){
                            Log.d(LOG_TAG, "listen error ", e);
                        }

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d(LOG_TAG, document.getId() + " => " + document.getData());

                            TaskEntry taskEntry = document.toObject(TaskEntry.class);
                            Log.d(LOG_TAG, onLineList.toString());
                            //taskEntry.setID();
                            onLineList.add(taskEntry);
                            rcAdapter.setTasks(onLineList);
                        }
                    }
                });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * authentication listener is destroy to take care of memory
    * */
    @Override
    protected void onStop() {
        super.onStop();
        firestoreListener.remove();
    }
}
