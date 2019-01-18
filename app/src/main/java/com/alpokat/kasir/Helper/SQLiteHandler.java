
package com.alpokat.kasir.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {


	public SQLiteHandler(Context context) {
		super(context, "data.db", null, 7);
		SQLiteDatabase db = this.getWritableDatabase();
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE_1 = "CREATE TABLE kasir ("
				+ "id 			INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "id_kasir 	TEXT,"
				+ "nama_kasir 	TEXT,"
				+ "id_toko 		TEXT,"
				+ "nama_toko 	TEXT,"
				+ "alamat	 	TEXT,"
				+ "hp		 	TEXT,"
				+ "header	 	TEXT,"
				+ "footer		TEXT)";
		db.execSQL(CREATE_TABLE_1);
		String CREATE_TABLE_2 = "CREATE TABLE referensi ("
				+ "id 			INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "devid		TEXT,"
				+ "no_ref		TEXT,"
				+ "mode			TEXT,"
				+ "exp			DATE,"
				+ "sinkron		TEXT,"
				+ "langganan	TEXT,"
				+ "id_kasir		TEXT)";
		db.execSQL(CREATE_TABLE_2);
		String CREATE_TABLE_3 = "CREATE TABLE keranjang ("
				+ "id 			INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "no_faktur	TEXT,"
				+ "id_produk	TEXT,"
				+ "nama_produk	TEXT,"
				+ "jumlah		INTEGER,"
				+ "harga_jual	BIGINT,"
				+ "total		BIGINT)";
		db.execSQL(CREATE_TABLE_3);
		String CREATE_TABLE_4 = "CREATE TABLE produk ("
				+ "id 			INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "id_produk	TEXT,"
				+ "nama_produk	TEXT,"
				+ "foto_produk	TEXT,"
				+ "harga		TEXT,"
				+ "harga_indo	TEXT,"
				+ "last_update	TEXT,"
				+ "favorit		TEXT DEFAULT '',"
				+ "kategori		TEXT,"
				+ "barcode 		TEXT)";
		db.execSQL(CREATE_TABLE_4);
		String CREATE_TABLE_5 = "CREATE TABLE transaksi ("
				+ "id 			INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "id_toko		TEXT,"
				+ "id_produk	TEXT,"
				+ "jumlah		TEXT,"
				+ "id_kasir		TEXT,"
				+ "id_pelanggan	TEXT,"
				+ "faktur		TEXT,"
				+ "tanggal		TEXT)";
		db.execSQL(CREATE_TABLE_5);
		String CREATE_TABLE_6 = "CREATE TABLE pelanggan ("
				+ "id 			INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "id_pelanggan	TEXT,"
				+ "nama			TEXT,"
				+ "hp			TEXT,"
				+ "alamat		TEXT,"
				+ "poin			TEXT)";
		db.execSQL(CREATE_TABLE_6);

	}


	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS kasir");
		db.execSQL("DROP TABLE IF EXISTS faktur_urut");
		db.execSQL("DROP TABLE IF EXISTS referensi");
		db.execSQL("DROP TABLE IF EXISTS keranjang");
		db.execSQL("DROP TABLE IF EXISTS produk");
		db.execSQL("DROP TABLE IF EXISTS transaksi");
		db.execSQL("DROP TABLE IF EXISTS pelanggan");
		onCreate(db);
	}


	public void TambahTransaksi(String id_toko,
								String id_produk,
								String jumlah,
								String id_kasir,
								String id_pelanggan,
								String faktur,
								String tanggal)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id_toko", id_toko);
		values.put("id_produk", id_produk);
		values.put("jumlah", jumlah);
		values.put("id_kasir", id_kasir);
		values.put("id_pelanggan", id_pelanggan);
		values.put("faktur", faktur);
		values.put("tanggal", tanggal);
		db.insert("transaksi", null, values);
		db.close(); // Closing database connection
	}

	public void TambahProduk( String id_produk,
							  String nama_produk,
							  String foto_produk,
							  String harga,
							  String harga_indo,
							  String last_update,
							  String favorit,
							  String kategori,
							  String barcode)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id_produk", id_produk);
		values.put("nama_produk", nama_produk);
		values.put("foto_produk", foto_produk);
		values.put("harga", harga);
		values.put("harga_indo", harga_indo);
		values.put("last_update", last_update);
		values.put("favorit", favorit);
		values.put("kategori", kategori);
		values.put("barcode", barcode);

		db.delete("produk", "id_produk='" + id_produk+"'", null);
		db.insert("produk", null, values);
		db.close();
	}

	public void TambahPelanggan( String id_pelanggan,
								 String nama,
								 String hp,
								 String alamat,
								 String poin)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id_pelanggan", id_pelanggan);
		values.put("nama", nama);
		values.put("hp", hp);
		values.put("alamat", alamat);
		values.put("poin", poin);
		db.delete("pelanggan", "id_pelanggan='" + id_pelanggan +"'", null);
		db.insert("pelanggan", null, values);
		db.close();
	}

	public HashMap<String, String> LastUpdateProduk() {
		HashMap<String, String> data = new HashMap<>();
		String selectQuery = "SELECT  * FROM produk ORDER BY last_update DESC";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		data.put("jumlah",String.valueOf(cursor.getCount()));
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			data.put("last_update", cursor.getString(6));
		}
		cursor.close();
		db.close();
		return data;
	}

	public void AddRef(String devid,
					   String no_ref,
					   String mode,
					   String exp,
					   String sinkron)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("devid", devid);
		values.put("no_ref", no_ref);
		values.put("mode", mode);
		values.put("exp", exp);
		values.put("sinkron", sinkron);
		db.insert("referensi", null, values);
		db.close(); // Closing database connection
	}

	public HashMap<String, String> BacaRef() {
		HashMap<String, String> reff = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM referensi";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			reff.put("devid", cursor.getString(1));
			reff.put("no_ref", cursor.getString(2));
			reff.put("mode", cursor.getString(3));
			reff.put("exp", cursor.getString(4));
			reff.put("sinkron", cursor.getString(5));
			reff.put("langganan", cursor.getString(6));
			reff.put("id_kasir", cursor.getString(7));
		}
		cursor.close();
		db.close();
		return reff;
	}


	public void LoginUser(String id_kasir,
						  String nama_kasir,
						  String id_toko,
						  String nama_toko,
						  String alamat,
						  String hp,
						  String header,
						  String footer)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id_kasir", id_kasir);
		values.put("nama_kasir", nama_kasir);
		values.put("id_toko", id_toko);
		values.put("nama_toko", nama_toko);
		values.put("alamat", alamat);
		values.put("hp", hp);
		values.put("header", header);
		values.put("footer", footer);
		db.insert("kasir", null, values);
		db.close(); // Closing database connection
	}

	public void IsiKeranjang( String id_produk,
							  String nama_produk,
							  String jumlah	,
							  String harga_jual,
							  String total)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id_produk", id_produk);
		values.put("nama_produk", nama_produk);
		values.put("jumlah", jumlah);
		values.put("harga_jual", harga_jual);
		values.put("total", total);
		db.insert("keranjang", null, values);
		db.close(); // Closing database connection
	}



	public HashMap<String, Integer> HitungItemBelanja(String id_produk) {
		HashMap<String, Integer> data = new HashMap<>();
		String selectQuery = "SELECT  * FROM keranjang WHERE id_produk='" + id_produk + "'";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		data.put("jumlah", Integer.valueOf(cursor.getCount()));
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			data.put("jumlah_produk", cursor.getInt(4));
			data.put("harga_jual", cursor.getInt(5));
		}
		cursor.close();
		db.close();
		return data;
	}


	/**
	 * Getting user data from database
	 * */
	public HashMap<String, String> BacaKasir() {
		HashMap<String, String> kasir = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM kasir";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			kasir.put("id_kasir", cursor.getString(1));
			kasir.put("nama_kasir", cursor.getString(2));
			kasir.put("id_toko", cursor.getString(3));
			kasir.put("nama_toko", cursor.getString(4));
			kasir.put("alamat", cursor.getString(5));
			kasir.put("hp", cursor.getString(6));
			kasir.put("header", cursor.getString(7));
			kasir.put("footer", cursor.getString(8));
		}
		cursor.close();
		db.close();
		return kasir;
	}


	/**
	 * Re crate database Delete all tables and create them again
	 * */
	public void HapusUser() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("kasir", null, null);
		db.delete("keranjang", null, null);
		db.delete("referensi", null, null);
		db.close();
	}

	public void HapusProduk(){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("produk", null, null);
		db.close();
	}

	public void HapusPelanggan(){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("pelanggan", null, null);
		db.close();
	}

	public void HapusTransaksi(){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("transaksi", null, null);
		db.close();
	}

	public void ReturTransaksi(String no_faktur){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("transaksi", "faktur='"+ no_faktur +"'", null);
		db.close();
	}

	public Cursor getProduct(){
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT  * FROM produk ORDER BY last_update DESC";
		Cursor cursor = db.rawQuery(query, null);
		return cursor;
	}


	public ArrayList<HashMap<String, String>> getAllProduct(){
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<HashMap<String, String>> productList = new ArrayList<>();
		String query = "SELECT  * FROM produk WHERE nama_produk IS NOT NULL AND nama_produk != '' ORDER BY LOWER(nama_produk) ASC";
		Cursor cursor = db.rawQuery(query,null);
		while (cursor.moveToNext()){
			HashMap<String,String> produk = new HashMap<>();
			produk.put("barcode",cursor.getString(9));
			produk.put("description",cursor.getString(2));
			produk.put("price_show",cursor.getString(4));
			produk.put("price",cursor.getString(5));
			productList.add(produk);
		}
		return  productList;
	}


	public ArrayList<HashMap<String, String>> searchProductByBarcode(String barcode){
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<HashMap<String, String>> productList = new ArrayList<>();
		String query = "SELECT  * FROM produk WHERE nama_produk IS NOT NULL AND nama_produk != '' AND barcode == '"+barcode+"'ORDER BY LOWER(nama_produk) ASC";
		Cursor cursor = db.rawQuery(query,null);
		while (cursor.moveToNext()){
			HashMap<String,String> produk = new HashMap<>();
			produk.put("barcode",cursor.getString(9));
			produk.put("description",cursor.getString(2));
			produk.put("price_show",cursor.getString(4));
			produk.put("price",cursor.getString(5));
			productList.add(produk);
		}
		return  productList;
	}

	public ArrayList<HashMap<String, String>> searchProductByText(String input){
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<HashMap<String, String>> productList = new ArrayList<>();
		String query = "SELECT  * FROM produk WHERE nama_produk IS NOT NULL AND nama_produk != '' AND nama_produk LIKE '%"+input+"%' ORDER BY LOWER(nama_produk) ASC";
		Cursor cursor = db.rawQuery(query,null);
		while (cursor.moveToNext()){
			HashMap<String,String> produk = new HashMap<>();
			produk.put("barcode",cursor.getString(9));
			produk.put("description",cursor.getString(2));
			produk.put("price_show",cursor.getString(4));
			produk.put("price",cursor.getString(5));
			productList.add(produk);
		}
		return  productList;
	}



}
