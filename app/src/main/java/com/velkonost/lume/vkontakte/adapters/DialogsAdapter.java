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

public class DialogsAdapter extends RecyclerView.Adapter<DialogsAdapter.ViewHolder> {

    private ArrayList<String> users, messages;
    private Context ctx;
    private VKList<VKApiDialog> list;

    public DialogsAdapter(ArrayList<String> users, ArrayList<String> messages, Context ctx, VKList<VKApiDialog> list) {
        this.users = users;
        this.messages = messages;
        this.ctx = ctx;
        this.list = list;
    }

//    public DialogsAdapter(ArrayList<String> users, ArrayList<String> messages, Context ctx) {
//        this.users = users;
//        this.messages = messages;
//        this.ctx = ctx;
//    }


    ///

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(ctx).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
//        holder.txt.setText(String.format("Navigation Item #%d", position));


        holder.txt.setText(users.get(position));
        holder.txt2.setText(messages.get(position));

//        if (list != null)
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    final ArrayList<String> inList = new ArrayList<>();
//                    final ArrayList<String> outList = new ArrayList<>();
//                    final int id = list.get(position).message.user_id;
//
//                    VKRequest request = new VKRequest("messages.getHistory", VKParameters.from(VKApiConst.USER_ID, id));
//                    request.executeWithListener(new VKRequest.VKRequestListener() {
//                        @Override
//                        public void onComplete(VKResponse response) {
//                            super.onComplete(response);
//
//                            try {
//                                JSONArray array = response.json.getJSONObject("response").getJSONArray("items");
//                                VKApiMessage[] msg = new VKApiMessage[array.length()];
//
//                                for (int i = 0; i < array.length(); i++) {
//                                    VKApiMessage mes = new VKApiMessage(array.getJSONObject(i));
//                                    msg[i] = mes;
//                                }
//
//                                for (VKApiMessage mess : msg) {
//                                    if (mess.out) {
//                                        outList.add(mess.body);
//                                    } else {
//                                        inList.add(mess.body);
//                                    }
//                                }
//
//                                ctx.startActivity(new Intent(ctx, SendMessage.class).putExtra("id", id).putExtra("in", inList).putExtra("out", outList));
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                }
//            });

    }

    @Override
    public int getItemCount() {
        return users.size();
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
}
