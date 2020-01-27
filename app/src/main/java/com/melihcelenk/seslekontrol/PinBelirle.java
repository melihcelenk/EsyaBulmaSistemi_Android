package com.melihcelenk.seslekontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PinBelirle extends AppCompatActivity {

    EditText pinEdt1;
    EditText pinEdt2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_belirle);
        pinEdt1 = findViewById(R.id.pinEdt1);
        pinEdt2 = findViewById(R.id.pinEdt2);

    }

    public void pinKaydet(View v){
        Button pinKaydetBtn = findViewById(R.id.pinKaydetBtn);
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (pinEdt1.getText().toString().equals(pinEdt2.getText().toString())){
            editor.putString("pinKodu",pinEdt1.getText().toString());
            editor.commit();
        }
        String savedString = sharedPreferences.getString("pinKodu","0000");
        pinKaydetBtn.setText(savedString);
    }
}
