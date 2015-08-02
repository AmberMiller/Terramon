package com.fullsail.terramon.Activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.fullsail.terramon.Fragments.Catch_Fragment;
import com.fullsail.terramon.R;

public class CatchActivity extends Activity {

//region Variables
    public static final String TAG = "CATCH_ACTIVITY";

    FragmentManager fragmentManager;
//endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catch);

        Intent intent = getIntent();
        String spawnID = intent.getStringExtra("spawnID");
        String monsterID = intent.getStringExtra("monsterID");

        hideSystemBars();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.CatchContainer, Catch_Fragment.newInstance(spawnID, monsterID), Catch_Fragment.TAG)
                .commit();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            hideSystemBars();
        }
    }

    private void hideSystemBars () {
        /* Hides system bars */
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //Hides the navigation bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // Hides the status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); //Swipe to show bars, doesn't trigger ui visibility change listeners
    }

}
