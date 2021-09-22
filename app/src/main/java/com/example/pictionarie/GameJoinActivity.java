package com.example.pictionarie;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.pictionarie.databinding.ActivityGameJoinBinding;
import com.example.pictionarie.model.Player;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Random;

import static com.example.pictionarie.Server.SERVER_KEY;

public class GameJoinActivity extends AppCompatActivity {
    private static final String TAG = "GameJoinActivity";
    ActivityGameJoinBinding binding;
    boolean validServerCode;
    boolean serverCreated;
    ProgressDialog creatingDialog;
    ProgressDialog joiningDialog;

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseDatabase.getInstance().goOffline();
        FirebaseDatabase.getInstance().goOnline();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameJoinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseDatabase.getInstance().goOffline();
        FirebaseDatabase.getInstance().goOnline();
        binding.createServerTV.setClickable(true);
        binding.joinServerTV.setClickable(true);


        binding.enterNameET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });



        binding.createServerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String name = binding.enterNameET.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(GameJoinActivity.this, "Enter your Name", Toast.LENGTH_SHORT).show();
                } else {

                    creatingDialog = new ProgressDialog(GameJoinActivity.this);
                    creatingDialog.setMessage("Creating Server..");
                    creatingDialog.setTitle("Just a moment!!");
                    creatingDialog.setIndeterminate(false);
                    creatingDialog.setCancelable(true);
                    creatingDialog.show();
                createServer(new Player(name, true, 1));
                }
            }
        });

        binding.joinServerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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


                    builder.setPositiveButton("Join", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            joiningDialog = new ProgressDialog(GameJoinActivity.this);
                            joiningDialog.setMessage("Joining Server..");
                            joiningDialog.setTitle("Just a moment!!");
                            joiningDialog.setIndeterminate(false);
                            joiningDialog.setCancelable(true);
                            joiningDialog.show();
                            String code = enterCodeET.getText().toString();
                            if (!code.equals("")) {
                                Server.databaseReference.child(SERVER_KEY).child(code).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            Integer turn =
                                                    snapshot.child("NumberOfPlayers").getValue(Integer.class);
                                            joinServer(code, new Player(name,false,turn + 1));

                                        } else {
                                            Toast.makeText(GameJoinActivity.this, "Server Not Found",
                                                    Toast.LENGTH_SHORT).show();
                                            joiningDialog.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }else{
                                Toast.makeText(GameJoinActivity.this,"Enter Code",
                                        Toast.LENGTH_SHORT).show();
                                joiningDialog.dismiss();

                            }

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    enterCodeET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_NULL){
                                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                            }
                            return true;
                        }
                    });
                         alertDialog.show();
                }
            }
        });

    }

    public void createServer(Player p) {
        serverCreated = false;
        Server.serverCode = createCode();
        Server.getNumberOfPlayerRef().setValue(1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                serverCreated = false;
                Server.getPlayerRef().child(String.valueOf(p.getTurn())).setValue(p).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            serverCreated = true;
                            Server.player = p;
                                Intent intent = new Intent(GameJoinActivity.this, lobbyActivity.class);
                                startActivity(intent);
                                creatingDialog.dismiss();
                                finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(GameJoinActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
                        }
                    });


            }
        });

    }

    public void hideKeyboard(View v) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

    }

    public String createCode() {
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
        validServerCode = true;
        Server.databaseReference.child(SERVER_KEY).child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    validServerCode = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (!validServerCode) {
            return createCode();
        }
        return code;

    }

    public void joinServer(String code, Player p) {
        Server.serverCode = code;
        Server.getPlayerRef().child(String.valueOf(p.getTurn())).setValue(p).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Server.getNumberOfPlayerRef().setValue(p.getTurn()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Server.player = p;
                        Intent intent = new Intent(GameJoinActivity.this, lobbyActivity.class);
                        joiningDialog.dismiss();
                        startActivity(intent);
                        finish();
                    }
                });

            }
        });



    }


}