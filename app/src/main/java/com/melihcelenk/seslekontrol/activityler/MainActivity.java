package com.melihcelenk.seslekontrol.activityler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melihcelenk.seslekontrol.DatabaseHandler;
import com.melihcelenk.seslekontrol.Haberlesme;
import com.melihcelenk.seslekontrol.IPArkaplanKontrol;
import com.melihcelenk.seslekontrol.LedControllerI;
import com.melihcelenk.seslekontrol.R;
import com.melihcelenk.seslekontrol.activityler.esyalarilisteleactivity.EsyalariListeleActivity;
import com.melihcelenk.seslekontrol.activityler.kurulumactivity.KurulumActivity;
import com.melihcelenk.seslekontrol.modeller.NodeData;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    TextView txvResult;
    DatabaseHandler db;
    ProgressBar progressBar;
    MutableLiveData<String> ipSonuc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        // Ağ ve ses ile ilgili sonuç metni
        txvResult = findViewById(R.id.textView);
        db = new DatabaseHandler(this);
        // Asenkron çalışmanın sona erişini kontrol için
        ipSonuc = new MutableLiveData<>();
        try {

            ipSonuc.setValue("Cihazlar kontrol ediliyor...");
            txvResult.setText("Sonuç:"+ ipSonuc.getValue());
            // Asenkron çalışma sırasında değişkeni takip et
            ipSonuc.observe(MainActivity.this, new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    txvResult.setText(ipSonuc.getValue());
                    // Ulaşılamayan cihaz olduğunda rengi kırmızıya çevirir
                    if(ipSonuc.getValue().contains("Şu cihazlara ulaşılamadı:")) txvResult.setTextColor(Color.RED);
                    // Henüz kurulum yapılmadıysa rengi maviye çevirir
                    else if(ipSonuc.getValue().contains("Henüz kurulum yapılmamış")) {
                        txvResult.setTextColor(Color.BLUE);
                        findViewById(R.id.esyalariListeleBtn).setEnabled(false);
                    }
                    // Bağlantı problemi yoksa rengi yeşile çevirir
                    else if(ipSonuc.getValue().contains("Bütün cihazlar ulaşılabilir durumda")) txvResult.setTextColor(Color.GREEN);
                }
            });
            // Arkaplanda bağlantı kontrolü ve IP güncelleme yapılır
            AsyncTask<Void, Integer, String> ipArkaplanKontrol = new IPArkaplanKontrol(MainActivity.this,progressBar,db,ipSonuc).execute((Void) null);

        }catch(Exception e){}

    } // onCreate sonu

    // Mikrofon butonuna basıldığında çalışacak metot
    public void getSpeechInput(View view){

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, 10);
        }
        else{
            Toast.makeText(this,"Konusma Tanima Desteklenmiyor", Toast.LENGTH_SHORT).show();
        }

    }// getSpeechInput sonu

    // Sol alttaki çarklı butona basıldığında KurulumActivity'e geçilir
    public void kurulum(View view){
        Intent intent = new Intent(getApplicationContext(), KurulumActivity.class);
        startActivity(intent);
    }
    // Eşyaları Listele butonuna basıldığında EsyalariListeleActivity'e geçilir
    public void esyalariListeleActivityGit(View view){
        Intent intent = new Intent(getApplicationContext(), EsyalariListeleActivity.class);
        intent.putExtra("listelemeModu","hepsi"); // bu seçenek için bütün eşyalar listelenir
        startActivity(intent);
    }
    //
    public void yeniEsyaEkle(View view){
        Intent intent = new Intent(getApplicationContext(), EsyaEkleActivity.class);
        startActivity(intent);
    }

    // Mikrofon ile konuşma sonlandığında çalışır
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            // getSpeechInput içinde requestCode 10 verilmişti
            case 10:
                if(resultCode==RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    // Mikrofon girdisini ekrana yazdır
                    txvResult.setText(result.get(0));

                    if(result.get(0).contains("nerede"))
                    {
                        String str = result.get(0);
                        String delims = "nerede+";
                        // İfade "nerede" ile ayrılıyor, token[0]'da aranan eşya olacak
                        String[] tokens = str.split(delims);
                        try{
                            Log.v("SinyalGonder","Gonderilecek");
                            Log.v("Token:","tokens[0]:" + tokens[0].trim() + "$");
                            // Eşya adı eşleşen bütün eşyaların ID'leri getiriliyor (birden fazla aynı eşya olabilir, bardak gibi)
                            ArrayList<Integer> uyusanIDler =  db.esyaIdGetirEsyaAdiIle(tokens[0].trim());
                            // İstekle uyuşan eşya var ise
                            if(uyusanIDler.size()>0){
                                // Uyuşan tek bir eşya var ise doğrudan sinyal gönderiliyor
                                if(uyusanIDler.size()==1){
                                    int sinyalGonderilecekID = db.EsyaGetirIdIle(uyusanIDler.get(0)).get_bolgeId();
                                    Log.v("SinyalGonderilecekID",""+sinyalGonderilecekID);
                                    String sinyalGonderilecekIP = db.getBolge(sinyalGonderilecekID).get_ipAdresi();
                                    Log.v("SinyalGonderilecekIP",sinyalGonderilecekIP);
                                    Haberlesme.SinyalGonder(sinyalGonderilecekIP,sinyalGonderilecekID,getApplicationContext(),ipSonuc);
                                }
                                // Uyuşan çok sayıda eşya varsa yeni bir aktivitede uyuşan eşyalar gösteriliyor
                                else{
                                    Log.v("Nerede","1'den fazla eşya var");
                                    Intent intent = new Intent(getApplicationContext(), EsyalariListeleActivity.class);
                                    intent.putExtra("esyaAdi",tokens[0].trim());
                                    intent.putExtra("listelemeModu","ozelArama"); // sadece uyuşan eşyaların listelenebilmesi için
                                    startActivity(intent);
                                }

                            }
                            // Uyuşan eşya yoksa
                            else{
                                Toast.makeText(this, tokens[0] + " sistemde kayıtlı değil.", Toast.LENGTH_SHORT).show();
                            }

                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    // Eşya ekleme | komut düzeni: [BÖLGEADI ekle EŞYAADI] (Örnek: Depo ekle Takım Çantası)
                    else if(result.get(0).contains("ekle")){
                        String str = result.get(0);
                        String delims = "ekle+";
                        // token[0] : BÖLGEADI
                        // token[1] : EŞYAADI olarak ayrılıyor
                        String[] tokens = str.split(delims);

                        // Zikredilen bölgenin mevcut olup olmadığının kontrolü
                        if(db.getBolgeEtiketIle(tokens[0].trim())==null){
                            Toast.makeText(this, tokens[0] + " adında bir bölge yok.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Intent intent = new Intent(getApplicationContext(), EsyaEkleActivity.class);
                            //Bu bilgiler Eşya Ekle Activity'e aktarılacak ve orada detaylı ekleme yapılacak
                            intent.putExtra("bolge",tokens[0].trim());
                            intent.putExtra("esyaAdi",tokens[1].trim());
                            startActivity(intent);
                        }
                    }
                    else if(result.get(0).contains("listele")){
                        Intent intent = new Intent(getApplicationContext(), EsyalariListeleActivity.class);
                        intent.putExtra("listelemeModu","hepsi"); // bu seçenek için bütün eşyalar listelenir
                        startActivity(intent);
                    }
                }
                break;

        }
    }// onActivityResult sonu

}//MainActivity sonu
