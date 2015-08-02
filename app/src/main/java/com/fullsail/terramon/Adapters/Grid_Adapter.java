package com.fullsail.terramon.Adapters;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fullsail.terramon.Fragments.Monsters_Fragment;
import com.fullsail.terramon.Globals.Globals;
import com.fullsail.terramon.Globals.Utils;
import com.fullsail.terramon.R;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Amber on 7/8/15.
 */
public class Grid_Adapter extends BaseAdapter {

    private static final String TAG = "GRID_ADAPTER";
    private static final int ID_CONSTANT = 0x001;

    Context context;
    List<ParseObject> list;
    int currentScreen;
    ParseFile image;

    public Grid_Adapter(Context _context, List<ParseObject> _list, int _currentScreen) {
        context = _context;
        list = _list;
        currentScreen = _currentScreen;

        Log.d(TAG, "GRID ADAPTER LIST: " + list);
    }

    @Override
    public int getCount() {
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

    @Override
    public ParseObject getItem(int position) {
        if (list != null && position < list.size() && position >= 0) {
            return list.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return ID_CONSTANT + position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ParseObject object = getItem(position);
        Log.d(TAG, "CURRENT OBJECT: " + object);

        switch (currentScreen) {
            case 0: case 3: //Inventory and Achievements
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.layout_items_grid, parent, false);
                }

                ImageView itemImage = (ImageView) convertView.findViewById(R.id.item_image);
                image = object.getParseFile("itemImage");
                Utils.displayParseImage(image, itemImage);

                TextView numItems = (TextView) convertView.findViewById(R.id.num_items);
                TextView itemName = (TextView) convertView.findViewById(R.id.item_name);

                itemName.setText(object.getString("itemTitle"));
                numItems.setText("x" + object.getInt("numItemsOwned"));

                if (currentScreen == 3) {
                    numItems.setVisibility(View.GONE);
                }

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currentScreen == 0) {
                            Log.d(TAG, "ITEM CLICKED");
                            Intent intent = new Intent(Globals.ITEM_DETAIL);
                            intent.putExtra("position", position);
                            context.sendBroadcast(intent);
                        }
                    }
                });

                break;
            case 1: //Monster
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.layout_monsters_grid, parent, false);
                }

                ImageView monsterView = (ImageView) convertView.findViewById(R.id.monster_view);
                ImageView monsterImage = (ImageView) convertView.findViewById(R.id.monster_image);

                switch (object.getString("monsterType")) {
                    case "Fire":
                        monsterView.setImageResource(R.drawable.monster_view_fire);
                        break;
                    case "Water":
                        monsterView.setImageResource(R.drawable.monster_view_water);
                        break;
                    case "Earth":
                        monsterView.setImageResource(R.drawable.monster_view_earth);
                        break;
                    case "Air":
                        monsterView.setImageResource(R.drawable.monster_view_air);
                        break;
                    case "Psychic":
                        monsterView.setImageResource(R.drawable.monster_view_psychic);
                        break;
                }

                image = object.getParseFile("monsterImage");
                Utils.displayParseImage(image, monsterImage);

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "MONSTER CLICKED");
                        Intent intent = new Intent(Globals.MONSTER_DETAIL);
                        intent.putExtra("position", position);
                        context.sendBroadcast(intent);
                    }
                });

                break;
            case 2: //Shop
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.layout_shop_grid, parent, false);
                }

                final String itemID = object.getObjectId();
                final TextView shopTitle = (TextView) convertView.findViewById(R.id.shop_title);
                TextView shopTitle2 = (TextView) convertView.findViewById(R.id.shop_title2);
                final TextView shopPrice = (TextView) convertView.findViewById(R.id.shop_price);

                shopTitle.setText(object.getString("itemTitle"));
                shopTitle2.setText(object.getString("itemSubtitle"));
                shopPrice.setText(context.getResources().getString(R.string.money_sign) + object.getDouble("itemCost"));

                final ImageView shopItemImage = (ImageView) convertView.findViewById(R.id.shop_item_image);
                image = object.getParseFile("itemImage");
                Utils.displayParseImage(image, shopItemImage);

                ImageButton buyButton = (ImageButton) convertView.findViewById(R.id.buy_button);
                buyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Buy Button Clicked");

                        /* Purchase Confirmation Alert */
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.purchase)
                                .setMessage(context.getResources().getString(R.string.buy)
                                        + " " + shopTitle.getText()
                                        + " " + context.getResources().getString(R.string.buy2)
                                        + " " + shopPrice.getText()
                                        + context.getResources().getString(R.string.question))
                                .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d(TAG, "Buying Item");
                                        itemPurchased(itemID);
                                    }
                                })
                                .setNegativeButton(context.getResources().getString(R.string.cancel), null)
                                .show();
                    }
                });
        }
                return convertView;
    }

    /* Adds or edits itemsOwned with purchased itemID */
    private void itemPurchased (String itemID) {
        ParseUser user = ParseUser.getCurrentUser();
        if (itemID.equals("1J9PEAK7Yw")) { //If itemID is for unlock full purchase, save true to user
            Log.d(TAG, "User Purchasing Full Version");
            for (ParseObject object : list) {
                if (object.getObjectId().equals(itemID)) {
                    list.remove(object);
                    this.notifyDataSetChanged();
                }
            }

            user.put("unlockedFullVersion", true);
            user.saveEventually();
            purchaseSuccessful(true);
        } else { //If anything but unlock full purchase, update or add itemsOwned JSONArray
            Log.d(TAG, "User Purchased Item: " + itemID);
            try {
                JSONArray array = user.getJSONArray("itemsOwned");
                boolean updated = false;

                if (array != null) { //If array is not null, loop through and update

                    Log.d(TAG, "Items Owned: " + array);
                    JSONArray owned = array.getJSONArray(0);

                    for (int i = 0; i < owned.length(); i++) {
                        JSONObject object = owned.getJSONObject(i);
                        if (object.getString("itemID").equals(itemID)) {
                            int num = object.getInt("numItems");
                            num++;
                            object.put("numItems", num);
                            updated = true;
                        }
                    }

                    if (!updated) { //If after loop, item did not update, add item to array
                        Log.d(TAG, "itemOwned array is null");
                        user.add("itemsOwned", Arrays.asList(new JSONObject()
                                .put("numItems", 1)
                                .put("itemID", itemID)));
                    }
                } else { //If array is null, add item to array
                    Log.d(TAG, "itemOwned array is null");
                    user.add("itemsOwned", Arrays.asList(new JSONObject()
                            .put("numItems", 1)
                            .put("itemID", itemID)));
                }

                user.saveEventually();
                purchaseSuccessful(false);
            } catch (Exception e) {
                e.printStackTrace();
                new AlertDialog.Builder(context)
                        .setTitle(R.string.not_saved)
                        .setNeutralButton(context.getResources().getString(R.string.ok), null)
                        .show();
            }
        }
    }

    /* Show Alert Dialog on purchase success */
    private void purchaseSuccessful (boolean upgrade) {
        Log.d(TAG, "Purchase Successful");
        if (upgrade) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.upgrade_success)
                    .setNeutralButton(context.getResources().getString(R.string.ok), null)
                    .show();
        } else {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.purchase_succes)
                    .setNeutralButton(context.getResources().getString(R.string.ok), null)
                    .show();
        }
    }
}
