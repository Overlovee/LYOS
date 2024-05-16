package com.example.lyos.FirebaseHandlers;

import androidx.annotation.NonNull;

import com.example.lyos.Models.Playlist;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PlaylistHandler {
    private static final String COLLECTION_NAME = "playlists";

    private FirebaseFirestore db;
    private CollectionReference collection;

    public PlaylistHandler() {
        db = FirebaseFirestore.getInstance();
        collection = db.collection(COLLECTION_NAME);
    }

    public Task<ArrayList<Playlist>> getAllData() {
        ArrayList<Playlist> list = new ArrayList<>();
        return collection.get().continueWith(new Continuation<QuerySnapshot, ArrayList<Playlist>>() {
            @Override
            public ArrayList<Playlist> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Playlist item = document.toObject(Playlist.class);
                        String id = document.getId();
                        item.setId(id);
                        list.add(item);
                    }
                }
                return list;
            }
        });
    }

    public void add(Playlist item) {

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
    public Task<ArrayList<Playlist>> search(String searchString) {
        return search(searchString, 30);
    }
    public Task<ArrayList<Playlist>> search(String searchString, int limit) {
        ArrayList<Playlist> list = new ArrayList<>();
        String normalizedSearchString = normalizeString(searchString);
        return collection.whereLessThanOrEqualTo("normalizedTitle", normalizedSearchString + "\uf8ff")
                .limit(limit)
                .get()
                .continueWith(new Continuation<QuerySnapshot, ArrayList<Playlist>>() {
                    @Override
                    public ArrayList<Playlist> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Playlist item = document.toObject(Playlist.class);
                                String id = document.getId();
                                item.setId(id);
                                list.add(item);
                            }
                        }
                        return list;
                    }
                });
    }
    public Task<Playlist> getInfoByID(String id) {
        DocumentReference docRef = collection.document(id);

        return docRef.get().continueWith(new Continuation<DocumentSnapshot, Playlist>() {
            @Override
            public Playlist then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Playlist item = document.toObject(Playlist.class);
                        item.setId(document.getId());
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
    public Task<ArrayList<Playlist>> searchByUserID(String id, int limit) {
        ArrayList<Playlist> list = new ArrayList<>();
        return collection.whereEqualTo("userID", id)
                .limit(limit)
                .get()
                .continueWith(new Continuation<QuerySnapshot, ArrayList<Playlist>>() {
                    @Override
                    public ArrayList<Playlist> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Playlist item = document.toObject(Playlist.class);
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
    private String normalizeString(String input) {
        // Remove non-alphanumeric characters and convert to lowercase
        return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }


    public void update(String id, Playlist item) {
        collection.document(id)
                .set(item);
    }
    public void delete(String id) {
        collection.document(id)
                .delete();
    }
}
