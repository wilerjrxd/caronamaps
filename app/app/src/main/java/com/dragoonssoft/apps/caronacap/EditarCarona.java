package com.dragoonssoft.apps.caronacap;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class EditarCarona extends AppCompatActivity {
    private String motorista, idMotorista, imgMotorista, nomeCaroneiro1, imgCaroneiro1, idCaroneiro1, nomeCaroneiro2, imgCaroneiro2, idCaroneiro2, nomeCaroneiro3, imgCaroneiro3, idCaroneiro3, nomeCaroneiro4, imgCaroneiro4, idCaroneiro4, horario, vagas, partida, idMotoristaDeOutraCarona;
    private ListView caronaDadosList;
    private ArrayList<CaronaInfo> caronaDadosArrayList = new ArrayList<>();
    private CaronaListMotoristaViewAdapter adapter;
    private Button fecharCaroneiro1Btn, fecharCaroneiro2Btn, fecharCaroneiro3Btn, fecharCaroneiro4Btn, msgCaroneiro1Btn, msgCaroneiro2Btn, msgCaroneiro3Btn, msgCaroneiro4Btn; //vai virar imgview
    private Query encontrarIdUsuarioEmCarona;
    private Toolbar pedirCaronaToolbar;
    private FirebaseUser mFirebaseUser;
    private Button pedirCancelarSairCaronaBtn;
    private Spinner locaisPartida, locaisDestino;
    private TextView partidaDestinoLabel, enviarProposta;
    private static int horas, minutos;
    private TextView cap_ob, ob_cap;
    private static boolean obCAPChecked = false, capOBChecked = true;
    private TimePicker horarioPicker;
    private static String dataCarona;
    private Button criarCaronaBtn;
    private static AlertDialog.Builder alert_builder;
    private static AlertDialog alert;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase db;
    private DatabaseReference caronaRef;
    private Toolbar criarCaronaToolbar;
    private ProgressDialog mCriarCaronaProgress;
    private Bundle extras;
    private int horasToInt, minutosToInt, numVagas;
    private String horasToString, minutosToString;
    private String destino;

    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_carona);

        criarCaronaToolbar = (Toolbar)findViewById(R.id.editar_carona_app_bar);
        setSupportActionBar(criarCaronaToolbar);
        getSupportActionBar().setTitle("Editar CaronaInfo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mCriarCaronaProgress = new ProgressDialog(this);
        mCriarCaronaProgress.setMessage("Atualizando dados da carona...");
        mCriarCaronaProgress.setCanceledOnTouchOutside(false);

        locaisPartida = (Spinner)findViewById(R.id.partidaSpinnerEditar);
        locaisDestino = (Spinner)findViewById(R.id.destinoSpinnerEditar);
        horarioPicker = (TimePicker)findViewById(R.id.horarioParaOBPickerEditar);
        //criarCaronaBtn = (Button)findViewById(R.id.criarCaronaParaOBBtnEditar);
        //ob_cap = (TextView) findViewById(R.id.ob_cap);
        //cap_ob = (TextView) findViewById(R.id.cap_ob);
        partidaDestinoLabel = (TextView) findViewById(R.id.partidaDestinoLabel);
        //enviarProposta = (TextView)findViewById(R.id.enviarProposta);

        ArrayAdapter locaisAdapter = ArrayAdapter.createFromResource(this,R.array.locais, android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter locaisPartidaCapAdapter = ArrayAdapter.createFromResource(this,R.array.locais_cap, android.R.layout.simple_spinner_dropdown_item);
        //ArrayAdapter capAdapter = ArrayAdapter.createFromResource(this,R.array.cap, android.R.layout.simple_spinner_dropdown_item);
        //ArrayAdapter vagasAdapter = ArrayAdapter.createFromResource(this,R.array.vagas, android.R.layout.simple_spinner_dropdown_item);

        locaisPartida.setAdapter(locaisAdapter);
        locaisDestino.setAdapter(locaisPartidaCapAdapter);
        //vagas.setAdapter(vagasAdapter);

        alert_builder = new AlertDialog.Builder(EditarCarona.this);
        alert_builder.setMessage("Os lugares de partida e destino não podem ser iguais.")
                .setCancelable(true);
        alert = alert_builder.create();
        alert.setTitle("Erro");

        mAuth = FirebaseAuth.getInstance();
        caronaRef = db.getInstance().getReference().child("caronas");

        Intent intent = getIntent();
        extras = intent.getExtras();
        idMotorista = extras.getString("idusuario");
        motorista = extras.getString("nome");
        imgMotorista = extras.getString("img");
        idCaroneiro1 = extras.getString("idcaroneiro1");
        nomeCaroneiro1 = extras.getString("nomecaroneiro1");
        imgCaroneiro1 = extras.getString("imgcaroneiro1");
        idCaroneiro2 = extras.getString("idcaroneiro2");
        nomeCaroneiro2 = extras.getString("nomecaroneiro2");
        imgCaroneiro2 = extras.getString("imgcaroneiro2");
        idCaroneiro3 = extras.getString("idcaroneiro3");
        nomeCaroneiro3 = extras.getString("nomecaroneiro3");
        imgCaroneiro3 = extras.getString("imgcaroneiro3");
        idCaroneiro4 = extras.getString("idcaroneiro4");
        nomeCaroneiro4 = extras.getString("nomecaroneiro4");
        imgCaroneiro4 = extras.getString("imgcaroneiro4");
        horario = extras.getString("horario");
        vagas = extras.getString("vagas");
        partida = extras.getString("partida");
        destino = extras.getString("destino");

        fecharCaroneiro1Btn = (Button)findViewById(R.id.tirarCaroneiro1Btn);
        fecharCaroneiro2Btn = (Button)findViewById(R.id.tirarCaroneiro2Btn);
        fecharCaroneiro3Btn = (Button)findViewById(R.id.tirarCaroneiro3Btn);
        fecharCaroneiro4Btn = (Button)findViewById(R.id.tirarCaroneiro4Btn);
        msgCaroneiro1Btn = (Button)findViewById(R.id.msg1Btn);
        msgCaroneiro2Btn = (Button)findViewById(R.id.msg2Btn);
        msgCaroneiro3Btn = (Button)findViewById(R.id.msg3Btn);
        msgCaroneiro4Btn = (Button)findViewById(R.id.msg4Btn);

        caronaDadosList = (ListView) findViewById(R.id.list_dados_carona);
        adapter = new CaronaListMotoristaViewAdapter(EditarCarona.this, R.layout.list_dados_carona_motorista_view_adapter,caronaDadosArrayList);
        caronaDadosList.setAdapter(adapter);

        horasToString = Character.toString(horario.charAt(0)) + Character.toString(horario.charAt(1));
        minutosToString = Character.toString(horario.charAt(3)) + Character.toString(horario.charAt(4));
        horasToInt = Integer.parseInt(horasToString);
        minutosToInt = Integer.parseInt(minutosToString);
        horarioPicker.setIs24HourView(true);
        horarioPicker.setHour(horasToInt);
        horarioPicker.setMinute(minutosToInt);
        locaisPartida.setSelection(locaisAdapter.getPosition(partida));
        locaisDestino.setSelection(locaisPartidaCapAdapter.getPosition(destino));

        CaronaInfo caronaDados = new CaronaInfo();//new CaronaInfo();
        caronaDados.setNome(motorista);
        caronaDados.setImg(imgMotorista);
        caronaDados.setNomecaroneiro1(nomeCaroneiro1);
        caronaDados.setImgcaroneiro1(imgCaroneiro1);
        caronaDados.setNomecaroneiro2(nomeCaroneiro2);
        caronaDados.setImgcaroneiro2(imgCaroneiro2);
        caronaDados.setNomecaroneiro3(nomeCaroneiro3);
        caronaDados.setImgcaroneiro3(imgCaroneiro3);
        caronaDados.setNomecaroneiro4(nomeCaroneiro4);
        caronaDados.setImgcaroneiro4(imgCaroneiro4);

        caronaDadosArrayList.add(caronaDados);
        adapter.notifyDataSetChanged();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    mFirebaseUser = user;
                    //Toast.makeText(PedirCarona.this, "User: " + user, Toast.LENGTH_SHORT).show();
                }else{
                    //Toast.makeText(PedirCarona.this, "User null", Toast.LENGTH_SHORT).show();
                }
            }
        };

       /* criarCaronaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getInstance().getCurrentUser();
                horas = horarioPicker.getCurrentHour();
                minutos = horarioPicker.getCurrentMinute();
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    if(!locaisPartida.getSelectedItem().toString().equals(locaisDestino.getSelectedItem().toString())){
                        if (user != null) {
                            mCriarCaronaProgress.show();
                            String idCarona = caronaRef.push().getKey();
                            caronaRef.child(idCarona).child("nome").setValue(user.getDisplayName());
                            caronaRef.child(idCarona).child("idusuario").setValue(user.getUid());
                            caronaRef.child(idCarona).child("img").setValue(user.getPhotoUrl());
                            preencherListCarona(idCarona);
                            caronaRef.child(idCarona).child("partida").setValue(locaisPartida.getSelectedItem().toString());
                            caronaRef.child(idCarona).child("destino").setValue(locaisDestino.getSelectedItem().toString());
                            caronaRef.child(idCarona).child("vagas").setValue(numVagas);
                            caronaRef.child(idCarona).child("data").setValue(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()));
                            if (horas >= 0 && horas < 10) {
                                if (minutos > 0 && minutos < 10)
                                    caronaRef.child(idCarona).child("horario").setValue("0" + horas + ":0" + minutos);
                            } else {
                                if (minutos > 0 && minutos < 10)
                                    caronaRef.child(idCarona).child("horario").setValue(horas + ":0" + minutos);
                                else
                                    caronaRef.child(idCarona).child("horario").setValue(horas + ":" + minutos);
                            }
                            caronaRef.child(idCarona).child("situacao").setValue("ativa").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //ListaCaronasFragmento.criarCaronaBtn.setText("@string/editar_carona");
                                        mCriarCaronaProgress.dismiss();
                                        startActivity(new Intent(EditarCarona.this, HomeTabbed.class));
                                        finish();
                                    }
                                }
                            });
                        }
                    }else{
                        Toast.makeText(EditarCarona.this, "Os locais de partida e destino não podem ser iguais.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    mCriarCaronaProgress.hide();
                    Toast.makeText(EditarCarona.this, "Sem conexão com a Internet.", Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        locaisPartida.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //if(locaisPartida.getSelectedItem().equals("CAP"))
                //locaisDestino.setAdapter(locaisPartidaCapAdapter);
                //else
                //     locaisDestino.setAdapter(capAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        locaisDestino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //if(locaisDestino.getSelectedItem().equals("CAP"))
                //    locaisPartida.setAdapter(locaisPartidaCapAdapter);
                //else
                //    locaisPartida.setAdapter(capAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        enviarProposta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    /*private void preencherListCarona(String idCarona){
        if(vagas.getSelectedItem().toString().equals("4")) {
            caronaRef.child(idCarona).child("idcaroneiro1").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro1").setValue("ASSENTO VAZIO");
            caronaRef.child(idCarona).child("imgcaroneiro1").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro2").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro2").setValue("ASSENTO VAZIO");
            caronaRef.child(idCarona).child("imgcaroneiro2").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro3").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro3").setValue("ASSENTO VAZIO");
            caronaRef.child(idCarona).child("imgcaroneiro3").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro4").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro4").setValue("ASSENTO VAZIO");
            caronaRef.child(idCarona).child("imgcaroneiro4").setValue("");
        }
        else if(vagas.getSelectedItem().toString().equals("3")){
            caronaRef.child(idCarona).child("idcaroneiro1").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro1").setValue("ASSENTO VAZIO");
            caronaRef.child(idCarona).child("imgcaroneiro1").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro2").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro2").setValue("ASSENTO VAZIO");
            caronaRef.child(idCarona).child("imgcaroneiro2").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro3").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro3").setValue("ASSENTO VAZIO");
            caronaRef.child(idCarona).child("imgcaroneiro3").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro4").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro4").setValue("Ocupado");
            caronaRef.child(idCarona).child("imgcaroneiro4").setValue("");
        }
        else if(vagas.getSelectedItem().toString().equals("2")){
            caronaRef.child(idCarona).child("idcaroneiro1").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro1").setValue("ASSENTO VAZIO");
            caronaRef.child(idCarona).child("imgcaroneiro1").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro2").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro2").setValue("ASSENTO VAZIO");
            caronaRef.child(idCarona).child("imgcaroneiro2").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro3").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro3").setValue("Ocupado");
            caronaRef.child(idCarona).child("imgcaroneiro3").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro4").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro4").setValue("Ocupado");
            caronaRef.child(idCarona).child("imgcaroneiro4").setValue("");
        }
        else if(vagas.getSelectedItem().toString().equals("1")){
            caronaRef.child(idCarona).child("idcaroneiro1").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro1").setValue("ASSENTO VAZIO");
            caronaRef.child(idCarona).child("imgcaroneiro1").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro2").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro2").setValue("Ocupado");
            caronaRef.child(idCarona).child("imgcaroneiro2").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro3").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro3").setValue("Ocupado");
            caronaRef.child(idCarona).child("imgcaroneiro3").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro4").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro4").setValue("Ocupado");
            caronaRef.child(idCarona).child("imgcaroneiro4").setValue("");
        }
        else if(vagas.getSelectedItem().toString().equals("0")){
            caronaRef.child(idCarona).child("idcaroneiro1").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro1").setValue("Ocupado");
            caronaRef.child(idCarona).child("imgcaroneiro1").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro2").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro2").setValue("Ocupado");
            caronaRef.child(idCarona).child("imgcaroneiro2").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro3").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro3").setValue("Ocupado");
            caronaRef.child(idCarona).child("imgcaroneiro3").setValue("");
            caronaRef.child(idCarona).child("idcaroneiro4").setValue("");
            caronaRef.child(idCarona).child("nomecaroneiro4").setValue("Ocupado");
            caronaRef.child(idCarona).child("imgcaroneiro4").setValue("");
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //listener for home
        if(item.getItemId()==android.R.id.home)
        {
            //salvar
            Toast.makeText(EditarCarona.this, "Voltar pressionado.", Toast.LENGTH_SHORT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

