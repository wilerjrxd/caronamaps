package com.dragoonssoft.apps.caronacap;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.ads.AdView;

public class Home extends AppCompatActivity {

    private static MenuItem pedidoCaronaItem, configItem, msgItem, sobreItem, procurarItem;
    private static SearchView pedidoCaronaView, configView, msgView, sobreView, procurarView;
    static boolean caronaCriada = false;
    private static ListView caronaList;
    private static String idCarona, idUsuario, nome, sobrenome, horario, vagas, partida, destino, telefone, data;
    private CaronaInfo caronaVec, caronaVec2, caronaVec3, caronaVec4, caronaVec5, caronaVec6, caronaVec7, caronaVec8;
   // private ArrayList<CaronaInfo> caronaArrayList;
   // private CaronaListAdapter adapter;
    private boolean listaAtualizada = false;
    private static AdView mAdView;
    private SectionsPageAdapter sectionsPageAdapter;
    private ViewPager viewPager;

    private static ConnectivityManager connMgr;
    private static NetworkInfo networkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        //if(savedInstanceState == null){
         //   getSupportFragmentManager().beginTransaction().add(R.id.frame_container, new ListaCaronasFragmento()).commit();
        //}
    }

    private void setupViewPager(ViewPager viewPager){
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        //adapter.addFragment(new CriarEditarCaronaFragmento(), "CaronaInfo");
        adapter.addFragment(new ListaCaronasFragmento(), "Lista de Caronas");
       // adapter.addFragment(new MensagensFragmento(), "Mensagens");
       // adapter.addFragment(new PedidosCaronasFragmento(), "Pedidos");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_home,menu);
        //SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        MenuItem searchItem = menu.findItem(R.id.procurarItem);
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
        });

        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.requestFocus();

        //this.menu = menu;  // this will copy menu values to upper defined menu so that we can change icon later akash

        /*pedidoCaronaItem = menu.findItem(R.id.pedidoCaronaItem);
        pedidoCaronaView = (SearchView) MenuItemCompat.getActionView(configItem);
        configItem = menu.findItem(R.id.pedidoCaronaItem);
        configView = (SearchView) MenuItemCompat.getActionView(configItem);
        msgItem = menu.findItem(R.id.msgItem);
        msgView = (SearchView) MenuItemCompat.getActionView(msgItem);
        sobreItem = menu.findItem(R.id.sobreItem);
        sobreView = (SearchView) MenuItemCompat.getActionView(sobreItem);
        procurarItem = menu.findItem(R.id.procurarItem);
        procurarView = (SearchView) MenuItemCompat.getActionView(procurarItem);*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.homeItem){
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new ListaCaronasFragmento());
            return true;
        }
        if(id == R.id.pedidoCaronaItem){
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new PedidosCaronasFragmento());
            return true;
        }
        if(id == R.id.configItem){
        	//startActivity(new Intent(Home.this, Config.class));
            return true;
        }
        if(id == R.id.msgItem) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new MensagensFragmento());
            return true;
        }
        if(id == R.id.sobreItem)
            return true;
        if(id == R.id.procurarItem)
            return true;

        return super.onOptionsItemSelected(item);
    }

}
