package com.alpokat.kasir.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.alpokat.kasir.R;
import com.bumptech.glide.Glide;

import im.delight.android.webview.AdvancedWebView;

public class Midtrans extends AppCompatActivity{

    private ImageView animasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midtrans);

        Intent intent = getIntent();
        String url1 = (intent.getStringExtra("url"));

        animasi = findViewById(R.id.imageLoading);
        Glide.with(this).load(R.drawable.loader).into(animasi);


        WebView wv = findViewById(R.id.webview);
        wv.setWebViewClient(new MyBrowser());

        wv.getSettings().setLoadsImagesAutomatically(true);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        wv.loadUrl(url1);

    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            animasi.setVisibility(View.VISIBLE);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            animasi.setVisibility(View.GONE);
            if(url.equals("https://toko.alpokat.com/resetCart/index.php")){
                finish();
            }
            super.onPageFinished(view, url);
        }


    }





}
