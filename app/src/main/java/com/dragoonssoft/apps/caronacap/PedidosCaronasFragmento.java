package com.dragoonssoft.apps.caronacap;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import androidx.core.app.Fragment;
import androidx.core.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class PedidosCaronasFragmento extends Fragment {


    private FirebaseAuth mAuth;
    private DatabaseReference usuariosRef;
    private FirebaseListAdapter<Notification> firebasePedidosListAdapter;
    private CaronaListAdapter caronaAdapter;
    private ArrayList<CaronaInfo> mCaronaArrayList;
   // private String usuarioImgUrl, usuarioId, usuarioNome;
    private ListView pedidosList;
    private String notificationType;
    private String notificationId;
    private int numVagas;
    private String numVagasTxt;
    private long horario;
    private DatabaseReference conversasRef;
    private ProgressDialog mCheckingAuthProgress;
    private int contador = 0;
    private TextView nenhumPedidoTxt;
    private TextView nenhumResultadoEncontrado;
    private int numPedidos = 0;
    private GetTimeAgo getTimeAgo = new GetTimeAgo();
    private DatabaseReference notificationsOffRef;
    private AlertDialog.Builder alert_builder, alert_builder2;
    private AlertDialog alert, alert2;
    private DatabaseReference caronaRef;
    private String vagasDisponiveis;
    final private static long INTERVAL=1000;
    final private static long TIMEOUT=10000;
    private CountDownTimer abrirConversaTimer;
    private String nomecaroneiro1, nomecaroneiro2, nomecaroneiro3, nomecaroneiro4;
    private RelativeLayout preferenciasLayout;
    static TextView pref1Text, pref2Text, pref3Text, pref4Text;
    private String preferenciasFromServer;
    private String pref1, pref2, pref3, pref4, rest1, rest2;
    private String auxStringPartida, auxStringDestino, auxStringParada1, auxStringParada2, auxStringPreferencias;
    private FirebaseListAdapter<CaronaInfo> firebaseListAdapterSearch;
    private TextView nenhumaCaronaPreferida;
    private View separador;
    private boolean controlarNenhumPedidoText = false;
    private int elapsed = 0;
    private ValueEventListener caronaValueListener;
    //private FirebaseRecyclerAdapter<CaronaInfo,CaronaViewHolder> caronaAdapter;
    private String statusField = null;
    private RecyclerView caronaList;
    private FirebaseListAdapter<CaronaInfo> firebaseCaronasListAdapter;

    public PedidosCaronasFragmento() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pedidos_caronas_fragmento, container, false);

        setHasOptionsMenu(true);

        Activity activity = (Activity)view.getContext();

        pedidosList = view.findViewById(R.id.pedidosList);
        caronaList = view.findViewById(R.id.caronasList);
        nenhumResultadoEncontrado = view.findViewById(R.id.nenhum_resultado);
        nenhumaCaronaPreferida = view.findViewById(R.id.nenhuma_carona_preferida);
        separador = view.findViewById(R.id.view);

        caronaList.setHasFixedSize(true);
        caronaList.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.recycler_divider));
        caronaList.addItemDecoration(itemDecorator);

        mCaronaArrayList = new ArrayList<CaronaInfo>();

        mAuth = FirebaseAuth.getInstance();
        notificationsOffRef = FirebaseDatabase.getInstance().getReference().child("notifications_off");
        usuariosRef = FirebaseDatabase.getInstance().getReference().child("usuarios");
        caronaRef = FirebaseDatabase.getInstance().getReference().child("caronas");

        mCheckingAuthProgress = new ProgressDialog(getActivity());
        mCheckingAuthProgress.setMessage("Aceitando o pedido de carona...");
        mCheckingAuthProgress.setCanceledOnTouchOutside(false);
        mCheckingAuthProgress.setCancelable(false);

        preferenciasLayout = view.findViewById(R.id.preferencias_layout);
        pref1Text = view.findViewById(R.id.pref_1);
        pref2Text = view.findViewById(R.id.pref_2);
        pref3Text = view.findViewById(R.id.pref_3);
        pref4Text = view.findViewById(R.id.pref_4);

        setupImageLoader();
        ImageLoader imageLoader = ImageLoader.getInstance();
        int defaultImage = getActivity().getResources().getIdentifier("@drawable/ic_launcher",null,getActivity().getApplicationContext().getPackageName());
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        firebasePedidosListAdapter = new FirebaseListAdapter<Notification>(getActivity(), Notification.class, R.layout.pedido_item_view, notificationsOffRef.child(mAuth.getCurrentUser().getUid())) {
            @Override
            protected void populateView(View view, Notification notificationInfo, int position) {
                if(notificationInfo.getType().equals("pedido")) {
                    TextView textView = view.findViewById(R.id.textPedidoItemList);
                    TextView horarioView = view.findViewById(R.id.horarioPedidoItemList);
                    ImageButton aceitarPedidoView = view.findViewById(R.id.aceitarPedidoItemList);
                    ImageView imgView = view.findViewById(R.id.profileImagePedidoItemList);

                    final String usuarioId = notificationInfo.getFrom();//notificationId = getRef(position).getKey();
                    final String usuarioNome = notificationInfo.getNome();
                    final String usuarioImg = notificationInfo.getImg();
                    final String usuarioTelefone = notificationInfo.getTelefone();

                    if(usuarioId != null)
                        nenhumResultadoEncontrado.setVisibility(View.GONE);

                    if(HomeTabbed.tabLayout.getTabAt(2).getText().toString().equals(getActivity().getResources().getString(R.string.preferencias_1)) || HomeTabbed.tabLayout.getTabAt(2).getText().toString().equals(getActivity().getResources().getString(R.string.preferencias)))
                        HomeTabbed.tabLayout.getTabAt(2).setText(getActivity().getResources().getString(R.string.pedidos_1));

                    textView.setText(notificationInfo.getNome() + " pediu carona para você.");
                    horario = notificationInfo.getHorario();
                    horarioView.setText(getTimeAgo.getTimeAgo(horario, getActivity().getApplicationContext()));
                    imageLoader.displayImage(notificationInfo.getImg(), imgView, options);
                    aceitarPedidoView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                            if (networkInfo != null && networkInfo.isConnected()) {
                                aceitarPedidoCarona(usuarioId, usuarioNome , usuarioImg, usuarioTelefone, getRef(position).getKey());
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "Você precisa estar conectado à Internet para fazer isso.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    imgView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent abrirConversa = new Intent(getActivity().getApplicationContext(), Conversa.class);
                            abrirConversa.putExtra("idusuario_chat", usuarioId);
                            abrirConversa.putExtra("nomeusuario_chat", usuarioNome);
                            abrirConversa.putExtra("imgusuario_chat", usuarioImg);
                            abrirConversa.putExtra("telefoneusuario_chat", usuarioTelefone);
                            startActivity(abrirConversa);
                        }
                    });
                }
            }
                        // }

        };

        FirebaseRecyclerAdapter<CaronaInfo, CaronaViewHolder> caronaAdapter = new FirebaseRecyclerAdapter<CaronaInfo, CaronaViewHolder>(
                CaronaInfo.class,
                R.layout.carona_item,
                CaronaViewHolder.class,
                caronaRef.orderByChild("horario")
        ) {
            @Override
            protected void populateViewHolder(CaronaViewHolder viewHolder, CaronaInfo model, int position) {
                //viewHolder.setPreferencias(model.getPreferencias());
                viewHolder.caronaPrefs = model.getPreferencias();
                if(model.getData().equals(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()))) {
                    viewHolder.nomeView.setText(model.getNome());
                    viewHolder.horarioView.setText(model.getHorario());
                    viewHolder.vagasView.setText(model.getVagas());
                    /*if(numVagasTxt.equals("0 vaga")){
                        vagasView.setTextColor(Color.RED);
                    }else{
                        vagasView.setTextColor(Color.BLACK);
                    }*/
                    if(model.getPartida() != null) {
                        if ("CAP".equals(model.getPartida())) {
                            String partidaDestinoTxt = null;
                            if(model.getParada1() != null && model.getParada2() != null){
                                if(!model.getParada1().equals("nopref") && !model.getParada2().equals("nopref"))
                                    partidaDestinoTxt = getActivity().getResources().getString(R.string.cap_volta) + " > " + model.getParada1() + " > " + model.getParada2() + " > " +model.getDestino();
                                else if(!model.getParada1().equals("nopref") && model.getParada2().equals("nopref"))
                                    partidaDestinoTxt = getActivity().getResources().getString(R.string.cap_volta) + " > " + model.getParada1() + " > " + model.getDestino();
                                else if(model.getParada1().equals("nopref") && !model.getParada2().equals("nopref"))
                                    partidaDestinoTxt = getActivity().getResources().getString(R.string.cap_volta) + " > " + model.getParada2() + " > " + model.getDestino();
                                else if(model.getParada1().equals("nopref") && model.getParada2().equals("nopref"))
                                    partidaDestinoTxt = getActivity().getResources().getString(R.string.cap_volta) + " > " + model.getDestino();
                            }else{
                                partidaDestinoTxt = getActivity().getResources().getString(R.string.cap_volta) + " > " + model.getDestino();
                            }
                            viewHolder.partidaDestinoView.setText(partidaDestinoTxt);
                            viewHolder.partidaDestinoView.setTextColor(getActivity().getResources().getColor(R.color.darkslateblue));
                        } else if ("CAP".equals(model.getDestino())) {
                            String partidaDestinoTxt = null;
                            if(model.getParada1() != null && model.getParada2() != null){
                                if(!model.getParada1().equals("nopref") && !model.getParada2().equals("nopref"))
                                    partidaDestinoTxt = model.getPartida() + " > " + model.getParada1() + " > " + model.getParada2() + " > " + getActivity().getResources().getString(R.string.cap_ida);
                                else if(!model.getParada1().equals("nopref") && model.getParada2().equals("nopref"))
                                    partidaDestinoTxt = model.getPartida() + " > " + model.getParada1() + " > " + getActivity().getResources().getString(R.string.cap_ida);
                                else if(model.getParada1().equals("nopref") && !model.getParada2().equals("nopref"))
                                    partidaDestinoTxt = model.getPartida() + " > " + model.getParada2() + " > " + getActivity().getResources().getString(R.string.cap_ida);
                                else if(model.getParada1().equals("nopref") && model.getParada2().equals("nopref"))
                                    partidaDestinoTxt = model.getPartida() + " > " + getActivity().getResources().getString(R.string.cap_ida);
                            }else{
                                partidaDestinoTxt = model.getPartida() + " > " + getActivity().getResources().getString(R.string.cap_ida);
                            }
                            viewHolder.partidaDestinoView.setText(partidaDestinoTxt);
                            viewHolder.partidaDestinoView.setTextColor(Color.RED);
                        } else {
                            String partidaDestinoTxt = null;
                            if(model.getParada1() != null && model.getParada2() != null) {
                                if (!model.getParada1().equals("nopref") && !model.getParada2().equals("nopref"))
                                    partidaDestinoTxt = model.getPartida() + " > " + model.getParada1() + " > " + model.getParada2() + " > " + model.getDestino();
                                else if (!model.getParada1().equals("nopref") && model.getParada2().equals("nopref"))
                                    partidaDestinoTxt = model.getPartida() + " > " + model.getParada1() + " > " + model.getDestino();
                                else if (model.getParada1().equals("nopref") && !model.getParada2().equals("nopref"))
                                    partidaDestinoTxt = model.getPartida() + " > " + model.getParada2() + " > " + model.getDestino();
                                else if (model.getParada1().equals("nopref") && model.getParada2().equals("nopref"))
                                    partidaDestinoTxt = model.getPartida() + " > " + model.getDestino();
                            }else{
                                partidaDestinoTxt = model.getPartida() + " > " + model.getDestino();
                            }
                            viewHolder.partidaDestinoView.setText(partidaDestinoTxt);
                        }
                    }else{
                        //viewHolder.setPartidaDestino(model.getPartidaDestino());
                        if(model.getPartidaDestino().contains("> CAP")){
                            viewHolder.partidaDestinoView.setTextColor(Color.RED);
                        }else if(model.getPartidaDestino().contains("CAP >")){
                            viewHolder.partidaDestinoView.setTextColor(getActivity().getResources().getColor(R.color.darkslateblue));
                        }else{
                            viewHolder.partidaDestinoView.setTextColor(Color.BLACK);
                        }
                    }
                    imageLoader.displayImage(model.getImg(), viewHolder.imgView, options);
                    /*if(usuarioStatus != null) {
                        if (!usuarioStatus.equals("motorista")) {
                            if (caronaPrefs != null) {
                                if (!caronaPrefs.contains("Nenhum_Nenhum_Nenhum_Nenhum")) {
                                    if (caronaPrefs.contains(Login.pref1) && caronaPrefs.contains(Login.pref2) && caronaPrefs.contains(Login.pref3) && caronaPrefs.contains("Nenhum")) {
                                        background.setBackgroundColor(getResources().getColor(R.color.green_translucent));
                                    } else if (caronaPrefs.contains(Login.pref1) && caronaPrefs.contains(Login.pref2) && caronaPrefs.contains("Nenhum") && caronaPrefs.contains("Nenhum")) {
                                        background.setBackgroundColor(getResources().getColor(R.color.green_translucent));
                                    } else if (caronaPrefs.contains(Login.pref1) && caronaPrefs.contains("Nenhum") && caronaPrefs.contains("Nenhum") && caronaPrefs.contains("Nenhum")) {
                                        background.setBackgroundColor(getResources().getColor(R.color.green_translucent));
                                    } else if (caronaPrefs.contains(Login.pref1) && caronaPrefs.contains(Login.pref2) && caronaPrefs.contains(Login.pref3) && caronaPrefs.contains(Login.pref4)) {
                                        background.setBackgroundColor(getResources().getColor(R.color.green_translucent));
                                    }
                                }
                            }
                        }
                    }*/
                }

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent;
                        intent = new Intent(getActivity().getApplicationContext(), DealWithCarona.class);
                        intent.putExtra("idcarona", getRef(position).getKey());
                        startActivity(intent);
                    }
                });
                if(!statusField.equals("motorista")) {
                    if(viewHolder.caronaPrefs != null) {
                        if (viewHolder.caronaPrefs.contains(Login.pref1) || viewHolder.caronaPrefs.contains(Login.pref2) || viewHolder.caronaPrefs.contains(Login.pref3) || viewHolder.caronaPrefs.contains(Login.pref4)) {
                            HomeTabbed.tabLayout.getTabAt(2).setText(getActivity().getResources().getString(R.string.preferencias_1));
                            //background.setBackgroundColor(getResources().getColor(R.color.green_translucent));
                            view.setVisibility(View.VISIBLE);
                        } else {
                            viewHolder.hideLayout();
                            if (!HomeTabbed.tabLayout.getTabAt(2).getText().toString().equals(getActivity().getResources().getString(R.string.preferencias_1))) {
                                HomeTabbed.tabLayout.getTabAt(2).setText(getActivity().getResources().getString(R.string.preferencias));
                                nenhumaCaronaPreferida.setVisibility(View.VISIBLE);
                            } else {
                                nenhumaCaronaPreferida.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }
        };
        /*pedidosArrayList = new ArrayList<>();
        adapter = new PedidosListAdapter(getActivity().getApplicationContext(), R.layout.pedido_item_view, pedidosArrayList);
        pedidosList.setAdapter(adapter);*/

        //carregarPedidos();

        //caronaAdapter = new CaronaListAdapter(getActivity().getApplicationContext(), R.layout.carona_item, mCaronaArrayList);

        firebaseCaronasListAdapter = new FirebaseListAdapter<CaronaInfo>(getActivity(), CaronaInfo.class, R.layout.carona_item, caronaRef.orderByChild("horario")) {
            @Override
            protected void populateView(View view, CaronaInfo carona, int position) {

                TextView nomeView = (TextView) view.findViewById(R.id.nomeList);
                TextView horarioView = (TextView) view.findViewById(R.id.horarioList);
                TextView vagasView = (TextView) view.findViewById(R.id.vagasList);
                TextView partidaDestinoView = (TextView) view.findViewById(R.id.partidaDestinoList);
                ImageView imgView = (ImageView) view.findViewById(R.id.profileImagePerfil);
                RelativeLayout background = view.findViewById(R.id.caronas_adapter_layout);
                final RelativeLayout.LayoutParams params;
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                String numVagasTxt = carona.getVagas();

                String caronaPrefs = carona.getPreferencias();

                if(carona.getData().equals(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()))) {
                    nomeView.setText(carona.getNome());
                    horarioView.setText(carona.getHorario());
                    vagasView.setText(numVagasTxt);
                    /*if(numVagasTxt.equals("0 vaga")){
                        vagasView.setTextColor(Color.RED);
                    }else{
                        vagasView.setTextColor(Color.BLACK);
                    }*/
                    if(carona.getPartida() != null) {
                        if ("CAP".equals(carona.getPartida())) {
                            String partidaDestinoTxt = null;
                            if(carona.getParada1() != null && carona.getParada2() != null){
                                if(!carona.getParada1().equals("nopref") && !carona.getParada2().equals("nopref"))
                                    partidaDestinoTxt = getActivity().getResources().getString(R.string.cap_volta) + " > " + carona.getParada1() + " > " + carona.getParada2() + " > " +carona.getDestino();
                                else if(!carona.getParada1().equals("nopref") && carona.getParada2().equals("nopref"))
                                    partidaDestinoTxt = getActivity().getResources().getString(R.string.cap_volta) + " > " + carona.getParada1() + " > " + carona.getDestino();
                                else if(carona.getParada1().equals("nopref") && !carona.getParada2().equals("nopref"))
                                    partidaDestinoTxt = getActivity().getResources().getString(R.string.cap_volta) + " > " + carona.getParada2() + " > " + carona.getDestino();
                                else if(carona.getParada1().equals("nopref") && carona.getParada2().equals("nopref"))
                                    partidaDestinoTxt = getActivity().getResources().getString(R.string.cap_volta) + " > " + carona.getDestino();
                            }else{
                                partidaDestinoTxt = getActivity().getResources().getString(R.string.cap_volta) + " > " + carona.getDestino();
                            }
                            partidaDestinoView.setText(partidaDestinoTxt);
                            partidaDestinoView.setTextColor(getActivity().getResources().getColor(R.color.darkslateblue));
                        } else if ("CAP".equals(carona.getDestino())) {
                            String partidaDestinoTxt = null;
                            if(carona.getParada1() != null && carona.getParada2() != null){
                                if(!carona.getParada1().equals("nopref") && !carona.getParada2().equals("nopref"))
                                    partidaDestinoTxt = carona.getPartida() + " > " + carona.getParada1() + " > " + carona.getParada2() + " > " + getActivity().getResources().getString(R.string.cap_ida);
                                else if(!carona.getParada1().equals("nopref") && carona.getParada2().equals("nopref"))
                                    partidaDestinoTxt = carona.getPartida() + " > " + carona.getParada1() + " > " + getActivity().getResources().getString(R.string.cap_ida);
                                else if(carona.getParada1().equals("nopref") && !carona.getParada2().equals("nopref"))
                                    partidaDestinoTxt = carona.getPartida() + " > " + carona.getParada2() + " > " + getActivity().getResources().getString(R.string.cap_ida);
                                else if(carona.getParada1().equals("nopref") && carona.getParada2().equals("nopref"))
                                    partidaDestinoTxt = carona.getPartida() + " > " + getActivity().getResources().getString(R.string.cap_ida);
                            }else{
                                partidaDestinoTxt = carona.getPartida() + " > " + getActivity().getResources().getString(R.string.cap_ida);
                            }
                            partidaDestinoView.setText(partidaDestinoTxt);
                            partidaDestinoView.setTextColor(Color.RED);
                        } else {
                            String partidaDestinoTxt = null;
                            if(carona.getParada1() != null && carona.getParada2() != null) {
                                if (!carona.getParada1().equals("nopref") && !carona.getParada2().equals("nopref"))
                                    partidaDestinoTxt = carona.getPartida() + " > " + carona.getParada1() + " > " + carona.getParada2() + " > " + carona.getDestino();
                                else if (!carona.getParada1().equals("nopref") && carona.getParada2().equals("nopref"))
                                    partidaDestinoTxt = carona.getPartida() + " > " + carona.getParada1() + " > " + carona.getDestino();
                                else if (carona.getParada1().equals("nopref") && !carona.getParada2().equals("nopref"))
                                    partidaDestinoTxt = carona.getPartida() + " > " + carona.getParada2() + " > " + carona.getDestino();
                                else if (carona.getParada1().equals("nopref") && carona.getParada2().equals("nopref"))
                                    partidaDestinoTxt = carona.getPartida() + " > " + carona.getDestino();
                            }else{
                                partidaDestinoTxt = carona.getPartida() + " > " + carona.getDestino();
                            }
                            partidaDestinoView.setText(partidaDestinoTxt);
                            partidaDestinoView.setTextColor(Color.BLACK);
                        }
                    }else{
                        partidaDestinoView.setText(carona.getPartidaDestino());
                        if(carona.getPartidaDestino().contains("> CAP")){
                            partidaDestinoView.setTextColor(Color.RED);
                        }else if(carona.getPartidaDestino().contains("CAP >")){
                            partidaDestinoView.setTextColor(getActivity().getResources().getColor(R.color.darkslateblue));
                        }else{
                            partidaDestinoView.setTextColor(Color.BLACK);
                        }
                    }
                    imageLoader.displayImage(carona.getImg(), imgView, options);
                }

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent;
                        intent = new Intent(getActivity().getApplicationContext(), DealWithCarona.class);
                        intent.putExtra("idcarona", getRef(position).getKey());
                        startActivity(intent);
                    }
                });
                if(!statusField.equals("motorista")) {
                    if (caronaPrefs.contains(Login.pref1) || caronaPrefs.contains(Login.pref2) || caronaPrefs.contains(Login.pref3) || caronaPrefs.contains(Login.pref4)) {
                        HomeTabbed.tabLayout.getTabAt(2).setText(getActivity().getResources().getString(R.string.preferencias_1));
                        //background.setBackgroundColor(getResources().getColor(R.color.green_translucent));
                        view.setVisibility(View.VISIBLE);
                    } else {
                        view.setVisibility(View.GONE);
                        view.setLayoutParams(new ListView.LayoutParams(0, 50));
                        if (!HomeTabbed.tabLayout.getTabAt(2).getText().toString().equals(getActivity().getResources().getString(R.string.preferencias_1))) {
                            HomeTabbed.tabLayout.getTabAt(2).setText(getActivity().getResources().getString(R.string.preferencias));
                            nenhumaCaronaPreferida.setVisibility(View.VISIBLE);
                        }else{
                            nenhumaCaronaPreferida.setVisibility(View.GONE);
                        }
                    }
                }
            }
        };


        usuariosRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status = dataSnapshot.child("status").getValue().toString();
                statusField = status;
                if(status.contains("idle") || status.contains("aguardando") || status.contains("caroneiro")){
                    //pedidosList.setAdapter(firebaseCaronasListAdapter);
                    if (dataSnapshot.hasChild("preferencias")) {
                        //controlarNenhumPedidoText = false;
                        if(Login.pref1 == null || Login.pref2 == null || Login.pref3 == null || Login.pref4 == null) {
                            preferenciasFromServer = dataSnapshot.child("preferencias").getValue().toString();
                            pref1 = preferenciasFromServer.split("_")[0];
                            rest1 = preferenciasFromServer.split("_")[1];
                            pref2 = rest1.split("-")[0];
                            rest2 = rest1.split("-")[1];
                            pref3 = rest2.split(";")[0];
                            pref4 = rest2.split(";")[1];
                            pref1Text.setText(pref1);
                            pref2Text.setText(pref2);
                            pref3Text.setText(pref3);
                            pref4Text.setText(pref4);
                            Login.pref1 = pref1Text.getText().toString();
                            Login.pref2 = pref2Text.getText().toString();
                            Login.pref3 = pref3Text.getText().toString();
                            Login.pref4 = pref4Text.getText().toString();
                        }else{
                            pref1Text.setText(Login.pref1);
                            pref2Text.setText(Login.pref2);
                            pref3Text.setText(Login.pref3);
                            pref4Text.setText(Login.pref4);
                        }

                        //pref[i] para i=1,2,3,4 representa cada preferência do usuário
                        //view.setVisibility(View.VISIBLE);
                        preferenciasLayout.setVisibility(View.VISIBLE);
                        if(nenhumResultadoEncontrado.getVisibility() == View.VISIBLE){
                            nenhumResultadoEncontrado.setVisibility(View.GONE);
                        }
                        nenhumaCaronaPreferida.setVisibility(View.VISIBLE);
                        pedidosList.setVisibility(View.GONE);
                        caronaList.setVisibility(View.VISIBLE);
                        caronaList.setAdapter(caronaAdapter);
                    }
                }else if(status.contains("motorista")){

                    HomeTabbed.tabLayout.getTabAt(2).setText(getActivity().getResources().getString(R.string.pedidos));
                    //view.setVisibility(View.VISIBLE);

                    preferenciasLayout.setVisibility(View.GONE);
                    nenhumaCaronaPreferida.setVisibility(View.GONE);
                    nenhumResultadoEncontrado.setVisibility(View.VISIBLE);
                    caronaList.setVisibility(View.GONE);
                    pedidosList.setVisibility(View.VISIBLE);
                    pedidosList.setAdapter(firebasePedidosListAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        notificationsOffRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //for(DataSnapshot ds : dataSnapshot.getChildren()) {
                if (dataSnapshot.exists()) {
                    //if (ds.hasChild("type")) {
                    nenhumResultadoEncontrado.setVisibility(View.GONE);
                    //Toast.makeText(getContext(), "nenhum resultado deve estar invisivel", Toast.LENGTH_SHORT).show();
                    //} else {
                    //    nenhumResultadoEncontrado.setVisibility(View.VISIBLE);
                    //}
                } else {
                    //nenhumResultadoEncontrado.setVisibility(View.VISIBLE);
                }
                //}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        preferenciasLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent selecionarPrefIntent = new Intent(getContext(), SelecionarPrefActivity.class);
                selecionarPrefIntent.putExtra("pref1", pref1Text.getText());
                selecionarPrefIntent.putExtra("pref2", pref2Text.getText());
                selecionarPrefIntent.putExtra("pref3", pref3Text.getText());
                selecionarPrefIntent.putExtra("pref4", pref4Text.getText());
                startActivity(selecionarPrefIntent);
            }
        });
        
        return view;
    }


    private void setupImageLoader(){
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getActivity().getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100*1024*1024).build();

        ImageLoader.getInstance().init(config);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

        //notificationsOffRef.goOnline();
        //usuariosRef.goOnline();
        //caronaRef.goOnline();


    }


    @Override
    public void onPause() {

        super.onPause();

        if(mCheckingAuthProgress != null){
            mCheckingAuthProgress.dismiss();
        }

        //notificationsOffRef.goOffline();
        //usuariosRef.goOffline();
        //caronaRef.goOffline();

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //pedidosArrayList.clear();
        if(mCheckingAuthProgress != null)
            mCheckingAuthProgress.dismiss();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){

        inflater.inflate(R.menu.menu_home_tabbed, menu);
        MenuItem searchItem = menu.findItem(R.id.procurarItem);
        searchItem.setVisible(false);
        /*MenuItem searchItem = menu.findItem(R.id.procurarItem);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText == null || newText.length() < 3){
                    caronasList.setAdapter(firebaseListAdapter);
                    return true;
                }else{
                    jaPesquisado = false;
                    searchOnFirebaseListViewAdapter(newText.toLowerCase());
                    Toast.makeText(getActivity().getApplicationContext(), "Executando pesquisa...", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });*/

        super.onCreateOptionsMenu(menu, inflater);

    }

    private void aceitarPedidoCarona(String usuarioId, String usuarioNome, String usuarioImgUrl, String usuarioTelefone, String notificationId) {
        /*task=new TimerTask(){
            @Override
            public void run() {
                elapsed+=INTERVAL;
                if(elapsed>=TIMEOUT){
                    this.cancel();
                    elapsed = 0;
                    mCheckingAuthProgress.hide();
                    Toast.makeText(getActivity().getApplicationContext(), "Pode ser que sua conexão com a Internet tenha expirado. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    checkVagasCarona(usuarioId, usuarioNome, usuarioImgUrl, usuarioTelefone, notificationId);
                }
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(task, INTERVAL, TIMEOUT);*/
        checkVagasCarona(usuarioId, usuarioNome, usuarioImgUrl, usuarioTelefone, notificationId);
    }

    private void checkVagasCarona(String usuarioId, String usuarioNome, String usuarioImgUrl, String usuarioTelefone, String notificationId){
        caronaRef.child(ListaCaronasFragmento.idParaVerCarona).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("vagas")){
                        vagasDisponiveis = dataSnapshot.child("vagas").getValue().toString();
                        nomecaroneiro1 = dataSnapshot.child("nomecaroneiro1").getValue().toString();
                        nomecaroneiro2 = dataSnapshot.child("nomecaroneiro2").getValue().toString();
                        nomecaroneiro3 = dataSnapshot.child("nomecaroneiro3").getValue().toString();
                        nomecaroneiro4 = dataSnapshot.child("nomecaroneiro4").getValue().toString();
                        if(vagasDisponiveis != null && nomecaroneiro1 != null && nomecaroneiro2 != null && nomecaroneiro3 != null && nomecaroneiro4 != null){
//                            task.cancel();
                            //elapsed = 0;
                            if(vagasDisponiveis.contains("0")){
                                numVagas = 0;
                            }else if(vagasDisponiveis.contains("1")){
                                numVagas = 1;
                            }else if(vagasDisponiveis.contains("2")){
                                numVagas = 2;
                            }else if(vagasDisponiveis.contains("3")){
                                numVagas = 3;
                            }else if(vagasDisponiveis.contains("4")){
                                numVagas = 4;
                            }

                            if(numVagas == 0){
                                alert_builder = new AlertDialog.Builder(getActivity());
                                alert_builder.setMessage("Sua carona já está cheia. Remova alguém para aceitar o pedido de " + usuarioNome + ".")
                                        .setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alert.hide();
                                    }
                                });
                                alert = alert_builder.create();
                                alert.show();

                            }else{

                                mCheckingAuthProgress.show();

                                numVagas--;
                                if(numVagas <= 1){
                                    numVagasTxt = String.valueOf(numVagas) + " vaga";
                                }else{
                                    numVagasTxt = String.valueOf(numVagas) + " vagas";
                                }

                                Map novoCaroneiroMap = new HashMap();

                                //criar um alerta para fazer o motorista enviar uma msg para o caroneiro para que combinem o lugar certo de se encontrarem
                                if("ASSENTO VAZIO".equals(nomecaroneiro1)){
                                    novoCaroneiroMap.put("idcaroneiro1", usuarioId);
                                    novoCaroneiroMap.put("nomecaroneiro1", usuarioNome);
                                    novoCaroneiroMap.put("imgcaroneiro1", usuarioImgUrl);
                                    novoCaroneiroMap.put("vagas", numVagasTxt);
                                    caronaRef.child(ListaCaronasFragmento.idParaVerCarona).updateChildren(novoCaroneiroMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                HashMap<String, String> notificationInfo = new HashMap<>();
                                                notificationInfo.put("from", mAuth.getCurrentUser().getUid());
                                                notificationInfo.put("type", "pedido_aceito");
                                                usuariosRef.child(usuarioId).child("status").setValue("caroneiro_" + mAuth.getCurrentUser().getUid());
                                                //usuariosRef.child(mAuth.getCurrentUser().getUid()).child("pedidos").child(usuarioId).removeValue();
                                                notificationsOffRef.child(mAuth.getCurrentUser().getUid()).child(notificationId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            removePedidosDoCaroneiroEmOutrasCaronas(usuarioId);
                                                            FirebaseDatabase.getInstance().getReference().child("notifications").child(usuarioId).push().setValue(notificationInfo);
                                                            Toast.makeText(getActivity().getApplicationContext(), usuarioNome + " entrou na sua carona", Toast.LENGTH_SHORT).show();
                                                            criarAlertaParaEnvioDeMsgAoCaroneiro(usuarioId, usuarioNome, usuarioImgUrl, usuarioTelefone);
                                                            mCheckingAuthProgress.hide();
                                                        }
                                                    }
                                                });
                                            }else{
                                                Toast.makeText(getContext(), "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }else if("ASSENTO VAZIO".equals(nomecaroneiro2)){
                                    novoCaroneiroMap.put("idcaroneiro2", usuarioId);
                                    novoCaroneiroMap.put("nomecaroneiro2", usuarioNome);
                                    novoCaroneiroMap.put("imgcaroneiro2", usuarioImgUrl);
                                    novoCaroneiroMap.put("vagas", numVagasTxt);
                                    caronaRef.child(ListaCaronasFragmento.idParaVerCarona).updateChildren(novoCaroneiroMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                HashMap<String, String> notificationInfo = new HashMap<>();
                                                notificationInfo.put("from", mAuth.getCurrentUser().getUid());
                                                notificationInfo.put("type", "pedido_aceito");

                                                usuariosRef.child(usuarioId).child("status").setValue("caroneiro_" + mAuth.getCurrentUser().getUid());
                                                //usuariosRef.child(mAuth.getCurrentUser().getUid()).child("pedidos").child(usuarioId).removeValue();
                                                notificationsOffRef.child(mAuth.getCurrentUser().getUid()).child(notificationId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            removePedidosDoCaroneiroEmOutrasCaronas(usuarioId);
                                                            FirebaseDatabase.getInstance().getReference().child("notifications").child(usuarioId).push().setValue(notificationInfo);
                                                            Toast.makeText(getActivity().getApplicationContext(), usuarioNome + " entrou na sua carona", Toast.LENGTH_SHORT).show();
                                                            criarAlertaParaEnvioDeMsgAoCaroneiro(usuarioId, usuarioNome, usuarioImgUrl, usuarioTelefone);
                                                            mCheckingAuthProgress.hide();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }else if("ASSENTO VAZIO".equals(nomecaroneiro3)) {
                                    novoCaroneiroMap.put("idcaroneiro3", usuarioId);
                                    novoCaroneiroMap.put("nomecaroneiro3", usuarioNome);
                                    novoCaroneiroMap.put("imgcaroneiro3", usuarioImgUrl);
                                    novoCaroneiroMap.put("vagas", numVagasTxt);
                                    caronaRef.child(ListaCaronasFragmento.idParaVerCarona).updateChildren(novoCaroneiroMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                HashMap<String, String> notificationInfo = new HashMap<>();
                                                notificationInfo.put("from", mAuth.getCurrentUser().getUid());
                                                notificationInfo.put("type", "pedido_aceito");

                                                usuariosRef.child(usuarioId).child("status").setValue("caroneiro_" + mAuth.getCurrentUser().getUid());
                                                //usuariosRef.child(mAuth.getCurrentUser().getUid()).child("pedidos").child(usuarioId).removeValue();
                                                notificationsOffRef.child(mAuth.getCurrentUser().getUid()).child(notificationId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            removePedidosDoCaroneiroEmOutrasCaronas(usuarioId);
                                                            FirebaseDatabase.getInstance().getReference().child("notifications").child(usuarioId).push().setValue(notificationInfo);
                                                            Toast.makeText(getActivity().getApplicationContext(), usuarioNome + " entrou na sua carona", Toast.LENGTH_SHORT).show();
                                                            criarAlertaParaEnvioDeMsgAoCaroneiro(usuarioId, usuarioNome, usuarioImgUrl, usuarioTelefone);
                                                            mCheckingAuthProgress.hide();
                                                        }else{
                                                            Toast.makeText(getActivity().getApplicationContext(), "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }else if("ASSENTO VAZIO".equals(nomecaroneiro4)) {
                                    novoCaroneiroMap.put("idcaroneiro4", usuarioId);
                                    novoCaroneiroMap.put("nomecaroneiro4", usuarioNome);
                                    novoCaroneiroMap.put("imgcaroneiro4", usuarioImgUrl);
                                    novoCaroneiroMap.put("vagas", numVagasTxt);
                                    caronaRef.child(ListaCaronasFragmento.idParaVerCarona).updateChildren(novoCaroneiroMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                HashMap<String, String> notificationInfo = new HashMap<>();
                                                notificationInfo.put("from", mAuth.getCurrentUser().getUid());
                                                notificationInfo.put("type", "pedido_aceito");
                                                usuariosRef.child(usuarioId).child("status").setValue("caroneiro_" + mAuth.getCurrentUser().getUid());
                                                //usuariosRef.child(mAuth.getCurrentUser().getUid()).child("pedidos").child(usuarioId).removeValue();
                                                notificationsOffRef.child(mAuth.getCurrentUser().getUid()).child(notificationId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            removePedidosDoCaroneiroEmOutrasCaronas(usuarioId);
                                                            FirebaseDatabase.getInstance().getReference().child("notifications").child(usuarioId).push().setValue(notificationInfo);
                                                            Toast.makeText(getActivity().getApplicationContext(), usuarioNome + " entrou na sua carona", Toast.LENGTH_SHORT).show();
                                                            criarAlertaParaEnvioDeMsgAoCaroneiro(usuarioId, usuarioNome, usuarioImgUrl, usuarioTelefone);
                                                            mCheckingAuthProgress.hide();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void criarAlertaParaEnvioDeMsgAoCaroneiro(String idDoCaroneiro, String nomeDoCaroneiro, String imgDoCaroneiro, String telefoneDoCaroneiro){
        alert_builder2 = new AlertDialog.Builder(getActivity());
        alert_builder2.setCancelable(true);
        alert_builder2.setMessage("Envie uma mensagem para combinar detalhes da carona com " + nomeDoCaroneiro + ".");
        alert_builder2.setPositiveButton("Enviar mensagem", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCheckingAuthProgress.setMessage("Abrindo chat...");
                mCheckingAuthProgress.show();
                Intent abrirConversa = new Intent(getActivity().getApplicationContext(), Conversa.class);
                abrirConversa.putExtra("idusuario_chat", idDoCaroneiro);
                abrirConversaTimer = new CountDownTimer(2000, 1000){
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        mCheckingAuthProgress.hide();
                        abrirConversa.putExtra("nomeusuario_chat", nomeDoCaroneiro);
                        abrirConversa.putExtra("imgusuario_chat", imgDoCaroneiro);
                        abrirConversa.putExtra("telefoneusuario_chat", telefoneDoCaroneiro);
                        //abrirConversa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(abrirConversa);
                    }
                };
                abrirConversaTimer.start();
            }
        });
        alert_builder2.setNegativeButton("Agora não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alert2.hide();
            }
        });
        alert2 = alert_builder2.create();
        alert2.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_settings) {
            Intent toPerfilIntent = new Intent(getActivity().getApplicationContext(), Perfil.class);
            Bundle extras = new Bundle();
            extras.putString("idusuario_chat", mAuth.getCurrentUser().getUid());
            extras.putString("idUsuarioCorrente", mAuth.getCurrentUser().getUid());
            toPerfilIntent.putExtras(extras);
            startActivity(toPerfilIntent);
        }else if(id == R.id.action_about){
            startActivity(new Intent(getActivity().getApplicationContext(), Sobre.class));
        }else if(id == R.id.action_privacy){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://dragoonssoft.wixsite.com/caronacap/politica-de-privacidade"));
            startActivity(browserIntent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        setRetainInstance(true);

    }

    @Override
    public void onStop() {
        super.onStop();
        if(alert != null){
            alert.dismiss();
        }
        if(alert2 != null){
            alert2.dismiss();
        }
    }

    private void removePedidosDoCaroneiroEmOutrasCaronas(String caroneiroId){
        notificationsOffRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    if(ds.hasChild("from")){
                        if(ds.child("from").getValue().toString().equals(caroneiroId)){
                            notificationsOffRef.child(ds.getKey()).removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static class CaronaViewHolder extends RecyclerView.ViewHolder{
        TextView nomeView;
        TextView horarioView;
        TextView vagasView;
        TextView partidaDestinoView;
        ImageView imgView;
        String caronaPrefs, dataCarona;
        DisplayImageOptions options;
        int defaultImage;
        ImageLoader imageLoader;
        RelativeLayout background;
        RelativeLayout.LayoutParams params;
        int first_params;
        public CaronaViewHolder(View itemView) {
            super(itemView);
            imageLoader = ImageLoader.getInstance();
            defaultImage = itemView.getContext().getResources().getIdentifier("@drawable/ic_launcher",null,itemView.getContext().getPackageName());
            options = new DisplayImageOptions.Builder().cacheInMemory(true)
                    .cacheOnDisk(true).resetViewBeforeLoading(true)
                    .showImageForEmptyUri(defaultImage)
                    .showImageOnFail(defaultImage)
                    .showImageOnLoading(defaultImage).build();
            nomeView = (TextView) itemView.findViewById(R.id.nomeList);
            horarioView = (TextView) itemView.findViewById(R.id.horarioList);
            vagasView = (TextView) itemView.findViewById(R.id.vagasList);
            partidaDestinoView = (TextView) itemView.findViewById(R.id.partidaDestinoList);
            imgView = (ImageView) itemView.findViewById(R.id.profileImagePerfil);
            background = itemView.findViewById(R.id.caronas_adapter_layout);
            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        public void setNome(String nome){
            nomeView.setText(nome);
        }
        public void setPreferencias(String preferencias){
            caronaPrefs = preferencias;
        }
        public void setHorario(String horario){
            horarioView.setText(horario);
        }
        public void setVagas(String vagas){
            vagasView.setText(vagas);
        }
        public void setPartidaDestino(String partidaDestino){
            partidaDestinoView.setText(partidaDestino);
        }
        public void setImg(String img){
            imageLoader.displayImage(img, imgView, options);
        }
        public void setData(String data){
            dataCarona = data;
        }

        private void hideLayout(){
            first_params = params.height;
            params.height = 0;
            background.setLayoutParams(params);
        }
        private void showLayout(){
            params.height = first_params;
            background.setLayoutParams(params);
        }



    }
}
