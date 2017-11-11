package com.velkonost.lume.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.velkonost.lume.R;
import com.velkonost.lume.vkontakte.models.MessagesList;

/**
 * Адаптер списка авторизованных пользователей
 */

class FwdMessagesAdapter extends RecyclerView.Adapter<FwdMessagesAdapter.ViewHolder> {

    private Context ctx;
    private MessagesList messagesList;

    FwdMessagesAdapter(MessagesList messagesList, Context ctx) {
        this.messagesList = messagesList;
        this.ctx = ctx;
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
        holder.messageDate.setText(formatDate(messagesList.getMessageDateByPosition(position)));
    }

    private String formatDate(String date) {
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);

        String hour = date.substring(11, 13);
        String minute = date.substring(14, 16);
        return hour + ":" + minute;
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

            userName = (TextView) itemView.findViewById(R.id.user_name);
            messageBody = (TextView) itemView.findViewById(R.id.message_body);
            messageDate = (TextView) itemView.findViewById(R.id.message_date);
        }
    }
}
