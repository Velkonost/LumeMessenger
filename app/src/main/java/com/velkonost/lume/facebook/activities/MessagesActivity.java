package com.velkonost.lume.facebook.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.velkonost.lume.R;
import com.velkonost.lume.facebook.activities.fragments.MessagesFragment;

import java.util.ArrayList;

import devlight.io.library.ntb.NavigationTabBar;

import static com.velkonost.lume.facebook.activities.Constants.VIEW_PAGER_PAGES.DIALOGS_PAGE;
import static com.velkonost.lume.facebook.activities.Constants.VIEW_PAGER_PAGES.FRIENDS_PAGE;
import static com.velkonost.lume.facebook.activities.Constants.VIEW_PAGER_PAGES.SETTINGS_PAGE;

/**
 * Created by admin on 09.11.2017.
 */

public class MessagesActivity extends AppCompatActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fb_main);

        viewPager = (ViewPager) findViewById(R.id.fb_viewPager);

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(new MessagesActivity.PageAdapter(getSupportFragmentManager()));
        initializeNavigationTabBar();
    }

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
        NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_fb);
        navigationTabBar.setModels(initializeModels());
        setNavigationTabBarConfiguration(navigationTabBar);
        setNavigationTabBarListeners(navigationTabBar);
    }

    private class PageAdapter extends FragmentPagerAdapter {

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {
                case 0: return MessagesFragment.newInstance(1);
                case 1: return MessagesFragment.newInstance(2);
                case 2: return MessagesFragment.newInstance(3);
                default: return MessagesFragment.newInstance(1);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
