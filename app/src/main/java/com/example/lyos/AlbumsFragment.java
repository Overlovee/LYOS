package com.example.lyos;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lyos.CustomAdapters.AlbumRecycleViewAdapter;
import com.example.lyos.CustomAdapters.PlaylistRecycleViewAdapter;
import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.AlbumHandler;
import com.example.lyos.FirebaseHandlers.PlaylistHandler;
import com.example.lyos.Models.Album;
import com.example.lyos.Models.Playlist;
import com.example.lyos.Models.ProfileDataLoader;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.databinding.FragmentAlbumsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlbumsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlbumsFragment extends Fragment {
    private FragmentAlbumsBinding fragmentAlbumsBinding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AlbumsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AlbumsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlbumsFragment newInstance(String param1, String param2) {
        AlbumsFragment fragment = new AlbumsFragment();
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
        fragmentAlbumsBinding = FragmentAlbumsBinding.inflate(getLayoutInflater());
        return fragmentAlbumsBinding.getRoot();
    }
    private ArrayList<Song> tracksArrayList = new ArrayList<>();
    private ArrayList<Album> albumArrayList = new ArrayList<>();
    SongRecycleViewAdapter trackAdapter;
    AlbumRecycleViewAdapter albumAdapter;
    private UserInfo user;
    Context context;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();

        // Kiểm tra xem có dữ liệu tài khoản đã được lưu trữ hay không
        ProfileDataLoader.loadProfileData(requireContext(), new ProfileDataLoader.OnProfileDataLoadedListener() {
            @Override
            public void onProfileDataLoaded(UserInfo u) {
                user = u;
                if (user != null){
                    //getUserTracksDataFromFirestore();
                    getAlbumsDataFromFirestore();

                    addEvents();
                }
                else {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onProfileDataLoadFailed() {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
    private void getAlbumsDataFromFirestore() {
        AlbumHandler handler = new AlbumHandler();
        handler.searchByUserID(user.getId()).addOnCompleteListener(new OnCompleteListener<ArrayList<Album>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Album>> task) {
                if (task.isSuccessful()) {
                    albumArrayList = task.getResult();
                    albumAdapter = new AlbumRecycleViewAdapter(context, albumArrayList);
                    fragmentAlbumsBinding.textViewZero.setVisibility(View.GONE);
                    fragmentAlbumsBinding.recycleViewItems.setVisibility(View.VISIBLE);
                    fragmentAlbumsBinding.recycleViewItems.setAdapter(albumAdapter);
                    fragmentAlbumsBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                    fragmentAlbumsBinding.recycleViewItems.setLayoutManager(mLayoutManager);
                    fragmentAlbumsBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());

                } else {
                    //and more action --.--
                    fragmentAlbumsBinding.textViewZero.setVisibility(View.VISIBLE);
                    fragmentAlbumsBinding.recycleViewItems.setVisibility(View.GONE);
                }
            }
        });
    }
    //    private void getUserTracksDataFromFirestore() {
//        SongHandler handler = new SongHandler();
//        handler.searchByUserID(user.getId(), 6).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
//            @Override
//            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
//                if (task.isSuccessful()) {
//                    tracksArrayList = task.getResult();
//                    if(!tracksArrayList.isEmpty()){
//                        fragmentPlaylistsBinding.layoutTracks.setVisibility(View.VISIBLE);
//                        trackAdapter = new SongRecycleViewAdapter(context, tracksArrayList);
//                        fragmentPlaylistsBinding.recycleViewTrackItems.setAdapter(trackAdapter);
//                        fragmentPlaylistsBinding.recycleViewTrackItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
//                        RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
//                        fragmentPlaylistsBinding.recycleViewTrackItems.setLayoutManager(mLayoutManager);
//                        fragmentPlaylistsBinding.recycleViewTrackItems.setItemAnimator(new DefaultItemAnimator());
//                    } else {
//                        fragmentPlaylistsBinding.layoutTracks.setVisibility(View.GONE);
//                    }
//                } else {
//                    //and more action --.--
//                    fragmentPlaylistsBinding.layoutTracks.setVisibility(View.GONE);
//                }
//            }
//        });
//    }
    private void addEvents(){

    }
}