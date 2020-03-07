package com.melihcelenk.seslekontrol;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    TextView txvResult;
    Retrofit retrofit;
    DatabaseHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txvResult = findViewById(R.id.textView);
        db = new DatabaseHandler(this);

    } // onCreate sonu

    public void getSpeechInput(View view){
        /*TODO: hızlı sonlanma sorununu çöz, bir token'a göre sonlanmayı araştır*/
        /* TODO: Hata kontrolleri ve ara yüzler, daha sonra eşyayı anahtar kelimelerle ekleme yapılacak. */
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
        Intent intent = new Intent(getApplicationContext(),KurulumActivity.class);
        startActivity(intent);
    }
    public void esyaEkle(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, 20);
        }
        else{
            Toast.makeText(this,"Konusma Tanima Desteklenmiyor", Toast.LENGTH_SHORT).show();
        }
    }

    public void esyalariGetir(){
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

                    String str = result.get(0);
                    String delims = "nerede+";
                    String[] tokens = str.split(delims);

                    if(result.get(0).equals("kimsin")) me();
                    else{
                        try{
                            Log.v("SinyalGonder","Gonderilecek");
                            Log.v("Token:","tokens[0]:" + tokens[0].trim() + "$");
                            int sinyalGonderilecekID = db.bolgeIdGetirEsyaAdiIle(tokens[0].trim());
                            Log.v("SinyalGonderilecekID",""+sinyalGonderilecekID);

                            String sinyalGonderilecekIP = db.getBolge(sinyalGonderilecekID).get_ipAdresi();
                            Log.v("SinyalGonderilecekIP",sinyalGonderilecekIP);

                            SinyalGonder(sinyalGonderilecekIP,sinyalGonderilecekID);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case 20:
                if(resultCode==RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txvResult.setText(result.get(0));

                    String str = result.get(0);
                    String delims = "ekle+";
                    String[] tokens = str.split(delims);

                    int eklenecekID = db.idGetirEtiketIle(tokens[0].trim());
                    db.addEsya(new Esya(eklenecekID,tokens[1].trim()));
                    esyalariGetir();

                }
                break;
        }
    }// onActivityResult sonu

    public void ledYak(){
        String url="http://192.168.0.103";
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .build();
        LedControllerI ledContrrolerIService= retrofit.create(LedControllerI.class);
        ledContrrolerIService.openLed().enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println("Basarili " + response.toString());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("Basarisiz " + t.toString());
            }
        });
    }
    public void ledKapa(){
        String url="http://192.168.0.103";
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .build();
        LedControllerI ledContrrolerIService= retrofit.create(LedControllerI.class);
        ledContrrolerIService.closeLed().enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println("Basarili" + response.toString());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("Basarisiz " + t.toString());
            }
        });
    }

    public void me(){
        String url="http://192.168.0.103";

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

    public void SinyalGonder(String ipAdresi, final int id){

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
                    Log.v("Sinyal","Sinyal Gonderilemedi");
                    Toast.makeText(getApplicationContext(), "Cihazdan cevap gelmedi.", Toast.LENGTH_LONG).show();
                    /*TODO Neden Response Almadığnıa Bak */
                }
            });

        }catch(Exception e){
            Log.v("RetrofitHata","Sinyal Gonderilemedi");
            e.printStackTrace();
        }
    }
}
