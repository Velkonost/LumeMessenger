package com.velkonost.lume.vkontakte.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.adapters.CustomAdapter;
import com.velkonost.lume.vkontakte.adapters.DialogsAdapter;
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
import java.util.Random;

import devlight.io.library.ntb.NavigationTabBar;

/**
 * @author Velkonost
 */

public class MainActivity extends AppCompatActivity {

    private String[] scope = new String[]{
            VKScope.MESSAGES, VKScope.FRIENDS
    };

    private ViewPager viewPager;
    private NavigationTabBar navigationTabBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vkontakte_main);

        viewPager = (ViewPager) findViewById(R.id.vp_horizontal_ntb);

        initUI();
    }

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

    private DialogsAdapter getDialogsAdapter(VKResponse response) {


        ArrayList<String> users = new ArrayList<String>();
        ArrayList<String> messages = new ArrayList<String>();

        VKApiGetDialogResponse getMessagesResponse = (VKApiGetDialogResponse) response.parsedModel;
        VKList<VKApiDialog> list = getMessagesResponse.items;

        completeRequestMessages(list, users, messages);

        return new DialogsAdapter(users, messages, MainActivity.this, list);
    }

    private void setRecyclerViewConfiguration(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        getBaseContext(), LinearLayoutManager.VERTICAL, false
                )
        );
    }

    private ArrayList<NavigationTabBar.Model> initializeModels() {
        ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        String[] colors = getResources().getStringArray(R.array.default_preview);

        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_first),
                        Color.parseColor(colors[0]))
                        .title("Messages")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_second),
                        Color.parseColor(colors[1]))
                        .title("Friends")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_third),
                        Color.parseColor(colors[2]))
                        .title("Profile")
                        .build()
        );

        return models;
    }


    private void setNavigationTabBarConfiguration(NavigationTabBar navigationTabBar) {
        navigationTabBar.setViewPager(viewPager, 2);
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


    private void initUI() {

        viewPager.setAdapter(new PagerAdapter() {
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
                    VKSdk.login(MainActivity.this, scope);
                }

                if (position == 0) {
                    VKRequest requestMessages = VKApi.messages().getDialogs(VKParameters.from("count", 10));
                    requestMessages.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            recyclerView.setAdapter(getDialogsAdapter(response));
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

                } else {
                    recyclerView.setAdapter(new CustomAdapter(MainActivity.this));
                }

                container.addView(view);
                return view;
            }
        });

        initializeNavigationTabBar();

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                for (int i = 0; i < navigationTabBar.getModels().size(); i++) {
                    final NavigationTabBar.Model model = navigationTabBar.getModels().get(i);
                    navigationTabBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final String title = String.valueOf(new Random().nextInt(15));
                            if (!model.isBadgeShowed()) {
                                model.setBadgeTitle(title);
                                model.showBadge();
                            } else model.updateBadgeTitle(title);
                        }
                    }, i * 100);
                }

            }
        });
    }
}
