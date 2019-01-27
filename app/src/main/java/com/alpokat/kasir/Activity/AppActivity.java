package com.alpokat.kasir.Activity;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.alpokat.kasir.Helper.SQLiteHandler;
import com.alpokat.kasir.Model.PenjualanModel;
import com.alpokat.kasir.Model.api.HttpsTrustManager;
import com.alpokat.kasir.Model.api.TransaksiModel;
import com.alpokat.kasir.Setting.AppConfig;
import com.alpokat.kasir.Setting.AppController;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import app.beelabs.com.codebase.base.BaseActivity;

public class AppActivity extends BaseActivity {

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

    protected void callFakturPenjualanToko(int idToko) {
//        SQLiteHandler db = new SQLiteHandler(getApplicationContext());
//        db.getTransaction(idToko, tgl);
        HttpsTrustManager.allowAllSSL(this);
        JsonArrayRequest MasukReq = new JsonArrayRequest(AppConfig.FAKTUR + idToko,
                new com.android.volley.Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("DEBUG", "testing");

                        List<TransaksiModel> models = new ArrayList();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                TransaksiModel trx = new TransaksiModel();
                                trx.setFaktur(obj.getString("faktur"));
                                trx.setIdToko(Integer.valueOf(obj.getString("id_toko")));
                                trx.setJumlah(Integer.valueOf(obj.getString("jumlah")));
                                trx.setTotal(Long.valueOf(obj.getString("total")));

                                models.add(trx);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                        getFakturTokoToday(models);


//
//                        if (response.length() == 0) {
//                            Toast.makeText(getApplicationContext(), "Faktur tidak ditemukan", Toast.LENGTH_LONG).show();
//                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        AppController.getInstance().addToRequestQueue(MasukReq);

    }

    protected void getFakturTokoToday(List<TransaksiModel> models) {
    }
}
