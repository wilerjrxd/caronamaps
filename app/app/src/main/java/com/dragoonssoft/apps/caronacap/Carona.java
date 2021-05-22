package com.dragoonssoft.apps.caronacap;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by wiler on 22/07/2017.
 */

public class Carona extends MultiDexApplication {

    private String idUsuarioCorrente;
    private DatabaseReference usuarioRef;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {

            idUsuarioCorrente = FirebaseAuth.getInstance().getCurrentUser().getUid();
            usuarioRef = FirebaseDatabase.getInstance().getReference().child("usuarios").child(idUsuarioCorrente);

            usuarioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot != null) {
                        ////////////////////////////////////////////////usuarioRef.child("online").onDisconnect().setValue(false);




                        //usuarioRef.child("online").setValue(true);
                        usuarioRef.child("online").onDisconnect().setValue(false);
                        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                        if (networkInfo != null && networkInfo.isConnected()) {
                            //usuarioRef.child("online").setValue(true);
                        }else{
                            //usuarioRef.child("online").setValue(false);
                        }
                    }else{
                        Toast.makeText(Carona.this, "DataSnapshot vazio.", Toast.LENGTH_SHORT).show();
                        //usuarioRef.child("online").onDisconnect().setValue(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
}
