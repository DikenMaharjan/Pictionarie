package com.example.pictionarie.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pictionarie.R;
import com.example.pictionarie.Server;
import com.example.pictionarie.model.Player;

import java.util.List;

public class lobbyPlayerLVA extends ArrayAdapter<Player> {
    Context context;
    public lobbyPlayerLVA(@NonNull Context context, @NonNull List<Player> objects) {
        super(context, 0, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.lobby_players_layout,
                    parent, false);
        }
        Player player = getItem(position);
        ((TextView) convertView.findViewById(R.id.lobbyListViewNameTV)).setText(player.getName());
        if (!player.host){
            (convertView.findViewById(R.id.hostIV)).setVisibility(View.INVISIBLE);
        }
        return convertView;
    }
}

