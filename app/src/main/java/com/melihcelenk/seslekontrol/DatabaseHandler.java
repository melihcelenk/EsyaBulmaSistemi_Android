package com.melihcelenk.seslekontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.melihcelenk.seslekontrol.modeller.Bolge;
import com.melihcelenk.seslekontrol.modeller.Esya;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "esyaBulmaVT";

    private static final String TABLE_BOLGELER = "bolgeler";
    private static final String KEY_ID = "id";
    private static final String KEY_ETIKET = "etiket";
    private static final String KEY_MAC_ADRESI = "mac_adresi";
    private static final String KEY_IP_ADRESI = "ip_adresi";

    private static final String TABLE_ESYALAR = "esyalar";
    private static final String KEY_ESYA_ID = "esyaID";
    private static final String KEY_BOLGE_ID = KEY_ID;
    private static final String KEY_ESYA_ADI = "esyaAdi";

    private static final String TABLE_ESYA_ANAHTAR_KELIMELER = "esya_anahtar_kelimeler";
    //private static final String KEY_ESYA_ID = "esyaID";
    private static final String KEY_ESYA_ANAHTAR = "esyaAnahtar";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BOLGELER_TABLOSU = "CREATE TABLE " + TABLE_BOLGELER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ETIKET + " TEXT,"
                + KEY_MAC_ADRESI + " TEXT NOT NULL UNIQUE," + KEY_IP_ADRESI + " TEXT" + ")";
        db.execSQL(CREATE_BOLGELER_TABLOSU);

        String CREATE_ESYALAR_TABLOSU = "CREATE TABLE " + TABLE_ESYALAR + "("
                + KEY_ESYA_ID + " INTEGER PRIMARY KEY,"
                + KEY_BOLGE_ID + " INTEGER NOT NULL,"
                + KEY_ESYA_ADI + " TEXT NOT NULL," +
                " FOREIGN KEY ("+ KEY_BOLGE_ID +")" +
                "       REFERENCES "+TABLE_BOLGELER+" ("+ KEY_ID +") "
                + ")";
        db.execSQL(CREATE_ESYALAR_TABLOSU);

        String CREATE_ESYA_ANAHTAR_KELIMELER_TABLOSU = "CREATE TABLE " + TABLE_ESYA_ANAHTAR_KELIMELER + "("
                + KEY_ESYA_ID + " INTEGER,"
                + KEY_ESYA_ANAHTAR + " TEXT NOT NULL," +
                " FOREIGN KEY ("+ KEY_ESYA_ID +")" +
                "       REFERENCES "+TABLE_ESYALAR+" ("+ KEY_ESYA_ID +") "
                + ")";
        db.execSQL(CREATE_ESYA_ANAHTAR_KELIMELER_TABLOSU);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOLGELER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ESYALAR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ESYA_ANAHTAR_KELIMELER);
        // Create tables again
        onCreate(db);
    }

    // yeni bölge ekleme
    long addBolge(Bolge bolge) { // var olan ID'yi silip yeni ID ekliyor
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ETIKET, bolge.get_etiket());
        values.put(KEY_MAC_ADRESI, bolge.get_macAdresi());
        values.put(KEY_IP_ADRESI, bolge.get_ipAdresi());
        long id = db.insertWithOnConflict(TABLE_BOLGELER,null,values,SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return id;
    }

    public int sonGuncellenenID() {
        SQLiteDatabase db = this.getWritableDatabase();

        final String MY_QUERY = "SELECT last_insert_rowid()";
        Cursor cur = db.rawQuery(MY_QUERY, null);
        cur.moveToFirst();
        int ID = cur.getInt(0);
        cur.close();
        db.close();
        return ID;
    }

    public int ekleVeyaDegistirBolge(Bolge bolge){
        SQLiteDatabase db = this.getWritableDatabase();

        String EKLE_VEYA_DEGISTIR_BOLGE = "insert or replace into bolgeler(id,etiket,mac_adresi,ip_adresi) " +
                "values(" +
                "   (select id from bolgeler where mac_adresi = \"" + bolge.get_macAdresi() + "\")," +
                "   \"" + bolge.get_etiket() + "\"," +
                "   \"" + bolge.get_macAdresi() + "\"," +
                "   \"" + bolge.get_ipAdresi() + "\")";
        db.execSQL(EKLE_VEYA_DEGISTIR_BOLGE);
        int sonGuncID = sonGuncellenenID();
        db.close();

        return sonGuncID;
    }

    public int ipDegistir(String mac, String ip){
        SQLiteDatabase db = this.getWritableDatabase();
        String IP_DEGISTIR = "update bolgeler set ip_adresi = '" + ip + "' WHERE mac_adresi='" + mac + "'";
        db.execSQL(IP_DEGISTIR);
        int sonGuncID = sonGuncellenenID();
        db.close();
        return sonGuncID;
    }

    public String etiketGetir(String macAdresi){
        SQLiteDatabase db = this.getReadableDatabase();
        String ETIKETGETIR = "select etiket from bolgeler where mac_adresi = \"" + macAdresi + "\"";
        Cursor cursor = db.rawQuery(ETIKETGETIR, null);
        if (cursor != null)
            cursor.moveToFirst();
        String etiket = cursor.getString(0);
        cursor.close();
        db.close();
        return etiket;
    }
    public String etiketGetirBolgeIdIle(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        String ETIKETGETIR = "select etiket from bolgeler where "+KEY_ID+" = \"" + id + "\"";
        Cursor cursor = db.rawQuery(ETIKETGETIR, null);
        if (cursor != null)
            cursor.moveToFirst();
        String etiket = cursor.getString(0);
        cursor.close();
        db.close();
        return etiket;
    }

    public String idGetir(String macAdresi){
        SQLiteDatabase db = this.getReadableDatabase();
        String IDGETIR = "select id from bolgeler where mac_adresi = \"" + macAdresi + "\"";
        Cursor cursor = db.rawQuery(IDGETIR, null);
        if (cursor != null)
            cursor.moveToFirst();
        String id = cursor.getString(0);
        cursor.close();
        db.close();
        return id;
    }

    public String ipGetir(String macAdresi){
        SQLiteDatabase db = this.getReadableDatabase();
        String IPGETIR = "select ip_adresi from bolgeler where mac_adresi = \"" + macAdresi + "\"";
        Cursor cursor = db.rawQuery(IPGETIR, null);
        if (cursor != null)
            cursor.moveToFirst();
        String ip = cursor.getString(0);
        cursor.close();
        db.close();
        return ip;
    }

    public String ipGetirBolgeIdIle(int bolgeId){
        SQLiteDatabase db = this.getReadableDatabase();
        String IPGETIR = "select ip_adresi from bolgeler where "+ KEY_BOLGE_ID + "= \"" + bolgeId + "\"";
        Cursor cursor = db.rawQuery(IPGETIR, null);
        if (cursor != null)
            cursor.moveToFirst();
        String ip = cursor.getString(0);
        cursor.close();
        db.close();
        return ip;
    }

    public String ipGetirEtiketIle(String etiket){
        SQLiteDatabase db = this.getReadableDatabase();
        String IPGETIR = "select ip_adresi from bolgeler where etiket = \"" + etiket + "\" COLLATE NOCASE";
        Cursor cursor = db.rawQuery(IPGETIR, null);
        if (cursor != null)
            cursor.moveToFirst();
        String ip = cursor.getString(0);
        cursor.close();
        db.close();
        return ip;
    }
    public int idGetirEtiketIle(String etiket){
        SQLiteDatabase db = this.getReadableDatabase();
        String IDGETIR = "select id from bolgeler where etiket = \"" + etiket + "\" COLLATE NOCASE";
        Cursor cursor = db.rawQuery(IDGETIR, null);
        if (cursor != null)
            cursor.moveToFirst();
        int id = cursor.getInt(0);
        cursor.close();
        db.close();
        return id;
    }

    public Bolge getBolge(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BOLGELER, new String[] { KEY_ID,
                        KEY_ETIKET, KEY_MAC_ADRESI, KEY_IP_ADRESI }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Bolge bolge = new Bolge(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),cursor.getString(3));
        db.close();
        return bolge;
    }

    public Bolge getBolgeMacIle(String mac) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BOLGELER, new String[] { KEY_ID,
                        KEY_ETIKET, KEY_MAC_ADRESI, KEY_IP_ADRESI }, KEY_MAC_ADRESI + "=?",
                new String[] { mac }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Bolge bolge = new Bolge(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),cursor.getString(3));
        db.close();
        return bolge;
    }

    public Bolge getBolgeEtiketIle(String etiket) {
        SQLiteDatabase db = this.getReadableDatabase();
        Bolge bolge = null;
        Cursor cursor = db.query(TABLE_BOLGELER, new String[] { KEY_ID,
                        KEY_ETIKET, KEY_MAC_ADRESI, KEY_IP_ADRESI }, KEY_ETIKET + "=? COLLATE NOCASE",
                new String[] { etiket }, null, null, null, null);
        if (cursor != null){
            if(cursor.moveToFirst()) {
                bolge = new Bolge(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3));
            }
        }


        db.close();
        return bolge;
    }

    public List<Bolge> getButunBolgeler() {
        List<Bolge> bolgeList = new ArrayList<Bolge>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_BOLGELER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Bolge bolge = new Bolge();
                bolge.set_id(Integer.parseInt(cursor.getString(0)));
                bolge.set_etiket(cursor.getString(1));
                bolge.set_macAdresi(cursor.getString(2));
                bolge.set_ipAdresi(cursor.getString(3));
                // Adding bolge to list
                bolgeList.add(bolge);
            } while (cursor.moveToNext());
        }
        db.close();
        return bolgeList;
    }


    public int updateBolge(Bolge bolge) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ETIKET, bolge.get_etiket());
        values.put(KEY_MAC_ADRESI, bolge.get_macAdresi());
        values.put(KEY_IP_ADRESI, bolge.get_ipAdresi());
        int x = db.update(TABLE_BOLGELER, values, KEY_ID + " = ?",
                new String[] { String.valueOf(bolge.get_id()) });
        db.close();
        return x;
    }

    public void deleteBolge(Bolge bolge) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOLGELER, KEY_ID + " = ?",
                new String[] { String.valueOf(bolge.get_id()) });
        db.close();
    }

    // Getting contacts Count
    public int getBolgeSayisi() {
        String countQuery = "SELECT  * FROM " + TABLE_BOLGELER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int x = cursor.getCount();
        cursor.close();
        db.close();
        // return count
        return x;
    }

    public List<Esya> getButunEsyalar() {
        List<Esya> esyaList = new ArrayList<Esya>();
        String selectQuery = "SELECT * FROM " + TABLE_ESYALAR;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Esya esya = new Esya();
                esya.set_esyaId(Integer.parseInt(cursor.getString(0)));
                esya.set_bolgeId(Integer.parseInt(cursor.getString(1)));
                esya.set_esyaAdi(cursor.getString(2));
                esya.set_esyaAnahtarKelimeler(AraEslesenEsyaAnahtarKelimeler(esya.get_esyaId()));
                esyaList.add(esya);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return esyaList;
    }
    public Esya EsyaGetirIdIle(int esyaID) {
        String selectQuery = "SELECT * FROM " + TABLE_ESYALAR + " WHERE "+ KEY_ESYA_ID +" = " + esyaID ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Esya esya = new Esya();
        if (cursor.moveToFirst()) {

            esya.set_esyaId(Integer.parseInt(cursor.getString(0)));
            esya.set_bolgeId(Integer.parseInt(cursor.getString(1)));
            esya.set_esyaAdi(cursor.getString(2));
            esya.set_esyaAnahtarKelimeler(AraEslesenEsyaAnahtarKelimeler(esyaID));

        }
        cursor.close();
        db.close();
        return esya;
    }

    public long addEsya(Esya esya) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ESYA_ADI, esya.get_esyaAdi());
        values.put(KEY_BOLGE_ID, esya.get_bolgeId());
        long id = db.insert(TABLE_ESYALAR,null,values);
        db.close();
        return id;
    }

    public ArrayList<Integer> esyaIdGetirEsyaAdiIle(String esyaAdi){
        ArrayList<Integer> idler = new ArrayList<Integer>();

        SQLiteDatabase db = this.getReadableDatabase();
        //String ESYAIDGETIRESYAADIILE = "select "+ KEY_BOLGE_ID +" from "+TABLE_ESYALAR+" where "+KEY_ESYA_ADI+" = \"" + esyaAdi + "\" COLLATE NOCASE";
        String ESYAIDGETIRESYAADIILE = "select " + KEY_ESYA_ID +" from esyalar where esyaAdi = \"" + esyaAdi + "\" COLLATE NOCASE";
        Cursor cursor = db.rawQuery(ESYAIDGETIRESYAADIILE, null);
        int id = -1;
        if (cursor != null && cursor.getCount()>=1){
            if (cursor.moveToFirst()) {
                do {
                    id = Integer.parseInt(cursor.getString(0));
                    idler.add(id);
                } while (cursor.moveToNext());
            }
        }

        else Log.e("DBHata","EsyaAdi ile BölgeId Getirilemedi");

        cursor.close();
        db.close();
        return idler;
    }

    public void addEsyaAnahtarKelimeler(long esyaID, ArrayList<String> esyaAnahtarKelimeler) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for(String e : esyaAnahtarKelimeler){
            if(!e.isEmpty()){
                values.put(KEY_ESYA_ID, esyaID);
                values.put(KEY_ESYA_ANAHTAR, e);
                db.insert(TABLE_ESYA_ANAHTAR_KELIMELER,null,values);
            }
            else{
                Log.v("EsyaAnahtarKelime","Boş string girildi.");
            }

        }
        db.close();
    }
    public void LogButunEsyaAnahtarKelimeler() {

        String selectQuery = "SELECT * FROM " + TABLE_ESYA_ANAHTAR_KELIMELER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Log.v("EsyaAnahtarKelime",(Integer.parseInt(cursor.getString(0))+" "+cursor.getString(1)));;
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    public ArrayList<String> AraEslesenEsyaAnahtarKelimeler(int esyaId) {
        ArrayList<String> anahtarKelimeler = new ArrayList<String>();
        String selectQuery = "SELECT * FROM " + TABLE_ESYA_ANAHTAR_KELIMELER + " WHERE " + KEY_ESYA_ID + " = " + esyaId;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.getCount()>=1){
            if (cursor.moveToFirst()) {
                do {
                    String anahtarKelime = cursor.getString(1);
                    anahtarKelimeler.add(anahtarKelime);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        db.close();
        return anahtarKelimeler;
    }









}
