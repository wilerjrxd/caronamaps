package com.dragoonssoft.apps.caronacap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Sobre extends AppCompatActivity {

    private TextView enviarProposta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre);

        enviarProposta = findViewById(R.id.feedback);

        enviarProposta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent abrirConversa = new Intent(Sobre.this, Conversa.class);
                abrirConversa.putExtra("idusuario_chat", "Gqwzf627C0acZe6G48Ynqhtn7003");
                abrirConversa.putExtra("nomeusuario_chat", "Dragoons Soft");
                abrirConversa.putExtra("imgusuario_chat", "@drawable/ic_launcher");
                abrirConversa.putExtra("telefoneusuario_chat", "03131988723091");
                startActivity(abrirConversa);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference().child("usuarios").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true);
    }
}
