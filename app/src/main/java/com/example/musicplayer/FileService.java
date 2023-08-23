package com.example.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

public class FileService extends Service {
    ArrayList<String> filenames;
    FileObserver observer;

    public FileService() {
        // Detects any changes that may happen to DIRECTORY_MUSIC
        File audioDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        audioDir = new File(audioDir.getAbsolutePath());

        observer = new FileObserver(audioDir) {
            @Override
            public void onEvent(int event, @Nullable String file) {
                switch (event) {
                    case FileObserver.CREATE:
                        filenames.add(file);
                    case FileObserver.DELETE:
                        filenames.remove(file);
                }
            }
        };
    }

    public void onCreate() {
        filenames = new ArrayList<>();
        observer.startWatching();
    }

    public void onDestroy() {
        observer.stopWatching();
        observer = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException ("Not yet implemented");
    }
}
