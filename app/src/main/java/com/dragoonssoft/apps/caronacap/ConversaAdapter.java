package com.dragoonssoft.apps.caronacap;

import android.content.res.Resources;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Created by wiler on 08/08/2017.
 */

public class ConversaAdapter extends RecyclerView.Adapter<MensagemAdapter.MensagemViewHolder>{
    @Override
    public MensagemAdapter.MensagemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(MensagemAdapter.MensagemViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    /*private List<ChatUsuarios> mConversaList;

    public ConversaAdapter(List<ChatUsuarios> mConversaList){
        this.mConversaList  = mConversaList;
    }

    @Override
    public ConversaViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_message_item, parent, false);

        return new ConversasViewHolder(v);
    }

    public class ConversaViewHolder extends RecyclerView.ViewHolder{

        public TextView msgText;
        public TextView horarioText;


        public ConversaViewHolder(View itemView) {
            super(itemView);

            msgText = (TextView) itemView.findViewById(R.id.message_item_text);
            horarioText = (TextView) itemView.findViewById(R.id.message_item_horario);

        }

    }

    @Override
    public void onBindViewHolder(MensagemAdapter.MensagemViewHolder holder, int position) {

        Resources res = holder.itemView.getContext().getResources();
        ChatUsuarios chat = mConversaList.get(position);
        holder.msgText.setText(msg.getMsg());
        holder.horarioText.setText(String.valueOf(msg.getHorario()));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        final boolean hasSdk17 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

        if(msg.getFrom().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

            holder.msgText.setBackground(res.getDrawable(R.drawable.message_text_background));
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.msgText.setLayoutParams(params);

            params2.addRule(RelativeLayout.ALIGN_RIGHT, R.id.message_item_text);
            //params2.addRule(RelativeLayout.ALIGN_END, R.id.message_item_text);
            params2.addRule(RelativeLayout.BELOW, R.id.message_item_text);
            params2.setMargins(0,0,4, 0);
            holder.horarioText.setLayoutParams(params2);

            //holder.msgText.setGravity(Gravity.RIGHT | Gravity.END);
        }else{
            holder.msgText.setBackground(res.getDrawable(R.drawable.message_text_background_other));
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.msgText.setLayoutParams(params);

            params2.addRule(RelativeLayout.ALIGN_LEFT, R.id.message_item_text);
            //params2.addRule(RelativeLayout.ALIGN_START, R.id.message_item_text);
            params2.addRule(RelativeLayout.BELOW, R.id.message_item_text);
            params2.setMargins(4,0,0, 0);
            holder.horarioText.setLayoutParams(params2);
        }

    }

    @Override
    public int getItemCount() {
        return mConversaList.size();
    }

*/
}
