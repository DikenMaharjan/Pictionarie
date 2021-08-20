package com.example.pictionarie;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pictionarie.databinding.ActivityGameJoinBinding;
import com.example.pictionarie.model.Player;
import com.example.pictionarie.model.Score;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Random;

import io.socket.client.Ack;
import io.socket.emitter.Emitter;

public class GameJoinActivity extends AppCompatActivity {
    private static final String TAG = "GameJoinActivity";
    ActivityGameJoinBinding binding;
    ProgressDialog creatingDialog;
    ProgressDialog joiningDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameJoinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.createServerTV.setClickable(true);
        binding.joinServerTV.setClickable(true);
        Student s = new Student("Diken", "Maharjan", 5);
        Log.d(TAG, "onCreate: "+ s.getFName());



        binding.enterNameET.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });

        binding.createServerTV.setOnClickListener(v -> {
            if (binding.enterNameET.getText().toString().equals("")) {
                Toast.makeText(GameJoinActivity.this, "Enter your Name", Toast.LENGTH_SHORT).show();
            } else {
                creatingDialog = new ProgressDialog(GameJoinActivity.this);
                creatingDialog.setMessage("Creating Server..");
                creatingDialog.setTitle("Just a moment!!");
                creatingDialog.setIndeterminate(false);
                creatingDialog.setCancelable(true);
                creatingDialog.show();
                connectToServer();
                createCode();
            }
        });

        binding.joinServerTV.setOnClickListener(v -> {
            String name = binding.enterNameET.getText().toString();

            if (name.equals("")) {
                Toast.makeText(GameJoinActivity.this, "Enter your Name",
                        Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(GameJoinActivity.this);
                builder.setTitle("Enter Code: ");
                View layout = View.inflate(GameJoinActivity.this, R.layout.enter_code_layout, null);
                EditText enterCodeET = layout.findViewById(R.id.enterCodeET);
                enterCodeET.setFilters(new InputFilter[]{new InputFilter.AllCaps(),
                        new InputFilter.LengthFilter(6)});
                builder.setView(layout);
                builder.setCancelable(false);
                builder.setPositiveButton("Join", (dialog, which) -> {
                    connectToServer();
                    joiningDialog = new ProgressDialog(GameJoinActivity.this);
                    joiningDialog.setMessage("Joining Server..");
                    joiningDialog.setTitle("Just a moment!!");
                    joiningDialog.setIndeterminate(false);
                    joiningDialog.setCancelable(true);
                    joiningDialog.show();
                    String code = enterCodeET.getText().toString();
                    Server.socket.emit("newCode", code, false, (Ack) args -> {
                        boolean exists = (boolean) args[0];
                        if (exists) {
                            Server.serverCode = code;
                            joinServer(binding.enterNameET.getText().toString(), false, code);

                        } else {
                            runOnUiThread(() -> Toast.makeText(GameJoinActivity.this, "Server Not Found",
                                    Toast.LENGTH_SHORT).show());
                            Server.socket.disconnect();
                        }
                        runOnUiThread(() -> joiningDialog.dismiss());
                    });

                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();


            }
        });

    }

    public void hideKeyboard(View v) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

    }


    void createCode() {
        Random random = new Random();

        StringBuilder ans = new StringBuilder();
        char a;
        int num;
        int character;

        for (int i = 0; i < Server.CODE_LENGTH; i++) {
            num = random.nextInt(2);
            if (num == 0) {
                character = random.nextInt(26);
                a = (char) (character + 'A');
            } else {
                character = random.nextInt(9);
                a = (char) (character + '0');
            }
            ans.append(a);
        }
        String code = ans.toString();
        Server.socket.emit("newCode", code,true,  (Ack) args -> {
            boolean exists = (boolean) args[0];
            if (!exists) {
                Server.serverCode = code;
                joinServer(binding.enterNameET.getText().toString(), true, code);
            } else {
                createCode();
            }
        });


    }

    private void joinServer(String name, boolean host, String code) {
        Server.socket.emit("createPlayer", name, host, code, (Ack) args -> {
            Server.player = Server.gson.fromJson(args[0].toString(), Player.class);
            runOnUiThread(() -> {
                if (joiningDialog != null) {
                    joiningDialog.dismiss();
                    joiningDialog = null;
                }
                if (creatingDialog != null) {
                    creatingDialog.dismiss();
                    creatingDialog = null;
                }
            });
            Intent intent = new Intent(GameJoinActivity.this, lobbyActivity.class);
            startActivity(intent);
            finish();

        });
    }


    private void connectToServer() {
        Server.socket.connect();
    }


}