package com.example.lyos.CustomAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PlaylistSelectionRecycleViewAdapter extends RecyclerView.Adapter<PlaylistSelectionRecycleViewAdapter.MyViewHolder>{
    private Context context;
    private ArrayList<Playlist> list;
    private Song song;
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

    public PlaylistSelectionRecycleViewAdapter(Context context, ArrayList<Playlist> list, Song song) {
        this.context = context;
        this.list = list;
        this.song = song;
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
                    updateUI(holder, item);
                } else {
                    fragmentActivity.getSupportFragmentManager().popBackStack();
                }
            }
            @Override
            public void onProfileDataLoadFailed() {
                fragmentActivity.getSupportFragmentManager().popBackStack();
            }
        });
        updateUI(holder, item);
    }
    private void updateUI(PlaylistSelectionRecycleViewAdapter.MyViewHolder holder, Playlist item){
        holder.itemView.setEnabled(true);
        holder.imageViewItem.setAlpha(1.0f);
        holder.textViewTitle.setText(item.getTitle());
        if(item.getSongList() == null){
            holder.textViewTracks.setText("Playlist: 0 tracks");
        } else {
            if(item.getSongList().contains(song.getId())){
                holder.itemView.setEnabled(false);
                holder.imageViewItem.setAlpha(0.6f);
            }
            holder.textViewTracks.setText("Playlist: " + String.valueOf(item.getSongList().size()) + " tracks");
            SongHandler handler = new SongHandler();
            if(item.getSongList().size() > 0){
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
            else {
                String imagePath = "images/lyos.png" ;
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(context).load(uri).into(holder.imageViewItem);
                }).addOnFailureListener(exception -> {
                    // Handle any errors
                });
            }
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

        addEvents(holder, item);
    }
    private void addEvents(PlaylistSelectionRecycleViewAdapter.MyViewHolder holder, Playlist item){
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(item.getSongList() == null){
                    item.setSongList(new ArrayList<>());
                }
                item.getSongList().add(song.getId());

                PlaylistHandler playlistHandler = new PlaylistHandler();
                playlistHandler.update(item.getId(), item);

                updateUI(holder, item);
            }
        });
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
}
