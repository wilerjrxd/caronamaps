package com.dragoonssoft.apps.caronacap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Cadastro extends AppCompatActivity {
    private TextInputLayout mMatricula, mTelefone;
    private static Button criarUsuarioBtn;
    private static AlertDialog.Builder alert_builder, alert_builder2, alert_builder3;
    private static AlertDialog alert, alert2, alert3;
    private static ProgressDialog mCadastroProgress;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser firebaseUser = null;
    private FirebaseDatabase db;
    private DatabaseReference usuarioRef;
    private FirebaseUser mFirebaseUser = null;
    private Toolbar cadastroToolbar;
    private TextView finalizarText;
    private Boolean isUsuarioCadastrado = false;
    private String facebookProfilePicUrl, facebookUserId, deviceTokenId;
    private TextView concordanciaPolitica;
    private String googleUserId;
    private String profilePicUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        //mMatricula = (TextInputLayout)findViewById(R.id.matricula);
        mTelefone = (TextInputLayout)findViewById(R.id.telefone);
        criarUsuarioBtn = (Button)findViewById(R.id.cadastrarBtn);
        finalizarText = (TextView)findViewById(R.id.finalizarText);
        cadastroToolbar = (Toolbar)findViewById(R.id.cadastro_toolbar);
        concordanciaPolitica = (TextView)findViewById(R.id.textView2);

        mCadastroProgress = new ProgressDialog(this);
        mCadastroProgress.setMessage("Cadastrando usuário...");
        mCadastroProgress.setCanceledOnTouchOutside(false);
        mCadastroProgress.setCancelable(false);

        setSupportActionBar(cadastroToolbar);
        getSupportActionBar().setTitle("Cadastro");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        deviceTokenId = getIntent().getStringExtra("deviceTokenId");

        alert_builder = new AlertDialog.Builder(Cadastro.this);
        alert_builder.setMessage("Senha e Confirmar Senha não conferem.")
                .setCancelable(true);
        alert = alert_builder.create();
        alert.setTitle("Senhas não conferem");

        alert_builder2 = new AlertDialog.Builder(Cadastro.this);
        alert_builder2.setMessage("Por favor, forneça um número de telefone válido para finalizar seu cadastro.")
                .setCancelable(true);
        alert2 = alert_builder2.create();
        alert2.setTitle("Campo vazio");

        alert_builder3 = new AlertDialog.Builder(Cadastro.this);
        alert_builder3.setMessage("Sua senha deve ter no mínimo 6 dígitos.")
                .setCancelable(true);
        alert3 = alert_builder3.create();
        alert3.setTitle("Senha com menos de 6 dígitos");

        mAuth = FirebaseAuth.getInstance();
        usuarioRef = db.getInstance().getReference().child("usuarios");

        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    mFirebaseUser = user;
                    finalizarText.setText("Finalize seu cadastro, " + mAuth.getInstance().getCurrentUser().getDisplayName()+".");
                    for(UserInfo profile : mFirebaseUser.getProviderData()){
                        if(profile.getProviderId().equals("facebook.com")){
                            facebookUserId = profile.getUid();
                            profilePicUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=300";
                        }else if(profile.getProviderId().equals("google.com")){
                            googleUserId = profile.getUid();
                            profilePicUrl = Login.personPhotoUrl.replace("s96", "s300");//"https://plus.google.com/s2/photos/profile/" + googleUserId + "?sz=300";

                        }
                    }
                    facebookProfilePicUrl = profilePicUrl;
                }else{
                    Toast.makeText(Cadastro.this, "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        //<string name="fornecimento_telefone">O fornecimento do seu número de telefone celular é exclusivamente para que os usuários possam entrar em contato com você. Se quiser que os usuários do aplicativo não possam realizar ligações para você, essa opção pode ser ativada em suas configurações de perfil.</string>

        criarUsuarioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTelefone.getEditText().getText().length() != 0) {
                    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        FirebaseUser user = mAuth.getInstance().getCurrentUser();
                        if(user != null) {''
                            mCadastroProgress.show();
                            Map usuarioInfo = new HashMap();
                            String id = mAuth.getInstance().getCurrentUser().getUid();
                            //Usuario usuario = new Usuario(user.getUid(),user.getDisplayName(),matricula.getText().toString(),user.getPhotoUrl());
                           // usuarioRef.child(user.getUid()).setValue(user.getUid());
                            usuarioInfo.put("device_token", deviceTokenId);
                            usuarioInfo.put("nome", user.getDisplayName());
                            //usuarioInfo.put("matricula", mMatricula.getEditText().getText().toString());
                            //if(mTelefone.getEditText().getText().toString() == null || mTelefone.getEditText().getText().toString().equals("")){
                                usuarioInfo.put("telefone", "+55" + mTelefone.getEditText().getText().toString());
                            //}
                            //else
                            //    usuarioRef.child(id).child("telefone").setValue("");
                            usuarioInfo.put("imgURL", facebookProfilePicUrl);
                            usuarioInfo.put("status", "idle");
                            //usuarioRef.child(id).child("moral").setValue("0");
                            usuarioInfo.put("registrado_desde", "Registrado em " + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + " de " + getMonthName() + " de " + Calendar.getInstance().get(Calendar.YEAR));
                            usuarioInfo.put("cadastrado", "true");
                            usuarioInfo.put("recebe_ligacoes", "true");
                            usuarioInfo.put("conversando_com", "ninguem");
                            usuarioInfo.put("online", true);
                            usuarioRef.child(id).setValue(usuarioInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent toHome = new Intent(Cadastro.this, HomeTabbed.class);
                                        toHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        mCadastroProgress.dismiss();
                                        startActivity(toHome);
                                        finish();
                                    }
                                }
                            });


                        }
                    } else {
                        mCadastroProgress.hide();
                        Toast.makeText(Cadastro.this, "Sem conexão com a Internet.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    alert2.show();
                }
            }
        });

        concordanciaPolitica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dragoonssoft.wixsite.com/caronacap/politica-de-privacidade"));
                startActivity(browserIntent);
            }
        });

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private String getMonthName() {
        int monthNum = Calendar.getInstance().get(Calendar.MONTH);

        if(monthNum == 0){
            return "janeiro";
        }else if(monthNum == 1){
            return "fevereiro";
        }else if(monthNum == 2){
            return "março";
        }else if(monthNum == 3){
            return "abril";
        }else if(monthNum == 4){
            return "maio";
        }else if(monthNum == 5){
            return "junho";
        }else if(monthNum == 6){
            return "julho";
        }else if(monthNum == 7){
            return "agosto";
        }else if(monthNum == 8){
            return "setembro";
        }else if(monthNum == 9){
            return "outubro";
        }else if(monthNum == 10){
            return "novembro";
        }else if(monthNum == 11){
            return "dezembro";
        }else {
            return null;
        }
    }

    private void showData(DataSnapshot dataSnapshot) {
        /*String keyValue = null;
        //Iterable<DataSnapshot> children = dataSnapshot.getChildren();
        for(DataSnapshot ds : dataSnapshot.getChildren()) {
            if(ds.exists()) {
                //Usuario usuarioInfo = ds.getValue(Usuario.class);
                keyValue = ds.getKey();
                if (keyValue.equals(mFirebaseUser.getUid())) {
                    isUsuarioCadastrado = true;
                    break;
                }
            } else {
                Toast.makeText(Cadastro.this, "Usuário não existe.", Toast.LENGTH_SHORT).show();
            }
        }

        if(isUsuarioCadastrado) {
            Toast.makeText(Cadastro.this, "Usuário cadastrado.", Toast.LENGTH_SHORT).show();
            Intent toHome = new Intent(Cadastro.this, HomeTabbed.class);
            toHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(toHome);
            finish();
        }*/
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
        mCadastroProgress.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseUser = mAuth.getCurrentUser();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mCadastroProgress.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == android.R.id.home){
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null){
                usuarioRef.child(firebaseUser.getUid()).removeValue();
                firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            finish();
                            //super.onBackPressed();
                        }
                    }
                });
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null){
                usuarioRef.child(firebaseUser.getUid()).removeValue();
                FirebaseAuth.getInstance().signOut();
                firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            finish();
                            //super.onBackPressed();
                        }
                    }
                });
            }
        }else{
            Toast.makeText(Cadastro.this, "Sem conexão com a Internet.", Toast.LENGTH_SHORT).show();
        }
        //
    }

}
