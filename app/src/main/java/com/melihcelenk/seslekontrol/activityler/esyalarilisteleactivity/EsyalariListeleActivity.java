package com.melihcelenk.seslekontrol.activityler.esyalarilisteleactivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.melihcelenk.seslekontrol.DatabaseHandler;
import com.melihcelenk.seslekontrol.Haberlesme;
import com.melihcelenk.seslekontrol.R;
import com.melihcelenk.seslekontrol.activityler.kurulumactivity.cihazlarAdapter;
import com.melihcelenk.seslekontrol.modeller.Esya;
import com.melihcelenk.seslekontrol.modeller.bulunanCihaz;

import java.util.ArrayList;

public class EsyalariListeleActivity extends AppCompatActivity {

    private RecyclerView esyalarRV;
    private esyalarAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    ArrayList<Esya> bulunanEsyalarArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esyalari_listele);
        final DatabaseHandler db = new DatabaseHandler(this);
        bulunanEsyalarArray = (ArrayList<Esya>) db.getButunEsyalar();

        esyalarRV = (RecyclerView) findViewById(R.id.esyalarRV);
        layoutManager = new LinearLayoutManager(this);
        esyalarRV.setLayoutManager(layoutManager);
        mAdapter = new esyalarAdapter(bulunanEsyalarArray);
        esyalarRV.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new esyalarAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }

            @Override
            public void onSinyalBtnClick(int position) {
                int id = bulunanEsyalarArray.get(position).get_bolgeId();
                String ip = db.ipGetirBolgeIdIle(id);
                Toast.makeText(EsyalariListeleActivity.this, db.etiketGetirBolgeIdIle(id)+ " bölgesine sinyal gönderiliyor.", Toast.LENGTH_SHORT).show();
                Haberlesme.SinyalGonder(ip,id,getApplicationContext());
            }
        });

    }
}
