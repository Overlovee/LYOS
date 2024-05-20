package com.example.lyos.Models;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.lyos.FirebaseHandlers.UserHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ProfileDataLoader {

    public static void loadProfileData(Context context, OnProfileDataLoadedListener listener) {
        // Kiểm tra xem có dữ liệu tài khoản đã được lưu trữ hay không
        String savedAccount = AccountUtils.getSavedAccount(context);
        if (savedAccount != null) {
            UserHandler userHandler = new UserHandler();
            userHandler.getInfoByID(savedAccount).addOnCompleteListener(new OnCompleteListener<UserInfo>() {
                @Override
                public void onComplete(@NonNull Task<UserInfo> task) {
                    if (task.isSuccessful()) {
                        UserInfo user = task.getResult();
                        if (user != null) {
                            if (listener != null) {
                                listener.onProfileDataLoaded(user);
                            }
                        } else {
                            if (listener != null) {
                                listener.onProfileDataLoadFailed();
                            }
                        }
                    } else {
                        if (listener != null) {
                            listener.onProfileDataLoadFailed();
                        }
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onProfileDataLoadFailed();
            }
        }
    }

    public interface OnProfileDataLoadedListener {
        void onProfileDataLoaded(UserInfo user);
        void onProfileDataLoadFailed();
    }
}

