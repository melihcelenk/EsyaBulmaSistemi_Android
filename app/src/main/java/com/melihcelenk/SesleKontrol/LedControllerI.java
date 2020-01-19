package com.melihcelenk.SesleKontrol;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface LedControllerI {
    @GET("LED=ON")
    Call<ResponseBody> openLed();

    @GET("LED=OFF")
    Call<ResponseBody> closeLed();
}
