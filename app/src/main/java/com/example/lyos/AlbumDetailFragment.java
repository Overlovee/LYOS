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
import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.Models.Album;
import com.example.lyos.Models.ProfileDataLoader;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.databinding.FragmentAlbumDetailBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlbumDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlbumDetailFragment extends Fragment {
    private Album album;
    private FragmentAlbumDetailBinding fragmentAlbumDetailBinding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AlbumDetailFragment() {
        // Required empty public constructor
        this.album = null;
    }
    public AlbumDetailFragment(Album album) {
        // Required empty public constructor
        this.album = album;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AlbumDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlbumDetailFragment newInstance(String param1, String param2) {
        AlbumDetailFragment fragment = new AlbumDetailFragment();
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
        fragmentAlbumDetailBinding = FragmentAlbumDetailBinding.inflate(getLayoutInflater());
        return fragmentAlbumDetailBinding.getRoot();
    }

    private UserInfo user;
    private UserInfo currentUserInfo = new UserInfo();
    private ArrayList<Song> arrayList = new ArrayList<>();
    SongRecycleViewAdapter adapter;
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
            }

            @Override
            public void onProfileDataLoadFailed() {

            }
        });
        if(album != null){
            setUpUI();
            addEvents();
            if(album.getSongList() != null){
                if(album.getSongList().size() > 0){
                    getDataFromFirestore();
                }
            }
        }
        else {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
    private void setUpUI(){

        fragmentAlbumDetailBinding.textViewTitle.setText(album.getTitle());
        fragmentAlbumDetailBinding.textViewDescription.setText(album.getDescription());
        if(album.getSongList() == null){
            fragmentAlbumDetailBinding.textViewTracks.setText("Album: 0 tracks");
            fragmentAlbumDetailBinding.buttonPlay.setEnabled(false);
            fragmentAlbumDetailBinding.buttonPlayRandom.setEnabled(false);
        }
        else {
            fragmentAlbumDetailBinding.textViewTracks.setText("Album: " + String.valueOf(album.getSongList().size()) + " tracks");
            fragmentAlbumDetailBinding.buttonPlay.setEnabled(true);
            fragmentAlbumDetailBinding.buttonPlayRandom.setEnabled(true);
        }

        UserHandler userHandler = new UserHandler();
        userHandler.getInfoByID(album.getUserID()).addOnCompleteListener(new OnCompleteListener<UserInfo>() {
            @Override
            public void onComplete(@NonNull Task<UserInfo> task) {
                if (task.isSuccessful()) {
                    currentUserInfo = task.getResult();
                    if (currentUserInfo != null) {
                        if(currentUserInfo.getId().equals(user.getId())){
                            fragmentAlbumDetailBinding.textViewFollowAction.setVisibility(View.GONE);
                        }
                        else {
                            fragmentAlbumDetailBinding.textViewFollowAction.setVisibility(View.VISIBLE);
                            if(currentUserInfo.getFollowers() != null){
                                if(currentUserInfo.getFollowers().contains(user.getId())){
                                    fragmentAlbumDetailBinding.textViewFollowAction.setText("Following");
                                    fragmentAlbumDetailBinding.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customPrimaryColor));
                                    fragmentAlbumDetailBinding.textViewFollowAction.setBackgroundResource(R.drawable.rounded_clicked_button_view);
                                }
                                else {
                                    fragmentAlbumDetailBinding.textViewFollowAction.setText("Follow");
                                    fragmentAlbumDetailBinding.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customDarkColor));
                                    fragmentAlbumDetailBinding.textViewFollowAction.setBackgroundResource(R.drawable.rounded_button_view);
                                }
                            }
                            else {
                                fragmentAlbumDetailBinding.textViewFollowAction.setText("Follow");
                                fragmentAlbumDetailBinding.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customDarkColor));
                                fragmentAlbumDetailBinding.textViewFollowAction.setBackgroundResource(R.drawable.rounded_button_view);
                            }
                        }

                        fragmentAlbumDetailBinding.textViewUserName.setText(currentUserInfo.getUsername());
                        // Load image using Glide library
                        // For User
                        String imagePath = "user_images/" + currentUserInfo.getProfilePhoto();
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Glide.with(context).load(uri).into(fragmentAlbumDetailBinding.imageViewUser);
                        }).addOnFailureListener(exception -> {
                            // Handle any errors
                        });
                    } else {
                        fragmentAlbumDetailBinding.textViewUserName.setText("Unknown");
                    }

                } else {
                    //and more action --.--
                }
            }
        });

        // Load image using Glide library
        // For Album
        String imagePath = "album_images/" + album.getImageFileName();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context).load(uri).into(fragmentAlbumDetailBinding.imageViewAlbum);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });
    }
    private void getDataFromFirestore() {
        SongHandler handler = new SongHandler();
        handler.getDataByListID(album.getSongList()).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
                if (task.isSuccessful()) {
                    arrayList = task.getResult();
                    adapter = new SongRecycleViewAdapter(context, arrayList);
                    fragmentAlbumDetailBinding.recycleViewItems.setAdapter(adapter);
                    fragmentAlbumDetailBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                    fragmentAlbumDetailBinding.recycleViewItems.setLayoutManager(mLayoutManager);
                    fragmentAlbumDetailBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());

                } else {
                    //and more action --.--
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
    private void addEvents(){
        fragmentAlbumDetailBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        fragmentAlbumDetailBinding.buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.playNewPlaylist(arrayList);
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
        fragmentAlbumDetailBinding.buttonPlayRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.playPlaylistWithRandomSongs(arrayList);
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
        fragmentAlbumDetailBinding.textViewFollowAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserHandler userHandler = new UserHandler();
                if (fragmentAlbumDetailBinding.textViewFollowAction.getText().equals("Follow")) {
                    if(currentUserInfo.getFollowers() == null){
                        currentUserInfo.setFollowers(new ArrayList<>());
                    }
                    currentUserInfo.getFollowers().add(user.getId());
                    userHandler.update(currentUserInfo.getId(), currentUserInfo);
                    fragmentAlbumDetailBinding.textViewFollowAction.setText("Following");
                    fragmentAlbumDetailBinding.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customPrimaryColor));
                    fragmentAlbumDetailBinding.textViewFollowAction.setBackgroundResource(R.drawable.rounded_clicked_button_view);
                } else {
                    if(currentUserInfo.getFollowers() == null){
                        currentUserInfo.setFollowers(new ArrayList<>());
                    }
                    else {
                        if (currentUserInfo.getFollowers().size() > 0) {
                            if (currentUserInfo.getFollowers().contains(user.getId())) {
                                currentUserInfo.getFollowers().remove(user.getId());
                                userHandler.update(currentUserInfo.getId(), currentUserInfo);
                                fragmentAlbumDetailBinding.textViewFollowAction.setText("Follow");
                                fragmentAlbumDetailBinding.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customDarkColor));
                                fragmentAlbumDetailBinding.textViewFollowAction.setBackgroundResource(R.drawable.rounded_button_view);
                            }
                        }
                    }
                }
            }
        });
    }
}