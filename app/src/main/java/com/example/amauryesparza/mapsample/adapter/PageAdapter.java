package com.example.amauryesparza.mapsample.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.amauryesparza.mapsample.PageBarList;
import com.example.amauryesparza.mapsample.PageMap;

/**
 * Created by AmauryEsparza on 12/8/15.
 */
public class PageAdapter  extends FragmentStatePagerAdapter {

    private int numTabs;

    public PageAdapter(FragmentManager fragmentManager, int numTabs){
        super(fragmentManager);
        this.numTabs = numTabs;
    }

    @Override
    public Fragment getItem(int position){
        switch(position){
            case 0:
                return new PageBarList();
            default:
                return new PageMap();
        }
    }

    @Override
    public int getCount(){
        return numTabs;
    }
}

