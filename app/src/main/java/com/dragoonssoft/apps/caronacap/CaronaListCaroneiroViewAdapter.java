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
import android.widget.Spinner;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.List;

/**
 * Created by wiler on 14/06/2017.
 */

public class CaronaListCaroneiroViewAdapter extends ArrayAdapter<CaronaInfo> {
    private Context mContext;
    int mResource;

    public CaronaListCaroneiroViewAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<CaronaInfo> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    private static class ViewHolder{
        TextView txHorario, txPartida, txNomeMotorista, txCaroneiro1, txCaroneiro2, txCaroneiro3, txCaroneiro4;
        Spinner spinPartida, spinDestino;
        ImageView motoristaImgView, caroneiro1ImgView, caroneiro2ImgView, caroneiro3ImgView, caroneiro4ImgView;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        setupImageLoader();
        //String idCarona = getItem(position).getId();
        //String idUsuario = getItem(position).getIdUsuario();
        String nome = getItem(position).getNome();
        String nomeCaroneiro1 = getItem(position).getNomecaroneiro1();
        String nomeCaroneiro2 = getItem(position).getNomecaroneiro2();
        String nomeCaroneiro3 = getItem(position).getNomecaroneiro3();
        String nomeCaroneiro4 = getItem(position).getNomecaroneiro4();
        String partida = getItem(position).getPartida();
        //String destino = getItem(position).getDestino();
        String horario = getItem(position).getHorario();
        //String vagas = getItem(position).getVagas();
        String imgUrl = getItem(position).getImg();
        String imgUrlCaroneiro1 = getItem(position).getImgcaroneiro1();
        String imgUrlCaroneiro2 = getItem(position).getImgcaroneiro2();
        String imgUrlCaroneiro3 = getItem(position).getImgcaroneiro3();
        String imgUrlCaroneiro4 = getItem(position).getImgcaroneiro4();

        //CaronaInfo carona = new CaronaInfo(nome, horario, vagas, partida,imgUrl);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        CaronaListCaroneiroViewAdapter.ViewHolder holder = new CaronaListCaroneiroViewAdapter.ViewHolder();
        holder.txHorario = (TextView)convertView.findViewById(R.id.horario);
        holder.motoristaImgView = (ImageView)convertView.findViewById(R.id.profileImagePerfil);
        holder.txNomeMotorista = (TextView)convertView.findViewById(R.id.motoristaNome);
        holder.caroneiro1ImgView = (ImageView)convertView.findViewById(R.id.caroneiro1Foto);
        holder.txCaroneiro1 = (TextView)convertView.findViewById(R.id.caroneiro1Nome);
        holder.caroneiro2ImgView = (ImageView)convertView.findViewById(R.id.caroneiro2Foto);
        holder.txCaroneiro2 = (TextView)convertView.findViewById(R.id.caroneiro2Nome);
        holder.caroneiro3ImgView = (ImageView)convertView.findViewById(R.id.caroneiro3Foto);
        holder.txCaroneiro3 = (TextView)convertView.findViewById(R.id.caroneiro3Nome);
        holder.caroneiro4ImgView = (ImageView)convertView.findViewById(R.id.caroneiro4Foto);
        holder.txCaroneiro4 = (TextView)convertView.findViewById(R.id.caroneiro4Nome);
        holder.txPartida = (TextView)convertView.findViewById(R.id.partidaDestinoLabel);
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
        imageLoader.displayImage(imgUrl, holder.motoristaImgView, options);
        imageLoader.displayImage(imgUrlCaroneiro1, holder.caroneiro1ImgView, options);
        imageLoader.displayImage(imgUrlCaroneiro2, holder.caroneiro2ImgView, options);
        imageLoader.displayImage(imgUrlCaroneiro3, holder.caroneiro3ImgView, options);
        imageLoader.displayImage(imgUrlCaroneiro4, holder.caroneiro4ImgView, options);
        holder.txHorario.setText(horario);
        holder.txPartida.setText(partida);
        holder.txNomeMotorista.setText(nome);
        holder.txCaroneiro1.setText(nomeCaroneiro1);
        holder.txCaroneiro2.setText(nomeCaroneiro2);
        holder.txCaroneiro3.setText(nomeCaroneiro3);
        holder.txCaroneiro4.setText(nomeCaroneiro4);

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
