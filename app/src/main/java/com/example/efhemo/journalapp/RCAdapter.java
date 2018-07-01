package com.example.efhemo.journalapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.efhemo.journalapp.database.AppDatabase;
import com.example.efhemo.journalapp.database.AppExecutors;
import com.example.efhemo.journalapp.model.TaskEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RCAdapter extends RecyclerView.Adapter<RCAdapter.RCViewHolder>  {

    private static final String LOG_TAG = RCAdapter.class.getSimpleName();
    // Member variable to handle item clicks
    final private OneItemClickListener mItemClickListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<TaskEntry> mTaskEntries;
    private Context context;

    private String emailIntent;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public RCAdapter(Context context, OneItemClickListener oneItemClickListener){
        this.context = context;
        this.mItemClickListener = oneItemClickListener;
    }

    @NonNull
    @Override
    public RCViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
        return new RCViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RCViewHolder holder, int position) {

        TaskEntry taskEntry = mTaskEntries.get(holder.getAdapterPosition());
        holder.textViewTitle.setText(taskEntry.getJournalTitle());
        holder.textViewDescription.setText(taskEntry.getJournalDescription());
        holder.textViewDate.setText(taskEntry.getJournalDate());
        final ImageView button = holder.imageViewpopmenu;

        //configure for clicking image pop up
        holder.imageViewpopmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), button);
                popupMenu.inflate(R.menu.popup_menu);

                //Popup menu for each Item Start
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.delete:
                                final AppDatabase mDb = AppDatabase.getsInstance(context);
                                AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        int position = holder.getAdapterPosition();
                                        //List<TaskEntry> tasks = adapter.getTasks();
                                        mDb.taskDao().deleteJournalTask(mTaskEntries.get(position));
                                    }
                                });
                                return true;
                            case R.id.push_online:
                                setFireStoreDoc(holder.getAdapterPosition());

                                return true;
                        }

                        return false;
                    }
                });

                popupMenu.show();
            }
        });

    }

    private void setFireStoreDoc(final int pos){

        TaskEntry  entry = mTaskEntries.get(pos);
        int id = entry.getID();

        if(user.getEmail() == null){
            Toast.makeText(context, "you need to sign in with your email", Toast.LENGTH_SHORT).show();
        }else{
            emailIntent = user.getEmail();
        }

        db.collection(emailIntent).document(id+"")
                .set(entry)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(LOG_TAG, "DocumentSnapshot successfully written!");
                        Toast.makeText(context, "Doc added to cloud for position "+pos, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG_TAG, "Error writing document", e);
                        Toast.makeText(context, "Error pushing to Cloud", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * When data changes, this method updates the list of mTaskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setTasks(List<TaskEntry> taskEntries) {
        mTaskEntries = taskEntries;
        notifyDataSetChanged();
    }

    public List<TaskEntry> getTasks() {
        return mTaskEntries;
    }

    @Override
    public int getItemCount() {
        if (mTaskEntries == null) {
            return 0;
        }
        return mTaskEntries.size();
    }



    public interface OneItemClickListener {
        void onOneItemClickListener(int itemId, String title, String desc, String date);
    }



   public class RCViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener{

        /*Viewholder*/
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewDate;
        ImageView imageViewpopmenu;
        RCViewHolder(final View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.title_text);
            textViewDescription = itemView.findViewById(R.id.description_text);
            textViewDate = itemView.findViewById(R.id.date_text);
            imageViewpopmenu = itemView.findViewById(R.id.pop_up_menu);
            itemView.setOnClickListener(this);


        }

       @Override
       public void onClick(View view) {
           TaskEntry taskEntry = mTaskEntries.get(getAdapterPosition()); //id by element in list
           int myId = taskEntry.getID();
           String clikedTitle = taskEntry.getJournalTitle();
           String clickedDescription = taskEntry.getJournalDescription();
           String clickedDate = taskEntry.getJournalDate();

           mItemClickListener.onOneItemClickListener(myId, clikedTitle, clickedDescription, clickedDate);
       }
   }
}
