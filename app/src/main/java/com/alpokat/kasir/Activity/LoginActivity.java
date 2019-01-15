package com.alpokat.kasir.Activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.alpokat.kasir.Helper.SessionManager;
import com.alpokat.kasir.Model.api.HttpsTrustManager;
import com.alpokat.kasir.Setting.AppConfig;
import com.alpokat.kasir.Helper.SQLiteHandler;
import com.alpokat.kasir.R;
import com.alpokat.kasir.Setting.AppController;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {


    private EditText email, password;
    private TextView back_office;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;
    private String dev_id;
    private Boolean cek;


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE
        };

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }


        Button login = findViewById(R.id.login);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        back_office = findViewById(R.id.BackOffice);

        back_office.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(AppConfig.HOST);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProsesLogin(email.getText().toString(), password.getText().toString());
            }
        });




    }

    private void cek_dev_id(final String id_def) {
        // Tag used to cancel the request
        String tag_string_req = "req_aktif";

        pDialog.setMessage("Memeriksa Perangkat");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.DEVID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    cek = error;

                    Intent i;
                    if(cek){
                        i = new Intent(getApplicationContext(),RefActivity.class);
                    }else{
                        i = new Intent(getApplicationContext(),MainActivity.class);

                        JSONObject aktif = jObj.getJSONObject("aktif");
                        String devid = aktif.getString("devid");
                        String no_ref = aktif.getString("no_ref");
                        String mode = aktif.getString("mode");
                        String exp = aktif.getString("exp");
                        db.AddRef(devid, no_ref, mode, exp, "ya");
                        session.setLogin(true);
                    }
                    startActivity(i);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "ERROR 1: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.getMessage() + " ERROR 2", Toast.LENGTH_SHORT).show();
                back_office.setText(error.getMessage());
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("dev_id",id_def);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void ProsesLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Sedang Login ...");
        showDialog();

        HttpsTrustManager.allowAllSSL();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    System.out.println(jObj);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        JSONObject kasir = jObj.getJSONObject("kasir");
                        String id_kasir = kasir.getString("id_kasir");
                        String nama_kasir = kasir.getString("nama_kasir");
                        String id_toko = kasir.getString("id_toko");
                        String nama_toko = kasir.getString("nama_toko");
                        String alamat = kasir.getString("alamat");
                        String hp = kasir.getString("hp");
                        String header = kasir.getString("header");
                        String footer = kasir.getString("footer");

                        db.LoginUser(id_kasir,nama_kasir,id_toko,nama_toko,alamat,hp,header,footer);
                        cek_dev_id(dev_id);
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);



                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),  "error 2", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.toString() , Toast.LENGTH_SHORT).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("email",email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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

            UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
            dev_id = deviceUuid.toString();
        }
        return true;
    }


}
