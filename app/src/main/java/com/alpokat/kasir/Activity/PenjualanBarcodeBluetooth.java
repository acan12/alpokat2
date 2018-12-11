package com.alpokat.kasir.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alpokat.kasir.Adapter.BelanjaAdapter;
import com.alpokat.kasir.Adapter.ProdukAdapter;
import com.alpokat.kasir.Adapter.SlidingPenjualanAdapter;
import com.alpokat.kasir.Fragment.BarangFragment;
import com.alpokat.kasir.Helper.SQLiteHandler;
import com.alpokat.kasir.Helper.SqlHelper;
import com.alpokat.kasir.Model.BelanjaModel;
import com.alpokat.kasir.Model.ProdukModel;
import com.alpokat.kasir.R;
import com.alpokat.kasir.Tab.SlidingTabLayout;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PenjualanBarcodeBluetooth extends AppCompatActivity {

    SQLiteHandler db;
    ListView listView;
    ArrayList<String> list_product = new ArrayList<>();
    ArrayAdapter<String> mAdapter;
    private SqlHelper dbcenter;

    public static PenjualanBarcodeBluetooth PA;
    private TextView total_belanja, jumlah_item;
    private ProgressDialog pDialog;

    private BelanjaAdapter adapter;
    private List<BelanjaModel> belanja_list;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjualan_barcode_bluetooth);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);


        db = new SQLiteHandler(this);
        final ArrayList<HashMap<String, String>> listProduct = db.GetProductForListView();

        listView = (ListView) findViewById(R.id.ListView);
        total_belanja = findViewById(R.id.total_belanja);
        jumlah_item = findViewById(R.id.jumlah_item);
        ImageButton batal = findViewById(R.id.batal);
        ImageButton selesai = findViewById(R.id.selesai);

        belanja_list = new ArrayList<>();
        adapter = new BelanjaAdapter(getApplicationContext(), belanja_list);


        int orientation = this.getResources().getConfiguration().orientation;
        LinearLayout.LayoutParams param_belanja,param_barang;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            param_belanja = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    8.0f);
            param_barang = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    4.0f);
        }else{
            param_belanja = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    9.0f);
            param_barang = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    3.0f);
        }

        PA = this;

        viewProduct();
        LoadTotalBelanja();

        belanja_list = new ArrayList<>();
        adapter = new BelanjaAdapter(getApplicationContext(), belanja_list);

        recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager2 = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(mLayoutManager2);
        recyclerView.addItemDecoration(new PenjualanBarcodeBluetooth.GridSpacingItemDecoration(1, dpToPx(), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        LoadKeranjang();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

//                System.out.println("========================================================= clay was here");
                String id_product = listProduct.get(i).values().toArray()[0].toString();
                String description = listProduct.get(i).values().toArray()[2].toString();
                String price = listProduct.get(i).values().toArray()[1].toString();

                addToChart(id_product, description,price);
            }
        });

        batal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PenjualanBarcodeBluetooth.this);
                builder.setTitle("Peringatan");
                builder.setMessage("Anda yakin membatalkan belanja ini ?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        SqlHelper dbcenter = new SqlHelper(PenjualanBarcodeBluetooth.this);
                        SQLiteDatabase db = dbcenter.getWritableDatabase();
                        db.execSQL("DELETE FROM keranjang");
                        PenjualanBarcodeBluetooth.PA.LoadTotalBelanja();
                        PenjualanBarcodeBluetooth.PA.LoadKeranjang();
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
                Intent i = new Intent(getApplicationContext(), PembayaranBarcodeActivity.class);
                i.putExtra("id_pelanggan", "0");
                i.putExtra("nama", "Pilih Pelanggan");
                startActivity(i);
            }
        });

        // Progress dialog
        pDialog = new ProgressDialog(PenjualanBarcodeBluetooth.this);
        pDialog.setCancelable(false);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // search fitur
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.item_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                ArrayList<HashMap<String, String>> tempList = new ArrayList<>();
                ArrayList<HashMap<String, String>> listProduct = db.GetProductForListView();

                for (HashMap<String, String> temp : listProduct){
                    // prepare data from list to put
                    HashMap<String,String> produk = new HashMap<>();
                    produk.put("barcode",temp.get("barcode"));
                    produk.put("description",temp.get("description"));
                    produk.put("price",temp.get("price"));

                    if(temp.get("description").toLowerCase().equals(s.toLowerCase()) || temp.get("barcode").toLowerCase().equals(s.toLowerCase())){
                        String id_product = temp.get("barcode");
                        String description = temp.get("description");
                        String price = temp.get("price");
                        addToChart(id_product, description,price);

                        Toast.makeText(PenjualanBarcodeBluetooth.this, "Product Added", Toast.LENGTH_SHORT).show();
                        searchView.setQuery("", false); // reset search
                        ListAdapter mAdapter = new SimpleAdapter(PenjualanBarcodeBluetooth.this, listProduct, R.layout.adapter_list_view_barcode,new String[]{"barcode","description","price"}, new int[]{R.id.id_produk, R.id.nama_produk, R.id.harga_jual});
                        listView.setAdapter(mAdapter);
                    }else if (temp.get("description").toLowerCase().contains(s.toLowerCase()) || temp.get("barcode").toLowerCase().contains(s.toLowerCase())){
                        tempList.add(produk);
                        ListAdapter mAdapter = new SimpleAdapter(PenjualanBarcodeBluetooth.this, tempList, R.layout.adapter_list_view_barcode,new String[]{"barcode","description","price"}, new int[]{R.id.id_produk, R.id.nama_produk, R.id.harga_jual});
                        listView.setAdapter(mAdapter);
                    }/*else if(s.equals("")){
                        ListAdapter mAdapter = new SimpleAdapter(PenjualanBarcodeBluetooth.this, tempList, R.layout.adapter_list_view_barcode,new String[]{"barcode","description","price"}, new int[]{R.id.id_produk, R.id.nama_produk, R.id.harga_jual});
                        listView.setAdapter(mAdapter);
                    }*/
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s.equals("") || s == null){ // reset list product
                    ArrayList<HashMap<String, String>> listProduct = db.GetProductForListView();
                    ListAdapter mAdapter = new SimpleAdapter(PenjualanBarcodeBluetooth.this, listProduct, R.layout.adapter_list_view_barcode,new String[]{"barcode","description","price"}, new int[]{R.id.id_produk, R.id.nama_produk, R.id.harga_jual});
                    listView.setAdapter(mAdapter);
                }
                return true;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    public void tutup() {
        SqlHelper dbcenter = new SqlHelper(getApplicationContext());
        SQLiteDatabase db = dbcenter.getWritableDatabase();
        db.execSQL("DELETE FROM keranjang");
        finish();
    }

    private void viewProduct(){
        ArrayList<HashMap<String, String>> listProduct = db.GetProductForListView();
        ListAdapter mAdapter = new SimpleAdapter(PenjualanBarcodeBluetooth.this, listProduct, R.layout.adapter_list_view_barcode,new String[]{"barcode","description","price"}, new int[]{R.id.id_produk, R.id.nama_produk, R.id.harga_jual});
        listView.setAdapter(mAdapter);
    }

    private void addToChart(String id_produk, String nama_produk, String harga_indo){
        int ji;
        HashMap<String, Integer> hitung = db.HitungItemBelanja(id_produk);
        if (hitung.get("jumlah") == 0) {
            db.IsiKeranjang(
                    id_produk,
                    nama_produk,
                    "1",
                    harga_indo,
                    harga_indo
            );
        } else {
            ji = hitung.get("jumlah_produk") + 1;
            int m = hitung.get("harga_jual");
            int t = m * ji;
            dbcenter = new SqlHelper(this);
            SQLiteDatabase db = dbcenter.getWritableDatabase();
            db.execSQL("UPDATE keranjang SET jumlah ='" + ji + "'," +
                    " total='" + t + "' " +
                    " WHERE id_produk='" + id_produk + "'");
        }


        PenjualanBarcodeBluetooth.PA.LoadTotalBelanja();
        PenjualanBarcodeBluetooth.PA.LoadKeranjang();
    }

    @SuppressLint("SetTextI18n")
    public void LoadTotalBelanja() {
        SqlHelper dbcenter = new SqlHelper(getApplicationContext());
        SQLiteDatabase dbp = dbcenter.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor cursor = dbp.rawQuery("SELECT * FROM keranjang", null);
        int total = 0;
        int jitem = 0;
        if (cursor.getCount() > 0) {
            for (int cc = 0; cc < cursor.getCount(); cc++) {
                cursor.moveToPosition(cc);
                total = total + Integer.valueOf(cursor.getString(6));
                jitem = jitem + Integer.valueOf(cursor.getString(4));
            }
        }

        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        String x = formatRupiah.format(total);

        total_belanja.setText(String.valueOf(x));
        jumlah_item.setText(jitem + "");
    }



    public void LoadKeranjang() {
        SqlHelper dbcenter = new SqlHelper(getApplicationContext());
        SQLiteDatabase dbp = dbcenter.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = dbp.rawQuery("SELECT * FROM keranjang", null);
        cursor.moveToFirst();
        belanja_list.clear();
        for (int cc = 0; cc < cursor.getCount(); cc++) {
            cursor.moveToPosition(cc);
            BelanjaModel daftar = new BelanjaModel();
            daftar.setId_produk(cursor.getString(2));
            daftar.setNama_produk(cursor.getString(3));
            daftar.setJumlah(cursor.getString(4));
            daftar.setHarga(cursor.getString(5));
            daftar.setTotal(cursor.getString(6));
            belanja_list.add(daftar);
        }

        adapter.notifyDataSetChanged();
        if(cursor.getCount()>1) {
            listView.smoothScrollToPosition(cursor.getCount() - 1);
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
