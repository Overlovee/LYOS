package com.example.lyos;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.lyos.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;

public class Login extends AppCompatActivity {

    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    private static final int REQ_ONE_TAP = 2;
    private boolean showOneTapUI = true;
    private ActivityLoginBinding activityLoginBinding;
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityLoginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(activityLoginBinding.getRoot());

//        oneTapClient = Identity.getSignInClient(this);
//        signUpRequest = BeginSignInRequest.builder()
//                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
//                        .setSupported(true)
//                        .build())
//                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                        .setSupported(true)
//                        // Your server's client ID, not your Android client ID.
//                        .setServerClientId(getString(R.string.default_web_client_id))
//                        // Only show accounts previously used to sign in.
//                        .setFilterByAuthorizedAccounts(true)
//                        .build())
//                // Automatically sign in when exactly one credential is retrieved.
//                .setAutoSelectEnabled(true)
//                .build();

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        addEvents();
    }
    private void addEvents(){
//        ActivityResultLauncher<IntentSenderRequest> activityResultLauncher
//                = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
//                new ActivityResultCallback<ActivityResult>() {
//                    @Override
//                    public void onActivityResult(ActivityResult o) {
//                        if(o.getResultCode() == Activity.RESULT_OK){
//                            try {
//                                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(o.getData());
//                                String idToken = credential.getGoogleIdToken();
//                                String username = credential.getId();
//                                String password = credential.getPassword();
//                                if (idToken !=  null) {
//                                    String email = credential.getId();
//                                    Toast.makeText(Login.this, email, Toast.LENGTH_LONG).show();
//                                }
//                            } catch (ApiException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                });
//        activityLoginBinding.buttonLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                oneTapClient.beginSignIn(signUpRequest)
//                        .addOnSuccessListener(Login.this, new OnSuccessListener<BeginSignInResult>() {
//                            @Override
//                            public void onSuccess(BeginSignInResult result) {
//                                IntentSenderRequest intentSenderRequest
//                                        = new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
//                                activityResultLauncher.launch(intentSenderRequest);
//                            }
//                        })
//                        .addOnFailureListener(Login.this, new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                // No saved credentials found. Launch the One Tap sign-up flow, or
//                                // do nothing and continue presenting the signed-out UI.
//                                Log.e("TAG", "Error occurred during sign-in process", e);
//                            }
//                        });
//            }
//        });
        activityLoginBinding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }
    private void signIn(){
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                finish();
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }

        }
    }
}