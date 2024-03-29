package com.melihcelenk.seslekontrol.activityler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.melihcelenk.seslekontrol.DatabaseHandler;
import com.melihcelenk.seslekontrol.Haberlesme;
import com.melihcelenk.seslekontrol.IPArkaplanKontrol;
import com.melihcelenk.seslekontrol.R;
import com.melihcelenk.seslekontrol.activityler.esyalarilisteleactivity.EsyalariListeleActivity;
import com.melihcelenk.seslekontrol.modeller.Esya;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class EsyaEkleActivity extends AppCompatActivity {
    DatabaseHandler db;
    EditText esyaAdiTV;
    EditText bolgeTV;
    Button esyaKaydetBtn;
    Button anahtarKelimeleriSoyleBtn;
    EditText anahtarKelimelerET;
    Button tekrarDeneBtn;
    TextView aciklamaTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esya_ekle);
        setTitle("Eşya Ekle");

        // bileşenleri tanımla
        db = new DatabaseHandler(this);
        esyaAdiTV = findViewById(R.id.esyaAdi_esyaEkleAct);
        bolgeTV = findViewById(R.id.bolgeTV_esyaEkleAct);
        esyaKaydetBtn = findViewById(R.id.esyaKaydetBtn_esyaEkleAct);
        tekrarDeneBtn = findViewById(R.id.tekrarDeneBtn);
        anahtarKelimeleriSoyleBtn = findViewById(R.id.anahtarKelimeleriSoyleBtn_esyaEkleAct);
        anahtarKelimelerET = findViewById(R.id.anahtarKelimelerET_esyaEkleAct);
        aciklamaTV = findViewById(R.id.aciklamaTV_esyaEkleAct);

        // Mikrofondan alınan verileri getir
        Intent intent = getIntent();
        final String bolge = intent.getStringExtra("bolge");
        final String esyaAdi = intent.getStringExtra("esyaAdi");

        esyaAdiTV.setText(esyaAdi);
        bolgeTV.setText(bolge);

        //Eğer mikrofon kullanılmadıysa
        if(bolge==null) {
            //Ses ile ilgili butonlar kaldırılıyor
            tekrarDeneBtn.setVisibility(View.GONE);
            anahtarKelimeleriSoyleBtn.setVisibility(View.GONE);
            aciklamaTV.setText("Yeni eşya eklemek için bilgileri girin.");
        }

        // Kaydet butonuna basıldığında
        esyaKaydetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String str = anahtarKelimelerET.getText().toString();
                    Log.v("AnahtarKelimeler","EditTextİçi: "+str);
                    // Anahtar kelimeleri boşluk ile ayır ve ArrayList'e ata
                    final ArrayList<String> anahtarKelimeler= new ArrayList<String>(Arrays.asList(str.split(" ")));

                    String bolgeAdiKaydedilen = bolgeTV.getText().toString();
                    // Zikredilen bölge adı sistemde mevcut değilse kullanıcıyı uyar
                    if(db.getBolgeEtiketIle(bolgeAdiKaydedilen)==null){
                        Toast.makeText(EsyaEkleActivity.this, bolgeAdiKaydedilen + " adında bir bölge yok.", Toast.LENGTH_SHORT).show();
                    }
                    // Bölge adı mevcut ise
                    else{
                        int eklenecekID = db.idGetirEtiketIle(bolgeAdiKaydedilen);
                        String esyaAdiKaydedilen = esyaAdiTV.getText().toString();
                        // Eşyayı ekle ve ID'sini döndür
                        long esyaId = db.addEsya(new Esya(eklenecekID,esyaAdiKaydedilen));
                        // Dönen ID'ye anahtar kelimeleri ekle
                        db.addEsyaAnahtarKelimeler(esyaId, anahtarKelimeler);

                        // EŞYAADI kaydedildi geri bildirimi göster
                        Toast.makeText(EsyaEkleActivity.this, esyaAdiKaydedilen + " kaydedildi.", Toast.LENGTH_SHORT).show();
                        db.LogButunEsyaAnahtarKelimeler();
                        esyalariLogla();

                        // Bütün Eşyaları Listele
                        Intent intent = new Intent(getApplicationContext(), EsyalariListeleActivity.class);
                        intent.putExtra("listelemeModu","hepsi");
                        startActivity(intent);
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

    }
    public void esyalariLogla(){
        ArrayList<Esya> butunEsyalar = (ArrayList<Esya>) db.getButunEsyalar();
        for (Esya cn : butunEsyalar) {
            String log = "esyaId: " + cn.get_esyaId() + "\tbolgeId: " + cn.get_bolgeId() + "\tesyaAdi: " + cn.get_esyaAdi();
            Log.d("BolgeBilgi: ", log);
        }
    }
    // Tekrar deneme butonuna tıklandığında mikrofon kelime almaya başlar
    public void tekrarDene(View view){

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, 20);
        }
        else{
            Toast.makeText(this,"Konusma Tanima Desteklenmiyor", Toast.LENGTH_SHORT).show();
        }

    }// tekrarDene sonu

    // Anahtar Kelimeleri Söyle butonuna basıldığında mikrofon kelime almaya başlar
    public void anahtarSoyle(View view){

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, 30);
        }
        else{
            Toast.makeText(this,"Konusma Tanima Desteklenmiyor", Toast.LENGTH_SHORT).show();
        }

    }// anahtarSoyle sonu

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            // "Tekrar Dene" üzerinden geliyor ise
            case 20:
                if(resultCode==RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    if(result.get(0).contains("ekle")){
                        String str = result.get(0);
                        String delims = "ekle+";
                        String[] tokens = str.split(delims);

                        // Zikredilen bölgenin mevcut olup olmadığının kontrolü
                        if(db.getBolgeEtiketIle(tokens[0].trim())==null){
                            Toast.makeText(this, tokens[0] + " adında bir bölge yok.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            bolgeTV.setText(tokens[0].trim());
                            esyaAdiTV.setText(tokens[1].trim());
                        }
                    }
                }
                break;

            // "Anahtar Kelimeleri Söyle üzerinden geliyor ise"
            case 30:
                if(resultCode==RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String str = result.get(0);
                    // EditText'e mikrofon ile söylenen anahtar kelimeleri yaz
                    anahtarKelimelerET.setText(str);
                }

        }
    }// onActivityResult sonu
}
