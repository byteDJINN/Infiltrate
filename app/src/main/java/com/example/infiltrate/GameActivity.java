package com.example.infiltrate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    ArrayList<Pair<Game.Role,int[]>> roleRestrictions = new ArrayList<Pair<Game.Role,int[]>>();
    ArrayList<String> playerList = new ArrayList<String>();
    Game g;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        // Get playerList and roleRestrictions
        Intent intent = getIntent();
        int[] maximums = new int[Game.Role.values().length];
        int[] minimums = new int[Game.Role.values().length];
        if (intent.getStringArrayListExtra("PLAYER_LIST") != null) {
            playerList = intent.getStringArrayListExtra("PLAYER_LIST");
        } else {startActivity(new Intent(GameActivity.this,PlayerSetupActivity.class));}
        if (intent.getIntArrayExtra("ROLE_RESTRICTIONS_MAX") != null) {
            maximums = intent.getIntArrayExtra("ROLE_RESTRICTIONS_MAX");
        } else {startActivity(new Intent(GameActivity.this,RoleSetupActivity.class));}
        if (intent.getIntArrayExtra("ROLE_RESTRICTIONS_MIN") != null) {
            minimums = intent.getIntArrayExtra("ROLE_RESTRICTIONS_MIN");
        } else {startActivity(new Intent(GameActivity.this,RoleSetupActivity.class));}
        // Parse through maximums and minimums to form roleRestrictions
        for (int i=0;i<Game.Role.values().length;i++) {
            Pair<Game.Role,int[]> item = new Pair<Game.Role,int[]>(Game.Role.values()[i],
                    new int[]{minimums[i],maximums[i]});
            roleRestrictions.add(item);
        }
        // Construct the game object
        g = new Game(playerList,roleRestrictions);
        updateLayout();
    }

    public void showRole(View v) {

        TextView roleTextView = (TextView)findViewById(R.id.role_tv);
        roleTextView.setVisibility(View.VISIBLE);
        TextView messagesTextView = findViewById(R.id.messages_tv);
        messagesTextView.setVisibility(View.VISIBLE);
        ScrollView scrollView = (ScrollView)findViewById(R.id.radio_group_scroll_view);
        scrollView.setVisibility(View.VISIBLE);

    }

    /**
     * Called when the end turn button is clicked.
     * @param v - The view passed from the onClick function of the button.
     */
    public void nextTurn(View v) { // Happens on End Turn button click
        RadioGroup rg = findViewById(R.id.target_radio_group); // get the RadioGroup
        // get selected radio button from radioGroup
        int selectedId = rg.getCheckedRadioButtonId();
        if (selectedId != -1) {
            // find the radiobutton by returned id
            RadioButton selectedRadioButton = (RadioButton) findViewById(selectedId);

            // Register the selection with the Game class
            g.setSelection(g.getCurrentPlayerName(), selectedRadioButton.getText().toString());
            // Do any necessary actions and end the player's turn
            g.doTurn();
            // Check win
            if (g.checkWin()) endGame(g.getWinningSide());
            // Convert someone to an ordinary role if necessary
            g.balance();
            // Update the layout (BEGINNING OF NEW TURN BLOCK)
            updateLayout();
        } else {showRole(v);}
    }

    /**
     * Updates the entire layout for the next player's turn
     */
    private void updateLayout() {
        // Update the player name text view
        TextView playerNameTextView = findViewById(R.id.player_name_tv);
        playerNameTextView.setText(g.getCurrentPlayerName());
        // Update the role text view
        TextView roleTextView = findViewById(R.id.role_tv);
        roleTextView.setText(g.getCurrentPlayerRole().name());
        if (g.getCurrentPlayerRole().isCitizen) roleTextView.setTextColor(Color.BLUE);
        else roleTextView.setTextColor(Color.RED);

        // Update the messages text view
        TextView messagesTextView = findViewById(R.id.messages_tv);
        messagesTextView.setText(g.getCurrentPlayerMessage());
        messagesTextView.setMovementMethod(new ScrollingMovementMethod());
        // Change visibility
        roleTextView.setVisibility(View.INVISIBLE);
        messagesTextView.setVisibility(View.INVISIBLE);
        ScrollView scrollView = (ScrollView)findViewById(R.id.radio_group_scroll_view);
        scrollView.setVisibility(View.INVISIBLE);
        // Update the radio buttons
        ArrayList<String> legalTargets = g.getLegalTargets(g.getCurrentPlayerName());
        final RadioButton[] rbOptions = new RadioButton[legalTargets.size()];
        RadioGroup rg = findViewById(R.id.target_radio_group); // get the RadioGroup
        rg.clearCheck();
        rg.removeAllViews(); // Remove all the current radio buttons
        rg.setOrientation(RadioGroup.VERTICAL);
        for(int i=0; i<legalTargets.size(); i++){
            rbOptions[i]  = new RadioButton(this);
            rbOptions[i].setText(legalTargets.get(i));
            rbOptions[i].setId(100+i);
            rg.addView(rbOptions[i]);
        }
    }
    private void endGame(String winningSide) {
        Intent intent = new Intent(GameActivity.this,TitleScreenActivity.class);
        intent.putExtra("WINNING_SIDE",winningSide);
        startActivity(intent);
    }


}