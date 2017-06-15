package com.nat.mymapproject;

import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class MyClusterRenderer extends DefaultClusterRenderer<MyItem> {

    public MyClusterRenderer(Context context, GoogleMap map,
                           ClusterManager<MyItem> clusterManager) {
        super(context, map, clusterManager);
    }

/*
    @Override
    protected int getColor(int clusterSize) {
        return Color.parseColor("#567238");
    }

*/
    @Override
    protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {

        if (item.getAccessCode() == MapsActivity.PRIVATE){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            markerOptions.alpha((float) 0.8);
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            markerOptions.alpha((float) 0.8);
        }
        super.onBeforeClusterItemRendered(item, markerOptions);
    }


}
