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

    private RecyclerView esyalarRV;
    private esyalarAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    ArrayList<Esya> bulunanEsyalarArray;
    private TextView sonucText;
    MutableLiveData<String> ipSonuc;
    TextView txvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esyalari_listele);
        ipSonuc = new MutableLiveData<>();
        txvResult = findViewById(R.id.sonucText);
        try {

            ipSonuc.setValue("Bekleniyor...");
            txvResult.setText("Sonuç:"+ ipSonuc.getValue());

            ipSonuc.observe(EsyalariListeleActivity.this, new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    txvResult.setText("Arkaplan sonlandı:"+ipSonuc.getValue());
                }
            });

            AsyncTask<Void, Integer, String> ipArkaplanKontrol = new IPArkaplanKontrol(EsyalariListeleActivity.this,ipSonuc).execute((Void) null);


        }catch(Exception e){}



        sonucText = findViewById(R.id.sonucText);
        final DatabaseHandler db = new DatabaseHandler(this);


        //

        Intent intent = getIntent();

        final String listelemeModu = intent.getStringExtra("listelemeModu");

        if(listelemeModu.equals("hepsi")){
            bulunanEsyalarArray = (ArrayList<Esya>) db.getButunEsyalar();
        }
        else{
            final String esyaAdi = intent.getStringExtra("esyaAdi");
            ArrayList<Integer> uyusanIDler =  db.esyaIdGetirEsyaAdiIle(esyaAdi);
            bulunanEsyalarArray = new ArrayList<>();
            for(int i : uyusanIDler){
                bulunanEsyalarArray.add(db.EsyaGetirIdIle(i));
            }
        }

        //

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
                final MutableLiveData<String> ipSonuc = new MutableLiveData<>();
                ipSonuc.setValue("Bekleniyor...");
                ipSonuc.observe(EsyalariListeleActivity.this, new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        sonucText.setText("Arkaplan sonlandı:"+ipSonuc.getValue());
                    }
                });
                Haberlesme.SinyalGonder(ip,id,getApplicationContext(), ipSonuc);
            }
        });

    }
}
