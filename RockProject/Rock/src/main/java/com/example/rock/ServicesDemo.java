package com.example.rock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class ServicesDemo extends Activity implements OnClickListener {
    private static final String TAG = "ServicesDemo";
    Button buttonStart, buttonStop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);

        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
    }

    public void onClick(View src) {
        switch (src.getId()) {
            case R.id.buttonStart:
                Log.d(TAG, "onClick: starting srvice");
                startService(new Intent(this, SendingService.class));
                Toast.makeText(getApplicationContext(), "Starting SendingService", Toast.LENGTH_LONG).show();
                break;
            case R.id.buttonStop:
                Log.d(TAG, "onClick: stopping srvice");
                stopService(new Intent(this, SendingService.class));
                Toast.makeText(getApplicationContext(), "Stopping SendingService", Toast.LENGTH_LONG).show();
                //File file = new File(Environment.getExternalStorageDirectory() + "/download/" + "Malaria-debug-unaligned.apk");
                //if (file.delete()) Log.d(TAG, "file deleted!");
                break;
        }
    }
}