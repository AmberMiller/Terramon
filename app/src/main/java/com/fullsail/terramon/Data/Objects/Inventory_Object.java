package com.fullsail.terramon.Data.Objects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

/**
 * Created by Amber on 7/8/15.
 */
public class Inventory_Object implements Parcelable {

    private int numItems;
    private String itemID;

    public Inventory_Object (int _numItems, String _itemID) {
        numItems = _numItems;
        itemID = _itemID;

    }

    public int getNumItems() {
        return numItems;
    }

    public String getItemID() {
        return itemID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
