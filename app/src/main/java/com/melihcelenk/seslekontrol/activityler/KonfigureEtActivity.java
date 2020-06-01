package com.melihcelenk.seslekontrol.activityler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.melihcelenk.seslekontrol.DatabaseHandler;
import com.melihcelenk.seslekontrol.LedControllerI;
import com.melihcelenk.seslekontrol.R;
import com.melihcelenk.seslekontrol.modeller.Bolge;
import com.melihcelenk.seslekontrol.modeller.KonfigurasyonData;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KonfigureEtActivity extends AppCompatActivity {
    DatabaseHandler db;
    Button cihazKnfgKaydetBtn;
    EditText bolgeEtiketiET;
    TextView cihazIDTV;
    TextView cihazIPTV;
    TextView cihazMACTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konfigure_et);
        setTitle("Cihaz Konfigürasyonu");

        // Bileşenleri tanımla
        cihazKnfgKaydetBtn = findViewById(R.id.cihazKnfgKaydetBtn);
        bolgeEtiketiET = findViewById(R.id.bolgeEtiketiET);
        cihazIDTV = findViewById(R.id.cihazIDTV);
        cihazIPTV = findViewById(R.id.cihazIPTV);
        cihazMACTV = findViewById(R.id.cihazMACTV);

        // KurulumActivity'den tıklanan cihazın IP ve MAC bilgilerini al
        Intent intent = getIntent();
        final String ipAdresi = intent.getStringExtra("ipAdresi");
        final String macAdresi = intent.getStringExtra("macAdresi");

        cihazIPTV.setText(ipAdresi);
        cihazMACTV.setText(macAdresi);

        db = new DatabaseHandler(this);
        // MAC adresi veri tabanında kayıtlıysa ID'sini getir
        try{
            cihazIDTV.setText(db.idGetir(macAdresi));
        }catch(Exception e){
            Log.e("DBHata","Database'den " + macAdresi + " cihazına ait bilgi getirilemedi.");
        }
        // MAC adresi veri tabanında kayıtlıysa bulunduğu bölgenin etiketini getir (her cihaz bir bölgeyi temsil eder)
        try{
            bolgeEtiketiET.setText(db.etiketGetir(macAdresi));
        }catch(Exception e){
            Log.e("DBHata","Database'den " + macAdresi + " cihazına ait bilgi getirilemedi.");
        }
        // Kayıtlı MAC adresi üzerine kayıtlı IP, yeni IP ile eşleşmiyorsa veri tabanındaki IP'yi güncelle
        try{
            if((db.ipGetir(macAdresi)!=ipAdresi)){
                db.ipDegistir(macAdresi,ipAdresi);
            }
        }catch(Exception e){
            Log.e("DBHata","Database'den " + macAdresi + " cihazına ait bilgi getirilemedi.");
        }
        // İmleci bölge etiketi EditText'inin sonuna getir
        bolgeEtiketiET.setSelection(bolgeEtiketiET.getText().length());
        // Kaydet butonuna basıldığında
        cihazKnfgKaydetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bolgeEtiketi = bolgeEtiketiET.getText().toString();
                // Bölge eklenecek veya varsa güncellenecek
                int sonId = db.ekleVeyaDegistirBolge(new Bolge(bolgeEtiketi,macAdresi,ipAdresi));
                bolgeleriGetir(); // Bütün bölgeleri log'la
                Log.v("Veritabani",sonId + " | " + bolgeEtiketi + " | " + macAdresi + " | " + ipAdresi);
                // Cihaza atanan ID'yi gönder (Cihazın EEPROM'unda kaydedilecek ve bağlantıda kullanılacak)
                nodeIdGonder(ipAdresi,String.valueOf(sonId));
            }
        });
    } // onCreate sonu

    public void bolgeleriGetir(){
        ArrayList<Bolge> butunBolgeler = (ArrayList<Bolge>) db.getButunBolgeler();
        for (Bolge cn : butunBolgeler) {
            String log = "Id: " + cn.get_id() + "\tEtiket: " + cn.get_etiket() + "\tMAC: " + cn.get_macAdresi() + "\tIP: " +
                    cn.get_ipAdresi();
            Log.d("BolgeBilgi: ", log);
        }
    }

    // Cihaza atanan ID'nin gönderilmesi
    public void nodeIdGonder(String ipAdresi, final String nodeId){

        String url="http://" + ipAdresi;
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        try {
            LedControllerI ledControllerIService= retrofit.create(LedControllerI.class);
            Call<KonfigurasyonData> call = ledControllerIService.getKonfigurasyonData(nodeId);
            call.enqueue(new Callback<KonfigurasyonData>() {
                @Override
                public void onResponse(Call<KonfigurasyonData> call, Response<KonfigurasyonData> response) {
                    try{
                        // Cihazdan gelen cevap başarılıysa
                        if(response.isSuccessful()){
                            KonfigurasyonData konfigurasyonData = response.body();
                            // Gönderilen ve alınan uyumlu ise
                            if(konfigurasyonData.getSetNodeId() == nodeId) {
                                cihazIDTV.setText(nodeId);
                                Toast.makeText(getApplicationContext(), "Cihaz Kaydedildi.", Toast.LENGTH_SHORT).show();
                                Log.v("Cihaz ID'si gönderildi:",nodeId);
                            }
                        }
                    }catch(JsonIOException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<KonfigurasyonData> call, Throwable t) {
                    Log.v("onFailure","KonfigurasyonData");
                }
            });


        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
