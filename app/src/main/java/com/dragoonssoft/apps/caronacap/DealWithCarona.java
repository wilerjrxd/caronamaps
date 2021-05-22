package com.dragoonssoft.apps.caronacap;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
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
import java.util.Timer;
import java.util.TimerTask;

import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;
import static androidx.core.app.NotificationCompat.DEFAULT_VIBRATE;
import static androidx.core.app.NotificationCompat.getActionCount;

public class DealWithCarona extends AppCompatActivity {

    private Toolbar dealWithCaronaToolbar;
    private Bundle extras;
    private ListView caronaDadosList;
    private ArrayList<CaronaInfo> caronaDadosArrayList = new ArrayList<>();
    private CaronaListMotoristaViewAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase db;
    private Query encontrarIdUsuarioEmCarona;
    private Toolbar pedirCaronaToolbar;
    private FirebaseUser mFirebaseUser;
    private Button pedirCancelarSairCaronaBtn;
    private String partida, destino;
    private Query caronaIdUsuarioRef;
    private DatabaseReference caronaRef, allUsuariosRef, usuarioRef, pedidosRef, notificationsRef, pedidosNotificacoesRef;
    private Query caronaIdUsuarioParaModificarBtnRef;
    private ProgressDialog mLidarComCaronaProgress;
    private Boolean isAlreadyDone = false, pedidoJaEnviado = false;
    private String usuarioStatus;
    private Boolean isFinished = false, isMotoristaUsuarioCorrente = false;
    private String idCarona, idMotoristaDeOutraCarona, nomeUsuario, idUsuario, imgUsuario, nomeCaroneiro1, imgCaroneiro1, idCaroneiro1, nomeCaroneiro2, imgCaroneiro2, idCaroneiro2, nomeCaroneiro3, imgCaroneiro3, idCaroneiro3, nomeCaroneiro4, imgCaroneiro4, idCaroneiro4, horario, vagas, partidaDestino, dataCarona;
    private Query caronaOrderByKeyRef;
    private int numVagas, vagasAux = 0;
    private TextView txHorario, txPartidaDestino, txNomeMotorista, txCaroneiro1, txCaroneiro2, txCaroneiro3, txCaroneiro4;
    private ImageView motoristaImgView, caroneiro1ImgView, caroneiro2ImgView, caroneiro3ImgView, caroneiro4ImgView;
    private Button fecharCaroneiro1Btn, fecharCaroneiro2Btn, fecharCaroneiro3Btn, fecharCaroneiro4Btn, msgCaroneiro1Btn, msgCaroneiro2Btn, msgCaroneiro3Btn, msgCaroneiro4Btn;
    static String idCaronaGlobal, idMotorista;
    private static AlertDialog.Builder alert_builder;
    private static AlertDialog alert;
    private Boolean justEntered = true;
    private String idCaroneiro1Temp, idCaroneiro2Temp, idCaroneiro3Temp, idCaroneiro4Temp;
    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
    private DatabaseReference conversasRef;
    private boolean notShown = false;
    //private boolean onScreen = true;
    private DatabaseReference notificationsOffRef;
    private String userId;
    private boolean isSaved = false;
    private boolean isNotificationAlreadySent = false;
    private boolean caronaExiste = true;
    private boolean jaCancelado = false;
    private ValueEventListener listener;
    private ValueEventListener paraCancelarPedidoListener, checkUsuarioStatusListener;
    private CountDownTimer checkUsuarioStatusTimer;
    private CountDownTimer pedidoCountDownTimer;
    private String imgURL, telefone;
    private CountDownTimer pedirCaronaTimer;
    private CountDownTimer checkIfIsDoneSettingValueTimer;
    private boolean pedidoJaCancelado = false;
    private ValueEventListener allUsuariosRefListener;
    private boolean toastJaMostrado = false;
    private TimerTask task, checkStatusTask;
    private Timer timer;
    private long elapsed = 0, elapsed2 = 0;
    private long INTERVAL = 1000, TIMEOUT = 20000;
    private String carro;
    private TextView txCarro;
    private Timer checkStatusTimer;
    private ValueEventListener caronaListener;
    private String parada1, parada2;
    private TextView txParada1Parada2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_with_carona);

        pedirCancelarSairCaronaBtn = (Button) findViewById(R.id.pedirCancelarSairCaronaBtn);
        dealWithCaronaToolbar = (Toolbar) findViewById(R.id.deal_with_carona_toolbar);
        setSupportActionBar(dealWithCaronaToolbar);
        getSupportActionBar().setTitle("Carona");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        idCarona = getIntent().getStringExtra("idcarona");

        mLidarComCaronaProgress = new ProgressDialog(this);
        mLidarComCaronaProgress.setMessage("Carregando dados da carona...");
        mLidarComCaronaProgress.setCanceledOnTouchOutside(false);
        mLidarComCaronaProgress.setCancelable(false);
        mLidarComCaronaProgress.show();

        setupImageLoader();
        ImageLoader imageLoader = ImageLoader.getInstance();
        int defaultImage = getResources().getIdentifier("@drawable/ic_launcher",null,getPackageName());
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        txPartidaDestino = (TextView)findViewById(R.id.partidaDestinoLabel);
        //txParada1Parada2 = (TextView)findViewById(R.id.paradasText);
        txHorario = (TextView)findViewById(R.id.horario);
        txNomeMotorista = (TextView)findViewById(R.id.motoristaNome);
        txCarro = (TextView)findViewById(R.id.carroNome);
        txCaroneiro1 = (TextView)findViewById(R.id.caroneiro1Nome);
        txCaroneiro2 = (TextView)findViewById(R.id.caroneiro2Nome);
        txCaroneiro3 = (TextView)findViewById(R.id.caroneiro3Nome);
        txCaroneiro4 = (TextView)findViewById(R.id.caroneiro4Nome);
        motoristaImgView = (ImageView)findViewById(R.id.profileImagePerfil);
        caroneiro1ImgView = (ImageView)findViewById(R.id.caroneiro1Foto);
        caroneiro2ImgView = (ImageView)findViewById(R.id.caroneiro2Foto);
        caroneiro3ImgView = (ImageView)findViewById(R.id.caroneiro3Foto);
        caroneiro4ImgView = (ImageView)findViewById(R.id.caroneiro4Foto);
        fecharCaroneiro1Btn = (Button)findViewById(R.id.tirarCaroneiro1Btn);
        fecharCaroneiro2Btn = (Button)findViewById(R.id.tirarCaroneiro2Btn);
        fecharCaroneiro3Btn = (Button)findViewById(R.id.tirarCaroneiro3Btn);
        fecharCaroneiro4Btn = (Button)findViewById(R.id.tirarCaroneiro4Btn);
        pedirCancelarSairCaronaBtn = (Button)findViewById(R.id.pedirCancelarSairCaronaBtn);
        pedirCancelarSairCaronaBtn.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        if(idCarona != null) {
            idCaronaGlobal = idCarona;
        }

        userId = mAuth.getCurrentUser().getUid();
        // caronaIdUsuarioRef = db.getInstance().getReference().child("caronas").orderByChild("idusuario").equalTo(idMotorista);
        //caronaIdUsuarioParaModificarBtnRef = db.getInstance().getReference().child("caronas").orderByChild("idusuario").equalTo(userId);
        caronaRef = db.getInstance().getReference().child("caronas").child(idCaronaGlobal);
        //caronaOrderByKeyRef = db.getInstance().getReference().child("caronas").orderByKey().equalTo(idCaronaGlobal);
        allUsuariosRef = db.getInstance().getReference().child("usuarios");
        usuarioRef = allUsuariosRef.child(userId);
        //pedidosRef = db.getInstance().getReference().child("pedidos");
        notificationsRef = db.getInstance().getReference().child("notifications");
        conversasRef = FirebaseDatabase.getInstance().getReference().child("conversas").child(mAuth.getCurrentUser().getUid());
        notificationsOffRef = FirebaseDatabase.getInstance().getReference().child("notifications_off");

        caronaListener = caronaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if(dataSnapshot.exists()) {
                    //if (!isCancelarBtnClicked) {
                        //caronaDadosList = (ListView) findViewById(R.id.list_dados_carona);
                        //adapter = new CaronaListMotoristaViewAdapter(DealWithCarona.this, R.layout.list_dados_carona_motorista_view_adapter, caronaDadosArrayList);
                        //caronaDadosList.setAdapter(adapter);
                    //if(onScreen){
                        if (dataSnapshot.exists()) {
                            caronaExiste = true;
                            idUsuario = mAuth.getCurrentUser().getUid();
                            idMotorista = dataSnapshot.child("idusuario").getValue().toString();
                            nomeUsuario = dataSnapshot.child("nome").getValue().toString();
                            imgUsuario = dataSnapshot.child("img").getValue().toString();
                            idCaroneiro1 = dataSnapshot.child("idcaroneiro1").getValue().toString();
                            nomeCaroneiro1 = dataSnapshot.child("nomecaroneiro1").getValue().toString();
                            imgCaroneiro1 = dataSnapshot.child("imgcaroneiro1").getValue().toString();
                            idCaroneiro2 = dataSnapshot.child("idcaroneiro2").getValue().toString();
                            nomeCaroneiro2 = dataSnapshot.child("nomecaroneiro2").getValue().toString();
                            imgCaroneiro2 = dataSnapshot.child("imgcaroneiro2").getValue().toString();
                            idCaroneiro3 = dataSnapshot.child("idcaroneiro3").getValue().toString();
                            nomeCaroneiro3 = dataSnapshot.child("nomecaroneiro3").getValue().toString();
                            imgCaroneiro3 = dataSnapshot.child("imgcaroneiro3").getValue().toString();
                            idCaroneiro4 = dataSnapshot.child("idcaroneiro4").getValue().toString();
                            nomeCaroneiro4 = dataSnapshot.child("nomecaroneiro4").getValue().toString();
                            imgCaroneiro4 = dataSnapshot.child("imgcaroneiro4").getValue().toString();
                            if(dataSnapshot.hasChild("partidaDestino"))
                                partidaDestino = dataSnapshot.child("partidaDestino").getValue().toString();
                            if(dataSnapshot.hasChild("partida"))
                                partida = dataSnapshot.child("partida").getValue().toString();
                            if(dataSnapshot.hasChild("destino"))
                                destino = dataSnapshot.child("destino").getValue().toString();
                            horario = dataSnapshot.child("horario").getValue().toString();
                            dataCarona = dataSnapshot.child("data").getValue().toString();
                            vagas = dataSnapshot.child("vagas").getValue().toString();
                            if(dataSnapshot.hasChild("carro"))
                                carro = dataSnapshot.child("carro").getValue().toString();
                            else
                                carro = "Não informado";

                            if(vagas != null) {
                                if (vagas.contains("0")) {
                                    numVagas = 0;
                                } else if (vagas.contains("1")) {
                                    numVagas = 1;
                                } else if (vagas.contains("2")) {
                                    numVagas = 2;
                                } else if (vagas.contains("3")) {
                                    numVagas = 3;
                                } else if (vagas.contains("4")) {
                                    numVagas = 4;
                                }else if(vagas.contains("5")){
                                    numVagas = 5;
                                }else if(vagas.contains("6")){
                                    numVagas = 6;
                                }else if(vagas.contains("7")){
                                    numVagas = 7;
                                }
                            }else{
                                Toast.makeText(DealWithCarona.this, "Algo deu errado. Tente entrar nos detalhes da carona novamente.", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            if(dataSnapshot.hasChild("partida")){
                                String partidaDestinoTxt = null;
                                if(dataSnapshot.hasChild("parada1") && dataSnapshot.hasChild("parada2")) {
                                    parada1 = dataSnapshot.child("parada1").getValue().toString();
                                    parada2 = dataSnapshot.child("parada2").getValue().toString();

                                    if("CAP".equals(partida)) {
                                        if (!parada1.equals("nopref") && !parada2.equals("nopref")) {
                                            partidaDestinoTxt = partida + " > " + parada1 + " > " + parada2 + " > " + destino;
                                        } else if (!parada1.equals("nopref") && parada2.equals("nopref")) {
                                            partidaDestinoTxt = partida + " > " + parada1 + " > " + destino;
                                        }else if (!parada2.equals("nopref") && parada1.equals("nopref")) {
                                            partidaDestinoTxt = partida + " > " + parada2 + " > " + destino;
                                        }else if (parada1.equals("nopref") && parada2.equals("nopref")) {
                                            partidaDestinoTxt = partida + " > " + destino;
                                        }
                                        txPartidaDestino.setText(partidaDestinoTxt);
                                        txPartidaDestino.setTextColor(getResources().getColor(R.color.darkslateblue));
                                    }else if("CAP".equals(destino)){
                                        if (!parada1.equals("nopref") && !parada2.equals("nopref")) {
                                            partidaDestinoTxt = partida + " > " + parada1 + " > " + parada2 + " > " + destino;
                                        } else if (!parada1.equals("nopref") && parada2.equals("nopref")) {
                                            partidaDestinoTxt = partida + " > " + parada1 + " > " + destino;
                                        }else if (!parada2.equals("nopref") && parada1.equals("nopref")) {
                                            partidaDestinoTxt = partida + " > " + parada2 + " > " + destino;
                                        }else if (parada1.equals("nopref") && parada2.equals("nopref")) {
                                            partidaDestinoTxt = partida + " > " + destino;
                                        }
                                        txPartidaDestino.setText(partidaDestinoTxt);
                                        txPartidaDestino.setTextColor(Color.RED);
                                    }else{
                                        if (!parada1.equals("nopref") && !parada2.equals("nopref")) {
                                            partidaDestinoTxt = partida + " > " + parada1 + " > " + parada2 + " > " + destino;
                                        } else if (!parada1.equals("nopref") && parada2.equals("nopref")) {
                                            partidaDestinoTxt = partida + " > " + parada1 + " > " + destino;
                                        }else if (!parada2.equals("nopref") && parada1.equals("nopref")) {
                                            partidaDestinoTxt = partida + " > " + parada2 + " > " + destino;
                                        }else if (parada1.equals("nopref") && parada2.equals("nopref")) {
                                            partidaDestinoTxt = partida + " > " + destino;
                                        }
                                        txPartidaDestino.setText(partidaDestinoTxt);
                                    }
                                }else{
                                    if("CAP".equals(partida)){
                                        partidaDestinoTxt = getResources().getString(R.string.cap_volta) + " > " + destino;
                                        txPartidaDestino.setText(partidaDestinoTxt);
                                        txPartidaDestino.setTextColor(getResources().getColor(R.color.darkslateblue));
                                    }else if("CAP".equals(destino)){
                                        partidaDestinoTxt = partida + " > " + getResources().getString(R.string.cap_ida);
                                        txPartidaDestino.setText(partidaDestinoTxt);
                                        txPartidaDestino.setTextColor(Color.RED);
                                    }else{
                                        partidaDestinoTxt = partida + " > " + destino;
                                        txPartidaDestino.setText(partidaDestinoTxt);
                                        txPartidaDestino.setTextColor(Color.BLACK);
                                    }
                                }
                            }else{
                                txPartidaDestino.setText(partidaDestino);
                                txPartidaDestino.setTextColor(Color.BLACK);
                            }

                            txHorario.setText(horario);
                            txNomeMotorista.setText(nomeUsuario);
                            txCarro.setText(carro);
                            txCaroneiro1.setText(nomeCaroneiro1);
                            txCaroneiro2.setText(nomeCaroneiro2);
                            txCaroneiro3.setText(nomeCaroneiro3);
                            txCaroneiro4.setText(nomeCaroneiro4);
                            imageLoader.displayImage(imgUsuario, motoristaImgView, options);
                            imageLoader.displayImage(imgCaroneiro1, caroneiro1ImgView, options);
                            imageLoader.displayImage(imgCaroneiro2, caroneiro2ImgView, options);
                            imageLoader.displayImage(imgCaroneiro3, caroneiro3ImgView, options);
                            imageLoader.displayImage(imgCaroneiro4, caroneiro4ImgView, options);

                            motoristaImgView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    toUsuarioPerfil(idMotorista, nomeUsuario, imgUsuario);
                                    //mLidarComCaronaProgress.hide();
                                }
                            });
                            caroneiro1ImgView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    toUsuarioPerfil(idCaroneiro1, nomeCaroneiro1, imgCaroneiro1);
                                    //mLidarComCaronaProgress.hide();
                                }
                            });
                            caroneiro2ImgView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    toUsuarioPerfil(idCaroneiro2, nomeCaroneiro2, imgCaroneiro2);
                                    //mLidarComCaronaProgress.hide();
                                }
                            });
                            caroneiro3ImgView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    toUsuarioPerfil(idCaroneiro3, nomeCaroneiro3, imgCaroneiro3);
                                    // mLidarComCaronaProgress.hide();
                                }
                            });
                            caroneiro4ImgView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    toUsuarioPerfil(idCaroneiro4, nomeCaroneiro4, imgCaroneiro4);
                                    // mLidarComCaronaProgress.hide();
                                }
                            });
                            //if(!isSaved) {

                            checkUsuarioStatusComTimerTask(nomeUsuario, idMotorista, idUsuario);

                            if(!isSaved) {
                                if (idMotorista.equals(userId)) {
                                    setTextoBotoes();
                                    setTextoCorDasLabels();
                                    isMotoristaUsuarioCorrente = true;
                                    invalidateOptionsMenu();
                                } else {
                                    setTextoCorDasLabels();
                                    isMotoristaUsuarioCorrente = false;
                                    invalidateOptionsMenu();
                                    fecharCaroneiro1Btn.setVisibility(View.GONE);
                                    fecharCaroneiro2Btn.setVisibility(View.GONE);
                                    fecharCaroneiro3Btn.setVisibility(View.GONE);
                                    fecharCaroneiro4Btn.setVisibility(View.GONE);
                                }
                            }

                            if (!dataCarona.equals(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime())) || isCaronaExpirada(horario)) {
                                if (idUsuario.equals(idMotorista)) {
                                    allUsuariosRef.child(idUsuario).child("status").setValue("idle");
                                }
                                if (idUsuario.equals(idMotorista) || idUsuario.equals(idCaroneiro1) || idUsuario.equals(idCaroneiro2) || idUsuario.equals(idCaroneiro3) || idUsuario.equals(idCaroneiro4)) {
                                    if (!idCaroneiro1.equals("")) {
                                        allUsuariosRef.child(idCaroneiro1).child("status").setValue("idle");
                                        if (idUsuario.equals(idCaroneiro1)) {
                                            ListaCaronasFragmento.criarCaronaBtn.setText(getResources().getString(R.string.criar_carona));
                                        }
                                    }
                                    if (!idCaroneiro2.equals("")) {
                                        allUsuariosRef.child(idCaroneiro2).child("status").setValue("idle");
                                        if (idUsuario.equals(idCaroneiro2)) {
                                            ListaCaronasFragmento.criarCaronaBtn.setText(getResources().getString(R.string.criar_carona));
                                        }
                                    }
                                    if (!idCaroneiro3.equals("")) {
                                        allUsuariosRef.child(idCaroneiro3).child("status").setValue("idle");
                                        if (idUsuario.equals(idCaroneiro3)) {
                                            ListaCaronasFragmento.criarCaronaBtn.setText(getResources().getString(R.string.criar_carona));
                                        }
                                    }
                                    if (!idCaroneiro4.equals("")) {
                                        allUsuariosRef.child(idCaroneiro4).child("status").setValue("idle");
                                        if (idUsuario.equals(idCaroneiro4)) {
                                            ListaCaronasFragmento.criarCaronaBtn.setText(getResources().getString(R.string.criar_carona));
                                        }
                                    }

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
                                    isUsuarioAguardandoCarona(idMotorista);
                                    caronaRef.removeValue();
                                    caronaExiste = false;
                                    finish();
                                } else {
                                    allUsuariosRef.child(idMotorista).child("status").setValue("idle");
                                    if (!idCaroneiro1.equals("")) {
                                        allUsuariosRef.child(idCaroneiro1).child("status").setValue("idle");
                                        if (idUsuario.equals(idCaroneiro1)) {
                                            ListaCaronasFragmento.criarCaronaBtn.setText(getResources().getString(R.string.criar_carona));
                                        }
                                    }
                                    if (!idCaroneiro2.equals("")) {
                                        allUsuariosRef.child(idCaroneiro2).child("status").setValue("idle");
                                        if (idUsuario.equals(idCaroneiro2)) {
                                            ListaCaronasFragmento.criarCaronaBtn.setText(getResources().getString(R.string.criar_carona));
                                        }
                                    }
                                    if (!idCaroneiro3.equals("")) {
                                        allUsuariosRef.child(idCaroneiro3).child("status").setValue("idle");
                                        if (idUsuario.equals(idCaroneiro3)) {
                                            ListaCaronasFragmento.criarCaronaBtn.setText(getResources().getString(R.string.criar_carona));
                                        }
                                    }
                                    if (!idCaroneiro4.equals("")) {
                                        allUsuariosRef.child(idCaroneiro4).child("status").setValue("idle");
                                        if (idUsuario.equals(idCaroneiro4)) {
                                            ListaCaronasFragmento.criarCaronaBtn.setText(getResources().getString(R.string.criar_carona));
                                        }
                                    }

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
                                    isUsuarioAguardandoCarona(idMotorista);
                                    caronaRef.removeValue();
                                    caronaExiste = false;
                                    finish();
                                }
                            }
                            //}
                            //doItWhenMotoristaIsUsuarioCorrenteOrNot(idMotorista, mAuth.getCurrentUser().getUid());
                            //Toast.makeText(PedirCarona.this, "DS MOTORISTA: " + dataSnapshot.child("nome").getValue().toString(), Toast.LENGTH_LONG).show();
                        }else{
                            if(idMotorista!=null) {
                                if (!idMotorista.equals(mAuth.getCurrentUser().getUid())) {
                                    //if(onScreen) {
                                    Toast.makeText(DealWithCarona.this, "Esta carona foi cancelada.", Toast.LENGTH_SHORT).show();
                                    ListaCaronasFragmento.criarCaronaBtn.setText(getResources().getString(R.string.criar_carona));
                                    finish();
                                    //}
                                }
                            }else{
                                Toast.makeText(DealWithCarona.this, "Esta carona foi cancelada ou expirou.", Toast.LENGTH_SHORT).show();
                                ListaCaronasFragmento.criarCaronaBtn.setText(getResources().getString(R.string.criar_carona));
                                finish();
                            }
                        }
                    //}
                //}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //RESERVANDO E LIBERANDO VAGAS

        fecharCaroneiro1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fecharCaroneiro1Btn.getText().equals("RESERVAR")) {
                    //if (txCaroneiro1.getText().equals("ASSENTO VAZIO")) {
                    // caronasRef.child("nomecaroneiro2").setValue("OCUPADO");
                    fecharCaroneiro1Btn.setText("LIBERAR");
                    txCaroneiro1.setText("OCUPADO");
                    txCaroneiro1.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    //numVagas--;
                } else if (fecharCaroneiro1Btn.getText().equals("REMOVER")) {
                    alert_builder = new AlertDialog.Builder(DealWithCarona.this);
                    alert_builder.setMessage("Deseja realmente remover esta pessoa de sua carona? Essa alteração será feita instantaneamente.").setCancelable(false);
                    alert_builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                            networkInfo = connMgr.getActiveNetworkInfo();
                            if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                                fecharCaroneiro1Btn.setText("LIBERAR");
                                txCaroneiro1.setText("OCUPADO");
                                txCaroneiro1.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                imageLoader.displayImage("", caroneiro1ImgView, options);
                                //numVagas--;
                                String nomecaroneiro1Aux = nomeCaroneiro1;
                                HashMap<String, String> notificationInfo = new HashMap<>();
                                notificationInfo.put("from", mAuth.getCurrentUser().getUid());
                                notificationInfo.put("type", "retirado_da_carona");
                                mLidarComCaronaProgress.setMessage("Retirando caroneiro...");
                                mLidarComCaronaProgress.show();
                                allUsuariosRef.child(idCaroneiro1).child("status").setValue("idle");
                                Map idCaroneiro1Map = new HashMap();
                                idCaroneiro1Map.put("idcaroneiro1", "");
                                idCaroneiro1Map.put("imgcaroneiro1", "");
                                idCaroneiro1Map.put("nomecaroneiro1", "OCUPADO");
                                caronaRef.updateChildren(idCaroneiro1Map);
                                notificationsRef.child(idCaroneiro1).push().setValue(notificationInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mLidarComCaronaProgress.hide();
                                            Toast.makeText(DealWithCarona.this, "Você retirou " + nomecaroneiro1Aux + " de sua carona.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            mLidarComCaronaProgress.hide();
                                            Toast.makeText(DealWithCarona.this, "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                Toast.makeText(DealWithCarona.this, "Você precisa estar conectado à Internet para fazer isso.", Toast.LENGTH_SHORT).show();
                                alert.hide();
                            }
                        }
                    }).setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alert.hide();
                        }
                    });
                    alert = alert_builder.create();
                    alert.show();
                } else{
                    fecharCaroneiro1Btn.setText("RESERVAR");
                    txCaroneiro1.setText("ASSENTO VAZIO");
                    txCaroneiro1.setTextColor(getResources().getColor(R.color.assento_vazio_color));
                }
            }

        });

        fecharCaroneiro2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fecharCaroneiro2Btn.getText().equals("RESERVAR")) {
                    //if (txCaroneiro1.getText().equals("ASSENTO VAZIO")) {
                    // caronasRef.child("nomecaroneiro2").setValue("OCUPADO");
                    fecharCaroneiro2Btn.setText("LIBERAR");
                    txCaroneiro2.setText("OCUPADO");
                    txCaroneiro2.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    //numVagas--;
                } else if (fecharCaroneiro2Btn.getText().equals("REMOVER")) {
                    alert_builder = new AlertDialog.Builder(DealWithCarona.this);
                    alert_builder.setMessage("Deseja realmente remover esta pessoa de sua carona?").setCancelable(false);
                    alert_builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                            networkInfo = connMgr.getActiveNetworkInfo();
                            if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                                fecharCaroneiro2Btn.setText("LIBERAR");
                                txCaroneiro2.setText("OCUPADO");
                                txCaroneiro2.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                imageLoader.displayImage("", caroneiro2ImgView, options);
                                //numVagas--;
                                String nomecaroneiro2Aux = nomeCaroneiro2;
                                HashMap<String, String> notificationInfo = new HashMap<>();
                                notificationInfo.put("from", mAuth.getCurrentUser().getUid());
                                notificationInfo.put("type", "retirado_da_carona");
                                mLidarComCaronaProgress.setMessage("Retirando caroneiro...");
                                mLidarComCaronaProgress.show();
                                allUsuariosRef.child(idCaroneiro2).child("status").setValue("idle");
                                Map idCaroneiro2Map = new HashMap();
                                idCaroneiro2Map.put("idcaroneiro2", "");
                                idCaroneiro2Map.put("imgcaroneiro2", "");
                                idCaroneiro2Map.put("nomecaroneiro2", "OCUPADO");
                                caronaRef.updateChildren(idCaroneiro2Map);
                                notificationsRef.child(idCaroneiro2).push().setValue(notificationInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mLidarComCaronaProgress.hide();
                                            Toast.makeText(DealWithCarona.this, "Você retirou " + nomecaroneiro2Aux + " de sua carona.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            mLidarComCaronaProgress.hide();
                                            Toast.makeText(DealWithCarona.this, "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                Toast.makeText(DealWithCarona.this, "Você precisa estar conectado à Internet para fazer isso.", Toast.LENGTH_SHORT).show();
                                alert.hide();
                            }
                        }
                    }).setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alert.hide();
                        }
                    });
                    alert = alert_builder.create();
                    alert.show();
                    // caronasRef.child("nomecaroneiro2").setValue("OCUPADO");
                    // usuariosRef.child(idCaroneiro2).child("status").setValue("idle");
                } else{
                    //caronasRef.child("nomecaroneiro1").setValue("ASSENTO VAZIO");
                    //caronasRef.child("idcaroneiro1").setValue("");
                    //caronasRef.child(ListaCaronasFragmento.idParaVerCarona).child("imgcaroneiro1").setValue("");
                    fecharCaroneiro2Btn.setText("RESERVAR");
                    txCaroneiro2.setText("ASSENTO VAZIO");
                    txCaroneiro2.setTextColor(getResources().getColor(R.color.assento_vazio_color));
                    //numVagas++;
                }
            }
        });

        fecharCaroneiro3Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fecharCaroneiro3Btn.getText().equals("RESERVAR")) {
                    //if (txCaroneiro1.getText().equals("ASSENTO VAZIO")) {
                    // caronasRef.child("nomecaroneiro2").setValue("OCUPADO");
                    fecharCaroneiro3Btn.setText("LIBERAR");
                    txCaroneiro3.setText("OCUPADO");
                    txCaroneiro3.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    //numVagas--;
                } else if (fecharCaroneiro3Btn.getText().equals("REMOVER")) {
                    alert_builder = new AlertDialog.Builder(DealWithCarona.this);
                    alert_builder.setMessage("Deseja realmente remover esta pessoa de sua carona?").setCancelable(false);
                    alert_builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                            networkInfo = connMgr.getActiveNetworkInfo();
                            if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                                fecharCaroneiro3Btn.setText("LIBERAR");
                                txCaroneiro3.setText("OCUPADO");
                                txCaroneiro3.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                imageLoader.displayImage("", caroneiro3ImgView, options);
                                //numVagas--;
                                String nomecaroneiro3Aux = nomeCaroneiro3;
                                HashMap<String, String> notificationInfo = new HashMap<>();
                                notificationInfo.put("from", mAuth.getCurrentUser().getUid());
                                notificationInfo.put("type", "retirado_da_carona");
                                mLidarComCaronaProgress.setMessage("Retirando caroneiro...");
                                mLidarComCaronaProgress.show();
                                allUsuariosRef.child(idCaroneiro3).child("status").setValue("idle");
                                Map idCaroneiro3Map = new HashMap();
                                idCaroneiro3Map.put("idcaroneiro3", "");
                                idCaroneiro3Map.put("imgcaroneiro3", "");
                                idCaroneiro3Map.put("nomecaroneiro3", "OCUPADO");
                                caronaRef.updateChildren(idCaroneiro3Map);
                                notificationsRef.child(idCaroneiro3).push().setValue(notificationInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mLidarComCaronaProgress.hide();
                                            Toast.makeText(DealWithCarona.this, "Você retirou " + nomecaroneiro3Aux + " de sua carona.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            mLidarComCaronaProgress.hide();
                                            Toast.makeText(DealWithCarona.this, "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                Toast.makeText(DealWithCarona.this, "Você precisa estar conectado à Internet para fazer isso.", Toast.LENGTH_SHORT).show();
                                alert.hide();
                            }
                        }
                    }).setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alert.hide();
                        }
                    });
                    alert = alert_builder.create();
                    alert.show();
                } else{
                    fecharCaroneiro3Btn.setText("RESERVAR");
                    txCaroneiro3.setText("ASSENTO VAZIO");
                    txCaroneiro3.setTextColor(getResources().getColor(R.color.assento_vazio_color));
                }
            }

        });

        fecharCaroneiro4Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fecharCaroneiro4Btn.getText().equals("RESERVAR")) {
                    //if (txCaroneiro1.getText().equals("ASSENTO VAZIO")) {
                    // caronasRef.child("nomecaroneiro2").setValue("OCUPADO");
                    fecharCaroneiro4Btn.setText("LIBERAR");
                    txCaroneiro4.setText("OCUPADO");
                    txCaroneiro4.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    //numVagas--;
                } else if (fecharCaroneiro4Btn.getText().equals("REMOVER")) {
                    alert_builder = new AlertDialog.Builder(DealWithCarona.this);
                    alert_builder.setMessage("Deseja realmente remover esta pessoa de sua carona?").setCancelable(false);
                    alert_builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                            networkInfo = connMgr.getActiveNetworkInfo();
                            if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                                fecharCaroneiro4Btn.setText("LIBERAR");
                                txCaroneiro4.setText("OCUPADO");
                                txCaroneiro4.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                imageLoader.displayImage("", caroneiro4ImgView, options);
                                //numVagas--;
                                String nomecaroneiro4Aux = nomeCaroneiro4;
                                HashMap<String, String> notificationInfo = new HashMap<>();
                                notificationInfo.put("from", mAuth.getCurrentUser().getUid());
                                notificationInfo.put("type", "retirado_da_carona");
                                mLidarComCaronaProgress.setMessage("Retirando caroneiro...");
                                mLidarComCaronaProgress.show();
                                allUsuariosRef.child(idCaroneiro4).child("status").setValue("idle");
                                Map idCaroneiro4Map = new HashMap();
                                idCaroneiro4Map.put("idcaroneiro4", "");
                                idCaroneiro4Map.put("imgcaroneiro4", "");
                                idCaroneiro4Map.put("nomecaroneiro4", "OCUPADO");
                                caronaRef.updateChildren(idCaroneiro4Map);
                                notificationsRef.child(idCaroneiro4).push().setValue(notificationInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mLidarComCaronaProgress.hide();
                                            Toast.makeText(DealWithCarona.this, "Você retirou " + nomecaroneiro4Aux + " de sua carona.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            mLidarComCaronaProgress.hide();
                                            Toast.makeText(DealWithCarona.this, "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                Toast.makeText(DealWithCarona.this, "Você precisa estar conectado à Internet para fazer isso.", Toast.LENGTH_SHORT).show();
                                alert.hide();
                            }
                        }
                    }).setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alert.hide();
                        }
                    });
                    alert = alert_builder.create();
                    alert.show();
                    // caronasRef.child("nomecaroneiro2").setValue("OCUPADO");
                    // usuariosRef.child(idCaroneiro2).child("status").setValue("idle");
                } else{
                    //caronasRef.child("nomecaroneiro1").setValue("ASSENTO VAZIO");
                    //caronasRef.child("idcaroneiro1").setValue("");
                    //caronasRef.child(ListaCaronasFragmento.idParaVerCarona).child("imgcaroneiro1").setValue("");
                    fecharCaroneiro4Btn.setText("RESERVAR");
                    txCaroneiro4.setText("ASSENTO VAZIO");
                    txCaroneiro4.setTextColor(getResources().getColor(R.color.assento_vazio_color));
                    //numVagas++;
                }
            }

        });

        /*notificationsOffRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if(onScreen) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.hasChild("type")) {
                            if(!isNotificationAlreadySent){
                                String keyValue = ds.getKey();
                                if (ds.child("type").getValue().toString().equals("msg")) {
                                    //String from_nome = ds.child("from_nome").getValue().toString();
                                    //String from_id = ds.child("from_id").getValue().toString();
                                    //String from_img = ds.child("from_img").getValue().toString();
                                    //String from_telefone = ds.child("from_telefone").getValue().toString();
                                    createAndOpenNotification("Mensagem", "Você recebeu uma nova mensagem", "msg", "", "", "", "", "1", keyValue);
                                }else if(ds.child("type").getValue().toString().equals("carona_cancelada")){
                                    createAndOpenNotification("Carona cancelada", "A carona em que você estava foi cancelada.", "carona_cancelada", "", "", "", "", "0" , keyValue);
                                }else if(ds.child("type").getValue().toString().equals("retirado_da_carona")){
                                    createAndOpenNotification("Retirado da carona", "Você foi retirado da carona em que estava.", "retirado_da_carona", "", "", "", "", "0" , keyValue);
                                }else if(ds.child("type").getValue().toString().equals("caroneiro_saiu")){
                                    String from_nome = ds.child("from_nome").getValue().toString();
                                    createAndOpenNotification("Caroneiro saiu", from_nome + " saiu da sua carona.", "caroneiro_saiu", "", "", "", "", "0" , keyValue);
                                }else if(ds.child("type").getValue().toString().equals("pedido_aceito")){
                                    String from_nome = ds.child("from_nome").getValue().toString();
                                    createAndOpenNotification("Pedido aceito", "Você foi aceito na carona de " + from_nome, "pedido_aceito", "", "", "", "", "0" , keyValue);
                                }else if(ds.child("type").getValue().toString().equals("pedido")){
                                    String from_nome = ds.child("from_nome").getValue().toString();
                                    createAndOpenNotification("Pedido de carona", from_nome + " te pediu uma carona.", "pedido", "", "", "", "", "2" , keyValue);
                                }
                            }
                        }
                    }
                //}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }
//8888888888888 corrigir
    private void isUsuarioAguardandoCarona(String idMotorista){
        listener = allUsuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(ds.hasChild("status")) {
                        String status = ds.child("status").getValue().toString();
                        if(status != null) {
                            if (status.contains("aguardando_")) {
                                String key = ds.getKey();
                                //if(status.contains("aguardando_" + idMotorista)) {
                                //    if(status.contains("aguardando_" + idMotorista + "_")){//se tem próximo motorista
                                //        String novoUsuarioStatus = status.replace("_" + idMotorista, "");
                                //        allUsuariosRef.child(key).child("status").setValue(novoUsuarioStatus);//.addOnCompleteListener();
                                //    }else{//se não tem próximo motorista
                                        allUsuariosRef.child(key).child("status").setValue("idle");
                                //    }
                                //}
                            }
                        }
                    }
                }
                allUsuariosRef.removeEventListener(listener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_deal_carona,menu);

        MenuItem salvar = menu.findItem(R.id.salvar_carona);
        if(!isMotoristaUsuarioCorrente)
            salvar.setVisible(false);
        else
            salvar.setVisible(true);

        //SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        return true;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        
        if(id == R.id.salvar_carona) {
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                if (caronaExiste) {
                    isSaved = true;

                    mLidarComCaronaProgress.setMessage("Salvando alterações...");
                    mLidarComCaronaProgress.show();
                    //update caroneiro 1
                    if (txCaroneiro1.getText().equals("ASSENTO VAZIO")) {
                        Map updateCarona = new HashMap();
                        updateCarona.put("nomecaroneiro1", txCaroneiro1.getText());
                        updateCarona.put("idcaroneiro1", "");
                        updateCarona.put("imgcaroneiro1", "");
                        //caronaRef.child("nomecaroneiro1").setValue(txCaroneiro1.getText());
                        //caronaRef.child("idcaroneiro1").setValue("");
                        //caronaRef.child("imgcaroneiro1").setValue("");
                        caronaRef.updateChildren(updateCarona);
                        vagasAux++;
                    } else if (txCaroneiro1.getText().equals("OCUPADO")) {
                        Map updateCarona = new HashMap();
                        updateCarona.put("nomecaroneiro1", txCaroneiro1.getText());
                        updateCarona.put("idcaroneiro1", "");
                        updateCarona.put("imgcaroneiro1", "");
                        //caronaRef.child("nomecaroneiro1").setValue(txCaroneiro1.getText());
                        //caronaRef.child("idcaroneiro1").setValue("");
                        //caronaRef.child("imgcaroneiro1").setValue("");
                        caronaRef.updateChildren(updateCarona);
                    } else {
                        Map updateCarona = new HashMap();
                        updateCarona.put("nomecaroneiro1", txCaroneiro1.getText());
                        updateCarona.put("idcaroneiro1", idCaroneiro1);
                        updateCarona.put("imgcaroneiro1", imgCaroneiro1);
                        //caronaRef.child("nomecaroneiro1").setValue(txCaroneiro1.getText());
                        //caronaRef.child("idcaroneiro1").setValue("");
                        //caronaRef.child("imgcaroneiro1").setValue("");
                        caronaRef.updateChildren(updateCarona);
                        //caronaRef.child("idcaroneiro1").setValue(idCaroneiro1);
                        //caronaRef.child("imgcaroneiro1").setValue(imgCaroneiro1);
                    }
                    //update caroneiro 2
                    if (txCaroneiro2.getText().equals("ASSENTO VAZIO")) {
                        Map updateCarona = new HashMap();
                        updateCarona.put("nomecaroneiro2", txCaroneiro2.getText());
                        updateCarona.put("idcaroneiro2", "");
                        updateCarona.put("imgcaroneiro2", "");
                        //caronaRef.child("nomecaroneiro1").setValue(txCaroneiro1.getText());
                        //caronaRef.child("idcaroneiro1").setValue("");
                        //caronaRef.child("imgcaroneiro1").setValue("");
                        caronaRef.updateChildren(updateCarona);
                        vagasAux++;
                    } else if (txCaroneiro2.getText().equals("OCUPADO")) {
                        Map updateCarona = new HashMap();
                        updateCarona.put("nomecaroneiro2", txCaroneiro2.getText());
                        updateCarona.put("idcaroneiro2", "");
                        updateCarona.put("imgcaroneiro2", "");
                        //caronaRef.child("nomecaroneiro1").setValue(txCaroneiro1.getText());
                        //caronaRef.child("idcaroneiro1").setValue("");
                        //caronaRef.child("imgcaroneiro1").setValue("");
                        caronaRef.updateChildren(updateCarona);
                    } else {
                        Map updateCarona = new HashMap();
                        updateCarona.put("nomecaroneiro2", txCaroneiro2.getText());
                        updateCarona.put("idcaroneiro2", idCaroneiro2);
                        updateCarona.put("imgcaroneiro2", imgCaroneiro2);
                        //caronaRef.child("nomecaroneiro1").setValue(txCaroneiro1.getText());
                        //caronaRef.child("idcaroneiro1").setValue("");
                        //caronaRef.child("imgcaroneiro1").setValue("");
                        caronaRef.updateChildren(updateCarona);
                        //caronaRef.child("idcaroneiro1").setValue(idCaroneiro1);
                        //caronaRef.child("imgcaroneiro1").setValue(imgCaroneiro1);
                    }
                    //update caroneiro 3
                    if (txCaroneiro3.getText().equals("ASSENTO VAZIO")) {
                        Map updateCarona = new HashMap();
                        updateCarona.put("nomecaroneiro3", txCaroneiro3.getText());
                        updateCarona.put("idcaroneiro3", "");
                        updateCarona.put("imgcaroneiro3", "");
                        //caronaRef.child("nomecaroneiro1").setValue(txCaroneiro1.getText());
                        //caronaRef.child("idcaroneiro1").setValue("");
                        //caronaRef.child("imgcaroneiro1").setValue("");
                        caronaRef.updateChildren(updateCarona);
                        vagasAux++;
                    } else if (txCaroneiro3.getText().equals("OCUPADO")) {
                        Map updateCarona = new HashMap();
                        updateCarona.put("nomecaroneiro3", txCaroneiro3.getText());
                        updateCarona.put("idcaroneiro3", "");
                        updateCarona.put("imgcaroneiro3", "");
                        //caronaRef.child("nomecaroneiro1").setValue(txCaroneiro1.getText());
                        //caronaRef.child("idcaroneiro1").setValue("");
                        //caronaRef.child("imgcaroneiro1").setValue("");
                        caronaRef.updateChildren(updateCarona);
                    } else {
                        Map updateCarona = new HashMap();
                        updateCarona.put("nomecaroneiro3", txCaroneiro3.getText());
                        updateCarona.put("idcaroneiro3", idCaroneiro3);
                        updateCarona.put("imgcaroneiro3", imgCaroneiro3);
                        //caronaRef.child("nomecaroneiro1").setValue(txCaroneiro1.getText());
                        //caronaRef.child("idcaroneiro1").setValue("");
                        //caronaRef.child("imgcaroneiro1").setValue("");
                        caronaRef.updateChildren(updateCarona);
                        //caronaRef.child("idcaroneiro1").setValue(idCaroneiro1);
                        //caronaRef.child("imgcaroneiro1").setValue(imgCaroneiro1);
                    }
                    //update caroneiro 4
                    if (txCaroneiro4.getText().equals("ASSENTO VAZIO")) {
                        Map updateCarona = new HashMap();
                        updateCarona.put("nomecaroneiro4", txCaroneiro4.getText());
                        updateCarona.put("idcaroneiro4", "");
                        updateCarona.put("imgcaroneiro4", "");
                        //caronaRef.child("nomecaroneiro1").setValue(txCaroneiro1.getText());
                        //caronaRef.child("idcaroneiro1").setValue("");
                        //caronaRef.child("imgcaroneiro1").setValue("");
                        caronaRef.updateChildren(updateCarona);
                        vagasAux++;
                    } else if (txCaroneiro4.getText().equals("OCUPADO")) {
                        Map updateCarona = new HashMap();
                        updateCarona.put("nomecaroneiro4", txCaroneiro4.getText());
                        updateCarona.put("idcaroneiro4", "");
                        updateCarona.put("imgcaroneiro4", "");
                        //caronaRef.child("nomecaroneiro1").setValue(txCaroneiro1.getText());
                        //caronaRef.child("idcaroneiro1").setValue("");
                        //caronaRef.child("imgcaroneiro1").setValue("");
                        caronaRef.updateChildren(updateCarona);
                    } else {
                        Map updateCarona = new HashMap();
                        updateCarona.put("nomecaroneiro4", txCaroneiro4.getText());
                        updateCarona.put("idcaroneiro4", idCaroneiro4);
                        updateCarona.put("imgcaroneiro4", imgCaroneiro4);
                        //caronaRef.child("nomecaroneiro1").setValue(txCaroneiro1.getText());
                        //caronaRef.child("idcaroneiro1").setValue("");
                        //caronaRef.child("imgcaroneiro1").setValue("");
                        caronaRef.updateChildren(updateCarona);
                        //caronaRef.child("idcaroneiro1").setValue(idCaroneiro1);
                        //caronaRef.child("imgcaroneiro1").setValue(imgCaroneiro1);
                    }
                    numVagas = vagasAux;

                    //allUsuariosRef.child(idCaroneiro3).child("status").setValue("idle");

                    if (numVagas <= 1) {
                        caronaRef.child("vagas").setValue(String.valueOf(numVagas) + " vaga").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                new CountDownTimer(2000, 500) {

                                    public void onTick(long millisUntilFinished) {

                                    }

                                    public void onFinish() {

                                        Toast.makeText(DealWithCarona.this, "Alterações realizadas com sucesso.", Toast.LENGTH_SHORT).show();
                                        mLidarComCaronaProgress.hide();
                                        //Intent toHome = new Intent(PedirCarona.this, HomeTabbed.class);
                                        //toHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        //startActivity(toHome);
                                        finish();

                                    }
                                }.start();
                            }
                        });
                    } else {
                        caronaRef.child("vagas").setValue(String.valueOf(numVagas) + " vagas").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                new CountDownTimer(2000, 500) {

                                    public void onTick(long millisUntilFinished) {

                                    }

                                    public void onFinish() {

                                        Toast.makeText(DealWithCarona.this, "Alterações realizadas com sucesso.", Toast.LENGTH_SHORT).show();
                                        mLidarComCaronaProgress.hide();
                                        //Intent toHome = new Intent(PedirCarona.this, HomeTabbed.class);
                                        //toHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        //startActivity(toHome);
                                        finish();

                                    }
                                }.start();
                            }
                        });
                    }
                }else{
                    Toast.makeText(this, "Não é possível salvar as alterações porque sua carona expirou.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {

                Toast.makeText(this, "Você precisa estar conectado à Internet para fazer isso.", Toast.LENGTH_SHORT).show();

            }
        }else if(id == android.R.id.home) {
            HomeTabbed.fragmentIndexToSelect = 0;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupImageLoader(){
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                DealWithCarona.this)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100*1024*1024).build();

        ImageLoader.getInstance().init(config);
    }

    private void toUsuarioPerfil(String idDoUsuario, String nomeDoUsuario, String imgDoUsuario) {
        if(idDoUsuario != null && !idDoUsuario.equals("")){
            Intent toPerfilIntent = new Intent(DealWithCarona.this, Perfil.class);
            Bundle extras = new Bundle();
            extras.putString("idusuario_chat", idDoUsuario);
            extras.putString("nomeusuario_chat", nomeDoUsuario);
            extras.putString("imgusuario_chat", imgDoUsuario);
            extras.putString("idusuario_corrente", mAuth.getCurrentUser().getUid());
            toPerfilIntent.putExtras(extras);
            startActivity(toPerfilIntent);
        }
    }

    private void checkUsuarioStatus(String nomeUsuario, String idMotorista, String idUsuarioCorrente) {
        allUsuariosRef.child(idUsuarioCorrente).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("status")) {
                    usuarioStatus = dataSnapshot.child("status").getValue().toString();
                    if (usuarioStatus != null && !usuarioStatus.equals("")) {
                        if (!usuarioStatus.equals("motorista_" + idCaronaGlobal)) {
                            if (!isSaved) {
                                getSupportActionBar().setTitle("Carona de " + nomeUsuario);
                                if (usuarioStatus.contains("caroneiro_" + idMotorista)) {
                                    pedirCancelarSairCaronaBtn.setText(getResources().getString(R.string.sair_carona));
                                    pedirCancelarSairCaronaBtn.setBackgroundColor(Color.RED);
                                    pedirCancelarSairCaronaBtn.setVisibility(View.VISIBLE);
                                } else if (usuarioStatus.contains("idle")) {
                                    pedirCancelarSairCaronaBtn.setText(getResources().getString(R.string.pedir_carona));
                                    pedirCancelarSairCaronaBtn.setBackgroundColor(getResources().getColor(R.color.green_text_caronaCAP));
                                    pedirCancelarSairCaronaBtn.setVisibility(View.VISIBLE);
                                } else if (usuarioStatus.contains("aguardando_")) {
                                        pedirCancelarSairCaronaBtn.setText(getResources().getString(R.string.cancelar_pedido));
                                        pedirCancelarSairCaronaBtn.setBackgroundColor(Color.RED);
                                        pedirCancelarSairCaronaBtn.setVisibility(View.VISIBLE);
                                } else if(usuarioStatus.contains("motorista")){//se for motorista
                                    pedirCancelarSairCaronaBtn.setVisibility(View.INVISIBLE);
                                }
                            }
                        } else {
                            getSupportActionBar().setTitle("Sua Carona");
                            pedirCancelarSairCaronaBtn.setText(getResources().getString(R.string.cancelar_carona));
                            pedirCancelarSairCaronaBtn.setBackgroundColor(Color.RED);
                            pedirCancelarSairCaronaBtn.setVisibility(View.VISIBLE);
                        }
                        if (!isAlreadyDone) {
                            pedirCancelarSairCaronaBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pedirCancelarSairCaronaListenerFunction();
                                }
                            });
                            mLidarComCaronaProgress.hide();
                            isAlreadyDone = true;
                        }
                        checkStatusTask.cancel();
                        elapsed2 = 0;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setTextoBotoes(){
        if(!isSaved) {
            if (txCaroneiro1.getText().equals("OCUPADO"))
                fecharCaroneiro1Btn.setText(R.string.liberar);
            else if (txCaroneiro1.getText().equals("ASSENTO VAZIO"))
                fecharCaroneiro1Btn.setText(R.string.reservar);
            else
                fecharCaroneiro1Btn.setText(R.string.remover);

            if (txCaroneiro2.getText().equals("OCUPADO"))
                fecharCaroneiro2Btn.setText(R.string.liberar);
            else if (txCaroneiro2.getText().equals("ASSENTO VAZIO"))
                fecharCaroneiro2Btn.setText(R.string.reservar);
            else
                fecharCaroneiro2Btn.setText(R.string.remover);

            if (txCaroneiro3.getText().equals("OCUPADO"))
                fecharCaroneiro3Btn.setText(R.string.liberar);
            else if (txCaroneiro3.getText().equals("ASSENTO VAZIO"))
                fecharCaroneiro3Btn.setText(R.string.reservar);
            else
                fecharCaroneiro3Btn.setText(R.string.remover);

            if (txCaroneiro4.getText().equals("OCUPADO"))
                fecharCaroneiro4Btn.setText(R.string.liberar);
            else if (txCaroneiro4.getText().equals("ASSENTO VAZIO"))
                fecharCaroneiro4Btn.setText(R.string.reservar);
            else
                fecharCaroneiro4Btn.setText(R.string.remover);
        }
    }

    private void setTextoCorDasLabels(){
        if(!isSaved) {
            if (txCaroneiro1.getText().equals("OCUPADO"))
                txCaroneiro1.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            else if (txCaroneiro1.getText().equals("ASSENTO VAZIO")) {
                txCaroneiro1.setTextColor(getResources().getColor(android.R.color.darker_gray));//txCaroneiro2.setTextColor(getResources().getColor(R.color.assento_vazio_color));
            } else {
                txCaroneiro1.setTextColor(getResources().getColor(android.R.color.black));
            }

            if (txCaroneiro2.getText().equals("OCUPADO"))
                txCaroneiro2.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            else if (txCaroneiro2.getText().equals("ASSENTO VAZIO")) {
                txCaroneiro2.setTextColor(getResources().getColor(android.R.color.darker_gray));//txCaroneiro2.setTextColor(getResources().getColor(R.color.assento_vazio_color));
            } else {
                txCaroneiro2.setTextColor(getResources().getColor(android.R.color.black));
            }

            if (txCaroneiro3.getText().equals("OCUPADO")) {
                txCaroneiro3.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            } else if (txCaroneiro3.getText().equals("ASSENTO VAZIO")) {
                txCaroneiro3.setTextColor(getResources().getColor(android.R.color.darker_gray));//txCaroneiro2.setTextColor(getResources().getColor(R.color.assento_vazio_color));
            } else {
                txCaroneiro3.setTextColor(getResources().getColor(android.R.color.black));
            }

            if (txCaroneiro4.getText().equals("OCUPADO")) {
                txCaroneiro4.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            } else if (txCaroneiro4.getText().equals("ASSENTO VAZIO")) {
                txCaroneiro4.setTextColor(getResources().getColor(android.R.color.darker_gray));//txCaroneiro2.setTextColor(getResources().getColor(R.color.assento_vazio_color));
            } else {
                txCaroneiro4.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseUser = mAuth.getCurrentUser();

        /*if(mFirebaseUser != null) {
            allUsuariosRef.child(mFirebaseUser.getUid()).child("online").setValue(true);
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        //onScreen = false;
        if(mAuth.getCurrentUser() != null)
            allUsuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(false);

        caronaRef.goOffline();
        notificationsOffRef.goOffline();
        usuarioRef.goOffline();
        allUsuariosRef.goOffline();
        notificationsRef.goOffline();

        mLidarComCaronaProgress.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        //onScreen = true;
        caronaRef.goOnline();
        notificationsOffRef.goOnline();
        usuarioRef.goOnline();
        allUsuariosRef.goOnline();
        notificationsRef.goOnline();
        if(mFirebaseUser != null) {
            allUsuariosRef.child(mFirebaseUser.getUid()).child("online").setValue(true);
            allUsuariosRef.child(mFirebaseUser.getUid()).child("conversando_com").setValue("tela_carona");
        }
    }

    @Override
    public void onDestroy(){
        mLidarComCaronaProgress.dismiss();
        if(alert != null) {
            alert.dismiss();
        }
        caronaRef.removeEventListener(caronaListener);
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        /*super.onBackPressed()*/
        HomeTabbed.fragmentIndexToSelect = 0;
        super.onBackPressed();
    }

    /*private void createAndOpenNotification(String title, String text, String type, String id, String nome, String imgUrl, String telefone, String tab, String keyValue){
        notificationsOffRef.child(mAuth.getCurrentUser().getUid()).child(keyValue).removeValue();
        isNotificationAlreadySent = true;
        HomeTabbed.mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE);
                //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        //if(type.equals("msg")) {
        HomeTabbed.resultIntent = new Intent("caronacap.apps.dragoonssoft.com.caronacap_TARGET_NOTIFICATION");
        HomeTabbed.resultIntent.putExtra(tab, "tab");
        //HomeTabbed.resultIntent.putExtra(nome, "nomeusuario_chat");
        //HomeTabbed.resultIntent.putExtra(imgUrl, "imgusuario_chat");
        //HomeTabbed.resultIntent.putExtra(telefone, "telefoneusuario_chat");
        //}

        HomeTabbed.resultPendingIntent =
                PendingIntent.getActivity(this, 0, HomeTabbed.resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        HomeTabbed.mBuilder.setContentIntent(HomeTabbed.resultPendingIntent);


        int mNotificationId = (int)System.currentTimeMillis();
        HomeTabbed.mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        HomeTabbed.mNotifyMgr.notify(mNotificationId, HomeTabbed.mBuilder.build());


    }*/

    private void pedirCancelarSairCaronaListenerFunction(){
        final String usuarioID = mAuth.getCurrentUser().getUid();
        if (pedirCancelarSairCaronaBtn.getText().equals(getResources().getString(R.string.sair_carona))) {
            alert_builder = new AlertDialog.Builder(DealWithCarona.this);
            alert_builder.setMessage("Deseja realmente sair desta carona?").setCancelable(false);
            alert_builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                        mLidarComCaronaProgress.setMessage("Saindo da carona...");
                        mLidarComCaronaProgress.show();
                        
                        Map updateCaroneiros = new HashMap();

                        if (idCaroneiro1.equals(userId)) {
                            updateCaroneiros.put("idcaroneiro1", "");
                            updateCaroneiros.put("nomecaroneiro1", "ASSENTO VAZIO");
                            updateCaroneiros.put("imgcaroneiro1", "");
                            numVagas++;
                            //caronaRef.child("idcaroneiro1").setValue("");
                            //caronaRef.child("nomecaroneiro1").setValue("ASSENTO VAZIO");
                            //caronaRef.child("imgcaroneiro1").setValue("");

                        } else if (idCaroneiro2.equals(userId)) {
                            updateCaroneiros.put("idcaroneiro2", "");
                            updateCaroneiros.put("nomecaroneiro2", "ASSENTO VAZIO");
                            updateCaroneiros.put("imgcaroneiro2", "");
                            numVagas++;
                            //caronaRef.child("idcaroneiro2").setValue("");
                            //caronaRef.child("nomecaroneiro2").setValue("ASSENTO VAZIO");
                            //caronaRef.child("imgcaroneiro2").setValue("");

                        } else if (idCaroneiro3.equals(userId)) {
                            updateCaroneiros.put("idcaroneiro3", "");
                            updateCaroneiros.put("nomecaroneiro3", "ASSENTO VAZIO");
                            updateCaroneiros.put("imgcaroneiro3", "");
                            numVagas++;
                            //caronaRef.child("idcaroneiro3").setValue("");
                            //caronaRef.child("nomecaroneiro3").setValue("ASSENTO VAZIO");
                            //caronaRef.child("imgcaroneiro3").setValue("");

                        } else if (idCaroneiro4.equals(userId)) {
                            updateCaroneiros.put("idcaroneiro4", "");
                            updateCaroneiros.put("nomecaroneiro4", "ASSENTO VAZIO");
                            updateCaroneiros.put("imgcaroneiro4", "");
                            numVagas++;
                            //caronaRef.child("idcaroneiro4").setValue("");
                            //caronaRef.child("nomecaroneiro4").setValue("ASSENTO VAZIO");
                            //caronaRef.child("imgcaroneiro4").setValue("");

                        }
                        //verificação da quantidade de vagas
                        if (numVagas <= 1) {
                            updateCaroneiros.put("vagas", String.valueOf(numVagas) + " vaga");
                            //caronaRef.child("vagas").setValue(String.valueOf(numVagas) + " vaga");
                        } else {
                            updateCaroneiros.put("vagas", String.valueOf(numVagas) + " vagas");
                            //caronaRef.child("vagas").setValue(String.valueOf(numVagas) + " vagas");
                        }

                        HashMap<String, String> notificationInfo = new HashMap<>();
                        notificationInfo.put("from", mAuth.getCurrentUser().getUid());
                        notificationInfo.put("type", "caroneiro_saiu");
                        caronaRef.updateChildren(updateCaroneiros).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    new CountDownTimer(2000, 1000) {

                                        public void onTick(long millisUntilFinished) {

                                        }

                                        public void onFinish() {
                                            usuarioRef.child("status").setValue("idle");
                                            notificationsRef.child(idMotorista).push().setValue(notificationInfo);
                                            Toast.makeText(DealWithCarona.this, "Você saiu da carona de " + nomeUsuario, Toast.LENGTH_SHORT).show();
                                            ListaCaronasFragmento.criarCaronaBtn.setText(R.string.criar_carona);
                                            ListaCaronasFragmento.idParaVerCarona = null;

                                            //if(mLidarComCaronaProgress != null) {
                                            mLidarComCaronaProgress.hide();
                                            finish();
                                        }
                                    }.start();

                                } else {
                                    mLidarComCaronaProgress.hide();
                                    Toast.makeText(DealWithCarona.this, "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }else{
                        Toast.makeText(DealWithCarona.this, "Você precisa estar conectado à Internet para fazer isso.", Toast.LENGTH_SHORT).show();
                        alert.hide();
                    }
                }
            }).setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alert.hide();
                }
            });
            alert = alert_builder.create();
            alert.show();

        } else if (pedirCancelarSairCaronaBtn.getText().equals(getResources().getString(R.string.cancelar_carona))) {
            alert_builder = new AlertDialog.Builder(DealWithCarona.this);
            alert_builder.setMessage("Deseja realmente cancelar sua carona?").setCancelable(false);
            alert_builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    networkInfo = connMgr.getActiveNetworkInfo();
                    if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                        //mLidarComCaronaProgress.show();

                        mLidarComCaronaProgress.setMessage("Cancelando sua carona...");
                        mLidarComCaronaProgress.show();

                        notificationsOffRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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

                        allUsuariosRefListener = allUsuariosRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (idCarona.equals(ListaCaronasFragmento.idParaVerCarona)) {
                                    String currentUserId = mAuth.getCurrentUser().getUid();
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        if (ds.exists()) {
                                            if(ds.hasChild("status")) {
                                                String status = ds.child("status").getValue().toString();
                                                if(status.contains("aguardando_")) {
                                                    String keyValue = ds.getKey();
                                                    if (status.contains("aguardando_" + currentUserId)) {
                                                        allUsuariosRef.child(keyValue).child("status").setValue("idle").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (!task.isSuccessful()) {
                                                                    allUsuariosRef.removeEventListener(allUsuariosRefListener);
                                                                    mLidarComCaronaProgress.hide();
                                                                    Toast.makeText(DealWithCarona.this, "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                        /*if (status.contains("aguardando_" + currentUserId + "_")) { //se tem próximo motorista
                                                            String novoUsuarioStatus = status.replace("_" + currentUserId, "");
                                                            allUsuariosRef.child(keyValue).child("status").setValue(novoUsuarioStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (!task.isSuccessful()) {
                                                                        allUsuariosRef.removeEventListener(allUsuariosRefListener);
                                                                        mLidarComCaronaProgress.hide();
                                                                        Toast.makeText(DealWithCarona.this, "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            allUsuariosRef.child(keyValue).child("status").setValue("idle").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (!task.isSuccessful()) {
                                                                        allUsuariosRef.removeEventListener(allUsuariosRefListener);
                                                                        mLidarComCaronaProgress.hide();
                                                                        Toast.makeText(DealWithCarona.this, "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        }*/
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    //new CountDownTimer(2000, 500) {

                                        //public void onTick(long millisUntilFinished) {

                                        //}

                                        //public void onFinish() {
                                    usuarioRef.child("status").setValue("idle");
                                    if (idCaroneiro1 != null && !idCaroneiro1.equals("")) {

                                        allUsuariosRef.child(idCaroneiro1).child("status").setValue("idle");
                                        //enviar notificação ao usuário
                                        HashMap<String, String> notificationInfo1 = new HashMap<>();
                                        notificationInfo1.put("from", mAuth.getCurrentUser().getUid());
                                        notificationInfo1.put("type", "carona_cancelada");
                                        notificationsRef.child(idCaroneiro1).push().setValue(notificationInfo1);

                                    }
                                    if (idCaroneiro2 != null && !idCaroneiro2.equals("")) {

                                        allUsuariosRef.child(idCaroneiro2).child("status").setValue("idle");
                                        //enviar notificação ao usuário
                                        HashMap<String, String> notificationInfo2 = new HashMap<>();
                                        notificationInfo2.put("from", mAuth.getCurrentUser().getUid());
                                        notificationInfo2.put("type", "carona_cancelada");
                                        notificationsRef.child(idCaroneiro2).push().setValue(notificationInfo2);

                                    }
                                    if (idCaroneiro3 != null && !idCaroneiro3.equals("")) {

                                        allUsuariosRef.child(idCaroneiro3).child("status").setValue("idle");
                                        //enviar notificação ao usuário
                                        HashMap<String, String> notificationInfo3 = new HashMap<>();
                                        notificationInfo3.put("from", mAuth.getCurrentUser().getUid());
                                        notificationInfo3.put("type", "carona_cancelada");
                                        notificationsRef.child(idCaroneiro3).push().setValue(notificationInfo3);

                                    }
                                    if (idCaroneiro4 != null && !idCaroneiro4.equals("")) {

                                        allUsuariosRef.child(idCaroneiro4).child("status").setValue("idle");
                                        //enviar notificação ao usuário
                                        HashMap<String, String> notificationInfo4 = new HashMap<>();
                                        notificationInfo4.put("from", mAuth.getCurrentUser().getUid());
                                        notificationInfo4.put("type", "carona_cancelada");
                                        notificationsRef.child(idCaroneiro4).push().setValue(notificationInfo4);

                                    }
                                            caronaRef.removeValue();
                                            idCaronaGlobal = null;
                                            ListaCaronasFragmento.idParaVerCarona = null;
                                            ListaCaronasFragmento.criarCaronaBtn.setText(getResources().getString(R.string.criar_carona));
                                            mLidarComCaronaProgress.hide();
                                            if(!toastJaMostrado) {
                                                Toast.makeText(DealWithCarona.this, "Carona cancelada", Toast.LENGTH_SHORT).show();
                                                toastJaMostrado = true;
                                            }
                                            allUsuariosRef.removeEventListener(allUsuariosRefListener);
                                            finish();

                                        //}
                                    //}.start();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }else{
                        Toast.makeText(DealWithCarona.this, "Você precisa estar conectado à Internet para fazer isso.", Toast.LENGTH_SHORT).show();
                        alert.hide();
                    }
                }
            }).setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alert.hide();
                }
            });
            alert = alert_builder.create();
            alert.show();

        } else if (pedirCancelarSairCaronaBtn.getText().equals(getResources().getString(R.string.pedir_carona))) {
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                mLidarComCaronaProgress.setMessage("Enviando pedido de carona...");
                mLidarComCaronaProgress.show();

                task=new TimerTask(){
                    @Override
                    public void run() {
                        elapsed+=INTERVAL;
                        if(elapsed>=TIMEOUT){
                            this.cancel();
                            elapsed = 0;
                            mLidarComCaronaProgress.hide();
                            Toast.makeText(DealWithCarona.this, "Pode ser que sua conexão com a Internet tenha expirado. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                            return;
                        }else{
                            executarPedido();
                        }
                    }
                };
                timer = new Timer();
                timer.scheduleAtFixedRate(task, INTERVAL, TIMEOUT);
            }else{
                Toast.makeText(DealWithCarona.this, "Você precisa estar conectado à Internet para fazer isso.", Toast.LENGTH_SHORT).show();
            }
        } else if (pedirCancelarSairCaronaBtn.getText().equals(getResources().getString(R.string.cancelar_pedido))) { //cancelar pedido de carona
            alert_builder = new AlertDialog.Builder(DealWithCarona.this);
            alert_builder.setMessage("Deseja realmente cancelar seu pedido de carona?").setCancelable(false);
            alert_builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                        //usuarioRef.child("status").setValue("aguardando_" + idMotorista);
                        mLidarComCaronaProgress.setMessage("Cancelando seu pedido de carona...");
                        mLidarComCaronaProgress.show();
                        //MELHORAR A REMOÇÃO NA PRÓXIMA VERSÃO => notificationsOffRef.child(idMotorista).orderByChild("from").equalTo(mAuth.getCurrentUser().getUid()).getRef().removeValue();
                        paraCancelarPedidoListener = notificationsOffRef.child(idMotorista).orderByChild("from").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        String userID = mAuth.getCurrentUser().getUid();
                                        if (ds.child("type").getValue().toString().equals("pedido") && ds.child("from").getValue().toString().equals(userID)) {
                                            String keyValue = ds.getKey();
                                            notificationsOffRef.child(idMotorista).child(keyValue).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        checkIfCancelarPedidoIsDone(keyValue);
                                                        allUsuariosRef.child(userID).child("status").setValue("idle");//.addOnCompleteListener();
                                                        /*if(usuarioStatus.contains(idMotorista)){
                                                            String novoUsuarioStatus = usuarioStatus.replace("_" + idMotorista, "");
                                                            if(!novoUsuarioStatus.contains("_"))
                                                                allUsuariosRef.child(userID).child("status").setValue("idle");//.addOnCompleteListener();
                                                            else
                                                                allUsuariosRef.child(userID).child("status").setValue(novoUsuarioStatus);//.addOnCompleteListener();

                                                        }*/
                                                    }
                                                        /*mLidarComCaronaProgress.hide();
                                                        allUsuariosRef.child(mAuth.getCurrentUser().getUid()).child("status").setValue("idle");
                                                        ListaCaronasFragmento.criarCaronaBtn.setText("CRIAR CARONA");
                                                        notificationsOffRef.child(idMotorista).orderByChild("from").removeEventListener(paraCancelarPedidoListener);
                                                        Toast.makeText(DealWithCarona.this, "Pedido cancelado", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }else{
                                                        mLidarComCaronaProgress.hide();
                                                        notificationsOffRef.child(idMotorista).child(keyValue).removeValue();
                                                        allUsuariosRef.child(mAuth.getCurrentUser().getUid()).child("status").setValue("idle");
                                                        ListaCaronasFragmento.criarCaronaBtn.setText("CRIAR CARONA");
                                                        notificationsOffRef.child(idMotorista).orderByChild("from").removeEventListener(paraCancelarPedidoListener);
                                                        //Toast.makeText(DealWithCarona.this, "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }*/
                                                }
                                            });
                                        }else{
                                            mLidarComCaronaProgress.hide();
                                            notificationsOffRef.child(idMotorista).orderByChild("from").removeEventListener(paraCancelarPedidoListener);
                                            //Toast.makeText(DealWithCarona.this, "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }else{
                        Toast.makeText(DealWithCarona.this, "Você precisa estar conectado à Internet para fazer isso.", Toast.LENGTH_SHORT).show();
                        alert.hide();
                    }

                }
            }).setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alert.hide();
                }
            });
            alert = alert_builder.create();
            alert.show();
            //caronaRef.child("pedidos").child(userId).removeValue();

            //allUsuariosRef.child(idMotorista).child("pedidos").child(userId).removeValue();

            //mLidarComCaronaProgress.hide();

        }
    }

    private void executarPedido() {
        allUsuariosRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                telefone = dataSnapshot.child("telefone").getValue().toString();
                imgURL = dataSnapshot.child("imgURL").getValue().toString();
                if (telefone != null && imgURL != null) {
                    task.cancel();
                    elapsed = 0;
                    pedidoJaEnviado = true;
                    Map notificationMap = new HashMap();
                    String pushKey = notificationsOffRef.child(idMotorista).push().getKey();
                    notificationMap.put("from", mAuth.getCurrentUser().getUid());
                    notificationMap.put("type", "pedido");
                    //notificationMap.put("idcarona", idCaronaGlobal);
                    notificationMap.put("horario", ServerValue.TIMESTAMP);
                    notificationMap.put("nome", mAuth.getCurrentUser().getDisplayName());
                    notificationMap.put("img", imgURL);
                    notificationMap.put("telefone", telefone);
                    notificationsOffRef.child(idMotorista).child(pushKey).setValue(notificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                String userID = mAuth.getCurrentUser().getUid();
                                Map notificationMap2 = new HashMap();
                                //String pushKey2 = notificationsRef.child(idMotorista).push().getKey();
                                notificationMap2.put("from", mAuth.getCurrentUser().getUid());
                                notificationMap2.put("type", "pedido");
                                notificationMap2.put("idcarona", idCaronaGlobal);
                                notificationMap2.put("horario", ServerValue.TIMESTAMP);
                                notificationsRef.child(idMotorista).child(pushKey).setValue(notificationMap2);
                                //if(usuarioStatus.startsWith("aguardando"))//usuário já está em uma carona
                                //    allUsuariosRef.child(userID).child("status").setValue(usuarioStatus + "_" + idMotorista);
                                //else//usuário não está em nenhuma carona
                                    allUsuariosRef.child(userID).child("status").setValue("aguardando_" + idMotorista);
                                pedirCancelarSairCaronaBtn.setText(getResources().getString(R.string.cancelar_pedido));
                                pedirCancelarSairCaronaBtn.setBackgroundColor(Color.RED);
                                ListaCaronasFragmento.criarCaronaBtn.setText(R.string.ver_carona);
                                mLidarComCaronaProgress.hide();
                                Toast.makeText(DealWithCarona.this, "Pedido enviado para " + nomeUsuario, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkUsuarioStatusComTimerTask(String nomeUsuario, String idMotorista, String idUsuarioCorrente){
        checkStatusTask=new TimerTask(){
            @Override
            public void run() {
                elapsed2+=INTERVAL;
                if(elapsed2>=TIMEOUT){
                    this.cancel();
                    elapsed2 = 0;
                    mLidarComCaronaProgress.hide();
                    Toast.makeText(DealWithCarona.this, "Pode ser que sua conexão com a Internet tenha expirado. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    checkUsuarioStatus(nomeUsuario, idMotorista, idUsuarioCorrente);
                }
            }
        };
        checkStatusTimer = new Timer();
        checkStatusTimer.scheduleAtFixedRate(checkStatusTask, INTERVAL, TIMEOUT);
    }

    private void checkIfCancelarPedidoIsDone(String keyValue){

        checkIfIsDoneSettingValueTimer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                notificationsOffRef.child(idMotorista).orderByKey().equalTo(keyValue).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!pedidoJaCancelado) {
                            if (!dataSnapshot.exists()) {
                                checkIfIsDoneSettingValueTimer.cancel();
                                pedidoJaCancelado = true;
                                pedidoJaEnviado = false;
                                mLidarComCaronaProgress.hide();
                                allUsuariosRef.child(mAuth.getCurrentUser().getUid()).child("status").setValue("idle");
                                ListaCaronasFragmento.criarCaronaBtn.setText(getResources().getString(R.string.criar_carona));
                                notificationsOffRef.child(idMotorista).orderByChild("from").removeEventListener(paraCancelarPedidoListener);
                                Toast.makeText(DealWithCarona.this, "Pedido cancelado", Toast.LENGTH_SHORT).show();
                                ListaCaronasFragmento.idParaVerCarona = null;
                                finish();
                            } else {
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }
}
