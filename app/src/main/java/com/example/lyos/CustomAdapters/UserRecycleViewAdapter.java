package com.example.lyos.CustomAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.MainActivity;
import com.example.lyos.Models.ProfileDataLoader;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class UserRecycleViewAdapter extends RecyclerView.Adapter<UserRecycleViewAdapter.MyViewHolder>{
    private Context context;
    private ArrayList<UserInfo> list;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewItem;
        public TextView textViewFollowAction;
        public TextView textViewTitle;
        public TextView textViewFollowers;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            textViewFollowAction = itemView.findViewById(R.id.textViewFollowAction);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewFollowers = itemView.findViewById(R.id.textViewFollowers);
        }
    }

    public UserRecycleViewAdapter(Context context, ArrayList<UserInfo> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_user_recycleview_item, parent, false);
        return new MyViewHolder(itemView);
    }

    private UserInfo currentUserInfo;
    private UserInfo user;
    FragmentActivity fragmentActivity;
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        UserInfo item = list.get(position);
        currentUserInfo = item;
        if (context instanceof FragmentActivity) {
            fragmentActivity = (FragmentActivity) context;
        }
        // Kiểm tra xem có dữ liệu tài khoản đã được lưu trữ hay không
        ProfileDataLoader.loadProfileData(context, new ProfileDataLoader.OnProfileDataLoadedListener() {
            @Override
            public void onProfileDataLoaded(UserInfo u) {
                user = u;
                setUpUI(holder, item);
            }
            @Override
            public void onProfileDataLoadFailed() {
                //fragmentActivity.getSupportFragmentManager().popBackStack();
            }
        });

    }
    public void setUpUI(UserRecycleViewAdapter.MyViewHolder holder, UserInfo item){
        holder.textViewTitle.setText(item.getUsername());
        if(item.getFollowers() == null){
            holder.textViewFollowers.setText("0 Followers");
        }
        else {
            holder.textViewFollowers.setText(String.valueOf(item.getFollowers().size()) + " Followers");
        }

        // Load image using Glide library
        String imagePath = "user_images/" + item.getProfilePhoto();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context).load(uri).into(holder.imageViewItem);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });
        if(user != null){
            if(item.getId().equals(user.getId())){
                holder.textViewFollowAction.setVisibility(View.GONE);
            }
            else {
                holder.textViewFollowAction.setVisibility(View.VISIBLE);
                if(item.getFollowers() != null){
                    if(item.getFollowers().contains(user.getId())){
                        holder.textViewFollowAction.setText("Following");
                        holder.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customPrimaryColor));
                        holder.textViewFollowAction.setBackgroundResource(R.drawable.rounded_clicked_button_view);
                    }
                    else {
                        holder.textViewFollowAction.setText("Follow");
                        holder.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customDarkColor));
                        holder.textViewFollowAction.setBackgroundResource(R.drawable.rounded_button_view);
                    }
                }
                else {
                    holder.textViewFollowAction.setText("Follow");
                    holder.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customDarkColor));
                    holder.textViewFollowAction.setBackgroundResource(R.drawable.rounded_button_view);
                }
            }
        }
        holder.textViewFollowAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserHandler userHandler = new UserHandler();
                if (holder.textViewFollowAction.getText().equals("Follow")) {
                    if(item.getFollowers() == null){
                        item.setFollowers(new ArrayList<>());
                    }
                    item.getFollowers().add(user.getId());
                    user.getFollowing().add(item.getId());
                    holder.textViewFollowers.setText(String.valueOf(item.getFollowers().size()) + " Followers");
                    userHandler.update(item.getId(), item);
                    userHandler.update(user.getId(), user);
                    holder.textViewFollowAction.setText("Following");
                    holder.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customPrimaryColor));
                    holder.textViewFollowAction.setBackgroundResource(R.drawable.rounded_clicked_button_view);
                } else {
                    if(item.getFollowers() == null){
                        item.setFollowers(new ArrayList<>());
                    }
                    else {
                        if (item.getFollowers().size() > 0) {
                            if (item.getFollowers().contains(user.getId())) {
                                item.getFollowers().remove(user.getId());
                                user.getFollowing().remove(item.getId());
                                holder.textViewFollowers.setText(String.valueOf(item.getFollowers().size()) + " Followers");
                                userHandler.update(item.getId(), item);
                                userHandler.update(user.getId(), user);
                                holder.textViewFollowAction.setText("Follow");
                                holder.textViewFollowAction.setTextColor(ContextCompat.getColor(context, R.color.customDarkColor));
                                holder.textViewFollowAction.setBackgroundResource(R.drawable.rounded_button_view);
                            }
                        }
                    }
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    if(item.getId().equals(user.getId())){
                        mainActivity.openProfileFragment();
                    }
                    else {
                        mainActivity.openProfileDetailFragment(item);
                    }
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
}
