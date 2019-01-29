package com.alpokat.toko.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alpokat.toko.Adapter.BelanjaAdapter;
import com.alpokat.toko.Helper.SQLiteHandler;
import com.alpokat.toko.Helper.SqlHelper;
import com.alpokat.toko.Model.BelanjaModel;
import com.alpokat.toko.R;
import com.alpokat.toko.support.ReportUtil;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PenjualanBarcodeBluetoothActivity extends AppActivity {
    @BindView(R.id.ListView)
    ListView listView;
    @BindView(R.id.total_belanja)
    TextView total_belanja;
    @BindView(R.id.jumlah_item)
    TextView jumlah_item;

    @BindView(R.id.batal)
    ImageButton batal;
    @BindView(R.id.selesai)
    ImageButton selesai;

    @BindView(R.id.cart_list)
    RecyclerView cartList;

    SQLiteHandler db;

    ArrayList<String> list_product = new ArrayList<>();
    ArrayAdapter<String> mAdapter;
    private SqlHelper dbcenter;

    public static PenjualanBarcodeBluetoothActivity PA;
    private ProgressDialog pDialog;

    private BelanjaAdapter adapter;
    private List<BelanjaModel> belanja_list;

    private ArrayList<HashMap<String, String>> listProduct;
    private boolean isBarcodeOff = true;
    private MenuItem menuItem;
    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjualan_barcode_bluetooth);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        db = new SQLiteHandler(this);
        listProduct = db.getAllProduct();
        belanja_list = new ArrayList<>();
        adapter = new BelanjaAdapter(getApplicationContext(), belanja_list);

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

        PA = this;

        showProduct(listProduct);
        LoadTotalBelanja();

        belanja_list = new ArrayList<>();
        adapter = new BelanjaAdapter(getApplicationContext(), belanja_list);

        RecyclerView.LayoutManager mLayoutManager2 = new GridLayoutManager(getApplicationContext(), 1);
        cartList.setLayoutManager(mLayoutManager2);
        cartList.addItemDecoration(new PenjualanBarcodeBluetoothActivity.GridSpacingItemDecoration(1, dpToPx(), true));
        cartList.setItemAnimator(new DefaultItemAnimator());
        cartList.setAdapter(adapter);

        LoadKeranjang();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (listProduct.size() == 0 || listProduct.get(i).values().size() == 0) return;

                String id_product = listProduct.get(i).get("id_product");
                String description = listProduct.get(i).get("description");
                String price = listProduct.get(i).get("price");

                addToChart(id_product, description, price);
            }
        });

        batal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PenjualanBarcodeBluetoothActivity.this);
                builder.setTitle("Peringatan");
                builder.setMessage("Anda yakin membatalkan belanja ini ?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but resetCart the dialog
                        SqlHelper dbcenter = new SqlHelper(PenjualanBarcodeBluetoothActivity.this);
                        SQLiteDatabase db = dbcenter.getWritableDatabase();
                        db.execSQL("DELETE FROM keranjang");
                        PenjualanBarcodeBluetoothActivity.PA.LoadTotalBelanja();
                        PenjualanBarcodeBluetoothActivity.PA.LoadKeranjang();
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
        pDialog = new ProgressDialog(PenjualanBarcodeBluetoothActivity.this);
        pDialog.setCancelable(false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // search fitur
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        menuItem = menu.findItem(R.id.item_search);
        menuItem.setEnabled(true);

        searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setQuery("", false); // reset search
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.equals("")) return false;

                ArrayList<HashMap<String, String>> tempList = new ArrayList<>();

                if (ReportUtil.isNumeric(s)) {
                    listProduct = db.searchProductByBarcode(s.toLowerCase());
                    if (listProduct.size() > 0) {
                        String id_product = listProduct.get(0).get("id_product");
                        String description = listProduct.get(0).get("description");
                        String price = listProduct.get(0).get("price");

                        addToChart(id_product, description, price);
                        clearListProduct(); // reset list product
                    }
                } else {
                    listProduct = db.searchProductByText(s);
                }


                ListAdapter mAdapter = new SimpleAdapter(PenjualanBarcodeBluetoothActivity.this, listProduct, R.layout.adapter_list_view_barcode, new String[]{"barcode", "description", "price", "price_show"}, new int[]{R.id.id_produk, R.id.nama_produk, R.id.harga_jual, R.id.harga_indo});
                listView.setAdapter(mAdapter);

                if (s.equals("")) { // reset list product
                    clearListProduct();
                }

                return true;
            }
        });

        searchView.setIconified(false);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(listView, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void clearListProduct() {
        listProduct = db.getAllProduct();
        ListAdapter mAdapter = new SimpleAdapter(PenjualanBarcodeBluetoothActivity.this, listProduct, R.layout.adapter_list_view_barcode, new String[]{"barcode", "description", "price", "price_show"}, new int[]{R.id.id_produk, R.id.nama_produk, R.id.harga_jual, R.id.harga_indo});
        listView.setAdapter(mAdapter);
    }

    public void resetCart() {
        SqlHelper dbcenter = new SqlHelper(getApplicationContext());
        SQLiteDatabase db = dbcenter.getWritableDatabase();
        db.execSQL("DELETE FROM keranjang");
        finish();
    }

    private void showProduct(ArrayList<HashMap<String, String>> listProduct) {
        ListAdapter mAdapter = new SimpleAdapter(PenjualanBarcodeBluetoothActivity.this, listProduct, R.layout.adapter_list_view_barcode, new String[]{"barcode", "description", "price", "price_show"}, new int[]{R.id.id_produk, R.id.nama_produk, R.id.harga_jual, R.id.harga_indo});
        listView.setAdapter(mAdapter);
    }

    private void addToChart(String id_produk, String nama_produk, String harga_indo) {
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


        PenjualanBarcodeBluetoothActivity.PA.LoadTotalBelanja();
        PenjualanBarcodeBluetoothActivity.PA.LoadKeranjang();
    }

    @SuppressLint("SetTextI18n")
    public void LoadTotalBelanja() {
        try {
            SqlHelper dbcenter = new SqlHelper(getApplicationContext());
            SQLiteDatabase dbp = dbcenter.getReadableDatabase();
            @SuppressLint("Recycle")
            Cursor cursor = dbp.rawQuery("SELECT * FROM keranjang", null);
            long total = 0;
            int jitem = 0;
            if (cursor.getCount() > 0) {
                for (int cc = 0; cc < cursor.getCount(); cc++) {
                    cursor.moveToPosition(cc);
                    total = total + Long.valueOf(cursor.getString(6));
                    jitem = jitem + Integer.valueOf(cursor.getString(4));
                }
            }

            Locale localeID = new Locale("in", "ID");
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
            String x = formatRupiah.format(total);

            total_belanja.setText(String.valueOf(x));
            jumlah_item.setText(jitem + "");

        } catch (Exception e) {
            resetCart();
            Log.e("Penjualan Barcode:", e.getMessage());
        }
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
        if (cursor.getCount() > 1) {
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
