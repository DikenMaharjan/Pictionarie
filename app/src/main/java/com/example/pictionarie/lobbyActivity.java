package com.example.pictionarie;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pictionarie.adapters.lobbyPlayerLVA;
import com.example.pictionarie.databinding.ActivityLobbyBinding;
import com.example.pictionarie.model.GameInformation;
import com.example.pictionarie.model.Player;
import com.example.pictionarie.model.Score;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import io.socket.client.Ack;
import io.socket.emitter.Emitter;

public class lobbyActivity extends AppCompatActivity {
    private static final String TAG = "lobbyActivity";
    ActivityLobbyBinding binding;
    ArrayAdapter<Player> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLobbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        adapter = new lobbyPlayerLVA(this, Server.allPlayerList);
        binding.listPlayersView.setAdapter(adapter);

        Server.socket.emit("getAllPlayers", (Ack) ack ->{
            try {
                JSONObject jsonObject = new JSONObject(ack[0].toString());
                Iterator<String> keys = jsonObject.keys();
                Server.allPlayerList.clear();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    Player p = Server.gson.fromJson(String.valueOf(jsonObject.getJSONObject(key)),
                            Player.class);
                    Server.allPlayerList.add(p);
                }
                runOnUiThread(() -> adapter.notifyDataSetChanged());


            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        Server.socket.on("beHost", (args) ->{
            Server.player.host = true;
            runOnUiThread(() -> {
                removeListeners();
                makeHostView();

            });

        });
        Server.socket.on("allPlayers" , args -> {
            try {
                JSONObject jsonObject = new JSONObject(args[0].toString());
                Iterator<String> keys = jsonObject.keys();
                Server.allPlayerList.clear();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    Player p = Server.gson.fromJson(String.valueOf(jsonObject.getJSONObject(key)),
                            Player.class);
                    Server.allPlayerList.add(p);
                }
                runOnUiThread(() -> adapter.notifyDataSetChanged());


            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        if (Server.player.host){
            makeHostView();
        }else{
            initializeListeners();
        }
        showServerCode();
        serverCodeCopyEnabler();
    }

    private void removeListeners() {
        Server.socket.off("roundsChanged");
        Server.socket.off("timeChanged");
        Server.socket.off("startChanged");
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


    private void initializeListeners() {
        binding.timeSpinner.setEnabled(false);
        binding.roundSpinner.setEnabled(false);
        binding.startButton.setClickable(false);
        binding.startButton.setText("Wait for the host");
        Server.socket.emit("getGameInformation", (Ack) args -> {
            GameInformation gameInformation = Server.gson.fromJson(args[0].toString(),
                    GameInformation.class);
            runOnUiThread(() -> {
                binding.roundSpinner.setSelection(gameInformation.getRounds());
                binding.timeSpinner.setSelection(gameInformation.getTime());
            });

            if (gameInformation.isStarted()){
                startGame();
            }
        });
        Server.socket.on("roundsChanged", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(()->{
                    binding.roundSpinner.setSelection((int) args[0]);
                });
            }
        });
        Server.socket.on("timeChanged", new Emitter.Listener(){
            @Override
            public void call(Object... args) {
                runOnUiThread(()->{
                    binding.timeSpinner.setSelection((int) args[0]);
                });
            }

        });
        Server.socket.on("startChanged", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if ((boolean) args[0]){
                    removeListeners();
                    startGame();
                }
            }
        });

    }

    private void startGame() {
        Server.socket.off("beHost");
        Intent intent = new Intent(lobbyActivity.this, GameActivity.class);
        startActivity(intent);
        finish();
    }

    private void makeHostView() {
        binding.roundSpinner.setEnabled(true);
        binding.timeSpinner.setEnabled(true);
        binding.startButton.setClickable(true);
        binding.startButton.setText("Start Game");
        Server.socket.emit("setGameInformation",
                Server.gson.toJson(new GameInformation(binding.roundSpinner.getSelectedItemPosition(),
                binding.timeSpinner.getSelectedItemPosition(),
                false)));

        binding.roundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Server.socket.emit("rounds", position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Server.socket.emit("time", position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Server.allPlayerList.size() > 1){
                Server.socket.emit("startGame", true);
                startGame();
            }else{
                Toast.makeText(lobbyActivity.this, "Not Enough Players", Toast.LENGTH_SHORT).show();
            }
            }

        });
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
    private void returnToHome() {
        removeListeners();
        Server.socket.off("beHost");
        Server.socket.off("allPlayers");
        Server.socket.off("totalScores");
        Server.socket.disconnect();
        Intent intent = new Intent(lobbyActivity.this, GameJoinActivity.class);
        startActivity(intent);
        finish();
    }

}