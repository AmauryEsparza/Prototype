package com.example.amauryesparza.mapsample;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.amauryesparza.mapsample.model.Bar;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by AmauryEsparza on 12/8/15.
 */
public class PageMap extends Fragment {

    private SupportMapFragment mapView;
    private ClusterManager<?> mClusterManager;
    private GoogleMap map;

    public PageMap() {
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.activity_maps, container, false);
        mapView = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        map = mapView.getMap();
        return fragmentView;
    }

//    private void setUpCluster(){
//        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(21.884115, -102.291805));
//        CameraUpdate zoom = CameraUpdateFactory.zoomTo(13.5f);
//        map.moveCamera(center);
//        map.animateCamera(zoom);
//        mClusterManager = new ClusterManager<Bar>(getContext(), map);
//        mClusterManager.setRenderer();
//    }

//    private class BarRender extends DefaultClusterRenderer<Bar>{
//
//    }
}

