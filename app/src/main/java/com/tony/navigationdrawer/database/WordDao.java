package com.tony.navigationdrawer.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.tony.navigationdrawer.model.Word;

import java.util.List;

@Dao
public interface WordDao {

    @Insert
    void insertAll(List<Word> words);

    @Insert
    void insertAll(Word... words);

    @Insert
    long insertWord(Word user);

    @Query("SELECT COUNT(*) from words")
    int countWords();

    @Query("SELECT * FROM words ORDER BY name")
    List<Word> getAll();

    @Query("SELECT * FROM words WHERE name = :name")
    Word findWordByName(String name);

    @Query("DELETE FROM words WHERE  name = :name")
    abstract void deleteByName(String name);

    @Query("DELETE FROM words WHERE  id = :id")
    abstract void deleteById(long id);

//    @Query("SELECT * FROM users JOIN userdetails ON users.id = userdetails.userId")
//    void joinTwoTable();



}
