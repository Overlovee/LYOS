package com.example.lyos.CustomAdapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lyos.FirebaseHandlers.PlaylistHandler;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.Interfaces.OnSelectionChangedListener;
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
import java.util.Map;

public class TracksSelectionRecycleViewAdapter extends RecyclerView.Adapter<TracksSelectionRecycleViewAdapter.MyViewHolder>{
    private Context context;
    private ArrayList<Pair<Song, Boolean>> list;
    private OnSelectionChangedListener selectionChangedListener;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewItem;
        public TextView textViewTitle;
        public TextView textViewUserName;
        public TextView textViewDuration;
        public CheckBox checkBoxSelection;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewDuration = itemView.findViewById(R.id.textViewDuration);
            checkBoxSelection = itemView.findViewById(R.id.checkBoxSelection);
        }
    }

    public TracksSelectionRecycleViewAdapter(Context context, ArrayList<Pair<Song, Boolean>> list) {
        this.context = context;
        this.list = list;
    }
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_track_selection_recycleview_item, parent, false);
        return new MyViewHolder(itemView);
    }

    private UserInfo currentUserInfo = new UserInfo();
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Pair<Song, Boolean> pair = list.get(position);
        Song item = pair.first;
        boolean isSelected = pair.second;

        holder.textViewTitle.setText(item.getTitle());
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
        holder.textViewDuration.setText("Duration: " + String.valueOf(item.getDuration()));

        // Load image using Glide library
        String imagePath = "images/" + item.getImageFileName();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context).load(uri).into(holder.imageViewItem);
        }).addOnFailureListener(exception -> {
            holder.imageViewItem.setImageResource(R.drawable.lyos); // Handle any errors
        });

        holder.checkBoxSelection.setChecked(isSelected);
        holder.checkBoxSelection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Pair<Song, Boolean> updatedPair = new Pair<>(item, isChecked);
            list.set(position, updatedPair);
            if (selectionChangedListener != null) {
                selectionChangedListener.onSelectionChanged(position, isChecked);
            }
        });
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
}
