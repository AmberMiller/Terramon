package com.fullsail.terramon.Data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.fullsail.terramon.Globals.Globals;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Amber on 7/27/15.
 */
public class SpawnData {

    public static final String TAG = "SPAWN_DATA";

    private static SpawnData instance;
    private static ArrayList<ParseObject> spawnedMonsters;
    private SpawnReceiver receiver = new SpawnReceiver();

    private SpawnData (Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Globals.SPAWNED_MONSTER);
        filter.addAction(Globals.MAP_LOADED);
        filter.addAction(Globals.RARE_SPAWN);
        context.registerReceiver(receiver, filter);
    }

    /* Return instance of SpawnData */
    public static SpawnData getInstance(Context context) {
        if (instance == null) {
            instance = new SpawnData(context);
        }
            return instance;
    }

    /* Return arraylist of spawns */
    public ArrayList<ParseObject> getSpawns () {
        return spawnedMonsters;
    }

    /* Get Spawned Monsters from Local Datastore */
    private void getLocalSpawnedMonsters (final Context context) {
        if (spawnedMonsters == null) {
            spawnedMonsters = new ArrayList<>();
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("SpawnClass");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                Log.d(TAG, "Got Spawns from Local Storage, Adding to Array...");
                spawnedMonsters.clear();

                for (ParseObject object : list) {
                    spawnedMonsters.add(object);
                }
                Log.d(TAG, "Spawned Monsters Num: " + spawnedMonsters.size());

                Intent intent = new Intent(Globals.LOADED_SPAWNS);
                context.sendBroadcast(intent);
            }
        });
    }

    /* Unpins All Spawns from Local Datastore */
    private void unpinAllSpawns (final Context context) {
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("SpawnClass");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                Log.d(TAG, "Unpinning All Spawns");
                spawnedMonsters = new ArrayList<>();

                ParseObject.unpinAllInBackground(list, new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        Intent intent = new Intent(Globals.LOADED_SPAWNS);
                        context.sendBroadcast(intent);
                    }
                });
            }
        });
    }

    /* Unpins Caught Monster from Local Datastore */
    public void catchSpawnedMonster (final Context context, String caughtSpawnID) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("SpawnClass");
        query.fromLocalDatastore();
        query.getInBackground(caughtSpawnID, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    parseObject.unpinInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d(TAG, "Monster unpinned");
                                getLocalSpawnedMonsters(context);
                            } else {
                                Log.d(TAG, "Unpinning Failed: " + e);
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "Error Unpinning Monster Spawn: " + e);
                }
            }
        });
    }

    /* Get a random rare spawn */
    private void spawnRareMonster (final Context context, final double latitude, final double longitude) {
        String[] rareMonsterList = new String[] {"UB8rfp2Lg3", "lsVEWVcQcZ", "GawQvazZT9"}; //List of rare spawn objectIDs
        int position = (int) Math.round(Math.random() * (rareMonsterList.length - 1));
        Log.d(TAG, "Random Spawn Number: " + position);

        /* Gets spawn from database and saves it out as a monster spawn on users' location */
        ParseQuery<ParseObject> query = ParseQuery.getQuery("MonsterClass");
        query.fromLocalDatastore();
        query.getInBackground(rareMonsterList[position], new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Rare Monster Spawned: " + parseObject.getString("monsterName"));
                    ParseObject rareSpawn = new ParseObject("SpawnClass");
                    rareSpawn.put("monsterPointer", parseObject);
                    rareSpawn.put("monsterLocation", new ParseGeoPoint(latitude, longitude));
                    rareSpawn.put("userPointer", ParseUser.getCurrentUser().getObjectId());
                    rareSpawn.pinInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d(TAG, "Rare Spawn Saved");
                                getLocalSpawnedMonsters(context);
                                Intent intent = new Intent(Globals.ITEM_USED);
                                intent.putExtra("itemUsed", true);
                                context.sendBroadcast(intent);
                            } else {
                                Log.d(TAG, "Rare Spawn Save Failed: " + e);
                                Intent intent = new Intent(Globals.ITEM_USED);
                                intent.putExtra("itemUsed", false);
                                context.sendBroadcast(intent);
                            }
                        }
                    });
                    rareSpawn.saveEventually();
                } else {
                    Log.d(TAG, "Couldn't find monster: " + e);
                    Intent intent = new Intent(Globals.ITEM_USED);
                    intent.putExtra("itemUsed", false);
                    context.sendBroadcast(intent);
                }
            }
        });
    }

    public class SpawnReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            /* Sent from map when loaded */
            if (intent.getAction().equals(Globals.MAP_LOADED)) {
                /* If passed distance is greater than 1000 meters, unpin spawns and load new ones */
                if (intent.getFloatExtra("distance", 0) > 1000) {
                    Log.d(TAG, "Map Loaded, Distance is: " + intent.getFloatExtra("distance", 0) + ". Unpinning all spawns...");
                    unpinAllSpawns(context);
                } else {
                    /* Load spawns from local storage */
                    Log.d(TAG, "Map Loaded, Retrieving Spawns from Storage...");
                    getLocalSpawnedMonsters(context);
                }
            }

            /* Called from SpawnService */
            if (intent.getAction().equals(Globals.SPAWNED_MONSTER)) {
                Log.d(TAG, "Monster Spawned, Retrieving Spawns from Storage...");
                getLocalSpawnedMonsters(context);
            }

            /* Called from Inventory to load rare spawn */
            if (intent.getAction().equals(Globals.RARE_SPAWN)) {
                Log.d(TAG, "Rare Spawn Item Used");
                spawnRareMonster(context, intent.getDoubleExtra("latitude", 0), intent.getDoubleExtra("longitude", 0));
            }
        }
    }
}
