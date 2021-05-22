package com.dragoonssoft.apps.caronacap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wiler on 01/06/2017.
 */

public class ConversaListAdapter extends ArrayAdapter<ChatUsuarios>{
    private Context mContext;
    int mResource, numVagas;
    DatabaseReference notificationsOffRef;
    AlertDialog.Builder alert_builder;
    AlertDialog alert;
    String numVagasTxt;
    ProgressDialog mCheckingAuthProgress;
    DatabaseReference caronaRef, usuariosRef;
    FirebaseAuth mAuth;

    public ConversaListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ChatUsuarios> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    private static class ViewHolder{
        TextView nomeImageChatItemList;
        ImageView profileImageChatItemList;
        //ImageView onlineSignChatItemList = (ImageView) view.findViewById(R.id.onlineSignChatItemList);
        ImageView vistoSignChatItemList;
        TextView horarioChatItemList;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        setupImageLoader();

        //String idDoOutroUsuario = getItem(position).getIdOutroUsuario();
        String horario = getItem(position).getHorario();
        String nome = getItem(position).getNome();
        String img = getItem(position).getImg();
        Boolean visto = getItem(position).getVisto();
        //String telefone = getItem(position).getTelefone();


        //CaronaInfo carona = new CaronaInfo(nome, horario, vagas, partida,imgUrl);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        ViewHolder holder = new ViewHolder();
        holder.profileImageChatItemList = (ImageView)convertView.findViewById(R.id.profileImageChatItemList);
        holder.nomeImageChatItemList = (TextView)convertView.findViewById(R.id.nomeChatItemList);
        holder.vistoSignChatItemList = (ImageView) convertView.findViewById(R.id.msgNaoVistaChatItemList);
        holder.horarioChatItemList = (TextView) convertView.findViewById(R.id.horarioChatItemLis);
        convertView.setTag(holder);

        ImageLoader imageLoader = ImageLoader.getInstance();
        int defaultImage = mContext.getResources().getIdentifier("@drawable/ic_launcher",null,mContext.getPackageName());
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();
        holder.nomeImageChatItemList.setText(nome);
        holder.horarioChatItemList.setText(horario);
        imageLoader.displayImage(img, holder.profileImageChatItemList, options);
        holder.profileImageChatItemList.setVisibility(View.VISIBLE);
        holder.horarioChatItemList.setVisibility(View.VISIBLE);
        if(visto)
            holder.vistoSignChatItemList.setVisibility(View.INVISIBLE);
        else
            holder.vistoSignChatItemList.setVisibility(View.VISIBLE);


        return convertView;
    }

    private void setupImageLoader(){
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100*1024*1024).build();

        ImageLoader.getInstance().init(config);
    }
}
