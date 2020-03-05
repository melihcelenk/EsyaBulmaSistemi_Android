package com.melihcelenk.seslekontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOLGELER);

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

        // Inserting Row
        //db.insert(TABLE_BOLGELER, null, values);
        long id = db.insertWithOnConflict(TABLE_BOLGELER,null,values,SQLiteDatabase.CONFLICT_REPLACE);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
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

    int ekleVeyaDegistirBolge(Bolge bolge){
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
    int ipDegistir(String mac, String ip){
        SQLiteDatabase db = this.getWritableDatabase();
        String IP_DEGISTIR = "update bolgeler set ip_adresi = '" + ip + "' WHERE mac_adresi='" + mac + "'";
        db.execSQL(IP_DEGISTIR);
        int sonGuncID = sonGuncellenenID();
        db.close();
        return sonGuncID;
    }


    String etiketGetir(String macAdresi){
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
    String idGetir(String macAdresi){
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

    String ipGetir(String macAdresi){
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

    // code to get the single contact
    Bolge getBolge(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BOLGELER, new String[] { KEY_ID,
                        KEY_ETIKET, KEY_MAC_ADRESI }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Bolge bolge = new Bolge(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),cursor.getString(3));

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

        return bolgeList;
    }

    public int updateBolge(Bolge bolge) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ETIKET, bolge.get_etiket());
        values.put(KEY_MAC_ADRESI, bolge.get_macAdresi());
        values.put(KEY_IP_ADRESI, bolge.get_ipAdresi());

        // updating row
        return db.update(TABLE_BOLGELER, values, KEY_ID + " = ?",
                new String[] { String.valueOf(bolge.get_id()) });
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
        cursor.close();

        // return count
        return cursor.getCount();
    }

}
