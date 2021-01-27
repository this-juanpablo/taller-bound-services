package com.barrera.boundservices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private EditText eTxtTime, eTxtMeters;
    private Button btnSave;

    private OdometerService odometer;
    private boolean bound = false;
    private int tiempo_act = 5;
    public int metros_p = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        watchMileage();

        btnSave.setOnClickListener(click -> {
            tiempo_act = Integer.parseInt(eTxtTime.getText().toString());
            metros_p = Integer.parseInt(eTxtMeters.getText().toString());
        });
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            OdometerService.OdometerBinder odometerBinder =
                    (OdometerService.OdometerBinder) binder;
            odometer = odometerBinder.getOdometer();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }
    };

    private void watchMileage() {
        final TextView distanceView = findViewById(R.id.eTxtDistance);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                double distance = 0.0;
                if (odometer != null) {
                    distance = odometer.getMiles();
                }
                String distanceStr = String.format("%1$,.2f miles", distance);
                distanceView.setText(distanceStr);
                handler.postDelayed(this, tiempo_act * 1000);

                TextView tempo_act = findViewById(R.id.eTxtTimeAct);
                tempo_act.setText(tiempo_act + "  segundos");

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, OdometerService.Permission_String) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{OdometerService.Permission_String}, 698);
        } else {
            Intent intent = new Intent(this, OdometerService.class);
            intent = intent.putExtra("metros", metros_p);
            intent = intent.putExtra("tiempo", tiempo_act);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }

    private void init() {
        eTxtTime = findViewById(R.id.eTxtTime);
        eTxtMeters = findViewById(R.id.eTxtMeters);
        btnSave = findViewById(R.id.btnSave);
    }
}