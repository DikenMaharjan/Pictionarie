package com.example.pictionarie;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pictionarie.adapters.FinalScoreRVA;
import com.example.pictionarie.adapters.MessagesRVA;
import com.example.pictionarie.adapters.ScoreViewRVA;
import com.example.pictionarie.databinding.ActivityGameBinding;
import com.example.pictionarie.model.Messages;
import com.example.pictionarie.model.Score;
import com.example.pictionarie.views.AvailableColor;
import com.example.pictionarie.views.DrawWindow;
import com.example.pictionarie.views.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import io.socket.client.Ack;

public class GameActivity extends AppCompatActivity {
    //</editor-fold>
    private static final String TAG = "GameActivity";
    //<editor-fold desc="Colors">
    int[] topColors = {
            Color.rgb(0, 0, 0),
            Color.rgb(127, 127, 127),
            Color.rgb(136, 0, 21),
            Color.rgb(237, 28, 36),
            Color.rgb(255, 127, 39),
            Color.rgb(255, 242, 0),
            Color.rgb(34, 177, 76),
            Color.rgb(0, 162, 242),
            Color.rgb(63, 72, 204),
            Color.rgb(163, 73, 164),
    };
    int[] bottomColors = {
            Color.rgb(255, 255, 255),
            Color.rgb(195, 195, 195),
            Color.rgb(185, 122, 87),
            Color.rgb(255, 174, 201),
            Color.rgb(255, 201, 14),
            Color.rgb(239, 228, 176),
            Color.rgb(181, 230, 29),
            Color.rgb(153, 217, 224),
            Color.rgb(112, 146, 190),
            Color.rgb(200, 191, 231)
    };
    int[][] colors = {topColors, bottomColors};
    ActivityGameBinding binding;
    View chooseWordView;
    View drawerIsChoosingView;
    View scoreView;
    View.OnClickListener optionOnClickListener;


    float density;
    int marginsColorPalette = 10;
    int colorPaletteSize;

    DrawWindow drawWindow;

    Tools selectedTool;
    Tools pencilTool;
    Tools fillColorTool;
    AvailableColor selectedColor;

    MessagesRVA messagesRVA;

    ScoreViewRVA scoresRVA;

    AlertDialog finalScoreDialog;


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        density = getResources().getDisplayMetrics().density;
        colorPaletteSize = (int) ((density * 50 + 0.5) - 2 * marginsColorPalette);

        drawWindow = new DrawWindow(this);
        drawWindow.enabled = false;
        binding.paintingWindow.addView(drawWindow);

        addAvailableTools();
        addAvailableColors();

        setSeekBarOnClickListener();

        setOnClickListener();

        messagesRVSetter();

        scoreRVSetter();

        optionOnClickListener = v -> {
            Server.socket.emit("wordChosen", ((TextView) v).getText());
            binding.paintingWindow.removeView(chooseWordView);
            binding.hintText.setText("Chosen Word:");
            binding.hintTextView.setText(((TextView) v).getText());
            chooseWordView = null;
        };

        Server.socket.on("beHost", (args) ->{
            Server.player.host = true;
        });

        Server.socket.emit("getScores", (Ack) ack -> {
            try {
                JSONObject jsonObject = new JSONObject(ack[0].toString());
                Iterator<String> keys = jsonObject.keys();
                Server.totalScores.clear();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Score s = Server.gson.fromJson(String.valueOf(jsonObject.getJSONObject(key)),
                            Score.class);
                    Server.totalScores.add(s);
                }
                runOnUiThread(() -> {
                    scoresRVA.notifyDataSetChanged();
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });


        Server.socket.on("makeAllAnswerIncorrect", args -> {
            for (int i = 0; i < Server.totalScores.size(); i++) {
                Server.totalScores.get(i).setAnswered(false);
            }
            runOnUiThread(() -> {
                scoresRVA.notifyDataSetChanged();
            });
        });
        Server.socket.on("showFinalScore", args -> {
            runOnUiThread(() -> {
                RecyclerView recyclerView = new RecyclerView(this);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
                FinalScoreRVA finalScoreRVA = new FinalScoreRVA(this);
                recyclerView.setAdapter(finalScoreRVA);
                recyclerView.setPadding(0, 10, 0, 0);
                Collections.sort(Server.totalScores, new Comparator<Score>() {
                    @Override
                    public int compare(Score lhs, Score rhs) {
                        if (lhs.getScore() > rhs.getScore()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
                finalScoreRVA.notifyDataSetChanged();
                recyclerView.setLayoutManager(gridLayoutManager);
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
                builder.setView(recyclerView);
                builder.setCancelable(false);
                builder.setTitle("Final Score");
                finalScoreDialog = builder.create();
                finalScoreDialog.show();

            });

        });
        Server.socket.on("restartGame", arg -> {
            finalScoreDialog.dismiss();
            for (String listener : MyListeners.gameActivityListenersList) {
                Server.socket.off(listener);
            }
            drawWindow.removeListeners();
            Intent intent = new Intent(GameActivity.this, lobbyActivity.class);
            startActivity(intent);
            finish();
        });
        Server.socket.on("answerCorrect", args -> {
            String id = args[0].toString();
            for (int i = 0; i < Server.totalScores.size(); i++) {
                if (Server.totalScores.get(i).getId().equals(id)) {
                    Server.totalScores.get(i).setAnswered(true);
                    break;
                }
            }
            runOnUiThread(() -> {
                scoresRVA.notifyDataSetChanged();
            });
        });

        Server.socket.on("totalScores", args -> {
            try {
                JSONObject jsonObject = new JSONObject(args[0].toString());
                Iterator<String> keys = jsonObject.keys();
                Server.totalScores.clear();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Score s = Server.gson.fromJson(String.valueOf(jsonObject.getJSONObject(key)),
                            Score.class);
                    Server.totalScores.add(s);
                }
                runOnUiThread(() -> {
                    scoresRVA.notifyDataSetChanged();
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        Server.socket.on("timeFinished", args -> {
            Server.socket.emit("wordChosen",
                    ((TextView) chooseWordView.findViewById(R.id.option1)).getText());
            runOnUiThread(() -> {
                binding.paintingWindow.removeView(chooseWordView);
                binding.hintText.setText("Chosen Word:");
                binding.hintTextView.setText(((TextView) chooseWordView.findViewById(R.id.option1)).getText());
                chooseWordView = null;
            });

        });
        Server.socket.on("roundInformation", args -> {
            String currentRound = args[1].toString();
            String totalRound = args[2].toString();
            runOnUiThread(() -> {
                binding.drawerTV.setText(args[0].toString());
                binding.roundsTV.setText(String.format("%s/%s", currentRound, totalRound));


            });
        });
        Server.socket.on("showScore", args -> {
            drawWindow.enabled = false;
            try {
                JSONObject score = new JSONObject(args[0].toString());
                Log.e(TAG, score.toString());
                JSONArray nameArray = score.getJSONArray("nameList");
                JSONArray scoreArray = score.getJSONArray("scoreList");

                List<Integer> listOfScores = new ArrayList<>();
                List<String> playerList = new ArrayList<>();
                for (int index = 0; index < scoreArray.length(); index++) {
                    int i = 0;
                    while (true) {
                        if (i == listOfScores.size() || listOfScores.get(i) < Integer.parseInt(scoreArray.get(index).toString())) {
                            listOfScores.add(i, Integer.parseInt(scoreArray.get(index).toString()));
                            playerList.add(i, nameArray.get(i).toString());
                            break;
                        } else {
                            i++;
                        }
                    }
                }

if(args[1] != null) {
    showScore(listOfScores, playerList, args[1].toString());
}else{
    showScore(listOfScores,playerList, null);
}
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        Server.socket.on("gameTimer", args -> {
            runOnUiThread(() -> binding.timeTV.setText(args[0].toString()));
        });
        Server.socket.on("chooseWordTimer", args -> {
            runOnUiThread(() -> binding.timeTV.setText(args[0].toString()));
        });
        Server.socket.on("gameFinished", args -> {
            finish();
        });
        Server.socket.on("chooseWord", args -> runOnUiThread(() -> {
            if (scoreView != null) {
                binding.paintingWindow.removeView(scoreView);
                scoreView = null;
            }
            if (Server.player.getSocketId().equals(args[0].toString())) {
                addChooseWordView(args[1].toString(), args[2].toString(), args[3].toString());
            } else {
                addDrawerIsChoosingView();
            }
        }));
        Server.socket.on("gameStarted", args -> {
            if (drawerIsChoosingView != null) {
                runOnUiThread(() -> {
                    binding.paintingWindow.removeView(drawerIsChoosingView);
                    drawerIsChoosingView = null;
                });

            }
            if (Server.player.getSocketId().equals(args[0].toString())) {
                /*StartDrawing*/
                drawWindow.selectPencil();
                drawWindow.enabled = true;
                drawWindow.startDrawing();
            } else {
                /*StartReceiving*/
                drawWindow.enabled = true;
                drawWindow.startReceiving();
            }

        });
        Server.socket.on("yourMessage", args -> {
            Messages messages = Server.gson.fromJson(args[0].toString(), Messages.class);
            Server.messagesList.add(messages);
            runOnUiThread(() -> {
                messagesRVA.notifyDataSetChanged();
                binding.messageRV.smoothScrollToPosition(Server.messagesList.size());
            });
        });
        Server.socket.on("yourHint", args -> {
            if (!Server.player.getSocketId().equals(args[1].toString())) {
                runOnUiThread(() -> {
                    binding.hintTextView.setText(args[0].toString());
                    binding.hintText.setText("HINT:");
                });

            }
        });
        Log.e(TAG, "onCreate: ");
        Server.socket.emit("ready");
        Server.player.ready = true;
    }


    private void setOnClickListener() {
        binding.sendButton.setOnClickListener(v -> {
            String message = binding.messageET.getText().toString();
            if (!message.equals("")) {
                Server.socket.emit("message", message, binding.timeTV.getText());
                binding.messageET.setText("");
            }
        });
        binding.messageET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.messageRV.smoothScrollToPosition(Server.messagesList.size());
            }
        });
    }

    private void addDrawerIsChoosingView() {
        drawerIsChoosingView = View.inflate(GameActivity.this, R.layout.drawer_is_choosing_layout
                , null);
        ((TextView) drawerIsChoosingView.findViewById(R.id.chooseText)).setText(String.format("%s" +
                " is choosing a word.", binding.drawerTV.getText()));
        binding.paintingWindow.addView(drawerIsChoosingView);
    }

    private void addChooseWordView(String option1, String option2, String option3) {
        chooseWordView = View.inflate(GameActivity.this, R.layout.choose_word_layout, null);
        binding.paintingWindow.addView(chooseWordView);
        ((TextView) chooseWordView.findViewById(R.id.option1)).setText(option1);
        chooseWordView.findViewById(R.id.option1).setOnClickListener(optionOnClickListener);

        ((TextView) chooseWordView.findViewById(R.id.option2)).setText(option2);
        chooseWordView.findViewById(R.id.option2).setOnClickListener(optionOnClickListener);

        ((TextView) chooseWordView.findViewById(R.id.option3)).setText(option3);
        chooseWordView.findViewById(R.id.option3).setOnClickListener(optionOnClickListener);


    }

    private void addAvailableTools() {
        Bitmap pencilBitmap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_pencil);
        Tools pencilView = new Tools(this, pencilBitmap);
        selectedTool = pencilView;
        pencilTool = pencilView;
        drawWindow.selectPencil();
        pencilView.setOnClickListener(v -> {
            drawWindow.selectPencil();
            select_tool((Tools) v);
        });
        addView(pencilView, binding.toolContainer);
        pencilView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.selected_animation));

        Bitmap eraserBitmap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_eraser);
        Tools eraserView = new Tools(this, eraserBitmap);
        eraserView.setOnClickListener(v -> {
            drawWindow.selectEraser();
            select_tool((Tools) v);
        });
        addView(eraserView, binding.toolContainer);


        Bitmap colorFillBitmap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_color_fill);
        Tools colorFillView = new Tools(this, colorFillBitmap);
        fillColorTool = colorFillView;
        colorFillView.setOnClickListener(v -> {
            drawWindow.selectFillColor();
            select_tool((Tools) v);
        });
        addView(colorFillView, binding.toolContainer);

        Bitmap clearBitmap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_clear);
        Tools clearView = new Tools(this, clearBitmap);
        addView(clearView, binding.toolContainer);
        clearView.setOnClickListener(v -> {
            if (drawWindow.drawing) {
                drawWindow.clear();
            }

            Animation selected_animation = AnimationUtils.loadAnimation(GameActivity.this,
                    R.anim.selected_animation);
            Animation dis_selected_animation = AnimationUtils.loadAnimation(GameActivity.this,
                    R.anim.dis_selected_animation);
            v.startAnimation(selected_animation);
            v.startAnimation(dis_selected_animation);

        });

        Bitmap undoBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_undo);
        Tools undoView = new Tools(this, undoBitmap);
        undoView.setOnClickListener(v -> {
            if (drawWindow.drawing) {
                drawWindow.undo();
            }

            Animation selected_animation = AnimationUtils.loadAnimation(GameActivity.this,
                    R.anim.selected_animation);
            Animation dis_selected_animation = AnimationUtils.loadAnimation(GameActivity.this,
                    R.anim.dis_selected_animation);
            v.startAnimation(selected_animation);
            v.startAnimation(dis_selected_animation);
        });
        addView(undoView, binding.toolContainer);


    }

    private void addView(View v, LinearLayout layout) {
        LinearLayout.LayoutParams temp = new LinearLayout.LayoutParams(colorPaletteSize, colorPaletteSize);
        temp.setMargins(marginsColorPalette, marginsColorPalette, marginsColorPalette, marginsColorPalette);
        v.setLayoutParams(temp);
        layout.addView(v);
    }

    private void setSeekBarOnClickListener() {
        binding.seekBar.setMax(200);
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress > 3 && progress < 200) {
                        drawWindow.changeStrokeWidth(progress);
                        drawWindow.pencilSizeShowing = true;
                        drawWindow.invalidate();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.seekBar.setOnTouchUp(() -> {
            drawWindow.pencilSizeShowing = false;
            drawWindow.invalidate();
        });
    }

    private void addAvailableColors() {
        int margin = 5;
        for (int[] rowColor : colors) {
            LinearLayout layout = new LinearLayout(this);
            layout.setLayoutParams(new LinearLayout.LayoutParams(colorPaletteSize / 2,
                    LinearLayout.LayoutParams.MATCH_PARENT
            ));
            layout.setOrientation(LinearLayout.VERTICAL);
            binding.colorContainer.addView(layout);
            for (int color : rowColor) {
                View v = new AvailableColor(this, color);
                int size = colorPaletteSize / 2 - 2 * margin;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(margin, margin, margin, margin);
                v.setLayoutParams(params);
                v.setOnClickListener(v1 -> {
                    drawWindow.selectColor(((AvailableColor) v1).color);
                    if (drawWindow.fillColorSelected) {
                        select_tool(fillColorTool);
                        drawWindow.selectFillColor();
                    } else {
                        select_tool(pencilTool);
                        drawWindow.selectPencil();
                    }

                    if (selectedColor != null) {
                        Animation dis_selected_animation =
                                AnimationUtils.loadAnimation(GameActivity.this,
                                        R.anim.dis_selected_animation);
                        selectedColor.startAnimation(dis_selected_animation);
                    }
                    selectedColor = (AvailableColor) v1;
                    Animation selected_animation =
                            AnimationUtils.loadAnimation(GameActivity.this,
                                    R.anim.selected_animation);
                    selectedColor.startAnimation(selected_animation);

                });
                layout.addView(v);
            }

        }
    }

    private void select_tool(Tools v) {
        selectedTool.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.dis_selected_animation));
        selectedTool = v;
        selectedTool.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.selected_animation));
    }

    private void messagesRVSetter() {
        messagesRVA = new MessagesRVA(this, Server.messagesList);
        binding.messageRV.setAdapter(messagesRVA);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        binding.messageRV.setLayoutManager(linearLayoutManager);
        binding.messageRV.smoothScrollToPosition(Server.messagesList.size());
    }

    private void scoreRVSetter() {
        scoresRVA = new ScoreViewRVA(this, Server.totalScores);
        binding.playersRV.setAdapter(scoresRVA);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);

        binding.playersRV.setLayoutManager(linearLayoutManager);

    }

    private void showScore(List<Integer> scoreList, List<String> playerList, String
            word) {

        scoreView = View.inflate(this, R.layout.score_layout, null);
        for (int i = 0; i < scoreList.size(); i++) {
            TextView playerName = new TextView(this);
            playerName.setText(playerList.get(i));
            ((LinearLayout) scoreView.findViewById(R.id.playerNameTV)).addView(playerName);
            TextView score = new TextView(this);
            score.setText(String.format(": +%s", scoreList.get(i)));
            if (scoreList.get(i) == 0) {
                score.setTextColor(Color.RED);
            } else {
                score.setTextColor(Color.GREEN);
            }
            ((LinearLayout) scoreView.findViewById(R.id.finalScoreTV)).addView(score);
        }
        ((TextView) scoreView.findViewById(R.id.chosenWordTV)).setText(word);
        runOnUiThread(() -> {
            binding.paintingWindow.addView(scoreView);
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure?").setMessage("Do you want to leave the game?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        drawWindow.removeListeners();
                        for (String listener : MyListeners.gameActivityListenersList) {
                            Server.socket.off(listener);
                        }
                        Server.socket.off("allPlayers");
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

        Server.socket.disconnect();
        Intent intent = new Intent(GameActivity.this, GameJoinActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        drawWindow.removeListeners();
        for (String listener : MyListeners.gameActivityListenersList) {
            Server.socket.off(listener);
        }
        super.onDestroy();
    }
}