package com.melihcelenk.seslekontrol.modeller;

import com.melihcelenk.seslekontrol.LedControllerI;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class bulunanCihaz {
    private String mac;

    public String getMac(){ return mac; }
    public String getIp() {
        return ip;
    }

    private String ip;

    public Boolean getLedDurum() {
        return ledDurum;
    }

    private Boolean ledDurum;

    public bulunanCihaz(String mac, String ip){
        this.mac = mac;
        this.ip = ip;
        ledDurum = false;
    }
    public bulunanCihaz(String mac, String ip, Boolean ledDurum){
        this.mac = mac;
        this.ip = ip;
        this.ledDurum = ledDurum;
    }

    public void LedDegistir(){
        if (ledDurum == false) ledYak();
        else ledKapa();
    }
    private void ledYak(){
        ledYak(ip);
        if(true) ledDurum = true;
    }
    private void ledKapa(){
        ledKapa(ip);
        if(true) ledDurum = false;
    }
    private void ledYak(String ip){
        String url = "http://" + ip;
        Retrofit retrofit = new Retrofit.Builder()
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
    private void ledKapa(String ip){
        String url = "http://" + ip;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .build();
        LedControllerI ledContrrolerIService= retrofit.create(LedControllerI.class);
        ledContrrolerIService.closeLed().enqueue(new Callback<ResponseBody>() {

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
}
