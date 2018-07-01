package com.example.efhemo.journalapp.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "journal_table", indices = {@Index(value = {"journalTitle", "journalDescription"}, unique = true)})
public class TaskEntry {

    @PrimaryKey(autoGenerate = true)
    private int ID;

    private String journalTitle;
    private String journalDescription;
    private String journalDate;

    @Ignore
    public TaskEntry() {
    }

    public TaskEntry(String journalTitle, String journalDescription, String journalDate, int ID) {
        this.journalTitle = journalTitle;
        this.journalDescription = journalDescription;
        this.journalDate = journalDate;
        this.ID = ID;
    }

    @Ignore
    public TaskEntry(String journalTitle, String journalDescription, String journalDate) {
        this.journalTitle = journalTitle;
        this.journalDescription = journalDescription;
        this.journalDate = journalDate;
    }

    public String getJournalTitle() {
        return journalTitle;
    }

    public String getJournalDescription() {
        return journalDescription;
    }

    public String getJournalDate() {
        return journalDate;
    }

    public void setJournalTitle(String journalTitle) {
        this.journalTitle = journalTitle;
    }

    public void setJournalDescription(String journalDescription) {
        this.journalDescription = journalDescription;
    }

    public void setJournalDate(String journalDate) {
        this.journalDate = journalDate;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {

        return ID;
    }
}
