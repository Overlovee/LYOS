package com.example.lyos;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.Models.Song;
import com.example.lyos.databinding.FragmentSeeAllBinding;
import com.example.lyos.databinding.TracksOptionsBottomSheetDialogLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SeeAllFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SeeAllFragment extends Fragment {

    private FragmentSeeAllBinding fragmentSeeAllBinding;
    private String userID;
    private ArrayList<String> likeArrayList;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SeeAllFragment() {
        // Required empty public constructor
        this.userID = "";
        this.likeArrayList = null;
    }
    public SeeAllFragment(String userID) {
        // Required empty public constructor
        this.userID = userID;
        this.likeArrayList = null;
    }
    public SeeAllFragment(ArrayList<String> likeArrayList) {
        // Required empty public constructor
        this.userID = "";
        this.likeArrayList = likeArrayList;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SeeAllFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SeeAllFragment newInstance(String param1, String param2) {
        SeeAllFragment fragment = new SeeAllFragment();
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
        fragmentSeeAllBinding = FragmentSeeAllBinding.inflate(getLayoutInflater());
        return fragmentSeeAllBinding.getRoot();
    }

    private ArrayList<Song> arrayList;
    SongRecycleViewAdapter adapter;
    Context context;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        arrayList = new ArrayList<>();
        if(!userID.isEmpty()){
            fragmentSeeAllBinding.textViewHeader.setText("Tracks");
            getUserTracksDataFromFirestore(userID);
        } else if (likeArrayList != null) {
            if (!likeArrayList.isEmpty()){
                fragmentSeeAllBinding.textViewHeader.setText("Likes");
                getLikesDataFromFirestore(likeArrayList);
            }
            else {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
        else {
            getActivity().getSupportFragmentManager().popBackStack();
        }
        addEvents();
    }
    private void getLikesDataFromFirestore(ArrayList<String> listID) {
        SongHandler handler = new SongHandler();
        handler.getDataByListID(listID).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
                if (task.isSuccessful()) {
                    arrayList = task.getResult();
                    if(!arrayList.isEmpty()){
                        fragmentSeeAllBinding.textViewZero.setVisibility(View.GONE);
                        fragmentSeeAllBinding.recycleViewItems.setVisibility(View.VISIBLE);
                        adapter = new SongRecycleViewAdapter(context, arrayList);
                        fragmentSeeAllBinding.recycleViewItems.setAdapter(adapter);
                        fragmentSeeAllBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                        RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                        fragmentSeeAllBinding.recycleViewItems.setLayoutManager(mLayoutManager);
                        fragmentSeeAllBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());
                    }
                    else {
                        fragmentSeeAllBinding.textViewZero.setVisibility(View.VISIBLE);
                        fragmentSeeAllBinding.recycleViewItems.setVisibility(View.GONE);
                    }
                } else {
                    //and more action --.--
                    fragmentSeeAllBinding.textViewZero.setVisibility(View.VISIBLE);
                    fragmentSeeAllBinding.recycleViewItems.setVisibility(View.GONE);
                }
            }
        });
    }
    private void getUserTracksDataFromFirestore(String id) {
        SongHandler handler = new SongHandler();
        handler.searchByUserID(id).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
                if (task.isSuccessful()) {
                    arrayList = task.getResult();
                    if(!arrayList.isEmpty()){
                        fragmentSeeAllBinding.textViewZero.setVisibility(View.GONE);
                        fragmentSeeAllBinding.recycleViewItems.setVisibility(View.VISIBLE);
                        adapter = new SongRecycleViewAdapter(context, arrayList);
                        fragmentSeeAllBinding.recycleViewItems.setAdapter(adapter);
                        fragmentSeeAllBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                        RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                        fragmentSeeAllBinding.recycleViewItems.setLayoutManager(mLayoutManager);
                        fragmentSeeAllBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());
                    } else {
                        fragmentSeeAllBinding.textViewZero.setVisibility(View.VISIBLE);
                        fragmentSeeAllBinding.recycleViewItems.setVisibility(View.GONE);
                    }
                } else {
                    //and more action --.--
                    fragmentSeeAllBinding.textViewZero.setVisibility(View.VISIBLE);
                    fragmentSeeAllBinding.recycleViewItems.setVisibility(View.GONE);
                }
            }
        });
    }
    private void addEvents(){
        fragmentSeeAllBinding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        fragmentSeeAllBinding.buttonMoreAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }
    private TracksOptionsBottomSheetDialogLayoutBinding dialogLayoutBinding;
    private void showDialog() {
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogLayoutBinding = TracksOptionsBottomSheetDialogLayoutBinding.inflate(inflater);

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogLayoutBinding.getRoot());

        dialogLayoutBinding.layoutPlayThisList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.playNewPlaylist(arrayList);
                    dialog.dismiss();
                }
            }
        });
        dialogLayoutBinding.layoutAddToNextUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem context có phải là instance của MainActivity hay không
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.addToNextUp(arrayList);
                    dialog.dismiss();
                }
            }
        });
        dialogLayoutBinding.layoutAddToPlaylist.setOnClickListener(new View.OnClickListener() {
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
}