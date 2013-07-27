package com.example.rock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Jasper on 7/21/13.
 */
public class DefaultActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Update malaria starting", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, TestDownloaderService.class);
        startService(intent);
    }
}
