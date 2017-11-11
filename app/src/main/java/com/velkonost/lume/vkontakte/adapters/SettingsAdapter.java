package com.velkonost.lume.vkontakte.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.velkonost.lume.MainActivity;
import com.velkonost.lume.R;
import com.vk.sdk.VKSdk;

import static com.velkonost.lume.vkontakte.db.Constants.DB_NAME;


/**
 * @author Velkonost
 */

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private Context ctx;

    public SettingsAdapter(Context ctx) {
        this.ctx = ctx;
    }


    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(ctx).inflate(R.layout.item_settings, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        switch (position) {
            case 0:
                holder.settingTitle.setText("Выйти");
                holder.settingTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ctx.deleteDatabase(DB_NAME);

                        VKSdk.logout();
                        ctx.startActivity(new Intent(ctx, MainActivity.class));
                        ((com.velkonost.lume.vkontakte.activities.MainActivity) ctx).finish();
                    }
                });
                break;
            case 1:
                holder.settingTitle.setText("Тема оформления");
                break;
            case 2:
                holder.settingTitle.setText("Установить пароль");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout settingWrap;
        TextView settingTitle;

        ViewHolder(final View itemView) {
            super(itemView);
            settingTitle = (TextView) itemView.findViewById(R.id.setting_name);
            settingWrap = (RelativeLayout) itemView.findViewById(R.id.setting_block);
        }
    }
}