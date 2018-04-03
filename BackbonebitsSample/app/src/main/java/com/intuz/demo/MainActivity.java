package com.intuz.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.backbonebits.Backbonebits;

public class MainActivity extends Activity {
    Backbonebits b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        Button brnNext = findViewById(R.id.brnNext);
        b = new Backbonebits(MainActivity.this);
        b.isShakeEnabled(true);
        b.initializeBBSDK(MainActivity.this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.openBBHelper();
            }
        });

        brnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,AnotherActivity.class);
                startActivity(i);
            }
        });

    }


}
