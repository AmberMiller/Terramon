package com.fullsail.terramon.Fragments;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fullsail.terramon.Interfaces.Button_Interface;
import com.fullsail.terramon.R;
import com.parse.ParseUser;


public class Settings_MyAccount_Fragment extends Fragment implements View.OnClickListener {

//region Variables
    public static final String TAG = "MYACCOUNT_FRAGMENT";

    Button_Interface listener;
    ParseUser user = ParseUser.getCurrentUser();

    /* UI Elements */
    LinearLayout signed_in;
    TextView logged_in_user;
    ImageButton loggedin_signin_button;
    ImageButton profile_button;
    ImageButton signin_button;

//endregion


//region Fragment Setup
    public static Settings_MyAccount_Fragment newInstance () {return new Settings_MyAccount_Fragment();}

    /* Ensure activity implements Button_Interface */
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
        return inflater.inflate(R.layout.fragment_settings_my_account, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        signed_in = (LinearLayout) getActivity().findViewById(R.id.signed_in);
        logged_in_user = (TextView) getActivity().findViewById(R.id.logged_in_user);

        /* Buttons */
        loggedin_signin_button = (ImageButton) getActivity().findViewById(R.id.myaccount_loggedin_signin_button);
        loggedin_signin_button.setOnClickListener(this);

        profile_button = (ImageButton) getActivity().findViewById(R.id.myaccount_profile_button);
        profile_button.setOnClickListener(this);

        signin_button = (ImageButton) getActivity().findViewById(R.id.myaccount_signin_button);
        signin_button.setOnClickListener(this);

        Log.d(TAG, "USER LOGGED IN AS: " + user.getUsername() + " Length: " + user.getUsername().length());
        if (user.getUsername().length() < 32) {
            signin_button.setVisibility(View.GONE);
            signed_in.setVisibility(View.VISIBLE);
            logged_in_user.setText(getResources().getString(R.string.logged_in) + " " + user.getUsername());
        } else {
            signin_button.setVisibility(View.VISIBLE);
            signed_in.setVisibility(View.GONE);
        }


    }
//endregion

    /* Send button id through interface to SettingsActivity */
    @Override
    public void onClick(View view) {
        listener.menuButtonClicked(view.getId());
    }
}
