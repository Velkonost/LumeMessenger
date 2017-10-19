package com.velkonost.lume.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.db.DBHelper;
import com.velkonost.lume.vkontakte.structures.MessagesList;

/**
 * Адаптер списка авторизованных пользователей
 */

class FwdMessagesAdapter extends RecyclerView.Adapter<FwdMessagesAdapter.ViewHolder> {

    private Context ctx;
    private MessagesList messagesList;
    private DBHelper dbHelper;

    FwdMessagesAdapter(MessagesList messagesList, Context ctx) {
        this.messagesList = messagesList;
        this.ctx = ctx;
        dbHelper = new DBHelper(ctx);

    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(ctx).inflate(R.layout.item_vkontakte_fwdmessage, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.userName.setText(messagesList.getMessageSendersByPosition(position));
        holder.messageBody.setText(messagesList.getMessageBodyByPosition(position));
        holder.messageDate.setText(messagesList.getMessageDateByPosition(position));

    }



    @Override
    public int getItemCount() {
        return messagesList.getMessagesAmount();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * Полное имя автора сообщения
         */
        TextView userName;

        /**
         * Тело сообщения
         */
        TextView messageBody;

        /**
         * Дата сообщения
         */
        TextView messageDate;

        ViewHolder(final View itemView) {
            super(itemView);

            userName = (TextView) itemView.findViewById(R.id.txt_vp_item_list);
            messageBody = (TextView) itemView.findViewById(R.id.txt_vp_item_list2);
            messageDate = (TextView) itemView.findViewById(R.id.txt_vp_item_list3);
        }
    }
}
