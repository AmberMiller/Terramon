package com.fullsail.terramon.Data.Objects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

/**
 * Created by Amber on 7/8/15.
 */
public class Achievement_Object {

    String achievementName;
    String achievementDescription;
    byte[] achievementImageTrue;
    byte[] achievementImageFalse;
    boolean achieved;

    public Achievement_Object(String _achievementName, String _achievementDescription,
                             ParseFile _achievementImageTrue, ParseFile _achievementImageFalse, boolean _achieved) {
        achievementName = _achievementName;
        achievementDescription = _achievementDescription;
        convertParseFileToByteArray(_achievementImageTrue, true);
        convertParseFileToByteArray(_achievementImageFalse, false);
        achieved = _achieved;
    }

    public String getAchievementName() {
        return achievementName;
    }

    public String getAchievementDescription() {
        return achievementDescription;
    }

    public byte[] getAchievementImageTrue() {
        return achievementImageTrue;
    }

    public byte[] getAchievementImageFalse() {
        return achievementImageFalse;
    }

    public int getAchievedNum() {
        if (achieved) {
            return 1;
        } else {
            return 0;
        }
    }

    public boolean getAchieved () {
        return achieved;
    }

    public byte[] getAchievementImage () {
        if (achieved) {
            return achievementImageTrue;
        } else {
            return achievementImageFalse;
        }
    }

    private void convertParseFileToByteArray (ParseFile image, final boolean trueFalse) {
        if (image != null) {
            image.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    if (e == null) {
                        if (trueFalse) {
                            achievementImageTrue = bytes;
                        } else {
                            achievementImageFalse = bytes;
                        }
                    }
                }
            });
        }
    }

    public Bitmap getItemImageBitmap (byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
