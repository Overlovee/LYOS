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
import com.example.lyos.Models.YoutubeVideo;
import com.example.lyos.R;

import java.util.ArrayList;

public class YoutubeVideoRecycleViewAdapter extends RecyclerView.Adapter<YoutubeVideoRecycleViewAdapter.MyViewHolder>{
    private Context context;
    private ArrayList<YoutubeVideo> list;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewItem;
        public TextView textViewTitle;
        public TextView textViewUserName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
        }
    }

    public YoutubeVideoRecycleViewAdapter(Context context, ArrayList<YoutubeVideo> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_youtube_object_recycleview_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        YoutubeVideo item = list.get(position);
        holder.textViewTitle.setText(item.getTitle());
        holder.textViewUserName.setText(item.getChannelTitle());

        // Load image using Glide library
        Glide.with(context).load(item.getImageUrl()).into(holder.imageViewItem);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
