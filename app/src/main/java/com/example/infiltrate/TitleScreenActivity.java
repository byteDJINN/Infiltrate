package com.example.infiltrate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class TitleScreenActivity extends AppCompatActivity {

    private String winningSide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);

        if (getIntent().getStringExtra("WINNING_SIDE") != null) {
            winningSide = getIntent().getStringExtra("WINNING_SIDE");
            TextView winningSideTextView = (TextView)findViewById(R.id.winning_side);
            winningSideTextView.setText(winningSide + " WON");
            winningSideTextView.setVisibility(View.VISIBLE);
        }
    }

    public void onPlayBtnClicked(View v) {
        startActivity(new Intent(TitleScreenActivity.this, PlayerSetupActivity.class));
    }
}