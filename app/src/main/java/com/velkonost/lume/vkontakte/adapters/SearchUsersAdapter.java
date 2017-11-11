package com.velkonost.lume.vkontakte.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.models.RoundImageView;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.velkonost.lume.Constants.DEBUG_TAG;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIRST_NAME;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.ID;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.LAST_NAME;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.ITEMS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.PHOTO_50;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.RESPONSE;
import static com.velkonost.lume.vkontakte.VkApiHelper.addFriend;
import static com.velkonost.lume.vkontakte.VkApiHelper.deleteFriend;
import static com.velkonost.lume.vkontakte.VkApiHelper.searchUsers;

/**
 * @author Velkonost
 */

public class SearchUsersAdapter extends RecyclerView.Adapter<SearchUsersAdapter.ViewHolder> {

    private Context ctx;

    private ArrayList usersNames;
    private ArrayList<String> usersPhotos;
    private ArrayList usersIds;

    private String queryStr;

    private ArrayList<String> friendsRequestsList;

    public SearchUsersAdapter(Context ctx, VKList listFriends) {
        this.ctx = ctx;

        usersNames = new ArrayList();
        usersIds = new ArrayList();

        friendsRequestsList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_vkontakte_search_users, parent, false));
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
                    search(queryStr);
                }
            });

            holder.searchUsersEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0) {
                        holder.searchUsersBtn.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_magnify_empty));
                    } else {
                        holder.searchUsersBtn.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_magnify));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        } else {
            final String userId = String.valueOf(usersIds.get(position - 1));

            if (friendsRequestsList.contains(userId)) {
                holder.sendFriendRequest.setImageDrawable(
                        ctx.getResources().getDrawable(R.drawable.ic_check)
                );
            } else {
                holder.sendFriendRequest.setImageDrawable(
                        ctx.getResources().getDrawable(R.drawable.ic_account_plus_dark)
                );
            }
            ViewGroup.LayoutParams params = holder.searchUsersWrap.getLayoutParams();
            params.height = 0;
            holder.searchUsersWrap.setLayoutParams(params);

            holder.userName.setText(String.valueOf(usersNames.get(position - 1)));
            holder.friendBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!friendsRequestsList.contains(userId)) {
                        VKRequest searchRequest = addFriend(userId);
                        searchRequest.executeWithListener(new VKRequest.VKRequestListener() {
                            @Override
                            public void onComplete(VKResponse response) {
                                holder.sendFriendRequest.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_check));
                                friendsRequestsList.add(userId);
                                super.onComplete(response);
                            }
                        });
                    } else {
                        VKRequest searchRequest = deleteFriend(userId);
                        searchRequest.executeWithListener(new VKRequest.VKRequestListener() {
                            @Override
                            public void onComplete(VKResponse response) {
                                holder.sendFriendRequest.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_account_plus_dark));
                                friendsRequestsList.remove(userId);
                                super.onComplete(response);
                            }
                        });
                    }
                }
            });

            Picasso
                    .with(ctx)
                    .load(usersPhotos.get(position - 1))
                    .transform(new RoundImageView())
                    .into(holder.userPhoto);

        }
    }

    private void search(String query) {
        VKRequest searchRequest = searchUsers(query);
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

                    usersNames = usersNamesByQuery;
                    usersIds = usersIdsByQuery;
                    usersPhotos = usersPhotosByQuery;
                    notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.i(DEBUG_TAG, String.valueOf(error));
            }

        });

    }


    @Override
    public int getItemCount() {
        return usersNames.size() + 1;
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout friendBlock;
        /**
         * Полное имя пользователя
         */
        TextView userName;

        ImageView userPhoto;
        ImageView sendFriendRequest;

        LinearLayout searchUsersWrap;
        EditText searchUsersEdit;
        ImageButton searchUsersBtn;


        ViewHolder(final View itemView) {
            super(itemView);

            friendBlock = (RelativeLayout) itemView.findViewById(R.id.friend_block);
            userName = (TextView) itemView.findViewById(R.id.user_name);
            userPhoto = (ImageView) itemView.findViewById(R.id.user_photo);

            searchUsersWrap = (LinearLayout) itemView.findViewById(R.id.search_users_wrap);
            searchUsersEdit = (EditText) itemView.findViewById(R.id.search_users);
            searchUsersBtn = (ImageButton) itemView.findViewById(R.id.go_search_users);

            sendFriendRequest = (ImageView) itemView.findViewById(R.id.send_request);
        }
    }
}
