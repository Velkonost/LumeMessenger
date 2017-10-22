package com.velkonost.lume.instagram.models;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.velkonost.lume.R;

import java.util.List;

/**
 * Created by admin on 22.10.2017.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder>{

    List<InfoPhoto> photos;

    public RVAdapter(List<InfoPhoto> photos){
        this.photos = photos;
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_instagram_cardview, parent, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PersonViewHolder holder, int position) {
        //Log.d("xyi", String.valueOf(photos.size()));


        Picasso
                .with(holder.img.getContext())
                .load(photos.get(position).getLink())//photos.get(position).getLink()
                .into(holder.img);
        holder.likesTxt.setText(photos.get(position).getLikes()+" likes");
        Log.d("xyi", photos.get(position).getLink());
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        ImageView img;
        TextView likesTxt;
        String link;

        PersonViewHolder(View itemView) {
            super(itemView);
            //cv = (CardView)itemView.findViewById(R.id.cardview);
            img = (ImageView)itemView.findViewById(R.id.imgInst);
            likesTxt = (TextView)itemView.findViewById(R.id.likes_inst);
        }
    }
}
