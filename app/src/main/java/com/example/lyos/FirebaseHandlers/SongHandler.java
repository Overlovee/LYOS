package com.example.lyos.FirebaseHandlers;

import androidx.annotation.NonNull;

import com.example.lyos.Models.Album;
import com.example.lyos.Models.Song;
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
import java.util.Date;
import java.util.HashSet;
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
    public Task<ArrayList<String>> getAllUniqueTags() {
        // Use a HashSet to store unique tags
        HashSet<String> uniqueTagsSet = new HashSet<>();

        // Perform Firestore query to get all songs
        return collection.get().continueWith(new Continuation<QuerySnapshot, ArrayList<String>>() {
            @Override
            public ArrayList<String> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Song song = document.toObject(Song.class);
                        if (song != null && song.getTag() != null) {
                            String[] tags = song.getTag().split("\\s*#\\s*");
                            for (String tag : tags) {
                                if (!tag.trim().isEmpty()) {
                                    uniqueTagsSet.add("#" + tag.trim());
                                }
                            }
                        }
                    }
                }
                // Convert the HashSet to an ArrayList
                return new ArrayList<>(uniqueTagsSet);
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
    public Task<ArrayList<Song>> getTopSongs(int limit) {
        ArrayList<Song> list = new ArrayList<>();
        return collection.orderBy("listens", com.google.firebase.firestore.Query.Direction.DESCENDING)
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
    public Task<ArrayList<Song>> getSongsNotInAnyAlbum(String userId) {
        // Tạo một danh sách để lưu trữ các ID của các bài hát nằm trong album của người dùng
        ArrayList<String> songIdsInAlbums = new ArrayList<>();

        // Tạo một task để lấy tất cả các album của người dùng
        Task<ArrayList<Album>> userAlbumsTask = new AlbumHandler().searchByUserID(userId);

        // Tạo một task để lấy tất cả các bài hát của người dùng
        Task<ArrayList<Song>> userSongsTask = new SongHandler().searchByUserID(userId);

        // Sử dụng Tasks.whenAllSuccess để chờ cả hai task hoàn thành
        return Tasks.whenAllSuccess(userAlbumsTask, userSongsTask).continueWith(new Continuation<List<Object>, ArrayList<Song>>() {
            @Override
            public ArrayList<Song> then(@NonNull Task<List<Object>> task) throws Exception {
                // Kiểm tra xem cả hai task đã hoàn thành thành công không
                if (task.isSuccessful()) {
                    // Lấy danh sách album và danh sách bài hát từ kết quả task
                    ArrayList<Album> userAlbums = (ArrayList<Album>) task.getResult().get(0);
                    ArrayList<Song> userSongs = (ArrayList<Song>) task.getResult().get(1);

                    // Lặp qua tất cả các album và lấy danh sách các bài hát nằm trong album
                    for (Album album : userAlbums) {
                        if (album.getSongList() != null) {
                            songIdsInAlbums.addAll(album.getSongList());
                        }
                    }

                    // Lọc ra các bài hát không nằm trong album
                    ArrayList<Song> songsNotInAlbums = new ArrayList<>();
                    for (Song song : userSongs) {
                        if (!songIdsInAlbums.contains(song.getId())) {
                            songsNotInAlbums.add(song);
                        }
                    }

                    // Trả về danh sách các bài hát không nằm trong album
                    return songsNotInAlbums;
                } else {
                    // Nếu có lỗi xảy ra trong quá trình lấy dữ liệu, trả về null hoặc xử lý theo ý muốn của bạn
                    Exception exception = task.getException();
                    if (exception != null) {
                        exception.printStackTrace();
                    }
                    return null;
                }
            }
        });
    }
    public Task<ArrayList<Song>> searchByTag(String tag) {
        ArrayList<Song> list = new ArrayList<>();
        String normalizedTag = normalizeString(tag);
        return collection
                .whereLessThanOrEqualTo("tag", normalizedTag)
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

    private String normalizeString(String input) {
        // Remove non-alphanumeric characters and convert to lowercase
        return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    public Task<Void> addSong(Song item) {
        item.setId(null);
        item.setNormalizedTitle(normalizeString(item.getTitle()));
        if(!item.getTag().contains("#")){
            item.setTag("#" + item.getTag());
        }
        item.setTag(item.getTag().trim());
        item.setType("system");
        item.setUploadDate(new Date());
        return collection.add(item)
                .continueWithTask(new Continuation<DocumentReference, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<DocumentReference> task) throws Exception {
                        if (task.isSuccessful()) {
                            // Nếu thêm thành công, trả về null
                            return Tasks.forResult(null);
                        } else {
                            // Nếu thêm tài liệu không thành công, ném ra ngoại lệ
                            throw task.getException();
                        }
                    }
                });
    }

    public Task<Void> updateSong(String id, Song item) {
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

    public Task<Void> deleteSong(String id) {
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
