package com.tony.navigationdrawer.events;

import com.tony.navigationdrawer.model.Word;

import java.util.List;

public class WordsEvent {
    private List<Word> wordList;

    public WordsEvent(List<Word> wordList) {
        this.wordList = wordList;
    }

    public List<Word> getWordList() {
        return this.wordList;
    }

    public void setWordList(List<Word> wordList) {
        this.wordList = wordList;
    }
}
