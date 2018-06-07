package com.aey.theapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button startTripBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startTripBtn = findViewById(R.id.btn_start_trip);
        startTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Maps Activity
                Intent mapsInent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(mapsInent);
            }
        });
    }
}
