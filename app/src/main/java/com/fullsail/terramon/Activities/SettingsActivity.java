package com.fullsail.terramon.Activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.fullsail.terramon.Fragments.Settings_Achievements_Fragment;
import com.fullsail.terramon.Fragments.Settings_Fragment;
import com.fullsail.terramon.Fragments.Settings_Help_Fragment;
import com.fullsail.terramon.Fragments.Settings_MyAccount_Fragment;
import com.fullsail.terramon.Fragments.Settings_MyAccount_Profile_Fragment;
import com.fullsail.terramon.Fragments.Settings_MyAccount_SignIn_Fragment;
import com.fullsail.terramon.Fragments.Settings_Settings_Fragment;
import com.fullsail.terramon.Interfaces.Button_Interface;
import com.fullsail.terramon.R;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

/**
 * Created by Amber on 7/6/15.
 */

public class SettingsActivity extends Activity implements Button_Interface {

//region Variables
    public static final String TAG = "SETTINGS_ACTIVITY";

    FragmentManager fragmentManager = getFragmentManager();
    int currentFrag; //Base == 0
                     // My Account == 1
                     // Achievements == 2
                     // Settings == 3
                     // Help == 4
                     // My Account Profile == 11
                     // My Account Signin == 12
                     // Help View Tutorial == 41

    /* UI Elements */
    ImageButton backButton;
    ImageView head1;
    ImageView head2;
    ImageView foot1;
    ImageView foot2;

//endregion

//region Activity Setup
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        hideSystemBars();

        fragmentManager.beginTransaction()
                .replace(R.id.SettingsContainer, Settings_Fragment.newInstance(), Settings_Fragment.TAG)
                .commit();

        backButton = (ImageButton) this.findViewById(R.id.settings_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentFrag) {
                    case 2: //If on achievements, change header images back to default and show footer, then returnToMain
                        head1.setImageResource(R.drawable.settings_head1);
                        head2.setImageResource(R.drawable.settings_head2);
                        foot1.setVisibility(View.VISIBLE);
                        foot2.setVisibility(View.VISIBLE);
                    case 1: case 3: case 4:
                        returnToMain();
                        break;
                    case 11: case 12:
                        Log.d(TAG, "Settings Go To My Account");
                        goToMyAccount();
                        break;
                    default:
                        Log.d(TAG, "Settings Finish");
                        finish();
                }
            }
        });

        head1 = (ImageView) this.findViewById(R.id.settingsHead1);
        head2 = (ImageView) this.findViewById(R.id.settingsHead2);
        foot1 = (ImageView) this.findViewById(R.id.settingsFoot1);
        foot2 = (ImageView) this.findViewById(R.id.settingsFoot2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
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

//region Navigation

    @Override
    public void menuButtonClicked(int buttonID) {
        Log.d(TAG, "Current Frag: " + currentFrag);

        switch (buttonID) {
            /* Main Menu */
            case R.id.myaccount_button:
                Log.d(TAG, "My Account Button Clicked");
                goToMyAccount();
                break;
//            case R.id.achievements_button:
//                Log.d(TAG, "Achievements Button Clicked");
//                goToAchievements();
//                break;
            case R.id.settings_button:
                Log.d(TAG, "Settings Button Clicked");
                goToSettings();
                break;
            case R.id.help_button:
                Log.d(TAG, "Help Button Clicked");
                goToHelp();
                break;
            /* My Account */
            case R.id.myaccount_profile_button:
                Log.d(TAG, "My Account Profile Button Clicked");
                currentFrag = 11;
                fragmentManager.beginTransaction()
                        .replace(R.id.SettingsContainer, Settings_MyAccount_Profile_Fragment.newInstance(), Settings_MyAccount_Profile_Fragment.TAG)
                        .commit();
                break;
            case R.id.myaccount_signin_button: case R.id.goto_signin_button: case R.id.myaccount_loggedin_signin_button:
                Log.d(TAG, "My Account Signin Button Clicked");
                currentFrag = 12;
                fragmentManager.beginTransaction()
                        .replace(R.id.SettingsContainer, Settings_MyAccount_SignIn_Fragment.newInstance(), Settings_MyAccount_SignIn_Fragment.TAG)
                        .commit();
                break;
//            case R.id.myaccount_facebook_button:
//                Log.d(TAG, "My Account Facebook Button Clicked");
//                break;
            /* Help */
            case R.id.view_tutorial_button:
                Log.d(TAG, "View Tutorial Button Clicked");
                ParseUser user = ParseUser.getCurrentUser();
                user.put("playedTutorial", false);
                user.saveEventually();
                this.finish();
                currentFrag = 41;
                break;
            default:
                returnToMain();
        }
    }

    /* Return to main settings fragment */
    private void returnToMain () {
        currentFrag = 0;
        fragmentManager.beginTransaction()
                .replace(R.id.SettingsContainer, Settings_Fragment.newInstance(), Settings_Fragment.TAG)
                .commit();
    }

    /* Load My Account Fragment */
    private void goToMyAccount () {
        currentFrag = 1;
        fragmentManager.beginTransaction()
                .replace(R.id.SettingsContainer, Settings_MyAccount_Fragment.newInstance(), Settings_MyAccount_Fragment.TAG)
                .commit();
    }

    /* Load Achievements Fragment */
    private void goToAchievements () {
        currentFrag = 2;
        // Hide footer and change header images to achievements
        head1.setImageResource(R.drawable.achievements_head1);
        head2.setImageResource(R.drawable.achievements_head2);
        foot1.setVisibility(View.GONE);
        foot2.setVisibility(View.GONE);

        fragmentManager.beginTransaction()
                .replace(R.id.SettingsContainer, Settings_Achievements_Fragment.newInstance(), Settings_Achievements_Fragment.TAG)
                .commit();
    }

    /* Load Settings Fragment */
    private void goToSettings () {
        currentFrag = 3;
        fragmentManager.beginTransaction()
                .replace(R.id.SettingsContainer, Settings_Settings_Fragment.newInstance(), Settings_Settings_Fragment.TAG)
                .commit();
    }

    /* Load Help Fragment */
    private void goToHelp () {
        currentFrag = 4;
        fragmentManager.beginTransaction()
                .replace(R.id.SettingsContainer, Settings_Help_Fragment.newInstance(), Settings_Help_Fragment.TAG)
                .commit();
    }
//endregion
}
