package com.example.lyos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lyos.CustomAdapters.AlbumRecycleViewAdapter;
import com.example.lyos.CustomAdapters.PlaylistRecycleViewAdapter;
import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.AlbumHandler;
import com.example.lyos.FirebaseHandlers.PlaylistHandler;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.Models.Album;
import com.example.lyos.Models.Playlist;
import com.example.lyos.Models.ProfileDataLoader;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.databinding.FragmentProfileBinding;
import com.example.lyos.databinding.GetAudioDialogLayoutBinding;
import com.example.lyos.databinding.ProfileEditingDialogLayoutBinding;
import com.example.lyos.databinding.ProfileOptionsDialogLayoutBinding;
import com.example.lyos.databinding.SongAddingDialogLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding fragmentProfileBinding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentProfileBinding = FragmentProfileBinding.inflate(getLayoutInflater());
        return fragmentProfileBinding.getRoot();
    }

    private ArrayList<Song> likesArrayList = new ArrayList<>();
    private ArrayList<Song> tracksArrayList = new ArrayList<>();
    private ArrayList<Album> albumArrayList = new ArrayList<>();
    private ArrayList<Playlist> playlistArrayList = new ArrayList<>();
    SongRecycleViewAdapter likeAdapter;
    SongRecycleViewAdapter trackAdapter;
    AlbumRecycleViewAdapter albumAdapter;
    PlaylistRecycleViewAdapter playlistAdapter;
    private UserInfo user;
    Context context;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();

        // Kiểm tra xem có dữ liệu tài khoản đã được lưu trữ hay không
        ProfileDataLoader.loadProfileData(requireContext(), new ProfileDataLoader.OnProfileDataLoadedListener() {
            @Override
            public void onProfileDataLoaded(UserInfo u) {
                user = u;
                if (user != null){
                    setUpUI();
                    addEvents();
                    if(user.getLikes().size() > 0){
                        fragmentProfileBinding.textViewZeroLikes.setVisibility(View.GONE);
                        fragmentProfileBinding.recycleViewLikeItems.setVisibility(View.VISIBLE);
                        fragmentProfileBinding.textViewSeeAllLikesAction.setVisibility(View.VISIBLE);
                        getLikesDataFromFirestore();
                    } else {
                        fragmentProfileBinding.textViewZeroLikes.setVisibility(View.VISIBLE);
                        fragmentProfileBinding.recycleViewLikeItems.setVisibility(View.GONE);
                        fragmentProfileBinding.textViewSeeAllLikesAction.setVisibility(View.GONE);
                    }
                    getUserTracksDataFromFirestore();
                    getAlbumsDataFromFirestore();
                    getPlaylistsDataFromFirestore();
                }
                else {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onProfileDataLoadFailed() {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
    private void setUpUI(){
        fragmentProfileBinding.textViewUserName.setText(user.getUsername());
        fragmentProfileBinding.textViewHeaderUserName.setText(user.getUsername());
        if(user.getFollowers() == null){
            fragmentProfileBinding.textViewFollowers.setText("0 Followers");
        }
        else {
            fragmentProfileBinding.textViewFollowers.setText(String.valueOf(user.getFollowers().size()) + " Followers");
        }
        if(user.getFollowing() == null){
            fragmentProfileBinding.textViewFollowing.setText("0 Following");
        }
        else {
            fragmentProfileBinding.textViewFollowing.setText(String.valueOf(user.getFollowing().size()) + " Following");
        }

        String imagePath = "user_images/" + user.getProfilePhoto();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context).load(uri).into(fragmentProfileBinding.imageViewProfile);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });

        imagePath = "user_banner_images/" + user.getProfileBanner();
        storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context).load(uri).into(fragmentProfileBinding.imageViewBanner);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });
    }
    private void getLikesDataFromFirestore() {
        SongHandler handler = new SongHandler();
        handler.getDataByListID(user.getLikes(), 6).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
                if (task.isSuccessful()) {
                    likesArrayList = task.getResult();
                    likeAdapter = new SongRecycleViewAdapter(context, likesArrayList);

                    fragmentProfileBinding.textViewZeroLikes.setVisibility(View.GONE);
                    fragmentProfileBinding.recycleViewLikeItems.setVisibility(View.VISIBLE);
                    fragmentProfileBinding.textViewSeeAllLikesAction.setVisibility(View.VISIBLE);

                    fragmentProfileBinding.recycleViewLikeItems.setAdapter(likeAdapter);
                    fragmentProfileBinding.recycleViewLikeItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                    fragmentProfileBinding.recycleViewLikeItems.setLayoutManager(mLayoutManager);
                    fragmentProfileBinding.recycleViewLikeItems.setItemAnimator(new DefaultItemAnimator());

                } else {
                    //and more action --.--
                    fragmentProfileBinding.textViewZeroLikes.setVisibility(View.VISIBLE);
                    fragmentProfileBinding.recycleViewLikeItems.setVisibility(View.GONE);
                    fragmentProfileBinding.textViewSeeAllLikesAction.setVisibility(View.GONE);
                }
            }
        });
    }
    private void getAlbumsDataFromFirestore() {
        AlbumHandler handler = new AlbumHandler();
        handler.searchByUserID(user.getId(), 6).addOnCompleteListener(new OnCompleteListener<ArrayList<Album>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Album>> task) {
                if (task.isSuccessful()) {
                    albumArrayList = task.getResult();
                    albumAdapter = new AlbumRecycleViewAdapter(context, albumArrayList);
                    fragmentProfileBinding.layoutAlbums.setVisibility(View.VISIBLE);
                    fragmentProfileBinding.recycleViewAlbumItems.setAdapter(albumAdapter);
                    fragmentProfileBinding.recycleViewAlbumItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                    fragmentProfileBinding.recycleViewAlbumItems.setLayoutManager(mLayoutManager);
                    fragmentProfileBinding.recycleViewAlbumItems.setItemAnimator(new DefaultItemAnimator());

                } else {
                    //and more action --.--
                    fragmentProfileBinding.layoutAlbums.setVisibility(View.GONE);
                }
            }
        });
    }
    private void getPlaylistsDataFromFirestore() {
        PlaylistHandler handler = new PlaylistHandler();
        handler.searchByUserID(user.getId(), 6).addOnCompleteListener(new OnCompleteListener<ArrayList<Playlist>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Playlist>> task) {
                if (task.isSuccessful()) {
                    playlistArrayList = task.getResult();
                    playlistAdapter = new PlaylistRecycleViewAdapter(context, playlistArrayList);

                    fragmentProfileBinding.textViewZeroPlaylists.setVisibility(View.GONE);
                    fragmentProfileBinding.recycleViewPlaylistItems.setVisibility(View.VISIBLE);

                    fragmentProfileBinding.recycleViewPlaylistItems.setAdapter(playlistAdapter);
                    fragmentProfileBinding.recycleViewPlaylistItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                    fragmentProfileBinding.recycleViewPlaylistItems.setLayoutManager(mLayoutManager);
                    fragmentProfileBinding.recycleViewPlaylistItems.setItemAnimator(new DefaultItemAnimator());

                } else {
                    //and more action --.--
                    fragmentProfileBinding.textViewZeroPlaylists.setVisibility(View.VISIBLE);
                    fragmentProfileBinding.recycleViewPlaylistItems.setVisibility(View.GONE);
                }
            }
        });
    }
    private void getUserTracksDataFromFirestore() {
        SongHandler handler = new SongHandler();
        handler.searchByUserID(user.getId(), 6).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
                if (task.isSuccessful()) {
                    tracksArrayList = task.getResult();
                    if(!tracksArrayList.isEmpty()){
                        fragmentProfileBinding.layoutTracks.setVisibility(View.VISIBLE);
                        trackAdapter = new SongRecycleViewAdapter(context, tracksArrayList);
                        fragmentProfileBinding.recycleViewTrackItems.setAdapter(trackAdapter);
                        fragmentProfileBinding.recycleViewTrackItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                        RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                        fragmentProfileBinding.recycleViewTrackItems.setLayoutManager(mLayoutManager);
                        fragmentProfileBinding.recycleViewTrackItems.setItemAnimator(new DefaultItemAnimator());
                    } else {
                        fragmentProfileBinding.layoutTracks.setVisibility(View.GONE);
                    }
                } else {
                    //and more action --.--
                    fragmentProfileBinding.layoutTracks.setVisibility(View.GONE);
                }
            }
        });
    }
    private void addEvents(){
        fragmentProfileBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        fragmentProfileBinding.buttonMoreAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileOptionsDialog(user);
            }
        });
        fragmentProfileBinding.textViewSeeAllTracksAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.openSeeAllFragment(user.getId());
                }
            }
        });
        fragmentProfileBinding.textViewSeeAllLikesAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.openSeeAllFragment(user.getLikes());
                }
            }
        });
    }

    private void showProfileOptionsDialog(UserInfo item) {
        ProfileOptionsDialogLayoutBinding dialogLayoutBinding;
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogLayoutBinding = ProfileOptionsDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogLayoutBinding.getRoot());
        dialogLayoutBinding.layoutEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileEditingDialog(item);
            }
        });
        dialogLayoutBinding.layoutAddNewTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSongAddingDialog(item);
            }
        });
        dialogLayoutBinding.layoutSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.signOut();
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
    ProfileEditingDialogLayoutBinding profileEditingDialogLayoutBinding;
    private void showProfileEditingDialog(UserInfo item) {

        LayoutInflater inflater = LayoutInflater.from(context);
        profileEditingDialogLayoutBinding = ProfileEditingDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(profileEditingDialogLayoutBinding.getRoot());

        profileEditingDialogLayoutBinding.editTextUserName.setText(item.getUsername());
        profileEditingDialogLayoutBinding.textViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        profileEditingDialogLayoutBinding.textViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newUserName = profileEditingDialogLayoutBinding.editTextUserName.getText().toString();
                updateNewInfo();
                dialog.dismiss();
            }
        });
        profileEditingDialogLayoutBinding.imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeOutput = 1;
                chooseImage();
            }
        });
        profileEditingDialogLayoutBinding.imageViewBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeOutput = 2;
                chooseImage();
            }
        });
        profileEditingDialogLayoutBinding.layoutImageViewBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeOutput = 2;
                chooseImage();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
    SongAddingDialogLayoutBinding songAddingDialogLayoutBinding;
    private void showSongAddingDialog(UserInfo item) {
        LayoutInflater inflater = LayoutInflater.from(context);
        songAddingDialogLayoutBinding = SongAddingDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(songAddingDialogLayoutBinding.getRoot());


        songAddingDialogLayoutBinding.textViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        songAddingDialogLayoutBinding.textViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = songAddingDialogLayoutBinding.editTextTitle.getText().toString();
                String description = songAddingDialogLayoutBinding.editTextDescription.getText().toString();
                String tag = songAddingDialogLayoutBinding.editTextTag.getText().toString();
                if(!title.isEmpty() && !description.isEmpty()
                        && !tag.isEmpty() && selectedMp3Uri != null
                        && selectedSongImageUri != null){
                    Song song = new Song();
                    song.setUserID(user.getId());
                    song.setTitle(songAddingDialogLayoutBinding.editTextTitle.getText().toString());
                    song.setTag(songAddingDialogLayoutBinding.editTextTag.getText().toString());
                    song.setDescription(songAddingDialogLayoutBinding.editTextDescription.getText().toString());
                    uploadNewTrack(song);
                    dialog.dismiss();
                }
                else {
                    Toast.makeText(context, "Please type all fields", Toast.LENGTH_LONG);
                }
            }
        });
        songAddingDialogLayoutBinding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeOutput = 0;
                chooseImage();
            }
        });
        songAddingDialogLayoutBinding.imageViewMp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
    GetAudioDialogLayoutBinding getAudioDialogLayoutBinding;
    private void showGetAudioDialog(ArrayList<String> mp3Files) {
        LayoutInflater inflater = LayoutInflater.from(context);
        getAudioDialogLayoutBinding = GetAudioDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(getAudioDialogLayoutBinding.getRoot());

        if(mp3Files != null){
            if(mp3Files.size() != 0){
                getAudioDialogLayoutBinding.textViewError.setVisibility(View.GONE);
                getAudioDialogLayoutBinding.recycleViewItems.setVisibility(View.VISIBLE);

                AudioRecycleViewAdapter audioRecycleViewAdapter = new AudioRecycleViewAdapter(context, mp3Files, dialog);
                getAudioDialogLayoutBinding.recycleViewItems.setAdapter(audioRecycleViewAdapter);

                getAudioDialogLayoutBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
                getAudioDialogLayoutBinding.recycleViewItems.setLayoutManager(layoutManager);
                getAudioDialogLayoutBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());
            }
            else {
                getAudioDialogLayoutBinding.textViewError.setVisibility(View.VISIBLE);
                getAudioDialogLayoutBinding.recycleViewItems.setVisibility(View.GONE);
            }
        }
        else {
            getAudioDialogLayoutBinding.textViewError.setVisibility(View.VISIBLE);
            getAudioDialogLayoutBinding.recycleViewItems.setVisibility(View.GONE);
        }

        getAudioDialogLayoutBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        getAudioDialogLayoutBinding.buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
    private class AudioRecycleViewAdapter extends RecyclerView.Adapter<AudioRecycleViewAdapter.MyViewHolder>{
        private Context context;
        private ArrayList<String> list;
        private Dialog dialog;
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView textViewTitle;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewTitle = itemView.findViewById(R.id.textViewTitle);
            }
        }

        public AudioRecycleViewAdapter(Context context, ArrayList<String> list, Dialog dialog) {
            this.context = context;
            this.list = list;
            this.dialog = dialog;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_audio_recycle_view, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
            String item = list.get(position);
            holder.textViewTitle.setText(item);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Tạo URI từ tên tệp MP3
                    selectedMp3Uri = mp3UrisArrayList.get(position);
                    if (selectedMp3Uri != null) {
                        songAddingDialogLayoutBinding.textViewTextMP3FileName.setText(item);
                        dialog.dismiss();
                    } else {
                        songAddingDialogLayoutBinding.textViewTextMP3FileName.setText("Can not get the mp3 file!");
                    }
                }
            });
        }
        @Override
        public int getItemCount() {
            return list.size();
        }
    }
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_MP3_PICK = 2;
    private Uri selectedImageUri;
    private Uri selectedProfileImageUri;
    private Uri selectedBannerImageUri;
    private Uri selectedSongImageUri;
    private Uri selectedMp3Uri;
    private String newUserName = "";
    private int typeOutput = -1;
    private ArrayList<Uri> mp3UrisArrayList;
    private void chooseImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // Kiểm tra xem thiết bị có ứng dụng để xử lý Intent này không
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            startActivityForResult(Intent.createChooser(intent, "Select image"), REQUEST_IMAGE_PICK);
        } else {
            // Nếu không có ứng dụng nào để xử lý Intent, thông báo cho người dùng
            Toast.makeText(context, "No application support", Toast.LENGTH_SHORT).show();
        }
    }
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CODE_MANAGE_ALL_FILES_ACCESS_PERMISSION = 2;

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_CODE_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            } else {
                // Quyền đã được cấp, bạn có thể thực hiện hành động cần thiết
                chooseMp3File();
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE);
            } else {
                // Quyền đã được cấp, bạn có thể thực hiện hành động cần thiết
                chooseMp3File();
            }
        }
    }
    private void chooseMp3File() {
        // Tạo danh sách để lưu trữ các Uri và tên file mp3
        mp3UrisArrayList = new ArrayList<>();
        ArrayList<String> mp3FileNames = new ArrayList<>();

        // Sử dụng ContentResolver để truy xuất dữ liệu từ bộ nhớ ngoài của thiết bị
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        // Kiểm tra và thêm các Uri và tên file mp3 vào danh sách
        if (cursor != null && cursor.moveToFirst()) {
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);

            do {
                long id = cursor.getLong(idColumn);
                String title = cursor.getString(titleColumn);
                Uri contentUri = ContentUris.withAppendedId(uri, id);

                mp3UrisArrayList.add(contentUri);
                mp3FileNames.add(title);
            } while (cursor.moveToNext());
            cursor.close();
        }

        showGetAudioDialog(mp3FileNames);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, bạn có thể thực hiện hành động cần thiết
                chooseMp3File();
            } else {
                // Quyền bị từ chối, hiển thị thông báo hoặc hành động khác
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == getActivity().RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if(typeOutput == 0){
                updateDialog(songAddingDialogLayoutBinding);
            }
            else {
                updateDialog(profileEditingDialogLayoutBinding);
            }
        }
        if (requestCode == REQUEST_CODE_MANAGE_ALL_FILES_ACCESS_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Quyền đã được cấp, bạn có thể thực hiện hành động cần thiết

//                    if (requestCode == REQUEST_MP3_PICK && resultCode == getActivity().RESULT_OK && data != null) {
//                        selectedMp3Uri = data.getData();
//                        if (selectedMp3Uri != null) {
//                            songAddingDialogLayoutBinding.textViewTextMP3FileName.setText("Upload mp3 successfully");
//                        }
//                    }
                    chooseMp3File();
                } else {
                    // Quyền bị từ chối, hiển thị thông báo hoặc hành động khác
                }
            }
        }
    }
    private void updateDialog(ProfileEditingDialogLayoutBinding dialogLayoutBinding){
        if (typeOutput == 1){
            // Update the profile picture with the selected image URI
            selectedProfileImageUri = selectedImageUri;
            if(selectedProfileImageUri != null){
                dialogLayoutBinding.imageViewProfile.setImageURI(selectedProfileImageUri);
            }
        } else if (typeOutput == 2){
            // Update the banner picture with the selected image URI
            selectedBannerImageUri = selectedImageUri;
            if(selectedBannerImageUri != null){
                dialogLayoutBinding.imageViewBanner.setImageURI(selectedBannerImageUri);
            }
        }
    }
    private void updateDialog(SongAddingDialogLayoutBinding songAddingDialogLayoutBinding){
        if (typeOutput == 0){
            // Update the profile picture with the selected image URI
            selectedSongImageUri = selectedImageUri;
            if(selectedSongImageUri != null){
                songAddingDialogLayoutBinding.imageView.setImageURI(selectedSongImageUri);
            }
        }
    }
    private void updateNewInfo(){
        if(selectedProfileImageUri != null){
            uploadProfileImageToFirebase();
        }
        if(selectedBannerImageUri != null){
            uploadBannerImageToFirebase();
        }
        if(newUserName != "" && !newUserName.equals(user.getUsername())){
            UserHandler userHandler = new UserHandler();
            userHandler.updateUserName(user.getId(), newUserName).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        user.setUsername(newUserName);
                        setUpUI();
                    }
                }
            });
        }

    }
    private void uploadNewTrack(Song song){
        // Nếu có URI của file MP3 đã chọn
        if (selectedMp3Uri != null) {
            // Lấy phần mở rộng của file từ URI của hình ảnh được chọn
            String fileExtension = getFileExtension(selectedMp3Uri);
            if (fileExtension == null) {
                Toast.makeText(context, "Unsupported file type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo tên mới cho hình ảnh mới
            String mp3FileName = user.getId() + System.currentTimeMillis() + "." + fileExtension;
            // Tạo reference đến nơi lưu trữ trên Firebase Storage
            StorageReference mp3StorageRef = FirebaseStorage.getInstance().getReference().child("mp3s").child(mp3FileName);

            // Upload file MP3 lên Firebase Storage
            mp3StorageRef.putFile(selectedMp3Uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Lấy thời lượng của file MP3
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(context, selectedMp3Uri);
                            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            long durationInMillis = Long.parseLong(durationStr);
                            try {
                                retriever.release();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            // Chuyển thời lượng thành giây
                            int durationInSeconds = (int) (durationInMillis / 1000);
                            song.setMp3FileName(mp3FileName);
                            song.setDuration(durationInSeconds);

                            // Khi upload MP3 thành công, tiến hành upload hình ảnh
                            uploadSongImage(song);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Upload file thất bại
                            Toast.makeText(context, "Failed to upload MP3 file", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Nếu không có file MP3 được chọn
            Toast.makeText(context, "No MP3 file selected", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadSongImage(Song song) {
        // Nếu có URI của hình ảnh đã chọn
        if (selectedSongImageUri != null) {
            // Lấy phần mở rộng của file từ URI của hình ảnh được chọn
            String fileExtension = getFileExtension(selectedSongImageUri);
            if (fileExtension == null) {
                Toast.makeText(context, "Unsupported file type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo tên mới cho hình ảnh
            String imageFileName = user.getId() + System.currentTimeMillis() + "." + fileExtension;
            // Tạo reference đến nơi lưu trữ trên Firebase Storage cho hình ảnh
            StorageReference imageStorageRef = FirebaseStorage.getInstance().getReference().child("images").child(imageFileName);

            // Upload hình ảnh lên Firebase Storage
            imageStorageRef.putFile(selectedSongImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            song.setImageFileName(imageFileName);
                            // Thêm mục Song vào Firebase Database bằng cách sử dụng SongHandler
                            SongHandler songHandler = new SongHandler();
                            songHandler.addSong(song)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Thêm thành công
                                            Toast.makeText(context, "Song added successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Thêm thất bại
                                            Toast.makeText(context, "Failed to add song", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Upload hình ảnh thất bại
                            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Nếu không có hình ảnh được chọn
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadProfileImageToFirebase() {
        if (selectedProfileImageUri != null) {
            // Lấy phần mở rộng của file từ URI của hình ảnh được chọn
            String fileExtension = getFileExtension(selectedProfileImageUri);
            if (fileExtension == null) {
                Toast.makeText(context, "Unsupported file type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo tên mới cho hình ảnh mới
            String newImageName = user.getId() + System.currentTimeMillis() + "." + fileExtension;
            StorageReference newImageRef = FirebaseStorage.getInstance().getReference().child("user_images/" + newImageName);

            // Lấy tên của ảnh hiện tại để xóa sau này
            String currentImageName = user.getProfilePhoto();
            // Tải lên hình ảnh mới
            newImageRef.putFile(selectedProfileImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Cập nhật URL của ảnh mới cho người dùng
                            user.setProfilePhoto(newImageName);
                            setUpUI();
                            UserHandler userHandler = new UserHandler();
                            userHandler.updateProfilePhoto(user.getId(), newImageName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Xóa ảnh cũ
                                        if (!currentImageName.isEmpty()) {
                                            StorageReference oldImageRef = FirebaseStorage.getInstance().getReference().child("user_images/" + currentImageName);
                                            oldImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Xóa thành công
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    // Xóa thất bại
                                                }
                                            });
                                        }

                                    } else {
                                        Toast.makeText(context, "Failed to update profile photo", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    private void uploadBannerImageToFirebase() {
        if (selectedBannerImageUri != null) {
            // Lấy phần mở rộng của file từ URI của hình ảnh được chọn
            String fileExtension = getFileExtension(selectedBannerImageUri);
            if (fileExtension == null) {
                Toast.makeText(context, "Unsupported file type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo tên mới cho hình ảnh mới
            String newImageName = user.getId() + System.currentTimeMillis() + "." + fileExtension;
            StorageReference newImageRef = FirebaseStorage.getInstance().getReference().child("user_banner_images/" + newImageName);

            // Lấy tên của ảnh hiện tại để xóa sau này
            String currentImageName = user.getProfileBanner();
            // Tải lên hình ảnh mới
            newImageRef.putFile(selectedBannerImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Cập nhật URL của ảnh mới cho người dùng
                            user.setProfilePhoto(newImageName);
                            setUpUI();
                            UserHandler userHandler = new UserHandler();
                            userHandler.updateProfileBanner(user.getId(), newImageName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Xóa ảnh cũ
                                        if (!currentImageName.isEmpty()) {
                                            StorageReference oldImageRef = FirebaseStorage.getInstance().getReference().child("user_banner_images/" + currentImageName);
                                            oldImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Xóa thành công
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    // Xóa thất bại
                                                }
                                            });
                                        }

                                    } else {
                                        Toast.makeText(context, "Failed to update photo", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}