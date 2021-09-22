package com.example.pictionarie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pictionarie.adapters.MessagesRVA;
import com.example.pictionarie.adapters.PlayersRVA;
import com.example.pictionarie.databinding.ActivityGameBinding;
import com.example.pictionarie.model.CurrentGameState;
import com.example.pictionarie.model.GameInformation;
import com.example.pictionarie.model.Messages;
import com.example.pictionarie.model.Score;
import com.example.pictionarie.views.AvailableColor;
import com.example.pictionarie.views.DrawWindow;
import com.example.pictionarie.views.Tools;
import com.example.pictionarie.views.VerticalSeekBar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    ActivityGameBinding binding;

    GameInformation gameInformation;
    int totalRounds;
    int totalTime;

    int hintCount = 0;

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
    //</editor-fold>

    DrawWindow drawWindow;
    List<String> threeWordList = new ArrayList<>();
    String chosenWord;

    ValueEventListener currentWordListener;
    ValueEventListener hintsListener;

    CurrentGameState currentGameState;


    View drawerIsChoosingView;
    View chooseWordView;
    View calculatingScoreView;
    View scoreView;

    View.OnClickListener optionOnClickListener;

    long remainingTime;

    CountDownTimer chooseTimer;
    CountDownTimer gameTimer;
    CountDownTimer scoreTimer;

    List<Messages> messagesArrayList;
    MessagesRVA messagesRVA;

    PlayersRVA playersRVA;

    float density;
    int marginsColorPalette = 10;
    int colorPaletteSize;

    Tools selectedTool;
    Tools pencilTool;
    Tools fillColorTool;

    AvailableColor selectedColor;

    String hint;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        density = getResources().getDisplayMetrics().density;
        colorPaletteSize = (int) ((density * 50 + 0.5) - 2 * marginsColorPalette);


        drawWindow = new DrawWindow(this, false);
        drawWindow.disable();
        binding.paintingWindow.addView(drawWindow);


        addAvailableTools();
        addAvailableColors();

        setSeekBarOnClickListener();

        messagesRVSetter();
        setMessageListener();

        playerRVSetter();
        setPlayersListener();

        setOnClickListener();

        Server.getAnswersRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() == Server.numberOfPlayers) {
                    drawWindow.disable();
                    gameTimer.cancel();
                    List<Long> listOfScores = new ArrayList<>();
                    List<String> playerList = new ArrayList<>();
                    int numberOfAnswers = -1;
                    for (DataSnapshot data : snapshot.getChildren()) {
                        numberOfAnswers += 1;
                        String player = data.getKey();
                        Score score = data.getValue(Score.class);
                        assert score != null;
                        long seconds = score.getSeconds();
                        if (listOfScores.isEmpty()) {
                            listOfScores.add(seconds);
                            playerList.add(player);
                        } else {
                            int i = 0;
                            while (true) {
                                if (i == listOfScores.size() || listOfScores.get(i) < seconds) {
                                    listOfScores.add(i, seconds);
                                    playerList.add(i, player);
                                    break;
                                } else {
                                    i++;
                                }
                            }
                        }
                    }
                    List<Integer> scoreList = new ArrayList<>();
                    for (int i = 0; i < listOfScores.size() - 1; i++) {
                        int s;
                        if (listOfScores.get(i) == 0) {
                            s = 0;
                        } else {
                            s =
                                    Math.round((float) (listOfScores.get(i) + (totalTime * 1000) / 2) / (totalTime * 1000) * 320 - (i * 25));

                        }
                        scoreList.add(s);
                    }
                    int drawerScore;
                    if (scoreList.isEmpty()) {
                        drawerScore = 0;

                    } else {
                        drawerScore =
                                Math.round(((float) numberOfAnswers / Server.numberOfPlayers) * scoreList.get(0));
                    }
                    int i = 0;
                    while (true) {
                        if (i == scoreList.size() || drawerScore > scoreList.get(i)) {
                            scoreList.add(i, drawerScore);
                            playerList.add(i, playerList.get(playerList.size() - 1));
                            playerList.remove(playerList.size() - 1);
                            break;
                        }
                        i++;
                    }
                    showScore(scoreList, playerList);


                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        setTimer();


        optionOnClickListener = v -> {
            chosenWord = ((TextView) v).getText().toString();
            chooseTimer.onFinish();
        };

        currentWordListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String word = snapshot.getValue(String.class);
                if (word != null) {
                    chosenWord = word;
                    binding.paintingWindow.removeView(drawerIsChoosingView);
                    gameTimer.start();
                    drawWindow.enabled = true;
                    drawWindow.startReceiving();
                    hint = dottedWord(chosenWord);
                    binding.hintText.setText("Hints:");
                    binding.hintTextView.setText(hint);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        hintsListener = new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapShot){
                String hints = snapShot.getValue(String.class);
                if (hints != null){
                    binding.hintText.setText("Hints:");
                    binding.hintTextView.setText(hints);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error){

            }
        };


        Server.getGameInformationReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gameInformation = snapshot.getValue(GameInformation.class);
                String[] rounds = getResources().getStringArray(R.array.rounds);
                String[] time = getResources().getStringArray(R.array.times_seconds);
                totalRounds = Integer.parseInt(rounds[gameInformation.getRounds()]);
                totalTime = Integer.parseInt(time[gameInformation.getTime()]);
                setTimer();
                Server.getCurrentStateReference().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        resetGame();
                        currentGameState = snapshot.getValue(CurrentGameState.class);
                        if (currentGameState != null) {
                            if (currentGameState.getRound() <= totalRounds) {
                                binding.roundsTV.setText(String.valueOf(currentGameState.getRound()));
                                binding.drawerTV.setText(currentGameState.getName());
                                if (Server.player.getTurn() == currentGameState.getTurn()) {
                                    chooseWords();
                                } else {
                                    drawerIsChoosingView = View.inflate(GameActivity.this,
                                            R.layout.drawer_is_choosing_layout, null);
                                    ((TextView) drawerIsChoosingView.findViewById(R.id.chooseText)).setText(currentGameState.getName() + " is choosing a word.");
                                    binding.paintingWindow.addView(drawerIsChoosingView);
                                    Server.getCurrentWordReference().addValueEventListener(currentWordListener);
                                    Server.getHintReference().addValueEventListener(hintsListener);
                                }
                            } else {
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void setTimer() {
        scoreTimer = new CountDownTimer(5000 + 200, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                binding.paintingWindow.removeView(scoreView);
                restartGame();
            }
        };

        gameTimer = new CountDownTimer(totalTime * 1000 + 200, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                binding.timeTV.setText(String.valueOf(millisUntilFinished / 1000));
                if (Server.player.getTurn() == currentGameState.getTurn()) {
                    if (hintCount == 0) {
                        if (millisUntilFinished < (totalTime * 1000 * 2) / 3) {
                            hint = dottedWord(chosenWord, hint);
                            Server.getHintReference().setValue(hint);
                            hintCount++;
                        }
                    }else if (hintCount == 1 && chosenWord.length() > 3){
                        if (millisUntilFinished < (totalTime * 1000) / 3){
                            hint = dottedWord(chosenWord, hint);
                            Server.getHintReference().setValue(hint);
                            hintCount++;
                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                calculatingScoreView = View.inflate(GameActivity.this,
                        R.layout.drawer_is_choosing_layout
                        , null);
                ((TextView) calculatingScoreView.findViewById(R.id.chooseText)).setText(
                        "Calculating Score:");
                ((TextView) calculatingScoreView.findViewById(R.id.chooseText)).setTextColor(Color.YELLOW);
                if (!Server.player.answered) {
                    Server.getAnswersRef().child(String.valueOf(Server.player.getTurn())).setValue(new Score(0, true));
                }
                binding.paintingWindow.addView(calculatingScoreView);
            }
        };

        chooseTimer = new CountDownTimer(15000 + 200, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.timeTV.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if (chosenWord == null) {
                    chosenWord = threeWordList.get(0);
                }
                hint = dottedWord(chosenWord);
                threeWordList.clear();
                Server.getCurrentWordReference().setValue(chosenWord);
                binding.paintingWindow.removeView(chooseWordView);
                binding.hintText.setText("Chosen Word: ");
                binding.hintTextView.setText(chosenWord);
                this.cancel();

                drawWindow.enabled = true;
                drawWindow.startDrawing();
                drawWindow.selectPencil();
                Server.player.answered = true;
                Server.getAnswersRef().child(String.valueOf(Server.player.getTurn())).setValue(new Score(-1,
                        true));
                Server.getPlayerRef().child(String.valueOf(Server.player.getTurn())).setValue(Server.player);
                gameTimer.start();
            }
        };

    }

    private void restartGame() {
        binding.paintingWindow.removeView(scoreView);
        if (Server.player.answered) {
            Server.player.answered = false;
        }
        Server.getPlayerRef().child(String.valueOf(Server.player.getTurn())).setValue(Server.player);
        if (Server.player.getTurn() == currentGameState.getTurn()) {
            if (currentGameState.getTurn() == Server.numberOfPlayers) {
                currentGameState.setRound(currentGameState.getRound() + 1);
                currentGameState.setTurn(1);
            } else {
                currentGameState.setTurn(currentGameState.getTurn() + 1);
            }
            currentGameState.setName(Server.turnPlayerHashmap.get(String.valueOf(currentGameState.getTurn())));
            Server.getCurrentWordReference().removeValue();
            Server.getAnswersRef().removeValue();
            Server.getCurrentStateReference().setValue(currentGameState);
        }
    }

    private void resetGame() {
        drawWindow.renewBoard();
        chosenWord = null;
        hint = null;
        hintCount = 0;
        Server.getCurrentWordReference().removeEventListener(currentWordListener);
        Server.getHintReference().removeEventListener(hintsListener);
        drawerIsChoosingView = null;
        chooseWordView = null;
    }

    private void chooseWords() {
        Random random = new Random();

        chooseWordView = View.inflate(this, R.layout.choose_word_layout, null);
        for (int i = 0; i < 3; i++) {
            threeWordList.add(capitalizeWord(Server.WORDS_LIST.get(random.nextInt(Server.WORDS_LIST.size()))));
        }
        chooseWordView.findViewById(R.id.option1).setOnClickListener(optionOnClickListener);
        ((TextView) chooseWordView.findViewById(R.id.option1)).setText(threeWordList.get(0));
        chooseWordView.findViewById(R.id.option2).setOnClickListener(optionOnClickListener);
        ((TextView) chooseWordView.findViewById(R.id.option2)).setText(threeWordList.get(1));
        chooseWordView.findViewById(R.id.option3).setOnClickListener(optionOnClickListener);
        ((TextView) chooseWordView.findViewById(R.id.option3)).setText(threeWordList.get(2));

        binding.paintingWindow.addView(chooseWordView);
        chooseTimer.start();


    }

    private String capitalizeWord(String word) {

        StringBuilder s = new StringBuilder();
        char ch = ' ';
        for (int i = 0; i < word.length(); i++) {
            if (ch == ' ' && word.charAt(i) != ' ')
                s.append(Character.toUpperCase(word.charAt(i)));
            else
                s.append(word.charAt(i));
            ch = word.charAt(i);
        }
        return s.toString().trim();
    }

    private void showScore(List<Integer> scoreList, List<String> playerList) {

        scoreView = View.inflate(this, R.layout.score_layout, null);
        for (int i = 0; i < scoreList.size(); i++) {
            TextView playerName = new TextView(this);
            playerName.setText(String.valueOf(Server.turnPlayerHashmap.get(playerList.get(i))));
            if (playerList.get(i).equals(String.valueOf(Server.player.getTurn()))){
                Server.player.setScore(Server.player.getScore() + scoreList.get(i));
            }
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
        ((TextView) scoreView.findViewById(R.id.chosenWordTV)).setText(chosenWord);
        binding.paintingWindow.removeView(calculatingScoreView);
        binding.paintingWindow.addView(scoreView);
        scoreTimer.start();
    }

    private void setMessageListener() {
        Server.getMessageRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages m = snapshot.getValue(Messages.class);
                messagesArrayList.add(m);
                messagesRVA.notifyDataSetChanged();
                binding.messageRV.smoothScrollToPosition(messagesArrayList.size());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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
        });

    }

    private void messagesRVSetter() {
        messagesArrayList = new ArrayList<>();
        messagesRVA = new MessagesRVA(this, messagesArrayList);
        binding.messageRV.setAdapter(messagesRVA);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        binding.messageRV.setLayoutManager(linearLayoutManager);
        binding.messageRV.smoothScrollToPosition(messagesArrayList.size());
    }

    private void playerRVSetter() {
        playersRVA = new PlayersRVA(this, Server.playerList);
        binding.playersRV.setAdapter(playersRVA);
        binding.playersRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL,
                false));
    }

    private void addView(View v, LinearLayout layout) {
        LinearLayout.LayoutParams temp = new LinearLayout.LayoutParams(colorPaletteSize, colorPaletteSize);
        temp.setMargins(marginsColorPalette, marginsColorPalette, marginsColorPalette, marginsColorPalette);
        v.setLayoutParams(temp);
        layout.addView(v);
    }

    private void select_tool(Tools v) {
        selectedTool.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.dis_selected_animation));
        selectedTool = v;
        selectedTool.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.selected_animation));
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
        colorFillView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawWindow.selectFillColor();
                select_tool((Tools) v);
            }
        });
        addView(colorFillView, binding.toolContainer);

        Bitmap clearBitmap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_clear);
        Tools clearView = new Tools(this, clearBitmap);
        addView(clearView, binding.toolContainer);
        clearView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawWindow.drawing) {
                    drawWindow.clear();
                }

                Animation selected_animation = AnimationUtils.loadAnimation(GameActivity.this,
                        R.anim.selected_animation);
                Animation dis_selected_animation = AnimationUtils.loadAnimation(GameActivity.this,
                        R.anim.dis_selected_animation);
                v.startAnimation(selected_animation);
                v.startAnimation(dis_selected_animation);

            }
        });

        Bitmap undoBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_undo);
        Tools undoView = new Tools(this, undoBitmap);
        undoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawWindow.drawing) {
                    drawWindow.undo();
                }

                Animation selected_animation = AnimationUtils.loadAnimation(GameActivity.this,
                        R.anim.selected_animation);
                Animation dis_selected_animation = AnimationUtils.loadAnimation(GameActivity.this,
                        R.anim.dis_selected_animation);
                v.startAnimation(selected_animation);
                v.startAnimation(dis_selected_animation);
            }
        });
        addView(undoView, binding.toolContainer);


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
        binding.seekBar.setOnTouchUp(new VerticalSeekBar.OnTouchUp() {
            @Override
            public void touchedUp() {
                drawWindow.pencilSizeShowing = false;
                drawWindow.invalidate();
            }
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
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawWindow.selectColor(((AvailableColor) v).color);
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
                        selectedColor = (AvailableColor) v;
                        Animation selected_animation =
                                AnimationUtils.loadAnimation(GameActivity.this,
                                        R.anim.selected_animation);
                        selectedColor.startAnimation(selected_animation);

                    }


                });
                layout.addView(v);
            }

        }
    }

    private void setPlayersListener() {
        Server.getPlayerRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playersRVA.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setOnClickListener() {
        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.messageET.getText().toString();
                if (!message.equals("")) {
                    if (!drawWindow.drawing) {
                        if (chosenWord != null) {
                            if (message.toLowerCase().equals(chosenWord.toLowerCase())) {
                                message = " guessed the word.";
                                if (!Server.player.answered) {
                                    Server.player.answered = true;
                                    Server.getPlayerRef().child(String.valueOf(Server.player.getTurn())).setValue(Server.player);
                                    Server.getAnswersRef().child(String.valueOf(Server.player.getTurn())).setValue(new Score(remainingTime, true));
                                    hint = null;
                                }
                            }
                        }
                    }
                    Messages m = new Messages(message, Server.getPlayer());

                    Server.getMessageRef()
                            .push()
                            .setValue(m);
                    binding.messageET.setText("");

                }
            }
        });
        binding.messageET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.messageRV.smoothScrollToPosition(messagesArrayList.size());
            }
        });
    }

    private String dottedWord(String word) {
        return dottedWord(word, "-1");
    }


    private static String dottedWord(String word, String dashedWord) {
        StringBuilder ans = new StringBuilder();
        if (dashedWord.equals("-1")) {
            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) != ' '){
                    ans.append("_ ");
                }else{
                    ans.append("  ");
                }
            }
        } else {
            Random random = new Random();
            int index;
            index = random.nextInt(dashedWord.length());
            while (dashedWord.charAt(index) != '_') {
                index = random.nextInt(word.length());
            }
            for (int i = 0; i < (dashedWord.length()); i++) {
                if (i != index) {
                    ans.append(dashedWord.charAt(i));
                }else {
                    ans.append(word.charAt(i/2));
                }

            }
        }
        return ans.toString();
    }

}
