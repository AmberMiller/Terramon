package com.fullsail.terramon;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.fullsail.terramon.Data.SpawnData;
import com.fullsail.terramon.Globals.Globals;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hoytdingus on 7/16/15.
 */
public class SpawnService extends Service {

    public static final String TAG = "SPAWN_SERVICE";

    private static Timer spawnTimer = new Timer();
    private LatLng userLocation;
    private int numSpawns = 0;
    private int passedNumSpawns;

    private SpawnData spawnData;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        spawnData = SpawnData.getInstance(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.hasExtra("userLocation") && intent.getParcelableExtra("userLocation") != null) {

            userLocation = (LatLng) intent.getParcelableExtra("userLocation");
            passedNumSpawns = intent.getIntExtra("numSpawns", 0);

            if (passedNumSpawns > numSpawns) {
                numSpawns = passedNumSpawns;
            }
        }

        if(userLocation != null) {
            spawnTimer.scheduleAtFixedRate(new spawnTask(), 0, 60000);
        }

        return START_STICKY;
    }

    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);
    }


    private class spawnTask extends TimerTask {

        Handler handler = new Handler();

        @Override
        public void run() {
            handler.post(runnable);
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (spawnData.getSpawns() != null) {
                    numSpawns = spawnData.getSpawns().size();
                } else {
                    numSpawns = 0;
                }

                if (numSpawns < 3) {
                    Log.d(TAG, "Num Spawns: " + numSpawns + " Running Service...");

                    ParseGeoPoint userGeoPoint = new ParseGeoPoint(userLocation.latitude, userLocation.longitude);
                    Log.d(TAG, "User Location: Lat: " + userLocation.latitude + " Lng: " + userLocation.longitude);

                    HashMap<String, Object> spawnValue = new HashMap<String, Object>();
                    spawnValue.put("userPointer", ParseUser.getCurrentUser().getObjectId());
                    spawnValue.put("userLocationLat", userGeoPoint.getLatitude());
                    spawnValue.put("userLocationLng", userGeoPoint.getLongitude());
                    spawnValue.put("radius", 600);

                    ParseCloud.callFunctionInBackground("getSpawn", spawnValue, new FunctionCallback<ParseObject>() {
                        @Override
                        public void done(final ParseObject spawn, ParseException e) {

                            if (e == null && spawn != null) {
                                Log.d(TAG, "Got Spawn from Cloud");
                                numSpawns++;

                                spawn.pinInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Log.d(TAG, "Pinned Spawn in Background");

                                        Intent intent = new Intent(Globals.SPAWNED_MONSTER);
                                        sendBroadcast(intent);
                                    }
                                });

                            } else {
                                Log.i("ERROR", e.toString());
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "Too many spawns, not running...");
                    stopSelf();
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service Destroyed");
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }


}

