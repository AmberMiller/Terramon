package com.fullsail.terramon.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.fullsail.terramon.Data.SpawnData;
import com.fullsail.terramon.Globals.Globals;
import com.fullsail.terramon.R;
import com.fullsail.terramon.SpawnService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

public class Mini_Map_Fragment extends MapFragment implements LocationListener {

//region Variables
    public static final String TAG = "MINI_MAP_FRAGMENT";
    private static final long MIN_TIME = 1000;
    private static final float MIN_DISTANCE = 1f;
    private static final int SPAWN_LIMIT = 3;
    private static final int VIEW_DISTANCE = 30;
    private static final int CATCH_DISTANCE = 20;
    private static final int LOAD_DISTANCE = 40;

    private GoogleMap map;
    private LocationManager locationManager;
    private Map_Receiver receiver;
    private SpawnData spawnData;

    private LatLng currentLocation;
    private boolean locationFound = false;

    private boolean serviceRan = false;
    private ParseUser currentUser;

    private ArrayList<MarkerOptions> monsterMarkers;
    private ArrayList<ParseObject> spawnedMonsters;
//endregion

//region Service
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if (getCurrentLocation() == null) {
                Log.d(TAG, "Current location was null, waiting 30s...");
                handler.postDelayed(this, 30000);
            } else {
                Log.d(TAG, "Current location found, calling service...");
                handler.removeCallbacks(this);
                callService();
            }
        }
    };
//endregion

//region Map Setup

    public static Mini_Map_Fragment getInstance () {
        return new Mini_Map_Fragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        map = this.getMap();
        map.setMyLocationEnabled(true);

        UiSettings settings = map.getUiSettings();
        settings.setAllGesturesEnabled(false);
        settings.setMyLocationButtonEnabled(false);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        receiver = new Map_Receiver();

        currentUser = ParseUser.getCurrentUser();

        spawnData = SpawnData.getInstance(getActivity());
        monsterMarkers = new ArrayList<>();
        spawnedMonsters = new ArrayList<>();

        currentLocation = getCurrentLocation();

        if (currentLocation != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));

            float distanceTraveled = getUserDistance(currentLocation.latitude, currentLocation.longitude);

            sendMapLoadedIntent(distanceTraveled);

            ParseGeoPoint userGeoPoint = new ParseGeoPoint(currentLocation.latitude, currentLocation.longitude);
            currentUser.put("userLocation", userGeoPoint);
            currentUser.saveEventually();
        } else {
            sendMapLoadedIntent(0);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        locationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, this);
    }

    @Override
    public void onProviderDisabled(String provider) {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Globals.SPAWNED_MONSTER);
        filter.addAction(Globals.LOADED_SPAWNS);
        filter.addAction(Globals.CAUGHT_MONSTER);

        getActivity().registerReceiver(receiver, filter);

//        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//
//        if (enabled) {
////            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
////            startActivity(intent);
//            Log.d(TAG, "GPS enabled, using GPS provider");
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                    MIN_TIME, // Milliseconds between updates.
//                    MIN_DISTANCE, // Meters moved between updates.
//                    this);
//        } else {
            Log.d(TAG, "GPS not enabled, using network provider");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME, // Milliseconds between updates.
                    MIN_DISTANCE, // Meters moved between updates.
                    this);
//        }
        loadSpawnsForMarkers();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(receiver);

        LatLng location = getCurrentLocation();
        ParseGeoPoint userGeoPoint = new ParseGeoPoint(location.latitude, location.longitude);
        currentUser.put("userLocation", userGeoPoint);
        currentUser.saveEventually();

        locationManager.removeUpdates(this);

        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "Stopping Service...");
        Intent serviceIntent = new Intent(getActivity(), SpawnService.class);
        getActivity().stopService(serviceIntent);
        super.onStop();
    }

//endregion

//region Location
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location Changed");
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        currentLocation = new LatLng(latitude, longitude);
        locationFound = true;

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));

        float[] userDistance = new float[1];

        /* Get distance between user's old location and current location */
        if (currentUser.getParseGeoPoint("userLocation") != null) {
            Location.distanceBetween(currentUser.getParseGeoPoint("userLocation").getLatitude(),
                    currentUser.getParseGeoPoint("userLocation").getLongitude(),
                    latitude,
                    longitude,
                    userDistance);

            Log.d(TAG, "User Distance Traveled: " + userDistance[0]);
            /* If distance traveled is greater than load distance, save new location */
            if (userDistance[0] > LOAD_DISTANCE) {
                Log.d(TAG, "Saving user GeoPoint...");
                ParseGeoPoint userGeoPoint = new ParseGeoPoint(latitude, longitude);
                currentUser.put("userLocation", userGeoPoint);
                currentUser.saveEventually();
            }
        } else { //If user location is null, save user location
            ParseGeoPoint userGeoPoint = new ParseGeoPoint(latitude, longitude);
            currentUser.put("userLocation", userGeoPoint);
            currentUser.saveEventually();
        }

        ParseObject closestSpawn = getClosestSpawn(latitude, longitude);
        if (closestSpawn != null) {
            spawnData.setClosestSpawnImage(closestSpawn.getObjectId());
            spawnData.setClosestSpawnID(closestSpawn.getObjectId());

            float[] distance = new float[1];

            Location.distanceBetween(closestSpawn.getParseGeoPoint("monsterLocation").getLatitude(),
                    closestSpawn.getParseGeoPoint("monsterLocation").getLongitude(),
                    latitude,
                    longitude,
                    distance);

            Log.d(TAG, "Distance: " + distance[0]);

            /* If monster is within catch distance, show catch button, else hide it */
            if (distance[0] < CATCH_DISTANCE) {
                Intent catchIntent = new Intent(Globals.SHOW_CATCH);
                getActivity().sendBroadcast(catchIntent);
            } else {
                Intent catchIntent = new Intent(Globals.HIDE_CATCH);
                getActivity().sendBroadcast(catchIntent);
            }

            /* If monster is within view distance, show image button, else hide it */
            if (distance[0] < VIEW_DISTANCE) {
                Log.d(TAG, "Within viewing range");
                Intent viewIntent = new Intent(Globals.VIEW_RANGE);
                viewIntent.putExtra("distance", distance[0]);
                getActivity().sendBroadcast(viewIntent);

                /* If user hasn't completed tutorial, send moved to broadcast */
                if (!currentUser.getBoolean("playedTutorial")) {
                    Intent tutIntent = new Intent(Globals.TUT_MOVED_TO);
                    getActivity().sendBroadcast(tutIntent);
                }
            } else {
                Intent intent = new Intent(Globals.HIDE_MONSTER);
                getActivity().sendBroadcast(intent);

                /* If user hasn't completed tutorial, send moved away broadcast */
                if (!currentUser.getBoolean("playedTutorial")) {
                    Intent tutIntent = new Intent(Globals.TUT_MOVED_AWAY);
                    getActivity().sendBroadcast(tutIntent);
                }
            }
        }
    }

    /* Get User's Current Location */
    public LatLng getCurrentLocation() {
        Log.d(TAG, "Getting Current Location...");
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Log.d(TAG, "Current Location: LAT " + latitude + " LNG " + longitude);

            return new LatLng(latitude, longitude);
        }

        return null;
    }

    /* Loops through monsters array and returns closest if within load distance */
    public ParseObject getClosestSpawn (double latitude, double longitude) {
        float closest = 0;
        ParseObject closestSpawn = null;

        if (spawnedMonsters.size() != 0) {
            Log.d(TAG, "Looping through spawned monsters...");
            for (int i = 0; i < spawnedMonsters.size(); i++) {
                ParseObject monster = spawnedMonsters.get(i);

                float[] distance = new float[1];

                /* Geofence check for distance between points */
                Location.distanceBetween(
                        monster.getParseGeoPoint("monsterLocation").getLatitude(),
                        monster.getParseGeoPoint("monsterLocation").getLongitude(),
                        latitude,
                        longitude,
                        distance);

                Log.d(TAG, "Spawn " + monster.getParseObject("monsterPointer").getString("monsterName") + " is " + distance[0] + " meters away.");

                if (distance[0] < LOAD_DISTANCE) {

                    Log.d(TAG, "Closest: " + closest + " Distance[0]: " + distance[0]);
                    if (closest == 0 || distance[0] <= closest) {
                        closest = distance[0];
                        Log.d(TAG, "New Closest: " + closest + " Distance[0]: " + distance[0]);
                        closestSpawn = monster;
                        Log.d(TAG, "Closest Spawn is: "
                                + closestSpawn.getParseObject("monsterPointer").getString("monsterName")
                                + " at " + distance[0] + " meters.");
                    }
                }
            }
        }
        return closestSpawn;
    }

    /* Get distance user traveled */
    private float getUserDistance (double latitude, double longitude) {
        Log.d(TAG, "Getting user distance traveled...");
        ParseGeoPoint geoPoint = currentUser.getParseGeoPoint("userLocation");
        if (geoPoint != null) {
            float[] distance = new float[1];

        /* Geofence check for distance between points */
            Location.distanceBetween(
                    geoPoint.getLatitude(),
                    geoPoint.getLongitude(),
                    latitude,
                    longitude,
                    distance);

            Log.d(TAG, "Distance Traveled: " + distance[0]);
            return distance[0];
        } else {
            Log.d(TAG, "No user GeoPoint");
            return 0;
        }
    }

    /* Send MAP_LOADED broadcast to SpawnData class to load local spawns */
    private void sendMapLoadedIntent (float distance) {
        Log.d(TAG, "Sending Map Loaded Intent");
        Intent mapLoadedIntent = new Intent(Globals.MAP_LOADED);
        mapLoadedIntent.putExtra("distance", distance);
        getActivity().sendBroadcast(mapLoadedIntent);
    }
//endregion

//region Markers
    public void createMarker (Double latitude, Double longitude, String title) {
        MarkerOptions marker = new MarkerOptions();

        marker.position(new LatLng(latitude, longitude));
        marker.title(title);
        marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.monster_map_icon));

        monsterMarkers.add(marker);
        addMarkersToMap();
    }

    public void addMarkersToMap () {

        getMap().clear();
        for (MarkerOptions marker : monsterMarkers) {
            getMap().addMarker(marker);
        }
        Log.d(TAG, "MARKERS: " + monsterMarkers);
    }

    /* Calls SpawnService if service was not already called and spawnedMonsters array is within limit, else stops service */
    public void callService() {
        Log.d(TAG, "Calling Service...");

        Log.i(TAG, "serviceRan: " + serviceRan + " Spawned Monsters Array Size: " + spawnedMonsters.size());
        Intent serviceIntent = new Intent(getActivity(), SpawnService.class);

        if (!serviceRan && spawnedMonsters.size() < SPAWN_LIMIT) {
            if (getCurrentLocation() == null) {
                handler.post(runnable);
            } else {
                serviceRan = true;
                serviceIntent.putExtra("userLocation", getCurrentLocation());
                serviceIntent.putExtra("numSpawns", spawnedMonsters.size());
                getActivity().startService(serviceIntent);
            }
        } else {
            Log.d(TAG, "Stopping Service...");
            getActivity().stopService(serviceIntent);
        }
    }

    public void loadSpawnsForMarkers () {
        spawnedMonsters = spawnData.getSpawnsArray();

        if (spawnedMonsters == null) {
            spawnedMonsters = new ArrayList<>();
        } else {
            spawnLoad();
        }

        /* Clear monster markers, add new markers from spawns */
        monsterMarkers.clear();

        for (ParseObject spawn : spawnedMonsters) {
            ParseGeoPoint monsterLocation = spawn.getParseGeoPoint("monsterLocation");

            Double lat = monsterLocation.getLatitude();
            Double lon = monsterLocation.getLongitude();
            String title = spawn.getObjectId();

            createMarker(lat, lon, title);
        }

    }

    public void spawnLoad () {
        spawnedMonsters = spawnData.getSpawnsArray();

        serviceRan = false;
        callService();

        if (spawnedMonsters.size() > 0) {
            Log.d(TAG, "User Played Tutorial: " + currentUser.getBoolean("playedTutorial"));
            if (!currentUser.getBoolean("playedTutorial")) {
                Intent tutIntent = new Intent(Globals.TUT_SPAWN_LOADED);
                getActivity().sendBroadcast(tutIntent);
            }
        }
    }
//endregion

//region Receiver
    public class Map_Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {

            /* Called from SpawnData when local spawns have been loaded */
            if (intent.getAction().equals(Globals.LOADED_SPAWNS)) {
                Log.d(TAG, "Loaded Spawns Broadcast Received");
                loadSpawnsForMarkers();
            }
        }
    }
//endregion
}
