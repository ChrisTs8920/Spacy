package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashAct extends AppCompatActivity {

    private static long SPLASH_DUR = 2000;

    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        this.setContentView(R.layout.splashlayout);
        this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // Show splash screen for 2 seconds, then move on to Browse Activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start browse Activity
                Intent nextAct = new Intent(getApplicationContext(), BrowseAct.class);
                startActivity(nextAct);
                finish();
            }
        }, SPLASH_DUR);
    }
}
