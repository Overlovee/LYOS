package com.example.lyos;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.lyos.CustomAdapters.AlbumRecycleViewAdapter;
import com.example.lyos.CustomAdapters.PlaylistRecycleViewAdapter;
import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.AlbumHandler;
import com.example.lyos.FirebaseHandlers.PlaylistHandler;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.Models.AccountUtils;
import com.example.lyos.Models.Album;
import com.example.lyos.Models.Playlist;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding fragmentProfileBinding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        fragmentProfileBinding = FragmentProfileBinding.inflate(getLayoutInflater());
        return fragmentProfileBinding.getRoot();
    }

    private ArrayList<Song> likesArrayList = new ArrayList<>();
    private ArrayList<Song> tracksArrayList = new ArrayList<>();
    private ArrayList<Album> albumArrayList = new ArrayList<>();
    private ArrayList<Playlist> playlistArrayList = new ArrayList<>();
    SongRecycleViewAdapter likeAdapter;
    SongRecycleViewAdapter trackAdapter;
    AlbumRecycleViewAdapter albumAdapter;
    PlaylistRecycleViewAdapter playlistAdapter;
    private UserInfo user;
    Context context;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Kiểm tra xem có dữ liệu tài khoản đã được lưu trữ hay không
        context = getContext();
        String savedAccount = AccountUtils.getSavedAccount(requireContext());
        if (savedAccount != null) {
            UserHandler userHandler = new UserHandler();
            userHandler.getInfoByID(savedAccount).addOnCompleteListener(new OnCompleteListener<UserInfo>() {
                @Override
                public void onComplete(@NonNull Task<UserInfo> task) {
                    if (task.isSuccessful()) {
                        user = task.getResult();
                        if (user != null) {
                            setUpUI();
                            addEvents();
                            if(user.getLikes().size() > 0){
                                fragmentProfileBinding.textViewZeroLikes.setVisibility(View.GONE);
                                fragmentProfileBinding.recycleViewLikeItems.setVisibility(View.VISIBLE);
                                fragmentProfileBinding.textViewSeeAllLikesAction.setVisibility(View.VISIBLE);
                                getLikesDataFromFirestore();
                            } else {
                                fragmentProfileBinding.textViewZeroLikes.setVisibility(View.VISIBLE);
                                fragmentProfileBinding.recycleViewLikeItems.setVisibility(View.GONE);
                                fragmentProfileBinding.textViewSeeAllLikesAction.setVisibility(View.GONE);

                            }
                            getUserTracksDataFromFirestore();
                            getAlbumsDataFromFirestore();
                            getPlaylistsDataFromFirestore();
                        } else {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }

                    } else {
                        //and more action --.--
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                }
            });
        }
    }
    private void setUpUI(){
        fragmentProfileBinding.textViewUserName.setText(user.getUsername());
        fragmentProfileBinding.textViewHeaderUserName.setText(user.getUsername());
        fragmentProfileBinding.textViewFollowers.setText(String.valueOf(user.getFollowers().size()) + " Followers");
        fragmentProfileBinding.textViewFollowing.setText(String.valueOf(user.getFollowing().size()) + " Following");

        String imagePath = "user_images/" + user.getProfilePhoto();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context).load(uri).into(fragmentProfileBinding.imageViewProfile);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });

        imagePath = "user_banner_images/" + user.getProfileBanner();
        storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context).load(uri).into(fragmentProfileBinding.imageViewBanner);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });
    }
    private void getLikesDataFromFirestore() {
        SongHandler handler = new SongHandler();
        handler.getDataByListID(user.getLikes(), 6).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
                if (task.isSuccessful()) {
                    likesArrayList = task.getResult();
                    likeAdapter = new SongRecycleViewAdapter(context, likesArrayList);

                    fragmentProfileBinding.textViewZeroLikes.setVisibility(View.GONE);
                    fragmentProfileBinding.recycleViewLikeItems.setVisibility(View.VISIBLE);
                    fragmentProfileBinding.textViewSeeAllLikesAction.setVisibility(View.VISIBLE);

                    fragmentProfileBinding.recycleViewLikeItems.setAdapter(likeAdapter);
                    fragmentProfileBinding.recycleViewLikeItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                    fragmentProfileBinding.recycleViewLikeItems.setLayoutManager(mLayoutManager);
                    fragmentProfileBinding.recycleViewLikeItems.setItemAnimator(new DefaultItemAnimator());

                } else {
                    //and more action --.--
                    fragmentProfileBinding.textViewZeroLikes.setVisibility(View.VISIBLE);
                    fragmentProfileBinding.recycleViewLikeItems.setVisibility(View.GONE);
                    fragmentProfileBinding.textViewSeeAllLikesAction.setVisibility(View.GONE);
                }
            }
        });
    }
    private void getAlbumsDataFromFirestore() {
        AlbumHandler handler = new AlbumHandler();
        handler.searchByUserID(user.getId(), 6).addOnCompleteListener(new OnCompleteListener<ArrayList<Album>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Album>> task) {
                if (task.isSuccessful()) {
                    albumArrayList = task.getResult();
                    albumAdapter = new AlbumRecycleViewAdapter(context, albumArrayList);
                    fragmentProfileBinding.layoutAlbums.setVisibility(View.VISIBLE);
                    fragmentProfileBinding.recycleViewAlbumItems.setAdapter(albumAdapter);
                    fragmentProfileBinding.recycleViewAlbumItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                    fragmentProfileBinding.recycleViewAlbumItems.setLayoutManager(mLayoutManager);
                    fragmentProfileBinding.recycleViewAlbumItems.setItemAnimator(new DefaultItemAnimator());

                } else {
                    //and more action --.--
                    fragmentProfileBinding.layoutAlbums.setVisibility(View.GONE);
                }
            }
        });
    }
    private void getPlaylistsDataFromFirestore() {
        PlaylistHandler handler = new PlaylistHandler();
        handler.searchByUserID(user.getId(), 6).addOnCompleteListener(new OnCompleteListener<ArrayList<Playlist>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Playlist>> task) {
                if (task.isSuccessful()) {
                    playlistArrayList = task.getResult();
                    playlistAdapter = new PlaylistRecycleViewAdapter(context, playlistArrayList);

                    fragmentProfileBinding.textViewZeroPlaylists.setVisibility(View.GONE);
                    fragmentProfileBinding.recycleViewPlaylistItems.setVisibility(View.VISIBLE);

                    fragmentProfileBinding.recycleViewPlaylistItems.setAdapter(playlistAdapter);
                    fragmentProfileBinding.recycleViewPlaylistItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                    fragmentProfileBinding.recycleViewPlaylistItems.setLayoutManager(mLayoutManager);
                    fragmentProfileBinding.recycleViewPlaylistItems.setItemAnimator(new DefaultItemAnimator());

                } else {
                    //and more action --.--
                    fragmentProfileBinding.textViewZeroPlaylists.setVisibility(View.VISIBLE);
                    fragmentProfileBinding.recycleViewPlaylistItems.setVisibility(View.GONE);
                }
            }
        });
    }
    private void getUserTracksDataFromFirestore() {
        SongHandler handler = new SongHandler();
        handler.searchByUserID(user.getId(), 6).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
                if (task.isSuccessful()) {
                    tracksArrayList = task.getResult();
                    if(!tracksArrayList.isEmpty()){
                        fragmentProfileBinding.layoutTracks.setVisibility(View.VISIBLE);
                        trackAdapter = new SongRecycleViewAdapter(context, tracksArrayList);
                        fragmentProfileBinding.recycleViewTrackItems.setAdapter(trackAdapter);
                        fragmentProfileBinding.recycleViewTrackItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                        RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                        fragmentProfileBinding.recycleViewTrackItems.setLayoutManager(mLayoutManager);
                        fragmentProfileBinding.recycleViewTrackItems.setItemAnimator(new DefaultItemAnimator());
                    } else {
                        fragmentProfileBinding.layoutTracks.setVisibility(View.GONE);
                    }
                } else {
                    //and more action --.--
                    fragmentProfileBinding.layoutTracks.setVisibility(View.GONE);
                }
            }
        });
    }
    private void addEvents(){
        fragmentProfileBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        fragmentProfileBinding.buttonMoreAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        fragmentProfileBinding.textViewSeeAllTracksAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.openSeeAllFragment(user.getId());
                }
            }
        });
        fragmentProfileBinding.textViewSeeAllLikesAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.openSeeAllFragment(user.getLikes());
                }
            }
        });
    }
}