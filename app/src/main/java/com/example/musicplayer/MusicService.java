package com.example.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    ArrayList<File> filenames;
    int[] songIDs;
    int currSong;
    MediaPlayer MP;
    boolean active;
    private final IBinder binder = new LocalBinder();

    @Override
    public void onCreate() {
        filenames = AudioFileReader.getAudioFiles();
        active = false;
        setSongIDs();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { // called right after onCreate
        currSong = intent.getIntExtra("currSong", 0);
        MP = new MediaPlayer();
        try {
            MP.setDataSource(String.valueOf(filenames.get(currSong)));
            MP.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MP.setLooping(false);
        // MP.setOnCompletionListener(this);
        MP.start();
        active = true;
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
        // nextSong();
    }

    public File nextSong() {
        if (MP.isPlaying()) {
            MP.stop();
            MP.release();
        }
        if (++currSong == filenames.size()) {
            currSong = 0;
        }
        MP = new MediaPlayer();
        try {
            MP.setDataSource(String.valueOf(filenames.get(currSong)));
            MP.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MP.setOnCompletionListener(this);
        if (active) {
            MP.start();
        }
        return filenames.get(currSong);
    }

    public File previousSong() {
        if (MP.isPlaying()) {
            MP.stop();
            MP.release();
        }
        if (--currSong == -1) {
            currSong = filenames.size() - 1;
        }
        MP = new MediaPlayer();
        try {
            MP.setDataSource(String.valueOf(filenames.get(currSong)));
            MP.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MP.setOnCompletionListener(this);
        if (active) {
            MP.start();
        }
        return filenames.get(currSong);
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
