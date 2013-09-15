package com.cajama.malaria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.cajama.background.DialogActivity;
import com.cajama.background.FinalSendingService;
import com.cajama.background.SyncService;
import com.cajama.background.UpdateService;
import com.cajama.malaria.entryLogs.QueueLogActivity;
import com.cajama.malaria.entryLogs.SentLogActivity;
import com.cajama.malaria.newreport.NewReportActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent startSyncDB = new Intent(this, SyncService.class);
        startService(startSyncDB);
        Intent startUpload = new Intent(this, FinalSendingService.class);
        startService(startUpload);
        Intent startUpdate = new Intent(this, UpdateService.class);
        startService(startUpdate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void submitNewReport(View view) {
        Intent intent = new Intent(this, NewReportActivity.class);
        startActivity(intent);
    }

    public void viewQueueLog(View view) {
    	Intent startUpload = new Intent(this, FinalSendingService.class);
        startService(startUpload);
        Intent intent = new Intent(this, QueueLogActivity.class);
        startActivity(intent);
    }

    public void viewSentLog(View view) {
        Intent intent = new Intent(this, SentLogActivity.class);
        startActivity(intent);
    }

}