package com.velkonost.lume.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.models.RoundImageView;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIRST_NAME;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.ID;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.LAST_NAME;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.ITEMS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.PHOTO_50;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.RESPONSE;

/**
 * @author Velkonost
 */

public class CreateChatAdapter extends RecyclerView.Adapter<CreateChatAdapter.ViewHolder> {

    private Context ctx;
    private VKList listFriends;

//    private ArrayList usersNames;
//    private ArrayList<String> usersPhotos;
//    private ArrayList usersIds;

    private String queryStr;
    private ArrayList<String> userIdsToInvite;

    public CreateChatAdapter(Context ctx, VKList listFriends) {
        this.ctx = ctx;
        this.listFriends = listFriends;

        userIdsToInvite = new ArrayList<>();
//        usersNames = new ArrayList();
//        usersIds = new ArrayList();

    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_vkontakte_create_chat, parent, false));
    }

    private void createChat(String query) {
        String userIdsStr = "";
        for (int i = 0; i < userIdsToInvite.size(); i++) {
            userIdsStr += userIdsToInvite.get(i) + ",";
        }

        VKRequest searchRequest = new VKRequest("messages.createChat", VKParameters.from("title", query, "user_ids", userIdsStr));
        searchRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                try {
                    JSONArray array = response.json.getJSONObject(RESPONSE).getJSONArray(ITEMS);

                    ArrayList<String> usersNamesByQuery = new ArrayList<String>();
                    ArrayList<String> usersIdsByQuery = new ArrayList<String>();
                    ArrayList<String> usersPhotosByQuery = new ArrayList<String>();

                    for (int i = 0; i < array.length(); i ++) {
                        JSONObject a = array.getJSONObject(i);
                        usersNamesByQuery.add(a.get(FIRST_NAME) + " " + a.get(LAST_NAME));
                        usersPhotosByQuery.add(String.valueOf(a.get(PHOTO_50)));
                        usersIdsByQuery.add( String.valueOf(a.get(ID)));
                    }

//                    usersNames = usersNamesByQuery;
//                    usersIds = usersIdsByQuery;
//                    usersPhotos = usersPhotosByQuery;
                    notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }

        });

    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (position == 0) {

            ViewGroup.LayoutParams params = holder.searchUsersWrap.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.searchUsersWrap.setLayoutParams(params);
            holder.searchUsersEdit.setText(queryStr);

            ViewGroup.LayoutParams paramsZeroHeight = holder.friendBlock.getLayoutParams();
            paramsZeroHeight.height = 0;
            holder.friendBlock.setLayoutParams(paramsZeroHeight);

            holder.searchUsersBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    queryStr = holder.searchUsersEdit.getText().toString();
                    createChat(queryStr);
                }
            });
        } else {
            ViewGroup.LayoutParams params = holder.searchUsersWrap.getLayoutParams();
            params.height = 0;
            holder.searchUsersWrap.setLayoutParams(params);

            holder.userName.setText(String.valueOf(listFriends.get(position - 1)));
            holder.addParticipant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String userId = String.valueOf(listFriends.get(position - 1).fields.get(ID));
                        if (userIdsToInvite.contains(userId)) {
                            userIdsToInvite.remove(userId);
                            holder.addParticipant.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_account_plus_dark));
                        } else {
                            userIdsToInvite.add(userId);
                            holder.addParticipant.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_check));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            try {
                Picasso
                        .with(ctx)
                        .load(String.valueOf(listFriends.get(position - 1).fields.get(PHOTO_50)))
                        .transform(new RoundImageView())
                        .into(holder.userPhoto);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return listFriends.size() + 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout friendBlock;
        /**
         * Полное имя пользователя
         */
        TextView userName;

        ImageView userPhoto;

        LinearLayout searchUsersWrap;
        EditText searchUsersEdit;
        Button searchUsersBtn;

        ImageView addParticipant;

        ViewHolder(final View itemView) {
            super(itemView);

            friendBlock = (RelativeLayout) itemView.findViewById(R.id.friend_block);
            userName = (TextView) itemView.findViewById(R.id.user_name);
            userPhoto = (ImageView) itemView.findViewById(R.id.user_photo);

            searchUsersWrap = (LinearLayout) itemView.findViewById(R.id.search_users_wrap);
            searchUsersEdit = (EditText) itemView.findViewById(R.id.search_users);
            searchUsersBtn = (Button) itemView.findViewById(R.id.go_search_users);

            addParticipant = (ImageView) itemView.findViewById(R.id.add_participant);
        }
    }
}
