package com.example.lyos.CustomAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lyos.Models.Song;
import com.example.lyos.R;

import java.util.ArrayList;

public class SongRecycleViewAdapter extends RecyclerView.Adapter<SongRecycleViewAdapter.MyViewHolder>{
    private Context context;
    private ArrayList<Song> list;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewItem;
        public TextView textViewTitle;
        public TextView textViewUserName;
        public TextView textViewDuration;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
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
//        Glide.with(context).load(item.getImageFileName()).into(holder.imageViewItem);
        holder.imageViewItem.setImageResource(R.drawable.ngoailecuanhau_youngtobieedasick);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
