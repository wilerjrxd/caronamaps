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
import android.support.design.widget.TabLayout;
import androidx.core.app.Fragment;
import androidx.core.app.FragmentManager;
import androidx.core.app.FragmentPagerAdapter;
import androidx.core.app.NotificationCompat;
import androidx.core.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeTabbed extends AppCompatActivity {

    /**
     * The {@link androidx.core.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link androidx.core.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private Toolbar mHomeToolbar;

    private AdView mAdView;
    static int fragmentIndexToSelect = 0;

    static NotificationCompat.Builder mBuilder;
    static Intent resultIntent;
    static PendingIntent resultPendingIntent;
    static NotificationManager mNotifyMgr;
    static boolean isUsuarioOnline = false;
    private AdRequest adRequest;
    private boolean isAdminLogado;
    private boolean onScreen = true;
    private boolean isNotificationAlreadySent = false;
    private DatabaseReference conversasRef, pedidosRef;
    private boolean isTextoPedidosMudado = false, isTextoChatMudado = false;
    static TabLayout tabLayout;
    private boolean temMsgsNaoVistas = false;
    private boolean temPedidos = false;
    private FirebaseAuth mAuth;
    private String tab;
    private int tabIndex;
    private int contadorDeClickOnBack = 0;
    static AlertDialog.Builder alert_builder;
    static AlertDialog alert;
    static ProgressDialog mHomeTabbedProgress;
    private CountDownTimer timer;
    private DatabaseReference usuarioRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_tabbed);

        mAuth = FirebaseAuth.getInstance();

        tab = getIntent().getStringExtra("tab");
        String abrir_update = getIntent().getStringExtra("update");
        String click_action = getIntent().getStringExtra("click_action");

        mHomeToolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(mHomeToolbar);
        getSupportActionBar().setTitle("caronaCAP");

        mAdView = (AdView) findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().build();
        /*if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals("Z2bNL808QPNlhue5etXpWwod7Ki2")
                || FirebaseAuth.getInstance().getCurrentUser().getUid().equals("Gqwzf627C0acZe6G48Ynqhtn7003")
                || FirebaseAuth.getInstance().getCurrentUser().getUid().equals("B2u82APfl7PihLpY6uqvldaQw863")){
            Toast.makeText(this, "Admin logado.", Toast.LENGTH_SHORT).show();
            mAdView.setVisibility(View.GONE);
        }else{
            mAdView.loadAd(adRequest);
            mAdView.setVisibility(View.VISIBLE);
        }*/
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        if(tab == null)
            mViewPager.setCurrentItem(fragmentIndexToSelect);
        else{
            tabIndex = Integer.parseInt(tab);
            mViewPager.setCurrentItem(tabIndex);
        }

        if(abrir_update != null){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.dragoonssoft.apps.caronacap"));
            startActivity(browserIntent);
        }

        alert_builder = new AlertDialog.Builder(this);

        if(mAuth.getCurrentUser() != null) {

            conversasRef = FirebaseDatabase.getInstance().getReference().child("conversas").child(mAuth.getCurrentUser().getUid());
            pedidosRef = FirebaseDatabase.getInstance().getReference().child("notifications_off").child(mAuth.getCurrentUser().getUid());
            usuarioRef = FirebaseDatabase.getInstance().getReference().child("usuarios").child(mAuth.getCurrentUser().getUid());

            conversasRef.orderByChild("visto").equalTo(false).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getChildren().iterator().hasNext()) {
                        tabLayout.getTabAt(1).setText(R.string.chat_1);
                    } else {
                        tabLayout.getTabAt(1).setText(R.string.chat);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            pedidosRef.orderByChild("type").equalTo("pedido").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot ds) {
                    if (ds.exists() && ds.getChildren().iterator().hasNext()) {
                        tabLayout.getTabAt(2).setText(R.string.pedidos_1);
                    } else {
                        tabLayout.getTabAt(2).setText(R.string.pedidos);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        /*FirebaseDatabase.getInstance().getReference().child("notifications_off").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(onScreen) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.hasChild("type")) {
                            //if(!isNotificationAlreadySent){
                                String keyValue = ds.getKey();
                                if (ds.child("type").getValue().toString().equals("pedido")) {
                                    //String from_nome = ds.child("from_nome").getValue().toString();
                                    //String from_id = ds.child("from_id").getValue().toString();
                                    //String from_img = ds.child("from_img").getValue().toString();
                                    //String from_telefone = ds.child("from_telefone").getValue().toString();
                                    //createAndOpenNotification("Mensagem", "Você recebeu uma nova mensagem", "msg", "", "", "", "", "1", keyValue);
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
                            //}
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


        
    }

    /*private void createAndOpenNotification(String title, String text, String type, String id, String nome, String imgUrl, String telefone, String tab, String keyValue){
        isNotificationAlreadySent = true;
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setOnlyAlertOnce(true)
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE);
                //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        //if(type.equals("msg")) {
        resultIntent = new Intent("caronacap.apps.dragoonssoft.com.caronacap_TARGET_NOTIFICATION");
        resultIntent.putExtra(tab, "tab");
        //HomeTabbed.resultIntent.putExtra(nome, "nomeusuario_chat");
        //HomeTabbed.resultIntent.putExtra(imgUrl, "imgusuario_chat");
        //HomeTabbed.resultIntent.putExtra(telefone, "telefoneusuario_chat");
        //}

        resultPendingIntent =
                PendingIntent.getActivity(this, 0, HomeTabbed.resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(HomeTabbed.resultPendingIntent);


        int mNotificationId = (int)System.currentTimeMillis();
        HomeTabbed.mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        HomeTabbed.mNotifyMgr.notify(mNotificationId, HomeTabbed.mBuilder.build());

        FirebaseDatabase.getInstance().getReference().child("notifications_off").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(keyValue).removeValue();

    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //super.onCreateOptionsMenu(menu);
        //getMenuInflater().inflate(R.menu.menu_home_tabbed,menu);
        //SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        /*MenuItem searchItem = menu.findItem(R.id.procurarItem);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //adapter.getFilter().filter(newText);
                return false;
            }
        });*/

        return true;
    }

    /*@Override
    public void onStart() {
        super.onStart();

        Toast.makeText(HomeTabbed.this, "Em Home", Toast.LENGTH_SHORT).show();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase.getInstance().getReference().child("usuarios").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true);
        }
    }*/

    /*@Override
    public void onStop() {
        super.onStop();
        Toast.makeText(this, "Saindo de home.", Toast.LENGTH_SHORT).show();
        //if(FirebaseAuth.getInstance().getCurrentUser() != null)
            FirebaseDatabase.getInstance().getReference().child("usuarios").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(false);
    }*/

    @Override
    public void onResume() {
        super.onResume();

        onScreen = true;

        if(alert_builder == null){
            alert_builder = new AlertDialog.Builder(this);
        }

        //if (FirebaseAuth.getInstance().getCurrentUser() != null) {

        //}
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent toLogin = new Intent(HomeTabbed.this, Login.class);
            toLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(toLogin);

        }else{
            if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals("Z2bNL808QPNlhue5etXpWwod7Ki2")
                    || FirebaseAuth.getInstance().getCurrentUser().getUid().equals("Gqwzf627C0acZe6G48Ynqhtn7003")
                    || FirebaseAuth.getInstance().getCurrentUser().getUid().equals("B2u82APfl7PihLpY6uqvldaQw863")
                    || FirebaseAuth.getInstance().getCurrentUser().getUid().equals("FTHWfAPe0GZW7B09ZINr0zbQO7z2")){

                isAdminLogado = true;
            }else{
                isAdminLogado = false;
            }

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()) {
                FirebaseDatabase.getInstance().getReference().child("usuarios").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true);
                FirebaseDatabase.getInstance().getReference().child("usuarios").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("conversando_com").setValue("ninguem");
                 if(!isAdminLogado){
                    //HomeTabbed.isUsuarioOnline = true;
                    if (mAdView != null) {
                        mAdView.loadAd(adRequest);
                        mAdView.setVisibility(View.VISIBLE);
                    }
                }else{
                    mAdView.setVisibility(View.GONE);
                }
            } else {
                mAdView.setVisibility(View.GONE);
                //HomeTabbed.isUsuarioOnline = false;
            }
        }            

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();FirebaseDatabase.getInstance().getReference().child("usuarios").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        onScreen = false;
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            FirebaseDatabase.getInstance().getReference().child("usuarios").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(false);
//            PedidosCaronasFragmento.notificationsOffRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeEventListener(PedidosCaronasFragmento.pedidosListener);
        }
    }

    @Override
    public void onBackPressed() {
        contadorDeClickOnBack++;
        if(contadorDeClickOnBack == 2) {
            super.onBackPressed();
        }else{
            Toast.makeText(this, "Aperte novamente para sair", Toast.LENGTH_SHORT).show();
            timer = new CountDownTimer(3000, 1000){

                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    contadorDeClickOnBack = 0;
                }
            }.start();
        }
    }

/*@Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_settings) {
            startActivity(new Intent(HomeTabbed.this, Perfil.class));
        }else if(id == R.id.action_about){
            startActivity(new Intent(HomeTabbed.this, Sobre.class));
        }
        //else if(id == R.id.procurarItem)
           // return true;

        return super.onOptionsItemSelected(item);
    }*/

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    ListaCaronasFragmento listaCaronasTab1 = new ListaCaronasFragmento();
                    return listaCaronasTab1;
                case 1:
                    MensagensFragmento mensagensTab2 = new MensagensFragmento();
                    return mensagensTab2;
                case 2:
                    PedidosCaronasFragmento pedidosTab3 = new PedidosCaronasFragmento();
                    return pedidosTab3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.caronas_tab);
                case 1:
                    return getResources().getString(R.string.msgs_tab);
                case 2:
                    return getResources().getString(R.string.pedidos_tab);
                default:
                    return null;
            }
        }
    }
}
