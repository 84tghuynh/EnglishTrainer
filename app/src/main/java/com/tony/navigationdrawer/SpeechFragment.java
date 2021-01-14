package com.tony.navigationdrawer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class SpeechFragment extends Fragment  {

    protected static final int RESULT_SPEECH = 555;
    private ImageButton btnSpeak;
    private EditText tvText;
    private String result;

    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private  boolean mListening = false;

    private TextToSpeech mTTS;
    private ImageButton mButtonSaySpeech, mButtonDeleteSpeech;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_speech,container,false);

        tvText = v.findViewById(R.id.tvText);
        btnSpeak = v.findViewById(R.id.btnSpeak);

        mButtonSaySpeech = v.findViewById(R.id.btnSaySpeech);
        mButtonDeleteSpeech = v.findViewById(R.id.btnDelSpeech);

        mTTS = new TextToSpeech(SpeechFragment.this.getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if( status == TextToSpeech.SUCCESS){
                    int result = mTTS.setLanguage(Locale.CANADA);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.d("Tony-Say","Language not Supported");
                    } else {
                        mButtonSaySpeech.setEnabled(true);
                    }
                }else{
                    Log.d("Tony-Say", "Initialization failed");
                }
            }
        });

        mButtonSaySpeech.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        mButtonDeleteSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result="";
                tvText.setText(result);
            }
        });
        result = "";
        //turnOnMic();
        SetupSpeech(v);

        return v;
    }

    private void speak(){
        String text = tvText.getText().toString();
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
    public void turnOnMic() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        Log.d("Tony-speech","Default Locale: " + Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"us-US");
        try{
            startActivityForResult(intent, RESULT_SPEECH);
            //tvText.setText("");
        }catch (ActivityNotFoundException e){
            // Toast.makeText(this.getApplicationContext(),"Your device doesn't support microphone",Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_SPEECH) {
            if (resultCode == RESULT_OK && data != null) {
                Log.d("Tony-speech", "onActivityResult");
               // ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                //result = result + " " + text.get(0);
                //tvText.setText(result);
            }
            //turnOnMic();
        }
    }

    private void SetupSpeech(View v){
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer( SpeechFragment.this.getContext());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                SpeechFragment.this.getContext().getPackageName());

       // mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,100000);

        SpeechFragment.SpeechRecognitionListener listener = new SpeechFragment.SpeechRecognitionListener();
        mSpeechRecognizer.setRecognitionListener(listener);

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mListening){
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                    btnSpeak.setBackgroundResource(R.drawable.ic_microphone_foreground);
                    mListening = true;
                    tvText.setText("");
                }else{
                    mListening = false;
                    mSpeechRecognizer.stopListening();
                    Log.d("Tony-speech", "Onclick and mListening = false");
                    btnSpeak.setBackgroundResource(R.drawable.ic_mic_none);

                }
                  // Toast.makeText(SpeechFragment.this.getContext(), "Oops - Speech Recognition Not Supported!",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void turnOffSpeechOnErrors()
    {
        if(mListening) {
            mSpeechRecognizer.stopListening();
            mListening = false;
        }
        Log.d("Tony-speech", "turnOffSpeechOnErrors, mListening is assigned = false");
        btnSpeak.setBackgroundResource(R.drawable.ic_mic_none);
    }

    @Override
    public void onPause() {
        mSpeechRecognizer.stopListening();
      //  mSpeechRecognizer.destroy();
        Log.d("Tony-speech", "onPause - SpeechFragment");

        if( mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d("Tony-speech", "onDestroy - SpeechFragment");
        mSpeechRecognizer.stopListening();
        mSpeechRecognizer.destroy();

        if( mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }
    protected class SpeechRecognitionListener implements RecognitionListener
    {

        @Override
        public void onBeginningOfSpeech()
        {
            Log.d("Tony-speech", "onBeginingOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
           // Log.d("Tony-speech buffer", buffer.toString());

        }

        @Override
        public void onEndOfSpeech()
        {
            Log.d("Tony-speech", "onEndOfSpeech");
        }

        @Override
        public void onError(int error)
        {
            Log.d("Tony-speech", "error: " + error);
             switch(error){
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: //1
                    Log.d("Tony-speech", "Error ERROR_NETWORK_TIMEOUT - " + error );
                    turnOffSpeechOnErrors();
                    break;
                case SpeechRecognizer.ERROR_NETWORK: //2
                    Log.d("Tony-speech", "Error ERROR_NETWORK - " + error );
                    turnOffSpeechOnErrors();
                    break;
                case SpeechRecognizer.ERROR_AUDIO: //3
                    Log.d("Tony-speech", "Error ERROR_NETWORK Audio recording error - " + error );
                    turnOffSpeechOnErrors();
                    break;
                case SpeechRecognizer.ERROR_SERVER: //4
                    Log.d("Tony-speech", "Error ERROR_SERVER Server sends error status. - " + error );
                    turnOffSpeechOnErrors();
                    break;
                case SpeechRecognizer.ERROR_CLIENT: //5
                    Log.d("Tony-speech", "Error ERROR_CLIENT Other client side errors. - " + error );
                    turnOffSpeechOnErrors();
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: //6
                    Log.d("Tony-speech", "Error ERROR_SPEECH_TIMEOUT No speech input - " + error );
//                    btnSpeak.setBackgroundResource(R.drawable.ic_mic_none);
                    //result = "";
                    //mListening = false;
                    turnOffSpeechOnErrors();
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH: //7
                    Log.d("Tony-speech", "Error ERROR_NO_MATCH No recognition result matched - " + error );
                    if(mListening) mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: //8
                    Log.d("Tony-speech", "Error ERROR_RECOGNIZER_BUSY RecognitionService busy. - " + error );
                    turnOffSpeechOnErrors();
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: //9
                    Log.d("Tony-speech", "Error ERROR_INSUFFICIENT_PERMISSIONS Insufficient permissions - " + error );
                    turnOffSpeechOnErrors();
                    break;
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params){ }

        @Override
        public void onPartialResults(Bundle partialResults){}

        @Override
        public void onReadyForSpeech(Bundle params)
        {
            btnSpeak.setBackgroundResource(R.drawable.ic_microphone_foreground);
            Log.d("Tony-speech", "onReadyForSpeech");
        }

        @Override
        public void onResults(Bundle results)
        {
            Log.d("Tony-speech", "onResults");
            ArrayList<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String thread  = text.get(0) + " ";
            result = result + thread + " ";
            tvText.setText(result);
            //tvText.append(thread);
            tvText.setSelection(result.length()); // move the cursor to the end of multlines edittext, auto scroll
            Log.d("Tony-speech", "onResults:::" + result);
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        }

        @Override
        public void onRmsChanged(float rmsdB){ }
    }
}
