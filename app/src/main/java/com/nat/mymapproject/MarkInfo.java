package com.nat.mymapproject;


import com.google.android.gms.maps.model.LatLng;

public class MarkInfo {
    public double latitude;
    public double longitude;
    public String title;
    public String text;
    public String address;
    public String user_name;
    private String key = "";


    public MarkInfo(){
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.user_name = MapsActivity.DEFAULT_NAME;
        this.address = "";
        this.title = "Title";
        this.text = "Text";
    }
    public MarkInfo(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
        this.user_name = MapsActivity.DEFAULT_NAME;
        this.address = "";
        this.title = "Title";
        this.text = "Text";
    }

    public MarkInfo(double latitude, double longitude, String user_name, String address){
        this.latitude = latitude;
        this.longitude = longitude;
        this.user_name = user_name;
        this.address = address;
        this.title = "Title";
        this.text = "Text";
    }

    public MarkInfo(LatLng latLng, String user_name, String address){
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.user_name = user_name;
        this.address = address;
        this.title = "Title";
        this.text = "Text";
    }

    public String hashKey(LatLng latLng){
        String mm = String.valueOf(latLng.longitude);
        String mm1 = String.valueOf(latLng.latitude);
        mm = mm.replace('.', '-');
        mm1 = mm1.replace('.', '-');

        key = mm + mm1;

        return key;
    }


    public String getKey(){
        return key;
    }





}
