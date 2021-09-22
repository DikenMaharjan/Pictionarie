package com.example.pictionarie;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pictionarie.adapters.lobbyPlayerLVA;
import com.example.pictionarie.databinding.ActivityLobbyBinding;
import com.example.pictionarie.model.CurrentGameState;
import com.example.pictionarie.model.GameInformation;
import com.example.pictionarie.model.Player;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

public class lobbyActivity extends AppCompatActivity {
    private static final String TAG = "lobbyActivity";
    ActivityLobbyBinding binding;
    ArrayAdapter<Player> adapter;
    ValueEventListener gameInformationListener;
    ValueEventListener numberOfPlayersListener;
    ChildEventListener allPlayerListener;
    ValueEventListener playerListUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLobbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeAllListeners();

        adapter = new lobbyPlayerLVA(this, Server.playerList);
        binding.listPlayersView.setAdapter(adapter);

        Server.getNumberOfPlayerRef().addValueEventListener(numberOfPlayersListener);
        Server.getPlayerRef().addChildEventListener(allPlayerListener);
        Server.getPlayerRef().addValueEventListener(playerListUpdater);

        showServerCode();
        serverCodeCopyEnabler();

        if (Server.player.host) {
            makeHostView();
        } else
            {
            binding.timeSpinner.setEnabled(false);
            binding.roundSpinner.setEnabled(false);
            binding.startButton.setClickable(false);
            binding.startButton.setText("Wait for the host");
            Server.getGameInformationReference().addValueEventListener(gameInformationListener);
        }


    }

    private void makeHostView() {
        binding.roundSpinner.setEnabled(true);
        binding.timeSpinner.setEnabled(true);
        binding.startButton.setClickable(true);
        binding.startButton.setText("Start Game");
        binding.roundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Server.getGameInformationReference().setValue(new GameInformation(
                        position,
                        binding.timeSpinner.getSelectedItemPosition(),
                        false)
                );

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Server.getGameInformationReference().setValue(new GameInformation(
                        binding.roundSpinner.getSelectedItemPosition(),
                        position,
                        false
                ));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { if (Server.playerList.size() > 1){
                Server.getGameInformationReference().setValue(new GameInformation(
                        binding.roundSpinner.getSelectedItemPosition(),
                        binding.timeSpinner.getSelectedItemPosition(),
                        true
                ));
                Server.getCurrentStateReference().setValue(new CurrentGameState(1, 1,
                        Server.player.getName()));
                Intent intent = new Intent(lobbyActivity.this, GameActivity.class);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(lobbyActivity.this, "Not Enough Players", Toast.LENGTH_SHORT).show();
            }
            }

        });
    }

    private void initializeAllListeners() {
        numberOfPlayersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer numberOfPlayers = snapshot.getValue(Integer.class);
                if (numberOfPlayers != null) {
                    Server.numberOfPlayers = numberOfPlayers;
                    if (numberOfPlayers == 1) {
                        Server.getPlayerRef().child(String.valueOf(Server.player.getTurn())).child(
                                "disconnected").onDisconnect().cancel();
                        Server.getNumberOfPlayerRef().onDisconnect().cancel();
                        Server.getDatabaseReference().onDisconnect().removeValue();
                    } else {
                        Server.getDatabaseReference().onDisconnect().cancel();
                        Server.getPlayerRef().child(String.valueOf(Server.player.getTurn())).child(
                                "disconnected").onDisconnect().setValue(true);
                        Server.getNumberOfPlayerRef().onDisconnect().setValue(numberOfPlayers - 1);
                        Server.getPlayerRef().child(String.valueOf(numberOfPlayers)).onDisconnect().removeValue();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        gameInformationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GameInformation gameInformation = snapshot.getValue(GameInformation.class);
                if (gameInformation != null) {
                    binding.timeSpinner.setSelection(gameInformation.getTime());
                    binding.roundSpinner.setSelection(gameInformation.getRounds());
                    if (gameInformation.isStarted()) {
                        Intent intent = new Intent(lobbyActivity.this, GameActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }else{
                    showConnectionLost();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };
        allPlayerListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Player disconnectedPlayer = snapshot.getValue(Player.class);
                if (disconnectedPlayer != null){
                if (disconnectedPlayer.disconnected) {
                    Server.playerList.remove(disconnectedPlayer.getTurn() - 1);
                    if (disconnectedPlayer.getTurn() < Server.player.getTurn()) {
                        if (Server.player.getTurn() == 2) {
                            Server.player.host = true;
                        }
                        Server.player.setTurn(Server.player.getTurn() - 1);
                        Server.getPlayerRef().child(String.valueOf(disconnectedPlayer.getTurn())).setValue(Server.player);

                    }
                    if (Server.player.host) {
                        makeHostView();
                    }
                }
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        playerListUpdater = new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Server.playerList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Server.playerList.add(dataSnapshot.getValue(Player.class));
                    Server.turnPlayerHashmap.put(dataSnapshot.getKey(),
                            (dataSnapshot.getValue(Player.class)).getName());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };



    }

    private void showConnectionLost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server Disconnected");
        builder.setMessage("Return to Home");
        builder.setPositiveButton("Return", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                returnToHome();

            }
        });
    }

    private void returnToHome() {
        disconnectFromServer();
        Intent intent = new Intent(lobbyActivity.this, GameJoinActivity.class);
        startActivity(intent);
        finish();
    }

    private void serverCodeCopyEnabler() {
        binding.serverCodeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager =
                        (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData code = ClipData.newPlainText("code", Server.serverCode);
                clipboardManager.setPrimaryClip(code);
                Toast.makeText(lobbyActivity.this, "Copied", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showServerCode() {
        binding.serverCodeTV.setText(String.format("%s   ", Server.serverCode));
    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure?").setMessage("Do you want to leave the game lobby?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        returnToHome();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }


    private void disconnectFromServer(){
        Server.getGameInformationReference().removeEventListener(gameInformationListener);
        Server.getNumberOfPlayerRef().removeEventListener(numberOfPlayersListener);
        Server.getPlayerRef().removeEventListener(allPlayerListener);
        FirebaseDatabase.getInstance().goOffline();
        Server.clear();

    }
}