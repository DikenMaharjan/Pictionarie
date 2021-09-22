package com.example.pictionarie.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pictionarie.R;
import com.example.pictionarie.model.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayersRVA extends RecyclerView.Adapter<PlayersRVA.Holder> {
    Context context;
    List<Player> playerArrayList;
    int answered_layout = 1;
    int unanswered_layout = 2;

    public PlayersRVA(Context context, List<Player> playerArrayList){
        this.context = context;
        this.playerArrayList = playerArrayList;

    }
    @NonNull
    @Override
    public PlayersRVA.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_player_rv, parent, false);
        if (viewType == answered_layout){
            view.setBackgroundResource(R.drawable.answered_layout);
        }
        return  new Holder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.nameTV.setText(
                playerArrayList.get(position).getName()
        );
        holder.scoreTV.setText(
                String.valueOf( playerArrayList.get(position).getScore())
        );

    }

    @Override
    public int getItemCount() {
        return playerArrayList.size();
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
