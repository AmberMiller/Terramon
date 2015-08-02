package com.fullsail.terramon.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fullsail.terramon.Data.Objects.Achievement_Object;
import com.fullsail.terramon.Data.Objects.Inventory_Object;
import com.fullsail.terramon.R;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Amber on 7/21/15.
 */
public class Inventory_Adapter extends BaseAdapter {

    private static final String TAG = "INVENTORY_ADAPTER";
    private static final int ID_CONSTANT = 0x002;

    Context context;
    List <Inventory_Object> items;

    public Inventory_Adapter(Context _context, List<Inventory_Object> _items) {
        context = _context;
        items = _items;
        Log.d(TAG, "LIST: " + items);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Inventory_Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ID_CONSTANT + position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Inventory_Object item = getItem(position);
        Log.d(TAG, "CURRENT OBJECT: " + item);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_items_grid, parent, false);
        }

        TextView numItems = (TextView) convertView.findViewById(R.id.num_items);
        TextView itemName = (TextView) convertView.findViewById(R.id.item_name);
        ImageView itemImage = (ImageView) convertView.findViewById(R.id.item_image);

//        itemName.setText(item.getItemName());
//        numItems.setText(item.getItemDescription());
//        itemImage.setImageBitmap(item.getItemImageBitmap());

        return convertView;
    }

}
