package com.example.musicplayer;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;

public class BrowseAct extends AppCompatActivity implements View.OnClickListener {

    private final int PERMISSION_REQUEST_CODE = 1; // permission code for READ_MEDIA_AUDIO
    ArrayList<File> filenames = null;
    LinearLayout parentLayout;
    TextView noTracksText;
    SearchView searchView;
    ImageButton refreshButton;
    String currSongString = "";

    SingletonCurr singletonCurr;

    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        this.setContentView(R.layout.browselayout);
        this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        askPerm();
        parentLayout = findViewById(R.id.linearLayout);
        noTracksText = findViewById(R.id.noTracks);
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()) {
                    filenames = AudioFileReader.getAudioFiles();
                } else {
                    filenames.removeIf(i -> !i.getName().contains(s)); // equal to a for loop
                }
                createListUi();
                return false;
            }
        });
        refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(this);
        singletonCurr = SingletonCurr.getInstance();
        filenames = AudioFileReader.getAudioFiles(); // if perm granted return song list, else return empty list
        createListUi();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        currSongString = singletonCurr.getCurrSongString();
        createListUi();
    }

    @Override
    public void onClick(View view) {
        if (view == refreshButton) {
            createListUi();
        }
    }

    private void askPerm() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) { // if android 12 and lower
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE); //This calls onRequestPermissionsResult()
            }
        } else { // if android 13+
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE); //This calls onRequestPermissionsResult()
            }
        }
    }

    private void createListUi() {
        //Layout parameters
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        ViewGroup.MarginLayoutParams paramsMargin = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        paramsMargin.setMargins(15, 0, 15, 15);

        // for every audio file detected, make a new CardView that contains a TextView
        // and add it to parent layout
        if (filenames.size() != 0) {
            noTracksText.setVisibility(View.GONE);
            parentLayout.removeViews(1, parentLayout.getChildCount() - 1); // remove all views except first one, in case of refresh
            for (File i : filenames) {
                //CardView
                CardView cardView = new CardView(getApplicationContext());
                cardView.setLayoutParams(paramsMargin);
                cardView.setCardBackgroundColor(getColor(R.color.primary_color));
                cardView.setRadius(5);
                cardView.setCardElevation(8);

                //TextView
                TextView txt = new TextView(getApplicationContext());
                txt.setLayoutParams(params);
                if (currSongString.equals(i.getName())) { // If a song is playing, set different style and text
                    txt.setText(String.format("%s - Playing", currSongString));
                    txt.setTextColor(getColor(R.color.secondary_color));
                } else {
                    txt.setTextColor(getColor(R.color.primary_text));
                    txt.setText(i.getName());
                }
                txt.setTypeface(null, Typeface.BOLD);
                TypedValue out = new TypedValue();
                getApplicationContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, out, true);
                txt.setBackgroundResource(out.resourceId); // sets visual feedback on touch
                txt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.album_default, 0, 0, 0);
                txt.setCompoundDrawablePadding(20);
                txt.setGravity(Gravity.CENTER_VERTICAL);
                txt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        singletonCurr.setCurrSongString(i.getName());
                        filenames = AudioFileReader.getAudioFiles();
                        // Start browse Activity
                        Intent nextAct = new Intent(getApplicationContext(), PlayerAct.class);
                        nextAct.putExtra("files", filenames); //send files to next act
                        nextAct.putExtra("currFile", i); // send current file to next act
                        startActivity(nextAct);
                    }
                });

                cardView.addView(txt);
                parentLayout.addView(cardView);
            }
        } else {
            noTracksText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) { //If permission is 'READ_EXTERNAL_STORAGE'
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) { //if permission not granted
                Toast.makeText(this, "READ PERMISSIONS REQUIRED.", Toast.LENGTH_LONG).show();
            } else {
                createListUi();
            }
        }
    }
}
