package com.example.lyos;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.Models.Song;
import com.example.lyos.databinding.FragmentSearchByTagBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchByTagFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchByTagFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private FragmentSearchByTagBinding fragmentSearchByTagBinding;
    private String tag;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SearchByTagFragment() {
        // Required empty public constructor
        tag = "";
    }
    public SearchByTagFragment(String tag) {
        // Required empty public constructor
        this.tag = tag;
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchByTagFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchByTagFragment newInstance(String param1, String param2) {
        SearchByTagFragment fragment = new SearchByTagFragment();
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentSearchByTagBinding = FragmentSearchByTagBinding.inflate(getLayoutInflater());
        return fragmentSearchByTagBinding.getRoot();
    }

    private ArrayList<Song> arrayList = new ArrayList<>();
    SongRecycleViewAdapter adapter;
    Context context;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        if(!tag.equals("")){
            getDataFromFirestore();
            addEvents();
        }
        else {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
    private void getDataFromFirestore() {
        SongHandler handler = new SongHandler();
        handler.searchByTag(tag).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
                if (task.isSuccessful()) {
                    arrayList = task.getResult();
                    adapter = new SongRecycleViewAdapter(context, arrayList);
                    fragmentSearchByTagBinding.recycleViewItems.setAdapter(adapter);
                    fragmentSearchByTagBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                    fragmentSearchByTagBinding.recycleViewItems.setLayoutManager(mLayoutManager);
                    fragmentSearchByTagBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());

                } else {
                    //and more action --.--
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
    private void addEvents(){
        fragmentSearchByTagBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.showToolbar();
                    getActivity().getSupportFragmentManager().popBackStack();
                    mainActivity.loadFragment(new SearchFragment());
                }

            }
        });
    }
}