package com.melihcelenk.seslekontrol.activityler.kurulumactivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melihcelenk.seslekontrol.DatabaseHandler;
import com.melihcelenk.seslekontrol.LedControllerI;
import com.melihcelenk.seslekontrol.R;
import com.melihcelenk.seslekontrol.activityler.KonfigureEtActivity;
import com.melihcelenk.seslekontrol.modeller.bulunanCihaz;
import com.melihcelenk.seslekontrol.modeller.NodeData;
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
    RecyclerView cihazlarRV;
    cihazlarAdapter mAdapter;
    RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kurulum);
        setTitle("Kurulum");

        db = new DatabaseHandler(this);
        sonucText = findViewById(R.id.sonucText);
        bulunanCihazlarArray = new ArrayList<bulunanCihaz>();
        cihazlarRV = (RecyclerView) findViewById(R.id.cihazlarRV);
        layoutManager = new LinearLayoutManager(this);
        cihazlarRV.setLayoutManager(layoutManager);
        mAdapter = new cihazlarAdapter(bulunanCihazlarArray);
        cihazlarRV.setAdapter(mAdapter);


        mAdapter.setOnItemClickListener(new cihazlarAdapter.OnItemClickListener() {
            // Listelenen cihazlardan birinin üzerine tıklandığında
            @Override
            public void onItemClick(int position) {
                bulunanCihaz bc = bulunanCihazlarArray.get(position);
                //mAdapter.notifyDataSetChanged();
                Intent intent = new Intent(getApplicationContext(), KonfigureEtActivity.class);
                intent.putExtra("ipAdresi",bc.getIp());
                intent.putExtra("macAdresi",bc.getMac());
                startActivity(intent);
            }
            // Listelenen cihazlardan birinin yanındaki led yakma switch butonuna tıklandığında
            @Override
            public void onLedYakClick(int position) {
                bulunanCihazlarArray.get(position).LedDegistir();
            }
        });
        try{
            InetAddress ipAddress = IPTools.getLocalIPv4Address();
            if (ipAddress == null){
                Toast.makeText(this, "IP adresiniz alınamadı.", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            e.printStackTrace();
        }


        // TARA BUTONU
        findViewById(R.id.taraBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Butonun başlattığı thread sürmüyorsa buton yeni bir thread başlatabilir
                if(tButon == null || !tButon.isAlive()){
                    tButon = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                IPBul(); // Sistemdeki uygun cihazların IP'lerini bulur
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
        // UiThread dışındaki thread'lerde Ui bileşenlerine doğrudan müdahale edilemez.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sonucText.setText(text + "\n");
                //sonucText.append(text + "\n");
                findViewById(R.id.taraBtn).setEnabled(butonDurum);
            }
        });
    }
    private void IPDiziyeEkle(final String cihazMAC, final String cihazIP) {
        // UiThread dışındaki thread'lerde Ui bileşenlerine doğrudan müdahale edilemez.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bulunanCihazlarArray.add(new bulunanCihaz(cihazMAC, cihazIP));
                mAdapter.notifyDataSetChanged();
            }
        });
    }
    private void bulunanCihazlarArrayTemizle() {
        // UiThread dışındaki thread'lerde Ui bileşenlerine doğrudan müdahale edilemez.
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
        //final long startTimeMillis = System.currentTimeMillis();
        try{
            SubnetDevices.fromLocalAddress().findDevices(new SubnetDevices.OnSubnetDeviceFound() {
                // Ağda subnet cihazı bulunduğunda
                @Override
                public void onDeviceFound(Device device) {
                    NodeData n = null;
                    try{
                        // Eğer bu cihaz projeye ait bir cihaz ise n'ye ata
                        n = ((n = arananCihazsaGetir(String.valueOf(device.ip))) != null) ? n : null;
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                    if (n!=null){
                        try {
                            // Cihazın MAC ve IP'sini diziye ekle (NodeData ile bulunanCihaz sınıfları karıştırılmamalı, birbirleri yerine kullanılmamalıdır, NodeData Retrofit'ten çekilen veri için var)
                            IPDiziyeEkle(n.getMacAddress(), n.getIp());
                        }
                        catch (Exception e) {
                            Log.e("Diziekle",e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onFinished(ArrayList<Device> devicesFound) {
                    //float timeTaken =  (System.currentTimeMillis() - startTimeMillis)/1000.0f;
                    sonucTextveButonDegistir("Ağdaki Toplam Cihaz Sayısı: " + devicesFound.size() + "\n\nListedeki her bir cihazın üstüne tıklayarak etiket veriniz.",true);
                    //sonucTextveButonDegistir("Finished "+timeTaken+" s");
                }
            });
        }catch(Exception e){
            Log.e("IPBulHata",e.getLocalizedMessage());
            e.printStackTrace();
        }
    }//--------IPBul sonu-----------------------------------------------------------------------

    public NodeData arananCihazsaGetir(String ipAdresi){
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
            NodeData nodeData = ledControllerIService.getNodeData().execute().body();
            Log.v("Node", nodeData.getIp());
            return nodeData;
        }
        catch(Exception e){
            Log.v("RetrofitHata",e.getLocalizedMessage());
            e.printStackTrace();
            return null;
        }
    }//----------------------arananCihazsaGetir sonu------------------------------

}
