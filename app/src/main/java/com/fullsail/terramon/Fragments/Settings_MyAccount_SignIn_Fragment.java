package com.fullsail.terramon.Fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fullsail.terramon.Globals.Globals;
import com.fullsail.terramon.Globals.Utils;
import com.fullsail.terramon.Interfaces.Button_Interface;
import com.fullsail.terramon.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Created by Amber on 7/6/15.
 */

public class Settings_MyAccount_SignIn_Fragment extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener {

    //region Variables
    public static final String TAG = "SIGNIN_FRAGMENT";

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    Button_Interface listener;
    private String currentUsername;
    private String usernameString;
    private boolean loginSucceeded = true;

    /* UI Elements */
    private ImageButton newUsersButton;
    private EditText usernameInput;
    private EditText passwordInput;
    private ImageButton submitButton;
//    private ImageButton loginfb_button;


//endregion

//region Fragment Setup
    public static Settings_MyAccount_SignIn_Fragment newInstance () { return new Settings_MyAccount_SignIn_Fragment(); }

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
        return inflater.inflate(R.layout.fragment_settings_my_account_sign_in, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /* Input */
        usernameInput = (EditText) getActivity().findViewById(R.id.signin_username_input);
        usernameInput.setOnEditorActionListener(this);

        passwordInput = (EditText) getActivity().findViewById(R.id.signin_password_input);
        passwordInput.setOnEditorActionListener(this);


        /* Buttons */
        newUsersButton = (ImageButton) getActivity().findViewById(R.id.settings_newuser_button);
        newUsersButton.setOnClickListener(this);

        submitButton = (ImageButton) getActivity().findViewById(R.id.signin_button);
        submitButton.setOnClickListener(this);

//        loginfb_button = (ImageButton) getActivity().findViewById(R.id.myaccount_facebook_button);
//        loginfb_button.setOnClickListener(this);
    }

    /* Clear focus and hide keyboard with done button */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        hideSystemBars();
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            hideKeyboard(v);
            v.clearFocus();
            v.callOnClick();
        }
        return false;
    }

    private void hideSystemBars () {
        /* Hides system bars */
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //Hides the navigation bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // Hides the status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); //Swipe to show bars, doesn't trigger ui visibility change listeners
    }
    /* Hide the keyboard */
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
//endregion

    @Override
    public void onClick(View view) {
//        case R.id.myaccount_facebook_button:
//        listener.menuButtonClicked(R.id.myaccount_facebook_button);
//        break;
        if (view.getId() == R.id.signin_button) {
            Log.d(TAG, "Submit Button Clicked");
            if (fieldsNotEmpty()) {
                loginSucceeded = true;
                submitButton.setEnabled(false);
                loginUser(getActivity(), usernameInput.getText().toString(), passwordInput.getText().toString());
            }
        } else {
            //Send button id through interface to SettingsActivity
            listener.menuButtonClicked(view.getId());
        }
    }

//region Sign In Functions
    /* Logs into ParseUser account */
    public void loginUser (final Context context, final String username, final String password) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            currentUsername = currentUser.getUsername();
        }
        ParseUser.logOut();
        Log.d(TAG, "Logged out");
        Utils.showProgressDialog(context);
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                Utils.dismissProgressDialog();
                if (e == null) { //If sign in succeeds, notify the user and exit settings
                    if (loginSucceeded) {
                        Log.d(TAG, "YAY! The user is logged in!");

                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.signed_in)
                                .setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getActivity().finish();
                                    }
                                })
                                .show();
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.not_saved)
                                .setNeutralButton(getResources().getString(R.string.ok), null)
                                .show();
                    }
                } else { //If sign in fails: if on default account, log them back in, otherwise, create a new account
                    Log.d(TAG, "SIGN IN FAILED: " + e.toString());
                    loginSucceeded = false;
                    submitButton.setEnabled(true);
                    if (currentUsername.length() > 30) {
                        Log.d(TAG, "RE_SIGNING IN TO DEFAULT");
                        loginUser(getActivity(), currentUsername, getResources().getString(R.string.default_password));
                    } else {
                        signUpUser(getActivity());
                    }
                }
            }
        });
    }

    /* Creates a new ParseUser account */
    private void signUpUser (final Context context) {
        Log.d(TAG, "CREATING NEW USER...");
        usernameString = Utils.createDummyUsername(32);

        final ParseUser user = new com.parse.ParseUser();
        user.setUsername(usernameString);
        user.setPassword(context.getResources().getString(R.string.default_password));
        user.signUpInBackground(new SignUpCallback() {

            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Utils.dismissProgressDialog();
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.not_saved)
                            .setNeutralButton(getResources().getString(R.string.ok), null)
                            .show();
                } else if (e.getCode() == ParseException.USERNAME_TAKEN) {
                    Log.d(TAG, "USERNAME TAKEN, CREATING NEW USERNAME...");
                    usernameString = Utils.createDummyUsername(32);
                    signUpUser(context);
                }
            }
        });
    }

    public boolean fieldsNotEmpty () {
        if (usernameInput.getText().toString().equals("")) {
            usernameInput.setError(getResources().getString(R.string.no_username));
            return false;
        }
        if (passwordInput.getText().toString().equals("")) {
            passwordInput.setError(getResources().getString(R.string.no_password));
            return false;
        }
        return true;
    }

//    private void signInWithFacebook () {
//        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
//            @Override
//            public void done(ParseUser user, ParseException err) {
//                if (user == null) {
//                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
//                } else if (user.isNew()) {
//                    Log.d("MyApp", "User signed up and logged in through Facebook!");
//                } else {
//                    Log.d("MyApp", "User logged in through Facebook!");
//                }
//            }
//        });
//    }

//endregion
}
