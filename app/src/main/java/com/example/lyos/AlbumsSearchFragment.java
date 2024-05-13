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

import com.example.lyos.CustomAdapters.AlbumRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.AlbumHandler;
import com.example.lyos.Models.Album;
import com.example.lyos.databinding.FragmentAlbumsSearchBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlbumsSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlbumsSearchFragment extends Fragment {
    private String searchString = "";
    private FragmentAlbumsSearchBinding fragmentAlbumsSearchBinding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AlbumsSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AlbumsSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlbumsSearchFragment newInstance(String param1, String param2) {
        AlbumsSearchFragment fragment = new AlbumsSearchFragment();
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
        arrayList = new ArrayList<>();
        fragmentAlbumsSearchBinding = FragmentAlbumsSearchBinding.inflate(getLayoutInflater());
        return fragmentAlbumsSearchBinding.getRoot();
    }
    private ArrayList<Album> arrayList;
    AlbumRecycleViewAdapter adapter;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        arrayList = new ArrayList<>();
        getDataFromFirestore();
    }

    private void getDataFromFirestore() {
        AlbumHandler handler = new AlbumHandler();
        handler.search(searchString).addOnCompleteListener(new OnCompleteListener<ArrayList<Album>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Album>> task) {
                if (task.isSuccessful()) {
                    Context context = getContext();
                    arrayList = task.getResult();
                    adapter = new AlbumRecycleViewAdapter(context, arrayList);
                    fragmentAlbumsSearchBinding.recycleViewItems.setAdapter(adapter);
                    fragmentAlbumsSearchBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                    fragmentAlbumsSearchBinding.recycleViewItems.setLayoutManager(mLayoutManager);
                    fragmentAlbumsSearchBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());

                } else {
                    //and more action --.--
                }
            }
        });
    }
}