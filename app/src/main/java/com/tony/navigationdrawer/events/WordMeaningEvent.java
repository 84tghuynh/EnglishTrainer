package com.tony.navigationdrawer.events;

import com.tony.navigationdrawer.model.WordMeaning;

public class WordMeaningEvent {
    private WordMeaning wordMeaning;

    public WordMeaningEvent(WordMeaning wordMeaning) {
        this.wordMeaning = wordMeaning;
    }

    public WordMeaning getWordMeaning() {
        return wordMeaning;
    }

    public void setWordMeaning(WordMeaning wordMeaning) {
        this.wordMeaning = wordMeaning;
    }
}
