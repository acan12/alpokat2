package com.alpokat.kasir.Setting;


public class AppConfig {

    public static String HOST = "https://toko.alpokat.com/";
//    public static String HOST = "http://192.168.1.9:8086/toko/";
    public static String SERVER                 = HOST + "android/";
    public static String ADD_PELANGGAN          = SERVER + "add_pelanggan.php";
    public static String LIST_PELANGGAN         = SERVER + "list_pelanggan.php?id_toko=";
    public static String LOGIN                  = SERVER + "login.php";
    public static String LIST_PRODUK            = SERVER + "list_barang.php?id_toko=";
    public static String FOTO                   = HOST   + "foto_produk/";
    public static String BAYAR                  = SERVER + "tes.php";
    public static String HAPUS_PELANGGAN        = SERVER + "hapus_pelanggan.php?id_pelanggan=";
    public static String DATA_KASIR             = SERVER + "data_kasir.php";
    public static String UPDATE_KASIR           = SERVER + "update_kasir.php";
    public static String UPDATE_PASSWORD        = SERVER + "update_password.php";
    public static String KONFIRMASI_RETUR       = SERVER + "retur_password.php";
    public static String DEVID                  = SERVER + "devid.php";
    public static String ADD_REF                = SERVER + "add_ref.php";
    public static String FAKTUR                 = SERVER + "list_faktur.php?id_toko=";
    public static String STATS                  = SERVER + "stats.php?id_toko=idtoko&tanggal=tgl";
    public static String LISENSI                = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyHBsY0XQ85WL5HfuI/N9rbnZgp3Sm8prtAtXA133mQqRyOpn+bJ2O33gTLxAdn3qpbyjnPenRojjGeVzj0DkAtDpGd/XFKODaDPRZBkP32LDSAFKoopBAyY1WClLVQYgIOodEq/+2x7uafdtfzYzlY21WBzGqYlQ4jruG2RpP3eJtfZUtrsCHiBmOBlbpQvNaD1sAudHvjW57cYOBh/ifXBPLQ+V2NsJmHKQCO6Lt3+Bc+g86m8192dfEwFwH3COepQuSeDDLvRVz36a2IGvoHXRDmDuBPQhUthudDKveWbgEKolGTo5TXODudspEZCvAzdJVJv/fSLJVSI93Q6b2QIDAQAB";
    public static String CEK_PRODUK             = SERVER + "cek_produk.php";
    public static String SIMPAN_PRODUK          = SERVER + "add_produk.php";
}