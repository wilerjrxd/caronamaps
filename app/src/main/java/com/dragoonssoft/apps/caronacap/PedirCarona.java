package com.dragoonssoft.apps.caronacap;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PedirCarona extends AppCompatActivity {
    private Bundle extras;
    private ListView caronaDadosList;
    private ArrayList<CaronaInfo> caronaDadosArrayList = new ArrayList<>();
    private CaronaListMotoristaViewAdapter adapter;
    private Button fecharCaroneiro1Btn, fecharCaroneiro2Btn, fecharCaroneiro3Btn, fecharCaroneiro4Btn, msgCaroneiro1Btn, msgCaroneiro2Btn, msgCaroneiro3Btn, msgCaroneiro4Btn; //vai virar imgview
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase db;
    private Query encontrarIdUsuarioEmCarona;
    private Toolbar pedirCaronaToolbar;
    private FirebaseUser mFirebaseUser;
    private Button pedirCancelarSairCaronaBtn;
    private String partida, destino;
    private Query caronaIdUsuarioRef;
    private DatabaseReference caronaRef, allUsuariosRef, usuarioRef, pedidosRef;
    private Query caronaIdUsuarioParaModificarBtnRef;
    private ProgressDialog mLidarComCaronaProgress;
    private Boolean isAlreadyDone = false;
    private Boolean isCancelarBtnClicked = false;
    private Boolean isEmOutraCarona = false;
    private Boolean isFinished = false;
    private String idCarona, idMotoristaDeOutraCarona, nomeUsuario, idUsuario, imgUsuario, nomeCaroneiro1, imgCaroneiro1, idCaroneiro1, nomeCaroneiro2, imgCaroneiro2, idCaroneiro2, nomeCaroneiro3, imgCaroneiro3, idCaroneiro3, nomeCaroneiro4, imgCaroneiro4, idCaroneiro4, horario, vagas, partidaDestino;
    private Query caronaOrderByKeyRef;
    private int numVagas = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedir_carona);

        pedirCancelarSairCaronaBtn = (Button) findViewById(R.id.pedirCancelarSairCaronaBtn);
        pedirCaronaToolbar = (Toolbar)findViewById(R.id.pedir_carona_toolbar);
        setSupportActionBar(pedirCaronaToolbar);
        getSupportActionBar().setTitle("CaronaInfo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        idCarona = getIntent().getStringExtra("idcarona");
        /*motorista = extras.getString("nome");
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
        partidaDestino = extras.getString("partidaDestino");
        //android:layout_alignParentTop="true" da listview
        fecharCaroneiro1Btn = (Button)findViewById(R.id.tirarCaroneiro1Btn);
        fecharCaroneiro2Btn = (Button)findViewById(R.id.tirarCaroneiro2Btn);
        fecharCaroneiro3Btn = (Button)findViewById(R.id.tirarCaroneiro3Btn);
        fecharCaroneiro4Btn = (Button)findViewById(R.id.tirarCaroneiro4Btn);
        msgCaroneiro1Btn = (Button)findViewById(R.id.msg1Btn);
        msgCaroneiro2Btn = (Button)findViewById(R.id.msg2Btn);
        msgCaroneiro3Btn = (Button)findViewById(R.id.msg3Btn);
        msgCaroneiro4Btn = (Button)findViewById(R.id.msg4Btn);*/

        /*caronaDadosList = (ListView) findViewById(R.id.list_dados_carona);
        adapter = new CaronaListMotoristaViewAdapter(PedirCarona.this, R.layout.list_dados_carona_motorista_view_adapter,caronaDadosArrayList);
        caronaDadosList.setAdapter(adapter);

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
        caronaDados.setHorario(horario);
        caronaDados.setPartidaDestino(partidaDestino);

        caronaDadosArrayList.add(caronaDados);
        adapter.notifyDataSetChanged();*/

        mLidarComCaronaProgress = new ProgressDialog(this);
        mLidarComCaronaProgress.setMessage("Carregando dados da carona...");
        mLidarComCaronaProgress.setCanceledOnTouchOutside(false);
        mLidarComCaronaProgress.show();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    mFirebaseUser = user;
                   // Toast.makeText(PedirCarona.this, "User: " + user, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(PedirCarona.this, "User null", Toast.LENGTH_SHORT).show();
                }
            }
        };

        String userId = mAuth.getCurrentUser().getUid();
       // caronaIdUsuarioRef = db.getInstance().getReference().child("caronas").orderByChild("idusuario").equalTo(idMotorista);
        caronaIdUsuarioParaModificarBtnRef = db.getInstance().getReference().child("caronas").orderByChild("idusuario").equalTo(userId);
        caronaRef = db.getInstance().getReference().child("caronas").child(idCarona);
        caronaOrderByKeyRef = db.getInstance().getReference().child("caronas").orderByKey().equalTo(idCarona);
        allUsuariosRef = db.getInstance().getReference().child("usuarios");
        usuarioRef = allUsuariosRef.child(userId);
        pedidosRef = db.getInstance().getReference().child("pedidos");

        caronaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(isCancelarBtnClicked == false){
                    caronaDadosList = (ListView) findViewById(R.id.list_dados_carona);
                    adapter = new CaronaListMotoristaViewAdapter(PedirCarona.this, R.layout.list_dados_carona_motorista_view_adapter, caronaDadosArrayList);
                    caronaDadosList.setAdapter(adapter);

                    CaronaInfo caronaInfo = new CaronaInfo();//dataSnapshot.getValue(CaronaInfo.class);
                    if(dataSnapshot.exists()) {
                        idUsuario = dataSnapshot.child("idusuario").getValue().toString();
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
                        partidaDestino = dataSnapshot.child("partidaDestino").getValue().toString();
                        horario = dataSnapshot.child("horario").getValue().toString();
                        vagas = dataSnapshot.child("vagas").getValue().toString();

                        caronaInfo.setNome(nomeUsuario);
                        caronaInfo.setImg(imgUsuario);
                        caronaInfo.setNomecaroneiro1(nomeCaroneiro1);
                        caronaInfo.setImgcaroneiro1(imgCaroneiro1);
                        caronaInfo.setNomecaroneiro2(nomeCaroneiro2);
                        caronaInfo.setImgcaroneiro2(imgCaroneiro2);
                        caronaInfo.setNomecaroneiro3(nomeCaroneiro3);
                        caronaInfo.setImgcaroneiro3(imgCaroneiro3);
                        caronaInfo.setNomecaroneiro4(nomeCaroneiro4);
                        caronaInfo.setImgcaroneiro4(imgCaroneiro4);
                        caronaInfo.setHorario(horario);
                        caronaInfo.setPartidaDestino(partidaDestino);

                        caronaDadosArrayList.add(caronaInfo);
                        adapter.notifyDataSetChanged();

                        mLidarComCaronaProgress.hide();

                        Toast.makeText(PedirCarona.this, "CaronaInfo de " + nomeUsuario + " carregada!", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(PedirCarona.this, "CaronaInfo " + key + " de " + dataSnapshot.child("idusuario").getValue().toString() + " carregada!", Toast.LENGTH_SHORT).show();

                        doItWhenMotoristaIsUsuarioCorrenteOrNot(idUsuario, mAuth.getCurrentUser().getUid());
                        //Toast.makeText(PedirCarona.this, "DS MOTORISTA: " + dataSnapshot.child("nome").getValue().toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private boolean isUsuarioEmOutraCarona() {
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("status").getValue().toString().equals("idle")) {

                    isEmOutraCarona = false;

                }else{

                    isEmOutraCarona = true;

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return isEmOutraCarona;
    }

    private void doItWhenMotoristaIsUsuarioCorrenteOrNot(String idUsuario, String userId){
        if(!isAlreadyDone) {
            if (!idUsuario.equals(userId)) {
                getSupportActionBar().setTitle("CaronaInfo de " + nomeUsuario);
                Toast.makeText(PedirCarona.this, getSupportActionBar().getTitle(), Toast.LENGTH_SHORT).show();
                if (idCaroneiro1.equals(userId) || idCaroneiro2.equals(userId) || idCaroneiro3.equals(userId) || idCaroneiro4.equals(userId)) {
                    pedirCancelarSairCaronaBtn.setText(getResources().getString(R.string.sair_carona));
                } else {
                    if (vagas.contains("4") || vagas.contains("3") || vagas.contains("2") || vagas.contains("1")) {
                        if (!isUsuarioEmOutraCarona())
                            pedirCancelarSairCaronaBtn.setText(getResources().getString(R.string.pedir_carona));
                        else
                            pedirCancelarSairCaronaBtn.setVisibility(View.INVISIBLE);
                    }
                }
                isAlreadyDone = true;
            } else {
                getSupportActionBar().setTitle("Sua CaronaInfo");
                Toast.makeText(PedirCarona.this, "Sua CaronaInfo", Toast.LENGTH_SHORT).show();

                pedirCancelarSairCaronaBtn.setText(getResources().getString(R.string.cancelar_carona));

                isAlreadyDone = true;
            }

            pedirCancelarSairCaronaBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if(networkInfo != null && networkInfo.isConnected()) {
                        mLidarComCaronaProgress.setMessage("Cancelando sua carona...");
                        mLidarComCaronaProgress.show();

                        caronaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    if (!isCancelarBtnClicked) {
                                        String keyValue = ds.getKey();
                                        String nomeDoCaroneiro = null;

                                        if (pedirCancelarSairCaronaBtn.getText().equals(getResources().getString(R.string.sair_carona))) {
                                            isCancelarBtnClicked = true;
                                            if (ds.child("idcaroneiro1").equals(userId)) {

                                                nomeDoCaroneiro = ds.child("nomecaroneiro1").getValue().toString();
                                                caronaRef.child("idcaroneiro1").setValue("");
                                                caronaRef.child("nomecaroneiro1").setValue("ASSENTO VAZIO");
                                                caronaRef.child("imgcaroneiro1").setValue("");

                                            } else if (ds.child("idcaroneiro2").equals(userId)) {

                                                nomeDoCaroneiro = ds.child("nomecaroneiro2").getValue().toString();
                                                caronaRef.child("idcaroneiro2").setValue("");
                                                caronaRef.child("nomecaroneiro2").setValue("ASSENTO VAZIO");
                                                caronaRef.child("imgcaroneiro2").setValue("");

                                            } else if (ds.child("idcaroneiro3").equals(userId)) {

                                                nomeDoCaroneiro = ds.child("nomecaroneiro3").getValue().toString();
                                                caronaRef.child("idcaroneiro3").setValue("");
                                                caronaRef.child("nomecaroneiro3").setValue("ASSENTO VAZIO");
                                                caronaRef.child("imgcaroneiro3").setValue("");

                                            } else if (ds.child("idcaroneiro4").equals(userId)) {

                                                nomeDoCaroneiro = ds.child("nomecaroneiro4").getValue().toString();
                                                caronaRef.child("idcaroneiro4").setValue("");
                                                caronaRef.child("nomecaroneiro4").setValue("ASSENTO VAZIO");
                                                caronaRef.child("imgcaroneiro4").setValue("");

                                            }
                                            usuarioRef.child("status").setValue("idle");
                                            Toast.makeText(PedirCarona.this, nomeDoCaroneiro + " saiu.", Toast.LENGTH_SHORT).show();
                                            //enviar notificação para o motorista avisando que o caroneiro saiu
                                            //nomeDoCaroneiro + " saiu de sua carona."

                                            mLidarComCaronaProgress.setMessage("Saindo da carona...");
                                            mLidarComCaronaProgress.show();

                                            new CountDownTimer(2000, 1000) {

                                                public void onTick(long millisUntilFinished) {

                                                }

                                                public void onFinish() {
                                                    Toast.makeText(PedirCarona.this, "2 segundos passados.", Toast.LENGTH_SHORT).show();
                                                    mLidarComCaronaProgress.dismiss();
                                                    //Intent toHome = new Intent(PedirCarona.this, HomeTabbed.class);
                                                    //toHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    //startActivity(toHome);
                                                    finish();
                                                }
                                            }.start();

                                        } else if (pedirCancelarSairCaronaBtn.getText().equals(getResources().getString(R.string.cancelar_carona))) {
                                            isCancelarBtnClicked = true;
                                            dataSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    usuarioRef.child("status").setValue("idle");
                                                    ListaCaronasFragmento.criarCaronaBtn.setText("CRIAR CARONA");
                                                    //Toast.makeText(PedirCarona.this, "CaronaInfo cancelada", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            mLidarComCaronaProgress.setMessage("Cancelando sua carona...");
                                            mLidarComCaronaProgress.show();

                                            new CountDownTimer(2000, 1000) {

                                                public void onTick(long millisUntilFinished) {

                                                }

                                                public void onFinish() {
                                                    Toast.makeText(PedirCarona.this, "2 segundos passados.", Toast.LENGTH_SHORT).show();
                                                    mLidarComCaronaProgress.dismiss();
                                                    //Intent toHome = new Intent(PedirCarona.this, HomeTabbed.class);
                                                    //toHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    //startActivity(toHome);
                                                    finish();
                                                }
                                            }.start();

                                        } else if (pedirCancelarSairCaronaBtn.getText().equals(getResources().getString(R.string.pedir_carona))) {
                                            isCancelarBtnClicked = true;
                                            usuarioRef.child("status").setValue("aguardando_" + idUsuario);
                                            pedirCancelarSairCaronaBtn.setText(getResources().getString(R.string.cancelar_pedido));

                                            Toast.makeText(PedirCarona.this, "Pedindo carona...", Toast.LENGTH_SHORT).show();
                                        } else { //cancelar pedido de carona
                                            isCancelarBtnClicked = true;
                                            Toast.makeText(PedirCarona.this, pedidosRef.child(userId).child(idUsuario).getRef().toString(), Toast.LENGTH_SHORT).show();

                                            usuarioRef.child("status").setValue("idle");
                                            pedirCancelarSairCaronaBtn.setText(getResources().getString(R.string.pedir_carona));

                                        }
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }else{
                        Toast.makeText(PedirCarona.this, "Sem conexão com a Internet.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            Toast.makeText(this, "Usuário já verificado.", Toast.LENGTH_SHORT).show();
        }

    }

    private void removerPedidosSeCaronaCanceladaPorMotorista(String idUsuarioMotorista){
        allUsuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String keyValue = null;
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.hasChild("aguardando_" + idUsuarioMotorista) || ds.hasChild("caroneiro_" + idUsuarioMotorista)){
                        keyValue = ds.getKey();
                        Toast.makeText(PedirCarona.this, "Referência PEDIDO: " + ds.child(keyValue).getRef().toString(), Toast.LENGTH_SHORT).show();
                        //ds.child(keyValue).child("status").setValue("idle");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mFirebaseUser = mAuth.getCurrentUser();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseUser = mAuth.getCurrentUser();
    }
}
