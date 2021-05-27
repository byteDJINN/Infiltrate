package com.example.infiltrate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.view.View;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

public class RoleSetupActivity extends AppCompatActivity {

    ArrayList<Pair<Game.Role,int[]>> roleRestrictions = new ArrayList<Pair<Game.Role,int[]>>();
    ArrayList<String> playerList = new ArrayList<String>();
    RoleListAdapter rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_setup);


        // Get the player list
        if (getIntent().getStringArrayListExtra("PLAYER_LIST") != null) {
            playerList = getIntent().getStringArrayListExtra("PLAYER_LIST");
        }
        rvAdapter = new RoleListAdapter(roleRestrictions);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.role_list_layout);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(rvAdapter);


        for (Game.Role role : Game.Role.values()) {
            roleRestrictions.add(new Pair<Game.Role,int[]>(role,new int[] {0,0}));
            rvAdapter.notifyItemInserted(roleRestrictions.size() - 1);
        }

    }

    public void onBackBtnClicked(View v) {
        Intent intent = new Intent(RoleSetupActivity.this,PlayerSetupActivity.class);
        intent.putExtra("PLAYER_LIST",playerList);
        startActivity(intent);
    }
    public void onNextBtnClicked(View v) {
        if (isLegalRoleRestrictions()) {
            Intent intent = new Intent(RoleSetupActivity.this, GameActivity.class);

            // Generate the parcelable role restrictions
            int[] minimums = new int[roleRestrictions.size()];
            int[] maximums = new int[roleRestrictions.size()];
            for (Pair<Game.Role, int[]> i : roleRestrictions) {
                minimums[i.first.ordinal()] = i.second[0];
                maximums[i.first.ordinal()] = i.second[1];
            }

            intent.putExtra("ROLE_RESTRICTIONS_MAX", maximums);
            intent.putExtra("ROLE_RESTRICTIONS_MIN", minimums);
            intent.putStringArrayListExtra("PLAYER_LIST", playerList);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Invalid values", Toast.LENGTH_SHORT).show();
        }
    }
    public boolean isLegalRoleRestrictions() {
        boolean legal = true;
        int minSumAlive = 0;
        int maxSumAlive = 0;
        int minSumDead = 0;
        int maxSumDead = 0;
        int infiltrators = 0;
        int citizens = 0;

        for (Pair<Game.Role,int[]> i : roleRestrictions) {
            if (i.second[0]>i.second[1]) {
                legal = false;
            }
            if (i.first.isAlive) {
                if (i.first.isCitizen) citizens+=i.second[1];
                else infiltrators+=i.second[1];
                minSumAlive += i.second[0];
                maxSumAlive += i.second[1];
            } else {
                if (i.first.isCitizen) citizens-=i.second[1];
                else infiltrators-=i.second[1];
                minSumDead += i.second[0];
                maxSumDead += i.second[1];
            }

        }
        legal = legal && minSumAlive <= playerList.size() && maxSumAlive >= playerList.size();
        legal = legal && minSumDead  <= playerList.size() && maxSumDead  >= playerList.size();
        legal = legal && citizens <= 0 && infiltrators <= 0;
        return legal;
    }
}