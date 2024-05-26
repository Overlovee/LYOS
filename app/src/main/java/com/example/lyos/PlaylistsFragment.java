package com.example.lyos;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.bumptech.glide.Glide;
import com.example.lyos.CustomAdapters.PlaylistRecycleViewAdapter;
import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.PlaylistHandler;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.Models.Playlist;
import com.example.lyos.Models.ProfileDataLoader;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.databinding.FragmentPlaylistsBinding;
import com.example.lyos.databinding.OtherSongOptionsBottomSheetDialogLayoutBinding;
import com.example.lyos.databinding.PlaylistAddingDialogLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlaylistsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaylistsFragment extends Fragment {
    private FragmentPlaylistsBinding fragmentPlaylistsBinding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PlaylistsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlaylistsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlaylistsFragment newInstance(String param1, String param2) {
        PlaylistsFragment fragment = new PlaylistsFragment();
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
        fragmentPlaylistsBinding = FragmentPlaylistsBinding.inflate(getLayoutInflater());
        return fragmentPlaylistsBinding.getRoot();
    }
    private ArrayList<Song> tracksArrayList = new ArrayList<>();
    private ArrayList<Playlist> playlistArrayList = new ArrayList<>();
    SongRecycleViewAdapter trackAdapter;
    PlaylistRecycleViewAdapter playlistAdapter;
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
                    getPlaylistsDataFromFirestore();

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
    private void getPlaylistsDataFromFirestore() {
        PlaylistHandler handler = new PlaylistHandler();
        handler.searchByUserID(user.getId()).addOnCompleteListener(new OnCompleteListener<ArrayList<Playlist>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Playlist>> task) {
                if (task.isSuccessful()) {
                    playlistArrayList = task.getResult();
                    playlistAdapter = new PlaylistRecycleViewAdapter(context, playlistArrayList);

                    fragmentPlaylistsBinding.textViewZero.setVisibility(View.GONE);
                    fragmentPlaylistsBinding.recycleViewItems.setVisibility(View.VISIBLE);

                    fragmentPlaylistsBinding.recycleViewItems.setAdapter(playlistAdapter);
                    fragmentPlaylistsBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    GridLayoutManager mLayoutManager = new GridLayoutManager(context, 2);
                    fragmentPlaylistsBinding.recycleViewItems.setLayoutManager(mLayoutManager);
                    fragmentPlaylistsBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());

                } else {
                    //and more action --.--
                    fragmentPlaylistsBinding.textViewZero.setVisibility(View.VISIBLE);
                    fragmentPlaylistsBinding.recycleViewItems.setVisibility(View.GONE);
                }
            }
        });
    }
    private void addEvents(){
        fragmentPlaylistsBinding.layoutCreateNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlaylistAddingDialog();
            }
        });
    }
    private void showPlaylistAddingDialog() {
        PlaylistAddingDialogLayoutBinding dialogLayoutBinding;
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogLayoutBinding = PlaylistAddingDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogLayoutBinding.getRoot());

        dialogLayoutBinding.textViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = dialogLayoutBinding.editTextTitle.getText().toString();
                if(!title.isEmpty()){
                    Playlist newPlaylist = new Playlist();
                    newPlaylist.setTitle(title);
                    newPlaylist.setUserID(user.getId());
                    PlaylistHandler playlistHandler = new PlaylistHandler();
                    playlistHandler.add(newPlaylist).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                getPlaylistsDataFromFirestore();
                            } else {

                            }
                        }
                    });
                }
                dialog.dismiss();
            }
        });
        dialogLayoutBinding.textViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
}