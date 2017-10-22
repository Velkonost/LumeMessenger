package com.velkonost.lume.instagram.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.instagram.instagramapi.engine.InstagramEngine;
import com.instagram.instagramapi.exceptions.InstagramException;
import com.instagram.instagramapi.interfaces.InstagramAPIResponseCallback;
import com.instagram.instagramapi.objects.IGMedia;
import com.instagram.instagramapi.objects.IGPagInfo;
import com.instagram.instagramapi.objects.IGUser;
import com.squareup.picasso.Picasso;
import com.velkonost.lume.R;
import com.velkonost.lume.instagram.activities.InstagramActivity;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by admin on 22.10.2017.
 */

public class ProfileFragment extends Fragment {
    private TextView posts_view, follows_view, following_view;
    private String username_txt = "", posts_txt = "", follows_txt = "", following_txt = "", profileImg_url = "";
    private CircleImageView profileImg;

    public static ProfileFragment newInstance(int page) {
        ProfileFragment pageFragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.item_instagram_profile, null);

        posts_view = (TextView) view.findViewById(R.id.posts_num);
        follows_view = (TextView) view.findViewById(R.id.follows_num);
        following_view = (TextView) view.findViewById(R.id.following_num);
        profileImg = (CircleImageView) view.findViewById(R.id.profileImg);

        InstagramEngine.getInstance(view.getContext()).getUserDetails(instagramUserResponseCallback);
        InstagramEngine.getInstance(view.getContext()).getMediaForUser(instagramMediaResponseCallback);
        return view;
    }

    InstagramAPIResponseCallback<ArrayList<IGMedia>> instagramMediaResponseCallback = new InstagramAPIResponseCallback<ArrayList<IGMedia>>() {
        @Override
        public void onResponse(ArrayList<IGMedia> responseObject, IGPagInfo pageInfo) {
            responseObject.get(0).getImages();
        }

        @Override
        public void onFailure(InstagramException exception) {

        }
    };

    InstagramAPIResponseCallback<IGUser> instagramUserResponseCallback = new InstagramAPIResponseCallback<IGUser>() {
        @Override
        public void onResponse(IGUser responseObject, IGPagInfo pageInfo) {

//            Toast.makeText(InstagramActivity.this, "Username: " + responseObject.getUsername(),
//                    Toast.LENGTH_LONG).show();
            username_txt = responseObject.getUsername();
            posts_txt = String.valueOf(responseObject.getMediaCount());
            follows_txt = String.valueOf(responseObject.getFollowedByCount());
            following_txt = String.valueOf(responseObject.getFollowsCount());
            profileImg_url = responseObject.getProfilePictureURL();

            posts_view.setText(posts_txt);
            follows_view.setText(follows_txt);
            following_view.setText(following_txt);

            Picasso
                    .with(profileImg.getContext())
                    .load(profileImg_url)
                    .into(profileImg);
        }

        @Override
        public void onFailure(InstagramException exception) {
            Log.v("SampleActivity", "Exception:" + exception.getMessage());
        }
    };
}
