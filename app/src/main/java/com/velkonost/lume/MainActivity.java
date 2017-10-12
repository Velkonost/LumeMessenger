package com.velkonost.lume;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKList;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mListView = (ListView) findViewById(R.id.listView);
        Button btn = (Button) findViewById(R.id.test);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.velkonost.lume.vkontakte.activities.MainActivity.class);
                MainActivity.this.startActivity(intent);

            }
        });

        final VKList[] listFriends = {null};
        if(VKSdk.isLoggedIn())
        {
            Toast.makeText(getApplicationContext(), "Already", Toast.LENGTH_SHORT).show();

            final VKRequest requestFriends = VKApi.friends().get(VKParameters.from("fields", "first_name, last_name, domain", "order", "hints"));


//            requestFriends.executeWithListener(new VKRequest.VKRequestListener() {
//                @Override
//                public void onComplete(VKResponse response) {
//                    super.onComplete(response);
//
//
//                    listFriends[0] = (VKList) response.parsedModel;
//
//                    Log.i("Список полей", String.valueOf(listFriends[0].get(0)));
//                    ArrayAdapter<String> arrayAdapterFriends = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, listFriends[0]);
////                    mListView.setAdapter(arrayAdapter);
//
//
//                }
//
//                @Override
//                public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
//                    super.attemptFailed(request, attemptNumber, totalAttempts);
//                }
//
//                @Override
//                public void onError(VKError error) {
//                    super.onError(error);
//                }
//
//                @Override
//                public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
//                    super.onProgress(progressType, bytesLoaded, bytesTotal);
//                }
//            });


        } else {
//            VKSdk.login(this, scope);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();

                VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "first_name,last_name,user_id,order"));
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);

                        VKList list = (VKList) response.parsedModel;
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, list);
                        mListView.setAdapter(arrayAdapter);
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
