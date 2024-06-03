package com.example.lyos;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lyos.CustomAdapters.PlaylistSelectionRecycleViewAdapter;
import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.PlaylistHandler;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.Models.Playlist;
import com.example.lyos.Models.ProfileDataLoader;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.databinding.AddingToPlaylistDialogLayoutBinding;
import com.example.lyos.databinding.ConfirmDialogLayoutBinding;
import com.example.lyos.databinding.FragmentPlaylistDetailBinding;
import com.example.lyos.databinding.OtherSongOptionsBottomSheetDialogLayoutBinding;
import com.example.lyos.databinding.PlaylistAddingDialogLayoutBinding;
import com.example.lyos.databinding.SongEditingDialogLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    SongInPlaylistRecycleViewAdapter adapter;
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
                    adapter = new SongInPlaylistRecycleViewAdapter(context, arrayList);
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
                    mainActivity.playPlaylistWithRandomSongs(arrayList);
                    fragmentActivity.getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
    public class SongInPlaylistRecycleViewAdapter extends RecyclerView.Adapter<SongInPlaylistRecycleViewAdapter.MyViewHolder>{
        private Context context;
        private ArrayList<Song> list;
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageViewItem;
            public ImageView imageViewMoreOptionsAction;
            public TextView textViewTitle;
            public TextView textViewUserName;
            public TextView textViewDuration;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                imageViewItem = itemView.findViewById(R.id.imageViewItem);
                imageViewMoreOptionsAction = itemView.findViewById(R.id.imageViewMoreOptionsAction);
                textViewTitle = itemView.findViewById(R.id.textViewTitle);
                textViewUserName = itemView.findViewById(R.id.textViewUserName);
                textViewDuration = itemView.findViewById(R.id.textViewDuration);
            }
        }

        public SongInPlaylistRecycleViewAdapter(Context context, ArrayList<Song> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public SongInPlaylistRecycleViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_song_recycleview_item, parent, false);
            return new SongInPlaylistRecycleViewAdapter.MyViewHolder(itemView);
        }

        private UserInfo user;
        private UserInfo currentUserInfo = new UserInfo();
        FragmentActivity fragmentActivity;
        SongHandler songHandler;
        @Override
        public void onBindViewHolder(SongInPlaylistRecycleViewAdapter.MyViewHolder holder, int position) {
            Song item = list.get(position);
            holder.textViewTitle.setText(item.getTitle());
            if (context instanceof FragmentActivity) {
                fragmentActivity = (FragmentActivity) context;
            }
            songHandler = new SongHandler();
            // Kiểm tra xem có dữ liệu tài khoản đã được lưu trữ hay không
            ProfileDataLoader.loadProfileData(context, new ProfileDataLoader.OnProfileDataLoadedListener() {
                @Override
                public void onProfileDataLoaded(UserInfo u) {
                    user = u;
                    if (user != null){
                    } else {
                        fragmentActivity.getSupportFragmentManager().popBackStack();
                    }
                }
                @Override
                public void onProfileDataLoadFailed() {
                    fragmentActivity.getSupportFragmentManager().popBackStack();
                }
            });

            UserHandler userHandler = new UserHandler();
            userHandler.getInfoByID(item.getUserID()).addOnCompleteListener(new OnCompleteListener<UserInfo>() {
                @Override
                public void onComplete(@NonNull Task<UserInfo> task) {
                    if (task.isSuccessful()) {
                        currentUserInfo = task.getResult();
                        if (currentUserInfo != null) {
                            holder.textViewUserName.setText(currentUserInfo.getUsername());
                        } else {
                            holder.textViewUserName.setText("Unknown");
                        }

                    } else {
                        //and more action --.--
                        holder.textViewUserName.setText("Unknown");
                    }
                }
            });

            int duration = item.getDuration();
            int sec = duration%60;
            int min = (duration - sec)/60;
            String seccond = String.valueOf(sec);
            if(sec < 10){
                seccond = "0" + seccond;
            }
            holder.textViewDuration.setText(String.valueOf(min) + ":" + seccond);

            // Load image using Glide library
            String imagePath = "images/" + item.getImageFileName();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(context).load(uri).into(holder.imageViewItem);
            }).addOnFailureListener(exception -> {
                // Handle any errors
            });
            holder.imageViewMoreOptionsAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentUserInfo != null) {
                        showDialog(holder, item);
                    } else {
                        // Xử lý khi currentUserInfo là null (ví dụ: hiển thị thông báo lỗi)
                        Toast.makeText(context, "Current user information is not available", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Kiểm tra xem context có phải là instance của MainActivity hay không
                    if (context instanceof MainActivity) {
                        MainActivity mainActivity = (MainActivity) context;
                        mainActivity.setAudioNowPlaying(item);
                    }
                }
            });
        }
        private OtherSongOptionsBottomSheetDialogLayoutBinding dialogLayoutBinding;
        private void showDialog(SongInPlaylistRecycleViewAdapter.MyViewHolder holder, Song item) {
            LayoutInflater inflater = LayoutInflater.from(context);
            dialogLayoutBinding = OtherSongOptionsBottomSheetDialogLayoutBinding.inflate(inflater);

            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(dialogLayoutBinding.getRoot());

            String imagePath = "images/" + item.getImageFileName();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(context).load(uri).into(dialogLayoutBinding.imageView);
            }).addOnFailureListener(exception -> {
                // Handle any errors
            });
            dialogLayoutBinding.textViewTitle.setText(item.getTitle());
            dialogLayoutBinding.textViewUserName.setText(currentUserInfo.getUsername());

            if(item.getLikedBy() != null){
                if(item.getLikedBy().contains(user.getId())){
                    dialogLayoutBinding.imageViewActionLike.setImageResource(R.drawable.hearted);
                }
                else {
                    dialogLayoutBinding.imageViewActionLike.setImageResource(R.drawable.heart);
                }
            } else {
                dialogLayoutBinding.imageViewActionLike.setImageResource(R.drawable.heart);
            }

            dialogLayoutBinding.layoutLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Kiểm tra xem người dùng đã thích bài hát hay chưa
                    if(item.getLikedBy() != null){
                        if (item.getLikedBy().contains(user.getId())) {
                            // Nếu đã thích, hủy thích bằng cách loại bỏ ID của người dùng khỏi danh sách thích
                            item.getLikedBy().remove(user.getId());
                            dialogLayoutBinding.imageViewActionLike.setImageResource(R.drawable.heart);
                        } else {
                            // Nếu chưa thích, thêm ID của người dùng vào danh sách thích
                            item.getLikedBy().add(user.getId());
                            dialogLayoutBinding.imageViewActionLike.setImageResource(R.drawable.hearted);
                        }
                    } else {
                        item.setLikedBy(new ArrayList<>());
                        item.getLikedBy().add(user.getId());
                        dialogLayoutBinding.imageViewActionLike.setImageResource(R.drawable.hearted);
                    }

                    // Sau khi cập nhật trạng thái thích của bài hát,
                    // gọi hàm updateSong để lưu thay đổi vào cơ sở dữ liệu
                    // Ví dụ:
                    songHandler.updateSong(item.getId(), item)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                }
            });

            dialogLayoutBinding.layoutAddToNextUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Kiểm tra xem context có phải là instance của MainActivity hay không
                    if (context instanceof MainActivity) {
                        MainActivity mainActivity = (MainActivity) context;
                        mainActivity.addToNextUp(item);
                    }
                }
            });
            dialogLayoutBinding.layoutAddToPlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddingToPlaylistDialog(item);
                }
            });
            dialogLayoutBinding.layoutGoToUserProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Kiểm tra xem context có phải là instance của MainActivity hay không
                    if (context instanceof MainActivity) {
                        MainActivity mainActivity = (MainActivity) context;
                        if(currentUserInfo.getId().equals(user.getId())){
                            mainActivity.openProfileFragment();
                            dialog.dismiss();
                        }
                        else {
                            mainActivity.openProfileDetailFragment(currentUserInfo);
                        }
                    }
                }
            });
            dialogLayoutBinding.layoutUpdate.setVisibility(View.GONE);
            dialogLayoutBinding.layoutDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteDialog(holder, item, dialog);
                }
            });
            dialog.show();
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.BOTTOM);

        }
        private void showDeleteDialog(SongInPlaylistRecycleViewAdapter.MyViewHolder holder, Song item, Dialog parentDialog) {
            LayoutInflater inflater = LayoutInflater.from(context);
            ConfirmDialogLayoutBinding dialogLayoutBinding = ConfirmDialogLayoutBinding.inflate(inflater);

            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(dialogLayoutBinding.getRoot());

            PlaylistHandler playlistHandler = new PlaylistHandler();

            dialogLayoutBinding.layoutYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(playlist.getSongList() != null){
                        playlist.getSongList().remove(item.getId());
                        playlistHandler.update(playlist.getId(), playlist);
                        dialog.dismiss();
                        parentDialog.dismiss();
                        holder.itemView.setVisibility(View.GONE);
                    }
                }
            });

            dialogLayoutBinding.layoutNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                }
            });
            dialogLayoutBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialogLayoutBinding.buttonClose.setOnClickListener(new View.OnClickListener() {
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
        private void showAddingToPlaylistDialog(Song song){
            LayoutInflater inflater = LayoutInflater.from(context);
            AddingToPlaylistDialogLayoutBinding addingToPlaylistDialogLayoutBinding = AddingToPlaylistDialogLayoutBinding.inflate(inflater);

            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(addingToPlaylistDialogLayoutBinding.getRoot());

            updateUIDialogLayout(addingToPlaylistDialogLayoutBinding, song);
            addingToPlaylistDialogLayoutBinding.buttonCreateNewPlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPlaylistAddingDialog(addingToPlaylistDialogLayoutBinding, song);
                    updateUIDialogLayout(addingToPlaylistDialogLayoutBinding, song);
                }
            });

            addingToPlaylistDialogLayoutBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        }
        private void updateUIDialogLayout(AddingToPlaylistDialogLayoutBinding dialogLayoutBinding, Song song){
            PlaylistHandler handler = new PlaylistHandler();
            handler.searchByUserID(user.getId()).addOnCompleteListener(new OnCompleteListener<ArrayList<Playlist>>() {
                @Override
                public void onComplete(@NonNull Task<ArrayList<Playlist>> task) {
                    if (task.isSuccessful()) {
                        ArrayList<Playlist> playlistArrayList = task.getResult();
                        PlaylistSelectionRecycleViewAdapter playlistAdapter = new PlaylistSelectionRecycleViewAdapter(context, playlistArrayList, song);

                        dialogLayoutBinding.textViewNoPlaylist.setVisibility(View.GONE);
                        dialogLayoutBinding.recycleViewItems.setVisibility(View.VISIBLE);

                        dialogLayoutBinding.recycleViewItems.setAdapter(playlistAdapter);
                        dialogLayoutBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                        GridLayoutManager mLayoutManager = new GridLayoutManager(context, 2);
                        dialogLayoutBinding.recycleViewItems.setLayoutManager(mLayoutManager);
                        dialogLayoutBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());

                    } else {
                        //and more action --.--
                        dialogLayoutBinding.textViewNoPlaylist.setVisibility(View.GONE);
                        dialogLayoutBinding.recycleViewItems.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        private void showPlaylistAddingDialog(AddingToPlaylistDialogLayoutBinding addingToPlaylistDialogLayoutBinding, Song song) {
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
                        playlistHandler.add(newPlaylist);
                        updateUIDialogLayout(addingToPlaylistDialogLayoutBinding, song);
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
        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}