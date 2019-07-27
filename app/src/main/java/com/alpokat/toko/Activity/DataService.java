package com.alpokat.toko.Activity;

import android.annotation.SuppressLint;
import android.app.Service;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.alpokat.toko.Helper.DataHandler;
import com.alpokat.toko.Helper.SqlHelper;
import com.alpokat.toko.Model.PenjualanModel;
import com.alpokat.toko.Setting.AppConfig;
import com.alpokat.toko.Setting.AppController;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DataService extends Service {

    private DataHandler db;
    private String exp,mode,devid,sinkron;
    private Handler m_handler;
    private Runnable m_handlerTask;
    private String id_toko,id_produk,jumlah,id_kasir,id_pelanggan,faktur,tanggal;
    private int jt;
    private String langganan;
    private String xkasir;
    private String noref;

    public DataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = new DataHandler(getApplicationContext());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        m_handler = new Handler();
        m_handlerTask = new Runnable() {
            @Override
            public void run() {
                if(cekKoneksi()) {

                    HashMap<String, String> ref = db.BacaRef();
                    exp = ref.get("exp");
                    mode = ref.get("mode");
                    devid = ref.get("devid");
                    sinkron = ref.get("sinkron");
                    langganan = ref.get("langganan");
                    xkasir = ref.get("id_kasir");
                    noref = ref.get("no_ref");

                    if(sinkron != null && sinkron.equalsIgnoreCase("tidak")) SinExp();
                    TampilPenjualan();
                    if(jt > 0) {
                        UploadPenjualan(id_toko,
                                id_produk,
                                jumlah,
                                id_kasir,
                                id_pelanggan,
                                faktur,
                                tanggal);
                    }
                }
                m_handler.postDelayed(m_handlerTask, 15 * 1000);
            }
        };
        m_handlerTask.run();
        return START_STICKY;
    }

    private void SinExp() {
        String tag_string_req = "req_login";
//        HttpsTrustManager.allowAllSSL(this);
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.ADD_REF, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        SqlHelper dbcenter = new SqlHelper(getApplicationContext());
                        SQLiteDatabase db1 = dbcenter.getWritableDatabase();
                        db1.execSQL("UPDATE referensi SET sinkron = 'ya' WHERE devid='" + devid + "'");

                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),  e.getMessage() + "Error 1", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.getMessage() + "Error 2", Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("model", "update");
                params.put("devid", devid);
                params.put("exp", exp);
                params.put("langganan", langganan);
                params.put("id_kasir", xkasir);
                params.put("no_ref", noref);
                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private boolean cekKoneksi() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() ==
                android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {

            return true;
        } else if (
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() ==
                                android.net.NetworkInfo.State.DISCONNECTED) {

            return false;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        m_handler.removeCallbacks(m_handlerTask);
        super.onDestroy();
        this.stopSelf();
    }

    public void TampilPenjualan() {

        SqlHelper dbcenter = new SqlHelper(getApplicationContext());
        SQLiteDatabase db = dbcenter.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT * FROM transaksi", null);
        jt = cursor.getCount();
        cursor.moveToFirst();

        id_toko = "";
        id_produk = "";
        jumlah = "";
        id_kasir = "";
        id_pelanggan = "";
        faktur = "";
        tanggal = "";

        for (int cc=0; cc < cursor.getCount(); cc++){
            cursor.moveToPosition(cc);
            PenjualanModel daftar = new PenjualanModel();
            daftar.setNo_faktur(cursor.getString(6));
            String x;
            if(cc == cursor.getCount()-1){
                x = "";
            }else{
                x = "/";
            }

            id_toko = id_toko + cursor.getString(1) + x;
            id_produk = id_produk + cursor.getString(2) + x;
            jumlah = jumlah +  cursor.getString(3) + x;
            id_kasir = id_kasir + cursor.getString(4) + x;
            id_pelanggan = id_pelanggan + cursor.getString(5) + x;
            faktur = faktur + cursor.getString(6) + x;
            tanggal = tanggal + cursor.getString(7) + x;
        }

    }

    private void UploadPenjualan(
            final String id_toko,
            final String id_produk,
            final String jumlah,
            final String id_kasir,
            final String id_pelanggan,
            final String faktur,
            final String tanggal) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";


//        HttpsTrustManager.allowAllSSL(this);
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.BAYAR, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {


                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        DataHandler db1 = new DataHandler(getApplicationContext());
                        db1.HapusTransaksi();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("id_toko", id_toko);
                params.put("id_produk", id_produk);
                params.put("jumlah", jumlah);
                params.put("id_kasir", id_kasir);
                params.put("id_pelanggan", id_pelanggan);
                params.put("faktur", faktur);
                params.put("tanggal", tanggal);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
