package com.alpokat.kasir.Model;

public class StatsModel {

    private int count;
    private int jumlah;
    private long total;
    private String tanggal;

    public StatsModel(int count, int jumlah, long total, String tanggal) {
        this.count = count;
        this.jumlah = jumlah;
        this.total = total;
        this.tanggal = tanggal;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }
}
