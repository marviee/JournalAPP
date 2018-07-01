package com.example.efhemo.journalapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.efhemo.journalapp.model.TaskEntry;

import java.util.List;

public class RCAdapterCloud extends RecyclerView.Adapter<RCAdapterCloud.RCCloudViewHolder>{

    List<TaskEntry> mTaskEntries;
    Context context;

    public RCAdapterCloud(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RCCloudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
        return new RCCloudViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RCCloudViewHolder holder, int position) {
        TaskEntry taskEntry = mTaskEntries.get(holder.getAdapterPosition());
        holder.textViewTitle.setText(taskEntry.getJournalTitle());
        holder.textViewDescription.setText(taskEntry.getJournalDescription());
        holder.textViewDate.setText(taskEntry.getJournalDate());
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

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if (mTaskEntries == null) {
            return 0;
        }
        return mTaskEntries.size();
    }

    public class RCCloudViewHolder extends RecyclerView.ViewHolder {

        /*Viewholder*/
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewDate;
        ImageView imageViewpopmenu;

        RCCloudViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.title_text);
            textViewDescription = itemView.findViewById(R.id.description_text);
            textViewDate = itemView.findViewById(R.id.date_text);
            itemView.findViewById(R.id.pop_up_menu).setVisibility(View.GONE);
        }

    }
}
