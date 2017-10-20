package com.velkonost.lume.vkontakte.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.adapters.CustomAdapter;
import com.velkonost.lume.vkontakte.adapters.DialogsAdapter;
import com.velkonost.lume.vkontakte.adapters.FriendsAdapter;
import com.velkonost.lume.vkontakte.db.DBHelper;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
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

import java.util.ArrayList;

import devlight.io.library.ntb.NavigationTabBar;

import static com.velkonost.lume.Constants.COMMA;
import static com.velkonost.lume.Constants.DEBUG_TAG;
import static com.velkonost.lume.vkontakte.Constants.API_METHODS.GET_PROFILE_INFO;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.AMOUNT_DIALOGS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.COUNT;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.DOMAIN;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIELDS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIRST_NAME;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.HINTS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.ID;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.LAST_NAME;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.ORDER;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.CHAT_ID;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.ITEMS;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.MESSAGE;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.RESPONSE;
import static com.velkonost.lume.vkontakte.Constants.RESPONSE_FIELDS.USER_ID;
import static com.velkonost.lume.vkontakte.Constants.VIEW_PAGER_PAGES.DIALOGS_PAGE;
import static com.velkonost.lume.vkontakte.Constants.VIEW_PAGER_PAGES.FRIENDS_PAGE;
import static com.velkonost.lume.vkontakte.Constants.VIEW_PAGER_PAGES.SETTINGS_PAGE;

/**
 * Основная активность
 */

public class MainActivity extends AppCompatActivity {

    /**
     * Массив с информацией, какие данные следует получить при авторизации
     * */
    private String[] scope = new String[]{
            VKScope.MESSAGES, VKScope.FRIENDS
    };

    private ViewPager viewPager;

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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vkontakte_main);

        viewPager = (ViewPager) findViewById(R.id.vp_horizontal_ntb);
        dbHelper = new DBHelper(this);

        initUI();
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

        VKApiGetDialogResponse getMessagesResponse = (VKApiGetDialogResponse) response.parsedModel;

        JSONObject jsonResponse = (JSONObject) response.json.get(RESPONSE);
        JSONArray messagesJSONArray = jsonResponse.getJSONArray(ITEMS);

        ArrayList<String> idsList = new ArrayList<>();
        for (int i = 0; i < messagesJSONArray.length(); i++) {
                JSONObject jsonMessage = (JSONObject) messagesJSONArray.get(i);
                jsonMessage = jsonMessage.getJSONObject(MESSAGE);

                String chatId;
                try {
                    chatId = jsonMessage.getString(CHAT_ID);
                } catch (JSONException e) {
                    chatId = jsonMessage.getString(USER_ID);
                }
                idsList.add(chatId);
            }

        VKList<VKApiDialog> list = getMessagesResponse.items;
        completeRequestMessages(list, users, messages);

        return new DialogsAdapter(users, messages, MainActivity.this, idsList, dbHelper);
    }

    /**
     * Получение адаптера для формирования списка друзей
     */
    private FriendsAdapter getFriendsAdapter(VKResponse response) {
        VKList list = (VKList) response.parsedModel;
        return new FriendsAdapter(MainActivity.this, list);
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
        String[] colors = getResources().getStringArray(R.array.default_preview);

        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_first),
                        Color.parseColor(colors[0]))
                        .title(DIALOGS_PAGE)
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_second),
                        Color.parseColor(colors[1]))
                        .title(FRIENDS_PAGE)
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_third),
                        Color.parseColor(colors[2]))
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
        final VKRequest requestFriends = VKApi.friends().get(VKParameters.from(FIELDS, FIRST_NAME + COMMA + LAST_NAME + COMMA + ID));

        requestFriends.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                VKList list = (VKList) response.parsedModel;
                for (int i = 0; i < list.size(); i++) {
                    try {
                        /**
                         * Добавление пользователя в локальную БД
                         */
                        dbHelper.insertUsers(list.get(i).fields.getString(ID), list.get(i).toString());
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
     * Формирование пользовательского интерфейса
     */
    private void initUI() {

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(new PagerAdapter() {
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
                setRecyclerViewConfiguration(recyclerView);

                if(!VKSdk.isLoggedIn()) {
                    /**
                     * Если пользователь еще не авторизован - перебрасывает на авторизацию
                     */
                    VKSdk.login(MainActivity.this, scope);
                } else {

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
                                    recyclerView.setAdapter(getDialogsAdapter(response));
                                } catch (JSONException e) {
                                    reportJSONException();
                                    e.printStackTrace();
                                }
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

                    } else if (position == 1) {
                        /**
                         * Вкладка друзей
                         */

                        /**
                         * Получение необходимой информации о друзьях, сортировка в порядке важности
                         */
                        final VKRequest requestFriends = VKApi.friends().get(VKParameters.from(
                                FIELDS, FIRST_NAME + COMMA + LAST_NAME + COMMA + DOMAIN,
                                ORDER, HINTS));

                        requestFriends.executeWithListener(new VKRequest.VKRequestListener() {
                            @Override
                            public void onComplete(VKResponse response) {
                                super.onComplete(response);
                                /**
                                 * Установка адаптера друзей
                                 */
                                recyclerView.setAdapter(getFriendsAdapter(response));
                            }

                            @Override
                            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                                super.attemptFailed(request, attemptNumber, totalAttempts);
                            }

                            @Override
                            public void onError(VKError error) {
                                super.onError(error);
                                Log.i(DEBUG_TAG, String.valueOf(error));
                            }

                            @Override
                            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                                super.onProgress(progressType, bytesLoaded, bytesTotal);
                            }
                        });
                    } else {
                        /**
                         * Вкладка настроек (?)
                         */
                        recyclerView.setAdapter(new CustomAdapter(MainActivity.this));
                    }
                }

                container.addView(view);
                return view;
            }
        });

        initializeNavigationTabBar();

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

//    private void initializeMessagesTable() {
//        VKRequest requestMessages = VKApi.messages().getDialogs(VKParameters.from("count", 10));
//        requestMessages.executeWithListener(new VKRequest.VKRequestListener() {
//
//            @Override
//            public void onComplete(VKResponse response) {
//                super.onComplete(response);
//                try {
//                    JSONObject jsonResponse = (JSONObject) response.json.get("response");
//                    JSONArray messagesJSONArray = jsonResponse.getJSONArray("items");
//
//                    for (int i = 0; i < messagesJSONArray.length(); i++) {
//                        JSONObject jsonMessage = (JSONObject) messagesJSONArray.get(i);
//                        jsonMessage = jsonMessage.getJSONObject("message");
//
//                        String chatId;
//                        String typeOfDialog;
//                        try {
//                            chatId = jsonMessage.getString("chat_id");
//                            typeOfDialog = "chat_id";
//                        } catch (JSONException e) {
//                            chatId = jsonMessage.getString("user_id");
//                            typeOfDialog = "user_id";
//                        }
//
//                        final String finalChatId = chatId;
//                        final JSONObject finalJsonMessage = jsonMessage;
//                        final String finalTypeOfDialog = typeOfDialog;
//
//                        Handler handler = new Handler();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                VKRequest request = new VKRequest("messages.getHistory", VKParameters.from(finalTypeOfDialog, finalChatId));
//                                request.executeWithListener(new VKRequest.VKRequestListener() {
//                                    @Override
//                                    public void onError(VKError error) {
//                                        super.onError(error);
//                                        Log.i(DEBUG_TAG, "Error getHistory:" + finalJsonMessage.toString());
//                                        Log.i(DEBUG_TAG, String.valueOf(error));
//                                    }
//
//                                    @Override
//                                    public void onComplete(VKResponse response) {
//                                        super.onComplete(response);
//
//                                        try {
//                                            JSONArray array = response.json.getJSONObject("response").getJSONArray("items");
//                                            VKApiMessage[] msg = new VKApiMessage[array.length()];
//
//                                            for (int i = 0; i < array.length(); i++) {
//                                                VKApiMessage mes = new VKApiMessage(array.getJSONObject(i));
//                                                msg[i] = mes;
//                                            }
//
//                                            for (final VKApiMessage message : msg) {
//                                                Handler handler = new Handler();
//                                                handler.postDelayed(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        final String id = String.valueOf(message.user_id);
//                                                        final String[] nickname = {dbHelper.getFromUsersNicknameById(id)};
//                                                        if (message.out) {
//                                                            nickname[0] = "Я";
//                                                        }
//
//                                                        if (nickname[0] == null) {
//                                                            requestUserNickname(finalChatId, message);
//                                                        } else {
//                                                            addNewMessageInDB(finalChatId, message, nickname[0]);
//                                                        }
//                                                    }}, timeDelay += 600);
//                                            }
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });
//                            }
//                        }, timeDelay += 800);
//                    }
//                } catch (JSONException e) {
//                    reportJSONException();
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(VKError error) {
//                super.onError(error);
//            }
//        });
//    }

    /**
     * Получение имени и фамилии пользователя по его идентификатору через API VK
     */
    private void requestUserNickname(
            final String chatId,
            final VKApiMessage message
    ) {
        final VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, message.user_id));
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
                        dbHelper.insertUsers(String.valueOf(message.user_id), nickname);
//                        addNewMessageInDB(chatId, message, nickname);

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
            final VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_ID, id));
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
                            dbHelper.insertUsers(id, nickname[0]);

                        }
                    });
                }
            }, timeDelay += 350);
        }
        return nickname[0];
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
                /**
                 * Инициализация таблицы пользователей в локальной БД при успешной авторизации
                 */
//                initializeUsersTable();
//                initializeMessagesTable();
                /**
                 * Формирование пользовательского интерфейса после успешной авторизации
                 */
                initUI();

            }
            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
