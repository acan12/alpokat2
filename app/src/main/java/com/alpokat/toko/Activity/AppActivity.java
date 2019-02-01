package com.alpokat.toko.Activity;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.alpokat.toko.Model.StatsModel;
import com.alpokat.toko.Model.api.HttpsTrustManager;
import com.alpokat.toko.Setting.AppConfig;
import com.alpokat.toko.Setting.AppController;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    protected void callStatsPenjualanToko(int idToko) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String today = dateFormat.format(date);

        String endpoint = AppConfig.STATS.replaceAll("idtoko", "" + idToko).replaceAll("tgl", today);

        HttpsTrustManager.allowAllSSL(this);
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


                        getFakturTokoToday(models);

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        AppController.getInstance().addToRequestQueue(MasukReq);

    }

    protected void getFakturTokoToday(List models) {
    }
}
