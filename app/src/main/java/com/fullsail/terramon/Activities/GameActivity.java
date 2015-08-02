package com.fullsail.terramon.Activities;

/**
 * Created by Amber on 7/5/15.
 */

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.fullsail.terramon.CameraBackground;
import com.fullsail.terramon.Fragments.Game_Fragment;
import com.fullsail.terramon.Fragments.Mini_Map_Fragment;
import com.fullsail.terramon.Interfaces.Button_Interface;
import com.fullsail.terramon.R;


public class GameActivity extends Activity implements Button_Interface {

//region Variables
    public static final String TAG = "GAME_ACTIVITY";

    FragmentManager fragmentManager;

    private Camera mCamera;
    private CameraBackground mCamBackground;

//endregion

    // static method to access the camera
    public static Camera getCameraInstance() {

        Camera c = null;

        try {

            c = Camera.open();

        } catch (Exception e) {
            Log.d(TAG, "Camera Exception: " + e);
        }

        return c;

    }

//region Activity Setup
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        /* Adding camera to the View */
        mCamera = getCameraInstance();
        mCamBackground = new CameraBackground(this, mCamera);
        FrameLayout camBackground = (FrameLayout) findViewById(R.id.camera_background);
        camBackground.addView(mCamBackground);

        hideSystemBars();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.GameContainer, Game_Fragment.newInstance(), Game_Fragment.TAG)
                .commit();

        fragmentManager.beginTransaction()
                .replace(R.id.MapContainer, Mini_Map_Fragment.getInstance(), Mini_Map_Fragment.TAG)
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
//endregion

    @Override
    public void menuButtonClicked(int buttonID) {
        switch (buttonID) {
            case R.id.settingsButton:
                Log.d(TAG, "Settings Button Clicked");
                startActivity(new Intent(GameActivity.this, SettingsActivity.class));
                break;
            case R.id.inventoryButton:
                Log.d(TAG, "Inventory Button Clicked");
                startActivity(new Intent(GameActivity.this, InventoryActivity.class));
                break;
            case R.id.monstersButton:
                Log.d(TAG, "Monsters Button Clicked");
                startActivity(new Intent(GameActivity.this, MonstersActivity.class));
                break;
            case R.id.shopButton:
                Log.d(TAG, "Shop Button Clicked");
                startActivity(new Intent(GameActivity.this, ShopActivity.class));
                break;
            case R.id.mapButton:
                Log.d(TAG, "Map Button Clicked");
                break;
            case R.id.caught_view_button:
                Log.d(TAG, "Caught View Button Clicked");
                startActivity(new Intent(GameActivity.this, MonstersActivity.class));
                break;
        }
    }
}
