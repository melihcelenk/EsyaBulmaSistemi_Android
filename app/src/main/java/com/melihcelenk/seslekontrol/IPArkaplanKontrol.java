package com.melihcelenk.seslekontrol;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melihcelenk.seslekontrol.modeller.Bolge;
import com.melihcelenk.seslekontrol.modeller.NodeData;
import com.melihcelenk.seslekontrol.modeller.bulunanCihaz;
import com.stealthcopter.networktools.SubnetDevices;
import com.stealthcopter.networktools.subnet.Device;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IPArkaplanKontrol extends AsyncTask<Void, Integer, Void>{
    ProgressBar progressBar;
    DatabaseHandler db;
    private Context context;
    public IPArkaplanKontrol(Context context, ProgressBar progressBar, DatabaseHandler db) {
        this.context= context;
        this.progressBar = progressBar;
        this.db = db;
    }
    public IPArkaplanKontrol(Context context, DatabaseHandler db) {
        this.context= context;
        this.db = db;
    }
    public IPArkaplanKontrol(Context context) {
        this.context= context;
        this.db = new DatabaseHandler(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        try {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(100);
            progressBar.setProgress(10);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
//        for (int i = 0; i < 101; i = i + 10) {
//            try {
//                publishProgress(i);
//                Thread.sleep(1000);
//            }
//            catch (InterruptedException e) {
//            }
//        }
        try {
            BaglantiKontrol();
        }
        catch (Exception e) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        try {
            progressBar.setVisibility(View.INVISIBLE);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Integer currentProgress = values[0];
        try{
            progressBar.setProgress(currentProgress);
        }catch(Exception e){
            e.printStackTrace();
        }
        Log.v("OnProgressUpdate",values[0].toString());
    }

    @Override
    protected void onCancelled(Void result) {
        super.onCancelled(result);

    }

    public void BaglantiKontrol(){
        final int[] yanlisIPliCihazSayisi = new int[1];
        yanlisIPliCihazSayisi[0] = 0;
        for (final Bolge bolge : db.getButunBolgeler()) {

            String url="http://"+bolge.get_ipAdresi();;
            Log.v("BaglantiKontrol","Kontrol ediliyor: "+ url);
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
                if(nodeData.getIp().equals(bolge.get_ipAdresi())){
                    Log.v("BaglantiKontrol", "IP'ler eşit:" + nodeData.getIp());
                }
                else {
                    yanlisIPliCihazSayisi[0]++;
                    Log.v("BaglantiKontrol", "Bir hata var. Cihazdaki IP: "+nodeData.getIp()+" Eski IP:"+bolge.get_ipAdresi());
                }


            }catch(Exception e){
                Log.v("YanlisIP", "IP'ye ulaşılamadı:"+bolge.get_ipAdresi()+ " güncellenecek.");
                yanlisIPliCihazSayisi[0]++;
                Log.v("BaglantiKontrol",e.getLocalizedMessage());
                e.printStackTrace();
            }

        }// for sonu
        Log.v("yanlisIPCihazSayisi",yanlisIPliCihazSayisi[0]+"");
        if(yanlisIPliCihazSayisi[0]>0){
            try {
                IPBulveGuncelle();
            }catch(Exception e){
                Toast.makeText(context, "IP'ler güncellenirken bir hata meydana geldi.", Toast.LENGTH_SHORT).show();
                Log.v("IPBulveGuncelleHata","IP'ler güncellenirken bir hata meydana geldi");
            }
        }
    }// Bağlantı kontrol sonu-------------------------------------------------------
    private void IPBulveGuncelle() {
        final ArrayList<bulunanCihaz> bulunanCihazlarArray = new ArrayList<bulunanCihaz>();
        Log.v("IPBul","Cihazlar taranıyor...");

        final long startTimeMillis = System.currentTimeMillis();
        try{
            /* TODO: Lokal IP alınamadığında uygulama çöküyor*/
            /* TODO: Sinyalde hata olursa IP güncelleme eklenecek */
            /*TODO: ArpInfo, ARPInfo.getIPAddressFromMAC(null) | MAC ile bulma ekle*/
            SubnetDevices.fromLocalAddress().findDevices(new SubnetDevices.OnSubnetDeviceFound() {
                @Override
                public void onDeviceFound(Device device) {
                    NodeData n;
                    n = ((n = arananCihazsaGetir(String.valueOf(device.ip))) != null) ? n : null;

                    if (n!=null){
                        try {
                            //IP'ler diziye ekleniyor
                            Log.v("onDeviceFound","n null değil, cihaz diziye eklenecek...");
                            Log.v("onDeviceFound","IP:"+n.getIp()+" MAC:"+n.getMacAddress());
                            final NodeData finalN = n;
                            //runOnUiThread(new Runnable() {
                            //    @Override
                            //    public void run() {
                            bulunanCihazlarArray.add(new bulunanCihaz(finalN.getMacAddress(), finalN.getIp()));
                            //    }
                            //});
                        }
                        catch (Exception e) {
                            Log.e("Diziekle",e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onFinished(ArrayList<Device> devicesFound) {
                    float timeTaken =  (System.currentTimeMillis() - startTimeMillis)/1000.0f;
                    Log.v("IpBulOnFinished","Ağdaki Toplam Cihaz Sayısı: " + devicesFound.size() + " Uyumlu cihaz sayısı:" + bulunanCihazlarArray.size());
                    for (int i=0;i<bulunanCihazlarArray.size();i++) {
                        Log.v("IpDegistiriliyor","Mac:"+bulunanCihazlarArray.get(i).getMac()+" eski IP:" + db.ipGetir(bulunanCihazlarArray.get(i).getMac()) + " yeniIP:"+bulunanCihazlarArray.get(i).getIp());

                        try{
                            db.ipDegistir(bulunanCihazlarArray.get(i).getMac(),bulunanCihazlarArray.get(i).getIp());
                            Log.v("IpDegistirildi","Mac:"+bulunanCihazlarArray.get(i).getMac()+" yeniIP:"+db.ipGetir(bulunanCihazlarArray.get(i).getMac()));
                        }catch(Exception e){
                            try{
                                Log.v("IpDegistirilemedi","Mac:"+bulunanCihazlarArray.get(i).getMac()+" yeniIP:"+db.ipGetir(bulunanCihazlarArray.get(i).getMac()));
                            }
                            catch(Exception e1){
                                e1.printStackTrace();
                                Log.e("IpBulOnFinished","Veritabanına erişilemedi veya arraya ulaşılamadı.");
                            }
                            e.printStackTrace();
                        }
                    }

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

        }catch(Exception e){
            Log.v("RetrofitHata",e.getLocalizedMessage());
            e.printStackTrace();
            return null;

        }
    }//----------------------arananCihazsaGetir sonu------------------------------
}


