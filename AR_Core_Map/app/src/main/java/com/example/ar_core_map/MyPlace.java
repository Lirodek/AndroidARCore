package com.example.ar_core_map;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class MyPlace {

    String title;
    LatLng latLng;
    double latitude, longitute;

    double[] arPos;
    int rate = 10;
    int color;

    public MyPlace(String title, double latitude, double longitute, int color){
        this.title = title;
        this.latLng = new LatLng(latitude, longitute);
        this.latitude = latitude;
        this.longitute = longitute;
        this.color = color;

    }

    public void setArPosition(Location currentLocation, float[] mePos){
        arPos = new double[]{
                mePos[0]+(currentLocation.getLongitude()-longitute)*rate,
                mePos[1]+(currentLocation.getLatitude()-latitude)*rate,
                mePos[2]*3
        };
    }

}
