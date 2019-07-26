package com.alpokat.toko.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alpokat.toko.Activity.PenjualanActivity;
import com.alpokat.toko.Activity.PenjualanBarcodeBluetoothActivity;
import com.alpokat.toko.Helper.SQLiteHandler;
import com.alpokat.toko.Helper.SqlHelper;
import com.alpokat.toko.Model.BelanjaModel;
import com.alpokat.toko.Model.realm.Keranjang;
import com.alpokat.toko.R;
import com.alpokat.toko.Setting.AppController;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.realm.RealmResults;

/**
 * Created by MacBookPro on 27/11/17.
 */


public class BelanjaAdapter extends RecyclerView.Adapter<BelanjaAdapter.MyViewHolder> {


    public Context mContext;
    public List<BelanjaModel> belanja_list;


    public BelanjaAdapter(Context mContext, List<BelanjaModel> belanja_list) {
        this.mContext = mContext;
        this.belanja_list = belanja_list;


    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_belanja, parent, false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {


        BelanjaModel obj = belanja_list.get(position);

        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String harga = formatRupiah.format(Double.parseDouble(obj.getHarga()));
        String total = formatRupiah.format(Double.parseDouble(obj.getTotal()));

        holder.id_produk.setText(obj.getId_produk());
        holder.nama_produk.setText(obj.getNama_produk());
        holder.harga.setText(harga);
        holder.jumlah.setText(obj.getJumlah());
        holder.total.setText(total);

    }

    @Override
    public int getItemCount() {
        return belanja_list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView id_produk,
                nama_produk,
                harga,
                total,
                jumlah,
                plus,
                minus;


        public MyViewHolder(View view) {
            super(view);
            nama_produk = view.findViewById(R.id.nama_produk);
            harga = view.findViewById(R.id.harga_jual);
            id_produk = view.findViewById(R.id.id_produk);
            total = view.findViewById(R.id.total_belanja);
            jumlah = view.findViewById(R.id.jumlah);
            plus = view.findViewById(R.id.plus);
            minus = view.findViewById(R.id.minus);


            plus.setOnClickListener(new View.OnClickListener() {
                public SqlHelper dbcenter;

                @Override
                public void onClick(View view) {

                    SQLiteHandler db = new SQLiteHandler(mContext);
                    HashMap<String, Integer> hitung = db.HitungItemBelanja(id_produk.getText().toString());
                    int m = hitung.get("harga_jual");

                    int x = Integer.valueOf(jumlah.getText().toString());
                    final int y = x + 1;
                    final int t = y * m;
                    jumlah.setText(y + "");
                    total.setText(t + "");


//                    dbcenter = new SqlHelper(mContext);
//                    SQLiteDatabase db1 = dbcenter.getWritableDatabase();
//                    db1.execSQL("UPDATE keranjang SET jumlah ='" + y + "'," +
//                            " total='" + t + "' " +
//                            " WHERE id_produk='" + id_produk.getText().toString() + "'");

                    final String produkId = id_produk.getText().toString();

                    db.updateProduct(produkId, y, t);

                    try {
                        PenjualanActivity.PA.LoadTotalBelanja();
                        PenjualanActivity.PA.LoadKeranjang();
                    } catch (Exception e) {
                        PenjualanBarcodeBluetoothActivity.PA.LoadTotalBelanja();
                        PenjualanBarcodeBluetoothActivity.PA.LoadKeranjang();
                    }


                }
            });
            minus.setOnClickListener(new View.OnClickListener() {
                public SqlHelper dbcenter;

                @Override
                public void onClick(View view) {

                    try {
                        SQLiteHandler db = new SQLiteHandler(mContext);
                        HashMap<String, Integer> hitung = db.HitungItemBelanja(id_produk.getText().toString());
                        int m = hitung.get("harga_jual");


                        int x = Integer.valueOf(jumlah.getText().toString());
                        if (x > 1) {
                            int y = x - 1;
                            int t = y * m;
                            jumlah.setText(y + "");
                            total.setText(t + "");

//                            dbcenter = new SqlHelper(mContext);
//                            SQLiteDatabase db1 = dbcenter.getWritableDatabase();
//                            db1.execSQL("UPDATE keranjang SET jumlah ='" + y + "'," +
//                                    " total='" + t + "' " +
//                                    " WHERE id_produk='" + id_produk.getText().toString() + "'");
//                            String produkId = id_produk.getText().toString();
//                            Keranjang keranjang = (Keranjang) AppController.getDb().getCollectionByKeyRealm("id_produk", produkId, Keranjang.class).get(0);
//                            keranjang.setJumlah(y);
//                            keranjang.setTotal(t);
//                            AppController.getDb().saveToRealm(keranjang);
                            final String produkId = id_produk.getText().toString();
                            db.updateProduct(produkId, y, t);

                            try {
                                PenjualanActivity.PA.LoadTotalBelanja();
                                PenjualanActivity.PA.LoadKeranjang();
                            } catch (Exception e) {
                                PenjualanBarcodeBluetoothActivity.PA.LoadTotalBelanja();
                                PenjualanBarcodeBluetoothActivity.PA.LoadKeranjang();
                            }
                        } else {

                            // Do nothing but resetCart the dialog
//                            SqlHelper dbcenter = new SqlHelper(mContext);
//                            SQLiteDatabase db2 = dbcenter.getWritableDatabase();
//                            db2.execSQL("DELETE FROM keranjang WHERE id_produk='" + id_produk.getText().toString() + "'");
                            String produkId = id_produk.getText().toString();
                            RealmResults<Keranjang> keranjangList = AppController.getDb().getCollectionByKeyRealm("id_produk", produkId, Keranjang.class);
                            AppController.getDb().deleteRealmBykey("id_produk", keranjangList.get(0).getId_produk(), Keranjang.class);
                            try {
                                PenjualanActivity.PA.LoadTotalBelanja();
                                PenjualanActivity.PA.LoadKeranjang();
                            } catch (Exception e) {
                                PenjualanBarcodeBluetoothActivity.PA.LoadTotalBelanja();
                                PenjualanBarcodeBluetoothActivity.PA.LoadKeranjang();
                            }

                        }
                    } catch (Exception e) {
                        Log.e("ERROR", "error");
                    }

                }
            });

        }
    }


}
