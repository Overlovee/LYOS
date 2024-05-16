package com.example.lyos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SearchView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.Models.AccountUtils;
import com.example.lyos.Models.Album;
import com.example.lyos.Models.Playlist;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.databinding.ActivityMainBinding;
import com.example.lyos.databinding.CurrentlyPlayingListBottomSheetDialogLayoutBinding;
import com.example.lyos.databinding.CurrentlyPlayingSongDialogLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String ACCOUNT_TYPE = "com.example.lyos.account";
    private ActivityMainBinding activityMainBinding;
    private String account = "Ykd4dpxIKqjqpsAupw2J";
    private UserInfo user;
    public void setAccount(String id){
        this.account = id;
        AccountUtils.saveAccount(MainActivity.this, account);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        activityMainBinding.layoutNowPlaying.setVisibility(View.GONE);

        Intent intent = new Intent(MainActivity.this, SplashActivity.class);
        startActivity(intent);

        //setAccount(account);

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
                }
                else {
                    activityMainBinding.textViewFragmentTitle.setVisibility(View.VISIBLE);
                    activityMainBinding.searchBar.setVisibility(View.GONE);
                    if (id == R.id.navigation_home) {
                        fragment = new HomeFragment();
                    } else if (id == R.id.navigation_albums) {
                        fragment = new AlbumsFragment();
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

    public void loadFragment(Fragment fragment)
    {
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
        activityMainBinding.stopButtonPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getPlayWhenReady()) {
                    // Nếu player đang phát, dừng phát
                    player.pause();
                    updatePlayPauseButton(false);
                } else {
                    // Nếu player đang dừng, bắt đầu phát
                    player.play();
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
//        LinearLayout layoutLike = dialog.findViewById(R.id.layoutLike);

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
    private ArrayList<Song> currentSongArrayList;
    private UserInfo currentUserInfo;
    private void setVisibleLayoutlNowPlaying() {
        if (activityMainBinding.layoutNowPlaying.getVisibility() == View.GONE) {
            activityMainBinding.layoutNowPlaying.setVisibility(View.VISIBLE);
            // Khởi tạo player nếu chưa được khởi tạo
            if (player == null) {
                player = new ExoPlayer.Builder(MainActivity.this).build();
                initializePlayerListener();
            }
            if (currentSongArrayList == null) {
                currentSongArrayList = new ArrayList<>();
            }
        }
    }
    public void setAudioNowPlaying(Song item) {
        // Hiển thị layout Now Playing nếu chưa hiển thị
        setVisibleLayoutlNowPlaying();
        // Thêm bài hát vào danh sách hiện tại
        currentSongArrayList.add(item);
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
            player.prepare();
            player.seekTo(0, C.TIME_UNSET); // Nhảy đến bài hát mới nhất
            player.play();

            // Cập nhật UI với thông tin bài hát hiện tại
            updateNowPlayingUI(item);
        }).addOnFailureListener(exception -> {
            // Xử lý lỗi tải URL
            Toast.makeText(MainActivity.this, "Error: Cannot load mp3!", Toast.LENGTH_LONG).show();
        });
    }
    private void updateNowPlayingUI(Song song) {
        // Cập nhật hình ảnh bài hát
        String imagePath = "images/" + song.getImageFileName();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(MainActivity.this).load(uri).into(activityMainBinding.imageViewNowPlaying);
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
                    } else {
                        activityMainBinding.textViewUserName.setText("Unknown");
                    }
                } else {
                    // Handle any errors
                }
            }
        });
    }
    private void initializePlayerListener() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                updatePlayPauseButton(player.getPlayWhenReady() && state == Player.STATE_READY);
            }
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                if (mediaItem != null) {
                    // Lấy index của bài hát hiện tại
                    int currentIndex = player.getCurrentMediaItemIndex();
                    // Lấy bài hát từ danh sách dựa trên index
                    Song currentSong = currentSongArrayList.get(currentIndex);

                    // Cập nhật UI với thông tin bài hát hiện tại
                    updateNowPlayingUI(currentSong);
                }
            }
        });
    }
    private void updatePlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            activityMainBinding.stopButtonPlayback.setImageResource(R.drawable.pause);
        } else {
            activityMainBinding.stopButtonPlayback.setImageResource(R.drawable.play);
        }
    }
    public void playNewPlaylist(ArrayList<Song> songArrayList) {
        // Initialize the player if it's null
        if (player == null) {
            player = new ExoPlayer.Builder(MainActivity.this).build();
            // Initialize the player listener
            initializePlayerListener();
        }
        else{
            // Clear the current playlist and stop the player
            clearPlaylist();
        }

        // Iterate through the song list and add each song to the player
        for (int i = 0; i < songArrayList.size(); i++) {
            Song song = songArrayList.get(i);
            String mp3Path = "mp3s/" + song.getMp3FileName();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(mp3Path);
            int finalI = i;
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                MediaItem mediaItem = MediaItem.fromUri(uri);
                player.addMediaItem(finalI, mediaItem);

                // If this is the last song in the list, prepare and start the player
                if (finalI == songArrayList.size() - 1) {
                    player.prepare();
                    player.play();
                    setVisibleLayoutlNowPlaying();
                }
            }).addOnFailureListener(exception -> {
                // Temporarily don't do anything --,--
            });

            // Update the UI for the currently playing song
            if (i == 0) {
                updateNowPlayingUI(song);
            }
        }
    }
    public void addToNextUp(Song item) {
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
    public void addToNextUp(ArrayList<Song> songArrayList) {
        // Hiển thị layout Now Playing nếu chưa hiển thị
        setVisibleLayoutlNowPlaying();
        // Lấy vị trí hiện tại của bài hát đang phát
        int currentIndex = player.getCurrentMediaItemIndex();

        // Thêm tất cả các bài hát trong songArrayList vào sau bài hát đang phát trong danh sách hiện tại
        if (currentIndex >= 0 && currentIndex < currentSongArrayList.size()) {
            currentSongArrayList.addAll(currentIndex + 1, songArrayList);
        } else {
            currentSongArrayList.addAll(songArrayList);
        }

        // Thêm tất cả các MediaItem của songArrayList vào danh sách phát của player
        if (player != null) {
            for (Song song : songArrayList) {
                String mp3Path = "mp3s/" + song.getMp3FileName();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(mp3Path);
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Tạo MediaItem từ URL
                    MediaItem mediaItem = MediaItem.fromUri(uri);
                    // Thêm MediaItem vào vị trí tiếp theo của bài hát đang phát
                    player.addMediaItem(currentIndex + 1, mediaItem);
                }).addOnFailureListener(exception -> {
                    // Xử lý lỗi tải URL
                    Toast.makeText(MainActivity.this, "Error: Cannot load mp3!", Toast.LENGTH_LONG).show();
                });
            }
        }
    }

    private void clearPlaylist(){
        player.stop();
        player.clearMediaItems();
        player.release();
    }
    private void hideToolbar(){
        activityMainBinding.toolbar.setVisibility(View.GONE);
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