package com.fullsail.terramon.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fullsail.terramon.Data.Objects.Monster_Object;
import com.fullsail.terramon.Globals.Utils;
import com.fullsail.terramon.R;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Amber on 7/21/15.
 */
public class Monster_Adapter extends BaseAdapter {

    private static final String TAG = "MONSTER_ADAPTER";
    private static final int ID_CONSTANT = 0x001;

    Context context;
    List<Monster_Object> monsters;

    public Monster_Adapter(Context _context, List<Monster_Object> _monsters) {
        context = _context;
        monsters = _monsters;
        Log.d(TAG, "LIST: " + monsters);
    }

    @Override
    public int getCount() {
        return monsters.size();
    }

    @Override
    public Monster_Object getItem(int position) {
        return monsters.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ID_CONSTANT + position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Monster_Object monster = getItem(position);
        Log.d(TAG, "CURRENT OBJECT: " + monster);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_monsters_grid, parent, false);
        }

        ImageView monsterView = (ImageView) convertView.findViewById(R.id.monster_view);
        ImageView monsterImage = (ImageView) convertView.findViewById(R.id.monster_image);

        switch (monster.getMonsterType()) {
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

        Utils.displayParseImage(monster.getMonsterImage(), monsterImage);

        return convertView;
    }
}
