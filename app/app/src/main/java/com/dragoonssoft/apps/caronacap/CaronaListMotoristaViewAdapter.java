package com.dragoonssoft.apps.caronacap;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class CaronaListMotoristaViewAdapter extends ArrayAdapter<CaronaInfo> {
    private Context mContext;
    int mResource;

    public CaronaListMotoristaViewAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<CaronaInfo> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    private static class ViewHolder{
        TextView txHorario, txPartidaDestino, txNomeMotorista, txCaroneiro1, txCaroneiro2, txCaroneiro3, txCaroneiro4;
        ImageView motoristaImgView, caroneiro1ImgView, caroneiro2ImgView, caroneiro3ImgView, caroneiro4ImgView;
        Button fecharCaroneiro1Btn, fecharCaroneiro2Btn, fecharCaroneiro3Btn, fecharCaroneiro4Btn;
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
        String idCaroneiro1 = getItem(position).getIdcaroneiro1();
        String idCaroneiro2 = getItem(position).getIdcaroneiro2();
        String idCaroneiro3 = getItem(position).getIdcaroneiro3();
        String idCaroneiro4 = getItem(position).getIdcaroneiro4();
        String partida = getItem(position).getPartidaDestino();
        //String destino = getItem(position).getDestino();
        String horario = getItem(position).getHorario();
        //String vagas = getItem(position).getVagas();
        String imgUrl = getItem(position).getImg();
        String imgUrlCaroneiro1 = getItem(position).getImgcaroneiro1();
        String imgUrlCaroneiro2 = getItem(position).getImgcaroneiro2();
        String imgUrlCaroneiro3 = getItem(position).getImgcaroneiro3();
        String imgUrlCaroneiro4 = getItem(position).getImgcaroneiro4();
        //FirebaseAuth mAuth = ListaCaronasFragmento.mAuth;
        DatabaseReference caronasRef = FirebaseDatabase.getInstance().getReference().child("caronas").child(ListaCaronasFragmento.idParaVerCarona);
        DatabaseReference usuariosRef = FirebaseDatabase.getInstance().getReference().child("usuarios");
        int numVagas = 0;

        //CaronaInfo carona = new CaronaInfo(nome, horario, vagas, partida,imgUrl);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        ViewHolder holder = new ViewHolder();
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
        holder.txPartidaDestino = (TextView)convertView.findViewById(R.id.partidaDestinoLabel);
        holder.fecharCaroneiro1Btn = (Button)convertView.findViewById(R.id.tirarCaroneiro1Btn);
        holder.fecharCaroneiro2Btn = (Button)convertView.findViewById(R.id.tirarCaroneiro2Btn);
        holder.fecharCaroneiro3Btn = (Button)convertView.findViewById(R.id.tirarCaroneiro3Btn);
        holder.fecharCaroneiro4Btn = (Button)convertView.findViewById(R.id.tirarCaroneiro4Btn);

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
        holder.txPartidaDestino.setText(partida);
        holder.txNomeMotorista.setText(nome);
        holder.txCaroneiro1.setText(nomeCaroneiro1);
        holder.txCaroneiro2.setText(nomeCaroneiro2);
        holder.txCaroneiro3.setText(nomeCaroneiro3);
        holder.txCaroneiro4.setText(nomeCaroneiro4);

        if(holder.txCaroneiro1.getText().equals("OCUPADO"))
            holder.fecharCaroneiro1Btn.setText(R.string.liberar);
        else
            holder.fecharCaroneiro1Btn.setText(R.string.reservar);

        if(holder.txCaroneiro2.getText().equals("OCUPADO"))
            holder.fecharCaroneiro2Btn.setText(R.string.liberar);
        else
            holder.fecharCaroneiro2Btn.setText(R.string.reservar);

        if(holder.txCaroneiro3.getText().equals("OCUPADO"))
            holder.fecharCaroneiro3Btn.setText(R.string.liberar);
        else
            holder.fecharCaroneiro3Btn.setText(R.string.reservar);

        if(holder.txCaroneiro4.getText().equals("OCUPADO"))
            holder.fecharCaroneiro4Btn.setText(R.string.liberar);
        else
            holder.fecharCaroneiro4Btn.setText(R.string.reservar);

        if(holder.txCaroneiro1.getText().equals("OCUPADO"))
            holder.txCaroneiro1.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
        else if(holder.txCaroneiro1.getText().equals("ASSENTO VAZIO"))
            holder.txCaroneiro1.setTextColor(mContext.getResources().getColor(R.color.assento_vazio_color));
        else{
            holder.txCaroneiro1.setTextColor(mContext.getResources().getColor(R.color.green_text_caronaCAP));
            numVagas++;
        }

        if(holder.txCaroneiro2.getText().equals("OCUPADO"))
            holder.txCaroneiro2.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
        else if(holder.txCaroneiro2.getText().equals("ASSENTO VAZIO"))
            holder.txCaroneiro2.setTextColor(mContext.getResources().getColor(R.color.assento_vazio_color));
        else
            holder.txCaroneiro2.setTextColor(mContext.getResources().getColor(R.color.green_text_caronaCAP));

        if(holder.txCaroneiro3.getText().equals("OCUPADO"))
            holder.txCaroneiro3.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
        else if(holder.txCaroneiro3.getText().equals("ASSENTO VAZIO"))
            holder.txCaroneiro3.setTextColor(mContext.getResources().getColor(R.color.assento_vazio_color));
        else{
            holder.txCaroneiro3.setTextColor(mContext.getResources().getColor(R.color.green_text_caronaCAP));
            numVagas++;
        }

        if(holder.txCaroneiro4.getText().equals("OCUPADO"))
            holder.txCaroneiro4.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
        else if(holder.txCaroneiro4.getText().equals("ASSENTO VAZIO"))
            holder.txCaroneiro4.setTextColor(mContext.getResources().getColor(R.color.assento_vazio_color));
        else{
            holder.txCaroneiro4.setTextColor(mContext.getResources().getColor(R.color.green_text_caronaCAP));
            numVagas++;
        }


        //RESERVANDO E LIBERANDO VAGAS

        holder.fecharCaroneiro1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.fecharCaroneiro1Btn.getText().equals("RESERVAR")){
                    if(holder.txCaroneiro1.getText().equals("ASSENTO VAZIO")){
                        //caronasRef.child(ListaCaronasFragmento.idParaVerCarona).child("nomecaroneiro1").setValue("OCUPADO");
                    }else{
                        //caronasRef.child("nomecaroneiro1").setValue("OCUPADO");
                        //usuariosRef.child(idCaroneiro1).child("status").setValue("idle");
                    }
                    holder.fecharCaroneiro1Btn.setText("LIBERAR");
                    holder.txCaroneiro1.setText("OCUPADO");
                    holder.txCaroneiro1.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
                    //numVagas=numVagas-1;
                }else{ //"LIBERAR"
                    //caronasRef.child("nomecaroneiro1").setValue("ASSENTO VAZIO");
                    //caronasRef.child("idcaroneiro1").setValue("");
                    //caronasRef.child(ListaCaronasFragmento.idParaVerCarona).child("imgcaroneiro1").setValue("");
                    holder.fecharCaroneiro1Btn.setText("RESERVAR");
                    holder.txCaroneiro1.setText("ASSENTO VAZIO");
                    holder.txCaroneiro1.setTextColor(mContext.getResources().getColor(R.color.assento_vazio_color));
                    //numVagas++;
                }
            }
        });

        holder.fecharCaroneiro2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.fecharCaroneiro2Btn.getText().equals("RESERVAR")){
                    if(holder.txCaroneiro2.getText().equals("ASSENTO VAZIO")){
                       // caronasRef.child("nomecaroneiro2").setValue("OCUPADO");
                    }else{
                       // caronasRef.child("nomecaroneiro2").setValue("OCUPADO");
                       // usuariosRef.child(idCaroneiro2).child("status").setValue("idle");
                    }
                    holder.fecharCaroneiro2Btn.setText("LIBERAR");
                    holder.txCaroneiro2.setText("OCUPADO");
                    holder.txCaroneiro2.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
                    //numVagas--;
                }else{ //"LIBERAR"
                    //caronasRef.child("nomecaroneiro2").setValue("ASSENTO VAZIO");
                    //caronasRef.child("idcaroneiro2").setValue("");
                    //caronasRef.child("imgcaroneiro2").setValue("");
                    holder.fecharCaroneiro2Btn.setText("RESERVAR");
                    holder.txCaroneiro2.setText("ASSENTO VAZIO");
                    holder.txCaroneiro2.setTextColor(mContext.getResources().getColor(R.color.assento_vazio_color));
                    //numVagas++;
                }
            }
        });

        holder.fecharCaroneiro3Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.fecharCaroneiro3Btn.getText().equals("RESERVAR")){
                    if(holder.txCaroneiro3.getText().equals("ASSENTO VAZIO")){
                        //caronasRef.child("nomecaroneiro3").setValue("OCUPADO");
                    }else{
                        //caronasRef.child("nomecaroneiro3").setValue("OCUPADO");
                        //usuariosRef.child(idCaroneiro3).child("status").setValue("idle");
                    }
                    holder.fecharCaroneiro3Btn.setText("LIBERAR");
                    holder.txCaroneiro3.setText("OCUPADO");
                    holder.txCaroneiro3.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
                    //numVagas--;
                }else{ //"LIBERAR"
                    //caronasRef.child("nomecaroneiro3").setValue("ASSENTO VAZIO");
                    //caronasRef.child("idcaroneiro3").setValue("");
                    //caronasRef.child("imgcaroneiro3").setValue("");
                    holder.fecharCaroneiro3Btn.setText("RESERVAR");
                    holder.txCaroneiro3.setText("ASSENTO VAZIO");
                    holder.txCaroneiro3.setTextColor(mContext.getResources().getColor(R.color.assento_vazio_color));
                    //numVagas++;
                }
            }
        });

        holder.fecharCaroneiro4Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.fecharCaroneiro4Btn.getText().equals("RESERVAR")){
                    if(holder.txCaroneiro4.getText().equals("ASSENTO VAZIO")){
                        //caronasRef.child("nomecaroneiro4").setValue("OCUPADO");
                    }else{
                        //caronasRef.child("nomecaroneiro4").setValue("OCUPADO");
                        //usuariosRef.child(idCaroneiro4).child("status").setValue("idle");
                    }
                    holder.fecharCaroneiro4Btn.setText("LIBERAR");
                    holder.txCaroneiro4.setText("OCUPADO");
                    holder.txCaroneiro4.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
                    //numVagas--;
                }else{ //"LIBERAR"
                    //caronasRef.child("nomecaroneiro4").setValue("ASSENTO VAZIO");
                    //caronasRef.child("idcaroneiro4").setValue("");
                    //caronasRef.child("imgcaroneiro4").setValue("");
                    holder.fecharCaroneiro4Btn.setText("RESERVAR");
                    holder.txCaroneiro4.setText("ASSENTO VAZIO");
                    holder.txCaroneiro4.setTextColor(mContext.getResources().getColor(R.color.assento_vazio_color));
                    //numVagas++;
                }
            }
        });

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
