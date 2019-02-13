package com.alpokat.toko.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.alpokat.toko.Helper.SQLiteHandler;
import com.alpokat.toko.Helper.SessionManager;
import com.alpokat.toko.Model.StatsModel;
import com.alpokat.toko.Model.api.HttpsTrustManager;
import com.alpokat.toko.Setting.AppConfig;
import com.alpokat.toko.Setting.AppController;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import app.beelabs.com.codebase.base.BaseActivity;
import app.beelabs.com.codebase.component.ProgressDialogComponent;

import static com.alpokat.toko.support.PermissionUtil.hasPermissions;

public class AppActivity extends BaseActivity {

    protected void setupPermissionApp(Activity activity){
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE
        };

        if (!hasPermissions(activity, PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
        }
    }

    protected boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void callStatsPenjualanToko(int idToko) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String today = dateFormat.format(date);

        String endpoint = AppConfig.STATS.replaceAll("idtoko", "" + idToko).replaceAll("tgl", today);

//        HttpsTrustManager.allowAllSSL(this);
        JsonArrayRequest MasukReq = new JsonArrayRequest(endpoint,
                new com.android.volley.Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("DEBUG", "testing");

                        List<StatsModel> models = new ArrayList();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);


                                StatsModel stats = new StatsModel(
                                        Integer.parseInt(obj.getString("count")),
                                        Integer.parseInt(obj.getString("jumlah")),
                                        Long.parseLong(obj.getString("total")),
                                        obj.getString("tanggal")
                                );

                                models.add(stats);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }



                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        AppController.getInstance().addToRequestQueue(MasukReq);
    }


    @SuppressLint("MissingPermission")
    protected void checkDeviceID(final ScanDeviceCallback callback) {
        TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            String tmDevice, tmSerial, androidId;
            assert tm != null;
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        final String devId = deviceUuid.toString();
        final SQLiteHandler db = new SQLiteHandler(getApplicationContext());
        final SessionManager session = new SessionManager(getApplicationContext());
        // Tag used to cancel the request
        String tag_string_req = "req_aktif";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.DEVID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
//                    hideDialog();
                    JSONObject jObj = new JSONObject(response);
                    boolean isError = jObj.getBoolean("error");

                    Intent i;
                    if (isError) {
                        i = new Intent(getApplicationContext(), RefActivity.class);
                        startActivity(i);
                        finish();
                    } else {

                        JSONObject aktif = jObj.getJSONObject("aktif");
                        String devid = aktif.getString("devid");
                        String no_ref = aktif.getString("no_ref");
                        String mode = aktif.getString("mode");
                        String exp = aktif.getString("exp");
                        db.AddRef(devid, no_ref, mode, exp, "ya");
                        session.setLogin(true);

                        Toast.makeText(AppActivity.this, "Pemeriksaan Perangkat selesai", Toast.LENGTH_SHORT).show();

                        callback.call();

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "ERROR 1: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage() + "Pemeriksaan Perangkat gagal", Toast.LENGTH_SHORT).show();
                try {
//                    hideDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("dev_id", devId);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    protected void getFakturTokoToday(List dataTransaksi) {

    }


    public class ScanDeviceCallback {
        public void call(){

        }
    }
}
