package com.example.pupil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    /*static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }*/

    private TextView mTextMessage;
    private ActionBar toolbar;
    private int curPageId = R.id.navigation_words;

    private Home fragmentSimple;
    private final String SIMPLE_FRAGMENT_TAG = "myfragmenttag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = getSupportActionBar();
        toolbar.setTitle(R.string.title_words);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_words);
        loadFragment(Words.newInstance());
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (curPageId == item.getItemId()) {
                return false;
            } else {
                curPageId = item.getItemId();
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        toolbar.setTitle(R.string.title_home);
                        loadFragment(Home.newInstance());
                        return true;
                    case R.id.navigation_words:
                        toolbar.setTitle(R.string.title_words);
                        loadFragment(Words.newInstance());
                        return true;
                    case R.id.navigation_settings:
                        toolbar.setTitle(R.string.title_settings);
                        loadFragment(Settings.newInstance());
                        return true;
                }
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_id, R.anim.fade_out);
        ft.replace(R.id.frame_container, fragment);
        ft.commit();
    }
}
