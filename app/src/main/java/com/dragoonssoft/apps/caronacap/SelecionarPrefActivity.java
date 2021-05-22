package com.dragoonssoft.apps.caronacap;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SelecionarPrefActivity extends AppCompatActivity {

    private Spinner pref1Spinner, pref2Spinner, pref3Spinner, pref4Spinner;
    private ArrayAdapter<CharSequence> pref1Adapter, pref2Adapter, pref3Adapter, pref4Adapter;

    private String pref1, pref2, pref3, pref4;
    private TextView salvar;
    private DatabaseReference usuariosRef;
    private CheckBox receberNotifications;

    private ProgressDialog mSelecionarPrefProgress;

    private String preferencias, pref1SpinnerText, pref2SpinnerText, pref3SpinnerText, pref4SpinnerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_pref);

        mSelecionarPrefProgress = new ProgressDialog(this);
        mSelecionarPrefProgress.setMessage("Salvando alterações...");
        mSelecionarPrefProgress.setCancelable(false);

        pref1 = getIntent().getStringExtra("pref1");
        pref2 = getIntent().getStringExtra("pref2");
        pref3 = getIntent().getStringExtra("pref3");
        pref4 = getIntent().getStringExtra("pref4");

        usuariosRef = FirebaseDatabase.getInstance().getReference().child("usuarios");

        if(pref1.equals(getResources().getString(R.string.nenhum))){
            pref1 = "-";
        }
        if(pref2.equals(getResources().getString(R.string.nenhum))){
            pref2 = "-";
        }
        if(pref3.equals(getResources().getString(R.string.nenhum))){
            pref3 = "-";
        }
        if(pref4.equals(getResources().getString(R.string.nenhum))){
            pref4 = "-";
        }

        pref1Spinner = findViewById(R.id.pref1Spinner);
        pref1Adapter = ArrayAdapter.createFromResource(this,R.array.locais_paradas_full, android.R.layout.simple_spinner_dropdown_item);
        pref1Spinner.setAdapter(pref1Adapter);
        int pref1Position = pref1Adapter.getPosition(pref1);
        pref1Spinner.setSelection(pref1Position);

        pref2Spinner = findViewById(R.id.pref2Spinner);
        pref2Adapter = ArrayAdapter.createFromResource(this,R.array.locais_paradas_full, android.R.layout.simple_spinner_dropdown_item);
        pref2Spinner.setAdapter(pref2Adapter);
        int pref2Position = pref2Adapter.getPosition(pref2);
        pref2Spinner.setSelection(pref2Position);

        pref3Spinner = findViewById(R.id.pref3Spinner);
        pref3Adapter = ArrayAdapter.createFromResource(this,R.array.locais_paradas_full, android.R.layout.simple_spinner_dropdown_item);
        pref3Spinner.setAdapter(pref3Adapter);
        int pref3Position = pref3Adapter.getPosition(pref3);
        pref3Spinner.setSelection(pref3Position);

        pref4Spinner = findViewById(R.id.pref4Spinner);
        pref4Adapter = ArrayAdapter.createFromResource(this,R.array.locais_paradas_full, android.R.layout.simple_spinner_dropdown_item);
        pref4Spinner.setAdapter(pref4Adapter);
        int pref4Position = pref4Adapter.getPosition(pref4);
        pref4Spinner.setSelection(pref4Position);

        receberNotifications = findViewById(R.id.receberNotificationsCheckBox);

        if(ListaCaronasFragmento.receberNotificationsPrefGlobal != null){
            if(ListaCaronasFragmento.receberNotificationsPrefGlobal.equals("true")){
                receberNotifications.setChecked(true);
            }else{
                receberNotifications.setChecked(false);
            }
        }

        salvar = findViewById(R.id.salvar_pref);

        salvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelecionarPrefProgress.show();
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    pref1SpinnerText = pref1Spinner.getSelectedItem().toString();
                    pref2SpinnerText = pref2Spinner.getSelectedItem().toString();
                    pref3SpinnerText = pref3Spinner.getSelectedItem().toString();
                    pref4SpinnerText = pref4Spinner.getSelectedItem().toString();
                    if (pref1SpinnerText.equals("-")) {
                        pref1SpinnerText = getResources().getString(R.string.nenhum);
                    }
                    if (pref2SpinnerText.equals("-")) {
                        pref2SpinnerText = getResources().getString(R.string.nenhum);
                    }
                    if (pref3SpinnerText.equals("-")) {
                        pref3SpinnerText = getResources().getString(R.string.nenhum);
                    }
                    if (pref4SpinnerText.equals("-")) {
                        pref4SpinnerText = getResources().getString(R.string.nenhum);
                    }
                    Map prefMap = new HashMap();
                    preferencias = pref1SpinnerText + "_" + pref2SpinnerText + "-" + pref3SpinnerText + ";" + pref4SpinnerText;
                    prefMap.put("preferencias", preferencias);
                    if(receberNotifications.isChecked())
                        prefMap.put("receber_notifications_pref", "true");
                    else
                        prefMap.put("receber_notifications_pref", "false");
                    usuariosRef.child(userID).updateChildren(prefMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            ListaCaronasFragmento.preferenciasGlobal = preferencias;
                            PedidosCaronasFragmento.pref1Text.setText(pref1SpinnerText);
                            PedidosCaronasFragmento.pref2Text.setText(pref2SpinnerText);
                            PedidosCaronasFragmento.pref3Text.setText(pref3SpinnerText);
                            PedidosCaronasFragmento.pref4Text.setText(pref4SpinnerText);
                            Login.pref1 = pref1SpinnerText;
                            Login.pref2 = pref2SpinnerText;
                            Login.pref3 = pref3SpinnerText;
                            Login.pref4 = pref4SpinnerText;
                            if(receberNotifications.isChecked()){
                                ListaCaronasFragmento.receberNotificationsPrefGlobal = "true";
                            }else{
                                ListaCaronasFragmento.receberNotificationsPrefGlobal = "false";
                            }
                            mSelecionarPrefProgress.hide();
                            //if(receberNotifications.isChecked())
                            //    Toast.makeText(SelecionarPrefActivity.this, "Quando uma carona com suas preferências for criada, você será notificado(a).", Toast.LENGTH_SHORT).show();
                            //else
                            Toast.makeText(SelecionarPrefActivity.this, "Alterações salvas.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });/*.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });*/
                }else{
                    Toast.makeText(SelecionarPrefActivity.this, "Conecte-se à Internet para fazer isso.", Toast.LENGTH_SHORT).show();
                    mSelecionarPrefProgress.hide();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSelecionarPrefProgress.dismiss();
    }
}
