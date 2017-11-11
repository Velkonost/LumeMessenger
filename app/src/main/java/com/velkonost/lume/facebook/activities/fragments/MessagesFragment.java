package com.velkonost.lume.facebook.activities.fragments;

import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.instagram.instagramapi.engine.InstagramEngine;
import com.instagram.instagramapi.exceptions.InstagramException;
import com.instagram.instagramapi.interfaces.InstagramAPIResponseCallback;
import com.instagram.instagramapi.objects.IGMedia;
import com.instagram.instagramapi.objects.IGPagInfo;
import com.instagram.instagramapi.objects.IGUser;
import com.squareup.picasso.Picasso;
import com.velkonost.lume.R;
import com.velkonost.lume.instagram.fragments.ProfileFragment;
import com.velkonost.lume.instagram.models.InfoPhoto;
import com.velkonost.lume.instagram.models.RVAdapter;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by admin on 09.11.2017.
 */

public class MessagesFragment extends Fragment {


    public static MessagesFragment newInstance(int page) {
        MessagesFragment pageFragment = new MessagesFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("page", page);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_instagram_main2);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_fb_main, null);
        Log.d("xyi", FacebookSdk.getApplicationSignature(getContext()));
        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me?fields=messages{message}",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback(){
                    public void onCompleted(GraphResponse response){
                        Log.d("xyi", response.toString());
                        Log.d("xyi", AccessToken.getCurrentAccessToken().toString());
                    }
                }).executeAsync();
        return view;
    }

}
