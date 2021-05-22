package com.dragoonssoft.apps.caronacap;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import androidx.core.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;
import static androidx.core.app.NotificationCompat.DEFAULT_VIBRATE;

public class MudarTelefone extends AppCompatActivity {

    private Toolbar mMudarTelefoneToolbar;
    private String telefoneUsuario, idUsuario;
    private TextInputLayout mTelefone;
    private Button salvarTelefoneBtn;
    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
    private DatabaseReference notificationsOffRef;
    private boolean onScreen = true;
    private DatabaseReference usuariosRef;
    private ProgressDialog mMudarTelefoneProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mudar_telefone);

        mMudarTelefoneToolbar = (Toolbar) findViewById(R.id.mudar_telefone_toolbar);
        setSupportActionBar(mMudarTelefoneToolbar);
        getSupportActionBar().setTitle("Mudar número de telefone");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //notificationsOffRef = FirebaseDatabase.getInstance().getReference().child("notifications_off");
        usuariosRef = FirebaseDatabase.getInstance().getReference().child("usuarios");

        idUsuario = getIntent().getStringExtra("idusuario_chat");
        telefoneUsuario = getIntent().getStringExtra("telefoneusuario_chat");

        mTelefone = (TextInputLayout) findViewById(R.id.telefone);
        salvarTelefoneBtn = (Button) findViewById(R.id.salvarTelBtn);

        mTelefone.getEditText().setText(telefoneUsuario);
        mTelefone.getEditText().setHint("Digite seu número de telefone");

        mMudarTelefoneProgress = new ProgressDialog(this);
        mMudarTelefoneProgress.setTitle("Alterando número de telefone");
        mMudarTelefoneProgress.setMessage("Aguarde enquanto seu número de telefone é alterado...");
        mMudarTelefoneProgress.setCanceledOnTouchOutside(false);
        mMudarTelefoneProgress.setCancelable(false);

        salvarTelefoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (mTelefone.getEditText().getText().toString().length() != 0) {
                        mMudarTelefoneProgress.show();
                        String numero = mTelefone.getEditText().getText().toString();
                        FirebaseDatabase.getInstance().getReference().child("usuarios").child(idUsuario).child("telefone").setValue(numero).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Perfil.telefoneGlobalParaMudarNum = numero;
                                    Perfil.telefonePerfilText.setText(numero);
                                    mMudarTelefoneProgress.hide();
                                    Toast.makeText(MudarTelefone.this, "Número alterado.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(MudarTelefone.this, "Formato de número de telefone inválido. Tente novamente.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MudarTelefone.this, "Você precisa estar conectado à Internet para fazer isso.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        onScreen = true;
        notificationsOffRef.goOnline();
        usuariosRef.goOnline();

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            usuariosRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true);
            usuariosRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("conversando_com").setValue("ninguem");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        onScreen = false;

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            usuariosRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(false);
        }
        //notificationsOffRef.goOffline();
        usuariosRef.goOffline();
    }

    @Override
    protected void onDestroy() {
        mMudarTelefoneProgress.dismiss();
        super.onDestroy();
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }*/
}
