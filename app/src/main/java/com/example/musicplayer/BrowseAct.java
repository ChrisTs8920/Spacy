package com.example.musicplayer;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
    ImageButton refreshButton;


    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        this.setContentView(R.layout.browselayout);
        askPerm();
        parentLayout = findViewById(R.id.linearLayout);
        noTracksText = findViewById(R.id.noTracks);
        refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(this);
        createListUi();
    }

    @Override
    public void onClick(View view) {
        if (view == refreshButton) {
            createListUi();
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();

    }

    private void askPerm() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE); //This calls onRequestPermissionsResult()
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

        filenames = AudioFileReader.getAudioFiles(); // if perm granted return song list, else return empty list
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
                cardView.setCardElevation(2);

                //TextView
                TextView txt = new TextView(getApplicationContext());
                txt.setLayoutParams(params);
                txt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Start browse Activity
                        Intent nextAct = new Intent(getApplicationContext(), PlayerAct.class);
                        nextAct.putExtra("files", filenames); //send files to next act
                        nextAct.putExtra("currFile", i); // send current file to next act
                        startActivity(nextAct);
                    }
                });
                txt.setTextColor(getColor(R.color.primary_text));
                txt.setTypeface(null, Typeface.BOLD);
                TypedValue out = new TypedValue();
                getApplicationContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, out, true);
                txt.setBackgroundResource(out.resourceId); // sets visual feedback on touch
                txt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.album_default, 0, 0, 0);
                txt.setCompoundDrawablePadding(20);
                txt.setGravity(Gravity.CENTER_VERTICAL);
                txt.setText(i.getName());

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
            }
        }
    }
}
