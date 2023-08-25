package com.example.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    ArrayList<File> filenames;
    int[] songIDs;
    int currSongId;
    MediaPlayer MP;
    boolean active;
    Notification notification;
    NotificationManager manager;
    private final IBinder binder = new LocalBinder();

    @Override
    public void onCreate() {
        SingletonCurr singletonCurr = SingletonCurr.getInstance();
        /*notification = new Notification.Builder(this)
                .setContentTitle("Currently Playing")
                .setContentText(singletonCurr.getCurrSongString())
                .setSmallIcon(R.mipmap.ic_launcher_astr)
                .build();*/
        // manager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        // .notify(1, notification.build());
        filenames = AudioFileReader.getAudioFiles();
        MP = new MediaPlayer();
        active = false;
        setSongIDs();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // startForeground(startId, notification); // make Foreground service
        if (currSongId == intent.getIntExtra("currSong", 0)) { // if same song selected, keep playing
            return START_STICKY;
        }
        currSongId = intent.getIntExtra("currSong", 0);
        if (MP.isPlaying()) {
            MP.stop();
            MP.release();
        }
        MP = new MediaPlayer();
        MP.setLooping(false);
        try {
            MP.setDataSource(String.valueOf(filenames.get(currSongId)));
            MP.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MP.start();
        active = true;
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { // called right after onCreate
        return binder;
    }

    @Override
    public void onDestroy() {
        MP.stop();
        MP.release();
        active = false;
    }

    public void setSongIDs() {
        songIDs = new int[filenames.size()];
        for (int i = 0; i < filenames.size(); i++) {
            songIDs[i] = i;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
    }

    public File nextSong() {
        if (MP.isPlaying()) {
            MP.stop();
            MP.release();
        }
        if (++currSongId == filenames.size()) {
            currSongId = 0;
        }
        MP = new MediaPlayer();
        try {
            MP.setDataSource(String.valueOf(filenames.get(currSongId)));
            MP.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (active) {
            MP.start();
        }
        return filenames.get(currSongId);
    }

    public File previousSong() {
        if (MP.isPlaying()) {
            MP.stop();
            MP.release();
        }
        if (--currSongId == -1) {
            currSongId = filenames.size() - 1;
        }
        MP = new MediaPlayer();
        try {
            MP.setDataSource(String.valueOf(filenames.get(currSongId)));
            MP.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (active) {
            MP.start();
        }
        return filenames.get(currSongId);
    }

    public void playSong() {
        if (active) {
            return;
        }
        MP.start();
        active = true;
    }

    public void pauseSong() {
        if (!active) {
            return;
        }
        MP.pause();
        active = false;
    }

    public class LocalBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }
}
