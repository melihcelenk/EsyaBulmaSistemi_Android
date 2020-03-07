package com.melihcelenk.seslekontrol;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LedControllerI {
    @GET("LED=ON")
    Call<ResponseBody> openLed();

    @GET("LED=OFF")
    Call<ResponseBody> closeLed();

    @GET("me")
    Call<NodeData> getNodeData();

    @GET("konfigurasyon")
    Call<KonfigurasyonData> getKonfigurasyonData(@Query("setNodeId") String nodeId);

    @GET("sinyalGonder")
    Call<SinyalGonderData> getSinyalGonderData(@Query("id") String id,@Query("durum") String durum);
}
