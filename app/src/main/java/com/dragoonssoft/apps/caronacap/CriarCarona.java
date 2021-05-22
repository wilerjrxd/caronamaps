package com.dragoonssoft.apps.caronacap;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import androidx.core.app.FragmentManager;
import androidx.core.app.FragmentTransaction;
import androidx.core.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;
import static androidx.core.app.NotificationCompat.DEFAULT_VIBRATE;

import androidx.core.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CriarCarona extends AppCompatActivity {
    private static Spinner locaisPartida, locaisDestino, vagas;
    private static TextView partidaDestinoLabel, enviarProposta;
    private static int horas, minutos;
    private static TextView cap_ob, ob_cap;
    private static boolean obCAPChecked = false, capOBChecked = true;
    private static TimePicker horario;
    private static String dataCarona;
    private static Button criarCaronaBtn;
    private static AlertDialog.Builder alert_builder;
    private static AlertDialog alert;
    private String carroGlobalParaMudarCarro;
    private EditText carroGlobalCriarCaronaText;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase db;
    private DatabaseReference caronaRef, usuarioRef;
    private Toolbar criarCaronaToolbar;
    private ProgressDialog mCriarCaronaProgress;
    private HashMap<String, String> caronaMap;
    private Boolean horaPassada = false;
    private DatabaseReference usuariosRef;
    private DatabaseReference notificationsOffRef;
    private boolean onScreen = true;
    private boolean isNotificationAlreadySent = false;
    private String imgUrlGrande;
    private String carroUsuario, partidaUsuario, destinoUsuario;
    private TimerTask task;
    private Timer timer;
    private long elapsed = 0;
    private long INTERVAL = 1000, TIMEOUT = 20000;
    private ArrayAdapter locaisAdapter, locaisPartidaCapAdapter;
    private Spinner parada1Spinner, parada2Spinner;
    private ArrayAdapter paradas1Adapter, paradas2Adapter;
    private String parada1Usuario, parada2Usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_carona);

        criarCaronaToolbar = (Toolbar)findViewById(R.id.criar_carona_app_bar);
        setSupportActionBar(criarCaronaToolbar);
        getSupportActionBar().setTitle("Criar carona");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCriarCaronaProgress = new ProgressDialog(this);
        mCriarCaronaProgress.setMessage("Carregando informações...");
        mCriarCaronaProgress.setCanceledOnTouchOutside(false);
        mCriarCaronaProgress.setCancelable(false);

        locaisPartida = (Spinner)findViewById(R.id.partidaSpinner);
        locaisDestino = (Spinner)findViewById(R.id.destinoSpinner);
        parada1Spinner = (Spinner)findViewById(R.id.parada1Spinner);
        parada2Spinner = (Spinner)findViewById(R.id.parada2Spinner);
        vagas = (Spinner)findViewById(R.id.vagasOBSpinner);
        horario = (TimePicker)findViewById(R.id.horarioParaOBPicker);
        criarCaronaBtn = (Button)findViewById(R.id.criarCaronaParaOBBtn);
        partidaDestinoLabel = (TextView) findViewById(R.id.partidaDestinoLabel);
        enviarProposta = (TextView)findViewById(R.id.enviarProposta);
        carroGlobalCriarCaronaText = (EditText) findViewById(R.id.carroText);

        locaisAdapter = ArrayAdapter.createFromResource(this,R.array.locais, android.R.layout.simple_spinner_dropdown_item);
        locaisPartidaCapAdapter = ArrayAdapter.createFromResource(this,R.array.locais_cap, android.R.layout.simple_spinner_dropdown_item);
        paradas1Adapter = ArrayAdapter.createFromResource(this,R.array.locais_paradas, android.R.layout.simple_spinner_dropdown_item);
        paradas2Adapter = ArrayAdapter.createFromResource(this,R.array.locais_paradas, android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter vagasAdapter = ArrayAdapter.createFromResource(this,R.array.vagas, android.R.layout.simple_spinner_dropdown_item);

        locaisPartida.setAdapter(locaisAdapter);
        locaisDestino.setAdapter(locaisPartidaCapAdapter);
        parada1Spinner.setAdapter(paradas1Adapter);
        parada2Spinner.setAdapter(paradas2Adapter);
        vagas.setAdapter(vagasAdapter);
        horario.setIs24HourView(true);

        alert_builder = new AlertDialog.Builder(CriarCarona.this);

        mAuth = FirebaseAuth.getInstance();
        caronaRef = db.getInstance().getReference().child("caronas");
        usuariosRef =  db.getInstance().getReference().child("usuarios");
        usuarioRef = usuariosRef.child(mAuth.getCurrentUser().getUid());
        //notificationsOffRef = FirebaseDatabase.getInstance().getReference().child("notifications_off");

        mCriarCaronaProgress.show();

        task=new TimerTask(){
            @Override
            public void run() {
                elapsed+=INTERVAL;
                if(elapsed>=TIMEOUT){
                    this.cancel();
                    elapsed = 0;
                    mCriarCaronaProgress.hide();
                    Toast.makeText(CriarCarona.this, "Pode ser que sua conexão com a Internet tenha expirado. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    pegarImageUrlAndCarro();
                }
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(task, INTERVAL, TIMEOUT);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        MapFragmentClass mf = new MapFragmentClass();
        ft.add(R.id.map_layout, mf);
        ft.commit();

        //usuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").onDisconnect().setValue(false);
        //usuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(true);

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

    private void pegarImageUrlAndCarro() {
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(ListaCaronasFragmento.imgUrlUsuarioCorrenteGlobal != null){
                    imgUrlGrande = ListaCaronasFragmento.imgUrlUsuarioCorrenteGlobal;
                }else{
                    imgUrlGrande = dataSnapshot.child("imgURL").getValue().toString();
                    ListaCaronasFragmento.imgUrlUsuarioCorrenteGlobal = imgUrlGrande;
                }
                if(dataSnapshot.hasChild("carro")){
                    if(ListaCaronasFragmento.carroUsuarioCorrenteGlobal != null) {
                        carroUsuario = ListaCaronasFragmento.carroUsuarioCorrenteGlobal;
                        carroGlobalCriarCaronaText.setText(carroUsuario);
                    }else{
                        carroUsuario = dataSnapshot.child("carro").getValue().toString();
                        carroGlobalCriarCaronaText.setText(carroUsuario);
                        ListaCaronasFragmento.carroUsuarioCorrenteGlobal = carroUsuario;
                    }
                }else{
                    carroUsuario = null;
                    ListaCaronasFragmento.carroUsuarioCorrenteGlobal = carroUsuario;
                    carroGlobalCriarCaronaText.setText(carroUsuario);
                }

                if(dataSnapshot.hasChild("partida") && dataSnapshot.hasChild("destino")){
                    partidaUsuario = dataSnapshot.child("partida").getValue().toString();
                    destinoUsuario = dataSnapshot.child("destino").getValue().toString();
                    int spinnerPartida = locaisAdapter.getPosition(partidaUsuario);
                    locaisPartida.setSelection(spinnerPartida);
                    int spinnerDestino = locaisAdapter.getPosition(destinoUsuario);
                    locaisDestino.setSelection(spinnerDestino);
                }

                if(dataSnapshot.hasChild("parada1") && dataSnapshot.hasChild("parada2")){
                    parada1Usuario = dataSnapshot.child("parada1").getValue().toString();
                    parada2Usuario = dataSnapshot.child("parada2").getValue().toString();
                    int spinnerParada1 = paradas1Adapter.getPosition(parada1Usuario);
                    parada1Spinner.setSelection(spinnerParada1);
                    int spinnerParada2 = paradas2Adapter.getPosition(parada2Usuario);
                    parada2Spinner.setSelection(spinnerParada2);
                }

                if(imgUrlGrande != null){
                    task.cancel();
                    elapsed = 0;
                    mCriarCaronaProgress.hide();
                    Calendar dateTime = Calendar.getInstance();
                    criarCaronaBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseUser user = mAuth.getInstance().getCurrentUser();
                            horas = horario.getCurrentHour();
                            minutos = horario.getCurrentMinute();
                            if(horas < dateTime.get(Calendar.HOUR_OF_DAY)) {
                                horaPassada = true;
                            }
                            else if(horas == dateTime.get(Calendar.HOUR_OF_DAY)){
                                if(minutos < dateTime.get(Calendar.MINUTE)) {
                                    horaPassada = true;
                                }else{
                                    horaPassada = false;
                                }
                            }else{
                                horaPassada = false;
                            }
                            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                            if (networkInfo != null && networkInfo.isConnected()) {
                                if(!horaPassada) {
                                    if (!locaisPartida.getSelectedItem().toString().equals(locaisDestino.getSelectedItem().toString())) {
                                        String parada1Item = parada1Spinner.getSelectedItem().toString();
                                        String parada2Item = parada2Spinner.getSelectedItem().toString();
                                        /*if (parada1Item.equals(parada2Item)) {
                                            if((parada1Item.contains("") && parada2Item.contains(""))
                                                    || (parada1Item.contains("") && !parada2Item.contains(""))
                                                    || (!parada1Item.contains("") && parada2Item.contains(""))
                                                    || (!parada1Item.contains("") && !parada2Item.contains(""))){*/
                                                if (user != null) {
                                                    mCriarCaronaProgress.setTitle("Registrando carona");
                                                    mCriarCaronaProgress.setMessage("Aguarde enquanto sua carona é registrada...");
                                                    mCriarCaronaProgress.show();
                                                    String txCarro = carroGlobalCriarCaronaText.getText().toString();
                                                    String idCarona = caronaRef.push().getKey();
                                                    caronaMap = new HashMap<>();
                                                    caronaMap.put("nome", user.getDisplayName());
                                                    caronaMap.put("nomelower", user.getDisplayName().toLowerCase());
                                                    caronaMap.put("idusuario", user.getUid());

                                                    if (imgUrlGrande != null) {
                                                        caronaMap.put("img", imgUrlGrande);
                                                    } else {
                                                        caronaMap.put("img", user.getPhotoUrl().toString());
                                                    }

                                                    if (txCarro.length() != 0 && !txCarro.equals("")) {
                                                        caronaMap.put("carro", txCarro);
                                                    } else {
                                                        caronaMap.put("carro", "Não informado");
                                                    }

                                                    caronaMap.put("partida", locaisPartida.getSelectedItem().toString());

                                                    caronaMap.put("destino", locaisDestino.getSelectedItem().toString());

                                                    if(parada1Item.equals("-"))
                                                        caronaMap.put("parada1", "nopref");
                                                    else
                                                        caronaMap.put("parada1", parada1Item);

                                                    if(parada2Item.equals("-"))
                                                        caronaMap.put("parada2", "nopref");
                                                    else
                                                        caronaMap.put("parada2", parada2Item);

                                                    caronaMap.put("partidaDestino", locaisPartida.getSelectedItem().toString() + " > " + locaisDestino.getSelectedItem().toString());

                                                    if (vagas.getSelectedItem().toString().equals("1") || vagas.getSelectedItem().toString().equals("0"))
                                                        caronaMap.put("vagas", vagas.getSelectedItem().toString() + " vaga");
                                                    else
                                                        caronaMap.put("vagas", vagas.getSelectedItem().toString() + " vagas");

                                                    if(!parada1Item.equals("-") && !parada2Item.equals("-")) {
                                                        caronaMap.put("preferencias", locaisPartida.getSelectedItem() + "_" + locaisDestino.getSelectedItem()
                                                                + "_" + parada1Item + "_" + parada2Item);
                                                        caronaMap.put("partidadestinolower", locaisPartida.getSelectedItem().toString().toLowerCase() + " > "  + parada1Item.toLowerCase() + " > " + parada2Item.toLowerCase() + " > " + locaisDestino.getSelectedItem().toString().toLowerCase());
                                                    }
                                                    else if(!parada1Item.equals("-") && parada2Item.equals("-")) {
                                                        caronaMap.put("preferencias", locaisPartida.getSelectedItem() + "_" + locaisDestino.getSelectedItem()
                                                                + "_" + parada1Item + "_nopref");
                                                        caronaMap.put("partidadestinolower", locaisPartida.getSelectedItem().toString().toLowerCase() + " > " + parada1Item.toLowerCase() + " > " + locaisDestino.getSelectedItem().toString().toLowerCase());
                                                    }
                                                    else if(parada1Item.equals("-") && !parada2Item.equals("-")) {
                                                        caronaMap.put("preferencias", locaisPartida.getSelectedItem() + "_" + locaisDestino.getSelectedItem()
                                                                + "_nopref" + "_" + parada2Item);
                                                        caronaMap.put("partidadestinolower", locaisPartida.getSelectedItem().toString().toLowerCase() + " > " + parada2Item.toLowerCase() + " > " + locaisDestino.getSelectedItem().toString().toLowerCase());
                                                    }
                                                    else if(parada1Item.equals("-") && parada2Item.equals("-")) {
                                                        caronaMap.put("preferencias", locaisPartida.getSelectedItem() + "_" + locaisDestino.getSelectedItem() + "_nopref" + "_nopref");
                                                        caronaMap.put("partidadestinolower", locaisPartida.getSelectedItem().toString().toLowerCase() + " > " + locaisDestino.getSelectedItem().toString().toLowerCase());
                                                    }
                                                    caronaMap.put("data", new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()));
                                                    if (horas >= 0 && horas < 10) {
                                                        if (minutos >= 0 && minutos < 10) {
                                                            caronaMap.put("horario", "0" + horas + ":0" + minutos + "h");
                                                            //caronaMap.put("horario_order", "0" + horas + "0" + minutos);
                                                        } else {
                                                            caronaMap.put("horario", "0" + horas + ":" + minutos + "h");
                                                            //caronaMap.put("horario_order", "0" + horas + minutos);
                                                        }
                                                    } else {
                                                        if (minutos >= 0 && minutos < 10) {
                                                            caronaMap.put("horario", horas + ":0" + minutos + "h");
                                                            //caronaMap.put("horario_order", horas + "0" + minutos);
                                                        } else {
                                                            caronaMap.put("horario", horas + ":" + minutos + "h");
                                                            //caronaMap.put("horario_order", horas + "" + minutos);
                                                        }
                                                    }
                                                    preencherListCarona(idCarona);
                                                    //caronaMap.put("situacao", "ativa");
                                                    caronaRef.child(idCarona).setValue(caronaMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                //Intent toHome = new Intent(CriarCarona.this, HomeTabbed.class);
                                                                //toHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                //startActivity(toHome);
                                                                Map statusCarroMap = new HashMap();
                                                                statusCarroMap.put("status", "motorista_" + idCarona);
                                                                statusCarroMap.put("carro", txCarro);
                                                                statusCarroMap.put("partida", locaisPartida.getSelectedItem().toString());
                                                                statusCarroMap.put("destino", locaisDestino.getSelectedItem().toString());
                                                                if(parada1Item.equals("-")){
                                                                    statusCarroMap.put("parada1", "-");
                                                                }else{
                                                                    statusCarroMap.put("parada1", parada1Item);
                                                                }

                                                                if(parada2Item.equals("-")){
                                                                    statusCarroMap.put("parada2", "-");
                                                                }else{
                                                                    statusCarroMap.put("parada2", parada2Item);
                                                                }
                                                                //usuarioRef.child("status").setValue("motorista_" + idCarona);
                                                                //usuarioRef.child("carro").setValue(carroGlobalCriarCaronaText.getText().toString());
                                                                usuarioRef.updateChildren(statusCarroMap);
                                                                ListaCaronasFragmento.carroUsuarioCorrenteGlobal = txCarro;
                                                                mCriarCaronaProgress.hide();
                                                                Toast.makeText(CriarCarona.this, "Carona registrada com sucesso!", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            } else {
                                                                Toast.makeText(CriarCarona.this, "Algo deu errado ao registrar sua carona. Tente novamente.", Toast.LENGTH_SHORT).show();
                                                                mCriarCaronaProgress.hide();
                                                            }
                                                        }
                                                    });
                                                }
                                            /*}else{
                                                alert_builder.setMessage("Os pontos de parada não podem ser iguais.")
                                                        .setCancelable(true);
                                                alert = alert_builder.create();
                                                alert.setTitle("Pontos de parada iguais");
                                                alert.show();//Toast.makeText(CriarCarona.this, "Os locais de partida e destino não podem ser iguais.", Toast.LENGTH_SHORT).show();
                                            }
                                        }*/
                                    }else{
                                        alert_builder.setMessage("Os pontos de partida e destino não podem ser iguais.")
                                                .setCancelable(true);
                                        alert = alert_builder.create();
                                        alert.setTitle("Partida e destino iguais");
                                        alert.show();
                                    }
                                }else{
                                    alert_builder.setMessage("O horário escolhido para a carona já passou.")
                                            .setCancelable(true);
                                    alert = alert_builder.create();
                                    alert.setTitle("Horário inválido");
                                    alert.show();
                                }
                            }else{
                                mCriarCaronaProgress.hide();
                                Toast.makeText(CriarCarona.this, "Sem conexão com a Internet.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    enviarProposta.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent abrirConversa = new Intent(CriarCarona.this, Conversa.class);
                            abrirConversa.putExtra("idusuario_chat", "Gqwzf627C0acZe6G48Ynqhtn7003");
                            abrirConversa.putExtra("nomeusuario_chat", "Dragoons Soft");
                            abrirConversa.putExtra("imgusuario_chat", "@drawable/ic_launcher");
                            abrirConversa.putExtra("telefoneusuario_chat", "03131988723091");
                            startActivity(abrirConversa);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed(){
        /*super.onBackPressed()*/
        finish();
    }

    private void preencherListCarona(String idCarona){
        if(vagas.getSelectedItem().toString().equals("4")) {
            caronaMap.put("idcaroneiro1", "");
            caronaMap.put("nomecaroneiro1","ASSENTO VAZIO");
            caronaMap.put("imgcaroneiro1","");
            caronaMap.put("idcaroneiro2","");
            caronaMap.put("nomecaroneiro2","ASSENTO VAZIO");
            caronaMap.put("imgcaroneiro2","");
            caronaMap.put("idcaroneiro3","");
            caronaMap.put("nomecaroneiro3","ASSENTO VAZIO");
            caronaMap.put("imgcaroneiro3","");
            caronaMap.put("idcaroneiro4","");
            caronaMap.put("nomecaroneiro4","ASSENTO VAZIO");
            caronaMap.put("imgcaroneiro4","");
        }
        else if(vagas.getSelectedItem().toString().equals("3")){
            caronaMap.put("idcaroneiro1", "");
            caronaMap.put("nomecaroneiro1","ASSENTO VAZIO");
            caronaMap.put("imgcaroneiro1","");
            caronaMap.put("idcaroneiro2","");
            caronaMap.put("nomecaroneiro2","ASSENTO VAZIO");
            caronaMap.put("imgcaroneiro2","");
            caronaMap.put("idcaroneiro3","");
            caronaMap.put("nomecaroneiro3","ASSENTO VAZIO");
            caronaMap.put("imgcaroneiro3","");
            caronaMap.put("idcaroneiro4","");
            caronaMap.put("nomecaroneiro4","OCUPADO");
            caronaMap.put("imgcaroneiro4","");
        }
        else if(vagas.getSelectedItem().toString().equals("2")){
            caronaMap.put("idcaroneiro1", "");
            caronaMap.put("nomecaroneiro1","ASSENTO VAZIO");
            caronaMap.put("imgcaroneiro1","");
            caronaMap.put("idcaroneiro2","");
            caronaMap.put("nomecaroneiro2","ASSENTO VAZIO");
            caronaMap.put("imgcaroneiro2","");
            caronaMap.put("idcaroneiro3","");
            caronaMap.put("nomecaroneiro3","OCUPADO");
            caronaMap.put("imgcaroneiro3","");
            caronaMap.put("idcaroneiro4","");
            caronaMap.put("nomecaroneiro4","OCUPADO");
            caronaMap.put("imgcaroneiro4","");
        }
        else if(vagas.getSelectedItem().toString().equals("1")){
            caronaMap.put("idcaroneiro1", "");
            caronaMap.put("nomecaroneiro1","ASSENTO VAZIO");
            caronaMap.put("imgcaroneiro1","");
            caronaMap.put("idcaroneiro2","");
            caronaMap.put("nomecaroneiro2","OCUPADO");
            caronaMap.put("imgcaroneiro2","");
            caronaMap.put("idcaroneiro3","");
            caronaMap.put("nomecaroneiro3","OCUPADO");
            caronaMap.put("imgcaroneiro3","");
            caronaMap.put("idcaroneiro4","");
            caronaMap.put("nomecaroneiro4","OCUPADO");
            caronaMap.put("imgcaroneiro4","");
        }
        else if(vagas.getSelectedItem().toString().equals("0")){
            caronaMap.put("idcaroneiro1", "");
            caronaMap.put("nomecaroneiro1","OCUPADO");
            caronaMap.put("imgcaroneiro1","");
            caronaMap.put("idcaroneiro2","");
            caronaMap.put("nomecaroneiro2","OCUPADO");
            caronaMap.put("imgcaroneiro2","");
            caronaMap.put("idcaroneiro3","");
            caronaMap.put("nomecaroneiro3","OCUPADO");
            caronaMap.put("imgcaroneiro3","");
            caronaMap.put("idcaroneiro4","");
            caronaMap.put("nomecaroneiro4","OCUPADO");
            caronaMap.put("imgcaroneiro4","");
        }
    }

    /*@Override
    public void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser() != null) {
            usuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(true);
        }

    }
    @Override
    public void onStop() {
        super.onStop();

        if(mAuth.getCurrentUser() != null)
            usuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(false);
    }*/

    public void onResume() {
        super.onResume();
        onScreen = true;
        usuarioRef.goOnline();
        usuariosRef.goOnline();
        notificationsOffRef.goOnline();
        if(mAuth.getCurrentUser() != null) {
            usuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(true);
        }

    }
    @Override
    public void onPause() {
        super.onPause();

        onScreen = false;

        if(mAuth.getCurrentUser() != null) {
            usuariosRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue(false);
        }

        usuarioRef.goOnline();
        usuariosRef.goOffline();
        notificationsOffRef.goOffline();
    }

    @Override
    public void onDestroy(){
        mCriarCaronaProgress.dismiss();
        super.onDestroy();
    }

    /*private void createAndOpenNotification(String title, String text, String type, String id, String nome, String imgUrl, String telefone, String tab, String keyValue){
        isNotificationAlreadySent = true;
        notificationsOffRef.child(mAuth.getCurrentUser().getUid()).child(keyValue).removeValue();
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

    }*/

    /*private class MyTaskOB extends AsyncTask<Void, Void, Void> {
        private String destino, vagasDisp, horarioCarona, query;
        private Connection connection = null;

        @Override
        protected void onPreExecute() {
            destino = para.getSelectedItem().toString();
            horarioCarona = String.valueOf(horario.getCurrentHour()) + ":" + String.valueOf(horario.getCurrentMinute());
            dataCarona = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
            vagasDisp = vagas.getSelectedItem().toString();
        }

        @Override
        protected Void doInBackground(Void... arg0){
            try {
                connection = SqlConnection.dbConnector();
                query = "insert into carona (IDUsuario,Nome,Partida,Destino,Vagas,Horario,Telefone,DataCarona) values ("+Login.idGlobal+","+Login.nomeGlobal+", CAP,"+destino+","+vagasDisp+","+horarioCarona+","+dataCarona+")";

                PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

                pst.executeUpdate();

                ResultSet tableKeys = pst.getGeneratedKeys();
                tableKeys.next();
                tableKeys.getInt(1);

                pst.close();
                tableKeys.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            finish();
            super.onPostExecute(result);
        }
    }

    private class MyTaskCAP extends AsyncTask<Void, Void, Void> {
        private String partida, vagasDisp, horarioCarona, query;
        private int count = 0;
        private Connection connection = null;

        @Override
        protected void onPreExecute() {
            partida = para.getSelectedItem().toString();
            horarioCarona = String.valueOf(horario.getCurrentHour()) + ":" + String.valueOf(horario.getCurrentMinute());
            dataCarona = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
            vagasDisp = vagas.getSelectedItem().toString();
        }

        @Override
        protected Void doInBackground(Void... arg0){
            try {
                //connection = SqlConnection.dbConnector();

                query = "insert into carona (IDUsuario,Nome,Partida,Destino,Vagas,Horario,Telefone,DataCarona) values ("+Login.idGlobal+","+Login.nomeGlobal+","+partida+", CAP,"+vagasDisp+","+horarioCarona+","+dataCarona+")";

                PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

                pst.executeUpdate();

                ResultSet tableKeys = pst.getGeneratedKeys();
                tableKeys.next();
                tableKeys.getInt(1);

                pst.close();
                tableKeys.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            obCAPChecked = false;
            capOBChecked = true;
            Toast.makeText(CriarCarona.this, "CaronaInfo criada com sucesso!", Toast.LENGTH_SHORT).show();
            Thread timer = new Thread() {
                public void run(){
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            };
            timer.start();

            finish();
            super.onPostExecute(result);
        }
    }*/

}
