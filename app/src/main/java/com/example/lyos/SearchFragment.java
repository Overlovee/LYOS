package com.example.lyos;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lyos.CustomAdapters.SearchViewPaperAdapter;
import com.example.lyos.databinding.FragmentSearchBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    final private int TAB_COUNT = 5;
    private FragmentSearchBinding fragmentSearchBinding;
    private String searchString = "";
    SearchViewPaperAdapter viewPagerAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            this.searchString = getArguments().getString("search_string", "");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentSearchBinding = FragmentSearchBinding.inflate(getLayoutInflater());
        return fragmentSearchBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(searchString.isEmpty()){
            viewPagerAdapter = new SearchViewPaperAdapter(getChildFragmentManager());
            fragmentSearchBinding.searchViewPager.setAdapter(viewPagerAdapter);
            fragmentSearchBinding.searchTab.setupWithViewPager(fragmentSearchBinding.searchViewPager);
            fragmentSearchBinding.searchViewPager.setOffscreenPageLimit(TAB_COUNT);
            fragmentSearchBinding.searchTab.setVisibility(View.GONE);

        }
        else {
            viewPagerAdapter = new SearchViewPaperAdapter(getChildFragmentManager(), searchString);

            fragmentSearchBinding.searchViewPager.setAdapter(viewPagerAdapter);
            fragmentSearchBinding.searchTab.setupWithViewPager(fragmentSearchBinding.searchViewPager);
            fragmentSearchBinding.searchViewPager.setOffscreenPageLimit(TAB_COUNT);
        }


    }
}