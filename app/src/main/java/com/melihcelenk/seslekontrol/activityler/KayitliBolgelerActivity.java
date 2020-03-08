package com.melihcelenk.seslekontrol.activityler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.melihcelenk.seslekontrol.modeller.Bolge;
import com.melihcelenk.seslekontrol.DatabaseHandler;
import com.melihcelenk.seslekontrol.R;

import java.util.ArrayList;

public class KayitliBolgelerActivity extends AppCompatActivity {
    DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kayitli_bolgeler);

        db = new DatabaseHandler(this);

        ArrayList<Bolge> butunBolgeler = (ArrayList<Bolge>) db.getButunBolgeler();
        for (Bolge cn : butunBolgeler) {
            String log = "Id: " + cn.get_id() + " ,Etiket: " + cn.get_etiket() + " ,MAC: " + cn.get_macAdresi() + " ,IP: " +
                    cn.get_ipAdresi();
            Log.d("BolgeBilgi: ", log);
        }
    }
}
