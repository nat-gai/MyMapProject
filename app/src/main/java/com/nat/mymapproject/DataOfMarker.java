package com.nat.mymapproject;

public class DataOfMarker {
    private double lat;
    private double lng;
    private String title;
    private String text;

    DataOfMarker(){
        lat = 0.0;
        lng = 0.0;
        title = "Cat";
        text = "blabla";
        }

    DataOfMarker(double lat, double lng, String title, String text){
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.text = text;
    }

    DataOfMarker(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
        title = "Cat";
        text = "blabla";
    }


}
