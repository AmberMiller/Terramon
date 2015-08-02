package com.fullsail.terramon.Globals;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.ImageView;

import com.fullsail.terramon.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Amber on 7/16/15.
 */
public class Utils {

    public static final String TAG = "UTILS";
    private static ProgressDialog progressDialog;


    /* Creates a randomized alphanumeric string of set length */
    public static String createDummyUsername(final int sizeOfRandomString) {
        final String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for(int i = 0; i < sizeOfRandomString; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));

        Log.d(TAG, "DUMMY USERNAME: " + sb.toString());
        return sb.toString();
    }

    /* Shows a progress dialog with indeterminate bar */
    public static void showProgressDialog (Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(context.getResources().getString(R.string.processing));
        progressDialog.setMessage(context.getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    /* Dismiss the progress dialog */
    public static void dismissProgressDialog () {
        progressDialog.dismiss();
    }

    /* Check the data connection */
    public static boolean checkConnection (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null) {
                if (info.isConnected() && info.isAvailable()) {
                    Log.d(TAG, "CONNECTED");
                    return true;
                }
            }
        }
        Log.d(TAG, "NOT CONNECTED");
        return false;
    }

    /* Converts ParseFile to bitmap */
    public static void displayParseImage (ParseFile image, final ImageView imageView) {

        if (image != null) {
            image.getDataInBackground(new GetDataCallback() {

                @Override
                public void done(byte[] data, ParseException e) {

                    if (e == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                        if (bmp != null) {
                            Log.d(TAG, "PARSE IMAGE SET");
                            imageView.setImageBitmap(bmp);
                        }
                    } else {
                        Log.d(TAG, "IMAGE DOWNLOAD FAILED: " + e.toString());
                    }
                }
            });

        } else {
            Log.d(TAG, "IMAGE IS NULL");
            imageView.setImageResource(R.drawable.placeholder);
        }
    }
}
