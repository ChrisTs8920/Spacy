package com.example.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    private final String CHANNEL_ID = "";
    private final int NOTIFICATION_ID = 1;
    ArrayList<File> filenames;
    int[] songIDs;
    int currSongId;
    MediaPlayer MP;
    boolean active;
    NotificationCompat.Builder notification;
    NotificationManager notificationManager;
    SingletonCurr singletonCurr;
    private final IBinder binder = new LocalBinder();

    @Override
    public void onCreate() {
        singletonCurr = SingletonCurr.getInstance();
        filenames = AudioFileReader.getAudioFiles();
        createNotificationChannel();
        createNotification();
        MP = new MediaPlayer();
        active = false;
        setSongIDs();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        notification.setContentText(singletonCurr.getCurrSongString());
        notificationManager.notify(NOTIFICATION_ID, notification.build());
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { // called right after onCreate
        return binder;
    }

    public void createNotification() {
        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Playing")
                .setContentText(singletonCurr.getCurrSongString())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.music_note))
                .setSmallIcon(R.mipmap.ic_launcher_astr)
                .setShowWhen(false)
                .setOngoing(true) // notification cannot be dismissed
                .setPriority(Notification.PRIORITY_DEFAULT); // for android 7.1 and lower

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this)
                    .notify(NOTIFICATION_ID, notification.build());
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ / Android 8 because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notification",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Default description");
            // Register the channel with the system
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Override
    public void onDestroy() {
        MP.stop();
        MP.release();
        active = false;
        notificationManager.cancel(NOTIFICATION_ID);
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

        // update notification
        singletonCurr.setCurrSongString(filenames.get(currSongId).getName());
        notification.setContentText(singletonCurr.getCurrSongString());
        notificationManager.notify(NOTIFICATION_ID, notification.build());

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

        // update notification
        singletonCurr.setCurrSongString(filenames.get(currSongId).getName());
        notification.setContentText(singletonCurr.getCurrSongString());
        notificationManager.notify(NOTIFICATION_ID, notification.build());

        return filenames.get(currSongId);
    }

    public void playSong() {
        if (active) {
            return;
        }
        MP.start();
        active = true;
        notification.setContentTitle("Playing");
        notificationManager.notify(NOTIFICATION_ID, notification.build());
    }

    public void pauseSong() {
        if (!active) {
            return;
        }
        MP.pause();
        active = false;
        notification.setContentTitle("Paused");
        notificationManager.notify(NOTIFICATION_ID, notification.build());
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
