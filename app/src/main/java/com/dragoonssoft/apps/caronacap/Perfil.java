package com.dragoonssoft.apps.caronacap;

import android.*;
import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.VisibilityPropagation;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;
import static androidx.core.app.NotificationCompat.DEFAULT_VIBRATE;

public class Perfil extends AppCompatActivity {
    private Toolbar mPerfilToolbar;
    private DatabaseReference usuarioRef;
    private String idUsuario, telefoneUsuario, idUsuarioCorrente;
    private ImageView fotoUsuario;
    private TextView nomeUsuario, registradoEm;
    //criar um badge para conter os pontos de moral do usuário
    private Button fazerLigacao, enviarMsg;
    private DatabaseReference conversasRef;
    private DatabaseReference usuariosRef;
    private FirebaseAuth mAuth;
    private String fotoUrl, nome;
    private String idOutroUsuario, nomeOutroUsuario, imgUrlOutroUsuario,telefoneOutroUsuario;
    private Button mudarNum;
    static TextView telefonePerfilText;
    private TextView receberLigacoesText;
    private Switch permitirSwitch;
    private String recebeLigacoes;
    private boolean notShown = false;
    private boolean onScreen = true;
    private DatabaseReference notificationsOffRef;
    private AlertDialog.Builder alert_builder;
    private AlertDialog alert, alert2;
    static String telefoneGlobalParaMudarNum;
    private boolean isNotificationAlreadySent = false;
    private String veioDeDealWithCarona;
    private CountDownTimer perfilTimer;
    private boolean telefoneJaCarregado = false;
    private ProgressDialog mCarregarDados;
    private RatingBar mRatingBar, mMyRatingBar;
    private TextView mReputacao, mQtdAvaliacoes, mAvaliadoPorMim;
    private float reputacao;
    private int qtdAvaliacoes;
    private DatabaseReference ratingsRef;
    private AlertDialog.Builder alert_builder2;
    private float rate;
    private Calendar dateTime = Calendar.getInstance();
    private View separator1, separator2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        mPerfilToolbar = (Toolbar) findViewById(R.id.perfil_toolbar);
        setSupportActionBar(mPerfilToolbar);
        getSupportActionBar().setTitle("Nome do Usuário");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        idUsuarioCorrente = getIntent().getStringExtra("idusuario_corrente");
        idOutroUsuario = getIntent().getStringExtra("idusuario_chat");
        nomeOutroUsuario = getIntent().getStringExtra("nomeusuario_chat");
        imgUrlOutroUsuario = getIntent().getStringExtra("imgusuario_chat");
        telefoneOutroUsuario = getIntent().getStringExtra("telefoneusuario_chat");
        veioDeDealWithCarona = getIntent().getStringExtra("dealwithcarona");
        
        mAuth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference().child("usuarios");
        //usuarioRef = usuariosRef.child(idOutroUsuario);
        conversasRef = FirebaseDatabase.getInstance().getReference().child("conversas").child(mAuth.getCurrentUser().getUid());
        notificationsOffRef = FirebaseDatabase.getInstance().getReference().child("notifications_off");
        //ratingsRef = FirebaseDatabase.getInstance().getReference().child("ratings");

        if(idUsuarioCorrente == null)
            idUsuarioCorrente = mAuth.getCurrentUser().getUid();

        //usuarioRef.keepSynced(true);

        fotoUsuario = (ImageView) findViewById(R.id.profileImagePerfil);
        nomeUsuario = (TextView) findViewById(R.id.nomePerfil);
        //mReputacao = findViewById(R.id.reputacao);
        //mRatingBar = findViewById(R.id.ratingBar);
        //mQtdAvaliacoes = findViewById(R.id.qtdAvaliacoes);
        //mMyRatingBar = findViewById(R.id.myRatingBar);
        //mAvaliadoPorMim = findViewById(R.id.avaliadoPorMim);
        //registradoEm = (TextView) findViewById(R.id.registradoPerfil);
        separator1 = findViewById(R.id.separator1);
        fazerLigacao = (Button) findViewById(R.id.fazerLigacaoPerfilBtn);
        enviarMsg = (Button) findViewById(R.id.enviarMsgPerfilBtn);
        separator2 = findViewById(R.id.separator2);
        mudarNum = (Button)findViewById(R.id.mudarNumBtn);
        telefonePerfilText = (TextView)findViewById(R.id.telefonePerfilText);
        receberLigacoesText = (TextView)findViewById(R.id.receberLigacoesText);
        permitirSwitch = (Switch)findViewById(R.id.permitirSwitch);

        mCarregarDados = new ProgressDialog(this);
        mCarregarDados.setCancelable(false);
        mCarregarDados.setMessage("Carregando dados do usuário...");

        alert_builder = new AlertDialog.Builder(Perfil.this);
        alert_builder2 = new AlertDialog.Builder(Perfil.this);

        setupImageLoader();
        ImageLoader imageLoader = ImageLoader.getInstance();
        int defaultImage = getResources().getIdentifier("@drawable/caronacap_512", null, getPackageName());
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        mCarregarDados.show();

        perfilTimer = new CountDownTimer(20000, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(!telefoneJaCarregado) {
                    usuariosRef.child(idOutroUsuario).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            telefoneOutroUsuario = dataSnapshot.child("telefone").getValue().toString();
                            if(telefoneOutroUsuario != null) {
                                telefoneJaCarregado = true;
                                perfilTimer.cancel();
                                mCarregarDados.hide();
                                if (idOutroUsuario == null) {
                                    idOutroUsuario = dataSnapshot.getKey();
                                }
                                if (nomeOutroUsuario == null) {
                                    nome = dataSnapshot.child("nome").getValue().toString();
                                    nomeOutroUsuario = nome;
                                }
                                if (imgUrlOutroUsuario == null) {
                                    fotoUrl = dataSnapshot.child("imgURL").getValue().toString();
                                    imgUrlOutroUsuario = fotoUrl;
                                }
                                recebeLigacoes = dataSnapshot.child("recebe_ligacoes").getValue().toString();

                                /*if(dataSnapshot.hasChild("reputacao")){
                                    reputacao = Float.parseFloat(String.valueOf(dataSnapshot.child("reputacao").getValue()));
                                    mReputacao.setText(String.valueOf(reputacao).replace(".",","));
                                    mRatingBar.setRating(reputacao);
                                }else{
                                    mReputacao.setText(String.valueOf(Float.parseFloat("0")).replace(".", ","));
                                    usuariosRef.child(idOutroUsuario).child("reputacao").setValue(0);
                                    mRatingBar.setRating(0);
                                }

                                String qtdAvaliacoesText;
                                if(dataSnapshot.hasChild("qtd_avaliacoes")){
                                    qtdAvaliacoes = Integer.parseInt(String.valueOf(dataSnapshot.child("qtd_avaliacoes").getValue()));
                                    if(qtdAvaliacoes < 1) {
                                        qtdAvaliacoesText = "Nenhuma avaliação ainda";
                                        mQtdAvaliacoes.setText(qtdAvaliacoesText);
                                    }else if(qtdAvaliacoes == 1) {
                                        qtdAvaliacoesText = qtdAvaliacoes + " usuário avaliou";
                                        mQtdAvaliacoes.setText(qtdAvaliacoesText);
                                    }else{
                                        qtdAvaliacoesText = qtdAvaliacoes + " usuários avaliaram";
                                        mQtdAvaliacoes.setText(qtdAvaliacoesText);
                                    }
                                }else{
                                    qtdAvaliacoesText = "Nenhuma avaliação ainda";
                                    mQtdAvaliacoes.setText(qtdAvaliacoesText);
                                    usuariosRef.child(idOutroUsuario).child("qtd_avaliacoes").setValue(0);
                                    qtdAvaliacoes = 0;
                                }*/

                                if (idOutroUsuario.equals(idUsuarioCorrente)) {
                                    fazerLigacao.setVisibility(View.GONE);
                                    enviarMsg.setVisibility(View.GONE);
                                    permitirSwitch.setVisibility(View.VISIBLE);
                                    receberLigacoesText.setVisibility(View.VISIBLE);
                                    telefonePerfilText.setVisibility(View.VISIBLE);
                                    mudarNum.setVisibility(View.VISIBLE);
                                    getSupportActionBar().setTitle("Seu perfil");
                                    separator1.setVisibility(View.VISIBLE);
                                    //mRatingBar.setIsIndicator(true);
                                    //mRatingBar.setVisibility(View.VISIBLE);
                                    //mQtdAvaliacoes.setVisibility(View.VISIBLE);
                                } else {
                                    getSupportActionBar().setTitle(nomeOutroUsuario);
                                    if (!idOutroUsuario.equals("Gqwzf627C0acZe6G48Ynqhtn7003")) {
                                        if (recebeLigacoes.equals("true")) {
                                            fazerLigacao.setVisibility(View.VISIBLE);
                                        } else {
                                            fazerLigacao.setVisibility(View.GONE);
                                        }
                                    } else {
                                        fazerLigacao.setVisibility(View.GONE);
                                    }
                                    enviarMsg.setVisibility(View.VISIBLE);
                                    permitirSwitch.setVisibility(View.GONE);
                                    receberLigacoesText.setVisibility(View.GONE);
                                    telefonePerfilText.setVisibility(View.GONE);
                                    mudarNum.setVisibility(View.GONE);
                                    separator1.setVisibility(View.VISIBLE);
                                    /*if(idUsuarioCorrente == null)
                                        idUsuarioCorrente = mAuth.getCurrentUser().getUid();
                                    ratingsRef.child(idOutroUsuario).child(idUsuarioCorrente).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                float myRate = Float.parseFloat(String.valueOf(dataSnapshot.child("rate").getValue()));
                                                String myRateDate = dataSnapshot.child("date").getValue().toString();
                                                mMyRatingBar.setRating(myRate);
                                                String avaliadoPorMimText = "Avaliado por você em " + myRateDate;
                                                mAvaliadoPorMim.setText(avaliadoPorMimText);
                                                mRatingBar.setIsIndicator(true);
                                                mMyRatingBar.setIsIndicator(true);
                                                mMyRatingBar.setVisibility(View.VISIBLE);
                                                mAvaliadoPorMim.setVisibility(View.VISIBLE);
                                            }else{
                                                mRatingBar.setIsIndicator(false);
                                                mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener(){

                                                    @Override
                                                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean b) {
                                                        rate = ratingBar.getRating();
                                                        alert_builder2.setTitle("Avaliar usuário")
                                                                .setMessage("Avaliar " + nomeOutroUsuario + " com a nota " + String.valueOf(rate) + "?")
                                                                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                                                                        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                                                                        if(networkInfo != null && networkInfo.isConnected()) {
                                                                            mCarregarDados.setMessage("Salvando avaliação...");
                                                                            mCarregarDados.show();

                                                                            String rateDateText = corrigirDiaDoMes(dateTime.get(Calendar.DAY_OF_MONTH)) + "/" + getMonthName() + "/" + dateTime.get(Calendar.YEAR);
                                                                            Map ratingMap = new HashMap();
                                                                            ratingMap.put(idOutroUsuario + "/" + idUsuarioCorrente + "/rate", rate);
                                                                            ratingMap.put(idOutroUsuario + "/" + idUsuarioCorrente + "/date", rateDateText);
                                                                            ratingsRef.updateChildren(ratingMap);

                                                                            reputacao+=rate;
                                                                            qtdAvaliacoes++;

                                                                            float reputacaoToServer = reputacao/qtdAvaliacoes;

                                                                            Map usuarioMap = new HashMap();
                                                                            usuarioMap.put(idOutroUsuario + "/reputacao", reputacaoToServer);
                                                                            usuarioMap.put(idOutroUsuario + "/qtd_avaliacoes", qtdAvaliacoes);
                                                                            usuariosRef.updateChildren(usuarioMap).addOnCompleteListener(new OnCompleteListener() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task task) {
                                                                                    String mAvaliadoPorMimText = "Avaliado por você em " + rateDateText;
                                                                                    mAvaliadoPorMim.setText(mAvaliadoPorMimText);
                                                                                    if(qtdAvaliacoes == 1)
                                                                                        mQtdAvaliacoes.setText(qtdAvaliacoes + " usuário avaliou");
                                                                                    else
                                                                                        mQtdAvaliacoes.setText(qtdAvaliacoes + " usuários avaliaram");

                                                                                    mRatingBar.setIsIndicator(true);
                                                                                    mMyRatingBar.setIsIndicator(true);
                                                                                    mMyRatingBar.setRating(rate);
                                                                                    mAvaliadoPorMim.setVisibility(View.VISIBLE);
                                                                                    mMyRatingBar.setVisibility(View.VISIBLE);
                                                                                    mReputacao.setText(String.valueOf(reputacaoToServer).replace(".",","));

                                                                                    mCarregarDados.hide();
                                                                                }
                                                                            });
                                                                        }else{
                                                                            Toast.makeText(Perfil.this, "Conecte-se com a Internet para avaliar o usuário.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                mRatingBar.setRating(0);
                                                                alert2.hide();
                                                            }
                                                        });
                                                        alert2 = alert_builder2.create();
                                                        alert2.show();
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });*/
                                }
                                nomeUsuario.setText(nomeOutroUsuario);
                                imageLoader.displayImage(imgUrlOutroUsuario, fotoUsuario, options);
                                telefonePerfilText.setText("Telefone: " + telefoneOutroUsuario);
                            }else{
                                usuariosRef.child(idOutroUsuario).child("telefone").setValue("0");
                            }
                            fazerLigacao.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alert_builder.setMessage("Esta ação pode lhe custar dinheiro, uma vez que utiliza créditos que você tem com sua operadora celular.")
                                            .setPositiveButton("CONTINUAR", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //fazer ligação
                                                    Intent ligacao = new Intent(Intent.ACTION_CALL);
                                                    ligacao.setData(Uri.parse("tel:" + telefoneOutroUsuario));
                                                    if (ActivityCompat.checkSelfPermission(Perfil.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                                        // TODO: Consider calling
                                                        //    ActivityCompat#requestPermissions
                                                        // here to request the missing permissions, and then overriding
                                                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                        //                                          int[] grantResults)
                                                        // to handle the case where the user grants the permission. See the documentation
                                                        // for ActivityCompat#requestPermissions for more details.
                                                        ActivityCompat.requestPermissions(Perfil.this, new String[]{Manifest.permission.CALL_PHONE}, 1);


                                                        return;
                                                    } else {
                                                        startActivity(ligacao);
                                                    }
                                                }
                                            })
                                            .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    alert.hide();
                                                }
                                            });
                                    alert = alert_builder.create();
                                    alert.setTitle("Ligação");
                                    alert.show();

                                }
                            });

                            enviarMsg.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //abrindo chat
                                    //ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                    //NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                                    //if (networkInfo != null && networkInfo.isConnected()) {
                                    Intent abrirConversa = new Intent(Perfil.this, Conversa.class);
                                    abrirConversa.putExtra("idusuario_chat", idOutroUsuario);
                                    abrirConversa.putExtra("nomeusuario_chat", nomeOutroUsuario);
                                    abrirConversa.putExtra("imgusuario_chat", imgUrlOutroUsuario);
                                    abrirConversa.putExtra("telefoneusuario_chat", telefoneOutroUsuario);
                                    startActivity(abrirConversa);
                                    //}else{
                                    //   Toast.makeText(Perfil.this, "Sem conexão com a Internet.", Toast.LENGTH_SHORT).show();
                                    //}
                                }
                            });

                            permitirSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if(isChecked){
                                        if(idUsuarioCorrente != null)
                                            usuariosRef.child(idUsuarioCorrente).child("recebe_ligacoes").setValue("true");
                                        else{
                                            idUsuarioCorrente = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                            usuariosRef.child(idUsuarioCorrente).child("recebe_ligacoes").setValue("true");
                                        }
                                        Toast.makeText(Perfil.this, "Aceitando ligações pelo app.", Toast.LENGTH_SHORT).show();
                                    }else{
                                        if(idUsuarioCorrente != null)
                                            usuariosRef.child(idUsuarioCorrente).child("recebe_ligacoes").setValue("false");
                                        else{
                                            idUsuarioCorrente = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                            usuariosRef.child(idUsuarioCorrente).child("recebe_ligacoes").setValue("false");
                                        }
                                        Toast.makeText(Perfil.this, "Não aceitando ligações pelo app.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            conversasRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        if (ds.exists()) {
                                            Boolean isMsgVista = (boolean) ds.child("visto").getValue();
                                            if (!isMsgVista) {
                                                if (notShown == false) {
                                                    //createAndOpenNotification("Mensagens não lidas", "Você tem mensagens não lidas.", "1");
                                                }
                                            }
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            mudarNum.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent mudarNumero = new Intent(Perfil.this, MudarTelefone.class);
                                    mudarNumero.putExtra("idusuario_chat", idOutroUsuario);
                                    mudarNumero.putExtra("telefoneusuario_chat", telefoneOutroUsuario);
                                    startActivity(mudarNumero);
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();

        /*notificationsOffRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(onScreen) {
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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


    }

    private String getMonthName() {
        int monthNum = Calendar.getInstance().get(Calendar.MONTH);

        if(monthNum == 0){
            return "01";
        }else if(monthNum == 1){
            return "02";
        }else if(monthNum == 2){
            return "03";
        }else if(monthNum == 3){
            return "04";
        }else if(monthNum == 4){
            return "05";
        }else if(monthNum == 5){
            return "06";
        }else if(monthNum == 6){
            return "07";
        }else if(monthNum == 7){
            return "08";
        }else if(monthNum == 8){
            return "09";
        }else if(monthNum == 9){
            return "10";
        }else if(monthNum == 10){
            return "11";
        }else if(monthNum == 11){
            return "12";
        }else {
            return null;
        }
    }

    private String corrigirDiaDoMes(int dia){
        if(dia < 10){
            return "0" + dia;
        }else{
            return String.valueOf(dia);
        }
    }

    private void setupImageLoader(){
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                Perfil.this)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100*1024*1024).build();

        ImageLoader.getInstance().init(config);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1 : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telefoneOutroUsuario));

                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        startActivity(intent);
                    }
                }
            }
        }
    }



    /*@Override
    public void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser() != null){
            usuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if(mAuth.getCurrentUser() != null)
            usuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(false);

    }*/

    /*private void createAndOpenNotification(String title, String text, String type, String id, String nome, String imgUrl, String telefone, String tab, String keyValue){
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

        notificationsOffRef.child(mAuth.getCurrentUser().getUid()).child(keyValue).removeValue();

    }*/

    @Override
    public void onResume() {
        super.onResume();

        onScreen = true;



        if(mAuth.getCurrentUser() != null){
            usuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(true);
            usuariosRef.child(mAuth.getCurrentUser().getUid()).child("conversando_com").setValue("ninguem");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        onScreen = false;

        usuariosRef.goOffline();
        usuarioRef.goOffline();
        conversasRef.goOffline();
        notificationsOffRef.goOffline();

        if(mAuth.getCurrentUser() != null) {
            usuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(false);
        }
    }

    @Override
    public void onDestroy(){
        if(alert != null) {
            alert.dismiss();
        }
        if(alert2 != null) {
            alert2.dismiss();
        }
        mCarregarDados.dismiss();
        super.onDestroy();
    }
}
