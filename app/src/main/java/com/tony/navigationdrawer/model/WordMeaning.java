package com.tony.navigationdrawer.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(tableName = "wordmeanings",
        foreignKeys = {
                @ForeignKey(entity = Word.class,
                        parentColumns = "id",
                        childColumns = "wordId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "wordId")
        })
public class WordMeaning {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long wordId;
    private String meaning;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }
}
