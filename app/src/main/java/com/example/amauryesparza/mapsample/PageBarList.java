package com.example.amauryesparza.mapsample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by AmauryEsparza on 12/8/15.
 */
public class PageBarList extends Fragment {

    public PageBarList(){}

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.pager_barlist, container, false);
    }
}
