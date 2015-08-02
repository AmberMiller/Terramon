package com.fullsail.terramon.Fragments;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fullsail.terramon.Adapters.Grid_Adapter;
import com.fullsail.terramon.Data.Objects.Inventory_Object;
import com.fullsail.terramon.Globals.Globals;
import com.fullsail.terramon.Globals.Utils;
import com.fullsail.terramon.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Inventory_Fragment extends Fragment {


//region Variables
    public static final String TAG = "INVENTORY_FRAGMENT";

    ArrayList<ParseObject> items = new ArrayList<>();
    Grid_Adapter adapter;
    ParseUser user;

    InventoryReceiver receiver = new InventoryReceiver();
    ParseObject item;

    /* UI Elements */
    GridView inventoryGridView;

    RelativeLayout itemDetailView;
    ImageView itemImage;
    TextView itemTitle;
    TextView itemNum;
    TextView itemDescription;
    ImageButton closeButton;
    ImageButton useButton;

//endregion

//region Fragment Setup
    public static Inventory_Fragment newInstance () {
        return new Inventory_Fragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        user = ParseUser.getCurrentUser();

        inventoryGridView = (GridView) getActivity().findViewById(R.id.inventoryGridView);

        itemDetailView = (RelativeLayout) getActivity().findViewById(R.id.item_detail_view);
        itemImage = (ImageView) getActivity().findViewById(R.id.item_detail_image);
        itemTitle = (TextView) getActivity().findViewById(R.id.item_detail_title);
        itemNum = (TextView) getActivity().findViewById(R.id.item_detail_num);
        itemDescription = (TextView) getActivity().findViewById(R.id.item_detail_description);

        closeButton = (ImageButton) getActivity().findViewById(R.id.item_detail_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Item Detail Close Button Clicked");
                itemDetailView.setVisibility(View.GONE);
            }
        });

        useButton = (ImageButton) getActivity().findViewById(R.id.item_use_button);
        useButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Use Item Button Clicked");
                useButton.setEnabled(false);
                closeButton.setEnabled(false);

                if (item.getObjectId().equals("yRdA2HS6HY")) {
                    ParseGeoPoint geoPoint = user.getParseGeoPoint("userLocation");

                    if (geoPoint != null) {
                        Intent rareSpawnIntent = new Intent(Globals.RARE_SPAWN);
                        rareSpawnIntent.putExtra("latitude", geoPoint.getLatitude());
                        rareSpawnIntent.putExtra("longitude", geoPoint.getLongitude());
                        getActivity().sendBroadcast(rareSpawnIntent);
                    } else {
                        Log.d(TAG, "No user GeoPoint");
                        showErrorAlert();
                        Toast.makeText(getActivity(), "No User GeoPoint", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Globals.ITEM_DETAIL);
        filter.addAction(Globals.ITEM_USED);

        getActivity().registerReceiver(receiver, filter);

        getItemList();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(receiver);

        super.onPause();
    }

//endregion

    private void setGridAdapter () {
        Log.d(TAG, "Setting Grid Adapter");
        adapter = new Grid_Adapter(getActivity(), items, 0);
        inventoryGridView.setAdapter(adapter);
    }

    private void getItemList () {
        try {
            JSONArray array = user.getJSONArray("itemsOwned");
            if (array != null) {
                Log.d(TAG, "Items Owned: " + array);
                JSONArray owned = array.getJSONArray(0);

                items.clear();

                for (int i = 0; i < owned.length(); i++) {
                    final JSONObject object = owned.getJSONObject(i);
                    final int num = object.getInt("numItems");

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("ShopClass");
                    query.fromLocalDatastore();
                    query.getInBackground(object.getString("itemID"), new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            Log.d(TAG, "Item Returned");
                            parseObject.put("numItemsOwned", num);
                            items.add(parseObject);
                            setGridAdapter();

                            useButton.setEnabled(true);
                            closeButton.setEnabled(true);
                        } else {
                            Log.d(TAG, "ITEM PULL ERROR: " + e);
                        }
                        }
                    });
                }
            } else {
                Log.d(TAG, "JSONArray is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void itemUsed () {
        Log.d(TAG, "itemUsed Function Called");
        try {
            JSONArray array = user.getJSONArray("itemsOwned");

            if (array != null) {
                Log.d(TAG, "Items Owned: " + array);
                JSONArray owned = array.getJSONArray(0);

                for (int i = 0; i < owned.length(); i++) {
                    JSONObject object = owned.getJSONObject(i);
                    if (object.getString("itemID").equals(item.getObjectId())) {
                        int num = object.getInt("numItems");
                        num--;
                        if (num <= 0) {
                            owned.remove(i);
                        } else {
                            object.put("numItems", num);
                        }
                    }
                }
            } else {
                Log.d(TAG, "JSONArray is null");
                showErrorAlert();
            }

            user.saveEventually();
            useButton.setEnabled(true);
            closeButton.setEnabled(true);
            getActivity().finish();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert();
        }
    }

    private void showErrorAlert () {
        useButton.setEnabled(true);
        closeButton.setEnabled(true);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.not_saved)
                .setNeutralButton(getResources().getString(R.string.ok), null)
                .show();
    }

    private void displayItemDetails (int position) {
        item = items.get(position);

        itemTitle.setText(item.getString("itemTitle"));
        itemDescription.setText(item.getString("itemSubtitle"));
        itemNum.setText("x" + item.getInt("numItemsOwned"));

        Utils.displayParseImage(item.getParseFile("itemImage"), itemImage);

        itemDetailView.setVisibility(View.VISIBLE);
    }

    public class InventoryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Globals.ITEM_DETAIL)) {
                displayItemDetails(intent.getIntExtra("position", 0));
            }

            if (intent.getAction().equals(Globals.ITEM_USED)) {

                if (intent.getBooleanExtra("itemUsed", false)) {
                    itemUsed();
                }
            }
        }
    }

}
