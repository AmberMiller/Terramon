package com.fullsail.terramon.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.fullsail.terramon.Data.Objects.Inventory_Object;
import com.fullsail.terramon.Fragments.Catch_Fragment;
import com.fullsail.terramon.Fragments.Game_Fragment;
import com.fullsail.terramon.Globals.Utils;
import com.fullsail.terramon.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class LauncherActivity extends Activity {

    public static final String TAG = "LAUNCHER_ACTIVITY";
    private String usernameString;
    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        connected = Utils.checkConnection(this);

        //If a user is logged in, continue, else, create dummy user
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "CURRENT USER: " + currentUser);
            goToGameActivity();
        } else {
            Log.d(TAG, "NO USER LOGGED IN");
            if (connected) {
                Log.d(TAG, "CREATING DUMMY USER...");
                signUpUser(this);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.no_connection)
                        .setNeutralButton(getResources().getString(R.string.ok), null)
                        .show();
            }
        }

        if (connected) {
            addMonstersFromParse();
            addItemsFromParse();
        }
    }

    /* Creates a new ParseUser account */
    private void signUpUser (final Context context) {
        Log.d(TAG, "CREATING NEW USER...");
        usernameString = Utils.createDummyUsername(32);

        final ParseUser user = new com.parse.ParseUser();
        user.setUsername(usernameString);
        user.setPassword(context.getResources().getString(R.string.default_password));
        Utils.showProgressDialog(context);
        user.signUpInBackground(new SignUpCallback() {

            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Utils.dismissProgressDialog();
                    goToGameActivity();
                } else if (e.getCode() == ParseException.USERNAME_TAKEN) {
                    Log.d(TAG, "USERNAME TAKEN, CREATING NEW USERNAME...");
                    usernameString = Utils.createDummyUsername(32);
                    signUpUser(context);
                }
            }
        });
    }

    /* Pulls MonsterClass from Parse and Saves in Local Datastore */
    private void addMonstersFromParse () {
        Log.d(TAG, "SAVING MONSTERS FROM PARSE");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("MonsterClass");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    ParseObject.pinAllInBackground(objects);
                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });
    }

    /* Pulls ShopClass from Parse and Saves in Local Datastore */
    private void addItemsFromParse () {
        Log.d(TAG, "SAVING ITEMS FROM PARSE");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ShopClass");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    ParseObject.pinAllInBackground(objects);
                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });
    }

    /* Closes Launcher and Opens GameActivity */
    private void goToGameActivity () {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        this.finish();
    }

    /* Adds Drawable Image to Parse as ParseFile */
    public void addImageFileToParse (int drawable, String imageName, final String parseClass, final String parseID, final String parseColumn) {
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), drawable);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[]image = stream.toByteArray();
        final ParseFile file = new ParseFile(imageName + ".png", image);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d(TAG, "Image Saved");
                ParseQuery<ParseObject> query = ParseQuery.getQuery(parseClass);

                query.getInBackground(parseID, new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException e) {
                        if (e == null) {
                            object.put(parseColumn, file);
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Log.d(TAG, "OBJECT SAVED");
                                }
                            });
                        }
                    }
                });
            }
        });
    }

}
