package com.example.lyos;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lyos.CustomAdapters.AlbumRecycleViewAdapter;
import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.CustomAdapters.TracksSelectionRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.AlbumHandler;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.Interfaces.OnSelectionChangedListener;
import com.example.lyos.Models.Album;
import com.example.lyos.Models.ProfileDataLoader;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.databinding.AlbumAddingDialogLayoutBinding;
import com.example.lyos.databinding.FragmentAlbumsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlbumsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlbumsFragment extends Fragment{
    private FragmentAlbumsBinding fragmentAlbumsBinding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AlbumsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AlbumsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlbumsFragment newInstance(String param1, String param2) {
        AlbumsFragment fragment = new AlbumsFragment();
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
        fragmentAlbumsBinding = FragmentAlbumsBinding.inflate(getLayoutInflater());
        return fragmentAlbumsBinding.getRoot();
    }
    private ArrayList<Song> tracksArrayList = new ArrayList<>();
    private ArrayList<Album> albumArrayList = new ArrayList<>();
    SongRecycleViewAdapter trackAdapter;
    AlbumRecycleViewAdapter albumAdapter;
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
                    getAlbumsDataFromFirestore();
                    addEvents();

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
    private void getAlbumsDataFromFirestore() {
        AlbumHandler handler = new AlbumHandler();
        handler.searchByUserID(user.getId()).addOnCompleteListener(new OnCompleteListener<ArrayList<Album>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Album>> task) {
                if (task.isSuccessful()) {
                    albumArrayList = task.getResult();
                    if(albumArrayList != null){
                        if(albumArrayList.size() > 0){
                            albumAdapter = new AlbumRecycleViewAdapter(context, albumArrayList);
                            fragmentAlbumsBinding.textViewZero.setVisibility(View.GONE);
                            fragmentAlbumsBinding.recycleViewItems.setVisibility(View.VISIBLE);
                            fragmentAlbumsBinding.recycleViewItems.setAdapter(albumAdapter);
                            fragmentAlbumsBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                            RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                            fragmentAlbumsBinding.recycleViewItems.setLayoutManager(mLayoutManager);
                            fragmentAlbumsBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());
                        }
                        else {
                            fragmentAlbumsBinding.textViewZero.setVisibility(View.VISIBLE);
                            fragmentAlbumsBinding.recycleViewItems.setVisibility(View.GONE);
                        }
                    }
                    else {
                        fragmentAlbumsBinding.textViewZero.setVisibility(View.VISIBLE);
                        fragmentAlbumsBinding.recycleViewItems.setVisibility(View.GONE);
                    }
                } else {
                    //and more action --.--
                    fragmentAlbumsBinding.textViewZero.setVisibility(View.VISIBLE);
                    fragmentAlbumsBinding.recycleViewItems.setVisibility(View.GONE);
                }
            }
        });
    }
    private void addEvents(){
        fragmentAlbumsBinding.layoutCreateNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlaylistAddingDialog();
            }
        });
    }
    private AlbumAddingDialogLayoutBinding dialogAlbumAddindBinding;
    private ArrayList<Pair<Song, Boolean>> songSelectionArrayList;
    private ArrayList<Song> songArrayList;
    private TracksSelectionRecycleViewAdapter tracksSelectionRecycleViewAdapter;
    private void showPlaylistAddingDialog() {
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogAlbumAddindBinding = AlbumAddingDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogAlbumAddindBinding.getRoot());

        dialogAlbumAddindBinding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        dialogAlbumAddindBinding.textViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = dialogAlbumAddindBinding.editTextTitle.getText().toString();
                String description = dialogAlbumAddindBinding.editTextDescription.getText().toString();
                // Tạo danh sách mới để chứa các bài hát đã chọn
                ArrayList<String> selectedSongs = new ArrayList<>();

                // Lặp qua danh sách songSelectionArrayList để lấy các bài hát đã chọn
                for (Pair<Song, Boolean> pair : songSelectionArrayList) {
                    if (pair.second) { // Kiểm tra xem bài hát có được chọn không
                        selectedSongs.add(pair.first.getId());
                    }
                }

                // Kiểm tra xem đã nhập đủ thông tin và có bài hát nào được chọn không
                if (!title.isEmpty() && selectedImageUri != null && !selectedSongs.isEmpty()) {
                    Album newAlbum = new Album();
                    newAlbum.setTitle(title);
                    newAlbum.setDescription(description);
                    newAlbum.setUserID(user.getId());
                    newAlbum.setSongList(selectedSongs);
                    uploadNewAlbum(newAlbum);
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Please type all fields and select at least one song", Toast.LENGTH_LONG).show();
                }
            }
        });

        SongHandler handler = new SongHandler();
        songSelectionArrayList = new ArrayList<>();
        songArrayList = new ArrayList<>();
        handler.getSongsNotInAnyAlbum(user.getId()).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
                if (task.isSuccessful()) {
                    songArrayList = task.getResult();
                    if(!songArrayList.isEmpty()){
                        for (Song song: songArrayList
                             ) {
                            songSelectionArrayList.add(new Pair<>(song, false));
                        }
                        dialogAlbumAddindBinding.textViewNoTracks.setVisibility(View.GONE);
                        dialogAlbumAddindBinding.recycleViewTrackItems.setVisibility(View.VISIBLE);

                        tracksSelectionRecycleViewAdapter = new TracksSelectionRecycleViewAdapter(context, songSelectionArrayList);
                        tracksSelectionRecycleViewAdapter.setOnSelectionChangedListener(new OnSelectionChangedListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onSelectionChanged(int position, boolean isSelected) {
                                Pair<Song, Boolean> pair = songSelectionArrayList.get(position);
                                pair = new Pair<>(pair.first, isSelected);
                                songSelectionArrayList.set(position, pair);
                                tracksSelectionRecycleViewAdapter.notifyDataSetChanged();
                            }
                        });
                        dialogAlbumAddindBinding.recycleViewTrackItems.setAdapter(tracksSelectionRecycleViewAdapter);
                        dialogAlbumAddindBinding.recycleViewTrackItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                        RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                        dialogAlbumAddindBinding.recycleViewTrackItems.setLayoutManager(mLayoutManager);
                        dialogAlbumAddindBinding.recycleViewTrackItems.setItemAnimator(new DefaultItemAnimator());
                    } else {
                        dialogAlbumAddindBinding.textViewNoTracks.setVisibility(View.VISIBLE);
                        dialogAlbumAddindBinding.recycleViewTrackItems.setVisibility(View.GONE);
                    }
                } else {
                    //and more action --.--
                    dialogAlbumAddindBinding.textViewNoTracks.setVisibility(View.VISIBLE);
                    dialogAlbumAddindBinding.recycleViewTrackItems.setVisibility(View.GONE);
                }
            }
        });

        dialogAlbumAddindBinding.textViewCancel.setOnClickListener(new View.OnClickListener() {
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
    private void uploadNewAlbum(Album item){
        // Nếu có URI của hình ảnh đã chọn
        if (selectedImageUri != null) {
            // Lấy phần mở rộng của file từ URI của hình ảnh được chọn
            String fileExtension = getFileExtension(selectedImageUri);
            if (fileExtension == null) {
                Toast.makeText(context, "Unsupported file type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo tên mới cho hình ảnh
            String imageFileName = user.getId() + System.currentTimeMillis() + "." + fileExtension;
            // Tạo reference đến nơi lưu trữ trên Firebase Storage cho hình ảnh
            StorageReference imageStorageRef = FirebaseStorage.getInstance().getReference().child("album_images").child(imageFileName);

            // Upload hình ảnh lên Firebase Storage
            imageStorageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            item.setImageFileName(imageFileName);
                            // Thêm mục Song vào Firebase Database bằng cách sử dụng SongHandler
                            AlbumHandler albumHandler = new AlbumHandler();
                            albumHandler.add(item);
                            getAlbumsDataFromFirestore();
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
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private static final int REQUEST_IMAGE_PICK = 1;
    private Uri selectedImageUri;
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == getActivity().RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if(selectedImageUri != null
                && dialogAlbumAddindBinding != null
            ){
                Glide.with(context).load(selectedImageUri).into(dialogAlbumAddindBinding.imageView);
            }
        }
    }
}