package com.tony.navigationdrawer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tony.navigationdrawer.database.AppDatabase;
import com.tony.navigationdrawer.events.WordsEvent;
import com.tony.navigationdrawer.model.Word;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LearnedFragment extends Fragment {

    private WebView webView;
    private String word;
    private String wordMeaning;
    private ImageButton btnDelete;

    private ListView listView;
    private WordAdapter wordAdapter;

    private AppDatabase db;
    private Executor executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this
        View v = inflater.inflate(R.layout.fragment_learned, container, false);
        btnDelete = v.findViewById(R.id.btnDelete);

        db = AppDatabase.getInstance(getContext());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Word> wordList = db.wordDao().getAll();
                EventBus.getDefault().post(new WordsEvent(wordList));
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Long> listId  = new ArrayList<Long>();
                CheckBox cb;
                TextView tvName, tvId;
                View childView;
                int count = wordAdapter.getCount();
                Log.d("Tony-delete","Count: wordAdapter: " + count + " Count: ListView " + listView.getCount());
                for (int i = 0; i < count; i++) {
                    childView = listView.getChildAt(i);
                    if (childView != null) {
                        cb = childView.findViewById(R.id.cbDelete);
                        tvName = childView.findViewById(R.id.tvName);
                        tvId = childView.findViewById(R.id.tvId);

                        if (cb.isChecked()) {
                            Log.d("Tony-delete", "Delete " + cb.isChecked() + " Delete " + " i: " + i +
                                    " User Adapter Count: " + wordAdapter.getCount() +
                                    " listview Count " + listView.getCount() +
                                    " id: " + tvId.getText() + " name " + tvName.getText());

                            listId.add(Long.valueOf(tvId.getText().toString()));
                            Log.d("Tony-delete", "Count after deleting: " + count + " i: " + i);
                        }
                    }
                }
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < listId.size(); i++) {
                            Log.d("Tony-delete", "Delete " + " Delete " + " i: " + i + " id: " + listId.get(i));
                            db.wordDao().deleteById(listId.get(i));
                        }

                        List<Word> wordList = db.wordDao().getAll();
                        EventBus.getDefault().post(new WordsEvent(wordList));
                    }
                });
            }
        });

        EventBus.getDefault().register(this);

        return v;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void wordsEventHandler(WordsEvent event) {
        List<Word> wordList = event.getWordList();
        //create the adapter
        wordAdapter = new WordAdapter(getContext(), R.layout.list_item, wordList);
        //set the adapter of the ListView
        listView = (ListView)getView().findViewById(R.id.rvItems);
        listView.setAdapter(wordAdapter);
    }


    public class WordAdapter extends ArrayAdapter<Word> {

        private List<Word> items;

        public WordAdapter(Context context, int textViewResourceId, List<Word> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        //This method is called once for every item in the ArrayList as the list is loaded.
        //It returns a View -- a list item in the ListView -- for each item in the ArrayList
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            //LayoutInflater vi = (LayoutInflater)getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
            LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            v = vi.inflate(R.layout.list_item, null);

            Word o = items.get(position);
            if (o != null) {
                TextView tt = (TextView) v.findViewById(R.id.tvId);
                TextView bt = (TextView) v.findViewById(R.id.tvName);
                CheckBox cb = (CheckBox) v.findViewById(R.id.cbDelete);

                bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      //  Toast.makeText(getContext(), "Image clicked", Toast.LENGTH_SHORT).show();
                        long wordId = o.getId();
                        String word = o.getName();

                        Log.d("Tony-delete", "Start Details with userId: " + wordId);
//                         Create an explicit Intent to start Relative Layout Activity
                        Intent intent = new Intent(getContext(),MeaningDetail.class);
                        intent.putExtra("wordId",wordId );
                        intent.putExtra("word",word);
                        startActivity(intent);
                    }
                });

                if (tt != null) {
                    tt.setText(String.valueOf(o.getId()));
                }
                if (bt != null) {
                    bt.setText(o.getName());
                }
            }



            return v;
        }
    }

    @Override
    public void onDestroy() {
        AppDatabase.destroyInstance();
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    @Override
    public void onPause() {
        AppDatabase.destroyInstance();
        EventBus.getDefault().unregister(this);
        super.onPause();
    }
}