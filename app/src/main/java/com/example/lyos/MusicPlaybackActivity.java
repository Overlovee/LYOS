package com.example.lyos;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import com.example.lyos.databinding.ActivityMusicPlaybackBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class MusicPlaybackActivity extends AppCompatActivity {

    private MediaPlayer mPlayer;
    private ActivityMusicPlaybackBinding activityMusicPlaybackBinding;
    private String songResourceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMusicPlaybackBinding = ActivityMusicPlaybackBinding.inflate(getLayoutInflater());
        setContentView(activityMusicPlaybackBinding.getRoot());

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // Create a Cloud Storage reference from the app
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("mp3s/"+ "ngoailecuanhau_youngtobieedasick(Obito).mp3");
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    // get URL from StorageReference successfully
                    String downloadUrl = uri.toString();
                    songResourceId = downloadUrl;
                    // Thiết lập dữ liệu nguồn cho MediaPlayer
                    mPlayer.setDataSource(songResourceId);
                    // Chuẩn bị dữ liệu nguồn
                    mPlayer.prepareAsync();
                    // Xử lý sự kiện khi dữ liệu nguồn đã được chuẩn bị xong
                    mPlayer.setOnPreparedListener(mp -> {
                        mp.start();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(exception -> {
            // Xử lý khi không thể lấy URL từ StorageReference

        });

        activityMusicPlaybackBinding.sBar.setClickable(false);
        activityMusicPlaybackBinding.btnPause.setEnabled(false);

        AddEvents();
    }
    void AddEvents(){
//        activityMusicPlaybackBinding.btnPlay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Playing Audio",
//                        Toast.LENGTH_SHORT).show();
//                mPlayer.start();
//                eTime = mPlayer.getDuration();
//                sTime = mPlayer.getCurrentPosition();
//                if(oTime == 0){
//                    activityMusicPlaybackBinding.sBar.setMax(eTime);
//                    oTime =1;
//                }
//                activityMusicPlaybackBinding.txtSongTime.setText(String.format("%d min, %d sec",
//                        TimeUnit.MILLISECONDS.toMinutes(eTime),
//                        TimeUnit.MILLISECONDS.toSeconds(eTime) -
//                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(eTime)))
//                );
//                activityMusicPlaybackBinding.txtStartTime.setText(String.format("%d min, %d sec",
//                        TimeUnit.MILLISECONDS.toMinutes(sTime),
//                        TimeUnit.MILLISECONDS.toSeconds(sTime) -
//                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime)))
//                );
//                activityMusicPlaybackBinding.sBar.setProgress(sTime);
//                activityMusicPlaybackBinding.btnPause.setEnabled(true);
//                activityMusicPlaybackBinding.btnPlay.setEnabled(false);
//            }
//        });
//        activityMusicPlaybackBinding.btnPause.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPlayer.pause();
//                activityMusicPlaybackBinding.btnPause.setEnabled(false);
//                activityMusicPlaybackBinding.btnPlay.setEnabled(true);
//                Toast.makeText(getApplicationContext(),"Pausing Audio", Toast.LENGTH_SHORT).show();
//            }
//        });
//        activityMusicPlaybackBinding.btnForward.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if((sTime + fTime) <= eTime)
//                {
//                    sTime = sTime + fTime;
//                    mPlayer.seekTo(sTime);
//                }
//                else
//                {
//                    Toast.makeText(getApplicationContext(), "Cannot jump forward 5 seconds", Toast.LENGTH_SHORT).show();
//                }
//                if(!activityMusicPlaybackBinding.btnPlay.isEnabled()){
//                    activityMusicPlaybackBinding.btnPlay.setEnabled(true);
//                }
//            }
//        });
//        activityMusicPlaybackBinding.btnBackward.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if((sTime - bTime) > 0)
//                {
//                    sTime = sTime - bTime;
//                    mPlayer.seekTo(sTime);
//                }
//                else
//                {
//                    Toast.makeText(getApplicationContext(), "Cannot jump backward 5 seconds", Toast.LENGTH_SHORT).show();
//                }
//                if(!activityMusicPlaybackBinding.btnPlay.isEnabled()){
//                    activityMusicPlaybackBinding.btnPlay.setEnabled(true);
//                }
//            }
//        });
    }
    //    private Runnable UpdateSongTime = new Runnable() {
//        @Override
//        public void run() {
//            sTime = mPlayer.getCurrentPosition();
//            activityMusicPlaybackBinding.txtStartTime.setText(String.format("%d min, %d sec",
//                    TimeUnit.MILLISECONDS.toMinutes(sTime),
//                    TimeUnit.MILLISECONDS.toSeconds(sTime) -
//                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime))) );
//            activityMusicPlaybackBinding.sBar.setProgress(sTime);
//            hdlr.postDelayed(this, 100);
//        }
//    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}