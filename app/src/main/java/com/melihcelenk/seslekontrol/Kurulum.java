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

public class Kurulum extends AppCompatActivity {

    TextView sonucText;
    ArrayList<String> bulunanCihazlarArray;

    private RecyclerView cihazlarRV;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kurulum);

        sonucText = findViewById(R.id.sonucText);
        bulunanCihazlarArray = new ArrayList<String>();

        cihazlarRV = (RecyclerView) findViewById(R.id.cihazlarRV);
        cihazlarRV.setHasFixedSize(true); //
        layoutManager = new LinearLayoutManager(this);
        cihazlarRV.setLayoutManager(layoutManager);
        mAdapter = new cihazlarAdapter(bulunanCihazlarArray);
        cihazlarRV.setAdapter(mAdapter);

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
                bulunanCihazlarArray.add(text);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void IPBul() {

        final long startTimeMillis = System.currentTimeMillis();

        SubnetDevices.fromLocalAddress().findDevices(new SubnetDevices.OnSubnetDeviceFound() {
            @Override
            public void onDeviceFound(Device device) {
                //if(arananCihazMi(device.ip) == true){
                    arananCihazMi(String.valueOf(device.ip));
                    IPDiziyeEkle(device.ip);
                //}
                //appendResultsText(device.ip);
            }

            @Override
            public void onFinished(ArrayList<Device> devicesFound) {
                float timeTaken =  (System.currentTimeMillis() - startTimeMillis)/1000.0f;
                //appendResultsText("Devices Found: " + devicesFound.size());
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
                //NODEDATA NULLPOINTER VERİYOR DÜZELT
                Log.v("me response",response.body().toString());
                arananMi[0] = true;
            }

            @Override
            public void onFailure(Call<NodeData> call, Throwable t) {
                Log.e("me error:",t.getMessage());
                arananMi[0] = false;
            }
        });

        return arananMi[0];
    }


}
