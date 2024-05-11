package com.example.lyos.CustomAdapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.lyos.AllSearchResultFragment;

public class SearchViewPaperAdapter extends FragmentPagerAdapter {
    final private int TAB_COUNT = 5;
    public SearchViewPaperAdapter(FragmentManager fm) {
        super(fm);
    }
    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0) {
            fragment = new AllSearchResultFragment();
        } else if (position == 1) {
            fragment = new AllSearchResultFragment();
        }
        else if (position == 2) {
            fragment = new AllSearchResultFragment();
        }
        else if (position == 3) {
            fragment = new AllSearchResultFragment();
        }
        else if (position == 4) {
            fragment = new AllSearchResultFragment();
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
            title = "All";
        } else if (position == 1) {
            title = "Tracks";
        }
        else if (position == 2) {
            title = "Users";
        }
        else if (position == 3) {
            title = "Albums";
        }
        else if (position == 4) {
            title = "From Youtube";
        }
        return title;
    }
}
