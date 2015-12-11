package com.example.amauryesparza.mapsample;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by AmauryEsparza on 12/8/15.
 */
public class PageMap extends Fragment  {

    SupportMapFragment mapView;
    GoogleMap map;

    public PageMap() {
    }

    public void onAttach(Context context){
        super.onAttach(context);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.activity_maps, container, false);

        mapView = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        map = mapView.getMap();


        return fragmentView;
    }
}

