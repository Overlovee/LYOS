package com.example.lyos.FirebaseHandlers;

import androidx.annotation.NonNull;

import com.example.lyos.Models.Song;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SongHandler {
    private static final String COLLECTION_NAME = "songs";
    private FirebaseFirestore db;
    private CollectionReference collection;

    public SongHandler() {
        db = FirebaseFirestore.getInstance();
        collection = db.collection(COLLECTION_NAME);
    }

    public Task<ArrayList<Song>> getAllSongs() {
        ArrayList<Song> list = new ArrayList<>();
        return collection.get().continueWith(new Continuation<QuerySnapshot, ArrayList<Song>>() {
            @Override
            public ArrayList<Song> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Song item = document.toObject(Song.class);
                        String id = document.getId();
                        item.setId(id);
                        list.add(item);
                    }
                }
                return list;
            }
        });
    }

    public void addSong(Song item) {
//        Map<String, Object> item = new HashMap<>();
//        item.put("first", "Alan");
//        item.put("middle", "Mathison");
//        item.put("last", "Turing");
//        item.put("born", 1912);

        collection.add(item)
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
    public Task<ArrayList<Song>> search(String searchString) {
        return search(searchString, 30);
    }
    public Task<ArrayList<Song>> search(String searchString, int limit) {
        ArrayList<Song> list = new ArrayList<>();
        String normalizedSearchString = normalizeString(searchString);
        return collection.whereLessThanOrEqualTo("normalizedTitle", normalizedSearchString + "\uf8ff")
                .limit(limit)
                .get()
                .continueWith(new Continuation<QuerySnapshot, ArrayList<Song>>() {
                    @Override
                    public ArrayList<Song> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Song item = document.toObject(Song.class);
                                if (item != null) {
                                    item.setId(document.getId());
                                    list.add(item);
                                }
                            }
                        }
                        return list;
                    }
                });
    }
    public Task<ArrayList<Song>> searchByUserID(String id) {
        return searchByUserID(id, 30);
    }
    public Task<ArrayList<Song>> searchByUserID(String id, int limit) {
        ArrayList<Song> list = new ArrayList<>();
        return collection.whereEqualTo("userID", id)
                .limit(limit)
                .get()
                .continueWith(new Continuation<QuerySnapshot, ArrayList<Song>>() {
                    @Override
                    public ArrayList<Song> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Song item = document.toObject(Song.class);
                                if (item != null) {
                                    item.setId(document.getId());
                                    list.add(item);
                                }
                            }
                        }
                        return list;
                    }
                });
    }
    public Task<Song> getInfoByID(String id) {
        DocumentReference docRef = collection.document(id);
        return docRef.get().continueWith(new Continuation<DocumentSnapshot, Song>() {
            @Override
            public Song then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Song item = document.toObject(Song.class);
                        if (item != null) {
                            item.setId(document.getId());
                        }
                        return item;
                    } else {
                        // Document does not exist
                        return null;
                    }
                } else {
                    // Task failed with an exception
                    throw task.getException();
                }
            }
        });
    }
    public Task<ArrayList<Song>> getDataByListID(ArrayList<String> ids) {
        return getDataByListID(ids, 30);
    }
    public Task<ArrayList<Song>> getDataByListID(ArrayList<String> ids, int limit) {
        ArrayList<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (int i = 0; i < Math.min(ids.size(), limit); i++) {
            DocumentReference docRef = collection.document(ids.get(i));
            tasks.add(docRef.get());
        }

        return Tasks.whenAllSuccess(tasks).continueWith(new Continuation<List<Object>, ArrayList<Song>>() {
            @Override
            public ArrayList<Song> then(@NonNull Task<List<Object>> task) throws Exception {
                ArrayList<Song> list = new ArrayList<>();
                for (Object result : task.getResult()) {
                    if (result instanceof DocumentSnapshot) {
                        DocumentSnapshot document = (DocumentSnapshot) result;
                        if (document.exists()) {
                            Song item = document.toObject(Song.class);
                            if (item != null) {
                                item.setId(document.getId());
                                list.add(item);
                            }
                        }
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


    public void updateSong(String id, Song item) {
        collection.document(id)
                .set(item);
    }
    public void deleteSong(String id) {
        collection.document(id)
                .delete();
    }
}
