package com.melihcelenk.seslekontrol;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    TextView txvResult;
    Retrofit retrofit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txvResult = findViewById(R.id.textView);

        String url="http://192.168.1.34";
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .build();



    } // onCreate sonu

    public void getSpeechInput(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, 10);
        }
        else{
            Toast.makeText(this,"Konusma Tanima Desteklenmiyor", Toast.LENGTH_SHORT).show();
        }

    }// getSpeechInput sonu

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 10:
                if(resultCode==RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txvResult.setText(result.get(0));
                    if(result.get(0).equals("yak")) ledYak();
                    else if(result.get(0).equals("söndür")) ledKapa();
                    else if(result.get(0).equals("kimsin")) me();
                }


                break;
        }
    }// onActivityResult sonu

    public void ledYak(){
        LedControllerI ledContrrolerIService= retrofit.create(LedControllerI.class);
        ledContrrolerIService.openLed().enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println("Basarili" + response.toString());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("Basarisiz" + t.toString());
            }
        });
    }
    public void ledKapa(){
        LedControllerI ledContrrolerIService= retrofit.create(LedControllerI.class);
        ledContrrolerIService.closeLed().enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println("Basarili" + response.toString());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("Basarisiz" + t.toString());
            }
        });
    }

    public void me(){
        String url="http://192.168.1.34";
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))

                .build();
        LedControllerI ledContrrolerIService= retrofit.create(LedControllerI.class);
        ledContrrolerIService.getNodeData().enqueue(new Callback<NodeData>() {

            @Override
            public void onResponse(Call<NodeData> call, Response<NodeData> response) {
                Log.v("me response",response.body().toString());
            }

            @Override
            public void onFailure(Call<NodeData> call, Throwable t) {
               Log.e("me error:",t.getMessage());
            }
        });
    }
}
