package com.velkonost.lume.facebook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.velkonost.lume.R;

import java.util.Arrays;

/**
 * Created by admin on 07.11.2017.
 */

public class MainActivity extends AppCompatActivity {
    private LoginButton fb_btn;
    private CallbackManager callbackManager;
    private LoginManager lb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fb_auth);

        callbackManager = CallbackManager.Factory.create();
        lb = LoginManager.getInstance();
        fb_btn = (LoginButton) findViewById(R.id.login_button);
        fb_btn.setReadPermissions("email");
        fb_btn.setReadPermissions("","");


        if(AccessToken.getCurrentAccessToken()!=null){
            final Intent intent = new Intent(MainActivity.this, MessagesActivity.class);
            startActivity(intent);
            finish();
        }
      //  lb.logInWithReadPermissions(this, Arrays.asList("public_profile"));

        fb_btn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("xyi", loginResult.toString());
                final Intent intent = new Intent(MainActivity.this, MessagesActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
