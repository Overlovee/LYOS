package com.example.lyos.CustomAdapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lyos.MainActivity;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.YoutubeVideo;
import com.example.lyos.R;
import com.example.lyos.databinding.YoutubeOptionsBottomSheetDialogLayoutBinding;

import java.util.ArrayList;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class YoutubeVideoRecycleViewAdapter extends RecyclerView.Adapter<YoutubeVideoRecycleViewAdapter.MyViewHolder>{
    private Context context;
    private ArrayList<YoutubeVideo> list;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewItem;
        public ImageView imageViewMoreOptionsAction;
        public TextView textViewTitle;
        public TextView textViewUserName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            imageViewMoreOptionsAction = itemView.findViewById(R.id.imageViewMoreOptionsAction);
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

        holder.imageViewMoreOptionsAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(item);
            }
        });
    }
    private YoutubeOptionsBottomSheetDialogLayoutBinding dialogLayoutBinding;
    private void showDialog(YoutubeVideo item) {
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogLayoutBinding = YoutubeOptionsBottomSheetDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogLayoutBinding.getRoot());

        Glide.with(context).load(item.getImageUrl()).into(dialogLayoutBinding.imageView);

        dialogLayoutBinding.textViewTitle.setText(item.getTitle());
        dialogLayoutBinding.textViewUserName.setText(item.getChannelTitle());
        dialogLayoutBinding.layoutAddToNextUp.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                if (context instanceof MainActivity) {
                    Song song = new Song();
                    song.setTitle(item.getTitle());
                    song.setMp3FileName(item.getVideoId());
                    song.setUserID(item.getChannelTitle());
                    song.setDescription(item.getDescription());
                    song.setUploadDate(item.getPublishedAt());
                    song.setImageFileName(item.getImageUrl());
                    song.setType("youtube");
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.addToNextUp(song);
                }
                dialog.dismiss();
            }
        });
        dialogLayoutBinding.layoutGoToYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo URI với ID video YouTube
                Uri youtubeUri = Uri.parse("https://www.youtube.com/watch?v=" + item.getVideoId());

                // Tạo Intent để mở ứng dụng YouTube với URI đã tạo
                Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, youtubeUri);

                // Kiểm tra xem có ứng dụng YouTube được cài đặt trên thiết bị không
                if (youtubeIntent.resolveActivity(context.getPackageManager()) != null) {
                    // Mở ứng dụng YouTube
                    context.startActivity(youtubeIntent);
                } else {
                    // Nếu không có ứng dụng YouTube, mở trình duyệt web và chuyển hướng đến video YouTube
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, youtubeUri);
                    context.startActivity(webIntent);
                }
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
