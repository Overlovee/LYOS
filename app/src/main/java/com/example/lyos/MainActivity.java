package com.example.lyos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.Models.AccountUtils;
import com.example.lyos.Models.Album;
import com.example.lyos.Models.Playlist;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.Services.MusicPlayerService;
import com.example.lyos.databinding.ActivityMainBinding;
import com.example.lyos.databinding.CurrentlyPlayingListBottomSheetDialogLayoutBinding;
import com.example.lyos.databinding.CurrentlyPlayingSongDialogLayoutBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding activityMainBinding;
    private String account = "";
    private UserInfo user;
    SongHandler songHandler;
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleSignInClient;
    private MusicPlayerService musicPlayerService;
    public void setAccount(String id){
        this.account = id;
        AccountUtils.saveAccount(MainActivity.this, account);
    }
    public void signOut(){
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                AccountUtils.removeAccount(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        Intent intent = new Intent(MainActivity.this, SplashActivity.class);
        startActivity(intent);

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        if(acc != null){
            UserHandler userHandler = new UserHandler();
            userHandler.getUserByEmail(acc.getEmail()).addOnCompleteListener(new OnCompleteListener<UserInfo>() {
                @Override
                public void onComplete(@NonNull Task<UserInfo> task) {
                    if (task.isSuccessful()) {
                        user = task.getResult();
                        if (user != null) {
                            setAccount(user.getId());
                        } else {
                            Intent intent = new Intent(MainActivity.this, SetUpProfile.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Exception e = task.getException();
                        e.printStackTrace();
                        Intent intent = new Intent(MainActivity.this, Login.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
        else {
            intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        }
        songHandler = new SongHandler();
        if (player != null) {
            activityMainBinding.layoutNowPlaying.setVisibility(View.VISIBLE);
        } else {
            activityMainBinding.layoutNowPlaying.setVisibility(View.GONE);
        }


        setSupportActionBar(activityMainBinding.toolbar);
        activityMainBinding.bottomNavigationMain.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                int id = item.getItemId();
                activityMainBinding.toolbar.setVisibility(View.VISIBLE);
                if (id == R.id.navigation_search) {
                    fragment = new SearchFragment();
                    activityMainBinding.textViewFragmentTitle.setVisibility(View.GONE);
                    activityMainBinding.searchBar.setVisibility(View.VISIBLE);
                    activityMainBinding.searchBar.setIconifiedByDefault(false);
                } else if (id == R.id.navigation_profile) {
                    activityMainBinding.toolbar.setVisibility(View.GONE);
                    fragment = new ProfileFragment();
                } else if (id == R.id.navigation_albums) {
                    activityMainBinding.textViewFragmentTitle.setVisibility(View.GONE);
                    activityMainBinding.searchBar.setVisibility(View.VISIBLE);
                    activityMainBinding.searchBar.setIconifiedByDefault(false);
                    fragment = new AlbumsFragment();
                }
                else {
                    activityMainBinding.textViewFragmentTitle.setVisibility(View.VISIBLE);
                    activityMainBinding.searchBar.setVisibility(View.GONE);
                    if (id == R.id.navigation_home) {
                        fragment = new HomeFragment();
                    } else if (id == R.id.navigation_playlists) {
                        fragment = new PlaylistsFragment();
                    } else {
                        return false;
                    }
                }
                String title = item.getTitle().toString();
                activityMainBinding.textViewFragmentTitle.setText(title);
                loadFragment(fragment);
                return true;
            }
        });
        activityMainBinding.bottomNavigationMain.setSelectedItemId(R.id.navigation_home);

        AddEvents();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
            AddBroadCastReceiverEvents();
        } else {
            // Đăng ký BroadcastReceiver cho các phiên bản thấp hơn
            AddLegacyBroadCastReceiverEvents();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Kết nối với dịch vụ
        Intent intent = new Intent(this, MusicPlayerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Ngắt kết nối với dịch vụ nếu đang kết nối
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }
    private boolean isServiceBound = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            musicPlayerService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicPlayerService = null;
            isServiceBound = false;
        }
    };
    private static final String INTERNAL_ACTION_PAUSE_OR_PLAY = "INTERNAL_PAUSE_OR_PLAY_ACTION";
    private static final String INTERNAL_ACTION_PREVIOUS = "INTERNAL_PREVIOUS_ACTION";
    private static final String INTERNAL_ACTION_NEXT = "INTERNAL_NEXT_ACTION";

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void AddBroadCastReceiverEvents() {
        // Đăng ký BroadcastReceiver để lắng nghe broadcast từ MusicPlayerService
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTERNAL_ACTION_PAUSE_OR_PLAY);
        filter.addAction(INTERNAL_ACTION_PREVIOUS);
        filter.addAction(INTERNAL_ACTION_NEXT);
        registerReceiver(handleBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    private void AddLegacyBroadCastReceiverEvents() {
        // Đăng ký BroadcastReceiver cho các phiên bản thấp hơn
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTERNAL_ACTION_PAUSE_OR_PLAY);
        filter.addAction(INTERNAL_ACTION_PREVIOUS);
        filter.addAction(INTERNAL_ACTION_NEXT);
        registerReceiver(handleBroadcastReceiver, filter);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, khởi động lại dịch vụ
                Intent serviceIntent = new Intent(this, MusicPlayerService.class);
                ContextCompat.startForegroundService(this, serviceIntent);
            } else {
                Log.d("MusicPlayerServicePermission", "Permission for notifications was denied.");
            }
        }
    }
    private final BroadcastReceiver handleBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case INTERNAL_ACTION_PAUSE_OR_PLAY:
                        Log.d("MainActivity", "Received INTERNAL_PAUSE_OR_PLAY action");
                        pauseOrplayAction();
                        break;
                    case INTERNAL_ACTION_PREVIOUS:
                        Log.d("MainActivity", "Received INTERNAL_PREVIOUS action");
                        previous();
                        break;
                    case INTERNAL_ACTION_NEXT:
                        Log.d("MainActivity", "Received INTERNAL_NEXT action");
                        next();
                        break;
                    default:
                        Log.d("MainActivity", "Unknown action received: " + action);
                }
            } else {
                Log.d("MainActivity", "Action is null");
            }
        }
    };

    // Phương thức để gọi phương thức cập nhật giao diện của thông báo từ MusicPlayerService
    private void updateNotificationUI(Bitmap imgBitmap, String title, String userName, String duration, boolean isPlaying) {
        // Gọi phương thức updateNotificationUI của MusicPlayerService để cập nhật giao diện của thông báo
        if (musicPlayerService != null) {
            musicPlayerService.updateNotificationUI(imgBitmap, title, userName, duration, isPlaying);
        }
    }

    private CurrentlyPlayingSongDialogLayoutBinding currentlyPlayingSongDialogLayoutBinding;
    private void showCurrentPlayingSongDialog() {
        if (currentSongArrayList.isEmpty()) {
            // Xử lý trường hợp danh sách rỗng
            Log.e("showCurrentPlayingSongDialog", "currentSongArrayList is empty.");
            return;
        }
        Song item = currentSongArrayList.get(player.getCurrentMediaItemIndex());
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        currentlyPlayingSongDialogLayoutBinding = CurrentlyPlayingSongDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(currentlyPlayingSongDialogLayoutBinding.getRoot());

        String imagePath = "images/" + item.getImageFileName();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(MainActivity.this)
                    .load(uri)
                    .into(currentlyPlayingSongDialogLayoutBinding.imageViewSong);
            Glide.with(MainActivity.this)
                    .load(uri)
                    .into(currentlyPlayingSongDialogLayoutBinding.imageViewBackGround);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });
        currentlyPlayingSongDialogLayoutBinding.textViewTitle.setText(item.getTitle());
        currentlyPlayingSongDialogLayoutBinding.textViewUserName.setText(currentUserInfo.getUsername());
        currentlyPlayingSongDialogLayoutBinding.textViewDescription.setText(item.getDescription());
        if (player.getPlayWhenReady()) {
            // Nếu player đang phát, dừng phát
            updatePlayPauseButton(true);
        } else {
            // Nếu player đang dừng, bắt đầu phát
            updatePlayPauseButton(false);
        }
        updateSeekBar();
        if(item.getLikedBy() != null){
            if(item.getLikedBy().contains(user.getId())){
                currentlyPlayingSongDialogLayoutBinding.buttonLike.setImageResource(R.drawable.hearted);
            }
            else {
                currentlyPlayingSongDialogLayoutBinding.buttonLike.setImageResource(R.drawable.heart);
            }
        } else {
            currentlyPlayingSongDialogLayoutBinding.buttonLike.setImageResource(R.drawable.heart);
        }
        currentlyPlayingSongDialogLayoutBinding.buttonLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem người dùng đã thích bài hát hay chưa
                if(item.getLikedBy() != null){
                    if (item.getLikedBy().contains(user.getId())) {
                        // Nếu đã thích, hủy thích bằng cách loại bỏ ID của người dùng khỏi danh sách thích
                        item.getLikedBy().remove(user.getId());
                        user.getLikes().remove(item.getId());
                        currentlyPlayingSongDialogLayoutBinding.buttonLike.setImageResource(R.drawable.heart);
                    } else {
                        // Nếu chưa thích, thêm ID của người dùng vào danh sách thích
                        item.getLikedBy().add(user.getId());
                        user.getLikes().add(item.getId());
                        currentlyPlayingSongDialogLayoutBinding.buttonLike.setImageResource(R.drawable.hearted);
                    }
                } else {
                    item.setLikedBy(new ArrayList<>());
                    item.getLikedBy().add(user.getId());
                    user.getLikes().add(item.getId());
                    currentlyPlayingSongDialogLayoutBinding.buttonLike.setImageResource(R.drawable.hearted);
                }

                // Sau khi cập nhật trạng thái thích của bài hát,
                // gọi hàm updateSong để lưu thay đổi vào cơ sở dữ liệu
                // Ví dụ:
                songHandler.updateSong(item.getId(), item)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
                UserHandler userHandler = new UserHandler();
                userHandler.update(user.getId(), user);
            }
        });

        currentlyPlayingSongDialogLayoutBinding.stopButtonPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseOrplayAction();
            }
        });

        // Lắng nghe sự kiện thay đổi vị trí của SeekBar
        currentlyPlayingSongDialogLayoutBinding.seekBarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean userSeeking = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Kiểm tra xem sự kiện này được kích hoạt bởi người dùng hay không
                if (fromUser) {
                    userSeeking = true;
                    // Cập nhật vị trí phát của media đến vị trí mới được chọn trên SeekBar
                    player.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userSeeking = false;
            }
        });
        currentlyPlayingSongDialogLayoutBinding.buttonRandomPlayingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Song> temp = currentSongArrayList;
                playPlaylistWithRandomSongs(temp);
            }
        });
        currentlyPlayingSongDialogLayoutBinding.buttonRepeatPlayingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra trạng thái hiện tại của chế độ lặp lại
                int currentRepeatMode = player.getRepeatMode();

                // Chuyển đổi chế độ lặp lại
                switch (currentRepeatMode) {
                    case Player.REPEAT_MODE_ALL:
                        // Nếu đang ở chế độ lặp lại tất cả các bài hát, chuyển sang chế độ lặp lại một bài hát
                        player.setRepeatMode(Player.REPEAT_MODE_ONE);
                        break;
                    default:
                        // Mặc định, chuyển sang chế độ lặp lại tất cả các bài hát
                        player.setRepeatMode(Player.REPEAT_MODE_ALL);
                        break;
                }
                updateRepeatButtonUI(player.getRepeatMode());
            }
        });
        currentlyPlayingSongDialogLayoutBinding.nextButtonPlayback.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                next();
            }
        });
        currentlyPlayingSongDialogLayoutBinding.preButtonPlayback.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                previous();
                handler.removeCallbacks(updateSeekBarRunnable);
                updateSeekBar();
                handler.post(updateSeekBarRunnable);
            }
        });

        currentlyPlayingSongDialogLayoutBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        currentlyPlayingSongDialogLayoutBinding.buttonShowPlayingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCurrentPlayingListDialog();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }
    private CurrentlyPlayingListBottomSheetDialogLayoutBinding currentlyPlayingListDialogLayoutBinding;
    private void showCurrentPlayingListDialog() {

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        currentlyPlayingListDialogLayoutBinding = CurrentlyPlayingListBottomSheetDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(currentlyPlayingListDialogLayoutBinding.getRoot());

        SongRecycleViewAdapter adapter = new SongRecycleViewAdapter(MainActivity.this, currentSongArrayList);
        currentlyPlayingListDialogLayoutBinding.recycleViewItems.setAdapter(adapter);
        currentlyPlayingListDialogLayoutBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(MainActivity.this ,DividerItemDecoration.VERTICAL));
        RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(MainActivity.this);
        currentlyPlayingListDialogLayoutBinding.recycleViewItems.setLayoutManager(mLayoutManager);
        currentlyPlayingListDialogLayoutBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());

        currentlyPlayingListDialogLayoutBinding.buttonRandomPlayingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPlaylistWithRandomSongs(currentSongArrayList);
                SongRecycleViewAdapter adapter = new SongRecycleViewAdapter(MainActivity.this, currentSongArrayList);
                currentlyPlayingListDialogLayoutBinding.recycleViewItems.setAdapter(adapter);
                currentlyPlayingListDialogLayoutBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(MainActivity.this ,DividerItemDecoration.VERTICAL));
                RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(MainActivity.this);
                currentlyPlayingListDialogLayoutBinding.recycleViewItems.setLayoutManager(mLayoutManager);
                currentlyPlayingListDialogLayoutBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());
            }
        });
        currentlyPlayingListDialogLayoutBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
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
    public void clearFocus(){
        activityMainBinding.searchBar.clearFocus();
    }
    private void performSearch(String searchString) {
        SearchFragment searchFragment = new SearchFragment();
        Bundle bundle = new Bundle();
        bundle.putString("search_string", searchString);
        searchFragment.setArguments(bundle);
        loadFragment(searchFragment);
    }
    //Player set up
    public ExoPlayer player;
    Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;
    public ArrayList<Song> currentSongArrayList;
    private UserInfo currentUserInfo;
    private void setVisibleLayoutNowPlaying(boolean visible) {
        if (visible) {
            if (activityMainBinding.layoutNowPlaying.getVisibility() == View.GONE) {
                activityMainBinding.layoutNowPlaying.setVisibility(View.VISIBLE);
                // Khởi động dịch vụ
                Intent intent = new Intent(this, MusicPlayerService.class);
                startService(intent);

                // Kết nối với dịch vụ
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            }
            if (player == null) {
                initializePlayerListener();
            }
            if (currentSongArrayList == null) {
                currentSongArrayList = new ArrayList<>();
            }
        } else {
            if (activityMainBinding.layoutNowPlaying.getVisibility() == View.VISIBLE) {
                activityMainBinding.layoutNowPlaying.setVisibility(View.GONE);
                // Ngắt kết nối với dịch vụ
                if (isServiceBound) {
                    unbindService(serviceConnection);
                    isServiceBound = false;
                }

                // Dừng dịch vụ
                Intent intent = new Intent(this, MusicPlayerService.class);
                stopService(intent);
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    public void setAudioNowPlaying(Song item) {
        initializePlayerListener();
        // Thêm bài hát vào danh sách hiện tại
        if(currentSongArrayList == null){
            currentSongArrayList = new ArrayList<>();
            currentSongArrayList.add(item);
        }
        else {
            currentSongArrayList.add(item);
        }

        if(item.getType().equals("system")){
            // Tạo đường dẫn tới tệp mp3
            String mp3Path = "mp3s/" + item.getMp3FileName();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(mp3Path);

            // Lấy URL của tệp mp3 và thêm vào danh sách phát của player
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                /// Tạo MediaItem từ URL
                MediaItem mediaItem = MediaItem.fromUri(uri);
                // Thêm MediaItem vào player
                player.addMediaItem(mediaItem);
                // Chuẩn bị player và phát ngay bài hát vừa thêm vào
                player.setRepeatMode(Player.REPEAT_MODE_ALL);
                player.prepare();
                int lastIndex = player.getMediaItemCount() - 1;
                player.seekTo(lastIndex, 0);
                // Hiển thị layout Now Playing nếu chưa hiển thị
                setVisibleLayoutNowPlaying(true);
                play();

                // Cập nhật UI với thông tin bài hát hiện tại
                updateNowPlayingUI(item);
            }).addOnFailureListener(exception -> {
                // Xử lý lỗi tải URL
                Toast.makeText(MainActivity.this, "Error: Cannot load mp3!", Toast.LENGTH_LONG).show();
            });
        }
        else if(item.getType().equals("youtube")){
            String youtubeLink = "http://youtube.com/watch?v=" + item.getMp3FileName(); // Assuming song.getMp3FileName() returns the video ID
            new YouTubeExtractor(MainActivity.this) {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                    if (ytFiles == null) {
                        Log.e("YouTubeExtractor", "ytFiles is null. Video ID: " + item.getMp3FileName());
                        Toast.makeText(MainActivity.this, "Error: Cannot extract mp3!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (ytFiles.size() == 0) {
                        Log.e("YouTubeExtractor", "ytFiles is empty. Video ID: " + item.getMp3FileName());
                        Toast.makeText(MainActivity.this, "Error: Cannot extract mp3!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    for (int i = 0; i < ytFiles.size(); i++) {
                        int key = ytFiles.keyAt(i);
                        YtFile ytFile = ytFiles.get(key);
                        Log.d("YouTubeExtractor", "Format: " + ytFile.getFormat().getHeight() + " URL: " + ytFile.getUrl());

                        if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                            String downloadUrl = ytFile.getUrl();
                            // Tạo MediaItem từ URL
                            MediaItem mediaItem = MediaItem.fromUri(downloadUrl);
                            // Thêm MediaItem vào player
                            player.addMediaItem(mediaItem);
                            // Chuẩn bị player và phát ngay bài hát vừa thêm vào
                            player.setRepeatMode(Player.REPEAT_MODE_ALL);
                            player.prepare();
                            player.seekTo(0, C.TIME_UNSET); // Nhảy đến bài hát mới nhất
                            // Hiển thị layout Now Playing nếu chưa hiển thị
                            setVisibleLayoutNowPlaying(true);
                            play();

                            // Cập nhật UI với thông tin bài hát hiện tại
                            updateNowPlayingUI(item);
                            break;
                        }
                    }
                }

                @Override
                protected void onCancelled() {
                    super.onCancelled();
                    Toast.makeText(MainActivity.this, "Error: Extraction was cancelled!", Toast.LENGTH_LONG).show();
                }
            }.extract(youtubeLink, false, false);

        }
    }
    @SuppressLint("StaticFieldLeak")
    public void playNewPlaylist(ArrayList<Song> songArrayList) {
        // Kiểm tra nếu songArrayList là null hoặc trống
        if (songArrayList == null || songArrayList.isEmpty()) {
            Log.e("playNewPlaylist", "Song list is null or empty.");
            return;
        }

        // Initialize the player if it's null
        if (player == null) {
            initializePlayerListener();
        } else {
            // Clear the current playlist and stop the player
            clearPlaylist();
            if (player == null) {
                initializePlayerListener();
            }
        }
        // Cập nhật currentSongArrayList với danh sách mới
        currentSongArrayList = songArrayList;
        // Initialize a list to store all MediaItems
        List<MediaItem> mediaItems = Collections.synchronizedList(new ArrayList<>());
        // Initialize a list to store all download tasks
        List<Task<Uri>> downloadTasks = new ArrayList<>();

        // Iterate through the song list and create download tasks for each song
        for (Song song : songArrayList) {
            if(song.getType().equals("system")){
                String mp3Path = "mp3s/" + song.getMp3FileName();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(mp3Path);
                Task<Uri> downloadTask = storageRef.getDownloadUrl();
                downloadTasks.add(downloadTask);
            }
            else if (song.getType().equals("youtube")) {
                String youtubeLink = "http://youtube.com/watch?v=" + song.getMp3FileName(); // Assuming song.getMp3FileName() returns the video ID
                new YouTubeExtractor(MainActivity.this) {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                        if (ytFiles != null) {
                            for (int i = 0; i < ytFiles.size(); i++) {
                                int key = ytFiles.keyAt(i);
                                YtFile ytFile = ytFiles.get(key);
                                if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                                    String downloadUrl = ytFile.getUrl();
                                    // Create a MediaItem from the download URL
                                    MediaItem mediaItem = MediaItem.fromUri(downloadUrl);
                                    mediaItems.add(mediaItem);
                                    break;
                                }
                            }
                        }
                    }
                }.extract(youtubeLink, false, false);
            }
        }

        // Wait for all download tasks to complete
        Tasks.whenAllSuccess(downloadTasks).addOnSuccessListener(results -> {
            for (int i = 0; i < results.size(); i++) {
                Uri uri = (Uri) results.get(i);
                // Create a MediaItem from the download URL
                MediaItem mediaItem = MediaItem.fromUri(uri);
                mediaItems.add(mediaItem);
            }

            // Kiểm tra nếu mediaItems không bị null hoặc trống
            if (mediaItems.isEmpty()) {
                Log.e("playNewPlaylist", "Media items list is empty.");
                return;
            }

            Log.d("playNewPlaylist", "Media items added: " + mediaItems.size());

            // Add all MediaItems to the player and start playback from the beginning
            try {
                Log.d("playNewPlaylist", "Adding media items to player.");
                player.addMediaItems(mediaItems);
                player.setRepeatMode(Player.REPEAT_MODE_ALL);
                player.prepare();
                play();
                setVisibleLayoutNowPlaying(true);

                // Update the UI for the currently playing song
                if (!songArrayList.isEmpty()) {
                    updateNowPlayingUI(songArrayList.get(0));
                }
            } catch (Exception e) {
                Log.e("playNewPlaylist", "Error adding media items to player", e);
            }
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.e("playNewPlaylist", "Failed to get download URLs", exception);
        });
    }
    public void playPlaylistWithRandomSongs(ArrayList<Song> songArrayList){
        ArrayList<Song> shuffledList = new ArrayList<>(songArrayList);
        Collections.shuffle(shuffledList);
        playNewPlaylist(shuffledList);
    }
    private void updateNowPlayingUI(Song song) {
        if(song.getType().equals("system")){
            if(currentlyPlayingSongDialogLayoutBinding != null) {
                currentlyPlayingSongDialogLayoutBinding.buttonLike.setEnabled(false);
            }

            // Cập nhật tiêu đề bài hát
            activityMainBinding.textViewTitle.setText(song.getTitle());
            // Cập nhật tên người dùng
            UserHandler userHandler = new UserHandler();
            userHandler.getInfoByID(song.getUserID()).addOnCompleteListener(new OnCompleteListener<UserInfo>() {
                @Override
                public void onComplete(@NonNull Task<UserInfo> task) {
                    if (task.isSuccessful()) {
                        currentUserInfo = task.getResult();
                        if (currentUserInfo != null) {
                            // Cập nhật hình ảnh bài hát
                            String imagePath = "images/" + song.getImageFileName();
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                Glide.with(MainActivity.this).load(uri).into(activityMainBinding.imageViewNowPlaying);
                                if(currentlyPlayingSongDialogLayoutBinding != null){
                                    Glide.with(MainActivity.this).load(uri).into(currentlyPlayingSongDialogLayoutBinding.imageViewSong);
                                    Glide.with(MainActivity.this).load(uri).into(currentlyPlayingSongDialogLayoutBinding.imageViewBackGround);
                                }
                                Glide.with(MainActivity.this)
                                        .asBitmap()
                                        .load(uri)
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                updateNotificationUI(resource,
                                                        song.getTitle(),
                                                        currentUserInfo.getUsername(),
                                                        String.valueOf(song.getDuration()),
                                                        player.getPlayWhenReady());
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                                // Xử lý khi việc tải hình ảnh bị hủy
                                            }
                                        });
                            }).addOnFailureListener(exception -> {
                                // Handle any errors
                            });
                            activityMainBinding.textViewUserName.setText(currentUserInfo.getUsername());
                            if(currentlyPlayingSongDialogLayoutBinding != null){
                                currentlyPlayingSongDialogLayoutBinding.textViewTitle.setText(song.getTitle());
                                currentlyPlayingSongDialogLayoutBinding.textViewUserName.setText(currentUserInfo.getUsername());
                                currentlyPlayingSongDialogLayoutBinding.textViewDescription.setText(song.getDescription());
                                if(song.getLikedBy() != null){
                                    if(song.getLikedBy().contains(user.getId())){
                                        currentlyPlayingSongDialogLayoutBinding.buttonLike.setImageResource(R.drawable.hearted);
                                    }
                                    else {
                                        currentlyPlayingSongDialogLayoutBinding.buttonLike.setImageResource(R.drawable.heart);
                                    }
                                } else {
                                    currentlyPlayingSongDialogLayoutBinding.buttonLike.setImageResource(R.drawable.heart);
                                }
                                pause();
                                play();
                            }
                        } else {
                            activityMainBinding.textViewUserName.setText("Unknown");
                        }
                    } else {
                        // Handle any errors
                    }
                }
            });
        }
        else if(song.getType().equals("youtube")){
            if(currentlyPlayingSongDialogLayoutBinding != null) {
                currentlyPlayingSongDialogLayoutBinding.buttonLike.setEnabled(true);
            }
            Glide.with(MainActivity.this).load(song.getImageFileName()).into(activityMainBinding.imageViewNowPlaying);
            if(currentlyPlayingSongDialogLayoutBinding != null){
                Glide.with(MainActivity.this).load(song.getImageFileName()).into(currentlyPlayingSongDialogLayoutBinding.imageViewSong);
                Glide.with(MainActivity.this).load(song.getImageFileName()).into(currentlyPlayingSongDialogLayoutBinding.imageViewBackGround);
            }
            activityMainBinding.textViewTitle.setText(song.getTitle());
            activityMainBinding.textViewUserName.setText(song.getUserID());
            currentlyPlayingSongDialogLayoutBinding.textViewTitle.setText(song.getTitle());
            currentlyPlayingSongDialogLayoutBinding.textViewUserName.setText(currentUserInfo.getUsername());
            currentlyPlayingSongDialogLayoutBinding.textViewDescription.setText(song.getDescription());
            pause();
            play();
        }
    }
    private void updateRepeatButtonUI(int repeatMode){
        if (currentlyPlayingSongDialogLayoutBinding != null) {
            switch (repeatMode) {
                case Player.REPEAT_MODE_ONE:
                    currentlyPlayingSongDialogLayoutBinding.buttonRepeatPlayingList.setImageResource(R.drawable.repeat_once);
                    break;
                case Player.REPEAT_MODE_ALL:
                    currentlyPlayingSongDialogLayoutBinding.buttonRepeatPlayingList.setImageResource(R.drawable.repeat);
                    break;
                default:
                    break;
            }
        }
    }
    private void initializePlayerListener() {
        if(player == null){
            player = new ExoPlayer.Builder(MainActivity.this).build();
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    updatePlayPauseButton(player.getPlayWhenReady() && state == Player.STATE_READY);
                    if (state == Player.STATE_READY) {
                        if (currentlyPlayingSongDialogLayoutBinding != null) {
                            long duration = player.getDuration();
                            currentlyPlayingSongDialogLayoutBinding.seekBarDuration.setMax((int) duration);
                        }
                        if (player.getPlayWhenReady()) {
                            updateSeekBar();
                        } else {

                        }
                    } else if (state == Player.STATE_ENDED) {

                    }
                }

                @Override
                public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                    if (mediaItem != null) {
                        // Lấy index của bài hát hiện tại
                        int currentIndex = player.getCurrentMediaItemIndex();

                        // Kiểm tra currentSongArrayList không null và index hợp lệ
                        if (currentSongArrayList != null && currentIndex >= 0 && currentIndex < currentSongArrayList.size()) {
                            // Lấy bài hát từ danh sách dựa trên index
                            Song currentSong = currentSongArrayList.get(currentIndex);
                            // Cập nhật UI với thông tin bài hát hiện tại
                            updateNowPlayingUI(currentSong);
                            currentSong.setListens(currentSong.getListens() + 1);
                            songHandler.updateSong(currentSong.getId(), currentSong);
                        } else {
                            // Log lỗi nếu currentSongArrayList bị null hoặc index không hợp lệ
                            Log.e("onMediaItemTransition", "currentSongArrayList is null or index out of bounds. currentIndex: " + currentIndex);
                        }
                    }
                }
            });
        }
    }
    private void updatePlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            activityMainBinding.stopButtonPlayback.setImageResource(R.drawable.pause);
            if (musicPlayerService != null) {
                musicPlayerService.updatePlayPauseButton(isPlaying);
            }
            if (currentlyPlayingSongDialogLayoutBinding != null) {
                currentlyPlayingSongDialogLayoutBinding.stopButtonPlayback.setImageResource(R.drawable.pause_button);
            }
        } else {
            activityMainBinding.stopButtonPlayback.setImageResource(R.drawable.play);
            if (musicPlayerService != null) {
                musicPlayerService.updatePlayPauseButton(isPlaying);
            }
            if (currentlyPlayingSongDialogLayoutBinding != null) {
                currentlyPlayingSongDialogLayoutBinding.stopButtonPlayback.setImageResource(R.drawable.play_button);
            }
            handler.post(updateSeekBarRunnable);
        }
    }
    // Phương thức để cập nhật seekBar và textViewDuration
    private void updateSeekBar() {
        if (handler != null) {
            updateSeekBarRunnable = new Runnable() {
                @Override
                public void run() {
                    if (player != null && player.isPlaying()) {
                        // Lấy thời gian hiện tại của media đang phát
                        long currentPosition = player.getCurrentPosition();
                        // Lấy thời gian tổng cộng của media đang phát
                        long totalDuration = player.getDuration();

                        // Tính thời gian còn lại
                        long remainingTime = totalDuration - currentPosition;

                        // Chuyển đổi thời gian còn lại thành định dạng phút:giây
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime);
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTime) - TimeUnit.MINUTES.toSeconds(minutes);

                        // Thiết lập giá trị tối đa cho seekBar
                        activityMainBinding.seekBar.setMax((int) totalDuration);
                        activityMainBinding.seekBar.setProgress((int) currentPosition);

                        if (currentlyPlayingSongDialogLayoutBinding != null) {
                            String remainingTimeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                            currentlyPlayingSongDialogLayoutBinding.textViewDuration.setText(remainingTimeText);
                            // Thiết lập giá trị tối đa cho seekBar
                            currentlyPlayingSongDialogLayoutBinding.seekBarDuration.setMax((int) totalDuration);
                            // Cập nhật giá trị của SeekBar để hiển thị vị trí hiện tại của media
                            currentlyPlayingSongDialogLayoutBinding.seekBarDuration.setProgress((int) currentPosition);
                        }
                        musicPlayerService.updateProcessAndDuration((int) currentPosition, (int) totalDuration);
                        // Gửi thông điệp cập nhật sau mỗi khoảng thời gian nhất định (ví dụ: 1 giây)
                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.post(updateSeekBarRunnable);
        }
    }
    public void play() {
        player.play();
        // Bắt đầu cập nhật seekBar và textViewDuration nếu media đang phát
        if (player.isPlaying()) {
            updateSeekBar();
        }
    }
    public void stop() {
        player.stop();
        // Gỡ bỏ việc cập nhật seekBar và textViewDuration
        handler.removeCallbacksAndMessages(null);
        handler.removeCallbacks(updateSeekBarRunnable);
    }
    public void pause() {
        player.pause();
        // Gỡ bỏ việc cập nhật seekBar và textViewDuration
        handler.removeCallbacks(updateSeekBarRunnable);
    }
    @OptIn(markerClass = UnstableApi.class)
    public void next(){
        int currentIndex = player.getCurrentMediaItemIndex();
        int lastIndex = player.getMediaItemCount() - 1;

        // Nếu đang ở bài hát cuối cùng, chuyển đến bài hát đầu tiên
        if (currentIndex == lastIndex) {
            player.seekTo(0, 0);
        } else {
            player.next();
        }
    }
    @OptIn(markerClass = UnstableApi.class)
    public void previous(){
        // Kiểm tra xem có phải đang ở đầu danh sách không
        if (player.hasPrevious()) {
            // Nếu có, quay lại bài hát trước đó trong danh sách
            player.previous();
        } else {
            // Nếu không, chuyển đến bài hát cuối cùng trong danh sách
            int lastIndex = player.getMediaItemCount() - 1;
            player.seekTo(lastIndex, 0);
        }
    }
    public void pauseOrplayAction(){
        if (player.getPlayWhenReady()) {
            // Nếu player đang phát, dừng phát
            pause();
            updatePlayPauseButton(false);
        } else {
            // Nếu player đang dừng, bắt đầu phát
            play();
            updatePlayPauseButton(true);
        }
    }

    private void clearPlaylist(){
        if(player != null){
            stop();
            player.clearMediaItems();
            player.release();
            player = null; // Set player to null to ensure it will be reinitialized later
            currentSongArrayList.clear();
            setVisibleLayoutNowPlaying(false);

            // Ngắt kết nối với dịch vụ
            if (isServiceBound) {
                unbindService(serviceConnection);
                isServiceBound = false;
            }

            // Dừng dịch vụ
            Intent intent = new Intent(this, MusicPlayerService.class);
            stopService(intent);
        }
    }
    @SuppressLint("StaticFieldLeak")
    public void addToNextUp(Song item) {
        if (player == null) {
            setAudioNowPlaying(item);
            return;
        }
        else {
            if (player.getMediaItemCount() == 0) {
                setAudioNowPlaying(item);
                return;
            }
        }

        if(item.getType().equals("system")){
            // Tạo đường dẫn tới tệp mp3
            String mp3Path = "mp3s/" + item.getMp3FileName();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(mp3Path);

            // Lấy URL của tệp mp3 và thêm vào danh sách phát của player
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Tạo MediaItem từ URL
                MediaItem mediaItem = MediaItem.fromUri(uri);

                // Lấy vị trí hiện tại của bài hát đang phát
                int currentIndex = player.getCurrentMediaItemIndex();

                // Thêm MediaItem vào vị trí tiếp theo trong danh sách phát
                player.addMediaItem(currentIndex + 1, mediaItem);

                // Cập nhật danh sách bài hát hiện tại
                currentSongArrayList.add(currentIndex + 1, item);


                // Hiển thị thông báo thành công
                Toast.makeText(MainActivity.this, "Added to next up!", Toast.LENGTH_SHORT).show();

            }).addOnFailureListener(exception -> {
                // Xử lý lỗi tải URL
                Toast.makeText(MainActivity.this, "Error: Cannot load mp3!", Toast.LENGTH_LONG).show();
            });
        }
        else if(item.getType().equals("youtube")){

            String youtubeLink = "http://youtube.com/watch?v=" + item.getMp3FileName(); // Assuming song.getMp3FileName() returns the video ID
            new YouTubeExtractor(MainActivity.this) {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                    if (ytFiles != null) {
                        for (int i = 0; i < ytFiles.size(); i++) {
                            int key = ytFiles.keyAt(i);
                            YtFile ytFile = ytFiles.get(key);
                            if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                                String downloadUrl = ytFile.getUrl();
                                // Tạo MediaItem từ URL
                                MediaItem mediaItem = MediaItem.fromUri(downloadUrl);

                                // Lấy vị trí hiện tại của bài hát đang phát
                                int currentIndex = player.getCurrentMediaItemIndex();

                                // Thêm MediaItem vào vị trí tiếp theo trong danh sách phát
                                player.addMediaItem(currentIndex + 1, mediaItem);

                                // Cập nhật danh sách bài hát hiện tại
                                currentSongArrayList.add(currentIndex + 1, item);

                                // Hiển thị thông báo thành công
                                Toast.makeText(MainActivity.this, "Added to next up!", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                }

                @Override
                protected void onCancelled() {
                    super.onCancelled();
                    Toast.makeText(MainActivity.this, "Error: Cannot load mp3!", Toast.LENGTH_LONG).show();
                }
            }.extract(youtubeLink, false, false);
        }
    }
    public void addToNextUp(ArrayList<Song> songArrayList) {
        if (player == null) {
            initializePlayerListener();
        }
        if (currentSongArrayList == null) {
            currentSongArrayList = new ArrayList<>();
        }

        // Hiển thị layout Now Playing nếu chưa hiển thị
        setVisibleLayoutNowPlaying(true);

        // Lấy vị trí hiện tại của bài hát đang phát
        int currentIndex = (player != null) ? player.getCurrentMediaItemIndex() : -1;

        // Thêm tất cả các bài hát trong songArrayList vào sau bài hát đang phát trong danh sách hiện tại
        if (currentIndex >= 0 && currentIndex < currentSongArrayList.size()) {
            currentSongArrayList.addAll(currentIndex + 1, songArrayList);
        } else {
            currentSongArrayList.addAll(songArrayList);
        }

        if (player != null) {
            for (Song song : songArrayList) {
                String mp3Path = "mp3s/" + song.getMp3FileName();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(mp3Path);
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Tạo MediaItem từ URL
                    MediaItem mediaItem = MediaItem.fromUri(uri);
                    // Thêm MediaItem vào vị trí tiếp theo của bài hát đang phát
                    if (currentIndex >= 0) {
                        player.addMediaItem(currentIndex + 1, mediaItem);
                    } else {
                        player.addMediaItem(mediaItem);
                    }

                    // Kiểm tra trạng thái của player
                    if (player.getPlaybackState() == Player.STATE_IDLE) {
                        player.prepare();
                        player.play();
                    } else if (!player.isPlaying()) {
                        player.play();
                    }

                    // Nếu player chưa có bài hát nào, phát bài hát đầu tiên ngay sau khi thêm
                    if (player.getMediaItemCount() == 1) {
                        player.seekTo(0, 0);
                        player.play();
                    }
                }).addOnFailureListener(exception -> {
                    // Xử lý lỗi tải URL
                    Toast.makeText(MainActivity.this, "Error: Cannot load mp3!", Toast.LENGTH_LONG).show();
                });
            }
        }
    }
    public void hideToolbar(){
        activityMainBinding.toolbar.setVisibility(View.GONE);
    }
    public void showToolbar(){
        activityMainBinding.textViewFragmentTitle.setVisibility(View.GONE);
        activityMainBinding.searchBar.setVisibility(View.VISIBLE);
        activityMainBinding.searchBar.setIconifiedByDefault(false);
        activityMainBinding.toolbar.setVisibility(View.VISIBLE);
    }
    public void openProfileFragment() {
        activityMainBinding.bottomNavigationMain.setSelectedItemId(R.id.navigation_profile);
    }
    public void openProfileDetailFragment(UserInfo item) {
        hideToolbar();
        ProfileDetailFragment fragment = new ProfileDetailFragment(item);
        loadFragment(fragment);
    }
    public void openAlbumDetailFragment(Album item) {
        hideToolbar();
        AlbumDetailFragment fragment = new AlbumDetailFragment(item);
        loadFragment(fragment);
    }
    public void openPlaylistDetailFragment(Playlist item) {
        hideToolbar();
        PlaylistDetailFragment fragment = new PlaylistDetailFragment(item);
        loadFragment(fragment);
    }
    public void openSearchByTagFragment(String tag) {
        hideToolbar();
        SearchByTagFragment fragment = new SearchByTagFragment(tag);
        loadFragment(fragment);
    }
    public void openSeeAllFragment(String userID) {
        hideToolbar();
        SeeAllFragment fragment = new SeeAllFragment(userID);
        loadFragment(fragment);
    }
    public void openSeeAllFragment(ArrayList<String> likeArrayList) {
        hideToolbar();
        SeeAllFragment fragment = new SeeAllFragment(likeArrayList);
        loadFragment(fragment);
    }
    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(activityMainBinding.frameFragment.getId(), fragment);
        transaction.commit();
    }
    private void AddEvents(){
        activityMainBinding.searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityMainBinding.searchBar.setIconifiedByDefault(false);
            }
        });
        activityMainBinding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!query.isEmpty()){
                    performSearch(query);
                    clearFocus();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    performSearch("");
                    clearFocus();
                }
                return false;
            }

        });
        activityMainBinding.clearButtonPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPlaylist();
            }
        });
        activityMainBinding.preButtonPlayback.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                previous();
            }
        });
        activityMainBinding.nextButtonPlayback.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                next();
            }
        });
        activityMainBinding.stopButtonPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseOrplayAction();
            }
        });
        activityMainBinding.layoutNowPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCurrentPlayingSongDialog();
            }
        });
    }
    @Override
    protected void onDestroy() {
        clearPlaylist();
        super.onDestroy();
    }
}