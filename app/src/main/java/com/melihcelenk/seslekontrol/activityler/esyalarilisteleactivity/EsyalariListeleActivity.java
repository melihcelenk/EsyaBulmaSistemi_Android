package com.melihcelenk.seslekontrol.activityler.esyalarilisteleactivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.melihcelenk.seslekontrol.DatabaseHandler;
import com.melihcelenk.seslekontrol.Haberlesme;
import com.melihcelenk.seslekontrol.IPArkaplanKontrol;
import com.melihcelenk.seslekontrol.R;
import com.melihcelenk.seslekontrol.modeller.Esya;

import java.util.ArrayList;

public class EsyalariListeleActivity extends AppCompatActivity {

    RecyclerView esyalarRV;
    esyalarAdapter mAdapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Esya> bulunanEsyalarArray;
    MutableLiveData<String> ipSonuc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esyalari_listele);
        setTitle("Eşyalar");

        final DatabaseHandler db = new DatabaseHandler(this);
        // Asenkron çalışmanın sonucunu görebilmek için sürekli dinlenilen canlı bir değişken kullanılıyor
        ipSonuc = new MutableLiveData<>();

        try {
            ipSonuc.setValue("Bekleniyor...");
            ipSonuc.observe(EsyalariListeleActivity.this, new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    Log.v("IpSonucDegisti",ipSonuc.getValue());
                }
            });
            AsyncTask<Void, Integer, String> ipArkaplanKontrol = new IPArkaplanKontrol(EsyalariListeleActivity.this,ipSonuc).execute((Void) null);
        }catch(Exception e){}


        Intent intent = getIntent();
        final String listelemeModu = intent.getStringExtra("listelemeModu");
        // Eğer başka bir aktiviteden bütün eşyaların listelenmesi istendiyse
        if(listelemeModu.equals("hepsi")){
            bulunanEsyalarArray = (ArrayList<Esya>) db.getButunEsyalar();
        }
        // Bir eşya araması yapılmışsa ve sadece eşleşenler aranıyorsa
        else if(listelemeModu.equals("ozelArama")){
            final String esyaAdi = intent.getStringExtra("esyaAdi");
            ArrayList<Integer> uyusanIDler =  db.esyaIdGetirEsyaAdiIle(esyaAdi);
            bulunanEsyalarArray = new ArrayList<>();
            for(int i : uyusanIDler){
                bulunanEsyalarArray.add(db.EsyaGetirIdIle(i));
            }
        }


        esyalarRV = (RecyclerView) findViewById(R.id.esyalarRV);
        layoutManager = new LinearLayoutManager(this);
        esyalarRV.setLayoutManager(layoutManager);
        mAdapter = new esyalarAdapter(bulunanEsyalarArray);
        esyalarRV.setAdapter(mAdapter);

        // Adapter çalışma mantığı için bkz esyalarAdapter
        mAdapter.setOnItemClickListener(new esyalarAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                int id = bulunanEsyalarArray.get(position).get_bolgeId();
                String esyaAdi = bulunanEsyalarArray.get(position).get_esyaAdi();
                Toast.makeText(EsyalariListeleActivity.this, esyaAdi + ", "+db.etiketGetirBolgeIdIle(id)+ " bölgesinde", Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onSinyalBtnClick(int position) {
                int id = bulunanEsyalarArray.get(position).get_bolgeId();
                String ip = db.ipGetirBolgeIdIle(id);
                Toast.makeText(EsyalariListeleActivity.this, db.etiketGetirBolgeIdIle(id)+ " bölgesine sinyal gönderiliyor.", Toast.LENGTH_SHORT).show();
                final MutableLiveData<String> ipSonuc = new MutableLiveData<>();
                ipSonuc.setValue("Bekleniyor...");
                ipSonuc.observe(EsyalariListeleActivity.this, new Observer<String>() {
                    @Override
                    public void onChanged(String s) {

                    }
                });
                Haberlesme.SinyalGonder(ip,id,getApplicationContext(), ipSonuc);
            }
        });

    }
}
