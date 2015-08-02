package com.fullsail.terramon.Fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.fullsail.terramon.Adapters.Grid_Adapter;
import com.fullsail.terramon.Globals.Utils;
import com.fullsail.terramon.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class Shop_Fragment extends Fragment {

//region Variables
    public static final String TAG = "SHOP_FRAGMENT";

    boolean connected;

    /* UI Elements */
    GridView shopGridView;

//endregion

//region Fragment Setup
    public static Shop_Fragment newInstance () {
        return new Shop_Fragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        connected = Utils.checkConnection(getActivity());
        if (!connected) {
            return inflater.inflate(R.layout.fragment_shop_noconnection, container, false);
        }
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        shopGridView = (GridView) getActivity().findViewById(R.id.shopGridView);

        if (connected) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ShopClass");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (e == null) {
                        Log.d(TAG, "SHOP ITEMS PULLED: " + list);
                        ParseUser user = ParseUser.getCurrentUser();
                        ArrayList<ParseObject> items = new ArrayList<ParseObject>();
                        boolean userUnlocked = user.getBoolean("unlockedFullVersion");

                        for (int i = 0; i < list.size(); i++) {
                            ParseObject item = list.get(i);
                            if (userUnlocked) {
                                if (!item.getObjectId().equals("1J9PEAK7Yw")) {
                                    items.add(item);
                                }
                            } else {
                                items.add(item);
                            }
                        }
                        Grid_Adapter adapter = new Grid_Adapter(getActivity(), items, 2);
                        shopGridView.setAdapter(adapter);
                    } else {
                        Log.d(TAG, "ERROR PULLING SHOP DATA: " + e);
                    }
                }
            });
        }
    }

//endregion

}
