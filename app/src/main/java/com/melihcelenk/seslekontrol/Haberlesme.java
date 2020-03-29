package com.melihcelenk.seslekontrol;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.melihcelenk.seslekontrol.activityler.MainActivity;
import com.melihcelenk.seslekontrol.modeller.SinyalGonderData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Haberlesme {
    public static void SinyalGonder(String ipAdresi, final int id, final Context context, ProgressBar progressBar){
        SinyalGonder(ipAdresi,id,context);
    }
    public static void SinyalGonder(final String ipAdresi, final int id, final Context context){

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
                        Log.v("sinyalResponse","Sinyalden cevap geldi. (IP:"+ipAdresi+")");
                        if(response.isSuccessful()){
                            Log.v("sinyalResponse","Cevap başarılı");
                            SinyalGonderData sinyalGonderData = response.body();
                            if(sinyalGonderData.getId() == String.valueOf(id) && sinyalGonderData.getDurum() == String.valueOf(23)) {
                                Toast.makeText(context, "Sinyal Gonderildi.", Toast.LENGTH_SHORT).show();
                                Log.v("IDSinyal:",id + " numaralı ID'ye sinyal gönderildi.");
                            }
                            else{ // IP, bu projeye ait farklı bir cihaza aitse bu durum çalışır
                                Log.v("sinyalResponse","Cevap ulaşılmak istenen cihazdan değil. IP'ler güncellenecek.");
                                IPleriGuncelle();
                            }
                        }
                        else {
                            Log.v("sinyalResponse","Başarısız cevap. IP'ler güncellenecek.");
                            IPleriGuncelle();
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
                    Toast.makeText(context, "Cihazdan cevap gelmedi. IP'ler güncellenecek.", Toast.LENGTH_LONG).show();
                    IPleriGuncelle();

                    /*TODO: IP'ler güncellenene kadar butonları kilitle */
                }

                private void IPleriGuncelle() {
                    /*TODO: Bu kısım denenmedi. Farklı IP'deki bir cihaz durumu*/
                    try{
                        new IPArkaplanKontrol(context).execute((Void) null);
                    }catch(Exception e){
                        Toast.makeText(context, "IP'ler güncellenirken bir sorun meydana geldi.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    Toast.makeText(context, "Sinyal Gönderilemedi", Toast.LENGTH_LONG).show();
                    Log.e("sinyalResponse:","Cihazdan hata mesajı geldi.");
                }
            });

        }catch(Exception e){
            Log.v("RetrofitHata","Sinyal Gonderilemedi");
            try{
                Log.v("RetrofitHataIP","IP'ler güncellenecek");
                e.printStackTrace();
                new IPArkaplanKontrol(context).execute((Void) null);
            }catch(Exception e1){
               e1.printStackTrace();
            }

            e.printStackTrace();
        }
    }//-----------------------------------SinyalGonder sonu---------------------------------------------------


}
