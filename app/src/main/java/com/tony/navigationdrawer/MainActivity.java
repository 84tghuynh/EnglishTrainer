package com.tony.navigationdrawer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private EditText editText_search;
    DataFromActivityToFragment dataFromActivityToFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRecordAudioPermission();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editText_search = findViewById(R.id.et_search);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null) {
           // callProfileFragment(navigationView);
        }

        editText_search.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    callDictionaryFragment(navigationView);
                    String[] keyword = editText_search.getText().toString().split("\\s+");
                    editText_search.setText("");
                    Toast.makeText(MainActivity.this, keyword[0], Toast.LENGTH_SHORT).show();

                    // Send data input to Fragment
                    final Handler handler = new Handler();
                    final Runnable r = new Runnable() {
                        public void run() {
                            dataFromActivityToFragment.sendData(keyword[0].trim());
                        }
                    };
                    handler.postDelayed(r, 50);
                }
                return false;
            }
        });


       // SetupSpeech();
    }

    protected void callDictionaryFragment(NavigationView navigationView){
        DictionaryFragment dicFrag = new DictionaryFragment();
        dataFromActivityToFragment = (DataFromActivityToFragment) dicFrag;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,dicFrag).commit();
       // navigationView.setCheckedItem(R.id.nav_profile);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_speech:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SpeechFragment()).commit();
                break;
            case R.id.nav_say:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SayFragment()).commit();
                break;
            case R.id.nav_learned:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new LearnedFragment()).commit();
                break;
            case R.id.nav_share:
                Toast.makeText(this, "Share",Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_send:
                Toast.makeText(this, "Send",Toast.LENGTH_LONG).show();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    private void requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String requiredPermission = Manifest.permission.RECORD_AUDIO;

            // If the user previously denied this permission then show a message explaining why
            // this permission is needed
            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 101);
            }
        }
    }

    public interface DataFromActivityToFragment {
        void sendData(String data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}