package com.alpokat.toko.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alpokat.toko.Dialog.TransactionReportTodayDialog;
import com.alpokat.toko.Helper.SQLiteHandler;
import com.alpokat.toko.Helper.SessionManager;
import com.alpokat.toko.Helper.SqlHelper;
import com.alpokat.toko.Model.api.HttpsTrustManager;
import com.alpokat.toko.R;
import com.alpokat.toko.Setting.AppConfig;
import com.alpokat.toko.Setting.AppController;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.itemanimators.AlphaCrossFadeAnimator;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import app.beelabs.com.codebase.component.LoadingDialogComponent;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppActivity {

    @BindView(R.id.penjualan)
    RelativeLayout penjualan;

    private SessionManager session;
    private SQLiteHandler db;
    private ProgressDialog pDialog;
    private String id_toko, nama_t, nama_k;
    private TextView trial;
    AlertDialog alertDialog1;
    CharSequence[] values = {" 1 Bulan ", " 3 Bulan ", " 6 Bulan ", " 12 Bulan "};
    private long selisih;
    private int add;
    private String exp, devid, sinkron;
    private String id_kasir;
    private String lx;
    private String mode;
    private String dev_id, url;
    private String order_id;

    private static final int PROFILE_SETTING = 100000;
    private AccountHeader headerResult = null;
    private Drawer result = null;
    private LoadingDialogComponent loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }


        RelativeLayout pelanggan = findViewById(R.id.pelanggan);
        RelativeLayout setting = findViewById(R.id.setting);
        RelativeLayout logout = findViewById(R.id.logout);
        TextView nama = findViewById(R.id.nama_kasir);
        TextView toko = findViewById(R.id.nama_toko);
        trial = findViewById(R.id.trial);

        boolean s = isMyServiceRunning(MyService.class);
        if (!s) {
            Intent intent = new Intent(getApplicationContext(), MyService.class);
            startService(intent);
        }

        // Progress dialog
        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> p = db.BacaKasir();
        nama_k = p.get("nama_kasir");
        nama_t = p.get("nama_kasir");
        nama.setText(nama_k);
        toko.setText(nama_t);
        id_toko = p.get("id_toko");
        id_kasir = p.get("id_kasir");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String sekarang = dateFormat.format(date);

        Date tglAwal = null;
        Date tglAkhir = null;
        HashMap<String, String> ref = db.BacaRef();
        exp = ref.get("exp");
        mode = ref.get("mode");
        devid = ref.get("devid");
        sinkron = ref.get("sinkron");
        try {
            tglAwal = dateFormat.parse(sekarang);
            if (exp == null) {
                tglAkhir = dateFormat.parse(sekarang);
            } else {
                tglAkhir = dateFormat.parse(exp);
            }
            System.out.println(tglAkhir);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        long bedaHari = tglAkhir.getTime() - tglAwal.getTime();
        selisih = TimeUnit.MILLISECONDS.toDays(bedaHari);

        tampilText();


        if (p.isEmpty()) {
            session.setLogin(false);
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        }


        pelanggan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DataPelangganActivity.class);
                startActivity(i);
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(i);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });


        trial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PilihLanggan();
            }
        });

        //=========================================================================================
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header_drawer)
                .build();

        //Create the drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withItemAnimator(new AlphaCrossFadeAnimator())
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Home").withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(1).withSelectable(true),

                        new SectionDrawerItem().withName("Penjualan"),
                        new PrimaryDrawerItem().withName("Mode Restoran").withIcon(FontAwesome.Icon.faw_table).withIdentifier(4).withSelectable(false),
                        new PrimaryDrawerItem().withName("Mode Retailer").withIcon(FontAwesome.Icon.faw_list_ol).withIdentifier(98).withSelectable(false),
                        new PrimaryDrawerItem().withName("Retur Penjualan").withIcon(FontAwesome.Icon.faw_retweet).withIdentifier(2).withSelectable(false),

                        new SectionDrawerItem().withName("Produk"),
                        new PrimaryDrawerItem().withName("Sinkron Produk").withIcon(FontAwesome.Icon.faw_sync_alt).withIdentifier(3).withSelectable(false),
                        new PrimaryDrawerItem().withName("Tambah Produk").withIcon(FontAwesome.Icon.faw_barcode).withIdentifier(7).withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Data Pelanggan").withIcon(FontAwesome.Icon.faw_users).withIdentifier(5).withSelectable(false),
                        new PrimaryDrawerItem().withName("Setting").withIcon(FontAwesome.Icon.faw_cogs).withIdentifier(6).withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Logout").withIcon(FontAwesome.Icon.faw_times_circle).withIdentifier(99).withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem != null) {
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 98) {
                                if (selisih > 0) {
                                    intent = new Intent(MainActivity.this, PenjualanBarcodeBluetoothActivity.class);
                                } else {
                                    intent = null;
                                }
                            } else if (drawerItem.getIdentifier() == 99) {
                                logoutUser();
                            } else if (drawerItem.getIdentifier() == 1) {

                            } else if (drawerItem.getIdentifier() == 2) {
                                intent = new Intent(MainActivity.this, DataPenjualan.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                Toast.makeText(MainActivity.this, "Sinkron data", Toast.LENGTH_SHORT).show();
                                SingkronProduk();
                            } else if (drawerItem.getIdentifier() == 4) {
                                if (selisih > 0) {
                                    intent = new Intent(MainActivity.this, PenjualanActivity.class);
                                } else {
                                    intent = null;
                                }
                            } else if (drawerItem.getIdentifier() == 5) {
                                intent = new Intent(MainActivity.this, DataPelangganActivity.class);
                            } else if (drawerItem.getIdentifier() == 6) {
                                intent = new Intent(MainActivity.this, SettingActivity.class);
                            } else if (drawerItem.getIdentifier() == 7) {
                                intent = new Intent(MainActivity.this, InputProduk.class);
                            }
                            if (intent != null) {
                                MainActivity.this.startActivity(intent);
                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

    }

    private void tampilText() {
        HashMap<String, String> ref = db.BacaRef();
        exp = ref.get("exp");
        mode = ref.get("mode");
        devid = ref.get("devid");
        sinkron = ref.get("sinkron");

        String teks = "";
        if (mode != null && mode.equalsIgnoreCase("trial")) {
            if (selisih < 1) {
                teks = "Free trial telah habis\nKlik untuk langganan";

                penjualan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Peringatan");
                        builder.setMessage("Masa trial anda telah habis, Apakah anda ingin berlangganan Alpokat POS ?");
                        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                PilihLanggan();
                            }
                        });

                        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });

            } else {
                teks = "Free trial tinggal " + selisih + " hari\nKlik untuk langganan";
                transaksi();
            }
            trial.setVisibility(View.VISIBLE);
        } else {
            if (selisih < 0) {
                teks = "Masa berlangganan habis\nKlik untuk langganan";
                penjualan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Peringatan");
                        builder.setMessage("Masa berlangganan anda telah habis, Apakah anda ingin memperpanjang kontrak Alpokat POS ?");
                        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                PilihLanggan();
                            }
                        });

                        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });

            } else if (selisih > 0 && selisih < 4) {
                teks = "Masa berlangganan\ntinggal " + selisih + " hari lagi\nKlik untuk memperpanjang";
                transaksi();
            } else if (selisih > 3) {
                trial.setVisibility(View.GONE);
                transaksi();
            }
        }

        trial.setText(teks);
    }


    @Override
    protected void getFakturTokoToday(List dataTransaksi) {
        if (loadingDialog != null) loadingDialog.dismiss();
        Log.e("DEBUG", "");
        TransactionReportTodayDialog dialogReport = new TransactionReportTodayDialog(dataTransaksi, this, R.style.CoconutDialogFullScreen);
        dialogReport.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sinkron != null && sinkron.equalsIgnoreCase("tidak")) {
            SinExp();
        }
    }

    private void SinExp() {
        String tag_string_req = "req_login";
        HttpsTrustManager.allowAllSSL(this);
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
                        db1.execSQL("UPDATE referensi SET sinkron = 'ya'");
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), "ini error : " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error 1: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage() + "zzzz", Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("model", "update");
                params.put("devid", devid);
                params.put("exp", exp);
                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void PilihLanggan() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Pilih lama berlangganan");
        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {


            public void onClick(DialogInterface dialog, int item) {

                char[] chars1 = "ABCDEF012GHIJKL345MNOPQR678STUVWXYZ9".toCharArray();
                StringBuilder sb1 = new StringBuilder();
                Random random1 = new Random();
                for (int i = 0; i < 5; i++) {
                    char c1 = chars1[random1.nextInt(chars1.length)];
                    sb1.append(c1);
                }
                String random_string = sb1.toString();
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
                order_id = df.format(c) + "-" + random_string;
                Intent i;
                switch (item) {
                    case 0:
                        url = "https://toko.alpokat.com/midtrans/examples/vt-web/process.php" +
                                "?order_id=" + order_id +
                                "&dev_id=" + dev_id +
                                "&id_produk=1_bulan" +
                                "&id_kasir=" + id_kasir;
                        i = new Intent(getApplicationContext(), Midtrans.class);
                        i.putExtra("url", url);
                        startActivity(i);
                        break;
                    case 1:
                        url = "https://toko.alpokat.com/midtrans/examples/vt-web/process.php" +
                                "?order_id=" + order_id +
                                "&dev_id=" + dev_id +
                                "&id_produk=3_bulan" +
                                "&id_kasir=" + id_kasir;
                        i = new Intent(getApplicationContext(), Midtrans.class);
                        i.putExtra("url", url);
                        startActivity(i);
                        break;
                    case 2:
                        url = "https://toko.alpokat.com/midtrans/examples/vt-web/process.php" +
                                "?order_id=" + order_id +
                                "&dev_id=" + dev_id +
                                "&id_produk=6_bulan" +
                                "&id_kasir=" + id_kasir;
                        i = new Intent(getApplicationContext(), Midtrans.class);
                        i.putExtra("url", url);
                        startActivity(i);
                        break;
                    case 3:
                        url = "https://toko.alpokat.com/midtrans/examples/vt-web/process.php" +
                                "?order_id=" + order_id +
                                "&dev_id=" + dev_id +
                                "&id_produk=12_bulan" +
                                "&id_kasir=" + id_kasir;
                        i = new Intent(getApplicationContext(), Midtrans.class);
                        i.putExtra("url", url);
                        startActivity(i);
                        break;
                }
                alertDialog1.dismiss();
            }
        });
        alertDialog1 = builder.create();
        alertDialog1.show();
    }

    private void transaksi() {
        penjualan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog = new LoadingDialogComponent("Loading", MainActivity.this, R.style.CoconutDialogFullScreen);
                loadingDialog.show();

                callStatsPenjualanToko(Integer.valueOf(id_toko));
            }
        });
    }

    private void logoutUser() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Peringatan");
        builder.setMessage("Anda yakin ingin Keluar");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                stopService(new Intent(getApplicationContext(), MyService.class));
                session.setLogin(false);
                db.HapusUser();
                finish();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_cloud, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.cloud) {
            Intent i = new Intent(getApplicationContext(), DataPenjualan.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.data) {
            Toast.makeText(getApplicationContext(), "Sinkron data", Toast.LENGTH_SHORT).show();
            SingkronProduk();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void SingkronProduk() {


        pDialog.setMessage("Sinkron Produk ...");
        showDialog();

        // Creating volley request obj
        HttpsTrustManager.allowAllSSL(this);
        JsonArrayRequest MasukReq = new JsonArrayRequest(AppConfig.LIST_PRODUK + id_toko,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        hideDialog();
                        db.HapusProduk();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                DownloadFoto(obj.getString("foto_produk"));
                                db.TambahProduk(obj.getString("id_produk"),
                                        obj.getString("nama_produk"),
                                        obj.getString("foto_produk"),
                                        obj.getString("harga"),
                                        obj.getString("harga_indo"),
                                        obj.getString("last_update"),
                                        "no",
                                        obj.getString("kategori"),
                                        obj.getString("barcode"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
            }
        });
        AppController.getInstance().addToRequestQueue(MasukReq);
        hideDialog();
        Toast.makeText(getApplicationContext(), "Sinkron data selesai", Toast.LENGTH_SHORT).show();
    }

    private void DownloadFoto(final String foto) {

        String path = Environment.getExternalStorageDirectory() + "/alpokat/" + foto;
        File imgFile = new File(path);
        if (!imgFile.exists()) {
            new MainActivity.DownloadFileFoto().execute(AppConfig.HOST + "foto_produk/" + foto, foto);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class DownloadFileFoto extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            int count;

            try {

                URL url = new URL(params[0]);
                String nama = params[1];
                URLConnection conexion = url.openConnection();
                conexion.connect();

                int lengthofFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Length of file: " + lengthofFile);

                File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "alpokat");

                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Toast.makeText(getApplicationContext(), "tidak bisa membuat folder", Toast.LENGTH_LONG).show();
                        return null;
                    }
                }

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream("sdcard/alpokat/" + nama);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthofFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception ignored) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.d("ANDRO_ASYNC", values[0]);
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

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }

            TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            String tmDevice, tmSerial, androidId;
            assert tm != null;
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            dev_id = deviceUuid.toString();
        }
        return true;
    }
}
