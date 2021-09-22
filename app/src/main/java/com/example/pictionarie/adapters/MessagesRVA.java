package com.example.pictionarie.adapters;

import android.content.Context;
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
    List<Messages> messagesArrayList = new ArrayList<>();
    int answered_view = 1;
    int un_answered_view = 0;

    public MessagesRVA(Context context, List<Messages> messagesArrayList){
        this.context = context;
        this.messagesArrayList = messagesArrayList;
    }
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_messages_rv, parent,
                false);
        if (viewType == answered_view){
            view.setBackgroundResource(R.drawable.answered_layout);
        }
        return  new Holder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull MessagesRVA.Holder holder, int position) {
        holder.nameTV.setText(nameMessageMerger(
                messagesArrayList.get(position).getPlayer().getName(),
                messagesArrayList.get(position).getMessage()
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
        View view;

        public Holder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            nameTV = itemView.findViewById(R.id.textView);
        }
    }
}
