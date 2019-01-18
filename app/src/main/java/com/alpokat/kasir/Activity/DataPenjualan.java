package com.alpokat.kasir.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.alpokat.kasir.Adapter.PenjualanAdapter;
import com.alpokat.kasir.Helper.SQLiteHandler;
import com.alpokat.kasir.Helper.SqlHelper;
import com.alpokat.kasir.Model.PenjualanModel;
import com.alpokat.kasir.Model.api.HttpsTrustManager;
import com.alpokat.kasir.R;
import com.alpokat.kasir.Setting.AppConfig;
import com.alpokat.kasir.Setting.AppController;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataPenjualan extends AppCompatActivity {

    public static DataPenjualan DJ;
    private PenjualanAdapter adapter;
    private List<PenjualanModel> penjualan_list;
    private SQLiteHandler db;
    private ProgressDialog pDialog;
    private FrameLayout dialog_retur;


    private Button batal, retur;
    private RecyclerView recyclerView;
    private TextView txt_faktur;
    private EditText password;
    private String xidtoko;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_penjualan);


        DJ = this;
        password = findViewById(R.id.password);
        txt_faktur = findViewById(R.id.txt_faktur);
        dialog_retur = findViewById(R.id.dialog_retur);
        batal = findViewById(R.id.batal);
        retur = findViewById(R.id.retur);

        dialog_retur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> p = db.BacaKasir();
        xidtoko = p.get("id_toko");

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        penjualan_list = new ArrayList<>();
        adapter = new PenjualanAdapter(getApplicationContext(), penjualan_list);

        recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager2 = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(mLayoutManager2);
        recyclerView.addItemDecoration(new DataPenjualan.GridSpacingItemDecoration(1, dpToPx(), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(5);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        TampilPenjualan();

        batal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_retur.setVisibility(View.GONE);
            }
        });


    }

    public void no_retur(final String no) {
        txt_faktur.setText(no);
        dialog_retur.setVisibility(View.VISIBLE);

        password.setText("");
        password.setFocusable(true);
        retur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturPassword(xidtoko, password.getText().toString(), no);
            }
        });
    }

    private void ReturPassword(final String id_toko, final String pass_retur, final String xfaktur) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Mengkonfirmasi password retur ...");
        showDialog();

        HttpsTrustManager.allowAllSSL(this);
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.KONFIRMASI_RETUR, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        SQLiteHandler db = new SQLiteHandler(getApplicationContext());
                        db.ReturTransaksi(xfaktur);
                        dialog_retur.setVisibility(View.GONE);
                        TampilPenjualan();
                        Toast.makeText(getApplicationContext(), "Penjualan berhasil diretur", Toast.LENGTH_SHORT).show();
                    } else {
                        // Error in login. Get the error message
                        Toast.makeText(getApplicationContext(), "Maaf password retur salah !", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Maaf server tidak bisa diakses !", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage() + "zzzz", Toast.LENGTH_SHORT).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("id_toko", id_toko);
                params.put("pass_retur", pass_retur);
                params.put("faktur", xfaktur);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_retur, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                CariPenjualan(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void TampilPenjualan() {

        SqlHelper dbcenter = new SqlHelper(getApplicationContext());
        SQLiteDatabase db = dbcenter.getReadableDatabase();
        penjualan_list.clear();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT * FROM transaksi", null);
        cursor.moveToFirst();


        for (int cc = 0; cc < cursor.getCount(); cc++) {
            cursor.moveToPosition(cc);
            PenjualanModel daftar = new PenjualanModel();
            daftar.setNo_faktur(cursor.getString(6));
        }

        adapter.notifyDataSetChanged();


    }

    public void CariPenjualan(String faktur) {

        JsonArrayRequest MasukReq = new JsonArrayRequest(AppConfig.URL_FAKTUR + xidtoko + "&cari=" + faktur,
                new com.android.volley.Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        penjualan_list.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                PenjualanModel daftar = new PenjualanModel();
                                daftar.setNo_faktur(obj.getString("faktur"));
                                penjualan_list.add(daftar);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        if (response.length() == 0) {
                            Toast.makeText(getApplicationContext(), "Faktur tidak ditemukan", Toast.LENGTH_LONG).show();
                        }


                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data
                        adapter.notifyDataSetChanged();
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(MasukReq);

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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}
