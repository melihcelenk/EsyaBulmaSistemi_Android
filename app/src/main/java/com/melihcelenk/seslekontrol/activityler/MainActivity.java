package com.melihcelenk.seslekontrol.activityler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
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
        txvResult = findViewById(R.id.textView);
        db = new DatabaseHandler(this);
        ipSonuc = new MutableLiveData<>();
        try {

            ipSonuc.setValue("Bekleniyor...");
            txvResult.setText("Sonuç:"+ ipSonuc.getValue());

            ipSonuc.observe(MainActivity.this, new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    txvResult.setText("Arkaplan sonlandı:"+ipSonuc.getValue());
                }
            });

            AsyncTask<Void, Integer, String> ipArkaplanKontrol = new IPArkaplanKontrol(MainActivity.this,progressBar,db,ipSonuc).execute((Void) null);


        }catch(Exception e){}


        //KONTROL AMAÇLI KONULDU TODO: SİLİNECEK------------------
        final int[] i = {23};
        txvResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i[0]++;
                txvResult.setText("mesela"+i[0]);
            }
        });
        // final int[] i = {23}; SATIRINDAN BURAYA KADAR SİLİNECEK


    } // onCreate sonu


    public void getSpeechInput(View view){
        /*TODO: hızlı sonlanma sorununu çöz, bir token'a göre sonlanmayı araştır*/
        /* TODO: Hata kontrolleri ve ara yüzler, daha sonra eşyayı anahtar kelimelerle ekleme yapılacak:
        *  TODO: Eşyayı ara, eğer birden fazla bulunursa gelen değerler içinde arama yap, tekrar DB'ye bağlanma  */

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
        intent.putExtra("listelemeModu","hepsi");
        startActivity(intent);
    }
    public void yeniEsyaEkle(View view){
        Intent intent = new Intent(getApplicationContext(), EsyaEkleActivity.class);
        intent.putExtra("listelemeModu","hepsi");
        startActivity(intent);
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
                            ArrayList<Integer> uyusanIDler =  db.esyaIdGetirEsyaAdiIle(tokens[0].trim());
                            if(uyusanIDler.size()>0){
                                if(uyusanIDler.size()==1){
                                    int sinyalGonderilecekID = db.EsyaGetirIdIle(uyusanIDler.get(0)).get_bolgeId();
                                    Log.v("SinyalGonderilecekID",""+sinyalGonderilecekID);
                                    String sinyalGonderilecekIP = db.getBolge(sinyalGonderilecekID).get_ipAdresi();
                                    Log.v("SinyalGonderilecekIP",sinyalGonderilecekIP);
                                    Haberlesme.SinyalGonder(sinyalGonderilecekIP,sinyalGonderilecekID,getApplicationContext(),ipSonuc);
                                }
                                else{

                                    Intent intent = new Intent(getApplicationContext(), EsyalariListeleActivity.class);
                                    intent.putExtra("esyaAdi",tokens[0].trim());
                                    startActivity(intent);
                                }

                            }
                            else{
                                Toast.makeText(this, tokens[0] + " sistemde kayıtlı değil.", Toast.LENGTH_SHORT).show();
                            }

                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    else if(result.get(0).contains("ekle")){
                        String str = result.get(0);
                        String delims = "ekle+";
                        String[] tokens = str.split(delims);

                        // Zikredilen bölgenin mevcut olup olmadığının kontrolü
                        if(db.getBolgeEtiketIle(tokens[0].trim())==null){
                            Toast.makeText(this, tokens[0] + " adında bir bölge yok.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Intent intent = new Intent(getApplicationContext(), EsyaEkleActivity.class);
                            intent.putExtra("bolge",tokens[0].trim());
                            intent.putExtra("esyaAdi",tokens[1].trim());
                            startActivity(intent);
                        }
                    }
                    else if(result.get(0).contains("listele")){ // TODO: Düzenlenecek
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






}
