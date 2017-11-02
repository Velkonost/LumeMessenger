package com.velkonost.lume.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.models.RoundImageView;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;

import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.PHOTO_50;

/**
 * Адаптер списка друзей авторизованного пользователя
 */

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private Context ctx;
    private VKList listFriends;


    public FriendsAdapter(Context ctx, VKList listFriends) {
        this.ctx = ctx;
        this.listFriends = listFriends;

    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_vkontakte_friend, parent, false));

    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.userName.setText(String.valueOf(listFriends.get(position)));
        try {
            Picasso
                    .with(ctx)
                    .load(String.valueOf(listFriends.get(position).fields.get(PHOTO_50)))
                    .transform(new RoundImageView())
                    .into(holder.userPhoto);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return listFriends.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout friendBlock;
        /**
         * Полное имя пользователя
         */
        TextView userName;

        ImageView userPhoto;

        ViewHolder(final View itemView) {
            super(itemView);

            friendBlock = (RelativeLayout) itemView.findViewById(R.id.friend_block);
            userName = (TextView) itemView.findViewById(R.id.user_name);
            userPhoto = (ImageView) itemView.findViewById(R.id.user_photo);

        }
    }
}
