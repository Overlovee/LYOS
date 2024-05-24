package com.example.lyos;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lyos.CustomAdapters.PlaylistSelectionRecycleViewAdapter;
import com.example.lyos.CustomAdapters.RankedSongRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.PlaylistHandler;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.Models.ColorUtils;
import com.example.lyos.Models.Playlist;
import com.example.lyos.Models.ProfileDataLoader;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.databinding.AddingToPlaylistDialogLayoutBinding;
import com.example.lyos.databinding.FragmentDefaultSearchBinding;
import com.example.lyos.databinding.OtherSongOptionsBottomSheetDialogLayoutBinding;
import com.example.lyos.databinding.PlaylistAddingDialogLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DefaultSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DefaultSearchFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private FragmentDefaultSearchBinding fragmentDefaultSearchBinding;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DefaultSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DefaultSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DefaultSearchFragment newInstance(String param1, String param2) {
        DefaultSearchFragment fragment = new DefaultSearchFragment();
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
        fragmentDefaultSearchBinding = FragmentDefaultSearchBinding.inflate(getLayoutInflater());
        return fragmentDefaultSearchBinding.getRoot();
    }

    private ArrayList<String> tagArrayList;
    Context context;
    private ArrayList<Integer> colors;
    private Random random;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();

        new SongHandler().getAllUniqueTags().addOnCompleteListener(new OnCompleteListener<ArrayList<String>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<String>> task) {
                if (task.isSuccessful()) {
                    tagArrayList = task.getResult();
                    updateUI();
                } else {

                }
            }
        });
    }
    private void updateUI(){
        if(tagArrayList == null){
            fragmentDefaultSearchBinding.textViewNoTags.setVisibility(View.VISIBLE);
            fragmentDefaultSearchBinding.recycleViewItems.setVisibility(View.GONE);
            return;
        }
        if(tagArrayList.size() == 0){
            fragmentDefaultSearchBinding.textViewNoTags.setVisibility(View.VISIBLE);
            fragmentDefaultSearchBinding.recycleViewItems.setVisibility(View.GONE);
            return;
        }
        fragmentDefaultSearchBinding.textViewNoTags.setVisibility(View.GONE);
        fragmentDefaultSearchBinding.recycleViewItems.setVisibility(View.VISIBLE);

        TagRecycleViewAdapter adapter = new TagRecycleViewAdapter(getContext(), tagArrayList);
        fragmentDefaultSearchBinding.recycleViewItems.setAdapter(adapter);
        fragmentDefaultSearchBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
        GridLayoutManager mLayoutManager = new GridLayoutManager(context, 2);
        fragmentDefaultSearchBinding.recycleViewItems.setLayoutManager(mLayoutManager);
        fragmentDefaultSearchBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());
    }
    private class TagRecycleViewAdapter extends RecyclerView.Adapter<TagRecycleViewAdapter.MyViewHolder> {
        private Context context;
        private ArrayList<String> tagList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView textViewTitle;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewTitle = itemView.findViewById(R.id.textViewTitle);
            }
        }

        public TagRecycleViewAdapter(Context context, ArrayList<String> tagList) {
            this.context = context;
            this.tagList = tagList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_tag_recycle_view, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            String tag = tagList.get(position);
            holder.textViewTitle.setText(tag);
            colors = ColorUtils.getBrightColors();
            random = new Random();
            int color = colors.get(random.nextInt(colors.size()));
            holder.textViewTitle.setTextColor(color);

            holder.itemView.setOnClickListener(v -> {
                // Handle click event for the tag item

            });
        }

        @Override
        public int getItemCount() {
            return tagList.size();
        }
    }
}