package com.velkonost.lume.instagram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.instagram.instagramapi.exceptions.InstagramException;
import com.instagram.instagramapi.interfaces.InstagramLoginCallbackListener;
import com.instagram.instagramapi.objects.IGSession;
import com.instagram.instagramapi.utils.InstagramKitLoginScope;
import com.instagram.instagramapi.widgets.InstagramLoginButton;
import com.velkonost.lume.R;

/**
 * Created by admin on 22.10.2017.
 */

public class MainActivity extends AppCompatActivity {
    private InstagramLoginButton instagramLoginButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instagram_main);

        String[] scopes = {InstagramKitLoginScope.BASIC, InstagramKitLoginScope.COMMENTS};

        instagramLoginButton = (InstagramLoginButton) findViewById(R.id.instagramLoginButton);
        instagramLoginButton.setInstagramLoginCallback(instagramLoginCallbackListener);
        //if you dont specify scopes, you will have basic access.
        instagramLoginButton.setScopes(scopes);
    }

    InstagramLoginCallbackListener instagramLoginCallbackListener = new InstagramLoginCallbackListener() {
        @Override
        public void onSuccess(IGSession session) {

//            Toast.makeText(MainActivity.this, "Wow!!! User trusts you :) " + session.getAccessToken(),
//                    Toast.LENGTH_LONG).show();
//            instagramLoginButton.setVisibility(View.INVISIBLE);
            Intent instIntent = new Intent(MainActivity.this, InstagramActivity.class);
            startActivity(instIntent);
            finish();
        }

        @Override
        public void onCancel() {
            Toast.makeText(MainActivity.this, "Oh Crap!!! Canceled.",
                    Toast.LENGTH_LONG).show();

        }

        @Override
        public void onError(InstagramException error) {
            Toast.makeText(MainActivity.this, "User does not trust you :(\n " + error.getMessage(),
                    Toast.LENGTH_LONG).show();

        }
    };
}
