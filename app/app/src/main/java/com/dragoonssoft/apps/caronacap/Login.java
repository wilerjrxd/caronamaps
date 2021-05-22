package com.dragoonssoft.apps.caronacap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener{
    //private static final String TAG = "Login";
    private static LoginButton entrarBtn;
    private static TextView criarContaTxt;
    private static boolean countDifUm = false;
    private static EditText usuario, senha;
    private AlertDialog.Builder alert_builder, alert_builder2, alert_builder3;
    private AlertDialog alert, alert2, alert3;
    static String idGlobal,nomeGlobal, sobrenomeGlobal,matriculaGlobal,senhaGlobal,telefoneGlobal,emailGlobal,carroGlobal,dataGlobal, fotoGlobal;
    private static ConnectivityManager connMgr;
    private static NetworkInfo networkInfo;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    static FirebaseUser mFirebaseUser = null;
    //private FirebaseDatabase db;
    //private DatabaseReference dbRef;
    private ProgressBar progressBar;
    private FirebaseDatabase db;
    private DatabaseReference usuarioRef;
    private CallbackManager callbackManager;
    private Query orderUsuariosPorId;
    private String deviceToken;
    private DataSnapshot ds, ds2;
    private ProgressDialog mCheckingAuthProgress;
    private boolean onScreen = true;
    private boolean entrou = false;
    private DatabaseReference conversasRef;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private SignInButton btnSignIn;
    static String personPhotoUrl;
    private long horarioinvremodelado;
    private String tab;
    public static String pref1, pref2, pref3, pref4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //usuario = (EditText)findViewById(R.id.usuario);
        //senha = (EditText)findViewById(R.id.senha);

        tab = getIntent().getStringExtra("tab");

        alert_builder = new AlertDialog.Builder(Login.this);
        alert_builder.setMessage("Dados incorretos ou usuário não cadastrado.")
                .setCancelable(true);
        alert = alert_builder.create();
        alert.setTitle("Erro");

        alert_builder2 = new AlertDialog.Builder(Login.this);
        alert_builder2.setMessage("Digite seu número de matrícula e sua senha para entrar.")
                .setCancelable(true);
        alert2 = alert_builder2.create();
        alert2.setTitle("Campos vazios");

        alert_builder3 = new AlertDialog.Builder(Login.this);
        alert_builder3.setMessage("Tente novamente.")
                .setCancelable(true);
        alert3 = alert_builder3.create();
        alert3.setTitle("Senha Incorreta");

        mCheckingAuthProgress = new ProgressDialog(this);
        mCheckingAuthProgress.setMessage("Entrando...");
        mCheckingAuthProgress.setCanceledOnTouchOutside(false);

        //TextView caronaTitle = (TextView) findViewById(R.id.textView3);
        //caronaTitle.setShadowLayer(16, 0, 0, Color.WHITE);

        callbackManager = CallbackManager.Factory.create();

        btnSignIn = (SignInButton) findViewById(R.id.signInButton);



        //btnSignIn.setSize(SignInButton.SIZE_STANDARD);
        //btnSignIn.setScopes(gso.getScopeArray());

        btnSignIn.setOnClickListener(this);

        entrarBtn = (LoginButton)findViewById(R.id.login_button);
        entrarBtn.setReadPermissions("email", "public_profile");//Arrays.asList("email"));
        entrarBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        mAuth = FirebaseAuth.getInstance();
        usuarioRef = db.getInstance().getReference().child("usuarios");
        //conversasRef = db.getInstance().getReference().child("conversas");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.google_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
       // orderUsuariosPorId = usuarioRef.orderByKey().equalTo(mAuth.getCurrentUser().getUid());

        /*conversasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast.makeText(Login.this, "Procurando conversas com falta de dados...", Toast.LENGTH_SHORT).show();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    conversasRef.child(ds.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds2 : dataSnapshot.getChildren()){
                                //if(!ds2.hasChild("nome")){
                                    usuarioRef.child(ds2.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String nome = dataSnapshot.child("nome").getValue().toString();
                                            String telefone = dataSnapshot.child("telefone").getValue().toString();
                                            String img = dataSnapshot.child("imgURL").getValue().toString();
                                            Map map = new HashMap();
                                            map.put("nome", nome);
                                            map.put("telefone", telefone);
                                            map.put("img", img);
                                            long horarioinv = (long)ds2.child("horarioinv").getValue();
                                            String horarioInvToString = String.valueOf(horarioinv);
                                            if(!horarioInvToString.startsWith("-2017")){
                                                if(horarioInvToString.length() == 13){
                                                    String dia = horarioInvToString.charAt(1) +""+ horarioInvToString.charAt(2);
                                                    String mes = horarioInvToString.charAt(3) +""+ horarioInvToString.charAt(4);
                                                    String ano = "2017";
                                                    String horaMinuto = horarioInvToString.charAt(9) +""+ horarioInvToString.charAt(10) +""+ horarioInvToString.charAt(11) +""+ horarioInvToString.charAt(12);
                                                    String horarioinvRemodelado = "-" +ano +""+ mes +""+ dia +""+ horaMinuto;
                                                    long horarioinvRemodeladoParaLong = Long.parseLong(horarioinvRemodelado);
                                                    //Toast.makeText(Login.this, "horarioinv tamanho: " + horarioInvToString.length() + " " + mes + " -- " + horarioinvRemodeladoParaLong, Toast.LENGTH_SHORT).show();
                                                    map.put("horarioinv", horarioinvRemodeladoParaLong);
                                                }else if(horarioInvToString.length() == 12){
                                                    String dia = horarioInvToString.charAt(1) +"";
                                                    String mes = horarioInvToString.charAt(2) +""+ horarioInvToString.charAt(3);
                                                    String ano = "2017";
                                                    String horaMinuto = horarioInvToString.charAt(8) +""+ horarioInvToString.charAt(9) +""+ horarioInvToString.charAt(10) +""+ horarioInvToString.charAt(11);
                                                    String horarioinvRemodelado = "-"+ano +""+ mes +"0"+ dia +""+ horaMinuto;
                                                    long horarioinvRemodeladoParaLong = Long.parseLong(horarioinvRemodelado);
                                                    //Toast.makeText(Login.this, "horarioinv tamanho: " + horarioInvToString.length() + " " + mes + " -- " + horarioinvRemodeladoParaLong, Toast.LENGTH_SHORT).show();
                                                    map.put("horarioinv", horarioinvRemodeladoParaLong);
                                                }
                                            }
                                            conversasRef.child(ds.getKey()).child(ds2.getKey()).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(Login.this, "Conversa com " + nome + " modificada.", Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Toast.makeText(Login.this, "Erro ao modificar conversa com " + nome + ".", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                //}


                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                Toast.makeText(Login.this, "Conversas verificadas e corrigidas.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
        /*usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(ds.exists()){
                        if(ds.child("status").getValue().toString().contains("motorista")){
                            Toast.makeText(Login.this, "Nome de quem é motorista: " + ds.child("nome").getValue().toString(), Toast.LENGTH_SHORT).show();
                            String telefone = ds.child("telefone").getValue().toString();
                            String telefoneCorrigido;
                            if(telefone.startsWith("031")){
                                telefoneCorrigido = telefone.replaceFirst("031","+55");
                                usuarioRef.child(ds.getKey()).child("telefone").setValue(telefoneCorrigido);
                            }
                        }else if(ds.child("status").getValue().toString().contains("aguardando")){
                            Toast.makeText(Login.this, "Nome de quem está aguardando: " + ds.child("nome").getValue().toString(), Toast.LENGTH_SHORT).show();
                            /*usuarioRef.child(ds.getKey()).child("telefone").setValue("");
                            Toast.makeText(Login.this, "Telefone verificado e corrigido.", Toast.LENGTH_SHORT).show();
                        }else if(ds.child("status").getValue().toString().contains("caroneiro")){
                            Toast.makeText(Login.this, "Nome de quem está caroneiro: " + ds.getKey(), Toast.LENGTH_SHORT).show();
                            
                            /*usuarioRef.child(ds.getKey()).child("telefone").setValue("");
                            Toast.makeText(Login.this, "Telefone verificado e corrigido.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                FirebaseUser user = mAuth.getCurrentUser();
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (user != null) {
                        mCheckingAuthProgress.show();
                        idGlobal = user.getUid();
                        nomeGlobal = user.getDisplayName();
                        fotoGlobal = user.getPhotoUrl().toString();
                        deviceToken = FirebaseInstanceId.getInstance().getToken();
                        //if(mAuth.getCurrentUser() != null){
                            usuarioRef.child(mAuth.getCurrentUser().getUid()).child("device_token").setValue(deviceToken);
                        //}
                        new CountDownTimer(2500, 500) {

                            public void onTick(long millisUntilFinished) {
                               // Toast.makeText(Login.this, idGlobal + " " + deviceToken, Toast.LENGTH_SHORT).show();
                            }

                            public void onFinish() {
                                checkIfUsuarioIsCadastradoAndChangeActivity(idGlobal);
                            }
                        }.start();
                        //checkIfUsuarioIsCadastradoAndChangeActivity(idGlobal);
                    }
                }else{
                    Toast.makeText(Login.this, "Sem conexão com a Internet.", Toast.LENGTH_SHORT).show();
                }
            }
        };
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

        mCheckingAuthProgress.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseUser = mAuth.getCurrentUser();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mCheckingAuthProgress.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);
        /////////////////////////////////////////////////////////////////// if(requestCode == RC_SIGN_IN)
        if (requestCode == RC_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                personPhotoUrl = account.getPhotoUrl().toString();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {

                }else{
                    Toast.makeText(this, "Sem conexão com a Internet.", Toast.LENGTH_SHORT).show();
                }
            }
        }else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    //usuarioRef.child(idUsuario).child("device_token").setValue(deviceToken);
                    //usuarioRef.child(idUsuario).child("device_token").setValue(accessToken.getToken());
                    //Toast.makeText(Login.this, "LOGADO COM SUCESSO! Access Token: " + deviceToken, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(Login.this, "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                    }else{
                        Toast.makeText(Login.this, "Sem conexão com a Internet.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                        } else {
                            // If sign in fails, display a message to the user.
                            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                            if (networkInfo != null && networkInfo.isConnected()) {
                                Toast.makeText(Login.this, "Algo deu errado. Tente novamente.", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(Login.this, "Sem conexão com a Internet.", Toast.LENGTH_SHORT).show();
                            }

                        }

                    }
                });
    }



    private void checkIfUsuarioIsCadastradoAndChangeActivity(String idUsuario){
        if(idUsuario != null) {
            usuarioRef.child(idUsuario).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!entrou) {
                        entrou = true;
                        if(!dataSnapshot.hasChild("receber_notifications_pref")){
                            usuarioRef.child(idUsuario).child("receber_notifications_pref").setValue("true");
                        }
                        if (dataSnapshot.hasChild("preferencias")) {
                            String preferenciasFromServer = dataSnapshot.child("preferencias").getValue().toString();
                            pref1 = preferenciasFromServer.split("_")[0];
                            String rest1 = preferenciasFromServer.split("_")[1];
                            pref2 = rest1.split("-")[0];
                            String rest2 = rest1.split("-")[1];
                            pref3 = rest2.split(";")[0];
                            pref4 = rest2.split(";")[1];
                        }
                        if (dataSnapshot.hasChild("cadastrado")) {
                            String isUsuarioCadastrado = dataSnapshot.child("cadastrado").getValue().toString();
                            usuarioRef.child(idUsuario).child("versao").setValue(getResources().getString(R.string.versao));
                            //if(!online) {
                            if (isUsuarioCadastrado.equals("true")) {
                                /*if (!dataSnapshot.child("device_token").getValue().toString().equals(deviceToken)) {
                                    if(deviceToken != null) {
                                        usuarioRef.child(idUsuario).child("device_token").setValue(deviceToken);
                                        Toast.makeText(Login.this, "Token ID atualizado." + deviceToken, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(Login.this, "Token ID não modificado.", Toast.LENGTH_SHORT).show();
                                }*/
                                usuarioRef.goOffline();
                                //Toast.makeText(Login.this, pref1 + " " + pref2 + " " + pref3 + " " + pref4, Toast.LENGTH_SHORT).show();
                                Intent toHome = new Intent(Login.this, HomeTabbed.class);
                                toHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                if(tab != null) {
                                    toHome.putExtra("tab", tab);
                                }
                                startActivity(toHome);
                                finish();
                            } else {
                                Intent toCadastro = new Intent(Login.this, Cadastro.class);
                                toCadastro.putExtra("deviceTokenId", deviceToken);
                                startActivity(toCadastro);
                                finish();
                            }
                        } else {
                            if (deviceToken != null) {
                                Intent toCadastro = new Intent(Login.this, Cadastro.class);
                                toCadastro.putExtra("deviceTokenId", deviceToken);
                                startActivity(toCadastro);
                                finish();
                            }
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else{
            Toast.makeText(this, "Usuário não encontrado.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        signIn();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
