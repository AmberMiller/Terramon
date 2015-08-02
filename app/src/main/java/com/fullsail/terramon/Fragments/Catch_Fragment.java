package com.fullsail.terramon.Fragments;

/**
 * Created by Amber on 7/31/15.
 */
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.fullsail.terramon.Activities.CatchActivity;
import com.fullsail.terramon.Data.SpawnData;
import com.fullsail.terramon.R;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class Catch_Fragment extends Fragment implements GestureOverlayView.OnGesturePerformedListener {

//region Variables
    public static final String TAG = "CATCH_FRAGMENT";

    private ParseUser currentUser;
    private SpawnData spawnData;
    private static String spawnID;
    private static String monsterID;
    private ParseObject monster;

    private boolean captureSuccess;

    timerThread timer;

    int secsPassed = 0;

    /* Gestures */
    private GestureLibrary gestureLibrary;

    private static final String GESTURE_SQUARE = "square";
    private static final String GESTURE_TRIANGLE = "triangle";
    private static final String GESTURE_SPIRAL = "spiral";
    private static final String GESTURE_STAR = "star";
    private static final String[] gestures = new String[] {GESTURE_SQUARE, GESTURE_TRIANGLE, GESTURE_SPIRAL, GESTURE_STAR};

    /* Game Elements */
    int wrongGestures = 0;
    int numGestures = 0;
    int currentGesture = 0;
    String requiredGesture;

    /* UI Elements */
    private GestureOverlayView gestureOverlayView;

    private TextView timerText;
    private ImageView monsterImage;
    private ImageView swipeImage;
    private ImageView wrongImage1;
    private ImageView wrongImage2;
    private ImageView wrongImage3;
    private ImageView gestureFeedImage;

//endregion

//region Fragment Setup
    public static Catch_Fragment newInstance (String _spawnID, String _monsterID) {
        spawnID = _spawnID;
        monsterID = _monsterID;
        return new Catch_Fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_catch_screen, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(false);

        currentUser = ParseUser.getCurrentUser();
        spawnData = SpawnData.getInstance(getActivity());

        gestureLibrary = GestureLibraries.fromRawResource(getActivity(), R.raw.gestures);
        if (!gestureLibrary.load()) {
            Log.w(TAG, "Could not load gesture library");
        }

        gestureOverlayView = (GestureOverlayView) getActivity().findViewById(R.id.gestureOverlayView);
        gestureOverlayView.addOnGesturePerformedListener(this);

        timerText = (TextView) getActivity().findViewById(R.id.timerText);
        monsterImage = (ImageView) getActivity().findViewById(R.id.catchMonsterImage);
        swipeImage = (ImageView) getActivity().findViewById(R.id.swipeArrow);
        wrongImage1 = (ImageView) getActivity().findViewById(R.id.wrong1);
        wrongImage2 = (ImageView) getActivity().findViewById(R.id.wrong2);
        wrongImage3 = (ImageView) getActivity().findViewById(R.id.wrong3);
        gestureFeedImage = (ImageView) getActivity().findViewById(R.id.gesture_feed_image);
        gestureFeedImage.setAlpha(0f);

        getParseMonster();
    }

    private void getParseMonster () {
        ParseQuery<ParseObject> query = new ParseQuery<>("MonsterClass");
        query.fromLocalDatastore();
        query.getInBackground(monsterID, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Current Monster is: " + parseObject.getString("monsterName"));
                    monster = parseObject;
                    parseObject.getParseFile("monsterImage").getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] bytes, ParseException e) {
                            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            monsterImage.setImageBitmap(image);
                            Log.d(TAG, "Image Set, Starting Game");
                            startGame();
                        }
                    });
                } else {
                    Log.d(TAG, "Error finding monster: " + e);
                    catchError();
                }
            }
        });
    }
//endregion

//region Gameplay
    private void startGame () {
        setNumGestures();

        currentGesture = 1;
        setRequiredGesture();

        int secs = numGestures * 4;

        //Set timerThread variable and execute passing in parameters
        timer = new timerThread();
        timer.execute(secs);
    }

    private void nextGesture () {
        showFeedback();
        currentGesture++;

        if (currentGesture > numGestures) {
//            Toast.makeText(getActivity(), "YOU WIN!!!", Toast.LENGTH_SHORT).show();
            captureMonster();
        } else {
            setRequiredGesture();
        }
    }

    private void setNumGestures () {
        int rarity = monster.getInt("monsterRarity");

        if (rarity < 10) {
            numGestures = 5;
        } else if (rarity >= 10 && rarity <= 30) {
            numGestures = 3;
        } else {
            numGestures = 2;
        }

        Log.d(TAG, "Monster Rarity: " + rarity + " Num Gestures: " + numGestures);
    }

    private void setRequiredGesture () {
        int randomNum = (int) Math.round(Math.random() * (gestures.length - 1));
        String gestureRequired = gestures[randomNum];

        switch (gestureRequired) {
            case GESTURE_SQUARE:
                swipeImage.setImageResource(R.drawable.gesture_square);
                break;
            case GESTURE_TRIANGLE:
                swipeImage.setImageResource(R.drawable.gesture_triangle);
                break;
            case GESTURE_SPIRAL:
                swipeImage.setImageResource(R.drawable.gesture_spiral);
                break;
            case GESTURE_STAR:
                swipeImage.setImageResource(R.drawable.gesture_star);
                break;
            default:
                break;
        }

        requiredGesture = gestureRequired;
        Log.d(TAG, "Required Gesture is: " + requiredGesture);
    }

    private void gotWrong () {
        wrongGestures++;

        switch (wrongGestures) {
            case 1:
                wrongImage1.setImageResource(R.drawable.wrong);
                break;
            case 2:
                wrongImage2.setImageResource(R.drawable.wrong);
                break;
            case 3:
                wrongImage3.setImageResource(R.drawable.wrong);
                catchError();
                break;
        }
    }

    private void showFeedback () {
        gestureFeedImage.setAlpha(1f);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(gestureFeedImage, "alpha",
                0f);
        fadeOut.setDuration(900);
        ObjectAnimator mover = ObjectAnimator.ofFloat(gestureFeedImage,
                "translationY", 400f, 0f);
        mover.setDuration(900);

        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.play(mover).with(fadeOut);
        animatorSet.start();
    }
//endregion

//region Capture
    private void captureMonster () {
        captureSuccess = true;
        timer.cancel(true);

        List monstersCaught = currentUser.getList("monstersCaught");
        Log.d(TAG, "Monsters Caught: " + monstersCaught);

        if (monstersCaught == null) {
            monstersCaught = new ArrayList();
        }

        monstersCaught.add(monsterID);
        Log.d(TAG, "Monsters Caught: " + monstersCaught);

        currentUser.put("monstersCaught", monstersCaught);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Monster Capture Save SUCCESS");
                    Intent intent = new Intent();
                    intent.putExtra("spawnID", spawnID);
                    getActivity().setResult(CatchActivity.RESULT_OK, intent);
                    getActivity().finish();
                } else {
                    Log.d(TAG, "Monster Capture Save FAILED: " + e);
                    catchError();
                }
            }
        });
    }

    /* Show alert dialog on catch error to notify user */
    private void catchError () {
        spawnData.catchSpawnedMonster(getActivity(), spawnID);

        timer.cancel(true);

        if (getActivity() != null) {

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.capture_error)
                    .setNeutralButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().setResult(CatchActivity.RESULT_CANCELED);
                            getActivity().finish();
                        }
                    })
                    .show();
        }
    }
//endregion

//region Gesture Detection

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);

        // one prediction needed
        if (predictions.size() > 0) {
            Prediction prediction = predictions.get(0);
            // checking prediction
            if (prediction.score > 1.0) {
                // and action


                if (requiredGesture.equals(prediction.name)) {
                    Log.d(TAG, prediction.name + " Gesture Received: CORRECT!");
                    nextGesture();
//                        Toast.makeText(getActivity(), "Gesture: " + prediction.name + " CORRECT!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, prediction.name + " Gesture Received: WRONG!");
                    gotWrong();
//                    Toast.makeText(getActivity(), "Gesture: " + prediction.name + " WRONG!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
//endregion

//region Timer
    public class timerThread extends AsyncTask <Integer, Integer, String> {

        int secs;

        @Override
        protected String doInBackground(Integer... params) {
            //Do long running operations here

            //Set variables to passed in parameters
            secs = params[0];

            //If everything is more than zero, run loop
            while (secs > 0) {

                //If isCancelled, break out of the loop
                if (isCancelled()) break;

                secs--;
                secsPassed++;

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(secs);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

            //Update UI with progress here

            if (secs < 10) {
                timerText.setText(("00:" + "0" + Integer.toString(progress[0])));
            } else {
                timerText.setText(("00:" + Integer.toString(progress[0])));
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!captureSuccess) {
                catchError();
            }
            this.cancel(true);
        }
}
//endregion

}
