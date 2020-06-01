package com.melihcelenk.seslekontrol;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

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

public class IPArkaplanKontrol extends AsyncTask<Void, Integer, String>{
    private int sonlanmaDurumu;
    ProgressBar progressBar;
    DatabaseHandler db;
    private Context context;
    private int progressDurum;
    private int progressDurumMax;
    private MutableLiveData<String> ipSonuc;
    ArrayList<Bolge> guncellenemeyenBolgeler;

    public IPArkaplanKontrol(Context context, ProgressBar progressBar, DatabaseHandler db, MutableLiveData<String> ipSonuc) {
        this.context= context;
        this.progressBar = progressBar;
        this.db = db;
        this.ipSonuc = ipSonuc;
    }
    public IPArkaplanKontrol(Context context, MutableLiveData<String> ipSonuc ) {
        this.context= context;
        this.db = new DatabaseHandler(context);
        this.ipSonuc = ipSonuc;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        guncellenemeyenBolgeler = new ArrayList<Bolge>();
        progressDurum=0;
        progressDurumMax=15;
        sonlanmaDurumu=0;
        try {
            if(progressBar!=null){
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setMax(progressDurumMax);
                progressBar.setProgress(progressDurum);
            }


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(Void... params) {
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
        return "doInBackground String";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Integer currentProgress = values[0];
        try{
            if(progressBar!=null){
                if(progressDurum/(float)progressDurumMax>0.70){
                    progressDurumMax*=2;
                    progressBar.setMax(progressDurumMax);

                }
                progressBar.setProgress(currentProgress);
                Log.v("Progress","ProgressDurum:"+progressDurum+" Max:"+progressDurumMax);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        Log.v("OnProgressUpdate",values[0].toString());
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        Log.v("OnPostExecute","sonlanmaDurumu:"+sonlanmaDurumu);

        try {
            if(progressBar!=null){
                progressBar.setVisibility(View.INVISIBLE);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        Log.v("onPostExecute", "Güncellenemeyen bölgeler listeleniyor...");
        String ulasilamayanCihazlar="";
        for(Bolge b : guncellenemeyenBolgeler){
            Toast.makeText(context, "Cihaza ulaşılamadı:"+b.get_etiket(), Toast.LENGTH_SHORT).show();
            Log.v("onPostExecute","Id:" + b.get_id() + " Etiket:" + b.get_etiket() + " IP:"+ b.get_ipAdresi() +" MAC:"+b.get_macAdresi());
            ulasilamayanCihazlar += "\n- "+b.get_etiket()+" -";
        }

        Log.v("IPArkaplanKontrol","onPostExecute: IPArkaplanKontrol sonlandı.");
        if(ulasilamayanCihazlar.isEmpty()){
            if(db.getButunBolgeler().isEmpty()){
                ipSonuc.setValue("Henüz kurulum yapılmamış.");
            }
            else{
                ipSonuc.setValue("Bütün cihazlar ulaşılabilir durumda.");
            }

        }
        else{
            ipSonuc.setValue("Şu cihazlara ulaşılamadı: "+ulasilamayanCihazlar);
        }


    }//onPostExecute sonu

    @Override
    protected void onCancelled(String result) {
        super.onCancelled(result);

    }

    public void BaglantiKontrol(){
        final int[] yanlisIPliCihazSayisi = new int[1];
        yanlisIPliCihazSayisi[0] = 0;
        for (final Bolge bolge : db.getButunBolgeler()) {
            publishProgress(++progressDurum);
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
                if(nodeData.getIp().equals(bolge.get_ipAdresi()) && nodeData.getNodeId() == bolge.get_id()){
                    Log.v("BaglantiKontrol", "IP'ler ve ID'ler eşit:" + nodeData.getIp());
                }
                else {
                    yanlisIPliCihazSayisi[0]++;
                    guncellenemeyenBolgeler.add(bolge);
                    Log.v("BaglantiKontrol", "Bir hata var. Cihazdaki ID ve IP: "+ nodeData.getNodeId() + " " + nodeData.getIp()+" Kayıtlı ID ve IP: "+bolge.get_id() + " " + bolge.get_ipAdresi());
                }

            }catch(Exception e){
                Log.v("YanlisIP", "Ulaşılamayan IP: "+bolge.get_ipAdresi()+ " , MAC adresi: "+ bolge.get_macAdresi());
                yanlisIPliCihazSayisi[0]++;
                guncellenemeyenBolgeler.add(bolge);
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
        else {
            sonlanmaDurumu++;
            Log.v("BaglantiKontrolSonu","Bağlantı kontrolü sonlandı. sonlanmaDurumu:"+sonlanmaDurumu);
        }
        while(sonlanmaDurumu==0){
            try {
                Thread.sleep(100);
                if(sonlanmaDurumu==1){Log.v("whileIci","sonlanmaDurumu:"+sonlanmaDurumu);}
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }// Bağlantı kontrol sonu-------------------------------------------------------
    private void IPBulveGuncelle() {
        final ArrayList<bulunanCihaz> bulunanCihazlarArray = new ArrayList<bulunanCihaz>();
        Log.v("IPBul","Cihazlar taranıyor...");

        final long startTimeMillis = System.currentTimeMillis();
        try{
            /* TODO: Lokal IP alınamadığında uygulama çöküyor*/

            SubnetDevices.fromLocalAddress().findDevices(new SubnetDevices.OnSubnetDeviceFound() {
                @Override
                public void onDeviceFound(Device device) {

                    publishProgress(++progressDurum);
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
                    Log.v("IpBulOnFinished","Ağdaki Toplam Cihaz Sayısı: " + devicesFound.size() + " Uyumlu cihaz sayısı:" + bulunanCihazlarArray.size());
                    for (int i=0;i<bulunanCihazlarArray.size();i++) {

                        try{
                            Log.v("IpDegistiriliyor","Mac:"+bulunanCihazlarArray.get(i).getMac()+" eski IP:" + db.ipGetir(bulunanCihazlarArray.get(i).getMac()) + " yeniIP:"+bulunanCihazlarArray.get(i).getIp());
                            db.ipDegistir(bulunanCihazlarArray.get(i).getMac(),bulunanCihazlarArray.get(i).getIp());
                            Log.v("IpDegistirildi","Mac:"+bulunanCihazlarArray.get(i).getMac()+" yeniIP:"+db.ipGetir(bulunanCihazlarArray.get(i).getMac()));
                            guncellenemeyenBolgeler.remove(db.getBolgeMacIle(bulunanCihazlarArray.get(i).getMac()));
                            Log.v("getBolgeMacIle","guncellenemeyenBolgeler.remove çalıştı.");
                        }catch(Exception e){
                            try{
                                Log.v("IpDegistirilemedi","Mac:"+bulunanCihazlarArray.get(i).getMac()+" IP:"+db.ipGetir(bulunanCihazlarArray.get(i).getMac()));
                            }
                            catch(Exception e1){
                                e1.printStackTrace();
                                Log.e("IpBulOnFinished","Veritabanına erişilemedi veya arraya ulaşılamadı.");
                            }
                            e.printStackTrace();
                        }
                    }
                    sonlanmaDurumu++;
                    Log.v("onFinished","sonlanmaDurumu2:"+sonlanmaDurumu);
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


