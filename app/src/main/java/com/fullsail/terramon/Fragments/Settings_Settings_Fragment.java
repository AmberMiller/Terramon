package com.fullsail.terramon.Fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.fullsail.terramon.Globals.Globals;
import com.fullsail.terramon.R;
import com.parse.ParseUser;

import java.util.ArrayList;


public class Settings_Settings_Fragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

//region Variables
    public static final String TAG = "SETTINGS_SETTINGS_FRAGMENT";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ParseUser user;

    /* UI Elements */
    Switch soundEffectsSwitch;
    Switch musicSwitch;
    Switch notificationsSwitch;
    ImageButton resetButton;
//endregion


    //region Fragment Setup
    public static Settings_Settings_Fragment newInstance () { return new Settings_Settings_Fragment(); }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences(Globals.PREFERENCES, Context.MODE_PRIVATE);
        user = ParseUser.getCurrentUser();

//        soundEffectsSwitch = (Switch) getActivity().findViewById(R.id.sounds_switch);
//        soundEffectsSwitch.setOnCheckedChangeListener(this);
//        musicSwitch = (Switch) getActivity().findViewById(R.id.music_switch);
//        musicSwitch.setOnCheckedChangeListener(this);
//        notificationsSwitch = (Switch) getActivity().findViewById(R.id.notifications_switch);
//        notificationsSwitch.setOnCheckedChangeListener(this);

        resetButton = (ImageButton) getActivity().findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getResources().getString(R.string.reset))
                        .setMessage(getActivity().getResources().getString(R.string.reset_progress))
                        .setNegativeButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (user != null) {
                                    user.put("monstersCaught", new ArrayList<>());
                                    user.put("itemsOwned", new ArrayList<>());
                                    user.put("playedTutorial", false);
                                    user.saveEventually();
                                }

                                getActivity().finish();
                            }
                        })
                        .setPositiveButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getActivity(), "Cancel", Toast.LENGTH_SHORT).show();
                            }
                        }).show();

            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        editor = sharedPreferences.edit();
//
//        switch (buttonView.getId()) {
//            case R.id.sounds_switch:
//                editor.putBoolean("sound", isChecked);
//                break;
//            case R.id.music_switch:
//                editor.putBoolean("music", isChecked);
//                break;
//            case R.id.notifications_switch:
//                editor.putBoolean("notifications", isChecked);
//                break;
//        }
//
//        editor.apply();
    }

//endregion
}
