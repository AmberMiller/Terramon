package com.fullsail.terramon.Fragments;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.fullsail.terramon.Interfaces.Button_Interface;
import com.fullsail.terramon.R;


public class Settings_Achievements_Fragment extends Fragment {

    //region Variables
    public static final String TAG = "ACHIEVEMENTS_FRAGMENT";

    /* UI Elements */
    GridView gridView;

//endregion


    //region Fragment Setup
    public static Settings_Achievements_Fragment newInstance() {
        return new Settings_Achievements_Fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_achievements, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        gridView = (GridView) getActivity().findViewById(R.id.achievements_gridview);
    }

//endregion
}
