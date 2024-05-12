package com.example.lyos.CustomAdapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.lyos.AllSearchResultFragment;
import com.example.lyos.DefaultSearchFragment;
import com.example.lyos.SearchOnYoutubeFragment;
import com.example.lyos.TracksSearchFragment;

public class SearchViewPaperAdapter extends FragmentPagerAdapter {
    final private int TAB_COUNT = 5;
    private String searchString = "";

    public SearchViewPaperAdapter(FragmentManager fm) {
        super(fm);
    }
    public SearchViewPaperAdapter(FragmentManager fm, String searchString) {
        super(fm);
        this.searchString = searchString;
    }
//
    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        if (position == 0) {
            fragment = new AllSearchResultFragment();
        } else if (position == 1) {
            fragment = new TracksSearchFragment();
        }
        else if (position == 2) {
            fragment = new AllSearchResultFragment();
        }
        else if (position == 3) {
            fragment = new AllSearchResultFragment();
        }
        else if (position == 4) {
            fragment = new SearchOnYoutubeFragment();
        }
        if(searchString.isEmpty()){
            fragment = new DefaultSearchFragment();
        }
        else{
            Bundle bundle = new Bundle();
            bundle.putString("search_string", searchString);
            fragment.setArguments(bundle);
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
