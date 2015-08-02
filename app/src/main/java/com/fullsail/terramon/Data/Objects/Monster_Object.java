package com.fullsail.terramon.Data.Objects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

/**
 * Created by Amber on 7/8/15.
 */
public class Monster_Object {

    private int numMonsters;
    private String monsterID;
    private int monsterNum;
    private String monsterType;
    private String monsterName;
    private String monsterDescription;
    private String monsterHeight;
    private String monsterWeight;
    private ParseFile monsterImage;
    private byte[] imageBytes;

    private Double latitude;
    private Double longitude;

    /* Object from Parse in service */
    public Monster_Object (String _monsterID, int _numMonsters, int _monsterNum, String _monsterType, String _monsterName,
                           String _monsterDescription, String _monsterHeight, String _monsterWeight, ParseFile _monsterImage,
                           Double _latitude, Double _longitude) {
        numMonsters = _numMonsters;
        monsterID = _monsterID;
        monsterNum = _monsterNum;
        monsterType = _monsterType;
        monsterName = _monsterName;
        monsterDescription = _monsterDescription;
        monsterHeight = _monsterHeight;
        monsterWeight = _monsterWeight;
        monsterImage = _monsterImage;

        latitude = _latitude;
        longitude = _longitude;
    }

    public Monster_Object (String _monsterID, int _numMonsters, int _monsterNum, String _monsterType, String _monsterName,
                           String _monsterDescription, String _monsterHeight, String _monsterWeight, byte[] _monsterImage,
                           Double _latitude, Double _longitude) {
        numMonsters = _numMonsters;
        monsterID = _monsterID;
        monsterNum = _monsterNum;
        monsterType = _monsterType;
        monsterName = _monsterName;
        monsterDescription = _monsterDescription;
        monsterHeight = _monsterHeight;
        monsterWeight = _monsterWeight;
        imageBytes = _monsterImage;

        latitude = _latitude;
        longitude = _longitude;
    }

    public Monster_Object (String _monsterID, int _numMonsters, int _monsterNum, String _monsterType, String _monsterName,
                           String _monsterDescription, String _monsterHeight, String _monsterWeight, ParseFile _monsterImage) {
        numMonsters = _numMonsters;
        monsterID = _monsterID;
        monsterNum = _monsterNum;
        monsterType = _monsterType;
        monsterName = _monsterName;
        monsterDescription = _monsterDescription;
        monsterHeight = _monsterHeight;
        monsterWeight = _monsterWeight;
        monsterImage = _monsterImage;

        Log.d("MONSTER_OBJECT", "MONSTER IMAGE: " + monsterImage);

//        convertParseFileToByteArray(_monsterImage);
    }

    public Monster_Object (String _monsterID, int _numMonsters, int _monsterNum, String _monsterType, String _monsterName,
                           String _monsterDescription, String _monsterHeight, String _monsterWeight, byte[] _monsterImage) {
        numMonsters = _numMonsters;
        monsterID = _monsterID;
        monsterNum = _monsterNum;
        monsterType = _monsterType;
        monsterName = _monsterName;
        monsterDescription = _monsterDescription;
        monsterHeight = _monsterHeight;
        monsterWeight = _monsterWeight;
        imageBytes = _monsterImage;
    }

    public int getNumMonsters() {
        return numMonsters;
    }

    public String getMonsterID() {
        return monsterID;
    }

    public int getMonsterNum() {
        return monsterNum;
    }

    public String getMonsterType() {
        return monsterType;
    }

    public String getMonsterName() {
        return monsterName;
    }

    public String getMonsterDescription() {
        return monsterDescription;
    }

    public String getMonsterHeight() {
        return monsterHeight;
    }

    public String getMonsterWeight() {
        return monsterWeight;
    }

    public ParseFile getMonsterImage() {
        return monsterImage;
    }

    public byte[] getMonsterBytes () {
        return imageBytes;
    }

    private void convertParseFileToByteArray (ParseFile image) {
        if (image != null) {
            image.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    if (e == null) {
                        Log.d("MONSTER_OBJECT", "DATA DOWNLOADED");
                        imageBytes = bytes;
                    }
                }
            });
        }
    }
//
//    public Bitmap getMonsterImageBitmap () {
//        if (imageBytes == null) {
//            convertParseFileToByteArray(monsterImage);
//        }
//        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }


}
