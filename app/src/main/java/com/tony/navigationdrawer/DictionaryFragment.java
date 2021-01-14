package com.tony.navigationdrawer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tony.navigationdrawer.database.AppDatabase;
import com.tony.navigationdrawer.events.WordsEvent;
import com.tony.navigationdrawer.model.Word;
import com.tony.navigationdrawer.model.WordMeaning;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DictionaryFragment extends Fragment implements MainActivity.DataFromActivityToFragment {

    private WebView webView;
    private String word;
    private String wordMeaning;
    private ImageButton btnSave;

    private TextToSpeech mTTS;
    private ImageButton mButtonSayDic;



    private AppDatabase db;
    private Executor executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dictionary,container,false);
        webView = v.findViewById(R.id.wv_search_result);
        btnSave = v.findViewById(R.id.btnSave);

        mButtonSayDic = v.findViewById(R.id.btnSayDic);
        db = AppDatabase.getInstance(getContext());

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Word word = new Word();
                word.setName(DictionaryFragment.this.word.trim());


                WordMeaning wm = new WordMeaning();
                wm.setMeaning(DictionaryFragment.this.wordMeaning);

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Word w = db.wordDao().findWordByName(word.getName());

                        if( w == null) {
                            long rowid =  db.wordDao().insertWord(word);
                            wm.setWordId(rowid);
                            db.wordMeaningDao().insertAll(wm);

                            Log.d("Tony-delete", "onCreate: data inserted: word: "  + word.getName());
                            Log.d("Tony-delete", "onCreate: data inserted: wordmeaning: " +   wordMeaning );

                           // Toast.makeText(getContext(),"Save " + word.getName() + " successfully", Toast.LENGTH_LONG).show();
                        }else{
                            Log.d("Tony-delete", "onCreate: data not inserted: word exisiting: " + word.getName());
                           // Toast.makeText(getContext(),"Word: " + word.getName() + " existed", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                btnSave.setEnabled(false);
                btnSave.setBackgroundResource(R.drawable.ic_save_none);
            }
        });

        mTTS = new TextToSpeech(DictionaryFragment.this.getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if( status == TextToSpeech.SUCCESS){
                    int result = mTTS.setLanguage(Locale.CANADA);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.d("Tony-Say","Language not Supported");
                    } else {
                        mButtonSayDic.setEnabled(true);
                    }
                }else{
                    Log.d("Tony-Say", "Initialization failed");
                }
            }
        });

        mButtonSayDic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        return v;
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

    public void processJSON(View view){
        JsonProcessingTask jsonProcessingTask = new JsonProcessingTask();
        jsonProcessingTask.execute();
    }

    class JsonProcessingTask extends AsyncTask<Void, Void, View> {

        private String data;
        private String jsonResponse;

        // Key Expired
        protected void cambridgeDictionary()
        {
            InputStream dataStream = null;
            //Fetching our data.
            try{
                //https://dictionary.cambridge.org/api/v1/dictionaries/american-english/search/first/?q=world&format=html&_dc=1605128006312&page=1&start=0&limit=25
                // https://dictionary.cambridge.org/api/v1/dictionaries/american-english/entries/world/?format=html&_dc=1605126571307&page=1&start=0&limit=25
                URL url = new URL("https://dictionary.cambridge.org/api/v1/dictionaries/american-english/entries/world/?format=html&_dc=1605126571307");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("accessKey", "LCj8Eh7sVgmzL8IjSvRlo41QwIeNwZYFNv787a6oPjpSYkdo2EqGJQlM4FeYq17L");
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    dataStream = connection.getInputStream();
//                    String encoding = connection.getContentEncoding() == null ? "UTF-8"
//                            : connection.getContentEncoding();
                }

            }catch (Exception err){
                Log.e("Tony", "Failed to fetch resource.");
            }

            //Converting data to a string.
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader
                    (dataStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }catch (IOException err){
                Log.e("Tony", "Error converting InputStream.");
            }

            //Converting string to a JSONArray object.
            try {
//                Getting full JSONArray.
//                If first element was a JSON Object you would want to use JSONObject instead.
//                data = new JSONArray(textBuilder.toString());
                JSONObject data1 = new JSONObject(textBuilder.toString());

                Log.d("Tony-json",data1.toString());

                //data = data1.get("entryContent").toString();
                data ="<article class=\"entry\"><toc-page><toc-entry><toc-pos-block><toc-pos>Noun</toc-pos><toc-sense><toc-form>world</toc-form> <toc-gw>(THE EARTH)</toc-gw></toc-sense><toc-sense><toc-form>world</toc-form> <toc-gw>(WHOLE AREA)</toc-gw></toc-sense><toc-sense><toc-form>world</toc-form> <toc-gw>(LARGE DEGREE)</toc-gw></toc-sense></toc-pos-block></toc-entry></toc-page><header><h1 class=\"hw\">world</h1></header> <span class=\"pos-block\"><header><span class=\"info\"><span class=\"posgram\"><span class=\"pos\">noun</span></span> <span class=\"info\"><a class=\"playback\" href=\"#\"><img alt=\"world: listen to American pronunciation\" src=\"https://dictionary.cambridge.org/external/images/us_pron.png?version=5.0.133\"/></a><audio><source type=\"audio/mpeg\" src=\"https://dictionary.cambridge.org/media/american-english/us_pron/w/wor/world/world.mp3\"/><source type=\"audio/ogg\" src=\"https://dictionary.cambridge.org/media/american-english/us_pron_ogg/w/wor/world/world.ogg\"/>Your browser does not support HTML5 audio.</audio><span class=\"pron\">/<span class=\"ipa\">wɜrld</span>/</span></span></span></header> <section class=\"senseGrp\"><section class=\"senseEntry\"><header><span class=\"gw\">THE EARTH</span></header> <section class=\"def-block\"><span class=\"definition\"><span class=\"info\"><span class=\"lvl\">›</span> <span class=\"gram\">[<span class=\"gcs\"><span class=\"gc\">U</span></span>]</span></span> <span class=\"def\">the planet on which human life has developed, esp. including all people and their ways of life: </span></span><span class=\"examp\"><span class=\"eg\">People from all over the world will be attending the conference.</span></span><span class=\"examp\"><span class=\"eg\">The rapid growth of computers has changed the world.</span></span></section> <section class=\"def-block\"><span class=\"definition\"><span class=\"info\"><span class=\"lvl\">›</span> <span class=\"gram\">[<span class=\"gcs\"><span class=\"gc\">U</span></span>]</span></span> <span class=\"def\">The world can also mean the whole physical universe: </span></span><span class=\"examp\"><span class=\"eg\">The world contains many solar systems, not just ours.</span></span></section></section><section class=\"senseEntry\"><header><span class=\"gw\">WHOLE AREA</span></header> <section class=\"def-block\"><span class=\"definition\"><span class=\"info\"><span class=\"lvl\">›</span> <span class=\"gram\">[<span class=\"gcs\"><span class=\"gc\">C</span></span>]</span></span> <span class=\"def\">all of a particular group or type of thing, such as countries or animals, or a whole area of human activity or understanding: </span></span><span class=\"examp\"><span class=\"eg\">the animal/plant world</span></span><span class=\"examp\"><span class=\"eg\">the business world</span></span><span class=\"examp\"><span class=\"eg\">the world of entertainment</span></span><span class=\"examp\"><span class=\"eg\">In the world of politics, the president’s voice is still the most powerful in the nation.</span></span></section></section><section class=\"senseEntry\"><header><span class=\"gw\">LARGE DEGREE</span></header> <section class=\"def-block\"><span class=\"definition\"><span class=\"info\"><span class=\"lvl\">›</span> <span class=\"gram\">[<span class=\"gcs\"><span class=\"gc\">U</span></span>]</span></span> <span class=\"def\">a large degree; a lot: </span></span><span class=\"examp\"><span class=\"eg\">There’s a world of difference between the two hotels.</span></span></section></section></section>   <span class=\"entry-xref\">→ <span class=\"lab\">Idioms</span> <a class=\"xr\" href=\"/dictionary/american-english/in-a-world-of-your-own\" data-resource=\"american-english\" data-topic=\"in-a-world-of-your-own\"><span class=\"f\">in a world of <i>your</i> own</span></a>, <a class=\"xr\" href=\"/dictionary/american-english/in-the-world\" data-resource=\"american-english\" data-topic=\"in-the-world\"><span class=\"f\">in the world</span></a>, <a class=\"xr\" href=\"/dictionary/american-english/man-of-the-world\" data-resource=\"american-english\" data-topic=\"man-of-the-world\"><span class=\"f\">man of the world</span></a> </span></span><footer><small></article>";
                Log.d("Tony-json",data1.get("entryContent").toString());

                Log.d("Tony-json", "JSON parsed.");
            }catch (JSONException err){
                Log.e("Tony", "Error Parsing JSON(" + err.getMessage() + ")");
            }
        }

        // Key forever
        protected void yandexDictionary(){
            InputStream dataStream = null;
            JSONArray dataJSONArray = null;

            //Fetching our data.
            try{
                //https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key=dict.1.1.20201105T051929Z.4cd666414ce58405.712c8280ba81d81f7a1fd7095694fb6d43e225ca&lang=en-en&text=White
                //String word ="While";
                String urlStr = String.format("https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key=dict.1.1.20201105T051929Z.4cd666414ce58405.712c8280ba81d81f7a1fd7095694fb6d43e225ca&lang=en-en&text=%s",word);
                URL url = new URL(urlStr);

                dataStream = url.openStream();
            }catch (Exception err){
                Log.e("Tony-json", "Failed to fetch resource.");
            }

            //Converting data to a string.
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader
                    (dataStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }catch (IOException err){
                Log.e("Tony-json", "Error converting InputStream.");
            }

            //Converting string to a JSONArray object.
            try {
                //Getting full JSONArray.
                //If first element was a JSON Object you would want to use JSONObject instead.
                //data = new JSONArray(textBuilder.toString());
                JSONObject dataObj = new JSONObject(textBuilder.toString());
                Log.d("tony-json",dataObj.toString());
                dataJSONArray = new JSONArray(dataObj.get("def").toString());
                //this.data = dataJSONArray.toString();
                this.data = JSONToHTML(dataJSONArray);
                Log.d("Tony-json",this.data);
                Log.d("Tony-json", "JSON parsed.");
            }catch (JSONException err){
                Log.e("Tony-json", "Error Parsing JSON(" + err.getMessage() + ")");
            }
        }

        protected String JSONToHTML(JSONArray dataArray){
            String result = "";
            if(dataArray != null & dataArray.length() >0) {
                try {
                    JSONObject dataObj = dataArray.getJSONObject(0);
                    result = "<h2>" + dataObj.get("text") + "</h2> " +
                            "<div><span>" + dataObj.get("pos") + " </span>" +
                            "<span>/" + dataObj.get("ts") + "/</span></div>"
                            + "<h3>Meaning</h3>";
                    JSONArray trDataJSONArray = new JSONArray(dataObj.get("tr").toString());
                    String ulMeaning = "<ol>";
                    for (int i = 0; i < trDataJSONArray.length(); i++) {
                        JSONObject trDataObj = trDataJSONArray.getJSONObject(i);
                        ulMeaning += "<li style='color: blue'>" + trDataObj.get("text") + "</li>";

                        if (trDataObj.has("syn")) {
                            JSONArray synDataJSONArray = new JSONArray(trDataObj.get("syn").toString());
                            String spanMeaning = "<div>";
                            for (int j = 0; j < synDataJSONArray.length(); j++) {
                                JSONObject synDataObj = synDataJSONArray.getJSONObject(j);

                                if (j == synDataJSONArray.length() - 1)
                                    spanMeaning += "<span>" + synDataObj.get("text") + "</span>";
                                else spanMeaning += "<span>" + synDataObj.get("text") + ", </span>";
                            }
                            spanMeaning += "</div>";
                            ulMeaning += spanMeaning;
                        }
                    }
                    ulMeaning += "</ol>";
                    result += ulMeaning;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected View doInBackground(Void... voids) {
            yandexDictionary();
            return null;
        }

        @Override
        protected void onPostExecute(View view) {

            View v = view;

            if(this.data == "") {
                this.data = "<h4>No meaning</h4>";
                DictionaryFragment.this.wordMeaning = "";
            }else
                DictionaryFragment.this.wordMeaning = this.data;

            webView.loadData(this.data, "text/html; charset=utf-8", "utf-8");

            super.onPostExecute(view);
        }
    }

    @Override
    public void sendData(String data) {
        Log.d("Tony-json", "SendData " + data);
        this.word = data;
        processJSON(this.getView());
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
