package com.fullsail.terramon.Activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;

/**
 * Created by hoytdingus on 7/9/15.
 */
public class Mini_Map_View extends MapView {


    final Path path = new Path();

    public Mini_Map_View(Context context, GoogleMapOptions options) {
        super(context, options);
    }


    @Override
    protected void dispatchDraw(Canvas circleMap) {

        MapView mapView = new MapView(getContext());
        circleMap.clipPath(path);
        path.addRoundRect(new RectF(0, 0, mapView.getWidth(), mapView.getHeight()), 10, 10, Path.Direction.CW);
        super.dispatchDraw(circleMap);

    }
}
