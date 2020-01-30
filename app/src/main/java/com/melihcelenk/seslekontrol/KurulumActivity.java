package com.melihcelenk.seslekontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stealthcopter.networktools.IPTools;
import com.stealthcopter.networktools.SubnetDevices;
import com.stealthcopter.networktools.subnet.Device;

import java.net.InetAddress;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KurulumActivity extends AppCompatActivity {

    TextView sonucText;
    //ArrayList<String> bulunanCihazlarArray;
    //SINIF YAZILACAK STRING YERİNE
    ArrayList<bulunanCihaz> bulunanCihazlarArray;

    private RecyclerView cihazlarRV;
    private cihazlarAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kurulum);

        sonucText = findViewById(R.id.sonucText);
//        bulunanCihazlarArray = new ArrayList<String>();
        bulunanCihazlarArray = new ArrayList<bulunanCihaz>();

        cihazlarRV = (RecyclerView) findViewById(R.id.cihazlarRV);
        cihazlarRV.setHasFixedSize(true); //
        layoutManager = new LinearLayoutManager(this);
        cihazlarRV.setLayoutManager(layoutManager);
        mAdapter = new cihazlarAdapter(bulunanCihazlarArray);
        cihazlarRV.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new cihazlarAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                bulunanCihazlarArray.get(position);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLedYakClick(int position) {
                bulunanCihazlarArray.get(position).LedDegistir();
            }
        });

        InetAddress ipAddress = IPTools.getLocalIPv4Address();
        if (ipAddress != null){
            // IP ADRESİ BULUNAMADI - YAPILACAK
        }
        findViewById(R.id.taraBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                    try {
                        IPBul();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
                }).start();
            }
        });
    }
    private void appendResultsText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sonucText.append(text + "\n");
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

    private void IPBul() {

        appendResultsText("Taranıyor...");
        final long startTimeMillis = System.currentTimeMillis();

        SubnetDevices.fromLocalAddress().findDevices(new SubnetDevices.OnSubnetDeviceFound() {
            @Override
            public void onDeviceFound(Device device) {
                if(arananCihazMi(String.valueOf(device.ip)) == true){
                    IPDiziyeEkle(device.ip);
                }

            }

            @Override
            public void onFinished(ArrayList<Device> devicesFound) {
                float timeTaken =  (System.currentTimeMillis() - startTimeMillis)/1000.0f;
                appendResultsText("Ağdaki Toplam Cihaz Sayısı: " + devicesFound.size());
                //appendResultsText("Finished "+timeTaken+" s");
            }
        });
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
        LedControllerI ledContrrolerIService= retrofit.create(LedControllerI.class);
        ledContrrolerIService.getNodeInfo().enqueue(new Callback<NodeData>() {

            @Override
            public void onResponse(Call<NodeData> call, Response<NodeData> response) {

                if(response.body()!=null){
                    Log.v("Me Cevap:",response.body().toString());
                    // CİHAZDAN GELEN BİLGİLERE GÖRE DÜZENLENECEK
                    arananMi[0] = true;
                }
                else arananMi[0] = false;

            }

            @Override
            public void onFailure(Call<NodeData> call, Throwable t) {
                Log.e("Me Hata:",t.getMessage());
                arananMi[0] = false;
            }
        });

        while(arananMi[0] == null){
        }
        Log.v("ArananMi:" , arananMi[0].toString());
        return arananMi[0];


    }


}
