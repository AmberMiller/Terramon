package com.fullsail.terramon.Fragments;

/**
 * Created by Amber on 7/5/15.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fullsail.terramon.Activities.CatchActivity;
import com.fullsail.terramon.Activities.GameActivity;
import com.fullsail.terramon.Activities.ShopActivity;
import com.fullsail.terramon.Data.SpawnData;
import com.fullsail.terramon.Globals.Globals;
import com.fullsail.terramon.Interfaces.Button_Interface;
import com.fullsail.terramon.R;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game_Fragment extends Fragment implements View.OnClickListener {

//region Variables
    public static final String TAG = "GAME_FRAGMENT";

    private Button_Interface listener;
    private GameReceiver receiver = new GameReceiver();
    private SpawnData spawnData;

    private int currentTut;
    private boolean spawnLoaded;
    private HashMap<String, ParseObject> spawnedMonsters;
    private HashMap<String, Bitmap> monsterImages;
    private String closestSpawnID;
    private ParseUser currentUser;

    /* UI Elements */
    private ImageButton settingsButton;
    private ImageButton inventoryButton;
    private ImageButton monstersButton;
    private ImageButton shopButton;
    private ImageButton mapButton;

    private ImageView monsterImage;

    private ImageButton catchButton;

    private RelativeLayout detailLayout;
    private ImageView monsterDetails;
    private TextView monsterName;
    private TextView monsterDescription;
    private TextView monsterNumber;

    private LinearLayout tutorialLayout;
    private ImageView tutorialImage;

    private RelativeLayout caughtLayout;
    private ImageButton caughtCloseButton;
    private ImageButton caughtViewMonsters;
    private ImageView caughtImage;
    private TextView caughtText;

//endregion

//region Fragment Setup
    public static Game_Fragment newInstance () {

        return new Game_Fragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (Button_Interface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Button_Interface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

        spawnData = SpawnData.getInstance(getActivity());
        currentUser = ParseUser.getCurrentUser();
        spawnedMonsters = new HashMap<>();
        monsterImages = new HashMap<>();

        /* UI Elements */
        settingsButton = (ImageButton) getActivity().findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(this);

        inventoryButton = (ImageButton) getActivity().findViewById(R.id.inventoryButton);
        inventoryButton.setOnClickListener(this);

        monstersButton = (ImageButton) getActivity().findViewById(R.id.monstersButton);
        monstersButton.setOnClickListener(this);

        shopButton = (ImageButton) getActivity().findViewById(R.id.shopButton);
        shopButton.setOnClickListener(this);

        mapButton = (ImageButton) getActivity().findViewById(R.id.mapButton);
        mapButton.setOnClickListener(this);

        monsterImage = (ImageView) getActivity().findViewById(R.id.monster_image);
        monsterImage.setOnClickListener(this);

        catchButton = (ImageButton) getActivity().findViewById(R.id.catch_button);
        catchButton.setOnClickListener(this);
        hideCatchButton();

        detailLayout = (RelativeLayout) getActivity().findViewById(R.id.detail_view_layout);
        monsterDetails = (ImageView) getActivity().findViewById(R.id.detail_view);
        monsterName = (TextView) getActivity().findViewById(R.id.detail_name);
        monsterDescription = (TextView) getActivity().findViewById(R.id.detail_decription);
        monsterNumber = (TextView) getActivity().findViewById(R.id.detail_number);

        tutorialLayout = (LinearLayout) getActivity().findViewById(R.id.tutorialLayout);
        tutorialImage = (ImageView) getActivity().findViewById(R.id.tutorialImage);
        tutorialImage.setOnClickListener(this);

        caughtLayout = (RelativeLayout) getActivity().findViewById(R.id.caught_notification_layout);
        caughtImage = (ImageView) getActivity().findViewById(R.id.caught_image);
        caughtText = (TextView) getActivity().findViewById(R.id.caught_text);
        caughtCloseButton = (ImageButton) getActivity().findViewById(R.id.caught_close_button);
        caughtCloseButton.setOnClickListener(this);
        caughtViewMonsters = (ImageButton) getActivity().findViewById(R.id.caught_view_button);
        caughtViewMonsters.setOnClickListener(this);

        /* Functions */
        Log.d(TAG, "PLAYED TUT: " + currentUser.getBoolean("playedTutorial"));
        if (!currentUser.getBoolean("playedTutorial")) {
            runTutorial();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        IntentFilter filter = new IntentFilter();
        filter.addAction(Globals.SHOW_CATCH);
        filter.addAction(Globals.HIDE_CATCH);
        filter.addAction(Globals.SHOW_DETAILS);
        filter.addAction(Globals.PLAYED_TUT);
        filter.addAction(Globals.TUT_SPAWN_LOADED);
        filter.addAction(Globals.TUT_MOVED_TO);
        filter.addAction(Globals.TUT_MOVED_AWAY);
        filter.addAction(Globals.TUT_TAPPED_MONSTER);
        filter.addAction(Globals.LOADED_SPAWNS);
        filter.addAction(Globals.VIEW_RANGE);
        filter.addAction(Globals.HIDE_MONSTER);

        getActivity().registerReceiver(receiver, filter);

        spawnData.onStart();

        currentUser = ParseUser.getCurrentUser();
        if (currentUser.getBoolean("playedTutorial")) {
            tutorialLayout.setVisibility(View.GONE);
            enableButtons();
        }

        spawnedMonsters = spawnData.getSpawns();
        monsterImages = spawnData.getImages();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(receiver);

        super.onPause();
    }

    @Override
    public void onStop() {
        spawnData.onStop();

        super.onStop();
    }

    //endregion

//region Functionality
    /* Disable All Buttons */
    public void disableButtons () {
        settingsButton.setEnabled(false);
        inventoryButton.setEnabled(false);
        monstersButton.setEnabled(false);
        shopButton.setEnabled(false);
        mapButton.setEnabled(false);
        catchButton.setEnabled(false);
    }

    /* Enable All Buttons */
    public void enableButtons () {
        settingsButton.setEnabled(true);
        inventoryButton.setEnabled(true);
        monstersButton.setEnabled(true);
        shopButton.setEnabled(true);
        mapButton.setEnabled(true);
        catchButton.setEnabled(true);
    }

    /* Start the Tutorial */
    public void runTutorial () {
        disableButtons();
        tutorialLayout.setVisibility(View.VISIBLE);
        tutorialImage.setImageResource(R.drawable.tut1);
        tutorialImage.setEnabled(true);
        currentTut = 1;
    }

    /* Set monster image with bitmap from monsterImages map */
    public void setMonsterImage (String currentSpawnID) {
        monsterImages = spawnData.getImages();
        Log.d(TAG, "Setting Monster Image from Monster Images: " + monsterImages);

        Bitmap image = monsterImages.get(currentSpawnID);
        monsterImage.setImageBitmap(image);
    }

    /* If monster image is not visible, show it */
    public void showMonsterImage () {
        Log.d(TAG, "Within view range, showing monster image...");
        monsterImage.setVisibility(View.VISIBLE);
    }

    /* Make monster image invisible */
    public void hideMonsterImage () {
        Log.d(TAG, "Out of view range, hiding monster image...");
        monsterImage.setVisibility(View.INVISIBLE);
    }

    /* Scale monster image based on distance */
    public void scaleMonsterImage (float distance) {
        if (monsterImage.getVisibility() == View.VISIBLE) {
            monsterImage.setScaleX((1 / distance) * 3);
            monsterImage.setScaleY((1 / distance) * 3);
        }
    }

    /* Show the Catch Button */
    public void showCatchButton () {
        Log.d(TAG, "Showing Catch Button");
        catchButton.setVisibility(View.VISIBLE);
        catchButton.setEnabled(true);
    }

    /* Hide the Catch Button */
    public void hideCatchButton () {
        Log.d(TAG, "Hiding Catch Button");
        catchButton.setVisibility(View.INVISIBLE);
    }

    /* Show Monster Details */
    public void showMonsterDetails () {
        ParseObject monster = spawnData.getClosestSpawn();
        if (monster != null) {
            String name = monster.getParseObject("monsterPointer").getString("monsterName");
            String desc = monster.getParseObject("monsterPointer").getString("monsterDescription");
            int num = monster.getParseObject("monsterPointer").getInt("monsterNum");
            String type = monster.getParseObject("monsterPointer").getString("monsterType");

            Log.d(TAG, "Name: " + name + " Desc: " + desc + " Num: " + num + " Type: " + type);

            if (detailLayout.getVisibility() == View.VISIBLE) {
                detailLayout.setVisibility(View.INVISIBLE);
            } else {
                monsterName.setText(name);
                monsterDescription.setText(desc);
                monsterNumber.setText("#" + num);

                switch (type) {
                    case "Fire":
                        monsterDetails.setImageResource(R.drawable.details_fire);
                        break;
                    case "Water":
                        monsterDetails.setImageResource(R.drawable.details_water);
                        break;
                    case "Air":
                        monsterDetails.setImageResource(R.drawable.details_air);
                        break;
                    case "Earth":
                        monsterDetails.setImageResource(R.drawable.details_earth);
                        break;
                    case "Psychic":
                        monsterDetails.setImageResource(R.drawable.details_psychic);
                        break;
                }
                detailLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    /* Shows caught dialog with monster data */
    private void showCaughtDialog () {
        detailLayout.setVisibility(View.INVISIBLE);
        catchButton.setVisibility(View.INVISIBLE);
        monsterImage.setVisibility(View.INVISIBLE);

        caughtLayout.setVisibility(View.VISIBLE);

        String name = spawnData.getClosestSpawn().getParseObject("monsterPointer").getString("monsterName");
        caughtText.setText(getResources().getString(R.string.caught) + " " + name + "!");

        caughtImage.setImageBitmap(spawnData.getClosestSpawnImage());

        Log.d(TAG, "User Played Tutorial: " + currentUser.getBoolean("playedTutorial"));
        if (!currentUser.getBoolean("playedTutorial")) {
            if (currentTut == 6) {
                moveToTut7();
            }
        }
    }
//endregion

//region Tutorial Functions
    private void moveToTut2 () {
        settingsButton.setEnabled(true);
        if (spawnLoaded) {
            moveToTut3();
        } else {
            tutorialImage.setImageResource(R.drawable.tut2);
            tutorialImage.setClickable(false);
            currentTut = 2;
        }
    }

    private void moveToTut3 () {
        tutorialImage.setImageResource(R.drawable.tut3);
        tutorialImage.setClickable(false);
        currentTut = 3;
    }

    private void moveToTut4 () {
        tutorialImage.setImageResource(R.drawable.tut4);
        tutorialImage.setClickable(false);
        currentTut = 4;
    }

    private void moveToTut5 () {
        tutorialImage.setImageResource(R.drawable.tut5);
        tutorialImage.setClickable(true);
        currentTut = 5;
    }

    private void moveToTut6 () {
        detailLayout.setVisibility(View.INVISIBLE);
        tutorialImage.setImageResource(R.drawable.tut6);
        tutorialImage.setClickable(false);
        catchButton.setEnabled(true);
        currentTut = 6;
    }

    private void moveToTut7 () {
        tutorialImage.setImageResource(R.drawable.tut7);
        currentTut = 7;

        currentUser.put("playedTutorial", true);
        currentUser.saveEventually();

        enableButtons();
    }
//endregion

//region Click Events
    /* Send button id through interface to SettingsActivity */
    @Override
    public void onClick(View view) {
        //On button click, call interface function to open new activity from GameActivity using button id
        switch (view.getId()) {
            case R.id.monster_image:
                monsterImageClick();
                break;
            case R.id.catch_button:
                catchClick();
                break;
            case R.id.caught_close_button:
                caughtCloseClick();
                break;
            case R.id.tutorialImage:
                tutorialImageClick();
                break;
            case R.id.caught_view_button:
                caughtCloseClick();
            default:
                listener.menuButtonClicked(view.getId());
        }

    }

    private void catchClick () {
        Log.d(TAG, "Caught Button Clicked");

        List monstersCaught = currentUser.getList("monstersCaught");
        Log.d(TAG, "Monsters Caught: " + monstersCaught);

        /* If user hasn't unlocked full version, alert user that full version must be purchased to continue */
        if (monstersCaught!= null && monstersCaught.size() == 5 && !currentUser.getBoolean("unlockedFullVersion")) {
             new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.full)
                    .setMessage(getActivity().getResources().getString(R.string.full_desc))
                    .setPositiveButton(caughtImage.getResources().getString(R.string.goToShop), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(getActivity(), ShopActivity.class));
                        }
                    })
                    .setNegativeButton(getActivity().getResources().getString(R.string.ok), null)
                    .show();
        } else {
            catchButton.setEnabled(false);
            Intent catchIntent = new Intent(getActivity(), CatchActivity.class);
            startActivityForResult(catchIntent, Globals.CATCH_REQUEST);
        }
    }

    /* If monster image is visible, show monster details layout */
    private void monsterImageClick () {
        if (closestSpawnID != null) {
            if (monsterImage.getVisibility() == View.VISIBLE) {
                showMonsterDetails();
            }

            Log.d(TAG, "User Played Tutorial: " + currentUser.getBoolean("playedTutorial"));
            if (!currentUser.getBoolean("playedTutorial")) {
                Intent tutIntent = new Intent(Globals.TUT_TAPPED_MONSTER);
                getActivity().sendBroadcast(tutIntent);
            }
        }
    }

    private void tutorialImageClick () {
        Log.d(TAG, "Tutorial Image Clicked. Current Tut: " + currentTut);
        switch (currentTut) {
            case 1:
                moveToTut2();
                break;
            case 2: case 3: case 4: case 6:
                break;
            case 5:
                moveToTut6();
                break;
            case 7:
                tutorialLayout.setVisibility(View.GONE);
                break;
            default:
                tutorialImage.setImageResource(R.drawable.tut1);
                currentTut = 1;
        }
    }

    private void caughtCloseClick () {
        Log.d(TAG, "Caught Close Button Clicked");
        caughtLayout.setVisibility(View.GONE);

        if (tutorialLayout.getVisibility() == View.VISIBLE) {
            tutorialLayout.setVisibility(View.GONE);
        }
    }
//endregion


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Globals.CATCH_REQUEST) {
            if (resultCode == GameActivity.RESULT_OK) {
                Log.d(TAG, "RESULT OK");
                if (!currentUser.getBoolean("playedTutorial")) {
                    moveToTut7();
                }
                catchButton.setEnabled(true);
                showCaughtDialog();

                Intent intent = new Intent(Globals.CAUGHT_MONSTER);
                getActivity().sendBroadcast(intent);
            } else {
                Log.d(TAG, "RESULT NOT OK");
            }
        }
    }

//region Receiver
    public class GameReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Globals.SHOW_CATCH)) {
                showCatchButton();
            }

            if (intent.getAction().equals(Globals.HIDE_CATCH)) {
                hideCatchButton();
            }

            if (intent.getAction().equals(Globals.SHOW_DETAILS)) {
                showMonsterDetails();
            }

            if (intent.getAction().equals(Globals.LOADED_SPAWNS)) {
                spawnedMonsters = spawnData.getSpawns();
            }

            if (intent.getAction().equals(Globals.VIEW_RANGE)) {
                Log.d(TAG, "Location Changed Broadcast Received");

                float distance = intent.getFloatExtra("distance", 0);

                /* If passed ID is different, set closestSpawnID and change monster image */
                if (!spawnData.getClosestSpawnID().equals(closestSpawnID)) {
                    closestSpawnID = spawnData.getClosestSpawnID();
                    setMonsterImage(closestSpawnID);

                    if (detailLayout.getVisibility() == View.VISIBLE) {
                        detailLayout.setVisibility(View.INVISIBLE);
                    }
                }
                showMonsterImage();
                scaleMonsterImage(distance);
            }

            if (intent.getAction().equals(Globals.HIDE_MONSTER)) {
                hideMonsterImage();
            }

            /* Tutorial Stuff */
            if (intent.getAction().equals(Globals.TUT_SPAWN_LOADED)) {
                spawnLoaded = true;
                if (currentTut == 2) {
                    moveToTut3();
                }
            }

            if (intent.getAction().equals(Globals.TUT_MOVED_TO)) {
                if (currentTut == 3) {
                    moveToTut4();
                }
            }

            if (intent.getAction().equals(Globals.TUT_MOVED_AWAY)) {
                if (currentTut == 4) {
                    moveToTut3();
                }
            }

            if (intent.getAction().equals(Globals.TUT_TAPPED_MONSTER)) {
                if (currentTut == 4) {
                    moveToTut5();
                }
            }
        }
    }
//endregion

}
