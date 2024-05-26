package com.example.lyos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.Models.AccountUtils;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.databinding.ActivitySetUpProfileBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SetUpProfile extends AppCompatActivity {

    private ActivitySetUpProfileBinding activitySetUpProfileBinding;
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySetUpProfileBinding = ActivitySetUpProfileBinding.inflate(getLayoutInflater());
        setContentView(activitySetUpProfileBinding.getRoot());

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        if(acc != null){
            activitySetUpProfileBinding.editTextUserName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().trim().isEmpty()) {
                        activitySetUpProfileBinding.buttonNext.setEnabled(true);
                    } else {
                        activitySetUpProfileBinding.buttonNext.setEnabled(false);
                    }
                }
            });
            activitySetUpProfileBinding.buttonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserHandler userHandler = new UserHandler();
                    UserInfo newUser = new UserInfo();
                    newUser.setEmail(acc.getEmail());
                    newUser.setUsername(activitySetUpProfileBinding.editTextUserName.getText().toString());
                    userHandler.add(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(SetUpProfile.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Thêm người dùng thất bại
                                Exception e = task.getException();
                                e.printStackTrace();
                                Intent intent = new Intent(SetUpProfile.this, Login.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
            });
        }
        else {
            Intent intent = new Intent(SetUpProfile.this, Login.class);
            startActivity(intent);
            finish();
        }
        addEvents();
    }
    void addEvents(){
        activitySetUpProfileBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }
    public void signOut(){
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                AccountUtils.removeAccount(SetUpProfile.this);
                Intent intent = new Intent(SetUpProfile.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}