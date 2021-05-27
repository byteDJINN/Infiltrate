package com.example.infiltrate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class PlayerSetupActivity extends AppCompatActivity {

    ArrayList<String> playerList = new ArrayList<>();
    PlayerListAdapter rvAdapter;
    EditText playerNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_setup);


        // Get the player list
        if (getIntent().getStringArrayListExtra("PLAYER_LIST") != null) {
            playerList = getIntent().getStringArrayListExtra("PLAYER_LIST");
        }

        playerNameEditText = (EditText)findViewById(R.id.player_name_edit_text);
        rvAdapter = new PlayerListAdapter(playerList);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.player_list_layout);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(rvAdapter);


    }


    /**
     * Click listener for "+" Button
     * @param v - passed from button
     */
    public void onPlusBtnClicked(View v) {

        try {
            if (!playerNameEditText.getText().toString().equals("") &&
                    !playerList.contains(playerNameEditText.getText().toString()) &&
                    !Pattern.compile("\\s").matcher(playerNameEditText.getText().toString()).find()) {
                playerList.add(playerNameEditText.getText().toString());
                rvAdapter.notifyItemInserted(playerList.size() - 1);
                playerNameEditText.setText("");
            } else {Toast.makeText(getApplicationContext(), "Invalid name", Toast.LENGTH_SHORT).show();}
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(), "Invalid name", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBackBtnClicked(View v) {
        startActivity(new Intent(PlayerSetupActivity.this, TitleScreenActivity.class));
    }
    public void onNextBtnClicked(View v) {
        if (playerList.size() >= 2) {
            Intent intent = new Intent(PlayerSetupActivity.this, RoleSetupActivity.class);
            intent.putExtra("PLAYER_LIST", playerList);
            startActivity(intent);
        }
    }
}
