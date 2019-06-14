package com.neostra.imin.printerupdate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class TestActivity extends Activity {
    private Button strat;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_print_case);
        strat = findViewById(R.id.negative);
        strat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(TestActivity.this, NeostraPrinterUpdateService.class);
                startForegroundService(serviceIntent);
            }
        });

    }
}
