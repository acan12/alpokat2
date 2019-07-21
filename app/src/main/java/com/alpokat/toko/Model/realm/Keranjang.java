package com.alpokat.toko.Model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Keranjang extends RealmObject {

    @PrimaryKey
    private long id;
    private String no_faktur;
    private String id_produk;
    private String nama_produk;
    private long jumlah;
    private long harga_jual;
    private long total;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNo_faktur() {
        return no_faktur;
    }

    public void setNo_faktur(String no_faktur) {
        this.no_faktur = no_faktur;
    }

    public String getId_produk() {
        return id_produk;
    }

    public void setId_produk(String id_produk) {
        this.id_produk = id_produk;
    }

    public String getNama_produk() {
        return nama_produk;
    }

    public void setNama_produk(String nama_produk) {
        this.nama_produk = nama_produk;
    }

    public long getJumlah() {
        return jumlah;
    }

    public void setJumlah(long jumlah) {
        this.jumlah = jumlah;
    }

    public long getHarga_jual() {
        return harga_jual;
    }

    public void setHarga_jual(long harga_jual) {
        this.harga_jual = harga_jual;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
