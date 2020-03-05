package com.melihcelenk.seslekontrol;

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

        cihazKnfgKaydetBtn = findViewById(R.id.cihazKnfgKaydetBtn);
        bolgeEtiketiET = findViewById(R.id.bolgeEtiketiET);
        cihazIDTV = findViewById(R.id.cihazIDTV);
        cihazIPTV = findViewById(R.id.cihazIPTV);
        cihazMACTV = findViewById(R.id.cihazMACTV);

        Intent intent = getIntent();
        final String ipAdresi = intent.getStringExtra("ipAdresi");
        final String macAdresi = intent.getStringExtra("macAdresi");

        cihazIPTV.setText(ipAdresi);
        cihazMACTV.setText(macAdresi);


        db = new DatabaseHandler(this);
        try{
            cihazIDTV.setText(db.idGetir(macAdresi));
            bolgeEtiketiET.setText(db.etiketGetir(macAdresi));
            bolgeEtiketiET.setSelection(bolgeEtiketiET.getText().length());
        }catch(Exception e){
            Log.e("DBHata","Database'den " + macAdresi + " cihazına ait bilgi getirilemedi.");
        }

        cihazKnfgKaydetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bolgeEtiketi = bolgeEtiketiET.getText().toString();
                int sonId = db.ekleVeyaDegistirBolge(new Bolge(bolgeEtiketi,macAdresi,ipAdresi));
                bolgeleriGetir();
                Log.v("Veritabani",sonId + " | " + bolgeEtiketi + " | " + macAdresi + " | " + ipAdresi);
                nodeIdGonder(ipAdresi,String.valueOf(sonId));
            }
        });


    }

    public void bolgeleriGetir(){
        ArrayList<Bolge> butunBolgeler = (ArrayList<Bolge>) db.getButunBolgeler();
        for (Bolge cn : butunBolgeler) {
            String log = "Id: " + cn.get_id() + "\tEtiket: " + cn.get_etiket() + "\tMAC: " + cn.get_macAdresi() + "\tIP: " +
                    cn.get_ipAdresi();
            Log.d("BolgeBilgi: ", log);
        }
    }

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
                        if(response.isSuccessful()){
                            KonfigurasyonData konfigurasyonData = response.body();
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
            //Log.v("RetrofitHata",e.getLocalizedMessage());
            e.printStackTrace();
        }

    }
}
