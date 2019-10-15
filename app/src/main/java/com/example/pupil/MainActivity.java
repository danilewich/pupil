package com.example.pupil;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /*static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }*/

    private TextView mTextMessage;
    private ActionBar toolbar;
    private int curPageId = R.id.navigation_words;

    private Words fragmentSimple;
    private final String SIMPLE_FRAGMENT_TAG = "myfragmenttag";
    final String LOG_TAG = "myLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = getSupportActionBar();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        toolbar.setTitle(R.string.title_words);
        navigation.setSelectedItemId(R.id.navigation_words);
        loadFragment(Words.newInstance());

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Log.d(LOG_TAG, "onCreate");
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    protected void onRestart() {
        super.onRestart();
        Log.d(LOG_TAG, "onRestart");
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(LOG_TAG, "onRestoreInstanceState");
    }

    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume ");
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState");
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return fragmentSimple;
    }

    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
    }

    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
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
