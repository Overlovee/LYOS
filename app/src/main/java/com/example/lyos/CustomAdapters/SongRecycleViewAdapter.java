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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lyos.Models.Song;
import com.example.lyos.R;
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

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Song item = list.get(position);
        holder.textViewTitle.setText(item.getTitle());
        holder.textViewUserName.setText(item.getUserID());

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
                showDialog();
            }
        });
    }
    private void showDialog() {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.other_song_options_bottom_sheet_dialog_layout);

        LinearLayout layoutLike = dialog.findViewById(R.id.layoutLike);
        LinearLayout layoutAddToNextUp = dialog.findViewById(R.id.layoutAddToNextUp);
        LinearLayout layoutAddToPlaylist = dialog.findViewById(R.id.layoutAddToPlaylist);
        LinearLayout layoutGoToUserProfile = dialog.findViewById(R.id.layoutGoToUserProfile);
        LinearLayout layoutViewComment = dialog.findViewById(R.id.layoutViewComment);

        layoutLike.setOnClickListener(new View.OnClickListener() {
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
