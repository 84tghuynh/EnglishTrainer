package com.tony.navigationdrawer;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class SayFragment extends Fragment {
    private TextToSpeech mTTS;


    private EditText mEditTextSay;
    private ImageButton mButtonSay;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_say,container,false);

        mEditTextSay = v.findViewById(R.id.etSay);
        mButtonSay = v.findViewById(R.id.btnSay);

        mTTS = new TextToSpeech(SayFragment.this.getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if( status == TextToSpeech.SUCCESS){
                    int result = mTTS.setLanguage(Locale.CANADA);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.d("Tony-Say","Language not Supported");
                    } else {
                        mButtonSay.setEnabled(true);
                    }
                }else{
                    Log.d("Tony-Say", "Initialization failed");
                }
            }
        });

        mButtonSay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        return v;
    }

    private void speak(){
        String text = mEditTextSay.getText().toString();
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
    public void onDestroy() {

        if( mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if( mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onPause();
    }
}
