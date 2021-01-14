package com.tony.navigationdrawer.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.tony.navigationdrawer.model.WordMeaning;

import java.util.List;

@Dao
public interface WordMeaningDao {
    @Insert
    void insertAll(List<WordMeaning> wordMeanings);

    @Insert
    void insertAll(WordMeaning... wordMeaning);

    @Query("SELECT COUNT(*) from wordmeanings")
    int countWordMeanings();

    @Query("SELECT * FROM wordmeanings ORDER BY id")
    List<WordMeaning> getAll();

    @Query("SELECT * FROM wordmeanings WHERE wordId = :wordId")
    WordMeaning findWordMeaningByWordId(long wordId);

    @Query("DELETE FROM wordmeanings WHERE  id = :id")
    abstract void deleteById(long id);
}
