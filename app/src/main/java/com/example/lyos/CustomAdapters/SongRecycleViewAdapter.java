package com.example.lyos.CustomAdapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lyos.FirebaseHandlers.PlaylistHandler;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.MainActivity;
import com.example.lyos.Models.Playlist;
import com.example.lyos.Models.ProfileDataLoader;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.R;
import com.example.lyos.databinding.AddingToPlaylistDialogLayoutBinding;
import com.example.lyos.databinding.ConfirmDialogLayoutBinding;
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

public class SongRecycleViewAdapter extends RecyclerView.Adapter<SongRecycleViewAdapter.MyViewHolder>{
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

    public SongRecycleViewAdapter(Context context, ArrayList<Song> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_song_recycleview_item, parent, false);
        return new MyViewHolder(itemView);
    }

    private UserInfo user;
    private UserInfo currentUserInfo = new UserInfo();
    FragmentActivity fragmentActivity;
    SongHandler songHandler;
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
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
    private void showDialog(MyViewHolder holder, Song item) {
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
        if(currentUserInfo.getId().equals(user.getId())){

            dialogLayoutBinding.layoutDelete.setVisibility(View.VISIBLE);
            dialogLayoutBinding.layoutUpdate.setVisibility(View.VISIBLE);

        }
        else {
            dialogLayoutBinding.layoutDelete.setVisibility(View.GONE);
            dialogLayoutBinding.layoutUpdate.setVisibility(View.GONE);
        }

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
        dialogLayoutBinding.layoutUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateDialog(holder, item, dialog);
            }
        });
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
    private void showDeleteDialog(MyViewHolder holder, Song item, Dialog parentDialog) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ConfirmDialogLayoutBinding dialogLayoutBinding = ConfirmDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogLayoutBinding.getRoot());

        SongHandler songHandler = new SongHandler();
        dialogLayoutBinding.layoutYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songHandler.deleteSong(item.getId())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Delete successfully!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                parentDialog.dismiss();
                                holder.itemView.setVisibility(View.GONE);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Can not delete!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
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
    private void showUpdateDialog(MyViewHolder holder, Song item, Dialog parentDialog) {
        LayoutInflater inflater = LayoutInflater.from(context);
        SongEditingDialogLayoutBinding dialogLayoutBinding = SongEditingDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogLayoutBinding.getRoot());

        SongHandler songHandler = new SongHandler();

        String imagePath = "images/" + item.getImageFileName();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context).load(uri).into(dialogLayoutBinding.imageView);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });

        dialogLayoutBinding.editTextTitle.setText(item.getTitle());
        dialogLayoutBinding.editTextTag.setText(item.getTag());
        dialogLayoutBinding.editTextDescription.setText(item.getDescription());
        dialogLayoutBinding.textViewUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = dialogLayoutBinding.editTextTitle.getText().toString();
                String tag = dialogLayoutBinding.editTextTag.getText().toString();
                String description = dialogLayoutBinding.editTextDescription.getText().toString();
                if(!title.isEmpty() || !tag.isEmpty() || !description.isEmpty()){
                    item.setTitle(title);
                    item.setTag(tag);
                    item.setDescription(description);
                    songHandler.updateSong(item.getId(), item)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "Update successfully!", Toast.LENGTH_SHORT).show();
                                    holder.textViewTitle.setText(item.getTitle());
                                    parentDialog.dismiss();
                                    dialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Can not update!", Toast.LENGTH_SHORT).show();
                                    parentDialog.dismiss();
                                    dialog.dismiss();
                                }
                            });
                }
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
