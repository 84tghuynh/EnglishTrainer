package com.tony.navigationdrawer.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Words")
public class Word {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo
    private String name;

    public int getTimesLookup() {
        return timesLookup;
    }

    public void setTimesLookup(int timesLookup) {
        this.timesLookup = timesLookup;
    }

    @ColumnInfo
    private int timesLookup;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
