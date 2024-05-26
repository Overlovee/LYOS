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
import com.example.lyos.databinding.ConfirmDialogLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PlaylistRecycleViewAdapter extends RecyclerView.Adapter<PlaylistRecycleViewAdapter.MyViewHolder>{
    private Context context;
    private ArrayList<Playlist> list;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewItem;
        public TextView textViewTitle;
        public TextView textViewUserName;
        public TextView textViewTracks;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewTracks = itemView.findViewById(R.id.textViewTracks);
        }
    }

    public PlaylistRecycleViewAdapter(Context context, ArrayList<Playlist> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_playlist_recycle_view, parent, false);
        return new MyViewHolder(itemView);
    }

    private UserInfo currentUserInfo = new UserInfo();
    private UserInfo user;
    FragmentActivity fragmentActivity;
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Playlist item = list.get(position);
        if (context instanceof FragmentActivity) {
            fragmentActivity = (FragmentActivity) context;
        }
        // Kiểm tra xem có dữ liệu tài khoản đã được lưu trữ hay không
        ProfileDataLoader.loadProfileData(context, new ProfileDataLoader.OnProfileDataLoadedListener() {
            @Override
            public void onProfileDataLoaded(UserInfo u) {
                user = u;
                if (user != null){
                    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            showDeleteDialog(holder, item);
                            return false;
                        }
                    });
                } else {
                    fragmentActivity.getSupportFragmentManager().popBackStack();
                }
            }
            @Override
            public void onProfileDataLoadFailed() {
                fragmentActivity.getSupportFragmentManager().popBackStack();
            }
        });

        holder.textViewTitle.setText(item.getTitle());
        if(item.getSongList() == null){
            holder.textViewTracks.setText("Playlist: 0 tracks");
        } else {
            holder.textViewTracks.setText("Playlist: " + String.valueOf(item.getSongList().size()) + " tracks");
            SongHandler handler = new SongHandler();
            handler.getInfoByID(item.getSongList().get(0)).addOnCompleteListener(new OnCompleteListener<Song>() {
                @Override
                public void onComplete(@NonNull Task<Song> task) {
                    if (task.isSuccessful()) {
                        Song song = task.getResult();
                        // Load image using Glide library
                        String imagePath = "images/" + song.getImageFileName();
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Glide.with(context).load(uri).into(holder.imageViewItem);
                        }).addOnFailureListener(exception -> {
                            // Handle any errors
                            holder.imageViewItem.setImageResource(R.drawable.lyos);
                        });

                    } else {
                        //and more action --.--
                        // Load image using Glide library
                        String imagePath = "images/lyos.png" ;
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Glide.with(context).load(uri).into(holder.imageViewItem);
                        }).addOnFailureListener(exception -> {
                            // Handle any errors
                        });
                    }
                }
            });
        }

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
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.openPlaylistDetailFragment(item);
                }
            }
        });
    }
    private void showDeleteDialog(PlaylistRecycleViewAdapter.MyViewHolder holder, Playlist item) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ConfirmDialogLayoutBinding dialogLayoutBinding = ConfirmDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogLayoutBinding.getRoot());

        PlaylistHandler playlistHandler = new PlaylistHandler();
        dialogLayoutBinding.layoutYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlistHandler.delete(item.getId())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Delete successfully!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
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
    @Override
    public int getItemCount() {
        return list.size();
    }
}
