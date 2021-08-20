package com.example.pictionarie.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pictionarie.R;
import com.example.pictionarie.Server;
import com.example.pictionarie.model.Player;
import com.example.pictionarie.model.Score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreViewRVA extends RecyclerView.Adapter<ScoreViewRVA.Holder> {
    Context context;
    List<Score> scoreList;
    int answered_layout = 1;
    int unanswered_layout = 2;

    public ScoreViewRVA(Context context, List<Score> scoreList){
        this.context = context;
        this.scoreList = scoreList;

    }
    @NonNull
    @Override
    public ScoreViewRVA.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_player_rv, parent, false);

        return  new Holder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.nameTV.setText(this.scoreList.get(position).getName());
        holder.scoreTV.setText(String.valueOf(this.scoreList.get(position).getScore()));

    }

    @Override
    public int getItemCount() {
        return this.scoreList.size();
    }
    public class Holder extends RecyclerView.ViewHolder{
        TextView nameTV;
        TextView scoreTV;
        View view;
        public Holder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            nameTV = itemView.findViewById(R.id.nameTV);
            scoreTV = itemView.findViewById(R.id.scoreTV);
        }
    }
}
