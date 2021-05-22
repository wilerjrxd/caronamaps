package com.dragoonssoft.apps.caronacap;


import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class CriarEditarCaronaFragmento extends Fragment {
    private static Spinner locais, vagas;
    private static TextView partidaDestinoLabel, enviarProposta;
    private static int horas, minutos;
    private static TextView cap_ob, ob_cap;
    private static boolean obCAPChecked = false, capOBChecked = true;
    private static TimePicker horario;
    private static String dataCarona;
    private static Button criarCaronaBtn;
    private static AlertDialog.Builder alert_builder;
    private static AlertDialog alert;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase db;
    private DatabaseReference caronaRef;

    public CriarEditarCaronaFragmento() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_criar_editar_carona_fragmento, container, false);
        locais = (Spinner)view.findViewById(R.id.de_capSpinner);
        vagas = (Spinner)view.findViewById(R.id.vagasOBSpinner);
        horario = (TimePicker)view.findViewById(R.id.horarioParaOBPicker);
        criarCaronaBtn = (Button)view.findViewById(R.id.criarCaronaParaOBBtn);
        ob_cap = (TextView) view.findViewById(R.id.ob_cap);
        cap_ob = (TextView) view.findViewById(R.id.cap_ob);
        partidaDestinoLabel = (TextView) view.findViewById(R.id.partidaDestinoLabel);
        enviarProposta = (TextView)view.findViewById(R.id.enviarProposta);

        ArrayAdapter paraAdapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),R.array.locais, android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter vagasAdapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),R.array.vagas, android.R.layout.simple_spinner_dropdown_item);

        locais.setAdapter(paraAdapter);
        vagas.setAdapter(vagasAdapter);
        horario.setIs24HourView(true);

        alert_builder = new AlertDialog.Builder(getActivity().getApplicationContext());
        alert_builder.setMessage("Os lugares de partida e destino não podem ser iguais.")
                .setCancelable(true);
        alert = alert_builder.create();
        alert.setTitle("Erro");

        mAuth = FirebaseAuth.getInstance();
        caronaRef = db.getInstance().getReference().child("caronas");

        ob_cap.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(!obCAPChecked) {
                    obCAPChecked = true;
                    capOBChecked = false;
                    //ob_cap.setTextColor(getResources().getColorStateList(R.color.colorPrimary, null));
                    //ob_cap.setTextColor(getResources().getColor(R.color.colorPrimary,null));
                    ob_cap.setBackgroundResource(android.R.color.holo_blue_bright);
                    //cap_ob.setTextColor(getResources().getColor(R.color.colorPrimaryDark,null));
                    cap_ob.setBackgroundResource(android.R.color.holo_blue_dark);
                    //ob_cap.setImageResource(R.drawable.ob_cap_on);
                    //cap_ob.setImageResource(R.drawable.cap_ob_off);
                    partidaDestinoLabel.setText("Saída");
                }
            }
        });

        cap_ob.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(!capOBChecked) {
                    obCAPChecked = false;
                    capOBChecked = true;
                    //cap_ob.setTextColor(getResources().getColor(R.color.colorPrimary,null));
                    cap_ob.setBackgroundResource(android.R.color.holo_blue_bright);
                    //ob_cap.setTextColor(getResources().getColor(R.color.colorPrimaryDark,null));
                    ob_cap.setBackgroundResource(android.R.color.holo_blue_dark);
                    //ob_cap.setImageResource(R.drawable.ob_cap_off);
                    //cap_ob.setImageResource(R.drawable.cap_ob_on);
                    partidaDestinoLabel.setText("Destino");
                }
            }
        });

        criarCaronaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getInstance().getCurrentUser();
                horas = horario.getCurrentHour();
                minutos = horario.getCurrentMinute();
                if(obCAPChecked && !capOBChecked) {
                    if(user != null) {
                        String idCarona = caronaRef.push().getKey();
                        //Usuario usuario = new Usuario(user.getUid(),user.getDisplayName(),matricula.getText().toString(),user.getPhotoUrl());
                        //caronaRef.child(idCarona).setValue(idCarona);
                        caronaRef.child(idCarona).child("nome").setValue(Login.nomeGlobal);
                        caronaRef.child(idCarona).child("idusuario").setValue(Login.idGlobal);
                        caronaRef.child(idCarona).child("partida").setValue(locais.getSelectedItem().toString());
                        caronaRef.child(idCarona).child("destino").setValue("CAP");
                        caronaRef.child(idCarona).child("vagas").setValue(vagas.getSelectedItem().toString());
                        caronaRef.child(idCarona).child("data").setValue(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()));
                        if(horas >= 0 && horas < 10) {
                            if (minutos > 0 && minutos < 10) {
                                caronaRef.child(idCarona).child("horario").setValue("0" + String.valueOf(horario.getCurrentHour()) + ":0" + String.valueOf(horario.getCurrentMinute()));
                            }
                        }
                        else{
                            caronaRef.child(idCarona).child("horario").setValue(String.valueOf(horario.getCurrentHour()) + ":" + String.valueOf(horario.getCurrentMinute()));
                        }
                        caronaRef.child(idCarona).child("img").setValue(Login.fotoGlobal);
                        caronaRef.child(idCarona).child("situacao").setValue("ativa");
                        //ListaCaronasFragmento.criarCaronaBtn.setText("@string/editar_carona");

                        //startActivity(new Intent(CriarCarona.this, Home.class));
                        //setar foto do url com universal android image manager
                    }
                    // new MyTaskCAP().execute();
                }
                else if(capOBChecked && !obCAPChecked) {
                    if(user != null) {
                        String idCarona = caronaRef.push().getKey();
                        //Usuario usuario = new Usuario(user.getUid(),user.getDisplayName(),matricula.getText().toString(),user.getPhotoUrl());
                        //caronaRef.child(idCarona).setValue(idCarona);
                        caronaRef.child(idCarona).child("nome").setValue(Login.nomeGlobal);
                        caronaRef.child(idCarona).child("idusuario").setValue(Login.idGlobal);
                        caronaRef.child(idCarona).child("partida").setValue("CAP");
                        caronaRef.child(idCarona).child("destino").setValue(locais.getSelectedItem().toString());
                        caronaRef.child(idCarona).child("vagas").setValue(vagas.getSelectedItem().toString());
                        caronaRef.child(idCarona).child("data").setValue(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()));
                        if(horas >= 0 && horas < 10) {
                            if (minutos > 0 && minutos < 10) {
                                caronaRef.child(idCarona).child("horario").setValue("0" + String.valueOf(horario.getCurrentHour()) + ":0" + String.valueOf(horario.getCurrentMinute()));
                            }
                        }
                        else{
                            caronaRef.child(idCarona).child("horario").setValue(String.valueOf(horario.getCurrentHour()) + ":" + String.valueOf(horario.getCurrentMinute()));
                        }
                        caronaRef.child(idCarona).child("img").setValue(Login.fotoGlobal);
                        caronaRef.child(idCarona).child("situacao").setValue("ativa");
                        //ListaCaronasFragmento.criarCaronaBtn.setText("@string/editar_carona");

                        //startActivity(new Intent(getActivity().getApplicationContext(), Home.class));
                        //setar foto do url com universal android image manager
                    }
                    // new MyTaskOB().execute();
                }
                else
                    Toast.makeText(getActivity().getApplicationContext(), "NENHUMA CHECKED", Toast.LENGTH_LONG).show();
                //new MyTask().execute();
            }
        });

        enviarProposta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }

}
