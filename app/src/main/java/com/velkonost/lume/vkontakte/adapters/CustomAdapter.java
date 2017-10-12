package com.velkonost.lume.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.velkonost.lume.R;
import com.vk.sdk.api.model.VKApiDialog;
import com.vk.sdk.api.model.VKList;

import java.util.ArrayList;

/**
 * @author Velkonost
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private ArrayList<String> users, messages;
    private Context ctx;
    private VKList<VKApiDialog> list;



    public CustomAdapter(Context ctx) {
        this.ctx = ctx;
    }

    ///

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(ctx).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.txt.setText("0");
        holder.txt2.setText("1");


    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txt;
        public TextView txt2;

        public ViewHolder(final View itemView) {
            super(itemView);
            txt = (TextView) itemView.findViewById(R.id.txt_vp_item_list);
            txt2 = (TextView) itemView.findViewById(R.id.txt_vp_item_list2);
        }
    }
    ///

}
