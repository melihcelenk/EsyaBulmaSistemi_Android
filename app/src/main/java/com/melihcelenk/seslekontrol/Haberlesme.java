package com.melihcelenk.seslekontrol;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

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
    public static void SinyalGonder(String ipAdresi, final int id, final Context context, final MutableLiveData<String> ipSonuc, ProgressBar progressBar){
        SinyalGonder(ipAdresi,id,context, ipSonuc);
    }
    public static void SinyalGonder(final String ipAdresi, final int id, final Context context, final MutableLiveData<String> ipSonuc){

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
                            SinyalGonderData sinyalGonderData = response.body();
                            Log.v("sinyalResponse","Cevap başarılı. Gönderilen ID:"+id + " Gelen ID:" + sinyalGonderData.getId());
                            Log.v("sinyalResponse","Gönderilen Durum:"+23 + " Gelen Durum:" + sinyalGonderData.getDurum());
                            if(sinyalGonderData.getId().equals(String.valueOf(id))) {
                                Log.v("sinyalResponse","ID'ler uyuşuyor.");
                                if(sinyalGonderData.getDurum().equals(String.valueOf(200))){
                                    Toast.makeText(context, "Sinyal Gonderildi.", Toast.LENGTH_SHORT).show();
                                    Log.v("IDSinyal:",id + " numaralı ID'ye sinyal gönderildi.");
                                }
                                else { Log.v("sinyalResponse","Durumlar uyuşmuyor." + String.valueOf(200)); }
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

                }

                private void IPleriGuncelle() {
                    Toast.makeText(context, "Sinyal Gönderilemedi", Toast.LENGTH_LONG).show();
                    Log.e("sinyalResponse:","Cihazdan hata mesajı geldi.");

                    try{
                        new IPArkaplanKontrol(context,ipSonuc).execute((Void) null);
                    }catch(Exception e){
                        Toast.makeText(context, "IP'ler güncellenirken bir sorun meydana geldi.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });

        }catch(Exception e){
            Log.v("RetrofitHata","Sinyal Gonderilemedi");
            try{
                Log.v("RetrofitHataIP","IP'ler güncellenecek");
                e.printStackTrace();
                new IPArkaplanKontrol(context,ipSonuc).execute((Void) null);
            }catch(Exception e1){
               e1.printStackTrace();
            }

            e.printStackTrace();
        }
    }//-----------------------------------SinyalGonder sonu---------------------------------------------------


}
