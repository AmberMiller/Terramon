package com.fullsail.terramon.Activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.fullsail.terramon.Fragments.Game_Fragment;
import com.fullsail.terramon.Fragments.Inventory_Fragment;
import com.fullsail.terramon.R;

public class InventoryActivity extends Activity {

//region Variables
    public static final String TAG = "INVENTORY_ACTIVITY";

    /* UI Elements */
    ImageButton backButton;
//endregion

//region Activity Setup
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        hideSystemBars();

        FragmentManager fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.InventoryContainer, Inventory_Fragment.newInstance(), Inventory_Fragment.TAG)
                .commit();

        /* Buttons */
        backButton = (ImageButton) this.findViewById(R.id.inventory_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    /* Hides system bars */

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            hideSystemBars();
        }
    }

    private void hideSystemBars () {
        /* Hides system bars */
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //Hides the navigation bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // Hides the status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); //Swipe to show bars, doesn't trigger ui visibility change listeners
    }

//endregion
}
