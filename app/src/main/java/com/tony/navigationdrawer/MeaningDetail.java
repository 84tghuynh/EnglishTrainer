package com.tony.navigationdrawer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;

import com.tony.navigationdrawer.database.AppDatabase;
import com.tony.navigationdrawer.events.WordMeaningEvent;
import com.tony.navigationdrawer.model.WordMeaning;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MeaningDetail extends AppCompatActivity {
    private AppDatabase db;
    private WordMeaning mWordMeaning;

    private TextToSpeech mTTS;
    private ImageButton mButtonSayLearn;
    private String word;


    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = AppDatabase.getInstance(this);
        setContentView(R.layout.activity_meaning_detail);

        mButtonSayLearn = findViewById(R.id.btnSayLearn);

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if( status == TextToSpeech.SUCCESS){
                    int result = mTTS.setLanguage(Locale.CANADA);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.d("Tony-Say","Language not Supported");
                    } else {
                        mButtonSayLearn.setEnabled(true);
                    }
                }else{
                    Log.d("Tony-Say", "Initialization failed");
                }
            }
        });

        mButtonSayLearn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        WebView wv;
        wv = (WebView) findViewById(R.id.wvDetails);
        WebSettings webSettings = wv.getSettings();
        Intent intent = getIntent();
        long wordId = intent.getLongExtra("wordId", -1);
             word = intent.getStringExtra("word");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                WordMeaning wm = db.wordMeaningDao().findWordMeaningByWordId(wordId);

                Log.d("Tony-delete","Thread Description of word id: " + wordId + " : " + wm.getMeaning());

                EventBus.getDefault().post(new WordMeaningEvent(wm));

            }
        });

        EventBus.getDefault().register(this);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void wordMeaningEventHandler(WordMeaningEvent event) {
        WordMeaning wordMeaning = event.getWordMeaning();
        WebView wv;
        wv = (WebView) findViewById(R.id.wvDetails);
        wv.loadData(wordMeaning.getMeaning(), "text/html; charset=utf-8", "utf-8");
        Log.d("Tony-delete","Event Description of user id: " + wordMeaning.getWordId() + " : " + wordMeaning.getMeaning());
    }

    private void speak(){
        String text = this.word;//tvText.getText().toString();
        float pitch = 1;
        float speed = 1;

        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);

//        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);

        switch(mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null)){

            case TextToSpeech.SUCCESS:
                Log.d("Tony-say", "Success" );
                break;
        }

    }

    @Override
    protected void onDestroy() {
        AppDatabase.destroyInstance();
        EventBus.getDefault().unregister(this);

        if( mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {

        AppDatabase.destroyInstance();
        EventBus.getDefault().unregister(this);

        if( mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onPause();
    }
}