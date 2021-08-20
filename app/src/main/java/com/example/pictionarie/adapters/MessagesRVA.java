package com.example.pictionarie.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.pictionarie.R;
import com.example.pictionarie.model.Messages;

import java.util.ArrayList;
import java.util.List;

public class MessagesRVA extends RecyclerView.Adapter<MessagesRVA.Holder> {
    Context context;
    List<Messages> messagesArrayList;
    int green_view = 1;
    int yellow_view = 0;
    int black_view = 2;

    public MessagesRVA(Context context, List<Messages> messagesArrayList){
        this.context = context;
        this.messagesArrayList = messagesArrayList;
    }
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView view = new TextView(context);
        view.setPadding(0, 15, 0,0);
        if(viewType == green_view) {
            view.setTextColor(Color.GREEN);
        }else if(viewType == yellow_view){
             view.setTextColor(Color.YELLOW);
        }
        return  new Holder(view);

    }

    @Override
    public int getItemViewType(int position) {
        Messages message = messagesArrayList.get(position);
        if (message.isCorrectAnswer()){
            if (message.isFirstTimeAnswer()) {
                return green_view;
            }else{
                return yellow_view;
            }
        }else{
            if (message.isAlreadyAnswered()){
                return green_view;
            }else{
                return black_view;
            }
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessagesRVA.Holder holder, int position) {
        Messages message = messagesArrayList.get(position);
        holder.nameTV.setText(nameMessageMerger(
              message.getName(), message.getMessage()
        ));


    }


    private Spanned nameMessageMerger(String name, String message){
        String ans = "<b>" + name + ":</b> " + message;
        return Html.fromHtml(ans);
    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    public class Holder extends RecyclerView.ViewHolder{

TextView nameTV;
        public Holder(@NonNull View itemView) {
            super(itemView);
            nameTV = (TextView) itemView;
        }
    }
}
