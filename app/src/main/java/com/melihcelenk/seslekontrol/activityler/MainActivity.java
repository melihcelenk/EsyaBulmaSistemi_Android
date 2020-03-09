package com.melihcelenk.seslekontrol.activityler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.melihcelenk.seslekontrol.DatabaseHandler;
import com.melihcelenk.seslekontrol.Haberlesme;
import com.melihcelenk.seslekontrol.IPArkaplanKontrol;
import com.melihcelenk.seslekontrol.LedControllerI;
import com.melihcelenk.seslekontrol.R;
import com.melihcelenk.seslekontrol.activityler.esyalarilisteleactivity.EsyalariListeleActivity;
import com.melihcelenk.seslekontrol.activityler.kurulumactivity.KurulumActivity;
import com.melihcelenk.seslekontrol.modeller.Esya;
import com.melihcelenk.seslekontrol.modeller.NodeData;
import com.melihcelenk.seslekontrol.modeller.SinyalGonderData;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        txvResult = findViewById(R.id.textView);
        db = new DatabaseHandler(this);
        /* TODO: Açılışta başka bir thread'de bütün cihazlara istek gönderip birinden cevap gelmeyince IPBulveGuncelle çalıştırılsın.*/
        new IPArkaplanKontrol(MainActivity.this,progressBar,db).execute((Void) null);

    } // onCreate sonu

    public void getSpeechInput(View view){
        /*TODO: hızlı sonlanma sorununu çöz, bir token'a göre sonlanmayı araştır*/
        /* TODO: Hata kontrolleri ve ara yüzler, daha sonra eşyayı anahtar kelimelerle ekleme yapılacak:
        *  Eşyayı ara, eğer birden fazla bulunursa gelen değerler içinde arama yap, tekrar DB'ye bağlanma  */

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
    public void kurulum(View view){
        Intent intent = new Intent(getApplicationContext(), KurulumActivity.class);
        startActivity(intent);
    }
    public void esyalariListeleActivityGit(View view){
        Intent intent = new Intent(getApplicationContext(), EsyalariListeleActivity.class);
        startActivity(intent);
    }

    public void esyalariLogla(){
        ArrayList<Esya> butunEsyalar = (ArrayList<Esya>) db.getButunEsyalar();
        for (Esya cn : butunEsyalar) {
            String log = "esyaId: " + cn.get_esyaId() + "\tbolgeId: " + cn.get_bolgeId() + "\tesyaAdi: " + cn.get_esyaAdi();
            Log.d("BolgeBilgi: ", log);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 10:
                if(resultCode==RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txvResult.setText(result.get(0));

                    if(result.get(0).contains("nerede"))
                    {
                        String str = result.get(0);
                        String delims = "nerede+";
                        String[] tokens = str.split(delims);
                        try{
                            Log.v("SinyalGonder","Gonderilecek");
                            Log.v("Token:","tokens[0]:" + tokens[0].trim() + "$");
                            int sinyalGonderilecekID = db.bolgeIdGetirEsyaAdiIle(tokens[0].trim());
                            Log.v("SinyalGonderilecekID",""+sinyalGonderilecekID);
                            String sinyalGonderilecekIP = db.getBolge(sinyalGonderilecekID).get_ipAdresi();
                            Log.v("SinyalGonderilecekIP",sinyalGonderilecekIP);
                            Haberlesme.SinyalGonder(sinyalGonderilecekIP,sinyalGonderilecekID,getApplicationContext());
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    else if(result.get(0).contains("ekle")){
                        String str = result.get(0);
                        String delims = "ekle+";
                        String[] tokens = str.split(delims);
                        try{
                            int eklenecekID = db.idGetirEtiketIle(tokens[0].trim());
                            db.addEsya(new Esya(eklenecekID,tokens[1].trim()));
                            esyalariLogla();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    else if(result.get(0).contains("listele")){
                        // TODO: eşyaları listele recyclerview için adaptör tasarla
                    }
                }
                break;

        }
    }// onActivityResult sonu


    public void me(String ipAdresi){
        String url="http://"+ipAdresi;

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))

                .build();
        LedControllerI ledContrrolerIService= retrofit.create(LedControllerI.class);
        ledContrrolerIService.getNodeData().enqueue(new Callback<NodeData>() {

            @Override
            public void onResponse(Call<NodeData> call, Response<NodeData> response) {
                Log.v("me response",response.body().toString());
            }

            @Override
            public void onFailure(Call<NodeData> call, Throwable t) {
               Log.e("me error:",t.getMessage());
            }
        });
    }

    /*TODO: Silinecek */
    public void ASinyalGonder(String ipAdresi, final int id){

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
            Call<SinyalGonderData> call = ledControllerIService.getSinyalGonderData(String.valueOf(id),String.valueOf(23));
            call.enqueue(new Callback<SinyalGonderData>() {
                @Override
                public void onResponse(Call<SinyalGonderData> call, Response<SinyalGonderData> response) {
                    try{
                        Log.v("sinyalResponse","Sinyalden cevap geldi");
                        if(response.isSuccessful()){
                            Log.v("sinyalResponse","Cevap başarılı");
                            SinyalGonderData sinyalGonderData = response.body();
                            if(sinyalGonderData.getId() == String.valueOf(id) && sinyalGonderData.getDurum() == String.valueOf(23)) {
                                Toast.makeText(getApplicationContext(), "Sinyal Gonderildi.", Toast.LENGTH_SHORT).show();
                                Log.v("IDSinyal:",id + " numaralı ID'ye sinyal gönderildi.");
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Sinyal Gönderilemedi", Toast.LENGTH_LONG).show();
                            Log.e("sinyalResponse:","Cihazdan hata mesajı geldi.");
                        }
                    }catch(JsonIOException e){
                        Log.v("sinyalResponse","Sinyalden gelen cevapta hata oluştu");
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<SinyalGonderData> call, Throwable t) {
                    Log.v("SinyalGonder","Cihazdan cevap gelmedi");
                    Log.v("SinyalBasarisiz","IP'ler güncellenecek...");
                    /*TODO: IPBulveGuncelle'yi Thread içine al*/
                    try{
                        //IPBulveGuncelle();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "Cihazdan cevap gelmedi. IP'ler güncellenecek.", Toast.LENGTH_LONG).show();
                    /*TODO: IP'ler güncellenene kadar butonları kilitle */
                }
            });

        }catch(Exception e){
            Log.v("RetrofitHata","Sinyal Gonderilemedi");
            e.printStackTrace();
        }
    }//-----------------------------------SinyalGonder sonu---------------------------------------------------




}