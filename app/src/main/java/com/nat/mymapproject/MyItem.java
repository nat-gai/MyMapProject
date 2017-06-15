package com.nat.mymapproject;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {
    public final LatLng mPosition;
    private int accessCode;
    //1 - private


    public MyItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public MyItem(LatLng latLng, int accessCode) {
        mPosition = latLng;
        this.accessCode = accessCode;
    }

    public int getAccessCode(){
        return accessCode;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
