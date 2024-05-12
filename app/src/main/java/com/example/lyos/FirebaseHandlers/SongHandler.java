package com.example.lyos.FirebaseHandlers;

import androidx.annotation.NonNull;

import com.example.lyos.Models.Song;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SongHandler {
    private static final String COLLECTION_NAME = "songs";

    private FirebaseFirestore db;
    private CollectionReference songsCollection;

    public SongHandler() {
        db = FirebaseFirestore.getInstance();
        songsCollection = db.collection(COLLECTION_NAME);
    }

    public Task<ArrayList<Song>> getAllSongs() {
        ArrayList<Song> list = new ArrayList<>();
        return songsCollection.get().continueWith(new Continuation<QuerySnapshot, ArrayList<Song>>() {
            @Override
            public ArrayList<Song> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Song song = document.toObject(Song.class);
                        list.add(song);
                    }
                }
                return list;
            }
        });
    }

    public void addSong(Song song) {
//        Map<String, Object> item = new HashMap<>();
//        item.put("first", "Alan");
//        item.put("middle", "Mathison");
//        item.put("last", "Turing");
//        item.put("born", 1912);

        songsCollection.add(song)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }
    public Task<ArrayList<Song>> searchSong(String searchString) {
        ArrayList<Song> list = new ArrayList<>();
        String normalizedSearchString = normalizeString(searchString);
        return songsCollection.whereLessThanOrEqualTo("normalizedTitle", normalizedSearchString + "\uf8ff")
                .get()
                .continueWith(new Continuation<QuerySnapshot, ArrayList<Song>>() {
                    @Override
                    public ArrayList<Song> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Song song = document.toObject(Song.class);
                                // Additional filtering if needed
                                list.add(song);
                            }
                        }
                        return list;
                    }
                });
    }
    private String normalizeString(String input) {
        // Remove non-alphanumeric characters and convert to lowercase
        return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }


    public void updateSong(String songId, Song song) {
        songsCollection.document(songId)
                .set(song);
    }
    public void deleteSong(String songId) {
        songsCollection.document(songId)
                .delete();
    }
}
