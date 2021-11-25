package com.phipemc.notesapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.phipemc.notesapp.activities.NotasFragment;
import com.phipemc.notesapp.activities.TaskFragment;

public class PagerAdapter extends FragmentPagerAdapter {

    private int numofTb;

    public PagerAdapter( FragmentManager fm, int x) {
        super(fm);
        this.numofTb = x;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return new NotasFragment();
            case 1:
                return new TaskFragment();
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return numofTb;
    }
}
