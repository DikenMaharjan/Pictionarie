package com.example.pictionarie.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pictionarie.R;
import com.example.pictionarie.Server;

public class FinalScoreRVA extends RecyclerView.Adapter<FinalScoreRVA.Holder> {
   Context context;
    public FinalScoreRVA(Context context){
       this.context = context;

   }
    @NonNull
    @Override
    public FinalScoreRVA.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.final_score_layout, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FinalScoreRVA.Holder holder, int position) {
        holder.nameTV.setText(Server.totalScores.get(position).getName());
        holder.scoreTV.setText(String.valueOf(Server.totalScores.get(position).getScore()));
        switch(position){
            case 0:
                holder.imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_king));
                break;
            case 1:
                holder.imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_queen));
                break;
            case 2:
                holder.imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_rook));
                break;
            case 3:
                holder.imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_bishop));
                break;
        }


    }

    @Override
    public int getItemCount() {
        return Server.totalScores.size();
    }

    public class Holder extends RecyclerView.ViewHolder{
        TextView nameTV;
        TextView scoreTV;
        ImageView imageView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            nameTV = (TextView) itemView.findViewById(R.id.finalScoreNameTV);
            scoreTV = (TextView) itemView.findViewById(R.id.finalScoreScoreTV);
            imageView = (ImageView) itemView.findViewById(R.id.statusIV);
        }
    }
}
