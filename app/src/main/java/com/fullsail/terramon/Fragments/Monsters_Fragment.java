package com.fullsail.terramon.Fragments;


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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fullsail.terramon.Adapters.Grid_Adapter;
import com.fullsail.terramon.Globals.Globals;
import com.fullsail.terramon.Globals.Utils;
import com.fullsail.terramon.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;


public class Monsters_Fragment extends Fragment {

//region Variables
    public static final String TAG = "MONSTERS_FRAGMENT";

    MonsterBroadcast receiver = new MonsterBroadcast();
    ArrayList<ParseObject> monsters = new ArrayList<>();
    Grid_Adapter adapter;

    /* UI Elements */
    GridView monstersGridView;
    LinearLayout monster_details;

//endregion

//region Fragment Setup
    public static Monsters_Fragment newInstance () {
        return new Monsters_Fragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monsters, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getMonsterList();

        monstersGridView = (GridView) getActivity().findViewById(R.id.monstersGridView);

        adapter = new Grid_Adapter(getActivity(), monsters, 1);
        monstersGridView.setAdapter(adapter);

        monster_details = (LinearLayout) getActivity().findViewById(R.id.monster_details);
        monster_details.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Globals.MONSTER_DETAIL);

        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(receiver);

        super.onPause();
    }

    private void getMonsterList () {
        ParseUser user = ParseUser.getCurrentUser();
        ArrayList<String> caught = (ArrayList<String>) user.get("monstersCaught");
        if (caught != null) {
            Log.d(TAG, "Caught: " + caught);
            for (String id : caught) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("MonsterClass");
                query.fromLocalDatastore();
                query.getInBackground(id, new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            monsters.add(parseObject);
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "MONSTER PULL ERROR: " + e);
                        }
                    }
                });
            }
        }
    }

    private void displayMonsterDetails (int position) {
        ParseObject object = monsters.get(position);

        ImageView detailViewLeft = (ImageView) getActivity().findViewById(R.id.monster_detail_view_left);
        ImageView detailViewRight = (ImageView) getActivity().findViewById(R.id.monster_detail_view_right);
        ImageButton closeButton = (ImageButton) getActivity().findViewById(R.id.detail_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monster_details.setVisibility(View.GONE);
            }
        });

        ImageView detailMonsterImage = (ImageView) getActivity().findViewById(R.id.monster_detail_image);
        TextView detailName = (TextView) getActivity().findViewById(R.id.monster_detail_name);
        TextView detailNumber = (TextView) getActivity().findViewById(R.id.monster_detail_number);
        TextView detailDescription = (TextView) getActivity().findViewById(R.id.monster_detail_description);
        TextView detailHeight = (TextView) getActivity().findViewById(R.id.monster_detail_height);
        TextView detailWeight = (TextView) getActivity().findViewById(R.id.monster_detail_weight);

        switch (object.getString("monsterType")) {
            case "Fire":
                detailViewLeft.setImageResource(R.drawable.details_fire_left);
                detailViewRight.setImageResource(R.drawable.details_fire_right);
                break;
            case "Water":
                detailViewLeft.setImageResource(R.drawable.details_water_left);
                detailViewRight.setImageResource(R.drawable.details_water_right);
                break;
            case "Earth":
                detailViewLeft.setImageResource(R.drawable.details_earth_left);
                detailViewRight.setImageResource(R.drawable.details_earth_right);
                break;
            case "Air":
                detailViewLeft.setImageResource(R.drawable.details_air_left);
                detailViewRight.setImageResource(R.drawable.details_air_right);
                break;
            case "Psychic":
                detailViewLeft.setImageResource(R.drawable.details_psychic_left);
                detailViewRight.setImageResource(R.drawable.details_psychic_right);
                break;
        }

        Utils.displayParseImage(object.getParseFile("monsterImage"), detailMonsterImage);
        detailName.setText(object.getString("monsterName"));
        detailNumber.setText("#" + object.getInt("monsterNum"));
        detailDescription.setText(object.getString("monsterDescription"));
        detailHeight.setText(object.getString("monsterHeight"));
        detailWeight.setText(object.getString("monsterWeight"));

        monster_details.setVisibility(View.VISIBLE);
    }
//endregion

//region Receiver
    public class MonsterBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Globals.MONSTER_DETAIL)) {
            displayMonsterDetails(intent.getIntExtra("position", 0));
        }
    }
}
//endregion
}
