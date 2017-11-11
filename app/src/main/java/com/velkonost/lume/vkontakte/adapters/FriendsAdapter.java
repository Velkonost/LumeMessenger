package com.velkonost.lume.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.models.RoundImageView;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;

import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.ID;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.PHOTO_50;
import static com.velkonost.lume.vkontakte.VkApiHelper.deleteFriend;
import static com.vk.sdk.VKUIHelper.getApplicationContext;


/**
 * Адаптер списка друзей авторизованного пользователя
 */

public class FriendsAdapter extends BaseAdapter {

    private Context ctx;
    private VKList listFriends;

    public FriendsAdapter(Context ctx, VKList listFriends) {
        this.ctx = ctx;
        this.listFriends = listFriends;
    }

    public VKList getListFriends() {
        return listFriends;
    }

    @Override
    public int getCount() {
        return listFriends.size();
    }

    @Override
    public Object getItem(int position) {
        return listFriends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getApplicationContext(),
                    R.layout.item_vkontakte_friend, null);
            new ViewHolder(convertView);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

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

        return convertView;
    }

     public void removeFriend(final int position) {
         VKRequest removeFriendRequest = null;
         try {
             removeFriendRequest = deleteFriend(String.valueOf(listFriends.get(position).fields.get(ID)));
             removeFriendRequest.executeWithListener(new VKRequest.VKRequestListener() {
                 @Override
                 public void onComplete(VKResponse response) {
                     listFriends.remove(position);
                     notifyDataSetChanged();
                     super.onComplete(response);
                 }
             });
         } catch (JSONException e) {
             e.printStackTrace();
         }
    }

    public String getFriendId(int position) {
        try {
            return String.valueOf(listFriends.get(position).fields.get(ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "0";
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

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
            itemView.setTag(this);
        }
    }
}
