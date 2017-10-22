package com.velkonost.lume.vkontakte.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.db.DBHelper;
import com.velkonost.lume.vkontakte.models.MessagesList;
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

import static com.velkonost.lume.Constants.DEBUG_TAG;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.GET_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.AMOUNT_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.COUNT;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.CHAT_ID;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.ITEMS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.RESPONSE;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.USER_ID;

/**
 * Адаптер сообщений диалога
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private Context ctx;
    private MessagesList messagesList;
    private String dialogId;
    private DBHelper dbHelper;

    public MessagesAdapter(MessagesList messagesList, Context ctx, String dialogId) {
        this.messagesList = messagesList;

        this.ctx = ctx;
        this.dialogId = dialogId;
        this.dbHelper = new DBHelper(ctx);
    }

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

                    VKRequest request = new VKRequest(GET_MESSAGES, VKParameters.from(typeOfDialog, dialogId, COUNT, AMOUNT_MESSAGES, "start_message_id", getLastMessageId()));
                    request.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);

                            JSONArray array = null;
                            try {
                                array = response.json.getJSONObject(RESPONSE).getJSONArray(ITEMS);
                                Log.i(DEBUG_TAG, array.toString());

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

                                /**
                                 * Список тел пересланных сообщений
                                 */
                                ArrayList<String> fwdMessagesBodies = new ArrayList<>();

                                /**
                                 * Список отправителей пересланных сообщений
                                 */
                                ArrayList<String> fwdMessagesSenders = new ArrayList<>();

                                /**
                                 * Список дат пересланных сообщений
                                 */
                                ArrayList<String> fwdMessagesDates = new ArrayList<>();

                                /**
                                 * Получение данных о пересланных сообщениях
                                 */
                                getFwdMessage(message, fwdMessagesBodies, fwdMessagesSenders, fwdMessagesDates);

                                if (typeOfDialog.equals(USER_ID)) {
                                    /**
                                     * Если диалог - личная переписка
                                     */
                                    if (!isSenderDetected[0]) {
                                        senderNicknameTemp[0] = getNicknameById(String.valueOf(message.user_id));
                                        isSenderDetected[0] = true;
                                    }

                                    if (message.out) {
                                        /**
                                         * Если авторизованный пользователь - отправитель сообщения
                                         */
                                        senderNickname[0] = "Я";
                                    } else {
                                        senderNickname[0] = senderNicknameTemp[0];
                                    }

                                } else {
                                    if (message.out) {
                                        /**
                                         * Если авторизованный пользователь - отправитель сообщения
                                         */
                                        senderNickname[0] = "Я";
                                    } else {
                                        senderNickname[0] = getNicknameById(String.valueOf(message.user_id));
                                    }
                                }

                                messagesList.addMessage(
                                        String.valueOf(message.id), message.body,
                                        getMessageDate(message.date), senderNickname[0],
                                        String.valueOf(message.out),
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

            LinearLayout.LayoutParams params
                    = new LinearLayout.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT);

            if (messagesList.getMessageIsOutByPosition(position).equals("true")) {
                holder.userName.setBackground(ContextCompat.getDrawable(ctx, R.drawable.dialog_message_from));
                holder.messageBody.setBackground(ContextCompat.getDrawable(ctx, R.drawable.dialog_message_from));
                holder.messageDate.setBackground(ContextCompat.getDrawable(ctx, R.drawable.dialog_message_from));
                holder.rvFwdMessages.setBackground(ContextCompat.getDrawable(ctx, R.drawable.dialog_message_from));

                params.gravity = Gravity.RIGHT;

            } else {
                holder.userName.setBackground(ContextCompat.getDrawable(ctx, R.drawable.dialog_message_to));
                holder.messageBody.setBackground(ContextCompat.getDrawable(ctx, R.drawable.dialog_message_to));
                holder.messageDate.setBackground(ContextCompat.getDrawable(ctx, R.drawable.dialog_message_to));
                holder.rvFwdMessages.setBackground(ContextCompat.getDrawable(ctx, R.drawable.dialog_message_to));

                params.gravity = Gravity.LEFT;

            }

            holder.userName.setLayoutParams(params);
            holder.messageBody.setLayoutParams(params);
            holder.messageDate.setLayoutParams(params);
            holder.rvFwdMessages.setLayoutParams(params);

            holder.userName.setText(messagesList.getMessageSendersByPosition(position));
            holder.messageBody.setText(messagesList.getMessageBodyByPosition(position));
            holder.messageDate.setText(messagesList.getMessageDateByPosition(position));

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
                VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, fwdMessage.user_id));
                request.executeSyncWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);

                        VKList list = (VKList) response.parsedModel;
                        fwdMessageUser[0] = String.valueOf(list.get(0));

                        /**
                         * Добавление пользователя в локальную БД
                         */
                        dbHelper.insertUsers(String.valueOf(fwdMessage.user_id), fwdMessageUser[0]);

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
            VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, id));
            request.executeSyncWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);

                    VKList list = (VKList) response.parsedModel;
                    nickname[0] = String.valueOf(list.get(0));
                    dbHelper.insertUsers(id, nickname[0]);

                }
            });
        }
        return nickname[0];
    }

    class ViewHolder extends RecyclerView.ViewHolder {

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

        ViewHolder(final View itemView, boolean last) {
            super(itemView);

            if (last) {
                btnShowMore = (Button) itemView.findViewById(R.id.show_more);
            } else {
                userName = (TextView) itemView.findViewById(R.id.txt_vp_item_list);
                messageBody = (TextView) itemView.findViewById(R.id.txt_vp_item_list2);
                messageDate = (TextView) itemView.findViewById(R.id.txt_vp_item_list3);

                rvFwdMessages = (RecyclerView) itemView.findViewById(R.id.rv);
            }
        }
    }
}
