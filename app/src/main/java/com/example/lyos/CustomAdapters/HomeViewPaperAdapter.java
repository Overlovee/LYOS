package com.example.lyos.CustomAdapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.lyos.TheMostLikedSongsFragment;
import com.example.lyos.TheMostListenedSongsFragment;

public class HomeViewPaperAdapter extends FragmentPagerAdapter {
    final private int TAB_COUNT = 2;

    public HomeViewPaperAdapter(FragmentManager fm) {
        super(fm);
    }
//
    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        if (position == 0) {
            fragment = new TheMostListenedSongsFragment();
        } else if (position == 1) {
            fragment = new TheMostLikedSongsFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0) {
            title = "By Listens";
        } else if (position == 1) {
            title = "By Likes";
        }
        return title;
    }
}
