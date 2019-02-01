package com.alpokat.toko.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alpokat.toko.Dialog.TransactionReportTodayDialog;
import com.alpokat.toko.Helper.SQLiteHandler;
import com.alpokat.toko.Helper.SessionManager;
import com.alpokat.toko.Model.api.HttpsTrustManager;
import com.alpokat.toko.R;
import com.alpokat.toko.Setting.AppConfig;
import com.alpokat.toko.Setting.AppController;
import com.alpokat.toko.support.DownloadPhoto;
import com.alpokat.toko.support.PermissionUtil;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
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

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import app.beelabs.com.codebase.component.LoadingDialogComponent;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.alpokat.toko.support.PermissionUtil.getDeviceID;

public class MainActivity extends AppActivity {

    @BindView(R.id.penjualan)
    RelativeLayout penjualan;

    @BindView(R.id.pelanggan)
    RelativeLayout pelanggan;
    @BindView(R.id.setting)
    RelativeLayout setting;
    @BindView(R.id.logout)
    RelativeLayout logout;
    @BindView(R.id.nama_kasir_text)
    TextView namaKasirText;
    @BindView(R.id.nama_toko_text)
    TextView namaTokoText;

    @BindView(R.id.trial_text)
    TextView trialText;


    private SessionManager session;
    private SQLiteHandler db;
    private String idToko, namaToko, namaKasir;
    AlertDialog alertDialog1;
    CharSequence[] values = {" 1 Bulan ", " 3 Bulan ", " 6 Bulan ", " 12 Bulan "};
    private long selisih;
    private int add;
    private String exp, devid, sinkron;
    private String idKasir;
    private String lx;
    private String mode;
    private String deviceID, url;
    private String orderId;

    private static final int PROFILE_SETTING = 100000;
    private AccountHeader headerResult = null;
    private Drawer result = null;
    private LoadingDialogComponent loadingDialog;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE
        };

        if (!PermissionUtil.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);

            deviceID = getDeviceID(this);
        }

        // Session manager
        session = new SessionManager(getApplicationContext());

        setupTokoProfile();

        setupMenuDrawer(savedInstanceState);

        startServiceSyncProduct();

    }

    private void startServiceSyncProduct() {
        boolean s = isMyServiceRunning(DataService.class);
        if (!s) {
            Intent intent = new Intent(getApplicationContext(), DataService.class);
            startService(intent);
        }
    }

    private void setupMenuDrawer(Bundle savedInstanceState) {
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
                        new PrimaryDrawerItem()
                                .withName("Home")
                                .withIcon(GoogleMaterial.Icon.gmd_home)
                                .withIdentifier(1)
                                .withSelectable(true),
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

                            switch ((int) drawerItem.getIdentifier()) {
                                case 98:
                                    if (selisih > 0) {
                                        intent = new Intent(MainActivity.this, PenjualanBarcodeBluetoothActivity.class);
                                        startActivity(intent);
                                    }
                                    break;

                                case 99:
                                    logoutUser();
                                    break;

                                case 2:
                                    intent = new Intent(MainActivity.this, DataPenjualan.class);
                                    startActivity(intent);
                                    break;

                                case 3:
                                    Toast.makeText(MainActivity.this, "Sinkron data ...", Toast.LENGTH_SHORT).show();
                                    SynchronizeProduct();
                                    break;

                                case 4:
                                    if (selisih > 0) {
                                        intent = new Intent(MainActivity.this, PenjualanActivity.class);
                                        startActivity(intent);
                                    }
                                    break;

                                case 5:
                                    intent = new Intent(MainActivity.this, DataPelangganActivity.class);
                                    startActivity(intent);
                                    break;

                                case 6:
                                    intent = new Intent(MainActivity.this, SettingActivity.class);
                                    startActivity(intent);
                                    break;

                                case 7:
                                    intent = new Intent(MainActivity.this, InputProdukActivity.class);
                                    startActivity(intent);
                            }
                        }


                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();
    }

    private void SynchronizeProduct() {

        // Creating volley request obj
        HttpsTrustManager.allowAllSSL(this);
        JsonArrayRequest MasukReq = new JsonArrayRequest(AppConfig.LIST_PRODUK + idToko,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
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

            }
        });
        AppController.getInstance().addToRequestQueue(MasukReq);

        Toast.makeText(getApplicationContext(), "Sinkron data selesai", Toast.LENGTH_SHORT).show();
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

            trialText.setVisibility(View.VISIBLE);
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
                trialText.setVisibility(View.GONE);
                transaksi();
            }
        }

        trialText.setText(teks);
    }

    private void DownloadFoto(final String foto) {

        String path = Environment.getExternalStorageDirectory() + "/alpokat/" + foto;
        File imgFile = new File(path);
        if (!imgFile.exists()) {
            (new DownloadPhoto(this)).execute(AppConfig.HOST + "foto_produk/" + foto, foto);
        }
    }

    @OnClick({R.id.pelanggan, R.id.setting, R.id.logout, R.id.trial_text})
    public void onAction(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.pelanggan:
                intent = new Intent(getApplicationContext(), DataPelangganActivity.class);
                startActivity(intent);
                break;

            case R.id.setting:
                intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
                break;

            case R.id.logout:
                logoutUser();
                break;

            case R.id.trial_text:
                PilihLanggan();
                break;
        }
    }



    private void logoutUser() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Peringatan");
        builder.setMessage("Anda yakin ingin Keluar");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                stopService(new Intent(getApplicationContext(), DataService.class));
                session.setLogin(false);
                db.HapusUser();
                dialog.dismiss();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();


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

    private void setupTokoProfile() {

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> p = db.BacaKasir();
        namaKasir = p.get("nama_kasir");
        namaToko = p.get("nama_toko");
        namaKasirText.setText(namaKasir);
        namaTokoText.setText(namaToko);
        idToko = p.get("id_toko");
        idKasir = p.get("id_kasir");


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
    }

    @Override
    protected void getFakturTokoToday(List dataTransaksi) {
        if (loadingDialog != null) loadingDialog.dismiss();
        Log.e("DEBUG", "");
        TransactionReportTodayDialog dialogReport = new TransactionReportTodayDialog(dataTransaksi, this, R.style.CoconutDialogFullScreen);
        dialogReport.show();

    }

    private void transaksi() {
        penjualan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog = new LoadingDialogComponent("Loading", MainActivity.this, R.style.CoconutDialogFullScreen);
                loadingDialog.show();

                callStatsPenjualanToko(Integer.valueOf(idToko));
            }
        });
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
                orderId = df.format(c) + "-" + random_string;
                Intent i;
                switch (item) {
                    case 0:
                        url = "https://toko.alpokat.com/midtrans/examples/vt-web/process.php" +
                                "?order_id=" + orderId +
                                "&dev_id=" + deviceID +
                                "&id_produk=1_bulan" +
                                "&id_kasir=" + idKasir;
                        i = new Intent(getApplicationContext(), Midtrans.class);
                        i.putExtra("url", url);
                        startActivity(i);
                        break;
                    case 1:
                        url = "https://toko.alpokat.com/midtrans/examples/vt-web/process.php" +
                                "?order_id=" + orderId +
                                "&dev_id=" + deviceID +
                                "&id_produk=3_bulan" +
                                "&id_kasir=" + idKasir;
                        i = new Intent(getApplicationContext(), Midtrans.class);
                        i.putExtra("url", url);
                        startActivity(i);
                        break;
                    case 2:
                        url = "https://toko.alpokat.com/midtrans/examples/vt-web/process.php" +
                                "?order_id=" + orderId +
                                "&dev_id=" + deviceID +
                                "&id_produk=6_bulan" +
                                "&id_kasir=" + idKasir;
                        i = new Intent(getApplicationContext(), Midtrans.class);
                        i.putExtra("url", url);
                        startActivity(i);
                        break;
                    case 3:
                        url = "https://toko.alpokat.com/midtrans/examples/vt-web/process.php" +
                                "?order_id=" + orderId +
                                "&dev_id=" + deviceID +
                                "&id_produk=12_bulan" +
                                "&id_kasir=" + idKasir;
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


}
