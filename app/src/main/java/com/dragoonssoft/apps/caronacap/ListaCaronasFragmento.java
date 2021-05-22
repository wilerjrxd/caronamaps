package com.dragoonssoft.apps.caronacap;


import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import androidx.core.app.Fragment;
import androidx.core.app.NotificationCompat;
import androidx.core.view.MenuItemCompat;
import androidx.core.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.common.StringUtils;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.NOTIFICATION_SERVICE;
import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListaCaronasFragmento extends Fragment {
    private static MenuItem pedidoCaronaItem, configItem, msgItem, sobreItem, procurarItem;
    private static SearchView pedidoCaronaView, configView, msgView, sobreView, procurarView;
    static Button criarCaronaBtn;
    private ArrayList<CaronaInfo> caronaArrayList;
    private CaronaListAdapter adapter;
    private static AdView mAdView;
    private static SwipeRefreshLayout refreshLayout;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase db;
    private DatabaseReference caronaRef, caronaDataRef, usuariosRef;
    private Query orderCaronasPorHorario, orderCaronasPorData, orderCaronasPorIdUsuario;
    private Bundle extras;
    private ListView caronasList;
    private boolean isCriarCarona = true;
    private Intent intentEditar;
    private String keyValue;
    private boolean areUsuariosSetToIdle = false;
    static String idParaVerCarona;
    static String numVagas;
    private static AlertDialog.Builder alert_builder;
    private String idMotorista, nomeCaroneiro1, nomeCaroneiro2, nomeCaroneiro3, nomeCaroneiro4, idCaroneiro1, idCaroneiro2, idCaroneiro3,idCaroneiro4,idUsuario, partida, destino, parada1, parada2;
    private static AlertDialog alert;
    static Boolean isCaronaCapCarregado = false, isProgressDismissed = false, isUsuarioAutenticado = false;
    private ProgressDialog mCarregandoCaronaCapProgress;
    private DatabaseReference usuarioRef;
    private FirebaseListAdapter<CaronaInfo> firebaseListAdapter, firebaseListAdapterSearch;
    private boolean jaPesquisado;
    private boolean jaPesquisado2;
    private Query caronaRefPartidaDestinoParaPesquisa, caronaRefMotoristaParaPesquisa;
    private DatabaseReference caronaRefParaPesquisa;
    private String auxStringPartidaDestino, auxStringMotorista;
    private TextView nenhumResultadoEncontrado;
    //static String tempCaroneiro1Id, tempCaroneiro2Id, tempCaroneiro3Id, tempCaroneiro4Id, tempIdMotorista;
    //private DatabaseReference notificationsRef;
    private boolean isUsuarioOnline = false;
    private boolean naListaDeCaronas = false;
    static String idUsuarioCorrenteGlobal, nomeUsuarioCorrenteGlobal, imgUrlUsuarioCorrenteGlobal, telefoneUsuarioCorrenteGlobal, carroUsuarioCorrenteGlobal;
    private int vezesQuePassouPorAqui=0;
    private DatabaseReference notificationsOffRef;
    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
    private boolean isNotificationSent = false;
    private boolean caronaExiste = false;
    private ValueEventListener listener;
    private DatabaseReference adRef;
    private ValueEventListener adRefListener;
    private CountDownTimer mostrarPropagandaTimer;
    private int counter = 0;
    private long elapsed = 0;
    private long INTERVAL = 1000, TIMEOUT = 20000;
    private TimerTask task;
    private Timer timer;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private boolean isOnScreen = true;
    static String preferenciasGlobal;

    private String usuarioStatus, usuarioPreferencias;
    static String receberNotificationsPrefGlobal;


    public ListaCaronasFragmento() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista_caronas_fragmento,container,false);

        setHasOptionsMenu(true);

        caronasList = (ListView) view.findViewById(R.id.caronasList);
        nenhumResultadoEncontrado = (TextView) view.findViewById(R.id.nenhum_resultado);

        mAuth = FirebaseAuth.getInstance();
        caronaRef = db.getInstance().getReference().child("caronas");
        caronaRefParaPesquisa = caronaRef;
        usuariosRef = db.getInstance().getReference().child("usuarios");
        usuarioRef = usuariosRef.child(mAuth.getCurrentUser().getUid());
        orderCaronasPorHorario = caronaRef.orderByChild("horario");//.equalTo(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()));
        //orderCaronasPorHorario.keepSynced(true);
        orderCaronasPorIdUsuario = caronaRef.orderByChild("idusuario").equalTo(mAuth.getCurrentUser().getUid());
        orderCaronasPorData = caronaRef.orderByChild("data");
        //notificationsRef = db.getInstance().getReference().child("notifications");
        notificationsOffRef = db.getInstance().getReference().child("notifications_off");
        adRef = db.getInstance().getReference().child("ad");

        idUsuarioCorrenteGlobal = mAuth.getCurrentUser().getUid();

        mCarregandoCaronaCapProgress = new ProgressDialog(getActivity());
        mCarregandoCaronaCapProgress.setMessage("Carregando...");
        mCarregandoCaronaCapProgress.setCanceledOnTouchOutside(false);
        mCarregandoCaronaCapProgress.setCancelable(false);

        /*alert_builder = new AlertDialog.Builder(getActivity());
        alert_builder.setMessage("Nenhuma carona disponível por enquanto")
                .setCancelable(true);
        alert = alert_builder.create();*/

        //caronasList.setHasFixedSize(true);
        //caronasList.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        setupImageLoader();
        imageLoader = ImageLoader.getInstance();
        int defaultImage = getActivity().getApplicationContext().getResources().getIdentifier("@drawable/ic_launcher_100",null,getActivity().getApplicationContext().getPackageName());
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();
        firebaseListAdapter = new FirebaseListAdapter<CaronaInfo>(getActivity(), CaronaInfo.class, R.layout.carona_item, orderCaronasPorHorario) {
            @Override
            protected void populateView(View view, CaronaInfo carona, int position) {

                TextView nomeView = (TextView) view.findViewById(R.id.nomeList);
                TextView horarioView = (TextView) view.findViewById(R.id.horarioList);
                TextView vagasView = (TextView) view.findViewById(R.id.vagasList);
                TextView partidaDestinoView = (TextView) view.findViewById(R.id.partidaDestinoList);
                ImageView imgView = (ImageView) view.findViewById(R.id.profileImagePerfil);
                RelativeLayout background = view.findViewById(R.id.caronas_adapter_layout);

                String numVagasTxt = carona.getVagas();

                //String caronaPrefs = carona.getPreferencias();

                /*if(caronaPrefs.contains(Login.pref1) || caronaPrefs.contains(Login.pref2) || caronaPrefs.contains(Login.pref3) || caronaPrefs.contains(Login.pref4)){
                    //background.setBackgroundColor(getResources().getColor(R.color.green_translucent));
                }*/

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
                }else {
                    if (criarCaronaBtn.getText().equals(getActivity().getResources().getString(R.string.ver_carona))) {
                        criarCaronaBtn.setText(getActivity().getResources().getString(R.string.criar_carona));
                    }
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
            }
        };
        //caronaArrayList = new ArrayList<>();
        //adapter = new CaronaListAdapter(getActivity().getApplicationContext(), R.layout.list_caronas_adapter,caronaArrayList);
        caronasList.setAdapter(firebaseListAdapter);

        if(!isCaronaCapCarregado) {
            mCarregandoCaronaCapProgress.show();
            new CountDownTimer(4000, 500) {

                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    //mCarregandoCaronaCapProgress.hide();
                    task=new TimerTask(){
                    @Override
                    public void run() {
                        elapsed+=INTERVAL;
                        if(elapsed>=TIMEOUT){
                            this.cancel();
                            elapsed = 0;
                            mCarregandoCaronaCapProgress.hide();
                            Toast.makeText(getActivity().getApplicationContext(), "Pode ser que sua conexão com a Internet tenha expirado. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                            return;
                        }else{
                            checkSeTemAtualizacaoOuPropaganda();
                        }
                    }
                };
                timer = new Timer();
                timer.scheduleAtFixedRate(task, INTERVAL, TIMEOUT);

                    isCaronaCapCarregado = true;
//                    Toast.makeText(getActivity().getApplicationContext(), "Caronas carregadas.", Toast.LENGTH_SHORT).show();
                }
            }.start();
        }

        criarCaronaBtn = (Button) view.findViewById(R.id.criarCaronaHorBtn);
        criarCaronaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //connMgr = (ConnectivityManager)getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                //networkInfo = connMgr.getActiveNetworkInfo();
                //if(networkInfo != null && networkInfo.isConnected()) {
                    if(criarCaronaBtn.getText().equals(getActivity().getResources().getString(R.string.criar_carona))) {
                        ///////////mCarregandoCaronaCapProgress.dismiss();
                        startActivity(new Intent(getActivity().getApplicationContext(), CriarCarona.class));
                    } else {
                        //////////mCarregandoCaronaCapProgress.dismiss();
                        Intent paraVerCaronaIntent = new Intent(getActivity().getApplicationContext(), DealWithCarona.class);
                        paraVerCaronaIntent.putExtra("idcarona", idParaVerCarona);
                        startActivity(paraVerCaronaIntent);
                    }
                //}
                //else{
                //    Toast.makeText(getActivity().getApplicationContext(), "Sem conexão com a Internet.", Toast.LENGTH_SHORT).show();
                //}
            }
        });



        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){

                }else{

                    Toast.makeText(getActivity().getApplicationContext(), "Algo deu errado. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();

                }
            }
        };

        orderCaronasPorData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userId = mAuth.getCurrentUser().getUid();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(ds.hasChildren()) {
                        if (getActivity() != null) {
                            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                            networkInfo = connMgr.getActiveNetworkInfo();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        caronaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if(naListaDeCaronas == false) {
                    if(dataSnapshot.hasChild("status")) {
                        usuarioStatus = dataSnapshot.child("status").getValue().toString();
                        String nenhum;
                        if(dataSnapshot.hasChild("preferencias")) {
                            usuarioPreferencias = dataSnapshot.child("preferencias").getValue().toString();
                            preferenciasGlobal = usuarioPreferencias;
                        }else{
                            if(getActivity() != null) {
                                nenhum = getActivity().getResources().getString(R.string.nenhum);
                                usuarioPreferencias = nenhum + "_" + nenhum + "-" + nenhum + ";" + nenhum;
                                preferenciasGlobal = usuarioPreferencias;
                            }
                        }
                        if(dataSnapshot.hasChild("receber_notifications_pref")){
                            if(receberNotificationsPrefGlobal == null)
                                receberNotificationsPrefGlobal = dataSnapshot.child("receber_notifications_pref").getValue().toString();
                        }else{
                            receberNotificationsPrefGlobal = "true";
                        }
                        if(usuarioStatus.contains("idle")){
                            if(getActivity() != null) {
                                criarCaronaBtn.setText(getActivity().getResources().getString(R.string.criar_carona));
                                HomeTabbed.tabLayout.getTabAt(2).setText(getActivity().getResources().getString(R.string.preferencias));
                            }
                        }else if(usuarioStatus.contains("aguardando") || usuarioStatus.contains("caroneiro")) {
                            HomeTabbed.tabLayout.getTabAt(2).setText(getActivity().getResources().getString(R.string.preferencias));
                            if(getActivity() != null) {
                                criarCaronaBtn.setText(getActivity().getResources().getString(R.string.ver_carona));
                            }
                        }else if(usuarioStatus.contains("motorista")){
                            String usuarioCaronaID = usuarioStatus.split("motorista_")[1];
                            if(usuarioCaronaID != null) {
                                caronaRef.orderByKey().equalTo(usuarioCaronaID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            if (getActivity() != null) {
                                                criarCaronaBtn.setText(getActivity().getResources().getString(R.string.ver_carona));
                                                HomeTabbed.tabLayout.getTabAt(2).setText(getActivity().getResources().getString(R.string.pedidos));
                                            }
                                        } else {
                                            usuarioRef.child("status").setValue("idle");
                                            if(getActivity() != null) {
                                                criarCaronaBtn.setText(getActivity().getResources().getString(R.string.criar_carona));
                                                HomeTabbed.tabLayout.getTabAt(2).setText(getActivity().getResources().getString(R.string.preferencias));
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                        ///////////////
                    	if(nomeUsuarioCorrenteGlobal == null)
                    		nomeUsuarioCorrenteGlobal = dataSnapshot.child("nome").getValue().toString();
                    	if(imgUrlUsuarioCorrenteGlobal == null)
                    		imgUrlUsuarioCorrenteGlobal = dataSnapshot.child("imgURL").getValue().toString();
                    	if(telefoneUsuarioCorrenteGlobal == null)
                    		telefoneUsuarioCorrenteGlobal = dataSnapshot.child("telefone").getValue().toString();
                    	if(carroUsuarioCorrenteGlobal == null){
                    	    if(dataSnapshot.hasChild("carro")){
                    	        carroUsuarioCorrenteGlobal = dataSnapshot.child("carro").getValue().toString();
                            }
                        }
                    	/////////////
                    }
                //}

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        return view;//inflater.inflate(R.layout.fragment_lista_caronas_fragmento, container, false);
    }

    private void showData(DataSnapshot dataSnapshot) {
        counter = 0;
        if(!dataSnapshot.hasChildren()){
            nenhumResultadoEncontrado.setVisibility(View.VISIBLE);
            new CountDownTimer(3000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    mCarregandoCaronaCapProgress.hide();
                }
            }.start();
        }else{
            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                //CaronaInfo caronaInfo = ds.getValue(CaronaInfo.class);
                if (ds.exists()) {
                    String keyValue = ds.getKey();
                    caronaExiste = true;
                    String s = null;
                    if(ds.hasChild("horario"))
                        s = ds.child("horario").getValue().toString();
                    String dataCarona = null;
                    if(ds.hasChild("data"))
                        dataCarona = ds.child("data").getValue().toString();
                    idUsuario = mAuth.getCurrentUser().getUid();
                    idMotorista = ds.child("idusuario").getValue().toString();
                    idCaroneiro1 = ds.child("idcaroneiro1").getValue().toString();
                    idCaroneiro2 = ds.child("idcaroneiro2").getValue().toString();
                    idCaroneiro3 = ds.child("idcaroneiro3").getValue().toString();
                    idCaroneiro4 = ds.child("idcaroneiro4").getValue().toString();
                    nomeCaroneiro1 = ds.child("nomecaroneiro1").getValue().toString();
                    nomeCaroneiro2 = ds.child("nomecaroneiro2").getValue().toString();
                    nomeCaroneiro3 = ds.child("nomecaroneiro3").getValue().toString();
                    nomeCaroneiro4 = ds.child("nomecaroneiro4").getValue().toString();
                    if(ds.hasChild("partida")){
                        partida = ds.child("partida").getValue().toString();
                    }
                    if(ds.hasChild("destino")){
                        destino = ds.child("destino").getValue().toString();
                    }
                    if(ds.hasChild("parada1")){
                        parada1 = ds.child("parada1").getValue().toString();
                    }
                    if(ds.hasChild("parada2")){
                        parada2 = ds.child("parada2").getValue().toString();
                    }
                    if(ds.hasChild("vagas"))
                        numVagas = ds.child("vagas").getValue().toString();
                    else
                        caronaRef.child(ds.getKey()).child("vagas").setValue("4 vagas");

                    if(!dataCarona.equals(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime())) || isCaronaExpirada(s)) {
                        isNotificationSent = false;
                        if (idUsuario.equals(idMotorista)) {
                            usuariosRef.child(idUsuario).child("status").setValue("idle");
                            criarCaronaBtn.setText(getActivity().getResources().getString(R.string.criar_carona));
                        }
                        if (idUsuario.equals(idMotorista) || idUsuario.equals(idCaroneiro1) || idUsuario.equals(idCaroneiro2) || idUsuario.equals(idCaroneiro3) || idUsuario.equals(idCaroneiro4)) {
                            if (!idCaroneiro1.equals("")) {
                                usuariosRef.child(idCaroneiro1).child("status").setValue("idle");
                                if(idUsuario.equals(idCaroneiro1)){
                                    criarCaronaBtn.setText(getActivity().getResources().getString(R.string.criar_carona));
                                }
                            }
                            if (!idCaroneiro2.equals("")) {

                                usuariosRef.child(idCaroneiro2).child("status").setValue("idle");
                                if(idUsuario.equals(idCaroneiro2)){
                                    criarCaronaBtn.setText(getActivity().getResources().getString(R.string.criar_carona));
                                }
                            }
                            if (!idCaroneiro3.equals("")) {

                                usuariosRef.child(idCaroneiro3).child("status").setValue("idle");
                                if(idUsuario.equals(idCaroneiro3)){
                                    criarCaronaBtn.setText(getActivity().getResources().getString(R.string.criar_carona));
                                }
                            }
                            if (!idCaroneiro4.equals("")) {

                                usuariosRef.child(idCaroneiro4).child("status").setValue("idle");
                                if(idUsuario.equals(idCaroneiro4)){
                                    criarCaronaBtn.setText(getActivity().getResources().getString(R.string.criar_carona));
                                }
                            }
                            isUsuarioAguardandoCarona(idMotorista);
                            caronaRef.child(keyValue).removeValue();
                            caronaExiste = false;

                            notificationsOffRef.child(idMotorista).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        dataSnapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }else{
                            if (idUsuario.equals(idMotorista)) {
                                usuariosRef.child(idUsuario).child("status").setValue("idle");
                                criarCaronaBtn.setText(getActivity().getResources().getString(R.string.criar_carona));
                            }
                            if (!idCaroneiro1.equals("")) {
                                usuariosRef.child(idCaroneiro1).child("status").setValue("idle");
                                if(idUsuario.equals(idCaroneiro1)){
                                    criarCaronaBtn.setText(R.string.criar_carona);
                                }
                            }
                            if (!idCaroneiro2.equals("")) {
                                usuariosRef.child(idCaroneiro2).child("status").setValue("idle");
                                if(idUsuario.equals(idCaroneiro2)){
                                    criarCaronaBtn.setText(R.string.criar_carona);
                                }
                            }
                            if (!idCaroneiro3.equals("")) {
                                usuariosRef.child(idCaroneiro3).child("status").setValue("idle");
                                if(idUsuario.equals(idCaroneiro3)){
                                    criarCaronaBtn.setText(R.string.criar_carona);
                                }
                            }
                            if (!idCaroneiro4.equals("")) {
                                usuariosRef.child(idCaroneiro4).child("status").setValue("idle");
                                if(idUsuario.equals(idCaroneiro4)){
                                    criarCaronaBtn.setText(R.string.criar_carona);
                                }
                            }
                            isUsuarioAguardandoCarona(idMotorista);
                            caronaRef.child(keyValue).removeValue();
                            caronaExiste = false;

                            notificationsOffRef.child(idMotorista).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        dataSnapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }


                    }else {
                        if (idUsuario.equals(idMotorista)) {
                            criarCaronaBtn.setText(getActivity().getResources().getString(R.string.ver_carona));
                            idParaVerCarona = keyValue;
                            if (ds.exists()) {
                                usuariosRef.child(idMotorista).child("status").setValue("motorista_" + keyValue);
                            }
                        } else {
                            if(usuarioStatus != null){
                                if(usuarioStatus.equals("aguardando_" + idMotorista) || idUsuario.equals(idCaroneiro1) || idUsuario.equals(idCaroneiro2) || idUsuario.equals(idCaroneiro3) || idUsuario.equals(idCaroneiro4)){
                                    criarCaronaBtn.setText(getActivity().getResources().getString(R.string.ver_carona));
                                    idParaVerCarona = keyValue;
                                }else{
                                    if(getActivity() != null)
                                        criarCaronaBtn.setText(getActivity().getResources().getString(R.string.criar_carona));
                                }
                                if(usuarioPreferencias != null){
                                    if(usuarioPreferencias.contains(partida) || usuarioPreferencias.contains(destino) || usuarioPreferencias.contains(parada1) || usuarioPreferencias.contains(parada2)){
                                        HomeTabbed.tabLayout.getTabAt(2).setText(getActivity().getResources().getString(R.string.preferencias_1));
                                    }else{
                                        HomeTabbed.tabLayout.getTabAt(2).setText(getActivity().getResources().getString(R.string.preferencias));
                                    }
                                }
                            }
                            //usuariosRef.child(idUsuario).child("status").setValue("idle");
                        }
                        nenhumResultadoEncontrado.setVisibility(View.GONE);
                    }

                    counter++;
                    if(counter >= dataSnapshot.getChildrenCount()){
                        mCarregandoCaronaCapProgress.hide();
                        counter = 0;
                    }

                }
            }
        }

            //}
        //}
    }

    private boolean isCaronaExpirada(String horario){
        String horasString = horario.split(":")[0];
        String minutosAuxString = horario.split(":")[1];
        String minutos = minutosAuxString.split("h")[0];
        Calendar horarioAtual = Calendar.getInstance();

        int hora = Integer.parseInt(horasString);
        int min = Integer.parseInt(minutos);

        if(hora < horarioAtual.get(Calendar.HOUR_OF_DAY)) {
            return true;
        }
        else if(hora == horarioAtual.get(Calendar.HOUR_OF_DAY)){
            if(min < horarioAtual.get(Calendar.MINUTE)-15) {
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }

    }

    /*private void showDataCaronaDoUsuario(DataSnapshot ds) {
        String keyValue = null;
        //Iterable<DataSnapshot> children = dataSnapshot.getChildren();
        //for(DataSnapshot ds : dataSnapshot.getChildren()){

        if(ds.hasChild(mAuth.getCurrentUser().getUid())) {
            keyValue = ds.getKey();
            //Toast.makeText(getActivity().getApplicationContext(), "CaronaInfo " + keyValue + "existe.", Toast.LENGTH_SHORT).show();
            //if (ds.hasChild("nome")) {
            /*if(criarCaronaBtn.getText().toString().equals("VER CARONA")) {
                CaronaInfo caronaInfo = ds.getValue(CaronaInfo.class);

                intentEditar = new Intent(getActivity().getApplicationContext(), PedirCarona.class);
                extras = new Bundle();
                extras.putString("idusuario", caronaInfo.getIdusuario());
                extras.putString("nome", caronaInfo.getNome());
                extras.putString("img", caronaInfo.getImg());
                extras.putString("idcaroneiro1", caronaInfo.getIdcaroneiro1());
                extras.putString("nomecaroneiro1", caronaInfo.getNomecaroneiro1());
                extras.putString("imgcaroneiro1", caronaInfo.getImgcaroneiro1());
                extras.putString("idcaroneiro2", caronaInfo.getIdcaroneiro2());
                extras.putString("nomecaroneiro2", caronaInfo.getNomecaroneiro2());
                extras.putString("imgcaroneiro2", caronaInfo.getImgcaroneiro2());
                extras.putString("idcaroneiro3", caronaInfo.getIdcaroneiro3());
                extras.putString("nomecaroneiro3", caronaInfo.getNomecaroneiro3());
                extras.putString("imgcaroneiro3", caronaInfo.getImgcaroneiro3());
                extras.putString("idcaroneiro4", caronaInfo.getIdcaroneiro4());
                extras.putString("nomecaroneiro4", caronaInfo.getNomecaroneiro4());
                extras.putString("imgcaroneiro4", caronaInfo.getImgcaroneiro4());
                extras.putString("horario", caronaInfo.getHorario());
                extras.putString("vagas", caronaInfo.getVagas());
                extras.putString("partida", caronaInfo.getPartida());
                extras.putString("destino", caronaInfo.getDestino());
                extras.putString("partidadestino", caronaInfo.getPartidaDestino());
                intentEditar.putExtras(extras);

                Toast.makeText(getActivity().getApplicationContext(), "CaronaInfo de " + caronaInfo.getNome(), Toast.LENGTH_SHORT).show();

                isCriarCarona = false;
            }

        }else{
            if(criarCaronaBtn.getText().toString().equals("CRIAR CARONA")){
                Toast.makeText(getActivity().getApplicationContext(), "CaronaInfo não existe.", Toast.LENGTH_SHORT).show();
                isCriarCarona = true;
            }
        }
        //}
    }*/

    @Override
    public void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);

        isOnScreen = true;
        caronaRef.goOnline();
        caronaRefParaPesquisa.goOnline();
        usuariosRef.goOnline();
        usuarioRef.goOnline();
        notificationsOffRef.goOnline();

    }
    
    @Override
    public void onPause() {
        super.onPause();

        isOnScreen = false;
        caronaRef.goOffline();
        caronaRefParaPesquisa.goOffline();
        usuariosRef.goOffline();
        usuarioRef.goOffline();
        notificationsOffRef.goOffline();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        mCarregandoCaronaCapProgress.dismiss();
        if(alert != null){
            alert.dismiss();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mCarregandoCaronaCapProgress.dismiss();
        if(alert != null){
            alert.dismiss();
        }
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

    /*private boolean isCaronaAntiga(String dataAtual, String dataCarona){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date date1 = sdf.parse(dataAtual);
            Date date2 = sdf.parse(dataCarona);
            Toast.makeText(getActivity().getApplicationContext(), date1.toString() + " " + date2.toString(), Toast.LENGTH_SHORT);
            if(date1.after(date2)) {
                Toast.makeText(getActivity().getApplicationContext(), "isCaronaAntiga: true", Toast.LENGTH_SHORT);
                return true;
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "isCaronaAntiga: false", Toast.LENGTH_SHORT);
                return false;
            }
        }catch(ParseException ex){
            ex.printStackTrace();
            return false;
        }
    }*/

    private void isUsuarioAguardandoCarona(String idMotorista){
        listener = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.exists()){
                        if(ds.hasChild("status")) {
                            String status = ds.child("status").getValue().toString();
                            if (status.equals("aguardando_" + idMotorista)) {
                                String key = ds.getKey();
                                usuariosRef.child(key).child("status").setValue("idle");
                            }
                        }
                    }
                }
                new CountDownTimer(800, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        usuariosRef.removeEventListener(listener);
                    }
                }.start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void searchOnFirebaseListViewAdapter(String child){

        caronaRefPartidaDestinoParaPesquisa = caronaRefParaPesquisa.orderByChild("partidadestinolower");
        caronaRefMotoristaParaPesquisa = caronaRefParaPesquisa.orderByChild("nomelower");

        caronaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                        if (ds.exists()) {
                            if(ds.hasChild("nomelower"))
                                auxStringMotorista = ds.child("nomelower").getValue().toString().toLowerCase();
                            else
                                auxStringMotorista = "";

                            if (ds.hasChild("partidadestinolower")){
                                auxStringPartidaDestino = ds.child("partidadestinolower").getValue().toString().toLowerCase();//ds.child("partidalower").getValue().toString().toLowerCase() + " > " + ds.child("destinolower").getValue().toString().toLowerCase();
                            }else {
                                auxStringPartidaDestino = "";
                            }

                            if (auxStringMotorista.contains(child) && auxStringPartidaDestino.contains(child)) {
                                firebaseListAdapterSearch = new FirebaseListAdapter<CaronaInfo>(getActivity(), CaronaInfo.class, R.layout.list_caronas_adapter, caronaRefMotoristaParaPesquisa.equalTo(auxStringMotorista)) {
                                    @Override
                                    protected void populateView(View view, CaronaInfo carona, int position) {

                                        TextView nomeView = (TextView) view.findViewById(R.id.nomeList);
                                        TextView horarioView = (TextView) view.findViewById(R.id.horarioList);
                                        TextView vagasView = (TextView) view.findViewById(R.id.vagasList);
                                        TextView partidaDestinoView = (TextView) view.findViewById(R.id.partidaDestinoList);
                                        ImageView imgView = (ImageView) view.findViewById(R.id.profileImagePerfil);

                                        String numVagasTxt = carona.getVagas();

                                        if(carona.getData().equals(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()))) {
                                            nomeView.setText(carona.getNome());
                                            horarioView.setText(carona.getHorario());
                                            vagasView.setText(numVagasTxt);
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
                                                    String partidaDestinoTxt = carona.getPartida() + " > " + getActivity().getResources().getString(R.string.cap_ida);
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
                                                    String partidaDestinoTxt = carona.getPartida() + " > " + carona.getDestino();
                                                    partidaDestinoView.setText(partidaDestinoTxt);
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
                                        }else {
                                            if (criarCaronaBtn.getText().equals(getActivity().getResources().getString(R.string.ver_carona))) {
                                                criarCaronaBtn.setText(getActivity().getResources().getString(R.string.criar_carona));
                                            }
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
                                    }
                                };
                                caronasList.setAdapter(firebaseListAdapterSearch);
                                Log.d("CARONA", "Carona com caracteres -- " + child + " -- encontrada!");
                            } else if (auxStringMotorista.contains(child) && !auxStringPartidaDestino.contains(child)) {
                                firebaseListAdapterSearch = new FirebaseListAdapter<CaronaInfo>(getActivity(), CaronaInfo.class, R.layout.list_caronas_adapter, caronaRefMotoristaParaPesquisa.equalTo(auxStringMotorista)) {
                                    @Override
                                    protected void populateView(View view, CaronaInfo carona, int position) {

                                        TextView nomeView = (TextView) view.findViewById(R.id.nomeList);
                                        TextView horarioView = (TextView) view.findViewById(R.id.horarioList);
                                        TextView vagasView = (TextView) view.findViewById(R.id.vagasList);
                                        TextView partidaDestinoView = (TextView) view.findViewById(R.id.partidaDestinoList);
                                        ImageView imgView = (ImageView) view.findViewById(R.id.profileImagePerfil);

                                        String numVagasTxt = carona.getVagas();

                                        if(carona.getData().equals(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()))) {
                                            nomeView.setText(carona.getNome());
                                            horarioView.setText(carona.getHorario());
                                            vagasView.setText(numVagasTxt);
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
                                                    String partidaDestinoTxt = carona.getPartida() + " > " + getActivity().getResources().getString(R.string.cap_ida);
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
                                                    String partidaDestinoTxt = carona.getPartida() + " > " + carona.getDestino();
                                                    partidaDestinoView.setText(partidaDestinoTxt);
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
                                        }else {
                                            if (criarCaronaBtn.getText().equals(getActivity().getResources().getString(R.string.ver_carona))) {
                                                criarCaronaBtn.setText(getActivity().getResources().getString(R.string.criar_carona));
                                            }
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
                                    }
                                };
                                caronasList.setAdapter(firebaseListAdapterSearch);
                                Log.d("CARONA", "Carona com caracteres -- " + child + " -- encontrada!");
                            } else if (!auxStringMotorista.contains(child) && auxStringPartidaDestino.contains(child)) {
                                firebaseListAdapterSearch = new FirebaseListAdapter<CaronaInfo>(getActivity(), CaronaInfo.class, R.layout.list_caronas_adapter, caronaRefPartidaDestinoParaPesquisa.equalTo(auxStringPartidaDestino)) {
                                    @Override
                                    protected void populateView(View view, CaronaInfo carona, int position) {

                                        TextView nomeView = (TextView) view.findViewById(R.id.nomeList);
                                        TextView horarioView = (TextView) view.findViewById(R.id.horarioList);
                                        TextView vagasView = (TextView) view.findViewById(R.id.vagasList);
                                        TextView partidaDestinoView = (TextView) view.findViewById(R.id.partidaDestinoList);
                                        ImageView imgView = (ImageView) view.findViewById(R.id.profileImagePerfil);

                                        String numVagasTxt = carona.getVagas();

                                        if(carona.getData().equals(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()))) {
                                            nomeView.setText(carona.getNome());
                                            horarioView.setText(carona.getHorario());
                                            vagasView.setText(numVagasTxt);
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
                                                    String partidaDestinoTxt = carona.getPartida() + " > " + getActivity().getResources().getString(R.string.cap_ida);
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
                                                    String partidaDestinoTxt = carona.getPartida() + " > " + carona.getDestino();
                                                    partidaDestinoView.setText(partidaDestinoTxt);
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
                                        }else {
                                            if (criarCaronaBtn.getText().equals(getActivity().getResources().getString(R.string.ver_carona))) {
                                                criarCaronaBtn.setText(getActivity().getResources().getString(R.string.criar_carona));
                                            }
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
                                    }
                                };
                                caronasList.setAdapter(firebaseListAdapterSearch);
                                Log.d("CARONA", "Carona com caracteres -- " + child + " -- encontrada!");
                            } else if(!auxStringMotorista.contains(child) && !auxStringPartidaDestino.contains(child)){
                                caronasList.setAdapter(firebaseListAdapter);
                                if(getActivity() != null)
                                    Toast.makeText(getActivity().getApplicationContext(), "Nenhum resultado encontrado. Mostrando todas as caronas disponíveis.", Toast.LENGTH_SHORT).show();
                            }else{
                                caronasList.setAdapter(firebaseListAdapter);
                            }
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //caronaArrayList = new ArrayList<>();
        //adapter = new CaronaListAdapter(getActivity().getApplicationContext(), R.layout.list_caronas_adapter,caronaArrayList);
    }

    private void checkSeTemAtualizacaoOuPropaganda(){
    	adRef.child("ad").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Random rand = new Random();
                    Random idRand = new Random();
                    int childrenCount = Integer.parseInt(String.valueOf(dataSnapshot.getChildrenCount()));
                    int idAd;
                    if(childrenCount == 2)
                        idAd = 2;
                    else
                        idAd = idRand.nextInt(childrenCount) + 2;
                    String idAdText = String.valueOf(idAd);
                    String linkImg = dataSnapshot.child(String.valueOf(idAd)).child("link_img").getValue().toString();
                    String linkSaberMais = dataSnapshot.child(idAdText).child("link_saber_mais").getValue().toString();
                    String versaoCorrente = dataSnapshot.child("1").child("versao").getValue().toString();
                    int impressions, clicks;

                    if(dataSnapshot.child(idAdText).hasChild("impressions"))
                        impressions = Integer.parseInt(String.valueOf(dataSnapshot.child(idAdText).child("impressions").getValue()));
                    else
                        impressions = 0;

                    if(dataSnapshot.child(idAdText).hasChild("clicks"))
                        clicks = Integer.parseInt(String.valueOf(dataSnapshot.child(idAdText).child("clicks").getValue()));
                    else
                        clicks = 0;



                    if (linkImg != null && linkSaberMais != null && versaoCorrente != null && getActivity() != null) {
                        task.cancel();
                        elapsed = 0;
                        if (!getActivity().getResources().getString(R.string.versao).equals(versaoCorrente)) {
                            alert_builder = new AlertDialog.Builder(getActivity());
                            alert_builder.setTitle("Atualização disponível")
                                .setMessage("Uma nova atualização do caronaCAP está disponível. Algumas funcionalidades da versão que você está utilizando podem não funcionar corretamente quando outros usuários pedirem ou oferecerem carona a você.")
                                .setPositiveButton("Atualizar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.dragoonssoft.apps.caronacap"));
                                        startActivity(browserIntent);
                                    }
                                })
                                .setCancelable(false);
                            alert = alert_builder.create();
                            alert.show();
                        }else{
                            int abcd = rand.nextInt(20);
                            if (abcd == 5) {
                                if (!linkImg.equals("") && !linkSaberMais.equals("") && isOnScreen) {
                                    impressions++;
                                    adRef.child("ad").child(idAdText).child("impressions").setValue(impressions);
                                    Intent abrirPropaganda = new Intent(getActivity().getApplicationContext(), Propaganda.class);
                                    abrirPropaganda.putExtra("link_img", linkImg);
                                    abrirPropaganda.putExtra("link_saber_mais", linkSaberMais);
                                    abrirPropaganda.putExtra("clicks", String.valueOf(clicks));
                                    abrirPropaganda.putExtra("id_ad", idAdText);
                                    startActivity(abrirPropaganda);
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

    private void createAndOpenNotification(String title, String text, String tab){
    	if(!isNotificationSent){
	        HomeTabbed.mBuilder = new NotificationCompat.Builder(getActivity())
	                        .setSmallIcon(R.mipmap.ic_launcher)
	                        .setContentTitle(title)
	                        .setContentText(text)
	                        .setAutoCancel(true)
	                        .setDefaults(DEFAULT_SOUND);
	                        //.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

	        HomeTabbed.resultIntent = new Intent("caronacap.apps.dragoonssoft.com.caronacap_TARGET_NOTIFICATION");
	        HomeTabbed.resultIntent.putExtra("tab", tab);

	        HomeTabbed.resultPendingIntent =
	                PendingIntent.getActivity(getActivity(), 0, HomeTabbed.resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	        HomeTabbed.mBuilder.setContentIntent(HomeTabbed.resultPendingIntent);


	        int mNotificationId = (int)System.currentTimeMillis();
	        HomeTabbed.mNotifyMgr = (NotificationManager)getActivity().getSystemService(NOTIFICATION_SERVICE);
	        HomeTabbed.mNotifyMgr.notify(mNotificationId, HomeTabbed.mBuilder.build());
	        isNotificationSent = true;
	    }

    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_home,menu);

        MenuItem searchItem = menu.findItem(R.id.procurarItem);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){

        inflater.inflate(R.menu.menu_home_tabbed, menu);
        MenuItem searchItem = menu.findItem(R.id.procurarItem);
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
                    searchOnFirebaseListViewAdapter(newText.toLowerCase());
                    //Toast.makeText(getActivity().getApplicationContext(), "Procurando por -= " + newText + " =-", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_settings) {

            Intent toPerfilIntent = new Intent(getActivity().getApplicationContext(), Perfil.class);
            Bundle extras = new Bundle();
            if(idUsuarioCorrenteGlobal != null) {
                extras.putString("idusuario_chat", idUsuarioCorrenteGlobal);
                extras.putString("idusuario_corrente", idUsuarioCorrenteGlobal);
            }else{
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                extras.putString("idusuario_chat", userID);
                extras.putString("idusuario_corrente", userID);
            }
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
    /*@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        setRetainInstance(true);

    }*/
}
