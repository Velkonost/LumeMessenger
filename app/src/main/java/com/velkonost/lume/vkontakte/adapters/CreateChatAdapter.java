package com.velkonost.lume.vkontakte.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.velkonost.lume.vkontakte.activities.MainActivity;
import com.velkonost.lume.vkontakte.activities.MessagesActivity;
import com.velkonost.lume.vkontakte.models.RoundImageView;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;

import java.util.ArrayList;

import static com.velkonost.lume.vkontakte.Constants.API_METHODS.CREATE_CHAT;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.ID;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.FWD_MESSAGES_BODIES_LISTS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.FWD_MESSAGES_DATES_LISTS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.FWD_MESSAGES_SENDERS_LISTS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_BODIES;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_DATES;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_IDS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_IS_OUT;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_SENDERS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.PHOTO_50;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.RESPONSE;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.TITLE;
import static com.vk.sdk.api.VKApiConst.USER_IDS;

/**
 * @author Velkonost
 */

public class CreateChatAdapter extends RecyclerView.Adapter<CreateChatAdapter.ViewHolder> {

    private Context ctx;
    private VKList listFriends;

    private String queryStr;
    private ArrayList<String> userIdsToInvite;

    public CreateChatAdapter(Context ctx, VKList listFriends) {
        this.ctx = ctx;
        this.listFriends = listFriends;

        userIdsToInvite = new ArrayList<>();
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

        VKRequest searchRequest = new VKRequest(CREATE_CHAT, VKParameters.from(TITLE, query, USER_IDS, userIdsStr));
        searchRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                try {
                    int chatId = response.json.getInt(RESPONSE);

                    ctx.startActivity(new Intent(ctx, MessagesActivity.class)
                            .putExtra(ID, String.valueOf(chatId))
                            .putExtra(MESSAGES_IDS, new ArrayList<>())
                            .putExtra(MESSAGES_BODIES, new ArrayList<>())
                            .putExtra(MESSAGES_IS_OUT, new ArrayList<>())
                            .putExtra(MESSAGES_DATES, new ArrayList<>())
                            .putExtra(MESSAGES_SENDERS, new ArrayList<>())
                            .putExtra(FWD_MESSAGES_BODIES_LISTS, new ArrayList<>())
                            .putExtra(FWD_MESSAGES_DATES_LISTS, new ArrayList<>())
                            .putExtra(FWD_MESSAGES_SENDERS_LISTS, new ArrayList<>())
                    );

                    ((MainActivity)ctx).setDefaultModeFromCreateChat();

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

            ViewGroup.LayoutParams searchParams = holder.chatNameWrap.getLayoutParams();
            searchParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.chatNameWrap.setLayoutParams(searchParams);

            ViewGroup.LayoutParams friendsParams = holder.friendBlock.getLayoutParams();
            friendsParams.height = 0;
            holder.friendBlock.setLayoutParams(friendsParams);

            holder.createChatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    queryStr = holder.chatNameEdit.getText().toString();
                    createChat(queryStr);
                }
            });
        } else {

            try {
                final String userId = String.valueOf(listFriends.get(position - 1).fields.get(ID));

                if (userIdsToInvite.contains(userId)) {
                    holder.addParticipant.setImageDrawable(
                            ctx.getResources().getDrawable(R.drawable.ic_check)
                    );
                } else {
                    holder.addParticipant.setImageDrawable(
                            ctx.getResources().getDrawable(R.drawable.ic_account_plus_dark)
                    );
                }

                ViewGroup.LayoutParams searchParams = holder.chatNameWrap.getLayoutParams();
                searchParams.height = 0;
                holder.chatNameWrap.setLayoutParams(searchParams);

                ViewGroup.LayoutParams friendsParams = holder.friendBlock.getLayoutParams();
                friendsParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.friendBlock.setLayoutParams(friendsParams);

                holder.userName.setText(String.valueOf(listFriends.get(position - 1)));
                holder.friendBlock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (userIdsToInvite.contains(userId)) {
                            userIdsToInvite.remove(userId);
                            holder.addParticipant.setImageDrawable(
                                    ctx.getResources().getDrawable(R.drawable.ic_account_plus_dark)
                            );
                        } else {
                            userIdsToInvite.add(userId);
                            holder.addParticipant.setImageDrawable(
                                    ctx.getResources().getDrawable(R.drawable.ic_check)
                            );
                        }
                    }
                });

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

        LinearLayout chatNameWrap;
        EditText chatNameEdit;
        Button createChatBtn;

        ImageView addParticipant;

        ViewHolder(final View itemView) {
            super(itemView);

            friendBlock = (RelativeLayout) itemView.findViewById(R.id.friend_block);
            userName = (TextView) itemView.findViewById(R.id.user_name);
            userPhoto = (ImageView) itemView.findViewById(R.id.user_photo);

            chatNameWrap = (LinearLayout) itemView.findViewById(R.id.chat_name_wrap);
            chatNameEdit = (EditText) itemView.findViewById(R.id.edit_chat_name);
            createChatBtn = (Button) itemView.findViewById(R.id.create_chat);

            addParticipant = (ImageView) itemView.findViewById(R.id.add_participant);
        }
    }
}
