package com.example.lyos.FirebaseHandlers;

import androidx.annotation.NonNull;

import com.example.lyos.Models.UserInfo;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserHandler {
    private static final String COLLECTION_NAME = "users";

    private FirebaseFirestore db;
    private CollectionReference collection;

    public UserHandler() {
        db = FirebaseFirestore.getInstance();
        collection = db.collection(COLLECTION_NAME);
    }
    public Task<Void> updateUserName(String userId, String newName) {
        return FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("username", newName);
    }
    public Task<Void> updateProfilePhoto(String userId, String photoUrl) {
        return FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("profilePhoto", photoUrl);
    }
    public Task<Void> updateProfileBanner(String userId, String photoUrl) {
        return FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("profileBanner", photoUrl);
    }

    public Task<ArrayList<UserInfo>> getAllData() {
        ArrayList<UserInfo> list = new ArrayList<>();
        return collection.get().continueWith(new Continuation<QuerySnapshot, ArrayList<UserInfo>>() {
            @Override
            public ArrayList<UserInfo> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        UserInfo item = document.toObject(UserInfo.class);
                        String id = document.getId();
                        item.setId(id);
                        list.add(item);
                    }
                }
                return list;
            }
        });
    }

    public Task<Void> add(UserInfo item) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        item.setNormalizedUsername(normalizeString(item.getUsername()));
        collection.add(item)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        taskCompletionSource.setResult(null); // Đánh dấu Task là thành công
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        taskCompletionSource.setException(e); // Đánh dấu Task là thất bại và truyền ngoại lệ
                    }
                });

        return taskCompletionSource.getTask();
    }
    public Task<ArrayList<UserInfo>> search(String searchString) {
        return search(searchString, 30);
    }
    public Task<ArrayList<UserInfo>> search(String searchString, int limit) {
        ArrayList<UserInfo> list = new ArrayList<>();
        String normalizedSearchString = normalizeString(searchString);
        return collection.whereLessThanOrEqualTo("normalizedUsername", normalizedSearchString + "\uf8ff")
                .limit(limit)
                .get()
                .continueWith(new Continuation<QuerySnapshot, ArrayList<UserInfo>>() {
                    @Override
                    public ArrayList<UserInfo> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                UserInfo item = document.toObject(UserInfo.class);
                                String id = document.getId();
                                item.setId(id);
                                list.add(item);
                            }
                        }
                        return list;
                    }
                });
    }
    public Task<UserInfo> getInfoByID(String id) {
        DocumentReference docRef = collection.document(id);

        return docRef.get().continueWith(new Continuation<DocumentSnapshot, UserInfo>() {
            @Override
            public UserInfo then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        UserInfo item = document.toObject(UserInfo.class);
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
    public Task<UserInfo> getUserByEmail(String email) {
        return collection.whereEqualTo("email", email)
                .get()
                .continueWith(new Continuation<QuerySnapshot, UserInfo>() {
                    @Override
                    public UserInfo then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                UserInfo item = document.toObject(UserInfo.class);
                                item.setId(document.getId());
                                return item;
                            }
                        }
                        return null; // No user found with the given email
                    }
                });
    }

    private String normalizeString(String input) {
        // Remove non-alphanumeric characters and convert to lowercase
        return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }


    public void update(String id, UserInfo item) {
        collection.document(id)
                .set(item);
    }
    public void delete(String id) {
        collection.document(id)
                .delete();
    }
}
