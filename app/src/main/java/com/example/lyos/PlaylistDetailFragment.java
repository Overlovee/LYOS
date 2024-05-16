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

import com.bumptech.glide.Glide;
import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.Models.Playlist;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.databinding.FragmentPlaylistDetailBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlaylistDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaylistDetailFragment extends Fragment {

    private FragmentPlaylistDetailBinding fragmentPlaylistDetailBinding;
    private Playlist playlist;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PlaylistDetailFragment() {
        // Required empty public constructor
    }
    public PlaylistDetailFragment(Playlist playlist) {
        this.playlist = playlist;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlaylistDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlaylistDetailFragment newInstance(String param1, String param2) {
        PlaylistDetailFragment fragment = new PlaylistDetailFragment();
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
        fragmentPlaylistDetailBinding = FragmentPlaylistDetailBinding.inflate(getLayoutInflater());
        return fragmentPlaylistDetailBinding.getRoot();
    }
    private UserInfo currentUserInfo = new UserInfo();
    private ArrayList<Song> arrayList = new ArrayList<>();
    SongRecycleViewAdapter adapter;
    Context context;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        if(playlist != null){
            setUpUI();
            addEvents();
            if(playlist.getSongList().size() > 0){
                getDataFromFirestore();
            }
        }
        else {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
    private void setUpUI(){
        fragmentPlaylistDetailBinding.textViewTitle.setText(playlist.getTitle());
        fragmentPlaylistDetailBinding.textViewTracks.setText("Playlist: " + String.valueOf(playlist.getSongList().size()) + " tracks");

        UserHandler userHandler = new UserHandler();
        userHandler.getInfoByID(playlist.getUserID()).addOnCompleteListener(new OnCompleteListener<UserInfo>() {
            @Override
            public void onComplete(@NonNull Task<UserInfo> task) {
                if (task.isSuccessful()) {
                    currentUserInfo = task.getResult();
                    if (currentUserInfo != null) {
                        fragmentPlaylistDetailBinding.textViewUserName.setText(currentUserInfo.getUsername());
                        // Load image using Glide library
                        // For User
                        String imagePath = "user_images/" + currentUserInfo.getProfilePhoto();
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Glide.with(context).load(uri).into(fragmentPlaylistDetailBinding.imageViewUser);
                        }).addOnFailureListener(exception -> {
                            // Handle any errors
                        });
                    } else {
                        fragmentPlaylistDetailBinding.textViewUserName.setText("Unknown");
                    }

                } else {
                    //and more action --.--
                }
            }
        });

        SongHandler handler = new SongHandler();
        handler.getInfoByID(playlist.getSongList().get(0)).addOnCompleteListener(new OnCompleteListener<Song>() {
            @Override
            public void onComplete(@NonNull Task<Song> task) {
                if (task.isSuccessful()) {
                    Song song = task.getResult();
                    // Load image using Glide library
                    String imagePath = "images/" + song.getImageFileName();
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Glide.with(context).load(uri).into(fragmentPlaylistDetailBinding.imageView);
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
                    });

                } else {
                    //and more action --.--
                    // Load image using Glide library
                    String imagePath = "images/lyos.png" ;
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Glide.with(context).load(uri).into(fragmentPlaylistDetailBinding.imageView);
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
                    });
                }
            }
        });
    }
    private void getDataFromFirestore() {
        SongHandler handler = new SongHandler();
        handler.getDataByListID(playlist.getSongList()).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
                if (task.isSuccessful()) {
                    arrayList = task.getResult();
                    adapter = new SongRecycleViewAdapter(context, arrayList);
                    fragmentPlaylistDetailBinding.recycleViewItems.setAdapter(adapter);
                    fragmentPlaylistDetailBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                    fragmentPlaylistDetailBinding.recycleViewItems.setLayoutManager(mLayoutManager);
                    fragmentPlaylistDetailBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());

                } else {
                    //and more action --.--
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
    private void addEvents(){
        fragmentPlaylistDetailBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}