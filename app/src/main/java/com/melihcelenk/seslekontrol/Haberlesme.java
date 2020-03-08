package com.melihcelenk.seslekontrol;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.melihcelenk.seslekontrol.modeller.SinyalGonderData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Haberlesme {
    public static void SinyalGonder(String ipAdresi, final int id, final Context context){

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
                                Toast.makeText(context, "Sinyal Gonderildi.", Toast.LENGTH_SHORT).show();
                                Log.v("IDSinyal:",id + " numaralı ID'ye sinyal gönderildi.");
                            }
                        }
                        else {
                            Toast.makeText(context, "Sinyal Gönderilemedi", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(context, "Cihazdan cevap gelmedi. IP'ler güncellenecek.", Toast.LENGTH_LONG).show();
                    /*TODO: IP'ler güncellenene kadar butonları kilitle */
                }
            });

        }catch(Exception e){
            Log.v("RetrofitHata","Sinyal Gonderilemedi");
            e.printStackTrace();
        }
    }//-----------------------------------SinyalGonder sonu---------------------------------------------------
}
