package com.example.musicplayer;

// Stores current Song playing
// This class could replace sending data between activities using putExtra()
public class SingletonCurr {
    private static SingletonCurr instance = null;
    private static String currSongString = "";

    private SingletonCurr() {

    }

    public static synchronized SingletonCurr getInstance() {
        if (instance == null) {
            instance = new SingletonCurr();
        }
        return instance;
    }

    public void setCurrSongString(String curr) {
        currSongString = curr;
    }

    public String getCurrSongString() {
        return currSongString;
    }
}
