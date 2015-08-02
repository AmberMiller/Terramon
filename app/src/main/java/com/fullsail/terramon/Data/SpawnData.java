package com.fullsail.terramon.Data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import com.fullsail.terramon.Globals.Globals;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Amber on 7/27/15.
 */
public class SpawnData {

//region Variables
    public static final String TAG = "SPAWN_DATA";

    private Context context;
    private static SpawnData instance;
    private SpawnReceiver receiver = new SpawnReceiver();

    private HashMap<String, ParseObject> spawnedMonsters;
    private ArrayList<ParseObject> spawnedMonstersArray;
    private HashMap<String, Bitmap> spawnMonsterImages;

    private String closestSpawnID;
    private Bitmap closestSpawnImage;
    private ParseObject closestSpawn;
//endregion

//region Setup
    private SpawnData (Context _context) {
        context = _context;
        onStart();
    }

    /* Return instance of SpawnData */
    public static SpawnData getInstance(Context context) {
        if (instance == null) {
            instance = new SpawnData(context);
        }
            return instance;
    }

    public void onStart () {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Globals.SPAWNED_MONSTER);
        filter.addAction(Globals.MAP_LOADED);
        filter.addAction(Globals.RARE_SPAWN);
        context.registerReceiver(receiver, filter);
    }

    public void onStop () {
        context.unregisterReceiver(receiver);
    }
//endregion

//region Getter Methods
    /* Return arraylist of spawns */
    public HashMap<String, ParseObject> getSpawns () {
        return spawnedMonsters;
    }

    public ArrayList<ParseObject> getSpawnsArray () {
        return spawnedMonstersArray;
    }

    /* Return map of spawn images */
    public HashMap<String, Bitmap> getImages () {
        return spawnMonsterImages;
    }

    public String getClosestSpawnID () {
        return closestSpawnID;
    }

    public Bitmap getClosestSpawnImage () {
        return closestSpawnImage;
    }

    public ParseObject getClosestSpawn () {
        return closestSpawn;
    }
//endregion

//region Setter Methods
    public void setClosestSpawnID (String closestID) {
        closestSpawnID = closestID;
        setClosestSpawn(closestSpawnID);
    }

    public void setClosestSpawnImage (String closestID) {
        closestSpawnImage = spawnMonsterImages.get(closestID);
    }

    public void setClosestSpawn (String closestSpawnID) {
        closestSpawn = spawnedMonsters.get(closestSpawnID);
    }
//region

//region Data Pulls
    /* Get Spawned Monsters from Local Datastore */
    private void getLocalSpawnedMonsters (final Context context) {
        if (spawnedMonsters == null) {
            spawnedMonsters = new HashMap<>();
        }

        if (spawnedMonstersArray == null) {
            spawnedMonstersArray = new ArrayList<>();
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("SpawnClass");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                Log.d(TAG, "Got Spawns from Local Storage, Adding to Array...");
                spawnedMonsters.clear();
                spawnedMonstersArray.clear();

                for (ParseObject object : list) {
                    spawnedMonsters.put(object.getObjectId(), object);
                    spawnedMonstersArray.add(object);

                    getSpawnImage(object);
                }
                Log.d(TAG, "Sending LOADED_SPAWNS Broadcast, Spawned Monsters Num: " + spawnedMonsters.size());

                Intent intent = new Intent(Globals.LOADED_SPAWNS);
                context.sendBroadcast(intent);
            }
        });
    }

    /* Gets Spawn Monster Image Data and Converts to Bitmap */
    private void getSpawnImage (final ParseObject spawn) {
        if (spawnMonsterImages == null) {
            spawnMonsterImages = new HashMap<>();
        }

        final ParseObject monsterSpawn = spawn.getParseObject("monsterPointer");
        monsterSpawn.getParseFile("monsterImage").getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, ParseException e) {
                if (bytes != null) {
                    Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    spawnMonsterImages.put(spawn.getObjectId(), image);
                }
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
                spawnedMonsters = new HashMap<String, ParseObject>();
                spawnedMonstersArray = new ArrayList<ParseObject>();

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
            public void done(final ParseObject parseObject, ParseException e) {
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
//endregion

//region Receiver
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
//endregion
}
