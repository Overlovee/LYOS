package com.example.lyos.FirebaseHandlers;

import androidx.annotation.NonNull;

import com.example.lyos.Models.Album;
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

public class AlbumHandler {
    private static final String COLLECTION_NAME = "albums";
    private final static int LIMIT = 30;
    private FirebaseFirestore db;
    private CollectionReference collection;

    public AlbumHandler() {
        db = FirebaseFirestore.getInstance();
        collection = db.collection(COLLECTION_NAME);
    }

    public Task<ArrayList<Album>> getAllData() {
        ArrayList<Album> list = new ArrayList<>();
        return collection.get().continueWith(new Continuation<QuerySnapshot, ArrayList<Album>>() {
            @Override
            public ArrayList<Album> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Album item = document.toObject(Album.class);
                        String id = document.getId();
                        item.setId(id);
                        list.add(item);
                    }
                }
                return list;
            }
        });
    }

    public void add(Album item) {

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
    public Task<ArrayList<Album>> search(String searchString) {
        return search(searchString, LIMIT);
    }
    public Task<ArrayList<Album>> search(String searchString, int limit) {
        ArrayList<Album> list = new ArrayList<>();
        String normalizedSearchString = normalizeString(searchString);
        return collection.whereLessThanOrEqualTo("normalizedTitle", normalizedSearchString + "\uf8ff")
                .limit(limit)
                .get()
                .continueWith(new Continuation<QuerySnapshot, ArrayList<Album>>() {
                    @Override
                    public ArrayList<Album> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Album item = document.toObject(Album.class);
                                String id = document.getId();
                                item.setId(id);
                                list.add(item);
                            }
                        }
                        return list;
                    }
                });
    }
    public Task<Album> getInfoByID(String id) {
        DocumentReference docRef = collection.document(id);

        return docRef.get().continueWith(new Continuation<DocumentSnapshot, Album>() {
            @Override
            public Album then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Album item = document.toObject(Album.class);
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
    public Task<ArrayList<Album>> searchByUserID(String id) {
        return searchByUserID(id, LIMIT);
    }
    public Task<ArrayList<Album>> searchByUserID(String id, int limit) {
        ArrayList<Album> list = new ArrayList<>();
        return collection.whereEqualTo("userID", id)
                .limit(limit)
                .get()
                .continueWith(new Continuation<QuerySnapshot, ArrayList<Album>>() {
                    @Override
                    public ArrayList<Album> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Album item = document.toObject(Album.class);
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


    public Task<Void> update(String id, Album item) {
        return collection.document(id)
                .set(item)
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                        if (task.isSuccessful()) {
                            return Tasks.forResult(null);
                        } else {
                            throw task.getException();
                        }
                    }
                });
    }

    public Task<Void> delete(String id) {
        return collection.document(id)
                .delete()
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                        if (task.isSuccessful()) {
                            return Tasks.forResult(null);
                        } else {
                            throw task.getException();
                        }
                    }
                });
    }
    public Task<Void> removeSongFromAllAlbums(String songId) {
        return collection.whereArrayContains("songList", songId)
                .get()
                .continueWithTask(new Continuation<QuerySnapshot, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            ArrayList<Task<Void>> tasks = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Album album = document.toObject(Album.class);
                                if (album.getSongList() != null) {
                                    if (album.getSongList().contains(songId)) {
                                        album.getSongList().remove(songId);
                                        tasks.add(collection.document(document.getId()).set(album));
                                    }
                                }
                            }
                            return Tasks.whenAll(tasks);
                        } else {
                            throw task.getException();
                        }
                    }
                });
    }
}
