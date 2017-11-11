package com.velkonost.lume.vkontakte.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.adapters.CreateChatAdapter;
import com.velkonost.lume.vkontakte.adapters.CustomAdapter;
import com.velkonost.lume.vkontakte.adapters.DialogsAdapter;
import com.velkonost.lume.vkontakte.adapters.FriendsAdapter;
import com.velkonost.lume.vkontakte.adapters.SearchUsersAdapter;
import com.velkonost.lume.vkontakte.db.DBHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiDialog;
import com.vk.sdk.api.model.VKApiGetDialogResponse;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import devlight.io.library.ntb.NavigationTabBar;

import static com.velkonost.lume.Constants.COMMA;
import static com.velkonost.lume.Constants.DEBUG_TAG;
import static com.velkonost.lume.R.id.fab;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.GET_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.GET_PROFILE_INFO;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.AMOUNT_DIALOGS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.AMOUNT_MESSAGES;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.COUNT;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.DOMAIN;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIELDS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIRST_NAME;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.HINTS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.ID;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.LAST_NAME;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.ORDER;
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
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.BODY;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.CHAT_ID;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.ITEMS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.MESSAGE;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.PHOTO_50;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.RESPONSE;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.TITLE;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.USER_ID;
import static com.velkonost.lume.vkontakte.Constants.VIEW_PAGER_PAGES.DIALOGS_PAGE;
import static com.velkonost.lume.vkontakte.Constants.VIEW_PAGER_PAGES.FRIENDS_PAGE;
import static com.velkonost.lume.vkontakte.Constants.VIEW_PAGER_PAGES.SETTINGS_PAGE;

/**
 * Основная активность
 */

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;

    /**
     * Нижняя панель навигации
     */
    private NavigationTabBar navigationTabBar;

    /**
     * Идентификатор авторизованного пользователя
     */
    private String authUserId;

    /**
     * Локальная БД
     */
    private DBHelper dbHelper;

    /**
     * Задержка между запросами к API VK
     */
    private long timeDelay = 0;

    /**
     * Адаптер вкладки друзей пользователя
     */
    private FriendsAdapter mFriendsAdapter;

    /**
     * Адаптер списка диалогов пользователя
     */
    private DialogsAdapter mDialogsAdapter;

    /**
     * Адаптер вкладки для создания новой беседы
     */
    private CreateChatAdapter mCreateChatAdapter;

    /**
     * Адаптер вкладки для поиска пользователей
     */
    private SearchUsersAdapter mSearchUsersAdapter;


    private PagerAdapter mPagerAdapterSearch;
    private PagerAdapter mPagerAdapterDefault;
    private PagerAdapter mPagerAdapterCreateChat;

    /**
     * Состояние адаптера для вкладки поиска пользователей
     */
    private boolean isSetSearchPageAdapter;

    /**
     * Состояние адаптера для вкладки создания беседы
     */
    private boolean isSetCreateChatPageAdapter;

    /**
     * Элемент, отображающийся поверх во время загрузки данных
     */
    private RelativeLayout loadingView;

    /**
     * Основная кнопка на экране
     */
    private FloatingActionButton mainFab;

    /**
     * Таймер для обновления списка диалогов
     */
    private TimerRefreshDialogsList timer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vkontakte_main);

        viewPager = (ViewPager) findViewById(R.id.vp_horizontal_ntb);
        mainFab = (FloatingActionButton) findViewById(fab);
        loadingView = (RelativeLayout) findViewById(R.id.loading_view);

        dbHelper = new DBHelper(this);

        /* Установка слушателя на основную кнопку */
        initializeFabListener();

        new InitializeData().execute();
        startTimerRefreshDialogsList();
    }

    /**
     * Запус таймера для обновления списка диалогов
     */
    private void startTimerRefreshDialogsList() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                timer = new TimerRefreshDialogsList(1000000000, REFRESH_MESSAGES_PERIOD);
                timer.start();
            }
        }, REFRESH_MESSAGES_PERIOD);
    }

    @Override
    public void onBackPressed() {
        if (timer != null) {
            timer.cancel();
        }
        super.onBackPressed();
    }

    /**
     * Таймер для обновления состояния списка диалогов через определенный период
     */
    private class TimerRefreshDialogsList extends CountDownTimer {

        TimerRefreshDialogsList(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            updateDialogs();
        }

        @Override
        public void onFinish() {
            finish();
        }
    }

    /**
     * Получение по API обновленного списка диалогов
     */
    private void updateDialogs() {
        VKRequest requestMessages = VKApi.messages().getDialogs(VKParameters.from(COUNT, AMOUNT_DIALOGS));
        requestMessages.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                refreshDialogsList(response);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });
    }

    /**
     * Обновление списка диалогов
     * @param response - ответ, полученный от запроса по API
     */
    private void refreshDialogsList(VKResponse response) {
        final ArrayList<String> users = mDialogsAdapter.getUsers();
        ArrayList<String> messages = mDialogsAdapter.getMessages();
        ArrayList<String> idsList = mDialogsAdapter.getIdsList();
        ArrayList<String> photosUrlsList = mDialogsAdapter.getPhotosUrls();

        try {
            JSONObject jsonResponse = (JSONObject) response.json.get(RESPONSE);
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

                String dialogPhoto = getDialogPhoto(jsonMessage, isChat);

                // иначе не работает! причина неизвестна
                if(String.valueOf(idsList.contains(chatId)).equals("false")) {
                    idsList.add(0, chatId);
                    photosUrlsList.add(0, dialogPhoto);

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
                                users.add(0, String.valueOf(dialogTitle[0]));
                            }
                        });
                    } else {
                        users.add(0, dialogTitle[0]);
                    }
                    messages.add(0, jsonMessage.getString(BODY) + " ");

                } else {

                    int dialogIndex = idsList.indexOf(chatId);
                    if (!messages.get(dialogIndex).equals(jsonMessage.getString(BODY))) {
                        String dialogTitle = users.get(dialogIndex);

                        idsList.remove(dialogIndex);
                        photosUrlsList.remove(dialogIndex);
                        messages.remove(dialogIndex);
                        users.remove(dialogIndex);

                        idsList.add(0, chatId);
                        photosUrlsList.add(0, dialogPhoto);
                        messages.add(0, jsonMessage.getString(BODY));
                        users.add(0, dialogTitle);
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateDialogsAdapter(users, idsList, photosUrlsList, messages);
    }

    /**
     * Получение изображения диалога
     * @param jsonMessage - json-объект сообщения
     * @param isChat - диалог с одним человеком или беседа
     * @return - ссылка на фото диалога
     */
    private String getDialogPhoto(JSONObject jsonMessage, boolean isChat) {
        String dialogPhoto;
        try {
            dialogPhoto = jsonMessage.getString(PHOTO_50);
        } catch (JSONException e) {
            if (!isChat) {
                try {
                    dialogPhoto = dbHelper.getFromUsersPhoto50UrlById(jsonMessage.getString(USER_ID));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    dialogPhoto = "0";
                }
            } else {
                dialogPhoto = "0";
            }
        }

        return dialogPhoto;
    }

    /**
     * Обновление адаптера списка диалогов
     * @param users - обновленный список названий диалогов
     * @param idsList - обновленный список идентфикаторов диалогов
     * @param messages - обновленный список последних сообщений диалогов
     */
    private void updateDialogsAdapter(
            ArrayList<String> users,
            ArrayList<String> idsList,
            ArrayList<String> photosUrlsList,
            ArrayList<String> messages
    ) {
        mDialogsAdapter.setUsers(users);
        mDialogsAdapter.setIdsList(idsList);
        mDialogsAdapter.setPhotosUrls(photosUrlsList);
        mDialogsAdapter.setMessages(messages);
        mDialogsAdapter.notifyDataSetChanged();
    }

    /**
     * Инициализация слушателя для основной кнопки
     */
    private void initializeFabListener() {
        mainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSetCreateChatPageAdapter) {
                    setCreateChatMode();
                } else {
                    setDefaultModeFromCreateChat();
                }
            }
        });
    }

    /**
     * Выполнение сложных операций, инициализация пользовательского интерфейса
     */
    private class InitializeData extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            initializeFirstPageAdapterDefault();
            initializePageAdapterCreateChat();
            initializePageAdapterSearch();

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            initUI();

            loadingView.animate().alpha(0.0f).setDuration(1500);
            viewPager.animate().alpha(1.0f).setDuration(1500);
            mainFab.animate().alpha(1.0f).setDuration(1500);

            initializeNavigationTabBar();
        }
    }

    /**
     * Получение имени и фамилии авторизованного пользователя
     */
    private void getThisProfileNickname() {
        VKRequest requestThisProfileInfo = new VKRequest(GET_PROFILE_INFO, VKParameters.from(FIELDS, FIRST_NAME + COMMA + LAST_NAME));
        requestThisProfileInfo.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                JSONObject list = response.json;
                try {
                    JSONObject a = (JSONObject) list.get(RESPONSE);
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

    /**
     * Составление списка диалогов с их последним сообщением
     */
    private void completeRequestMessages(VKList<VKApiDialog> list, final ArrayList<String> users,
                                         ArrayList<String> messages) {

        for (final VKApiDialog msg : list) {
            final String[] dialogTitle = {msg.message.title};

            if (dialogTitle[0].equals("")) {
                VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, msg.message.user_id));

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
            messages.add(msg.message.body);
        }
    }

    /**
     * Получение адаптера для формирования списка диалогов
     */
    private DialogsAdapter getDialogsAdapter(VKResponse response) throws JSONException {

        ArrayList<String> users = new ArrayList<String>();
        ArrayList<String> messages = new ArrayList<String>();
        ArrayList<String> idsList = new ArrayList<>();
        ArrayList<String> photosUrls = new ArrayList<>();

        VKApiGetDialogResponse getMessagesResponse = (VKApiGetDialogResponse) response.parsedModel;

        JSONObject jsonResponse = (JSONObject) response.json.get(RESPONSE);
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

            String dialogPhoto;
            try {
                dialogPhoto = jsonMessage.getString(PHOTO_50);
            } catch (JSONException e) {
                if (!isChat) {
                    dialogPhoto = dbHelper.getFromUsersPhoto50UrlById(jsonMessage.getString(USER_ID));
                    if (dialogPhoto.equals("-1")) {

                        final VKRequest request = VKApi.users().get(VKParameters.from(
                                VKApiConst.USER_ID, jsonMessage.getString(USER_ID),
                                FIELDS, PHOTO_50)
                        );
                        final JSONObject finalJsonMessage = jsonMessage;

                        request.executeWithListener(new VKRequest.VKRequestListener() {
                            @Override
                            public void onError(VKError error) {
                                super.onError(error);
                            }

                            @Override
                            public void onComplete(VKResponse response) {
                                super.onComplete(response);

                                VKList list = (VKList) response.parsedModel;
                                String nickname = String.valueOf(list.get(0));

                                try {
                                    String photo50Url = list.get(0).fields.getString(PHOTO_50);
                                    dbHelper.insertUsers(finalJsonMessage.getString(USER_ID), nickname, photo50Url);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        dialogPhoto = dbHelper.getFromUsersPhoto50UrlById(jsonMessage.getString(USER_ID));
                    }
                } else {
                    dialogPhoto = "0";
                }
            }
            idsList.add(chatId);
            photosUrls.add(dialogPhoto);
        }

        VKList<VKApiDialog> list = getMessagesResponse.items;
        completeRequestMessages(list, users, messages);

        mDialogsAdapter = new DialogsAdapter(users, messages, MainActivity.this, idsList, photosUrls, dbHelper);
        return mDialogsAdapter;
    }

    /**
     * Получение адаптера для формирования списка друзей
     */
    private FriendsAdapter getFriendsAdapter(VKResponse response) {
        VKList list = (VKList) response.parsedModel;
        mFriendsAdapter = new FriendsAdapter(MainActivity.this, list);
        return mFriendsAdapter;
    }

    /**
     * Установка настроек RecyclerView
     */
    private void setRecyclerViewConfiguration(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        getBaseContext(), LinearLayoutManager.VERTICAL, false
                )
        );
    }

    /**
     * Инициализация основных вкладок активности
     */
    private ArrayList<NavigationTabBar.Model> initializeModels() {
        ArrayList<NavigationTabBar.Model> models = new ArrayList<>();

        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_message_text),
                        ContextCompat.getColor(this, R.color.colorLightBlue))
                        .title(DIALOGS_PAGE)
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_account_multiple),
                        ContextCompat.getColor(this, R.color.colorLightBlue))
                        .title(FRIENDS_PAGE)
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_settings),
                        ContextCompat.getColor(this, R.color.colorLightBlue))
                        .title(SETTINGS_PAGE)
                        .build()
        );

        return models;
    }

    private void setNavigationTabBarConfiguration(NavigationTabBar navigationTabBar) {
        navigationTabBar.setViewPager(viewPager, 0);
        //IMPORTANT: ENABLE SCROLL BEHAVIOUR IN COORDINATOR LAYOUT
        navigationTabBar.setBehaviorEnabled(true);
    }

    private void setNavigationTabBarListeners(NavigationTabBar navigationTabBar) {
        setOnTabBarSelectedIndexListener(navigationTabBar);
        setOnPageChangeListener(navigationTabBar);
    }

    private void setOnTabBarSelectedIndexListener(NavigationTabBar navigationTabBar) {
        navigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(final NavigationTabBar.Model model, final int index) {
            }

            @Override
            public void onEndTabSelected(final NavigationTabBar.Model model, final int index) {
                model.hideBadge();
            }
        });
    }

    private void setOnPageChangeListener(NavigationTabBar navigationTabBar) {
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {

            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });
    }

    private void initializeNavigationTabBar() {
        NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        navigationTabBar.setModels(initializeModels());
        navigationTabBar.animate().alpha(1.0f).setDuration(1500);

        setNavigationTabBarConfiguration(navigationTabBar);
        setNavigationTabBarListeners(navigationTabBar);
    }

    private void reportJSONException() {
        Log.i(DEBUG_TAG, "json exception");
    }

    /**
     * Инициализация таблицы пользователей в локальной БД
     */
    private void initializeUsersTable() {
        /**
         * Получение информации о друзьях авторизованного пользователя
         */
        final VKRequest requestFriends = VKApi.friends().get(
                VKParameters.from(FIELDS, FIRST_NAME + COMMA + LAST_NAME + COMMA + ID + COMMA + PHOTO_50)
        );

        requestFriends.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                VKList list = (VKList)  response.parsedModel;
                for (int i = 0; i < list.size(); i++) {
                    try {
                        /**
                         * Добавление пользователя в локальную БД
                         */
                        dbHelper.insertUsers(
                                list.get(i).fields.getString(ID),
                                list.get(i).toString(),
                                list.get(i).fields.getString(PHOTO_50)
                        );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
//                initializeMessagesTable();
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
            }
        });
    }

    /**
     * Первая инициализация стандартного адаптера вкладок (с запросами к API)
     */
    private void initializeFirstPageAdapterDefault() {
        mPagerAdapterDefault = new PagerAdapter() {
            /**
             * Кол-во вкладок
             */
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                View view = LayoutInflater.from(
                        getBaseContext()).inflate(R.layout.item_vp_list, null, false);

                final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv);
                final SwipeMenuListView mListView = (SwipeMenuListView) view.findViewById(R.id.listView);

                setRecyclerViewConfiguration(recyclerView);

                if (position == 0) {
                    /**
                     * Вкладка диалогов
                     */

                    /**
                     * Получение последних "count" диалогов с их последними сообщениями
                     */
                    VKRequest requestMessages = VKApi.messages().getDialogs(VKParameters.from(COUNT, AMOUNT_DIALOGS));
                    requestMessages.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            try {
                                /**
                                 * Установка адаптера диалогов
                                 */
                                mListView.setVisibility(View.INVISIBLE);
                                recyclerView.setVisibility(View.VISIBLE);

                                recyclerView.setAdapter(getDialogsAdapter(response));
                            } catch (JSONException e) {
                                reportJSONException();
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                        }
                    });

                } else if (position == 1) {
                    /**
                     * Вкладка друзей
                     */

                    /**
                     * Получение необходимой информации о друзьях, сортировка в порядке важности
                     */
                    final VKRequest requestFriends = VKApi.friends().get(VKParameters.from(
                            FIELDS, FIRST_NAME + COMMA + LAST_NAME + COMMA + DOMAIN + COMMA + PHOTO_50,
                            ORDER, HINTS));

                    requestFriends.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            /**
                             * Установка адаптера друзей
                             */
                            mListView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.INVISIBLE);

                            mListView.setAdapter(getFriendsAdapter(response));
                            mListView.setMenuCreator(getSwipeMenuCreatorFriends());

                            mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                                    if (index == 0) {
                                        openDialogByPosition(position);
                                    }
                                    else if (index == 1) {
                                        mFriendsAdapter.removeFriend(position);
                                    }
                                    return true;
                                }
                            });
                            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    mListView.smoothOpenMenu(position);
                                }
                            });
                        }

                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                        }
                    });
                } else {
                    /**
                     * Вкладка настроек (?)
                     */
                    recyclerView.setAdapter(new CustomAdapter(MainActivity.this));
                }

                container.addView(view);
                return view;
            }
        };
    }

    /**
     * Открытие диалога с выбранным пользователем по его позиции в списке друзей
     * @param position - позиция друга в списке
     */
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
        final String id = mFriendsAdapter.getFriendId(position);

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
                    startActivity(new Intent(MainActivity.this, MessagesActivity.class)
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
                VKRequest request = VKApi.users().get(VKParameters.from(
                        VKApiConst.USER_ID, fwdMessage.user_id,
                        FIELDS, PHOTO_50
                ));
                request.executeSyncWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);

                        VKList list = (VKList) response.parsedModel;
                        fwdMessageUser[0] = String.valueOf(list.get(0));

                        try {
                            String photo50Url = list.get(0).fields.getString(PHOTO_50);
                            dbHelper.insertUsers(String.valueOf(fwdMessage.user_id), fwdMessageUser[0], photo50Url);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
     * Вторичная инициализация стандартного адаптера вкладок (Без запросо к API)
     */
    private void initializePageAdapterDefault() {
        mPagerAdapterDefault = new PagerAdapter() {
            /**
             * Кол-во вкладок
             */
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                final View view = LayoutInflater.from(
                        getBaseContext()).inflate(R.layout.item_vp_list, null, false);

                final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv);
                final SwipeMenuListView mListView = (SwipeMenuListView) view.findViewById(R.id.listView);
                setRecyclerViewConfiguration(recyclerView);

                if (position == 0) {
                    mListView.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);

                    recyclerView.setAdapter(mDialogsAdapter);
                } else if (position == 1) {
                    mListView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);

                    mListView.setAdapter(mFriendsAdapter);
                    mListView.setMenuCreator(getSwipeMenuCreatorFriends());

                    mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                            if (index == 0) {
                                openDialogByPosition(position);
                            }
                            else if (index == 1) {
                                mFriendsAdapter.removeFriend(position);
                            }
                            return true;
                        }
                    });

                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            mListView.smoothOpenMenu(position);
                        }
                    });

                } else {
                    /**
                     * Вкладка настроек (?)
                     */
                    recyclerView.setAdapter(new CustomAdapter(MainActivity.this));
                }

                container.addView(view);
                return view;
            }
        };
    }

    /**
     * Инициализация меню для смахивания элементов в списке друзей
     */
    private SwipeMenuCreator getSwipeMenuCreatorFriends() {
        return new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openDialogItem = new SwipeMenuItem(
                        getApplicationContext());
                openDialogItem.setBackground(R.color.colorLightGrey);
                openDialogItem.setWidth(dp2px(72));
                openDialogItem.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_lead_pencil));
                openDialogItem.setTitleSize(12);
                openDialogItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(openDialogItem);

                // create "remove" item
                SwipeMenuItem removeFriendItem = new SwipeMenuItem(
                        getApplicationContext());
                removeFriendItem.setBackground(R.color.colorLightGrey);
                removeFriendItem.setWidth(dp2px(72));
                removeFriendItem.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_close));
                removeFriendItem.setTitleSize(12);
                removeFriendItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(removeFriendItem);
            }
        };
    }

    /**
     * Конвертация их dp в px
     * @param dp - знаичение в dp
     * @return - значение в px
     */
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    /**
     * Инициализация адаптера вкладок с поиском пользователей
     */
    private void initializePageAdapterSearch() {
        mPagerAdapterSearch = new PagerAdapter() {
            /**
             * Кол-во вкладок
             */
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                final View view = LayoutInflater.from(
                        getBaseContext()).inflate(R.layout.item_vp_list, null, false);

                final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv);
                final SwipeMenuListView mListView = (SwipeMenuListView) view.findViewById(R.id.listView);

                setRecyclerViewConfiguration(recyclerView);

                if (position == 0) {
                    mListView.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);

                    recyclerView.setAdapter(mDialogsAdapter);
                } else if (position == 1) {
                    mListView.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);

                    mSearchUsersAdapter = new SearchUsersAdapter(MainActivity.this, new VKList());
                    recyclerView.setAdapter(mSearchUsersAdapter);

                } else {
                    /**
                     * Вкладка настроек (?)
                     */

                    recyclerView.setAdapter(new CustomAdapter(MainActivity.this));
                }
                container.addView(view);
                return view;
            }
        };
    }

    /**
     * Инициализация адаптера вкладок с созданием чата
     */
    private void initializePageAdapterCreateChat() {
        mPagerAdapterCreateChat = new PagerAdapter() {
            /**
             * Кол-во вкладок
             */
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                final View view = LayoutInflater.from(
                        getBaseContext()).inflate(R.layout.item_vp_list, null, false);

                final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv);
                final SwipeMenuListView mListView = (SwipeMenuListView) view.findViewById(R.id.listView);
                setRecyclerViewConfiguration(recyclerView);

                if (position == 0) {
                    mListView.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);

                    mCreateChatAdapter = new CreateChatAdapter(MainActivity.this, mFriendsAdapter.getListFriends());
                    recyclerView.setAdapter(mCreateChatAdapter);
                } else if (position == 1) {
                    mListView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);

                    mListView.setAdapter(mFriendsAdapter);
                    mListView.setMenuCreator(getSwipeMenuCreatorFriends());

                    mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                            if (index == 0) {
                                openDialogByPosition(position);
                            }
                            else if (index == 1) {
                                mFriendsAdapter.removeFriend(position);
                            }
                            return true;
                        }
                    });

                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            mListView.smoothOpenMenu(position);
                        }
                    });
                } else {
                    /**
                     * Вкладка настроек (?)
                     */

                    recyclerView.setAdapter(new CustomAdapter(MainActivity.this));
                }
                container.addView(view);
                return view;
            }
        };
    }

    /**
     * Установка адаптера вкладок с созданием чата
     */
    private void setCreateChatMode() {
        mainFab.setImageDrawable(
                ContextCompat.getDrawable(MainActivity.this,
                        R.drawable.ic_wechat)
        );

        viewPager.removeAllViews();
        viewPager.setAdapter(null);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(mPagerAdapterCreateChat);
        viewPager.setCurrentItem(0);

        isSetCreateChatPageAdapter = true;
        isSetSearchPageAdapter = false;
    }

    /**
     * Установка стандартного адаптера вкладок после адаптера вкладок с созданием чата
     */
    public void setDefaultModeFromCreateChat() {
        mainFab.setImageDrawable(
                ContextCompat.getDrawable(MainActivity.this,
                        R.drawable.ic_plus)
        );

        initializePageAdapterDefault();
        viewPager.removeAllViews();
        viewPager.setAdapter(null);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(mPagerAdapterDefault);
        viewPager.setCurrentItem(0);

        isSetCreateChatPageAdapter = false;
        isSetSearchPageAdapter = false;
    }

    /**
     * Установка адаптера вкладок с поиском пользователей
     */
    private void setSearchMode() {
        mainFab.setImageDrawable(
                ContextCompat.getDrawable(MainActivity.this,
                        R.drawable.ic_account_multiple)
        );
        mainFab.refreshDrawableState();

        viewPager.removeAllViews();
        viewPager.setAdapter(null);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(mPagerAdapterSearch);
        viewPager.setCurrentItem(1);

        isSetSearchPageAdapter = true;
        isSetCreateChatPageAdapter = false;
    }

    /**
     * Установка стандартного адаптера вкладок после адаптера вкладок с поиском пользователей
     */
    private void setDefaultModeFromSearch() {
        mainFab.setImageDrawable(
                ContextCompat.getDrawable(MainActivity.this,
                        R.drawable.ic_plus)
        );

        initializePageAdapterDefault();
        viewPager.removeAllViews();
        viewPager.setAdapter(null);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(mPagerAdapterDefault);
        viewPager.setCurrentItem(1);

        isSetSearchPageAdapter = false;
        isSetCreateChatPageAdapter = false;
    }

    /**
     * Формирование пользовательского интерфейса
     */
    private void initUI() {

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(mPagerAdapterDefault);

        isSetCreateChatPageAdapter = false;
        isSetSearchPageAdapter = false;

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if(position == 0) {
                    mainFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isSetCreateChatPageAdapter) {
                                setCreateChatMode();
                            } else {
                                setDefaultModeFromCreateChat();
                            }
                        }
                    });
                } else if (position == 1) {
                    mainFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isSetSearchPageAdapter) {
                               setSearchMode();
                            } else {
                               setDefaultModeFromSearch();
                            }
                        }
                    });
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

//        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                for (int i = 0; i < navigationTabBar.getModels().size(); i++) {
//                    final NavigationTabBar.Model model = navigationTabBar.getModels().get(i);
//                    navigationTabBar.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            final String title = String.valueOf(new Random().nextInt(15));
//                            if (!model.isBadgeShowed()) {
//                                model.setBadgeTitle(title);
//                                model.showBadge();
//                            } else model.updateBadgeTitle(title);
//                        }
//                    }, i * 100);
//                }
//
//            }
//        });
    }

    /**
     * Получение имени и фамилии пользователя по его идентификатору через API VK
     */
    private void requestUserNickname(
            final String chatId,
            final VKApiMessage message
    ) {
        final VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, message.user_id, FIELDS, PHOTO_50));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onError(VKError error) {
                        super.onError(error);
                        Log.i(DEBUG_TAG, "Error find nickname:" + message.user_id);
                        Log.i(DEBUG_TAG, String.valueOf(error));
                    }

                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);

                        VKList list = (VKList) response.parsedModel;
                        String nickname = String.valueOf(list.get(0));
                        try {
                            String photo50Url = list.get(0).fields.getString(PHOTO_50);
                            dbHelper.insertUsers(String.valueOf(message.user_id), nickname, photo50Url);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, timeDelay += 350);
    }

    /**
     * Получение имени и фимилии пользователя по его идентификатору
     */
    private String getNicknameById(final String id) {
        /**
         *Попытка получить через локальную бд
         */
        final String[] nickname = {dbHelper.getFromUsersNicknameById(id)};
        if (nickname[0] == null) {
            /**
             * Иначе через API VK
             */
            final VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, id, FIELDS, PHOTO_50));
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    request.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                            Log.i(DEBUG_TAG, "Error find nickname:" + id);
                            Log.i(DEBUG_TAG, String.valueOf(error));
                        }

                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);

                            VKList list = (VKList) response.parsedModel;
                            nickname[0] = String.valueOf(list.get(0));

                            try {
                                String photo50Url = list.get(0).fields.getString(PHOTO_50);
                                dbHelper.insertUsers(id, nickname[0], photo50Url);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            }, timeDelay += 350);
        }
        return nickname[0];
    }
}
