package com.example.lyos;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
import com.example.lyos.Models.ProfileDataLoader;
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
    private UserInfo user;
    private ArrayList<Song> arrayList = new ArrayList<>();
    SongRecycleViewAdapter adapter;
    FragmentActivity fragmentActivity;
    Context context;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();

        if (context instanceof FragmentActivity) {
            fragmentActivity = (FragmentActivity) context;
        }
        // Kiểm tra xem có dữ liệu tài khoản đã được lưu trữ hay không
        ProfileDataLoader.loadProfileData(context, new ProfileDataLoader.OnProfileDataLoadedListener() {
            @Override
            public void onProfileDataLoaded(UserInfo u) {
                user = u;
            }
            @Override
            public void onProfileDataLoadFailed() {

            }
        });

        if(playlist != null){
            setUpUI();
            addEvents();
            if(playlist.getSongList() != null){
                if(playlist.getSongList().size() > 0){
                    getDataFromFirestore();
                }
            }
        }
        else {
            fragmentActivity.getSupportFragmentManager().popBackStack();
        }
    }
    private void setUpUI(){
        fragmentPlaylistDetailBinding.textViewTitle.setText(playlist.getTitle());
        if(playlist.getSongList() == null){
            fragmentPlaylistDetailBinding.textViewTracks.setText("Playlist: 0 tracks");
            fragmentPlaylistDetailBinding.imageView.setImageResource(R.drawable.lyos);
            fragmentPlaylistDetailBinding.buttonPlay.setEnabled(false);
            fragmentPlaylistDetailBinding.buttonPlayRandom.setEnabled(false);
        }
        else {
            fragmentPlaylistDetailBinding.textViewTracks.setText("Playlist: " + String.valueOf(playlist.getSongList().size()) + " tracks");

            if(playlist.getSongList().size() > 0){
                fragmentPlaylistDetailBinding.buttonPlay.setEnabled(true);
                fragmentPlaylistDetailBinding.buttonPlayRandom.setEnabled(true);
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

                            fragmentPlaylistDetailBinding.imageView.setImageResource(R.drawable.lyos);
                        }
                    }
                });
            }
        }

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
                        if(currentUserInfo.getId().equals(user.getId())){
                            fragmentPlaylistDetailBinding.textViewFollowAction.setVisibility(View.GONE);
                        }
                        else {
                            fragmentPlaylistDetailBinding.textViewFollowAction.setVisibility(View.VISIBLE);
                            if(currentUserInfo.getFollowers() != null){
                                if(currentUserInfo.getFollowers().contains(user.getId())){
                                    fragmentPlaylistDetailBinding.textViewFollowAction.setText("Following");
                                    fragmentPlaylistDetailBinding.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customPrimaryColor));
                                    fragmentPlaylistDetailBinding.textViewFollowAction.setBackgroundResource(R.drawable.rounded_clicked_button_view);
                                }
                                else {
                                    fragmentPlaylistDetailBinding.textViewFollowAction.setText("Follow");
                                    fragmentPlaylistDetailBinding.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customDarkColor));
                                    fragmentPlaylistDetailBinding.textViewFollowAction.setBackgroundResource(R.drawable.rounded_button_view);
                                }
                            }
                            else {
                                fragmentPlaylistDetailBinding.textViewFollowAction.setText("Follow");
                                fragmentPlaylistDetailBinding.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customDarkColor));
                                fragmentPlaylistDetailBinding.textViewFollowAction.setBackgroundResource(R.drawable.rounded_button_view);
                            }
                        }
                    } else {
                        fragmentPlaylistDetailBinding.textViewUserName.setText("Unknown");
                    }

                } else {
                    //and more action --.--
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
                    fragmentActivity.getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
    private void addEvents(){
        fragmentPlaylistDetailBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentActivity.getSupportFragmentManager().popBackStack();
            }
        });
        fragmentPlaylistDetailBinding.textViewFollowAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserHandler userHandler = new UserHandler();
                if (fragmentPlaylistDetailBinding.textViewFollowAction.getText().equals("Follow")) {
                    if(currentUserInfo.getFollowers() == null){
                        currentUserInfo.setFollowers(new ArrayList<>());
                    }
                    currentUserInfo.getFollowers().add(user.getId());
                    userHandler.update(currentUserInfo.getId(), currentUserInfo);
                    fragmentPlaylistDetailBinding.textViewFollowAction.setText("Following");
                    fragmentPlaylistDetailBinding.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customPrimaryColor));
                    fragmentPlaylistDetailBinding.textViewFollowAction.setBackgroundResource(R.drawable.rounded_clicked_button_view);
                } else {
                    if(currentUserInfo.getFollowers() == null){
                        currentUserInfo.setFollowers(new ArrayList<>());
                    }
                    else {
                        if(currentUserInfo.getFollowers().size() > 0 ){
                            if(currentUserInfo.getFollowers().contains(user.getId())){
                                currentUserInfo.getFollowers().remove(user.getId());
                                userHandler.update(currentUserInfo.getId(), currentUserInfo);
                                fragmentPlaylistDetailBinding.textViewFollowAction.setText("Follow");
                                fragmentPlaylistDetailBinding.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customDarkColor));
                                fragmentPlaylistDetailBinding.textViewFollowAction.setBackgroundResource(R.drawable.rounded_button_view);
                            }
                        }
                    }
                }
            }
        });
        fragmentPlaylistDetailBinding.buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.playNewPlaylist(arrayList);
                    fragmentActivity.getSupportFragmentManager().popBackStack();
                }
            }
        });
        fragmentPlaylistDetailBinding.buttonPlayRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.playNewPlaylist(arrayList);
                    fragmentActivity.getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
}