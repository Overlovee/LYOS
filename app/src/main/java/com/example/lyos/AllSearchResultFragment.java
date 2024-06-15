package com.example.lyos;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lyos.CustomAdapters.AlbumRecycleViewAdapter;
import com.example.lyos.CustomAdapters.SongRecycleViewAdapter;
import com.example.lyos.CustomAdapters.UserRecycleViewAdapter;
import com.example.lyos.CustomAdapters.YoutubeVideoRecycleViewAdapter;
import com.example.lyos.FirebaseHandlers.AlbumHandler;
import com.example.lyos.FirebaseHandlers.SongHandler;
import com.example.lyos.FirebaseHandlers.UserHandler;
import com.example.lyos.Models.Album;
import com.example.lyos.Models.Song;
import com.example.lyos.Models.UserInfo;
import com.example.lyos.Models.YoutubeVideo;
import com.example.lyos.databinding.FragmentAllSearchResultBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllSearchResultFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllSearchResultFragment extends Fragment {

    private String searchString = "";
    private FragmentAllSearchResultBinding fragmentAllSearchResultBinding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllSearchResultFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllSearchResultFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllSearchResultFragment newInstance(String param1, String param2) {
        AllSearchResultFragment fragment = new AllSearchResultFragment();
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
            this.searchString = getArguments().getString("search_string", "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentAllSearchResultBinding = FragmentAllSearchResultBinding.inflate(getLayoutInflater());
        return fragmentAllSearchResultBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDataFromFirestore(searchString);
    }
    ArrayList<Song> songArrayList;
    ArrayList<UserInfo> userInfoArrayList;
    ArrayList<Album> albumArrayList;
    ArrayList<YoutubeVideo> youtubeVideoArrayList;
    private void getDataFromFirestore(String searchString) {
        songArrayList = new ArrayList<>();
        userInfoArrayList = new ArrayList<>();
        albumArrayList = new ArrayList<>();
        youtubeVideoArrayList = new ArrayList<>();

        getSongDataFromFirestore(searchString);
        getAlbumDataFromFirestore(searchString);
        getUserDataFromFirestore(searchString);
        getYoutubeData();
    }

    private void getSongDataFromFirestore(String searchString) {
        SongHandler handler = new SongHandler();
        handler.search(searchString, 5).addOnCompleteListener(new OnCompleteListener<ArrayList<Song>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Song>> task) {
                if (task.isSuccessful()) {
                    Context context = getContext();
                    songArrayList = task.getResult();
                    if(!songArrayList.isEmpty()){
                        fragmentAllSearchResultBinding.layoutTracks.setVisibility(View.VISIBLE);
                        SongRecycleViewAdapter adapter = new SongRecycleViewAdapter(context, songArrayList);
                        fragmentAllSearchResultBinding.recycleViewTrackItems.setAdapter(adapter);
                        fragmentAllSearchResultBinding.recycleViewTrackItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                        RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                        fragmentAllSearchResultBinding.recycleViewTrackItems.setLayoutManager(mLayoutManager);
                        fragmentAllSearchResultBinding.recycleViewTrackItems.setItemAnimator(new DefaultItemAnimator());
                    } else {
                        fragmentAllSearchResultBinding.layoutTracks.setVisibility(View.GONE);
                    }

                } else {
                    //and more action --.--
                    fragmentAllSearchResultBinding.layoutTracks.setVisibility(View.GONE);
                }
            }
        });
    }
    private void getAlbumDataFromFirestore(String searchString) {
        AlbumHandler handler = new AlbumHandler();
        handler.search(searchString, 5).addOnCompleteListener(new OnCompleteListener<ArrayList<Album>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Album>> task) {
                if (task.isSuccessful()) {
                    Context context = getContext();
                    albumArrayList = task.getResult();
                    if(!albumArrayList.isEmpty()){
                        fragmentAllSearchResultBinding.layoutTopResults.setVisibility(View.VISIBLE);
                        AlbumRecycleViewAdapter adapter = new AlbumRecycleViewAdapter(context, albumArrayList);
                        fragmentAllSearchResultBinding.recycleViewTopResultItems.setAdapter(adapter);
                        fragmentAllSearchResultBinding.recycleViewTopResultItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                        RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                        fragmentAllSearchResultBinding.recycleViewTopResultItems.setLayoutManager(mLayoutManager);
                        fragmentAllSearchResultBinding.recycleViewTopResultItems.setItemAnimator(new DefaultItemAnimator());
                    } else {
                        fragmentAllSearchResultBinding.layoutTopResults.setVisibility(View.GONE);
                    }

                } else {
                    //and more action --.--
                    fragmentAllSearchResultBinding.layoutTopResults.setVisibility(View.GONE);
                }
            }
        });
    }
    private void getUserDataFromFirestore(String searchString) {
        UserHandler handler = new UserHandler();
        handler.search(searchString, 5).addOnCompleteListener(new OnCompleteListener<ArrayList<UserInfo>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<UserInfo>> task) {
                if (task.isSuccessful()) {
                    Context context = getContext();
                    userInfoArrayList = task.getResult();
                    if(!userInfoArrayList.isEmpty()){
                        fragmentAllSearchResultBinding.layoutUsers.setVisibility(View.VISIBLE);
                        UserRecycleViewAdapter adapter = new UserRecycleViewAdapter(context, userInfoArrayList);
                        fragmentAllSearchResultBinding.recycleViewUserItems.setAdapter(adapter);
                        fragmentAllSearchResultBinding.recycleViewUserItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                        RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                        fragmentAllSearchResultBinding.recycleViewUserItems.setLayoutManager(mLayoutManager);
                        fragmentAllSearchResultBinding.recycleViewUserItems.setItemAnimator(new DefaultItemAnimator());
                    } else {
                        fragmentAllSearchResultBinding.layoutUsers.setVisibility(View.GONE);
                    }
                } else {
                    //and more action --.--
                    fragmentAllSearchResultBinding.layoutUsers.setVisibility(View.GONE);
                }
            }
        });
    }
    public void getYoutubeData() {
        fetchDataFromApi(getContext());
    }
    private String apiKey = "AIzaSyBrbnzR3kdjM5e8Scs-mwkzDeIux8LD6-E";
    private String url = "";
    private int expectedTotalResults = 2;
    void fetchDataFromApi(Context context) {
        url = "https://www.googleapis.com/youtube/v3/search?key="+apiKey+"&q=" + searchString + "&type=video&part=snippet&maxResults="+String.valueOf(expectedTotalResults);
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJsonData(response, context);
                fragmentAllSearchResultBinding.textViewSearchError.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                fragmentAllSearchResultBinding.textViewSearchError.setVisibility(View.VISIBLE);
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }

    void parseJsonData(String jsonString, Context context) {
        try {
            JSONObject response = new JSONObject(jsonString);
            JSONArray items = response.optJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.optJSONObject(i);
                    if (item != null) {
                        String videoId = item.optJSONObject("id").optString("videoId", "");
                        String publishedAtStr = item.optJSONObject("snippet").optString("publishedAt", "");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                        Date publishedAt = dateFormat.parse(publishedAtStr);
                        String channelId = item.optJSONObject("snippet").optString("channelId", "");
                        String channelTitle = item.optJSONObject("snippet").optString("channelTitle", "");
                        String title = item.optJSONObject("snippet").optString("title", "");
                        String description = item.optJSONObject("snippet").optString("description", "");
                        String imageUrl = item.optJSONObject("snippet").optJSONObject("thumbnails").optJSONObject("high").optString("url", "");

                        YoutubeVideo youtubeVideo = new YoutubeVideo(videoId, publishedAt, channelId, channelTitle, title, description, imageUrl);
                        youtubeVideoArrayList.add(youtubeVideo);
                    }
                }
                if(!youtubeVideoArrayList.isEmpty()){
                    fragmentAllSearchResultBinding.layoutSearchedOnYoutubeVideos.setVisibility(View.VISIBLE);
                    YoutubeVideoRecycleViewAdapter adapter = new YoutubeVideoRecycleViewAdapter(context, youtubeVideoArrayList);
                    fragmentAllSearchResultBinding.recycleViewSearchedOnYoutubeVideoItems.setAdapter(adapter);
                    fragmentAllSearchResultBinding.recycleViewSearchedOnYoutubeVideoItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                    fragmentAllSearchResultBinding.recycleViewSearchedOnYoutubeVideoItems.setLayoutManager(mLayoutManager);
                    fragmentAllSearchResultBinding.recycleViewSearchedOnYoutubeVideoItems.setItemAnimator(new DefaultItemAnimator());
                } else {
                    fragmentAllSearchResultBinding.layoutSearchedOnYoutubeVideos.setVisibility(View.GONE);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            fragmentAllSearchResultBinding.layoutSearchedOnYoutubeVideos.setVisibility(View.GONE);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}