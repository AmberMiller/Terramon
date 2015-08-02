package com.fullsail.terramon;

import android.app.Application;

import com.fullsail.terramon.Data.SpawnData;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

/**
 * Created by Amber on 7/8/15.
 */
public class AppClass extends Application {

    SpawnData spawnData;

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "ICnUrdMNEVEFXFtiuQWJ7Et7vXkTch2dCqBAM2kR", "q09kwhhq5b9yr9kXRhgH80JQTmZDWCbx8T25IP54");
        ParseFacebookUtils.initialize(this);

        spawnData = SpawnData.getInstance(this);
    }

    @Override
    public void onTerminate() {
        spawnData.onStop();
        super.onTerminate();
    }
}
