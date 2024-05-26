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
import com.example.lyos.Models.ProfileDataLoader;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.databinding.FragmentProfileDetailBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileDetailFragment extends Fragment {
    private UserInfo currentUserInfo;
    private FragmentProfileDetailBinding fragmentProfileDetailBinding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileDetailFragment() {
        // Required empty public constructor
        currentUserInfo = null;
    }
    public ProfileDetailFragment(UserInfo user) {
        // Required empty public constructor
        this.currentUserInfo = user;
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileDetailFragment newInstance(String param1, String param2) {
        ProfileDetailFragment fragment = new ProfileDetailFragment();
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
        fragmentProfileDetailBinding = FragmentProfileDetailBinding.inflate(getLayoutInflater());
        return fragmentProfileDetailBinding.getRoot();
    }

    private ArrayList<Song> trackArrayList = new ArrayList<>();
    private ArrayList<Song> likeArrayList = new ArrayList<>();
    private UserInfo user;
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
        if(currentUserInfo != null){
            setUpUI();
            addEvents();
            if(currentUserInfo.getLikes().size() > 0){
                fragmentProfileDetailBinding.textViewZeroLikes.setVisibility(View.GONE);
                fragmentProfileDetailBinding.recycleViewLikeItems.setVisibility(View.VISIBLE);
                fragmentProfileDetailBinding.textViewSeeAllLikesAction.setVisibility(View.VISIBLE);
                getLikesDataFromFirestore();
            } else {
                fragmentProfileDetailBinding.textViewZeroLikes.setVisibility(View.VISIBLE);
                fragmentProfileDetailBinding.recycleViewLikeItems.setVisibility(View.GONE);
                fragmentProfileDetailBinding.textViewSeeAllLikesAction.setVisibility(View.GONE);

            }
            getUserTracksDataFromFirestore();
        } else{
            // Finish the activity if user is null
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
    private void setUpUI(){
        fragmentProfileDetailBinding.textViewUserName.setText(currentUserInfo.getUsername());
        fragmentProfileDetailBinding.textViewHeaderUserName.setText(currentUserInfo.getUsername());
        if(currentUserInfo.getFollowers() == null){
            fragmentProfileDetailBinding.textViewFollowers.setText("0 Followers");
        }
        else {
            fragmentProfileDetailBinding.textViewFollowers.setText(String.valueOf(currentUserInfo.getFollowers().size()) + " Followers");
        }
        if(currentUserInfo.getFollowing() == null){
            fragmentProfileDetailBinding.textViewFollowing.setText("0 Following");
        }
        else {
            fragmentProfileDetailBinding.textViewFollowing.setText(String.valueOf(currentUserInfo.getFollowing().size()) + " Following");
        }


        String imagePath = "user_images/" + currentUserInfo.getProfilePhoto();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context).load(uri).into(fragmentProfileDetailBinding.imageViewProfile);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });

        imagePath = "user_banner_images/" + currentUserInfo.getProfileBanner();
        storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context).load(uri).into(fragmentProfileDetailBinding.imageViewBanner);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });
        fragmentProfileDetailBinding.textViewFollowAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserHandler userHandler = new UserHandler();
                if (fragmentProfileDetailBinding.textViewFollowAction.getText().equals("Follow")) {
                    if(currentUserInfo.getFollowers() == null){
                        currentUserInfo.setFollowers(new ArrayList<>());
                    }
                    currentUserInfo.getFollowers().add(user.getId());
                    userHandler.update(currentUserInfo.getId(), currentUserInfo);
                    fragmentProfileDetailBinding.textViewFollowAction.setText("Following");
                    fragmentProfileDetailBinding.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customPrimaryColor));
                    fragmentProfileDetailBinding.textViewFollowAction.setBackgroundResource(R.drawable.rounded_clicked_button_view);
                } else {
                    if(currentUserInfo.getFollowers() == null){
                        currentUserInfo.setFollowers(new ArrayList<>());
                    }
                    currentUserInfo.getFollowers().remove(user.getId());
                    userHandler.update(currentUserInfo.getId(), currentUserInfo);
                    fragmentProfileDetailBinding.textViewFollowAction.setText("Follow");
                    fragmentProfileDetailBinding.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customDarkColor));
                    fragmentProfileDetailBinding.textViewFollowAction.setBackgroundResource(R.drawable.rounded_button_view);
                }
            }
        });
    }
    private void getLikesDataFromFirestore() {
        SongHandler handler = new SongHandler();
        handler.getDataByListID(currentUserInfo.getLikes(), 6).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
                if (task.isSuccessful()) {
                    trackArrayList = task.getResult();
                    adapter = new SongRecycleViewAdapter(context, trackArrayList);
                    fragmentProfileDetailBinding.recycleViewLikeItems.setAdapter(adapter);
                    fragmentProfileDetailBinding.recycleViewLikeItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                    fragmentProfileDetailBinding.recycleViewLikeItems.setLayoutManager(mLayoutManager);
                    fragmentProfileDetailBinding.recycleViewLikeItems.setItemAnimator(new DefaultItemAnimator());

                } else {
                    //and more action --.--
                    fragmentProfileDetailBinding.textViewZeroLikes.setVisibility(View.VISIBLE);
                    fragmentProfileDetailBinding.recycleViewLikeItems.setVisibility(View.GONE);
                    fragmentProfileDetailBinding.textViewSeeAllLikesAction.setVisibility(View.GONE);
                }
            }
        });
    }
    private void getUserTracksDataFromFirestore() {
        SongHandler handler = new SongHandler();
        handler.searchByUserID(currentUserInfo.getId(), 6).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
                if (task.isSuccessful()) {
                    likeArrayList = task.getResult();
                    if(!likeArrayList.isEmpty()){
                        fragmentProfileDetailBinding.textViewZeroTracks.setVisibility(View.GONE);
                        fragmentProfileDetailBinding.recycleViewTrackItems.setVisibility(View.VISIBLE);
                        fragmentProfileDetailBinding.textViewSeeAllTracksAction.setVisibility(View.VISIBLE);
                        adapter = new SongRecycleViewAdapter(context, likeArrayList);
                        fragmentProfileDetailBinding.recycleViewTrackItems.setAdapter(adapter);
                        fragmentProfileDetailBinding.recycleViewTrackItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                        RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                        fragmentProfileDetailBinding.recycleViewTrackItems.setLayoutManager(mLayoutManager);
                        fragmentProfileDetailBinding.recycleViewTrackItems.setItemAnimator(new DefaultItemAnimator());
                    } else {
                        fragmentProfileDetailBinding.textViewZeroTracks.setVisibility(View.VISIBLE);
                        fragmentProfileDetailBinding.recycleViewTrackItems.setVisibility(View.GONE);
                        fragmentProfileDetailBinding.textViewSeeAllTracksAction.setVisibility(View.GONE);
                    }
                } else {
                    //and more action --.--
                    fragmentProfileDetailBinding.textViewZeroTracks.setVisibility(View.VISIBLE);
                    fragmentProfileDetailBinding.recycleViewTrackItems.setVisibility(View.GONE);
                    fragmentProfileDetailBinding.textViewSeeAllTracksAction.setVisibility(View.GONE);
                }
            }
        });
    }
    private void addEvents(){
        fragmentProfileDetailBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        fragmentProfileDetailBinding.buttonMoreAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        fragmentProfileDetailBinding.textViewSeeAllTracksAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.openSeeAllFragment(currentUserInfo.getId());
                }
            }
        });
        fragmentProfileDetailBinding.textViewSeeAllLikesAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.openSeeAllFragment(currentUserInfo.getLikes());
                }
            }
        });
    }
}