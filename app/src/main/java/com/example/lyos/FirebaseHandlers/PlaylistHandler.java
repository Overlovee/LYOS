package com.example.lyos.FirebaseHandlers;

import androidx.annotation.NonNull;

import com.example.lyos.Models.Playlist;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PlaylistHandler {
    private static final String COLLECTION_NAME = "playlists";
    private final static int LIMIT = 30;
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

    public Task<Void> add(Playlist playlist) {
        return collection.add(playlist).continueWithTask(new Continuation<DocumentReference, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<DocumentReference> task) throws Exception {
                if (task.isSuccessful()) {
                    return Tasks.forResult(null);
                } else {
                    throw task.getException();
                }
            }
        });
    }
    public Task<ArrayList<Playlist>> search(String searchString) {
        return search(searchString, LIMIT);
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
    public Task<ArrayList<Playlist>> searchByUserID(String id) {

        return searchByUserID(id, 30);
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


    public Task<Void> update(String id, Playlist item) {
        return collection.document(id)
                .set(item)
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                        if (task.isSuccessful()) {
                            return Tasks.forResult(null); // Thành công, trả về Task<Void> trống
                        } else {
                            throw task.getException(); // Thất bại, ném ra ngoại lệ
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
                            return Tasks.forResult(null); // Thành công, trả về Task<Void> trống
                        } else {
                            throw task.getException(); // Thất bại, ném ra ngoại lệ
                        }
                    }
                });
    }
}
