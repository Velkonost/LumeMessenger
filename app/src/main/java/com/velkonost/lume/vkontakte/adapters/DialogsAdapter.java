package com.velkonost.lume.vkontakte.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.activities.MessagesActivity;
import com.velkonost.lume.vkontakte.db.DBHelper;
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
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import static com.velkonost.lume.vkontakte.Constants.API_METHODS.GET_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.AMOUNT_DIALOGS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.AMOUNT_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.COUNT;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIELDS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.ID;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.OFFSET;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.FWD_MESSAGES_BODIES_LISTS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.FWD_MESSAGES_DATES_LISTS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.FWD_MESSAGES_SENDERS_LISTS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_BODIES;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_DATES;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_IDS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_IS_OUT;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_SENDERS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_SENDERS_IDS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.BODY;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.CHAT_ID;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.ITEMS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.MESSAGE;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.PHOTO_50;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.RESPONSE;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.TITLE;
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

    /**
     * Список идентификаторов диалогов
     */
    private ArrayList<String> idsList;

    private ArrayList<String> photosUrls;

    private DBHelper dbHelper;

    /**
     * Количество уже отображаемых диалогов в списке
     */
    private int alreadyShowedDialogsAmount;

    public DialogsAdapter(ArrayList<String> users, ArrayList<String> messages,
                          Context ctx, ArrayList<String> idsList,
                          ArrayList<String> photosUrls, DBHelper dbHelper) {
        this.users = users;
        this.messages = messages;
        this.ctx = ctx;

        this.idsList = idsList;
        this.photosUrls = photosUrls;

        this.dbHelper = dbHelper;

        alreadyShowedDialogsAmount = 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(ctx).inflate(R.layout.item_vkontakte_btn_show_more, parent, false);
            //Create viewholder for your default cell
            return new ViewHolder(view, true);

        } else {
            view = LayoutInflater.from(ctx).inflate(R.layout.item_vkontakte_dialog, parent, false);
            //Create viewholder for your footer view
            return new ViewHolder(view, false);
        }

    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (position == users.size() - 1) {
            holder.btnShowMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alreadyShowedDialogsAmount += AMOUNT_DIALOGS;
                    VKRequest requestUpdatedDialogs = VKApi.messages().getDialogs(VKParameters.from(
                            COUNT, AMOUNT_DIALOGS,
                            OFFSET, alreadyShowedDialogsAmount
                    ));

                    requestUpdatedDialogs.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            JSONObject jsonResponse = null;
                            try {
                                jsonResponse = (JSONObject) response.json.get(RESPONSE);
                                JSONArray messagesJSONArray = jsonResponse.getJSONArray(ITEMS);

                                for (int i = 0; i < messagesJSONArray.length(); i++) {
                                    JSONObject jsonMessage = (JSONObject) messagesJSONArray.get(i);
                                    jsonMessage = jsonMessage.getJSONObject(MESSAGE);

                                    String chatId;
                                    boolean isChat;
                                    try {
                                        chatId = jsonMessage.getString(CHAT_ID);
                                        isChat = true;
                                    } catch (JSONException e) {
                                        chatId = jsonMessage.getString(USER_ID);
                                        isChat = false;
                                    }

                                    String dialogPhoto = " ";
                                    try {
                                        dialogPhoto = jsonMessage.getString(PHOTO_50);
                                    } catch (JSONException e) {
                                        if (!isChat) {
                                            dialogPhoto = dbHelper.getFromUsersPhoto50UrlById(jsonMessage.getString(USER_ID));
                                        } else {
                                            dialogPhoto = "0";
                                        }
                                    }

                                    idsList.add(chatId);
                                    photosUrls.add(dialogPhoto);

                                    final String[] dialogTitle = {jsonMessage.getString(TITLE)};

                                    if (dialogTitle[0].equals("")) {
                                        VKRequest request = VKApi.users().get(
                                                VKParameters.from(
                                                        VKApiConst.USER_ID,
                                                        jsonMessage.getString(USER_ID)
                                                )
                                        );

                                        request.executeSyncWithListener(new VKRequest.VKRequestListener() {
                                            @Override
                                            public void onComplete(VKResponse response) {
                                                super.onComplete(response);

                                                VKList list = (VKList) response.parsedModel;
                                                dialogTitle[0] = String.valueOf(list.get(0));
                                                users.add(String.valueOf(dialogTitle[0]));
                                            }
                                        });
                                    } else {
                                        users.add(dialogTitle[0]);
                                    }
                                    messages.add(jsonMessage.getString(BODY) + " ");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            super.onComplete(response);
                        }
                    });
                }
            });
        } else {
            holder.dialogName.setText(users.get(position));
            holder.lastMessage.setText(messages.get(position));

            if (photosUrls.get(position).equals("0")) {
                Picasso
                        .with(ctx)
                        .load(String.valueOf(ctx.getResources().getDrawable(R.drawable.ic_ab_app)))
                        .transform(new RoundImageView())
                        .into(holder.dialogPhoto);
            } else {
                Picasso
                        .with(ctx)
                        .load(photosUrls.get(position))
                        .transform(new RoundImageView())
                        .into(holder.dialogPhoto);
            }

            /**
             * Открытие диалога - переход к последним сообщениям
             */
            holder.dialogBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDialogByPosition(position);
                }
            });
        }

    }

    private void openDialogByPosition(int position) {
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

        final ArrayList<String> messagesSendersIds = new ArrayList<>();

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
        VKRequest request = new VKRequest(
                GET_MESSAGES,
                VKParameters.from(typeOfDialog, id, COUNT, AMOUNT_MESSAGES)
        );

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
                        messagesSendersIds.add(String.valueOf(message.user_id));
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
                            .putExtra(MESSAGES_SENDERS_IDS, messagesSendersIds)
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


    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public void setMessages(ArrayList<String> messages) {
        this.messages = messages;
    }

    public void setIdsList(ArrayList<String> idsList) {
        this.idsList = idsList;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public ArrayList<String> getIdsList() {
        return idsList;
    }

    public ArrayList<String> getPhotosUrls() {
        return photosUrls;
    }

    public void setPhotosUrls(ArrayList<String> photosUrls) {
        this.photosUrls = photosUrls;
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == users.size() - 1) ? 0 : 1;
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

        Button btnShowMore;

        ImageView dialogPhoto;

        ViewHolder(final View itemView, boolean last) {
            super(itemView);
            if (last) {
                btnShowMore = (Button) itemView.findViewById(R.id.show_more);
            } else {
                dialogBlock = (LinearLayout) itemView.findViewById(R.id.dialog_block);

                dialogName = (TextView) itemView.findViewById(R.id.txt_vp_item_list);
                lastMessage = (TextView) itemView.findViewById(R.id.txt_vp_item_list2);

                dialogPhoto = (ImageView) itemView.findViewById(R.id.dialog_icon);
            }
        }
    }
}
