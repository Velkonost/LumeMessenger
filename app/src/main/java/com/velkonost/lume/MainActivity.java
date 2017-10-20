package com.velkonost.lume;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.velkonost.lume.vkontakte.db.DBHelper;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;

import static com.velkonost.lume.Constants.COMMA;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIELDS;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.FIRST_NAME;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.ID;
import static com.velkonost.lume.vkontakte.Constants.API_PARAMETERS.LAST_NAME;

public class MainActivity extends AppCompatActivity {

    /**
     * Массив с информацией, какие данные следует получить при авторизации
     * */
    private String[] scope = new String[]{
            VKScope.MESSAGES, VKScope.FRIENDS
    };

    /**
     * Локальная БД
     */
    private DBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        Button btn = (Button) findViewById(R.id.vk);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainActivity.this, com.velkonost.lume.vkontakte.activities.MainActivity.class);
                //MainActivity.this.startActivity(intent);
                if(VKSdk.isLoggedIn()) {
                    startActivity(new Intent(MainActivity.this, com.velkonost.lume.vkontakte.activities.MainActivity.class));
//            finish();
                } else {
                    VKSdk.login(MainActivity.this, scope);
                }

            }
        });

        ((Button) findViewById(R.id.test)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dbmanager = new Intent(MainActivity.this, AndroidDatabaseManager.class);
                startActivity(dbmanager);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
                //Toast.makeText(getApplicationContext(), "Good", Toast.LENGTH_LONG).show();
                // startActivity(new Intent(MainActivity.this, AllAct.class));
                //finish();
                initializeUsersTable();
                startActivity(new Intent(MainActivity.this, com.velkonost.lume.vkontakte.activities.MainActivity.class));
//                finish();
            }

            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                //Toast.makeText(getApplicationContext(), "Bad", Toast.LENGTH_LONG).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
}
