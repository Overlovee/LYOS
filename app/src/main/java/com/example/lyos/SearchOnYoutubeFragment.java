package com.example.lyos;
import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
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
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lyos.CustomAdapters.YoutubeVideoRecycleViewAdapter;
import com.example.lyos.Models.YoutubeVideo;
import com.example.lyos.databinding.FragmentSearchOnYoutubeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchOnYoutubeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchOnYoutubeFragment extends Fragment {

    private String searchString = "";
    private FragmentSearchOnYoutubeBinding fragmentSearchOnYoutubeBinding;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SearchOnYoutubeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchOnYoutubeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchOnYoutubeFragment newInstance(String param1, String param2) {
        SearchOnYoutubeFragment fragment = new SearchOnYoutubeFragment();
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
        arrayList = new ArrayList<>();
        fragmentSearchOnYoutubeBinding = FragmentSearchOnYoutubeBinding.inflate(getLayoutInflater());
        return fragmentSearchOnYoutubeBinding.getRoot();
    }
    private ArrayList<YoutubeVideo> arrayList;
    YoutubeVideoRecycleViewAdapter adapter;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        arrayList = new ArrayList<>();
        fetchDataFromApi(getContext());
    }

//    private String nextPageToken = "";
//    private int totalResults = 0;
//    private int currentTotalResults = 0;
//    private int resultsPerPage = 0;
    private String apiKey = "AIzaSyA3U_Em89z-aoNc4kPxWMmF4uDtCykRI5g";
    private String url = "";
    private int expectedTotalResults = 20;
    void fetchDataFromApi(Context context) {
        url = "https://www.googleapis.com/youtube/v3/search?key="+apiKey+"&q=" + searchString + "&type=video&part=snippet&maxResults="+String.valueOf(expectedTotalResults);
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJsonData(response, context);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Có lỗi xảy ra khi tải dữ liệu", Toast.LENGTH_LONG).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }

    void parseJsonData(String jsonString, Context context) {
        try {
            JSONObject response = new JSONObject(jsonString);
//            nextPageToken = response.optString("nextPageToken", "");
//            JSONObject pageInfo = response.optJSONObject("pageInfo");
//            if (pageInfo != null) {
//                totalResults = pageInfo.optInt("totalResults", 0);
//                resultsPerPage = pageInfo.optInt("resultsPerPage", 0);
//            }
//            currentTotalResults += resultsPerPage;
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
                        arrayList.add(youtubeVideo);
                    }
                }
//                if (currentTotalResults < expectedTotalResults) {
//                    adapter = new SongRecycleViewAdapter(context, arrayList);
//                    fragmentSearchOnYoutubeBinding.recycleViewItems.setAdapter(adapter);
//                    fragmentSearchOnYoutubeBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
//                    RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
//                    fragmentSearchOnYoutubeBinding.recycleViewItems.setLayoutManager(mLayoutManager);
//                    fragmentSearchOnYoutubeBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());
//                }
//                else {
//                    adapter.notifyDataSetChanged();
//                }
                adapter = new YoutubeVideoRecycleViewAdapter(context, arrayList);
                fragmentSearchOnYoutubeBinding.recycleViewItems.setAdapter(adapter);
                fragmentSearchOnYoutubeBinding.recycleViewItems.addItemDecoration(new DividerItemDecoration(context ,DividerItemDecoration.VERTICAL));
                RecyclerView.LayoutManager mLayoutManager= new LinearLayoutManager(context);
                fragmentSearchOnYoutubeBinding.recycleViewItems.setLayoutManager(mLayoutManager);
                fragmentSearchOnYoutubeBinding.recycleViewItems.setItemAnimator(new DefaultItemAnimator());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Có lỗi xảy ra khi xử lý dữ liệu JSON", Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}