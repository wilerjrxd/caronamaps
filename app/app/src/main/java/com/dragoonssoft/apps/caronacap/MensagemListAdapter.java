package com.dragoonssoft.apps.caronacap;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.List;

/**
 * Created by wiler on 01/06/2017.
 */

public class MensagemListAdapter extends ArrayAdapter<Mensagens>{
    private Context mContext;
    int mResource;

    public MensagemListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Mensagens> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    private static class ViewHolder{
        TextView txMsg, txHorario;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String msg = getItem(position).getMsg();
        String horario = String.valueOf(getItem(position).getHorario());

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        ViewHolder holder = new ViewHolder();
        holder.txMsg = (TextView)convertView.findViewById(R.id.message_item_text);
        holder.txHorario = (TextView)convertView.findViewById(R.id.message_item_horario);
        convertView.setTag(holder);

        holder.txMsg.setText(msg);
        holder.txHorario.setText(horario);

        return convertView;
    }

}