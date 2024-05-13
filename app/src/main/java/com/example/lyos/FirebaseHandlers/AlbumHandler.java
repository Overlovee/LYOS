package com.example.lyos.FirebaseHandlers;

import androidx.annotation.NonNull;

import com.example.lyos.Models.Album;
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

public class AlbumHandler {
    private static final String COLLECTION_NAME = "albums";

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
        ArrayList<Album> list = new ArrayList<>();
        String normalizedSearchString = normalizeString(searchString);
        return collection.whereLessThanOrEqualTo("normalizedTitle", normalizedSearchString + "\uf8ff")
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
    private String normalizeString(String input) {
        // Remove non-alphanumeric characters and convert to lowercase
        return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }


    public void update(String id, Album item) {
        collection.document(id)
                .set(item);
    }
    public void delete(String id) {
        collection.document(id)
                .delete();
    }
}
