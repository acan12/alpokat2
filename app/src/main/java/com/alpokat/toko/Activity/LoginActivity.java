package com.alpokat.toko.Activity;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alpokat.toko.Helper.DataHandler;
import com.alpokat.toko.Helper.SessionManager;
import com.alpokat.toko.R;
import com.alpokat.toko.Setting.AppConfig;
import com.alpokat.toko.Setting.AppController;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppActivity {

    @BindView(R.id.login)
    Button login;
    @BindView(R.id.email)
    EditText email;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.back_office)
    TextView backOffice;

//    Button login = findViewById(R.id.login);
//    email = findViewById(R.id.email);
//    password = findViewById(R.id.password);
//    back_office = findViewById(R.id.BackOffice);

    //    private EditText email, password;
//    private TextView back_office;
    private ProgressDialog pDialog;
    private DataHandler db;
    private SessionManager session;
    private String dev_id;
    private Boolean cek;


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        setupPermissionApp(this);

        backOffice.setOnClickListener(new View.OnClickListener() {
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
        db = new DataHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProsesLogin(email.getText().toString(), password.getText().toString());
            }
        });


    }

    private void ProsesLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Sedang Login ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    hideDialog();
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

                        db.LoginUser(id_kasir, nama_kasir, id_toko, nama_toko, alamat, hp, header, footer);
//                        checkDeviceID(dev_id);

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra(AppConfig.FROM_LOGIN, true);
                        startActivity(intent);
                        finish();

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "error 2", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                backOffice.setText(error.getMessage());
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                try {
                    hideDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void showDialog() {
        if (pDialog != null && !pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() throws Exception {
        if (pDialog != null && pDialog.isShowing())
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

//            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
//            dev_id = deviceUuid.toString();
        }
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
