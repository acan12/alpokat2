package com.alpokat.toko.support;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

@SuppressLint("StaticFieldLeak")
public class DownloadPhoto extends AsyncTask<String, String, String> {
    private Context context;

    public DownloadPhoto(Context context) {
        this.context = context;
    }

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
                    Toast.makeText(context.getApplicationContext(), "tidak bisa membuat folder", Toast.LENGTH_LONG).show();
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