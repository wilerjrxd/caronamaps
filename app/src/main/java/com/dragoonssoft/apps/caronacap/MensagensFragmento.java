package com.dragoonssoft.apps.caronacap;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.core.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MensagensFragmento extends Fragment {

    private ListView chatList;
    //private ListView chatList;
    private DatabaseReference conversasRef, usuariosRef;
    private FirebaseAuth mAuth;
    private Query orderConversasPorHorario;
    private String idUsuarioCorrente;//, nome, imgUrl, telefone;
    private TextView nenhumResultadoEncontrado;
    //private ConnectivityManager connMgr;
    //private NetworkInfo networkInfo;
    //private String id_outro_usuario;
    private String id_usuario_field;
    static ArrayList<ChatUsuarios> chatArrayList;
    private ConversaListAdapter adapter;
    private String nomeUsuarioQuePediu, imgUsuarioQuePediu, telefoneUsuarioQuePediu;
    private int counter = 0;
    private int contador = 0;
    private CountDownTimer carregarMensagensTimer;
    private ValueEventListener listener;
    private boolean visto;
    private String horario;
    private String idOutroUsuario;
    private ValueEventListener carregarChatUsuariosListener;
    static boolean mensagensJaCarregadas = false;
    private ValueEventListener usuariosListener;
    private ProgressDialog mCarregandoCaronaCapProgress;
    private  ImageLoader imageLoader;
    private  DisplayImageOptions options;
    private  static int defaultImage;
    private AlertDialog removerAlert;
    private DatabaseReference mensagensRef;

    public MensagensFragmento() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.mensagens_fragmento, container, false);

        setHasOptionsMenu(true);

        chatList = (ListView) view.findViewById(R.id.chatList);
        //chatList = (ListView)view.findViewById(R.id.chatList);
        nenhumResultadoEncontrado = (TextView) view.findViewById(R.id.nenhum_resultado);

        mAuth = FirebaseAuth.getInstance();
        idUsuarioCorrente = mAuth.getCurrentUser().getUid();
        conversasRef = FirebaseDatabase.getInstance().getReference().child("conversas").child(idUsuarioCorrente);
        mensagensRef = FirebaseDatabase.getInstance().getReference().child("mensagens").child(idUsuarioCorrente);
        orderConversasPorHorario = conversasRef.orderByChild("horarioinv");
        usuariosRef = FirebaseDatabase.getInstance().getReference().child("usuarios");

        imageLoader = ImageLoader.getInstance();
        defaultImage = getActivity().getApplicationContext().getResources().getIdentifier("@drawable/ic_launcher", null, getActivity().getApplicationContext().getPackageName());
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        setupImageLoader();

        FirebaseListAdapter<ChatUsuarios> firebaseListAdapter = new FirebaseListAdapter<ChatUsuarios>(getActivity(), ChatUsuarios.class, R.layout.item_user_listing, conversasRef.orderByChild("horarioinv")) {
            @Override
            protected void populateView(View view, ChatUsuarios chatUsuarios, int position) {
                TextView nomeImageChatItemList = (TextView) view.findViewById(R.id.nomeChatItemList);
                ImageView profileImageChatItemList = (ImageView) view.findViewById(R.id.profileImageChatItemList);
                ImageView vistoSignChatItemList = (ImageView) view.findViewById(R.id.msgNaoVistaChatItemList);
                TextView horarioChatItemList = (TextView) view.findViewById(R.id.horarioChatItemLis);

                final String id_outro_usuario = getRef(position).getKey();
                final String nome = chatUsuarios.getNome();
                final String imgUrl = chatUsuarios.getImg();
                final String horario = chatUsuarios.getHorario();
                final String telefone = chatUsuarios.getTelefone();
                final boolean visto = chatUsuarios.getVisto();
                nomeImageChatItemList.setText(nome);
                imageLoader.displayImage(imgUrl, profileImageChatItemList, options);
                horarioChatItemList.setText(horario);
                nomeImageChatItemList.setVisibility(View.VISIBLE);
                profileImageChatItemList.setVisibility(View.VISIBLE);
                horarioChatItemList.setVisibility(View.VISIBLE);
                if(visto){
                    vistoSignChatItemList.setVisibility(View.INVISIBLE);
                }else{
                    vistoSignChatItemList.setVisibility(View.VISIBLE);
                }

                if(id_outro_usuario != null){
                    nenhumResultadoEncontrado.setVisibility(View.GONE);
                }else{
                    nenhumResultadoEncontrado.setVisibility(View.VISIBLE);
                }

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent abrirConversa = new Intent(getActivity().getApplicationContext(), Conversa.class);
                        abrirConversa.putExtra("idusuario_chat", id_outro_usuario);
                        abrirConversa.putExtra("nomeusuario_chat", nome);
                        abrirConversa.putExtra("imgusuario_chat", imgUrl);
                        abrirConversa.putExtra("telefoneusuario_chat", telefone);
                        startActivity(abrirConversa);
                    }


                });

                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setCancelable(true);
                        builder.setTitle("Conversa com " + nome);
                        builder.setItems(new CharSequence[]
                                        {"Abrir conversa", "Excluir conversa"},
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // The 'which' argument contains the index position
                                        // of the selected item
                                        switch (which) {
                                            case 0:
                                                Intent abrirConversa = new Intent(getActivity().getApplicationContext(), Conversa.class);
                                                abrirConversa.putExtra("idusuario_chat", id_outro_usuario);
                                                abrirConversa.putExtra("nomeusuario_chat", nome);
                                                abrirConversa.putExtra("imgusuario_chat", imgUrl);
                                                abrirConversa.putExtra("telefoneusuario_chat", telefone);
                                                startActivity(abrirConversa);
                                                break;
                                            case 1:
                                                AlertDialog.Builder builderRemover = new AlertDialog.Builder(getContext());
                                                builderRemover.setCancelable(true);
                                                builderRemover.setMessage("Deseja mesmo excluir sua conversa com " + nome + "?")
                                                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                conversasRef.child(id_outro_usuario).removeValue();
                                                                mensagensRef.child(id_outro_usuario).removeValue();
                                                            }
                                                        })
                                                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                removerAlert.hide();
                                                            }
                                                        });

                                                removerAlert = builderRemover.create();
                                                removerAlert.show();

                                                break;
                                        }
                                    }
                                });
                        builder.create().show();

                        return true;
                    }

                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        chatList.setAdapter(firebaseListAdapter);
        //chatList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        /*chatList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {
                // Capture total checked items
                final int checkedCount = list.getCheckedItemCount();
                // Set the CAB title according to total checked items
                mode.setTitle(checkedCount + " Selected");
                // Calls toggleSelection method from ListViewAdapter Class
                firebaseListAdapter.toggleSelection(position);
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        // Calls getSelectedIds method from ListViewAdapter Class
                        SparseBooleanArray selected = listviewadapter
                                .getSelectedIds();
                        // Captures all selected ids with a loop
                        for (int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                WorldPopulation selecteditem = listviewadapter
                                        .getItem(selected.keyAt(i));
                                // Remove selected items following the ids
                                listviewadapter.remove(selecteditem);
                            }
                        }
                        // Close CAB
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.activity_main, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub
                listviewadapter.removeSelection();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }
        });*/
        /*chatArrayList = new ArrayList<>();
        adapter = new ConversaListAdapter(getActivity().getApplicationContext(), R.layout.item_user_listing, chatArrayList);
        chatList.setAdapter(adapter);*/

        return view;
    }

    public static class ConversasViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView nomeImageChatItemList, horarioChatItemList;
        ImageView profileImageChatItemList;
        ImageView vistoSignChatItemList;

        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        public ConversasViewHolder(View itemView){
            super(itemView);
            
            mView = itemView;
            
        }

        public void setNome(String nome){
            nomeImageChatItemList = (TextView) itemView.findViewById(R.id.nomeChatItemList);
            nomeImageChatItemList.setText(nome);
        }

        public void setImg(String img){
            profileImageChatItemList = (ImageView) itemView.findViewById(R.id.profileImageChatItemList);
            imageLoader.displayImage(img, profileImageChatItemList, options);
        }

        public void setVisto(boolean visto){
            vistoSignChatItemList = (ImageView) itemView.findViewById(R.id.msgNaoVistaChatItemList);
            if(visto){
                vistoSignChatItemList.setVisibility(View.INVISIBLE);
            }else{
                vistoSignChatItemList.setVisibility(View.VISIBLE);
            }
        }

        public void setHorario(String horario){
            horarioChatItemList = (TextView) itemView.findViewById(R.id.horarioChatItemLis);
            horarioChatItemList.setText(horario);
        }



    }

    @Override
    public void onResume() {
        super.onResume();
        conversasRef.goOnline();
        usuariosRef.goOnline();
    }

    @Override
    public void onPause() {
        super.onPause();

        /*if(carregarChatUsuariosListener != null)
            orderConversasPorHorario.removeEventListener(carregarChatUsuariosListener);
        if(!chatArrayList.isEmpty()) {
            chatArrayList.clear();
            adapter.notifyDataSetChanged();
        }*/

        //usuariosRef.removeEventListener(usuariosListener);

        conversasRef.goOffline();
        usuariosRef.goOffline();

    }

    @Override
    public void onStart() {
        super.onStart();

        /*ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
            HomeTabbed.isUsuarioOnline = true;
        else
            HomeTabbed.isUsuarioOnline = false;*/

        FirebaseRecyclerAdapter<ChatUsuarios, ConversasViewHolder> conversasAdapter = new FirebaseRecyclerAdapter<ChatUsuarios, ConversasViewHolder>(
                ChatUsuarios.class,
                R.layout.item_user_listing,
                ConversasViewHolder.class,
                conversasRef.orderByChild("horarioinv")
        ) {
            @Override
            protected void populateViewHolder(ConversasViewHolder viewHolder, ChatUsuarios chatUsuario, int position) {
                final String id_outro_usuario = getRef(position).getKey();
                final String nome = chatUsuario.getNome();
                final String imgUrl = chatUsuario.getImg();
                final String horario = chatUsuario.getHorario();
                final String telefone = chatUsuario.getTelefone();
                final boolean visto = chatUsuario.getVisto();

                viewHolder.setNome(nome);
                viewHolder.setImg(imgUrl);
                viewHolder.setHorario(horario);
                viewHolder.setVisto(visto);

                if(id_outro_usuario != null){
                    nenhumResultadoEncontrado.setVisibility(View.GONE);
                }else{
                    nenhumResultadoEncontrado.setVisibility(View.VISIBLE);
                }

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent abrirConversa = new Intent(getActivity().getApplicationContext(), Conversa.class);
                        abrirConversa.putExtra("idusuario_chat", id_outro_usuario);
                        abrirConversa.putExtra("nomeusuario_chat", nome);
                        abrirConversa.putExtra("imgusuario_chat", imgUrl);
                        abrirConversa.putExtra("telefoneusuario_chat", telefone);
                        startActivity(abrirConversa);
                    }
                });

            }
        };
        //chatList.setAdapter(conversasAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*if(!chatArrayList.isEmpty()) {
            chatArrayList.clear();
            adapter.notifyDataSetChanged();
        }*/


        //usuariosRef.removeEventListener(usuariosListener);
//        if(carregarChatUsuariosListener != null)
  //          orderConversasPorHorario.removeEventListener(carregarChatUsuariosListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){

        inflater.inflate(R.menu.menu_home_tabbed, menu);
        MenuItem searchItem = menu.findItem(R.id.procurarItem);
        searchItem.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_settings) {
            Intent toPerfilIntent = new Intent(getActivity().getApplicationContext(), Perfil.class);
            Bundle extras = new Bundle();
            extras.putString("idusuario_chat", mAuth.getCurrentUser().getUid());
            extras.putString("idUsuarioCorrente", mAuth.getCurrentUser().getUid());
            toPerfilIntent.putExtras(extras);
            startActivity(toPerfilIntent);
        }else if(id == R.id.action_about){
            startActivity(new Intent(getActivity().getApplicationContext(), Sobre.class));
        }else if(id == R.id.action_privacy){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://dragoonssoft.wixsite.com/caronacap/politica-de-privacidade"));
            startActivity(browserIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        setRetainInstance(true);

    }

    private void setupImageLoader(){
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getActivity().getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100*1024*1024).build();
        ImageLoader.getInstance().init(config);
    }
}