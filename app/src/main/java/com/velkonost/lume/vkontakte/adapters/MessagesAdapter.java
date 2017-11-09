package com.velkonost.lume.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.activities.MessagesActivity;
import com.velkonost.lume.vkontakte.db.DBHelper;
import com.velkonost.lume.vkontakte.models.MessagesList;
import com.velkonost.lume.vkontakte.models.RoundImageView;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKList;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import static com.velkonost.lume.vkontakte.Constants.API_METHODS.GET_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.AMOUNT_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.COUNT;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIELDS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.START_MESSAGE_ID;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.CHAT_ID;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.ITEMS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.PHOTO_50;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.RESPONSE;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.USER_ID;

/**
 * Адаптер сообщений диалога
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private Context ctx;
    private MessagesList messagesList;

    /**
     * Идентификатор текущего диалога
     */
    private String dialogId;
    private DBHelper dbHelper;

    private MessagesActivity mMessagesActivity;

    private int countMessagesOutRow;
    private int countMessagesInRow;

    public MessagesAdapter(MessagesActivity messagesActivity, MessagesList messagesList, Context ctx, String dialogId) {
        this.messagesList = messagesList;
        mMessagesActivity = messagesActivity;

        this.ctx = ctx;
        this.dialogId = dialogId;
        this.dbHelper = new DBHelper(ctx);

        countMessagesInRow = 0;
        countMessagesOutRow = 0;
    }

    /**
     * Получение идентификатора последнего сообщения
     */
    private String getLastMessageId() {
        return String.valueOf(Integer.parseInt(messagesList.getMessageIdsByPosition(messagesList.getMessagesAmount() - 1)) - 1);
    }

    public void setMessagesList(MessagesList messagesList) {
        this.messagesList = messagesList;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(ctx).inflate(R.layout.item_vkontakte_btn_show_more, parent, false);
            //Create viewholder for your default cell
            return new ViewHolder(view, true);

        }
        else {
            view = LayoutInflater.from(ctx).inflate(R.layout.item_vkontakte_message, parent, false);
            //Create viewholder for your footer view
            return new ViewHolder(view, false);
        }
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (position == messagesList.getMessagesAmount() - 1) {
            holder.btnShowMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /**
                     * Определение типа диалога
                     */
                    final String typeOfDialog = Integer.parseInt(dialogId) < 100000000 ? CHAT_ID : USER_ID;

                    /**
                     * Определен ли отправитель? (для личных диалого)
                     */
                    final boolean[] isSenderDetected = {false};

                    /**
                     * Полное имя отправителя сообщения
                     */
                    final String[] senderNickname = new String[1];

                    /**
                     * Переменная для временного хранения имени отправителя
                     */
                    final String[] senderNicknameTemp = new String[1];

                    VKRequest request = new VKRequest(
                            GET_MESSAGES,
                            VKParameters.from(typeOfDialog, dialogId, COUNT, AMOUNT_MESSAGES, START_MESSAGE_ID, getLastMessageId())
                    );
                    request.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);

                            JSONArray array = null;
                            try {
                                array = response.json.getJSONObject(RESPONSE).getJSONArray(ITEMS);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            VKApiMessage[] msg = new VKApiMessage[array.length()];

                            for (int i = 0; i < array.length(); i++) {
                                VKApiMessage mes = null;
                                try {
                                    mes = new VKApiMessage(array.getJSONObject(i));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                msg[i] = mes;
                            }

                            for (final VKApiMessage message : msg) {

                                /*
                                  Список тел пересланных сообщений
                                 */
                                ArrayList<String> fwdMessagesBodies = new ArrayList<>();

                                /*
                                  Список отправителей пересланных сообщений
                                 */
                                ArrayList<String> fwdMessagesSenders = new ArrayList<>();

                                /*
                                  Список дат пересланных сообщений
                                 */
                                ArrayList<String> fwdMessagesDates = new ArrayList<>();

                                /*
                                  Получение данных о пересланных сообщениях
                                 */
                                getFwdMessage(message, fwdMessagesBodies, fwdMessagesSenders, fwdMessagesDates);

                                if (typeOfDialog.equals(USER_ID)) {
                                    /*
                                      Если диалог - личная переписка
                                     */
                                    if (!isSenderDetected[0]) {
                                        senderNicknameTemp[0] = getNicknameById(String.valueOf(message.user_id));
                                        isSenderDetected[0] = true;
                                    }

                                    if (message.out) {
                                        /*
                                          Если авторизованный пользователь - отправитель сообщения
                                         */
                                        senderNickname[0] = "Я";
                                    } else {
                                        senderNickname[0] = senderNicknameTemp[0];
                                    }

                                } else {
                                    if (message.out) {
                                        /*
                                          Если авторизованный пользователь - отправитель сообщения
                                         */
                                        senderNickname[0] = "Я";
                                    } else {
                                        senderNickname[0] = getNicknameById(String.valueOf(message.user_id));
                                    }
                                }

                                messagesList.addMessage(
                                        String.valueOf(message.id), message.body,
                                        getMessageDate(message.date), senderNickname[0],
                                        String.valueOf(message.user_id), String.valueOf(message.out),
                                        fwdMessagesBodies, fwdMessagesSenders, fwdMessagesDates
                                );
                            }
                            setMessagesList(messagesList);

                            notifyDataSetChanged();
                        }
                    });
                }
            });
        } else {
            holder.messageWrap.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mMessagesActivity.addFwdMessageToSend(messagesList.getMessageIdsByPosition(position));
                    return true;
                }
            });

            holder.messageWrap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMessagesActivity.removeFwdMessageToSend(messagesList.getMessageIdsByPosition(position));
                }
            });

            RelativeLayout.LayoutParams params
                    = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

            if (messagesList.getMessageIsOutByPosition(position).equals("true")) {
                holder.messageSentIcon.setVisibility(View.VISIBLE);
                holder.messageReceivedIcon.setVisibility(View.INVISIBLE);
                holder.userPhotoReceived.setVisibility(View.INVISIBLE);

                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            } else {
                holder.messageSentIcon.setVisibility(View.INVISIBLE);
                holder.messageReceivedIcon.setVisibility(View.VISIBLE);

                Picasso
                        .with(ctx)
                        .load(dbHelper.getFromUsersPhoto50UrlById(messagesList.getSenderIdsByPosition(position)))
                        .transform(new RoundImageView())
                        .into(holder.userPhotoReceived);

                holder.userPhotoReceived.setVisibility(View.VISIBLE);
            }

            holder.messageWrap.setLayoutParams(params);
            holder.messageBody.setText(messagesList.getMessageBodyByPosition(position));
            holder.messageDate.setText(formatDate(messagesList.getMessageDateByPosition(position)));

            holder.rvFwdMessages.setHasFixedSize(true);
            holder.rvFwdMessages.setLayoutManager(
                    new LinearLayoutManager(
                            ctx, LinearLayoutManager.VERTICAL, false
                    )
            );

            MessagesList fwdMessagesList = new MessagesList(
                    messagesList.getFwdMessagesBodiesByPosition(position),
                    messagesList.getFwdMessagesDatesByPosition(position),
                    messagesList.getFwdMessagesSendersByPosition(position)
            );
            holder.rvFwdMessages.setAdapter(new FwdMessagesAdapter(fwdMessagesList, ctx));
        }
    }

    private String formatDate(String date) {
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);

        String hour = date.substring(11, 13);
        String minute = date.substring(14, 16);
        return hour + ":" + minute;
    }

    @Override
    public int getItemCount() {
        return messagesList.getMessagesAmount();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == messagesList.getMessagesAmount() - 1) ? 0 : 1;
    }

    /**
     * Получения данных о пересланных сообщениях
     * @param message - сообщения, о пересланных сообщениях которого необходимо получить информацию
     * @param fwdMessagesBodies - список тел пересланных сообщений
     * @param fwdMessagesSenders - список отправителей пересланных сообщений
     * @param fwdMessagesDates - список дат пересланных сообщений
     */
    private void getFwdMessage(
            VKApiMessage message,
            ArrayList<String> fwdMessagesBodies,
            ArrayList<String> fwdMessagesSenders,
            ArrayList<String> fwdMessagesDates
    ) {
        int messageFwdAmount = message.fwd_messages.size();
        for (int fwdMessageIndex = 0; fwdMessageIndex < messageFwdAmount; fwdMessageIndex++) {

            final VKApiMessage fwdMessage = message.fwd_messages.get(fwdMessageIndex);

            final String[] fwdMessageUser = {dbHelper.getFromUsersNicknameById(String.valueOf(fwdMessage.user_id))};
            if (fwdMessageUser[0] == null) {
                VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, fwdMessage.user_id, FIELDS, PHOTO_50));
                request.executeSyncWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);

                        VKList list = (VKList) response.parsedModel;
                        fwdMessageUser[0] = String.valueOf(list.get(0));

                        try {
                            String photo50Url = String.valueOf(list.get(0).fields.get(PHOTO_50));
                            dbHelper.insertUsers(String.valueOf(fwdMessage.user_id), fwdMessageUser[0], photo50Url);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        /**
                         * Добавление пользователя в локальную БД
                         */

                    }
                });
            }

            fwdMessagesSenders.add(fwdMessageUser[0]);
            fwdMessagesBodies.add(fwdMessage.body);
            fwdMessagesDates.add(getMessageDate(fwdMessage.date));

            int fwdMessageFwdAmount = fwdMessage.fwd_messages.size();
            if (fwdMessageFwdAmount > 0) {
                /**
                 * Если у пересланного сообщения есть пересланные сообщения
                 */
                getFwdMessage(fwdMessage, fwdMessagesBodies, fwdMessagesSenders, fwdMessagesDates);
            }

        }
    }

    /**
     * Конвертация из unix в нормальную дату
     * @param unixSeconds - время в unix-системе
     * @return - отформатированная дата
     */
    private String getMessageDate(long unixSeconds) {
        Date date = new Date(unixSeconds * 1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date
        sdf.setTimeZone(TimeZone.getDefault()); // give a timezone reference for formating (see comment at the bottom
        return sdf.format(date).substring(0, 19);
    }

    /**
     * Получение имени и фамилии пользователя по его идентификатору
     * @param id - идентификатор пользователь
     * @return - полное имя пользователя
     */
    private String getNicknameById(final String id) {
        /**
         * Попытка получение из локальной БД
         */
        final String[] nickname = {dbHelper.getFromUsersNicknameById(id)};
        if (nickname[0] == null) {
            /**
             * Иначе через API VK
             */
            VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, id, FIELDS, PHOTO_50));
            request.executeSyncWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);

                    VKList list = (VKList) response.parsedModel;
                    nickname[0] = String.valueOf(list.get(0));
                    try {
                        String photo50Url = String.valueOf(list.get(0).fields.get(PHOTO_50));
                        dbHelper.insertUsers(id, nickname[0], photo50Url);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });
        }
        return nickname[0];
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout messageWrap;

        /**
         * Полное имя автора сообщения
         */
        TextView userName;

        /**
         * Тело сообщения
         */
        TextView messageBody;

        /**
         * Дата сообщения
         */
        TextView messageDate;

        /**
         * Пересланные сообщения
         */
        RecyclerView rvFwdMessages;

        Button btnShowMore;

        ImageView userPhotoReceived;
        ImageView userPhotoSent;

        ImageView messageBackground;
        ImageView messageSentIcon;
        ImageView messageReceivedIcon;

        ViewHolder(final View itemView, boolean last) {
            super(itemView);

            if (last) {
                btnShowMore = (Button) itemView.findViewById(R.id.show_more);
            } else {
                messageWrap = (LinearLayout) itemView.findViewById(R.id.message_wrap);

//                userName = (TextView) itemView.findViewById(R.id.user_name);
                messageBody = (TextView) itemView.findViewById(R.id.message_body);
                messageDate = (TextView) itemView.findViewById(R.id.message_date);
                messageBackground = (ImageView) itemView.findViewById(R.id.message_background);

                userPhotoReceived = (ImageView) itemView.findViewById(R.id.user_photo_received);
                userPhotoSent = (ImageView) itemView.findViewById(R.id.user_photo_sent);

                rvFwdMessages = (RecyclerView) itemView.findViewById(R.id.fwd_messages_rv);
                messageSentIcon = (ImageView) itemView.findViewById(R.id.message_sent_icon);
                messageReceivedIcon = (ImageView) itemView.findViewById(R.id.message_received_icon);

            }
        }
    }
}
