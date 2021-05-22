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

public class CaronaListAdapter extends ArrayAdapter<CaronaInfo>{
    private Context mContext;
    int mResource;

    public CaronaListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<CaronaInfo> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    private static class ViewHolder{
        TextView txNome, txHorario, txVagas, txPartidaDestino;
        ImageView imgView;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        setupImageLoader();
        //String idCarona = getItem(position).getId();
        //String idUsuario = getItem(position).getIdUsuario();
        String nome = getItem(position).getNome();
        String partidaDestino = getItem(position).getPartidaDestino();
        //String destino = getItem(position).getDestino();
        String horario = getItem(position).getHorario();
        String vagas = getItem(position).getVagas();
        String imgUrl = getItem(position).getImg();

        //CaronaInfo carona = new CaronaInfo(nome, horario, vagas, partida,imgUrl);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        ViewHolder holder = new ViewHolder();
        holder.txNome = (TextView)convertView.findViewById(R.id.nomeList);
        holder.txHorario = (TextView)convertView.findViewById(R.id.horarioList);
        holder.txVagas = (TextView)convertView.findViewById(R.id.vagasList);
        holder.txPartidaDestino = (TextView)convertView.findViewById(R.id.partidaDestinoList);
        holder.imgView = (ImageView)convertView.findViewById(R.id.profileImagePerfil);
        convertView.setTag(holder);
        //TextView txImgUrl = (TextView)convertView.findViewById(R.id.profileImage);
        //String sobrenome = getItem(position).getSobrenome();
        
        ImageLoader imageLoader = ImageLoader.getInstance();
        int defaultImage = mContext.getResources().getIdentifier("@drawable/ic_launcher",null,mContext.getPackageName());
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();
        imageLoader.displayImage(imgUrl, holder.imgView, options);
        holder.txNome.setText(nome);
        holder.txHorario.setText(horario);
        holder.txVagas.setText(vagas);
        holder.txPartidaDestino.setText(partidaDestino);

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
