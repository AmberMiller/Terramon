package com.fullsail.terramon.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fullsail.terramon.Data.Objects.Achievement_Object;
import com.fullsail.terramon.R;

import java.util.List;

/**
 * Created by Amber on 7/22/15.
 */
public class Achievements_Adapter extends BaseAdapter {

    private static final String TAG = "ACHIEVEMENTS_ADAPTER";
    private static final int ID_CONSTANT = 0x003;

    Context context;
    List<Achievement_Object> achievements;

    public Achievements_Adapter (Context _context, List<Achievement_Object> _items) {
        context = _context;
        achievements = _items;
        Log.d(TAG, "LIST: " + achievements);
    }

    @Override
    public int getCount() {
        return achievements.size();
    }

    @Override
    public Achievement_Object getItem(int position) {
        return achievements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ID_CONSTANT + position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Achievement_Object achievement = getItem(position);
        Log.d(TAG, "CURRENT OBJECT: " + achievement);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_items_grid, parent, false);
        }

        TextView numItems = (TextView) convertView.findViewById(R.id.num_items);
        TextView itemName = (TextView) convertView.findViewById(R.id.item_name);
        ImageView itemImage = (ImageView) convertView.findViewById(R.id.item_image);

        itemName.setText(achievement.getAchievementName());
        numItems.setVisibility(View.GONE);
        itemImage.setImageBitmap(achievement.getItemImageBitmap(achievement.getAchievementImage()));

        return convertView;
    }
}
