package com.velkonost.lume.vkontakte.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.adapters.MessagesAdapter;
import com.velkonost.lume.vkontakte.structures.MessagesList;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static com.velkonost.lume.vkontakte.Constants.API_METHODS.SEND_MESSAGE;
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
    ArrayList< ArrayList<String> > fwdMessagesBodiesLists = new ArrayList<>();

    /**
     * Список списков отправителей пересланных сообщений
     */
    ArrayList< ArrayList<String> > fwdMessagesSendersLists = new ArrayList<>();

    /**
     * Список списков дат пересланных сообщений
     */
    ArrayList< ArrayList<String> > fwdMessagesDatesLists = new ArrayList<>();

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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vkontakte_messages);

        initializeMessages();
        id = getIntent().getStringExtra(ID);

        editNewMessage = (EditText) findViewById(R.id.editNewMessage);
        btnSendNewMessage = (Button) findViewById(R.id.sendNewMessage);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv);
        setRecyclerViewConfiguration(recyclerView);

        recyclerView.setAdapter(new MessagesAdapter(messagesList, MessagesActivity.this));

        /**
         * Отправка нового сообщения
         */
        btnSendNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String typeOfDialog = Integer.parseInt(id) < 100000000 ? CHAT_ID : USER_ID;
                VKRequest request = new VKRequest(SEND_MESSAGE, VKParameters.from(typeOfDialog, id, VKApiConst.MESSAGE, editNewMessage.getText().toString()));

                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                    }
                });
            }
        });
    }

    /**
     * Инициализация списков данных сообщений
     */
    private void initializeMessages() {
        getIntentData();
        reverseMessagesDataArrays();
        initializeMessagesList();
    }

    /**
     * Получение списков данных сообщений
     */
    private void getIntentData() {
        messagesIds = getIntent().getStringArrayListExtra(MESSAGES_IDS);
        messagesBodies = getIntent().getStringArrayListExtra(MESSAGES_BODIES);
        messagesSenders = getIntent().getStringArrayListExtra(MESSAGES_SENDERS);
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
                new MessagesList(messagesIds, messagesBodies, messagesDates, messagesSenders, messagesIsOut, fwdMessagesBodiesLists, fwdMessagesDatesLists, fwdMessagesSendersLists);

    }

    /**
     * Сортировка списков данных сообщений в обратном порядке
     */
    private void reverseMessagesDataArrays() {
        Arrays.sort(messagesIds.toArray(), Collections.reverseOrder());
        Arrays.sort(messagesBodies.toArray(), Collections.reverseOrder());
        Arrays.sort(messagesSenders.toArray(), Collections.reverseOrder());
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
                        getBaseContext(), LinearLayoutManager.VERTICAL, false
                )
        );
    }
}
