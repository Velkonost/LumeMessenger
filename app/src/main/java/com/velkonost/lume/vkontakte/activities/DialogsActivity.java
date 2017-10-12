package com.velkonost.lume.vkontakte.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

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
import com.vk.sdk.api.model.VKList;

import java.util.ArrayList;

/**
 * @author Velkonost
 */

public class DialogsActivity extends AppCompatActivity {

    private String[] scope = new String[]{
            VKScope.MESSAGES, VKScope.FRIENDS
    };

    private ListView mListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_vkontakte_dialogs);

//        mListView = (ListView) findViewById(R.id.listView);

        if(VKSdk.isLoggedIn()) {
            executeRequestMessages();
//            VKSdk.logout();
        } else {
            VKSdk.login(this, scope);
        }
    }


    private void executeRequestMessages() {
        final VKRequest requestMessages = VKApi.messages().getDialogs(VKParameters.from("count", 10));
        requestMessages.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                VKApiGetDialogResponse getMessagesResponse = (VKApiGetDialogResponse) response.parsedModel;

                VKList<VKApiDialog> list = getMessagesResponse.items;

                final ArrayList<String> messages = new ArrayList<String>();
                final ArrayList<String> users = new ArrayList<String>();

                for(final VKApiDialog msg : list) {
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

//                mListView.setAdapter(new CustomAdapter(users, messages, DialogsActivity.this, list));
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

    private void failLogin() {
        Toast.makeText(getApplicationContext(), "FailLogin", Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
                executeRequestMessages();
            }
            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                failLogin();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
