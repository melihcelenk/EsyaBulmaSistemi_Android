package com.melihcelenk.seslekontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stealthcopter.networktools.IPTools;
import com.stealthcopter.networktools.SubnetDevices;
import com.stealthcopter.networktools.subnet.Device;

import java.net.InetAddress;
import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KurulumActivity extends AppCompatActivity {

    TextView sonucText;
    ArrayList<bulunanCihaz> bulunanCihazlarArray;
    Thread tButon;
    DatabaseHandler db;

    private RecyclerView cihazlarRV;
    private cihazlarAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kurulum);

        db = new DatabaseHandler(this);

        sonucText = findViewById(R.id.sonucText);
        bulunanCihazlarArray = new ArrayList<bulunanCihaz>();

        cihazlarRV = (RecyclerView) findViewById(R.id.cihazlarRV);
        //cihazlarRV.setHasFixedSize(true);
        //cihazlarRV.setNestedScrollingEnabled(false);
        layoutManager = new LinearLayoutManager(this);
        cihazlarRV.setLayoutManager(layoutManager);
        mAdapter = new cihazlarAdapter(bulunanCihazlarArray);
        cihazlarRV.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new cihazlarAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(KurulumActivity.this, "Led Durumu:" + bulunanCihazlarArray.get(position).getLedDurum().toString(), Toast.LENGTH_SHORT).show();
                //mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLedYakClick(int position) {
                bulunanCihazlarArray.get(position).LedDegistir();
            }
        });

        InetAddress ipAddress = IPTools.getLocalIPv4Address();
        if (ipAddress != null){
            // IP ADRESİ BULUNAMADI - DÜZENLENECEK
        }

        // TARA BUTONU
        findViewById(R.id.taraBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tButon == null || !tButon.isAlive()){
                    tButon = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                IPBul();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    findViewById(R.id.taraBtn).setEnabled(false);

                }
                if (tButon.getState() == Thread.State.NEW)
                {
                    tButon.start();
                }
                else
                {
                    Toast.makeText(KurulumActivity.this, "Cihazlar taranamadı.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void sonucTextveButonDegistir(final String text, final Boolean butonDurum) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sonucText.setText(text + "\n");
                //sonucText.append(text + "\n");
                findViewById(R.id.taraBtn).setEnabled(butonDurum);
            }
        });
    }
    private void IPDiziyeEkle(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bulunanCihazlarArray.add(new bulunanCihaz(text));
                mAdapter.notifyDataSetChanged();
            }
        });
    }
    private void bulunanCihazlarArrayTemizle() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bulunanCihazlarArray.clear();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void IPBul() {

        sonucTextveButonDegistir("Taranıyor...",false);
        bulunanCihazlarArrayTemizle();
        final long startTimeMillis = System.currentTimeMillis();
        try{
            SubnetDevices.fromLocalAddress().findDevices(new SubnetDevices.OnSubnetDeviceFound() {
                @Override
                public void onDeviceFound(Device device) {
                    if(arananCihazMi(String.valueOf(device.ip)) == true){
                        try {
                            IPDiziyeEkle(device.ip);
                        } catch (Exception e) {
                            Log.e("Diziekle",e.getMessage());
                            e.printStackTrace();
                        }
                    }

                }

                @Override
                public void onFinished(ArrayList<Device> devicesFound) {
                    float timeTaken =  (System.currentTimeMillis() - startTimeMillis)/1000.0f;
                    sonucTextveButonDegistir("Ağdaki Toplam Cihaz Sayısı: " + devicesFound.size(),true);
                    //sonucTextveButonDegistir("Finished "+timeTaken+" s");
                }
            });
        }catch(Exception e){
            Log.e("IPBulHata",e.getLocalizedMessage());
            e.printStackTrace();
        }

    }

    public Boolean arananCihazMi(String ipAdresi){
        final Boolean[] arananMi = new Boolean[1];
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
            Bolge bolge = ledControllerIService.getBolgeBilgi().execute().body();
            Log.v("Node", bolge.get_ipAdresi());
            return true;
            /*ledControllerIService.getBolgeBilgi().enqueue(new Callback<Bolge>() {

                @Override
                public void onResponse(Call<Bolge> call, Response<Bolge> response) {

                    if(response.body()!=null){
                        Log.v("Me Cevap:",response.body().toString());
                        // CİHAZDAN GELEN BİLGİLERE GÖRE DÜZENLENECEK
                        arananMi[0] = true;
                    }
                    else {
                        arananMi[0] = false;
                    }

                }

                @Override
                public void onFailure(Call<Bolge> call, Throwable t) {
                    Log.e("Me Hata:",t.getMessage());
                    arananMi[0] = false;
                }
            });*/
        }catch(Exception e){
            Log.v("RetrofitHata",e.getLocalizedMessage());
            e.printStackTrace();
            return false;

        }


      /*  while(arananMi[0] == null){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e("Thread ",e.getMessage());
                e.printStackTrace();
            }
        }*/

        /* TODO: */
        // DÜZENLENECEK: THREAD SONLANDIĞINDA BURAYI ÇALIŞTIRACAK KOD YAZILACAK
     /*   if(arananMi[0] != null){
            Log.v("ArananMi:" , arananMi[0].toString() + " IP:" + ipAdresi);
            return arananMi[0];
        }
        else {
            Log.v("ArananMi:" , "Null" + " IP:" + ipAdresi);
            return false;
        }*/



    }


}
