package com.alpokat.toko.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alpokat.toko.Adapter.BelanjaAdapter;
import com.alpokat.toko.Adapter.SlidingPenjualanAdapter;
import com.alpokat.toko.Helper.DataHandler;
import com.alpokat.toko.Model.BelanjaModel;
import com.alpokat.toko.Model.realm.Keranjang;
import com.alpokat.toko.R;
import com.alpokat.toko.Setting.AppConfig;
import com.alpokat.toko.Setting.AppController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import app.beelabs.com.utilc.MoneyUtil;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

public class PenjualanActivity extends AppCompatActivity {

    @BindView(R.id.stl_tabs)
    TabLayout stlTabs;
    @SuppressLint("StaticFieldLeak")
    public static PenjualanActivity PA;
    private TextView total_belanja, jumlah_item;
    private ProgressDialog pDialog;
    private DataHandler db;
    private String id_toko;
    private LinearLayout daftar_belanja, daftar_barang;

    private BelanjaAdapter adapter;
    private List<BelanjaModel> belanja_list;
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjualan_lanscape);

        ButterKnife.bind(this);


        total_belanja = findViewById(R.id.total_belanja);
        jumlah_item = findViewById(R.id.jumlah_item);
        ImageButton batal = findViewById(R.id.batal);
        ImageButton selesai = findViewById(R.id.selesai);
        daftar_belanja = findViewById(R.id.daftar_belanja);
        daftar_barang = findViewById(R.id.daftar_barang);


        int orientation = this.getResources().getConfiguration().orientation;
        LinearLayout.LayoutParams param_belanja, param_barang;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            param_belanja = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    8.0f);
            param_barang = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    4.0f);
        } else {
            param_belanja = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    9.0f);
            param_barang = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    3.0f);
        }


        daftar_belanja.setLayoutParams(param_belanja);
        daftar_barang.setLayoutParams(param_barang);


        batal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PenjualanActivity.this);
                builder.setTitle("Peringatan");
                builder.setMessage("Anda yakin membatalkan belanja ini ?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        AppController.getDb().deleteRealm(Keranjang.class);

                        PenjualanActivity.PA.LoadTotalBelanja();
                        PenjualanActivity.PA.LoadKeranjang();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        selesai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), PembayaranActivity.class);
                i.putExtra(AppConfig.FROM_PAGE, AppConfig.PENJUALAN_ACTIVITY );
                i.putExtra("id_pelanggan", "0");
                i.putExtra("nama", "Pilih Pelanggan");
                startActivity(i);
            }
        });

        // Progress dialog
        pDialog = new ProgressDialog(PenjualanActivity.this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new DataHandler(getApplicationContext());
        HashMap<String, String> p = db.BacaKasir();
        id_toko = p.get("id_toko");


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(5);
        }


        PA = this;

        ViewPager mViewPager = findViewById(R.id.vp_tabs);
        mViewPager.setAdapter(new SlidingPenjualanAdapter(getSupportFragmentManager(), this));
        mViewPager.setClipToPadding(false);

        stlTabs.setupWithViewPager(mViewPager);

        LoadTotalBelanja();

        belanja_list = new ArrayList<>();
        adapter = new BelanjaAdapter(getApplicationContext(), belanja_list);

        recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager2 = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(mLayoutManager2);
        recyclerView.addItemDecoration(new PenjualanActivity.GridSpacingItemDecoration(1, dpToPx(), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        LoadKeranjang();
    }


    public void resetCart() {
        AppController.getDb().deleteRealm(Keranjang.class);
        finish();
    }

    public void LoadTotalBelanja() {
        RealmResults<Keranjang> keranjangList = AppController.getDb().getCollectionRealm(Keranjang.class);
        int total = 0;
        int jitem = 0;

        try {
            if (keranjangList.size() > 0) {
                for (Keranjang keranjang : keranjangList) {
                    total += keranjang.getTotal();
                    jitem += keranjang.getJumlah();

                }
            }

            total_belanja.setText(MoneyUtil.Companion.convertIDRCurrencyFormat((double) total, 0));
            jumlah_item.setText(jitem + "");

        } catch (Exception e) {
            resetCart();
            Log.e("Penjualan Barcode:", e.getMessage());
        }
    }

    public void LoadKeranjang() {
        belanja_list.clear();
        RealmResults<Keranjang> keranjangList = AppController.getDb().getCollectionRealm(Keranjang.class);
        for (Keranjang keranjang : keranjangList) {
            BelanjaModel daftar = new BelanjaModel();
            daftar.setId_produk(keranjang.getId_produk());
            daftar.setNama_produk(keranjang.getNama_produk());
            daftar.setJumlah(String.valueOf(keranjang.getJumlah()));
            daftar.setHarga(String.valueOf(keranjang.getHarga_jual()));
            daftar.setTotal(String.valueOf(keranjang.getTotal()));
            belanja_list.add(daftar);

        }

        adapter.notifyDataSetChanged();
        if (keranjangList.size() > 1) {
            recyclerView.smoothScrollToPosition(keranjangList.size() - 1);
        }
    }

    private int dpToPx() {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics()));
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}
