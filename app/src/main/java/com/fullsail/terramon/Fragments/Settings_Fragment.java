package com.fullsail.terramon.Fragments;

/**
 * Created by Amber on 7/5/15.
 */

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.fullsail.terramon.Interfaces.Button_Interface;
import com.fullsail.terramon.R;

public class Settings_Fragment extends Fragment implements View.OnClickListener {

    //region Variables
    public static final String TAG = "SETTINGS_FRAGMENT";

    Button_Interface listener;

    /* UI Elements */
    ImageButton myaccountButton;
//    ImageButton achievementsButton;
    ImageButton settingsButton;
    ImageButton helpButton;

//endregion


//region Fragment Setup
    public static Settings_Fragment newInstance () {
        return new Settings_Fragment();
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
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /* Buttons */
        myaccountButton = (ImageButton) getActivity().findViewById(R.id.myaccount_button);
        myaccountButton.setOnClickListener(this);

//        achievementsButton = (ImageButton) getActivity().findViewById(R.id.achievements_button);
//        achievementsButton.setOnClickListener(this);

        settingsButton = (ImageButton) getActivity().findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(this);

        helpButton = (ImageButton) getActivity().findViewById(R.id.help_button);
        helpButton.setOnClickListener(this);
    }

//endregion

    /* Send button id through interface to SettingsActivity */
    @Override
    public void onClick(View view) {
        //On button click, call interface function to open new activity from SettingsActivity using button id
        listener.menuButtonClicked(view.getId());
    }
}
