package com.example.amauryesparza.mapsample.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by AmauryEsparza on 12/10/15.
 */
public class Bar implements ClusterItem{
    String name;
    String address;
    private LatLng mLocation;
    // ... and so on
    int photoId;

    Bar(String name, String address, int photoId, LatLng location) {
        this.name = name;
        this.address = address;
        this.photoId = photoId;
        this.mLocation = location;
    }

    @Override
    public LatLng getPosition(){
        return mLocation;
    }
}
