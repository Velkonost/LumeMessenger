package com.velkonost.lume.vkontakte.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.adapters.MessagesAdapter;
import com.velkonost.lume.vkontakte.db.DBHelper;
import com.velkonost.lume.vkontakte.models.MessagesList;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import static com.velkonost.lume.Constants.DEBUG_TAG;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.GET_LONG_POLL_MESSAGES_HISTORY;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.GET_LONG_POLL_SERVER;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.SEND_MESSAGE;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIELDS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FORWARD_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.ID;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.IS_NEED_PTS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.PTS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.PTS_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.TS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.TS_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.FWD_MESSAGES_BODIES_LISTS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.FWD_MESSAGES_DATES_LISTS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.FWD_MESSAGES_SENDERS_LISTS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_BODIES;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_DATES;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_IDS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_IS_OUT;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_SENDERS;
import static com.velkonost.lume.vkontakte.Constants.MESSAGES_DATA.MESSAGES_SENDERS_IDS;
import static com.velkonost.lume.vkontakte.Constants.REFRESH_MESSAGES_PERIOD;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.CHAT_ID;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.ITEMS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.PHOTO_50;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.RESPONSE;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.USER_ID;

/**
 * Активность открытого диалога
 * Отображает список сообщений
 */

public class MessagesActivity extends AppCompatActivity {

    /**
     * Список идентификаторов сообщений
     */
    ArrayList<String> messagesIds = new ArrayList<>();

    /**
     * Список тел сообщений
     */
    ArrayList<String> messagesBodies = new ArrayList<>();

    /**
     * Список отправителей сообщений
     */
    ArrayList<String> messagesSenders = new ArrayList<>();

    ArrayList<String> messagesSendersIds = new ArrayList<>();

    /**
     * Булевский список отправлений сообщений (сообщение отправление от авторизованного пользователя?)
     */
    ArrayList<String> messagesIsOut = new ArrayList<>();

    /**
     * Список дат сообщений
     */
    ArrayList<String> messagesDates = new ArrayList<>();

    /**
     * Список списков тел пересланных сообщений
     */
    ArrayList<ArrayList<String>> fwdMessagesBodiesLists = new ArrayList<>();

    /**
     * Список списков отправителей пересланных сообщений
     */
    ArrayList<ArrayList<String>> fwdMessagesSendersLists = new ArrayList<>();

    /**
     * Список списков дат пересланных сообщений
     */
    ArrayList<ArrayList<String>> fwdMessagesDatesLists = new ArrayList<>();

    /**
     * Форма для написания нового сообщения
     */
    private EditText editNewMessage;

    /**
     * Кнопка для отправки нового сообщения
     */
    private Button btnSendNewMessage;

    /**
     * Объект для хранения списков данных сообщений и их пересланных сообщений
     */
    private MessagesList messagesList;

    /**
     * Идентификатор диалога
     */
    private String id;

    private String ts, pts;

    private DBHelper dbHelper;

    private TimerCheckMessagesState timer;

    private RecyclerView recyclerView;

    private TextView fwdMessagesToSend;
    private ArrayList<String> fwdMessagesToSendList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vkontakte_messages);

        connectLongPollServer();
        initializeMessages();
        id = getIntent().getStringExtra(ID);
        dbHelper = new DBHelper(this);

        ts = dbHelper.getValueFromMetaData(TS_MESSAGES);
        pts = dbHelper.getValueFromMetaData(PTS_MESSAGES);

        fwdMessagesToSendList = new ArrayList<>();
        fwdMessagesToSend = (TextView) findViewById(R.id.fwd_messages);
        editNewMessage = (EditText) findViewById(R.id.editNewMessage);
        btnSendNewMessage = (Button) findViewById(R.id.sendNewMessage);

        recyclerView = (RecyclerView) findViewById(R.id.rv);
        setRecyclerViewConfiguration(recyclerView);

        recyclerView.setAdapter(new MessagesAdapter(MessagesActivity.this, messagesList, MessagesActivity.this, id));

        /**
         * Отправка нового сообщения
         */
        btnSendNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String typeOfDialog = Integer.parseInt(id) < 100000000 ? CHAT_ID : USER_ID;

                String newMessageBody = editNewMessage.getText().toString();
                editNewMessage.setText("");

                String fwdMessagesToSendStr = "";
                for (int i = 0; i < fwdMessagesToSendList.size(); i++) {
                    fwdMessagesToSendStr += fwdMessagesToSendList.get(i) + ",";
                }
                fwdMessagesToSendList = new ArrayList<String>();

                VKRequest request = new VKRequest(
                        SEND_MESSAGE,
                        VKParameters.from(
                                typeOfDialog, id,
                                VKApiConst.MESSAGE, newMessageBody,
                                FORWARD_MESSAGES, fwdMessagesToSendStr
                        )
                );

                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        refreshMessages();
                        connectLongPollServer();
                    }
                });
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                timer = new TimerCheckMessagesState(1000000000, REFRESH_MESSAGES_PERIOD);
                timer.start();

            }
        }, REFRESH_MESSAGES_PERIOD);
    }

    @Override
    protected void onStop() {
        if (timer != null) {
            timer.cancel();
        }
        super.onStop();
    }

    /**
     * Инициализация списков данных сообщений
     */
    private void initializeMessages() {
        getIntentData();
//        reverseMessagesDataArrays();
        initializeMessagesList();
    }

    /**
     * Получение списков данных сообщений
     */
    private void getIntentData() {
        messagesIds = getIntent().getStringArrayListExtra(MESSAGES_IDS);
        messagesBodies = getIntent().getStringArrayListExtra(MESSAGES_BODIES);
        messagesSenders = getIntent().getStringArrayListExtra(MESSAGES_SENDERS);
        messagesSendersIds = getIntent().getStringArrayListExtra(MESSAGES_SENDERS_IDS);
        messagesIsOut = getIntent().getStringArrayListExtra(MESSAGES_IS_OUT);
        messagesDates = getIntent().getStringArrayListExtra(MESSAGES_DATES);

        fwdMessagesBodiesLists = (ArrayList<ArrayList<String>>) getIntent().getSerializableExtra(FWD_MESSAGES_BODIES_LISTS);
        fwdMessagesDatesLists = (ArrayList<ArrayList<String>>) getIntent().getSerializableExtra(FWD_MESSAGES_DATES_LISTS);
        fwdMessagesSendersLists = (ArrayList<ArrayList<String>>) getIntent().getSerializableExtra(FWD_MESSAGES_SENDERS_LISTS);
    }

    /**
     * Инициализация списка для хранения списков данных сообщений
     */
    private void initializeMessagesList() {
        messagesList =
                new MessagesList(
                        messagesIds, messagesBodies, messagesDates,
                        messagesSenders, messagesSendersIds, messagesIsOut,
                        fwdMessagesBodiesLists, fwdMessagesDatesLists, fwdMessagesSendersLists
                );

    }

    /**
     * Сортировка списков данных сообщений в обратном порядке
     */
    private void reverseMessagesDataArrays() {
        Arrays.sort(messagesIds.toArray(), Collections.reverseOrder());
        Arrays.sort(messagesBodies.toArray(), Collections.reverseOrder());
        Arrays.sort(messagesSenders.toArray(), Collections.reverseOrder());
        Arrays.sort(messagesSendersIds.toArray(), Collections.reverseOrder());
        Arrays.sort(messagesIsOut.toArray(), Collections.reverseOrder());
        Arrays.sort(messagesDates.toArray(), Collections.reverseOrder());

//        Arrays.sort(fwdMessagesBodiesLists.toArray(), Collections.reverseOrder());
//        Arrays.sort(fwdMessagesDatesLists.toArray(), Collections.reverseOrder());
//        Arrays.sort(fwdMessagesSendersLists.toArray(), Collections.reverseOrder());
    }

    private void setRecyclerViewConfiguration(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        getBaseContext(), LinearLayoutManager.VERTICAL, true
                )
        );
    }

    /**
     * Получение данных, необходимых для обновления списка сообщений
     */
    private void connectLongPollServer() {
        VKRequest requestThisProfileInfo = new VKRequest(GET_LONG_POLL_SERVER, VKParameters.from(IS_NEED_PTS, true));
        requestThisProfileInfo.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                JSONObject list = response.json;
                try {
                    JSONObject a = (JSONObject) list.get(RESPONSE);
                    ts = a.getString(TS);
                    pts = a.getString(PTS);

                    dbHelper.updateMetaData(TS_MESSAGES, ts);
                    dbHelper.updateMetaData(PTS_MESSAGES, pts);

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

    /**
     * Таймер для обновления состояния списка сообщений
     */
    private class TimerCheckMessagesState extends CountDownTimer {

        TimerCheckMessagesState(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            refreshMessages();
            connectLongPollServer();
        }

        @Override
        public void onFinish() {
            finish();
        }
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
     * Обновление списка сообщений
     */
    private void refreshMessages() {
        VKRequest request = new VKRequest(GET_LONG_POLL_MESSAGES_HISTORY, VKParameters.from(TS, ts, PTS, pts, FIELDS, ""));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                /**
                 * Полное имя отправителя сообщения
                 */
                String senderNickname;

                JSONObject jsonResponse;
                JSONArray messagesJSONArray = null;

                try {
                    jsonResponse = (JSONObject) response.json.get(RESPONSE);
                    messagesJSONArray = jsonResponse.getJSONObject(MESSAGES).getJSONArray(ITEMS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < messagesJSONArray.length(); i++) {
                    VKApiMessage message = null;
                    String chatId = null;
                    try {
                        message = new VKApiMessage(messagesJSONArray.getJSONObject(i));
                        chatId = getDialogIdOfNewMessage((JSONObject) messagesJSONArray.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (chatId.equals(id)) {
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

                        if (message.out) {
                            /**
                             * Если авторизованный пользователь - отправитель сообщения
                             */
                            senderNickname = "Я";
                        } else {
                            senderNickname = getNicknameById(String.valueOf(message.user_id));
                        }

                        fwdMessagesBodiesLists.add(0, fwdMessagesBodies);
                        fwdMessagesDatesLists.add(0, fwdMessagesDates);
                        fwdMessagesSendersLists.add(0, fwdMessagesSenders);

                        messagesIds.add(0, String.valueOf(message.id));
                        messagesBodies.add(0, message.body);
                        messagesIsOut.add(0, String.valueOf(message.out));
                        messagesDates.add(0, getMessageDate(message.date));
                        messagesSenders.add(0, senderNickname);
                    }
                }

                setNewMessagesData();
                refreshMessagesAdapter();
            }
        });
    }

    /**
     * Получение идентификатора диалога по сообщению
     * @param jsonMessage - json-объект сообщения
     * @return - идентификатор диалога
     */
    private String getDialogIdOfNewMessage(JSONObject jsonMessage) {
        try {
            return jsonMessage.getString(CHAT_ID);
        } catch (JSONException e) {
            try {
                return jsonMessage.getString(USER_ID);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Установка полученных данных
     */
    private void setNewMessagesData() {
        messagesList.setIds(messagesIds);
        messagesList.setBodies(messagesBodies);
        messagesList.setIsOut(messagesIsOut);
        messagesList.setDates(messagesDates);
        messagesList.setSenders(messagesSenders);

        messagesList.setFwdMessagesBodiesLists(fwdMessagesBodiesLists);
        messagesList.setFwdMessagesDatesLists(fwdMessagesDatesLists);
        messagesList.setFwdMessagesSendersLists(fwdMessagesSendersLists);
    }

    /**
     * Обновление состояния списка сообщений
     */
    public void refreshMessagesAdapter() {
        ((MessagesAdapter)recyclerView.getAdapter()).setMessagesList(messagesList);
        recyclerView.getAdapter().notifyDataSetChanged();
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

    public void addFwdMessageToSend(String messageId) {
        if (!fwdMessagesToSendList.contains(messageId)) {
            fwdMessagesToSendList.add(messageId);
            fwdMessagesToSend.setText(fwdMessagesToSendList.toString());
        }
    }

    public void removeFwdMessageToSend(String messageId) {
        if (fwdMessagesToSendList.contains(messageId)) {
            fwdMessagesToSendList.remove(messageId);
            fwdMessagesToSend.setText(fwdMessagesToSendList.toString());
        }
    }

}
