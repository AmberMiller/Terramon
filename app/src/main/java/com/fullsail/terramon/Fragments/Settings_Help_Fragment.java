package com.fullsail.terramon.Fragments;


import android.app.Activity;
import android.media.Image;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.fullsail.terramon.Interfaces.Button_Interface;
import com.fullsail.terramon.R;


public class Settings_Help_Fragment extends Fragment implements View.OnClickListener {

    //region Variables
    public static final String TAG = "SETTINGS_FRAGMENT";

    Button_Interface listener;

    /* UI Elements */
    ImageButton viewTutorialButton;
//endregion


    //region Fragment Setup
    public static Settings_Help_Fragment newInstance () {
        return new Settings_Help_Fragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_help, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /* Buttons */
        viewTutorialButton = (ImageButton) getActivity().findViewById(R.id.view_tutorial_button);
        viewTutorialButton.setOnClickListener(this);
    }

//endregion

    /* Send button id through interface to SettingsActivity */
    @Override
    public void onClick(View view) {
        //On button click, call interface function to open new activity from SettingsActivity using button id
        listener.menuButtonClicked(view.getId());
    }





}
