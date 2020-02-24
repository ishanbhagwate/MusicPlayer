package com.shivam.musicplayer.Adapters;

import com.shivam.musicplayer.Fragments.AllSongsFragment;
import com.shivam.musicplayer.Fragments.LastAddedFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {


    public ViewPagerAdapter( FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){

            case 0:
                AllSongsFragment allSongsFragment = new AllSongsFragment();
                return allSongsFragment;
            case 1:
                LastAddedFragment lastAddedFragment = new LastAddedFragment();
                return lastAddedFragment;
                default:
                    return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }
}
