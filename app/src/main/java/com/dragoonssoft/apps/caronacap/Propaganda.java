package com.dragoonssoft.apps.caronacap;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class Propaganda extends AppCompatActivity {

    private ImageButton fecharBtn;
    private ImageView propaganda;
    private DatabaseReference adRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_propaganda);

        adRef = FirebaseDatabase.getInstance().getReference().child("ad").child("ad");

        setupImageLoader();
        ImageLoader imageLoader = ImageLoader.getInstance();
        int defaultImage = getResources().getIdentifier("@drawable/ic_launcher",null,getPackageName());
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        String linkImg = getIntent().getStringExtra("link_img");
        String linkSaberMais = getIntent().getStringExtra("link_saber_mais");
        String clicks = getIntent().getStringExtra("clicks");
        String idAdText = getIntent().getStringExtra("id_ad");

        fecharBtn = findViewById(R.id.fecharBtn);
        propaganda = findViewById(R.id.propaganda);

        imageLoader.displayImage(linkImg, propaganda, options);

        fecharBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        propaganda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clicksInt = Integer.parseInt(clicks);
                clicksInt++;
                adRef.child(idAdText).child("clicks").setValue(clicksInt);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkSaberMais));
                startActivity(browserIntent);
            }
        });

    }

    private void setupImageLoader(){
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                Propaganda.this)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100*1024*1024).build();

        ImageLoader.getInstance().init(config);
    }
}
