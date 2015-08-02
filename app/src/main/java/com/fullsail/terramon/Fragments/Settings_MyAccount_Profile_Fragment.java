package com.fullsail.terramon.Fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
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

import com.fullsail.terramon.Globals.Utils;
import com.fullsail.terramon.Interfaces.Button_Interface;
import com.fullsail.terramon.R;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by Amber on 7/6/15.
 */


public class Settings_MyAccount_Profile_Fragment extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener {

//region Variables
    public static final String TAG = "PROFILE_FRAGMENT";

    Button_Interface listener;

    /* UI Elements */
    EditText firstNameInput;
    EditText lastNameInput;
    EditText emailInput;
    EditText usernameInput;
    EditText passwordInput;
    EditText confirmPasswordInput;

    ImageButton gotoSigninButton;
    ImageButton submitButton;

//endregion

//region Fragment Setup
    public static Settings_MyAccount_Profile_Fragment newInstance () {return new Settings_MyAccount_Profile_Fragment();}

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_my_account_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /* Input */
        firstNameInput = (EditText) getActivity().findViewById(R.id.first_name_input);
        firstNameInput.setOnEditorActionListener(this);

        lastNameInput = (EditText) getActivity().findViewById(R.id.last_name_input);
        lastNameInput.setOnEditorActionListener(this);

        emailInput = (EditText) getActivity().findViewById(R.id.email_input);
        emailInput.setOnEditorActionListener(this);

        usernameInput = (EditText) getActivity().findViewById(R.id.username_input);
        usernameInput.setOnEditorActionListener(this);

        passwordInput = (EditText) getActivity().findViewById(R.id.profile_password_input);
        passwordInput.setOnEditorActionListener(this);

        confirmPasswordInput = (EditText) getActivity().findViewById(R.id.profile_confirm_password_input);
        confirmPasswordInput.setOnEditorActionListener(this);

        /* Buttons */
        gotoSigninButton = (ImageButton) getActivity().findViewById(R.id.goto_signin_button);
        gotoSigninButton.setOnClickListener(this);

        submitButton = (ImageButton) getActivity().findViewById(R.id.profile_submit_button);
        submitButton.setOnClickListener(this);

        ParseUser user = ParseUser.getCurrentUser();

        if (user != null && user.getUsername().length() < 32) {
//                final EditText input = new EditText(getActivity());
//                input.setHint(R.string.password);
//
//                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
//                    .setTitle(R.string.login_before)
//                    .setView(input)
//                    .setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            String pass = input.getEditableText().toString();
//
//                                if (pass.equals(user.get("password").toString())) {
                                    firstNameInput.setText(user.get("firstName").toString());
                                    lastNameInput.setText(user.get("lastName").toString());
                                    emailInput.setText(user.getEmail());
                                    usernameInput.setText(user.getUsername());
//                                } else {
//                                    input.setError(getResources().getString(R.string.wrong_password));
//                                }
//                        }
//                    })
//                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            listener.menuButtonClicked(R.id.myaccount_button);
//                        }
//                    })
//                    .create();
//                alertDialog.show();
        }
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

    /* Send button id through interface to SettingsActivity */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.profile_submit_button) {
            if (fieldsNotEmpty() && validEmailFormat() && usernameLength() && validPassword() && passwordsMatch()) {
                submitButton.setEnabled(false);
                updateUser(firstNameInput.getText().toString(), lastNameInput.getText().toString(), usernameInput.getText().toString(),
                           passwordInput.getText().toString(), emailInput.getText().toString());
            }
        } else {
            listener.menuButtonClicked(view.getId());
        }

    }

//region Profile Update
    /* Updates user data  */
    public void updateUser (String firstName, String lastName, String username, String password, String email) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        Utils.showProgressDialog(getActivity());
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(TAG, "USER UPDATED");
                    Utils.dismissProgressDialog();
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.saved)
                            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finish();
                                }
                            })
                            .show();
                } else {
                    Log.d(TAG, "UPDATE FAILED: " + e.toString());
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.not_saved)
                            .setNeutralButton(R.string.ok, null)
                            .show();
                }
            }
        });
    }

    /* Checks if EditTexts are empty */
    public boolean fieldsNotEmpty () {
        if (TextUtils.isEmpty(firstNameInput.getText().toString())) {
            firstNameInput.setError(getResources().getString(R.string.first_name_error));
            return false;
        }

        if (TextUtils.isEmpty(lastNameInput.getText().toString())) {
            lastNameInput.setError(getResources().getString(R.string.last_name_error));
            return false;
        }

        if (TextUtils.isEmpty(emailInput.getText().toString())) {
            emailInput.setError(getResources().getString(R.string.no_email));
            return false;
        }

        if (TextUtils.isEmpty(usernameInput.getText().toString())) {
            usernameInput.setError(getResources().getString(R.string.no_username));
            return false;
        }

        if (TextUtils.isEmpty(passwordInput.getText().toString())) {
            passwordInput.setError(getResources().getString(R.string.no_password));
            return false;
        }

        if (TextUtils.isEmpty(confirmPasswordInput.getText().toString())) {
            confirmPasswordInput.setError(getResources().getString(R.string.password_confirm_error));
        }

        return true;
    }

    /* Checks if email is valid format using regex */
    private boolean validEmailFormat () {
        String email = emailInput.getText().toString();
        String emailPattern = "[a-zA-Z0-9_.]+@[a-zA-Z]+.[a-zA-Z]{2,}";

        if (!email.matches(emailPattern)) {
            emailInput.setError(getResources().getString(R.string.email_error));
            return false;
        }
        return true;
    }

    /* Checks is username is within length limit */
    private boolean usernameLength () {
        String username = usernameInput.getText().toString();

        if (username.length() < 1 || username.length() > 30) {
            usernameInput.setError(getResources().getString(R.string.username_error));
            return false;
        }
        return true;
    }

    /* Checks is password is valid using regex */
    private boolean validPassword () {
        String pass = passwordInput.getText().toString();
        String passPattern = "(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{5,20}";

        if (!pass.matches(passPattern)) {
            passwordInput.setError(getResources().getString(R.string.password_error));
            return false;
        }
        return true;
    }

    /* Checks is passwords match */
    private boolean passwordsMatch () {
        if (!passwordInput.getText().toString().equals(confirmPasswordInput.getText().toString())) {
            confirmPasswordInput.setError(getResources().getString(R.string.no_match));
            return false;
        }
        return true;
    }
//endregion
}
