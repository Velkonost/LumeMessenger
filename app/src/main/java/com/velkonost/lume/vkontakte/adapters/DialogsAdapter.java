package com.velkonost.lume.vkontakte.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.activities.MessagesActivity;
import com.velkonost.lume.vkontakte.db.DBHelper;
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
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.ID;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.FWD_MESSAGES_BODIES_LISTS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.FWD_MESSAGES_DATES_LISTS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.FWD_MESSAGES_SENDERS_LISTS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_BODIES;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_DATES;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_IDS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_IS_OUT;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_SENDERS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.CHAT_ID;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.ITEMS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.RESPONSE;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.USER_ID;

/**
 * Адаптер списка диалогов
 */
public class DialogsAdapter extends RecyclerView.Adapter<DialogsAdapter.ViewHolder> {

    /**
     * Список имен пользователей
     */
    private ArrayList<String> users;
    /**
     * Список тел сообщений
     */
    private ArrayList<String> messages;

    private Context ctx;

    private ArrayList<String> idsList;

    private DBHelper dbHelper;

    public DialogsAdapter(ArrayList<String> users, ArrayList<String> messages,
                          Context ctx, ArrayList<String> idsList, DBHelper dbHelper) {
        this.users = users;
        this.messages = messages;
        this.ctx = ctx;

        this.idsList = idsList;
        this.dbHelper = dbHelper;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(ctx).inflate(R.layout.item_vkontakte_dialog, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {


        holder.dialogName.setText(users.get(position));
        holder.lastMessage.setText(messages.get(position));

        /**
         * Открытие диалога - переход к последним сообщениям
         */
        holder.dialogBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * Список идентификаторов сообщений
                 */
                final ArrayList<String> messagesIds = new ArrayList<>();

                /**
                 * Список тел сообщений
                 */
                final ArrayList<String> messagesBodies = new ArrayList<>();

                /**
                 * Булевский список направлений сообщений (отправлено от авторизованного сообщения)
                 */
                final ArrayList<Boolean> messagesIsOut = new ArrayList<>();

                /**
                 * Список отправителей сообщений
                 */
                final ArrayList<String> messagesSenders = new ArrayList<>();

                /**
                 * Список дат сообщений
                 */
                final ArrayList<String> messagesDates = new ArrayList<>();

                /**
                 * Список списков тел пересланных сообщений
                 */
                final ArrayList< ArrayList<String> > fwdMessagesBodiesLists = new ArrayList<>();

                /**
                 * Список списков отправителей пересланных сообщений
                 */
                final ArrayList< ArrayList<String> > fwdMessagesSendersLists = new ArrayList<>();

                /**
                 * Список списков дат пересланных сообщений
                 */
                final ArrayList< ArrayList<String> > fwdMessagesDatesLists = new ArrayList<>();

                /**
                 * Идентификатор диалога
                 */
                final String id = idsList.get(position);

                /**
                 * Определение типа диалога
                 */
                final String typeOfDialog = Integer.parseInt(id) < 100000000 ? CHAT_ID : USER_ID;

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

                /**
                 * Получение "count" последних сообщений диалога
                 */
                VKRequest request = new VKRequest(GET_MESSAGES, VKParameters.from(typeOfDialog, id, COUNT, AMOUNT_MESSAGES));
                Log.i(DEBUG_TAG, request.toString());
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);

                        try {
                            JSONArray array = response.json.getJSONObject(RESPONSE).getJSONArray(ITEMS);
                            VKApiMessage[] msg = new VKApiMessage[array.length()];

                            for (int i = 0; i < array.length(); i++) {
                                VKApiMessage mes = new VKApiMessage(array.getJSONObject(i));
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

                                fwdMessagesBodiesLists.add(fwdMessagesBodies);
                                fwdMessagesDatesLists.add(fwdMessagesDates);
                                fwdMessagesSendersLists.add(fwdMessagesSenders);

                                messagesIds.add(String.valueOf(message.id));
                                messagesBodies.add(message.body);
                                messagesIsOut.add(message.out);
                                messagesDates.add(getMessageDate(message.date));
                                messagesSenders.add(senderNickname[0]);

                            }

                            /**
                             * Открытие новой активности, передача полученный данных о сообщениях
                             */
                            ctx.startActivity(new Intent(ctx, MessagesActivity.class)
                                    .putExtra(ID, id)
                                    .putExtra(MESSAGES_IDS, messagesIds)
                                    .putExtra(MESSAGES_BODIES, messagesBodies)
                                    .putExtra(MESSAGES_IS_OUT, messagesIsOut)
                                    .putExtra(MESSAGES_DATES, messagesDates)
                                    .putExtra(MESSAGES_SENDERS, messagesSenders)
                                    .putExtra(FWD_MESSAGES_BODIES_LISTS, fwdMessagesBodiesLists)
                                    .putExtra(FWD_MESSAGES_DATES_LISTS, fwdMessagesDatesLists)
                                    .putExtra(FWD_MESSAGES_SENDERS_LISTS, fwdMessagesSendersLists)
                            );

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

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



    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * Блок диалога
         */
        LinearLayout dialogBlock;

        /**
         * Название диалога
         */
        TextView dialogName;

        /**
         * Тело последнего сообщения
         */
        TextView lastMessage;

        ViewHolder(final View itemView) {
            super(itemView);

            dialogBlock = (LinearLayout) itemView.findViewById(R.id.dialog_block);

            dialogName = (TextView) itemView.findViewById(R.id.txt_vp_item_list);
            lastMessage = (TextView) itemView.findViewById(R.id.txt_vp_item_list2);
        }
    }
}