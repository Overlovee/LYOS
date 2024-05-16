package com.example.lyos.CustomAdapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.MainActivity;
import com.example.lyos.Models.Playlist;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.R;
import com.example.lyos.databinding.OtherSongOptionsBottomSheetDialogLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PlaylistRecycleViewAdapter extends RecyclerView.Adapter<PlaylistRecycleViewAdapter.MyViewHolder>{
    private Context context;
    private ArrayList<Playlist> list;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewItem;
        public ImageView imageViewMoreOptionsAction;
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
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Playlist item = list.get(position);
        holder.textViewTitle.setText(item.getTitle());
        holder.textViewTracks.setText("Playlist: " + String.valueOf(item.getSongList().size()) + " tracks");
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

    private OtherSongOptionsBottomSheetDialogLayoutBinding dialogLayoutBinding;
    private void showDialog(Playlist item) {
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogLayoutBinding = OtherSongOptionsBottomSheetDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogLayoutBinding.getRoot());

        // Load image using Glide library
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
                        Glide.with(context).load(uri).into(dialogLayoutBinding.imageView);
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
                    });

                } else {
                    //and more action --.--
                    // Load image using Glide library
                    String imagePath = "images/lyos.png" ;
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Glide.with(context).load(uri).into(dialogLayoutBinding.imageView);
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
                    });
                }
            }
        });

        dialogLayoutBinding.textViewTitle.setText(item.getTitle());
        dialogLayoutBinding.textViewUserName.setText(currentUserInfo.getUsername());
//        LinearLayout layoutLike = dialog.findViewById(R.id.layoutLike);
//        LinearLayout layoutAddToNextUp = dialog.findViewById(R.id.layoutAddToNextUp);
//        LinearLayout layoutAddToPlaylist = dialog.findViewById(R.id.layoutAddToPlaylist);
//        LinearLayout layoutGoToUserProfile = dialog.findViewById(R.id.layoutGoToUserProfile);
//        LinearLayout layoutViewComment = dialog.findViewById(R.id.layoutViewComment);

        dialogLayoutBinding.layoutLike.setOnClickListener(new View.OnClickListener() {
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
