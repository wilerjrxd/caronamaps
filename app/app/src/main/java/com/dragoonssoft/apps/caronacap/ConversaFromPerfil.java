package com.dragoonssoft.apps.caronacap;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;
import static androidx.core.app.NotificationCompat.DEFAULT_VIBRATE;

public class ConversaFromPerfil extends AppCompatActivity {

    private String idOutroUsuario, nomeOutroUsuario, imgUrlOutroUsuario;
    private Toolbar conversaToolbar;
    private DatabaseReference rootRef;
    private ImageView barImgView;
    private TextView barTitleView, barOnOffStatusView;
    private TextView typeMsgEditText;
    private ImageButton sendMsgBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference usuarioRef;
    private boolean isUsuarioOnline;
    private DatabaseReference allUsuariosRef, mensagensRef;
    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
    private String telefoneOutroUsuario;
    private ImageView barCallView;
    private final List<Mensagens> mensagemList = new ArrayList<>();
    private RecyclerView mMensagensList;
    //private ListView mMensagensList;
    private LinearLayoutManager mLinearLayout;
    private MensagemAdapter mAdapter;
    private MensagemListAdapter adapter;
    private ArrayList<Mensagens> mensagemArrayList;
    private String keyValue = null;
    private GetTimeAgo getTimeAgo = new GetTimeAgo();
    private Calendar dateTime = Calendar.getInstance();
    private Boolean online = false;
    private DatabaseReference notificationsRef;
    private DatabaseReference conversasRef;
    private String outroUsuarioConversandoCom;
    private String idUsuarioCorrente, imgUrlUsuarioCorrente, nomeUsuarioCorrente, telefoneUsuarioCorrente;
    private boolean auxBoolean;
    private boolean onScreen = true;
    private boolean isNotificationAlreadySent = false;
    private static AlertDialog.Builder alert_builder;
    private static AlertDialog alert;
    private RelativeLayout.LayoutParams params, params2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversa_from_perfil);

        conversaToolbar = (Toolbar) findViewById(R.id.conversa_toolbar);
        setSupportActionBar(conversaToolbar);

        setupImageLoader();
        ImageLoader imageLoader = ImageLoader.getInstance();
        int defaultImage = getResources().getIdentifier("@drawable/ic_launcher",null,getPackageName());
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);

        params2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.ALIGN_LEFT);
        params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params2.setMargins(0, 2, 0, 0);

        alert_builder = new AlertDialog.Builder(ConversaFromPerfil.this);
        alert = alert_builder.create();
        alert.setTitle("Ligação");

        idOutroUsuario = getIntent().getStringExtra("idusuario_chat");
        nomeOutroUsuario = getIntent().getStringExtra("nomeusuario_chat");
        imgUrlOutroUsuario = getIntent().getStringExtra("imgusuario_chat");
        telefoneOutroUsuario = getIntent().getStringExtra("telefoneusuario_chat");

        idUsuarioCorrente = ListaCaronasFragmento.idUsuarioCorrenteGlobal;
        nomeUsuarioCorrente = ListaCaronasFragmento.nomeUsuarioCorrenteGlobal;
        imgUrlUsuarioCorrente = ListaCaronasFragmento.imgUrlUsuarioCorrenteGlobal;
        telefoneUsuarioCorrente = ListaCaronasFragmento.telefoneUsuarioCorrenteGlobal;

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        allUsuariosRef = rootRef.child("usuarios");
        usuarioRef = allUsuariosRef.child(idOutroUsuario);
        notificationsRef = rootRef.child("notifications");
        conversasRef = FirebaseDatabase.getInstance().getReference().child("conversas");

        allUsuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").onDisconnect().setValue(false);
        allUsuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        barTitleView = (TextView) findViewById(R.id.custom_bar_title);
        barOnOffStatusView = (TextView) findViewById(R.id.custom_online_offline_status);
        barImgView = (ImageView) findViewById(R.id.custom_bar_icon);
        barCallView = (ImageView) findViewById(R.id.custom_call_btn);

        mMensagensList = (RecyclerView) findViewById(R.id.mensagensList);
        //mMensagensList = (ListView) findViewById(R.id.mensagensList);
        mLinearLayout = new LinearLayoutManager(this);
        mLinearLayout.setStackFromEnd(true);
        typeMsgEditText = (EditText) findViewById(R.id.typeMsgEditText);
        sendMsgBtn = (ImageButton) findViewById(R.id.sendMsgBtn);

        /*mensagemArrayList = new ArrayList<>();
        adapter = new MensagemListAdapter(Conversa.this, R.layout.list_message_item, mensagemArrayList);
        mMensagensList.setAdapter(adapter);*/

        mAdapter = new MensagemAdapter(mensagemList);

        mMensagensList.setHasFixedSize(true);
        mMensagensList.setLayoutManager(mLinearLayout);
        //mMensagensList.scrollToPosition(mensagemList.size()-1);
        mMensagensList.setAdapter(mAdapter);

        carregarMensagens();

        barTitleView.setText(nomeOutroUsuario);
        imageLoader.displayImage(imgUrlOutroUsuario, barImgView, options);
        //typeMsgEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(144)});

        if(!idOutroUsuario.equals("Z2bNL808QPNlhue5etXpWwod7Ki2")){
            barCallView.setVisibility(View.VISIBLE);
        }

        /*firebaseListAdapter = new FirebaseListAdapter<Mensagens>(this, Mensagens.class, R.layout.list_message_adapter, orderCaronasPorHorario) {
            @Override
            protected void populateView(View view, CaronaInfo carona, int position) {

                TextView nomeView = (TextView) view.findViewById(R.id.nomeList);
                TextView horarioView = (TextView) view.findViewById(R.id.horarioList);
                TextView vagasView = (TextView) view.findViewById(R.id.vagasList);
                TextView partidaDestinoView = (TextView) view.findViewById(R.id.partidaDestinoList);
                ImageView imgView = (ImageView) view.findViewById(R.id.profileImagePerfil);
                ImageLoader imageLoader = ImageLoader.getInstance();
                int defaultImage = getActivity().getApplicationContext().getResources().getIdentifier("@drawable/ic_launcher",null,getActivity().getApplicationContext().getPackageName());
                DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                        .cacheOnDisk(true).resetViewBeforeLoading(true)
                        .showImageForEmptyUri(defaultImage)
                        .showImageOnFail(defaultImage)
                        .showImageOnLoading(defaultImage).build();

                if(carona.getData().equals(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()))) {
                    nomeView.setText(carona.getNome());
                    horarioView.setText(carona.getHorario());
                    vagasView.setText(carona.getVagas());
                    partidaDestinoView.setText(carona.getPartidaDestino());
                    imageLoader.displayImage(carona.getImg(), imgView, options);
                    Toast.makeText(mActivity, "EQUALS", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(mActivity, "DOES NOT EQUAL", Toast.LENGTH_SHORT).show();
                    if(criarCaronaBtn.getText().equals("VER CARONA")){
                        criarCaronaBtn.setText("CRIAR CARONA");
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
        caronasList.setAdapter(firebaseListAdapter);*/

        typeMsgEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        barCallView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alert_builder.setMessage("Esta ação pode lhe custar dinheiro, uma vez que utiliza créditos que você tem com sua operadora celular.")
                        .setPositiveButton("CONTINUAR", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //fazer ligação
                                Intent ligacao = new Intent(Intent.ACTION_CALL);
                                ligacao.setData(Uri.parse("tel:" + telefoneOutroUsuario));
                                if (ActivityCompat.checkSelfPermission(ConversaFromPerfil.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    ActivityCompat.requestPermissions(ConversaFromPerfil.this, new String[]{Manifest.permission.CALL_PHONE}, 1);


                                    return;
                                }else{
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

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("online")) {
                    ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if(networkInfo != null && networkInfo.isConnected()){
                        barOnOffStatusView.setText("online");
                        barOnOffStatusView.setVisibility(View.VISIBLE);
                        barTitleView.setLayoutParams(params2);
                    }else{
                        barOnOffStatusView.setText("");
                        barOnOffStatusView.setVisibility(View.GONE);
                        barTitleView.setLayoutParams(params);
                    }

                    online = (boolean) dataSnapshot.child("online").getValue();
                    if (online == true) {
                        barOnOffStatusView.setText("online");
                        barOnOffStatusView.setVisibility(View.VISIBLE);
                        barTitleView.setLayoutParams(params2);
                    } else {
                        barOnOffStatusView.setText("");
                        barOnOffStatusView.setVisibility(View.GONE);
                        barTitleView.setLayoutParams(params);
                    }
                    outroUsuarioConversandoCom = dataSnapshot.child("conversando_com").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*rootRef.child("Conversa").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(idOutroUsuario)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("visto", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Conversa/" + mAuth.getCurrentUser().getUid() + "/" + idOutroUsuario, chatAddMap);
                    chatUserMap.put("Conversa/" + idOutroUsuario + "/" + mAuth.getCurrentUser().getUid(), chatAddMap);

                    rootRef.updateChildren(chatUserMap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        conversasRef.child(mAuth.getCurrentUser().getUid()).child(idOutroUsuario).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    conversasRef.child(mAuth.getCurrentUser().getUid()).child(idOutroUsuario).child("visto").setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensagem();
            }
        });

        barImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent abrirConversa = new Intent(ConversaFromPerfil.this, Perfil.class);
                abrirConversa.putExtra("idusuario_chat", idOutroUsuario);
                abrirConversa.putExtra("nomeusuario_chat", nomeOutroUsuario);
                abrirConversa.putExtra("imgusuario_chat", imgUrlOutroUsuario);
                abrirConversa.putExtra("telefoneusuario_chat", telefoneOutroUsuario);
                startActivity(abrirConversa);
            }
        });

        rootRef.child("notifications_off").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(onScreen) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.hasChild("type")) {
                            String keyValue = ds.getKey();
                            if (ds.child("type").getValue().toString().equals("msg")) {
                                //String from_nome = ds.child("from_nome").getValue().toString();
                                //String from_id = ds.child("from_id").getValue().toString();
                                //String from_img = ds.child("from_img").getValue().toString();
                                //String from_telefone = ds.child("from_telefone").getValue().toString();
                                createAndOpenNotification("Mensagem", "Você recebeu uma nova mensagem", "msg", "", "", "", "", "1", keyValue);
                            }/*else if(ds.child("type").getValue().toString().equals("carona_cancelada")){
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
                            }*/
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        startActivity(intent);
                    }
                }
            }
        }
    }
    private void carregarMensagens() {

        rootRef.child("mensagens").child(mAuth.getCurrentUser().getUid()).child(idOutroUsuario).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Mensagens mensagens = dataSnapshot.getValue(Mensagens.class);

                mensagens.setHorario(mensagens.getHorario());
                mensagens.setMsg(mensagens.getMsg());
                mensagemList.add(mensagens);
                mAdapter.notifyDataSetChanged();
                mMensagensList.scrollToPosition(mensagemList.size()-1);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Mensagens mensagens = dataSnapshot.getValue(Mensagens.class);
                mensagens.setFrom(dataSnapshot.getKey());

                mensagens.setHorario(mensagens.getHorario());
                mensagens.setMsg(mensagens.getMsg());
                mensagemList.add(mensagens);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    private void enviarMensagem() {

        String msg = typeMsgEditText.getText().toString();
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){
            if(msg.length() != 0){

                String usuarioCorrenteRef = "mensagens/" + mAuth.getCurrentUser().getUid() + "/" + idOutroUsuario;
                String outroUsuarioRef = "mensagens/" + idOutroUsuario + "/" + mAuth.getCurrentUser().getUid();
                String conversaUsuarioCorrenteRef = "conversas/" + mAuth.getCurrentUser().getUid() + "/" + idOutroUsuario;
                String conversaOutroUsuarioRef = "conversas/" + idOutroUsuario + "/" + mAuth.getCurrentUser().getUid();

                DatabaseReference outroUsuarioMsgPush = rootRef.child("mensagens").child(mAuth.getCurrentUser().getUid()).child(idOutroUsuario).push();
                int horas = dateTime.get(Calendar.HOUR_OF_DAY);
                int minutos = dateTime.get(Calendar.MINUTE);
                String msgOutroUsuarioPushId = outroUsuarioMsgPush.getKey();
                String horarioCorrigido = corrigirFormatoHorario(horas, minutos);
                String horario = "-" + dateTime.get(Calendar.DAY_OF_MONTH) + getMonthName() + dateTime.get(Calendar.YEAR) + corrigirFormatoHorarioSemDoisPontos(horas, minutos);
                long horarioToInt = Long.parseLong(horario);

                Map msgMap = new HashMap();
                msgMap.put("msg", msg);
                //msgMap.put("visto", false);
                msgMap.put("tipo", "texto");
                msgMap.put("horario", horarioCorrigido + ", " + dateTime.get(Calendar.DAY_OF_MONTH) + "/" + getMonthName() + "/" + dateTime.get(Calendar.YEAR));
                msgMap.put("from", mAuth.getCurrentUser().getUid());

                Map msgOutroUsuarioMap = new HashMap();
                msgOutroUsuarioMap.put(usuarioCorrenteRef + "/" + msgOutroUsuarioPushId, msgMap);
                msgOutroUsuarioMap.put(outroUsuarioRef + "/" + msgOutroUsuarioPushId, msgMap);

                Map conversaUsuarioCorrenteDataMap = new HashMap();
                conversaUsuarioCorrenteDataMap.put(conversaUsuarioCorrenteRef + "/visto", true);
                conversaUsuarioCorrenteDataMap.put(conversaUsuarioCorrenteRef + "/horario", horarioCorrigido);
                conversaUsuarioCorrenteDataMap.put(conversaUsuarioCorrenteRef + "/horarioinv", horarioToInt);

                Map conversaOutroUsuarioDataMap = new HashMap();
                conversaOutroUsuarioDataMap.put(conversaOutroUsuarioRef + "/visto", false);
                conversaOutroUsuarioDataMap.put(conversaOutroUsuarioRef + "/horario", horarioCorrigido);
                conversaOutroUsuarioDataMap.put(conversaOutroUsuarioRef + "/horarioinv", horarioToInt);

                rootRef.updateChildren(msgOutroUsuarioMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            rootRef.updateChildren(conversaUsuarioCorrenteDataMap);
                            rootRef.updateChildren(conversaOutroUsuarioDataMap);
                            sendNotification(online);
                        }
                    }
                });

                /*conversasRef.child(mAuth.getCurrentUser().getUid()).child(idOutroUsuario).child("visto").setValue(true);
                conversasRef.child(idOutroUsuario).child(mAuth.getCurrentUser().getUid()).child("visto").setValue(false);
                conversasRef.child(mAuth.getCurrentUser().getUid()).child(idOutroUsuario).child("horario").setValue(horarioCorrigido);
                conversasRef.child(idOutroUsuario).child(mAuth.getCurrentUser().getUid()).child("horario").setValue(horarioCorrigido);
                conversasRef.child(mAuth.getCurrentUser().getUid()).child(idOutroUsuario).child("horarioinv").setValue(horarioToInt);
                conversasRef.child(idOutroUsuario).child(mAuth.getCurrentUser().getUid()).child("horarioinv").setValue(horarioToInt);*/


                typeMsgEditText.setText("");
                //play sending sound

            }
        }else{
            Toast.makeText(this, "Conecte-se à Internet para enviar a mensagem.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotification(boolean online) {
        if(!outroUsuarioConversandoCom.equals(mAuth.getCurrentUser().getUid())){
            if(!isNotificationAlreadySent) {
                if (!online) {
                    HashMap<String, String> notificationInfo = new HashMap<>();
                    notificationInfo.put("from", mAuth.getCurrentUser().getUid());
                    notificationInfo.put("type", "msg");
                    notificationsRef.child(idOutroUsuario).push().setValue(notificationInfo);
                } else {
                    String key = rootRef.child("notifications_off").child(idOutroUsuario).push().getKey();
                    HashMap<String, String> notificationOff = new HashMap<>();
                    //notificationOff.put("from_id", idUsuarioCorrente);
                    //notificationOff.put("from_nome", nomeUsuarioCorrente);
                    //notificationOff.put("from_img", imgUrlUsuarioCorrente);
                    //notificationOff.put("from_telefone", telefoneUsuarioCorrente);
                    notificationOff.put("type", "msg");

                    rootRef.child("notifications_off").child(idOutroUsuario).push().setValue(notificationOff);
                }
                isNotificationAlreadySent = true;
            }
        }
    }

    private String corrigirFormatoHorario(int horas, int minutos){
        if (horas >= 0 && horas < 10) {
            if (minutos >= 0 && minutos < 10) {
                return "0" + horas + ":0" + minutos;
            } else {
                return "0" + horas + ":" + minutos;
            }
        } else {
            if (minutos >= 0 && minutos < 10) {
                return horas + ":0" + minutos;
            } else {
                return horas + ":" + minutos;
            }
        }
    }

    private String corrigirFormatoHorarioSemDoisPontos(int horas, int minutos){
        if (horas >= 0 && horas < 10) {
            if (minutos >= 0 && minutos < 10) {
                return "0" + horas + "0" + minutos;
            } else {
                return "0" + horas + minutos;
            }
        } else {
            if (minutos >= 0 && minutos < 10) {
                return horas + "0" + minutos;
            } else {
                return horas +""+ minutos;
            }
        }
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

    private void setupImageLoader(){
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100*1024*1024).build();
        ImageLoader.getInstance().init(config);
    }

    private void createAndOpenNotification(String title, String text, String type, String id, String nome, String imgUrl, String telefone, String tab, String keyValue){

        HomeTabbed.mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setOnlyAlertOnce(true)
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

        rootRef.child("notifications_off").child(mAuth.getCurrentUser().getUid()).child(keyValue).removeValue();

    }

    /*@Override
    public void onStart() {
        super.onStart();
        Toast.makeText(Conversa.this, "Em Conversa", Toast.LENGTH_SHORT).show();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            allUsuariosRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true);
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onPause() {
        super.onPause();

        onScreen = false;

        if(mAuth.getCurrentUser() != null){
            allUsuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(false);
            allUsuariosRef.child(mAuth.getCurrentUser().getUid()).child("conversando_com").setValue("ninguem");
        }

        rootRef.goOffline();
        allUsuariosRef.goOffline();
        usuarioRef.goOffline();
        notificationsRef.goOffline();
        conversasRef.goOffline();

    }

    @Override
    public void onResume() {
        super.onResume();

        onScreen = true;
        isNotificationAlreadySent = false;

        rootRef.goOnline();
        allUsuariosRef.goOnline();
        usuarioRef.goOnline();
        notificationsRef.goOnline();
        conversasRef.goOnline();

        if(mAuth.getCurrentUser() != null) {
            allUsuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(true);
            allUsuariosRef.child(mAuth.getCurrentUser().getUid()).child("conversando_com").setValue(idOutroUsuario);
        }

        if(!online) {
            barOnOffStatusView.setText("");
            barOnOffStatusView.setVisibility(View.GONE);
        }else{
            ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()){
                barOnOffStatusView.setText("online");
                barOnOffStatusView.setVisibility(View.VISIBLE);
                barTitleView.setLayoutParams(params2);
            } else {
                barOnOffStatusView.setText("");
                barOnOffStatusView.setVisibility(View.GONE);
                barTitleView.setLayoutParams(params);
            }
        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(alert != null) {
            alert.dismiss();
        }
    }
    /*@Override
    public void onStop() {
        super.onStop();
        Toast.makeText(Conversa.this, "Saindo de Conversa", Toast.LENGTH_SHORT).show();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            allUsuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(false);
        }
    }*/
}
