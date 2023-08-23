package com.example.musicplayer;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class AudioFileReader {

    public static ArrayList<File> getAudioFiles() {
        File audioDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        ArrayList<File> filenames = new ArrayList<>();
        if (audioDir.exists() && audioDir.isDirectory()) {
            File[] files = audioDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isAudioFile(file)) {
                        filenames.add(file);
                    }
                }
            }
        }
        return filenames;
    }

    public static boolean isAudioFile(File file) {
        String filename = file.getName();
        return filename.endsWith(".mp3") || filename.endsWith(".ogg") || filename.endsWith(".wav");
    }
}
