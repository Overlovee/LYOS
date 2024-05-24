package com.example.lyos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
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
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.Models.AccountUtils;
import com.example.lyos.Models.Album;
import com.example.lyos.Models.Playlist;
import com.example.lyos.Models.ProfileDataLoader;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.databinding.ActivityMainBinding;
import com.example.lyos.databinding.CurrentlyPlayingListBottomSheetDialogLayoutBinding;
import com.example.lyos.databinding.CurrentlyPlayingSongDialogLayoutBinding;
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
    private static final String ACCOUNT_TYPE = "com.example.lyos.account";
    private ActivityMainBinding activityMainBinding;
    private String account = "Ykd4dpxIKqjqpsAupw2J";
    private UserInfo user;
    SongHandler songHandler;
    public void setAccount(String id){
        this.account = id;
        AccountUtils.saveAccount(MainActivity.this, account);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        songHandler = new SongHandler();
        activityMainBinding.layoutNowPlaying.setVisibility(View.GONE);

        Intent intent = new Intent(MainActivity.this, SplashActivity.class);
        startActivity(intent);

        //setAccount(account);

        // Kiểm tra xem có dữ liệu tài khoản đã được lưu trữ hay không
        ProfileDataLoader.loadProfileData(MainActivity.this, new ProfileDataLoader.OnProfileDataLoadedListener() {
            @Override
            public void onProfileDataLoaded(UserInfo u) {
                user = u;
                if (user != null){
                } else {
                    finish();
                }
            }
            @Override
            public void onProfileDataLoadFailed() {
                finish();
            }
        });

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
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(activityMainBinding.frameFragment.getId(), fragment);
        transaction.addToBackStack(null);
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
                activityMainBinding.layoutNowPlaying.setVisibility(View.GONE);
                clearPlaylist();
            }
        });
        activityMainBinding.preButtonPlayback.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
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
        });
        activityMainBinding.nextButtonPlayback.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                int currentIndex = player.getCurrentMediaItemIndex();
                int lastIndex = player.getMediaItemCount() - 1;

                // Nếu đang ở bài hát cuối cùng, chuyển đến bài hát đầu tiên
                if (currentIndex == lastIndex) {
                    player.seekToDefaultPosition();
                } else {
                    player.next();
                }
            }
        });
        activityMainBinding.stopButtonPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        activityMainBinding.layoutNowPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCurrentPlayingSongDialog();
            }
        });
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
                        currentlyPlayingSongDialogLayoutBinding.buttonLike.setImageResource(R.drawable.heart);
                    } else {
                        // Nếu chưa thích, thêm ID của người dùng vào danh sách thích
                        item.getLikedBy().add(user.getId());
                        currentlyPlayingSongDialogLayoutBinding.buttonLike.setImageResource(R.drawable.hearted);
                    }
                } else {
                    item.setLikedBy(new ArrayList<>());
                    item.getLikedBy().add(user.getId());
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
            }
        });

        currentlyPlayingSongDialogLayoutBinding.stopButtonPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                int currentIndex = player.getCurrentMediaItemIndex();
                int lastIndex = player.getMediaItemCount() - 1;

                // Nếu đang ở bài hát cuối cùng, chuyển đến bài hát đầu tiên
                if (currentIndex == lastIndex) {
                    player.seekToDefaultPosition();
                } else {
                    player.next();
                }
            }
        });
        currentlyPlayingSongDialogLayoutBinding.preButtonPlayback.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                // Kiểm tra xem có phải đang ở đầu danh sách không
                if (player.hasPrevious()) {
                    // Nếu có, quay lại bài hát trước đó trong danh sách
                    player.previous();
                } else {
                    // Nếu không, chuyển đến bài hát cuối cùng trong danh sách
                    int lastIndex = player.getMediaItemCount() - 1;
                    player.seekTo(lastIndex, 0);
                }
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
    @Override
    protected void onDestroy() {
        clearPlaylist();
        super.onDestroy();
    }
    private void performSearch(String searchString) {
        SearchFragment searchFragment = new SearchFragment();
        Bundle bundle = new Bundle();
        bundle.putString("search_string", searchString);
        searchFragment.setArguments(bundle);
        loadFragment(searchFragment);
    }
    //Player set up
    private ExoPlayer player;
    Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;
    private ArrayList<Song> currentSongArrayList;
    private UserInfo currentUserInfo;
    private void setVisibleLayoutNowPlaying(boolean visible) {
        if (visible) {
            if (activityMainBinding.layoutNowPlaying.getVisibility() == View.GONE) {
                activityMainBinding.layoutNowPlaying.setVisibility(View.VISIBLE);
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
                player.seekTo(0, C.TIME_UNSET); // Nhảy đến bài hát mới nhất
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
    private void updateNowPlayingUI(Song song) {
        if(song.getType().equals("system")){
            if(currentlyPlayingSongDialogLayoutBinding != null) {
                currentlyPlayingSongDialogLayoutBinding.buttonLike.setEnabled(false);
            }
            // Cập nhật hình ảnh bài hát
            String imagePath = "images/" + song.getImageFileName();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(MainActivity.this).load(uri).into(activityMainBinding.imageViewNowPlaying);
                if(currentlyPlayingSongDialogLayoutBinding != null){
                    Glide.with(MainActivity.this).load(uri).into(currentlyPlayingSongDialogLayoutBinding.imageViewSong);
                    Glide.with(MainActivity.this).load(uri).into(currentlyPlayingSongDialogLayoutBinding.imageViewBackGround);
                }
            }).addOnFailureListener(exception -> {
                // Handle any errors
            });

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
            if (currentlyPlayingSongDialogLayoutBinding != null) {
                currentlyPlayingSongDialogLayoutBinding.stopButtonPlayback.setImageResource(R.drawable.pause_button);
            }
        } else {
            activityMainBinding.stopButtonPlayback.setImageResource(R.drawable.play);
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

                        // Gửi thông điệp cập nhật sau mỗi khoảng thời gian nhất định (ví dụ: 1 giây)
                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.post(updateSeekBarRunnable);
        }
    }
    private void play() {
        player.play();
        // Bắt đầu cập nhật seekBar và textViewDuration nếu media đang phát
        if (player.isPlaying()) {
            updateSeekBar();
        }
    }
    private void stop() {
        player.stop();
        // Gỡ bỏ việc cập nhật seekBar và textViewDuration
        handler.removeCallbacksAndMessages(null);
        handler.removeCallbacks(updateSeekBarRunnable);
    }
    private void pause() {
        player.pause();
        // Gỡ bỏ việc cập nhật seekBar và textViewDuration
        handler.removeCallbacks(updateSeekBarRunnable);
    }
    private void clearPlaylist(){
        stop();
        player.clearMediaItems();
        player.release();
        player = null; // Set player to null to ensure it will be reinitialized later
        currentSongArrayList.clear();
        setVisibleLayoutNowPlaying(false);
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

    private void hideToolbar(){
        activityMainBinding.toolbar.setVisibility(View.GONE);
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
}