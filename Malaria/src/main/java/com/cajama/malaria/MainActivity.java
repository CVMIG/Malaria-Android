package com.cajama.malaria;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cajama.background.FinalSendingService;
import com.cajama.background.SyncService;
import com.cajama.background.UpdateService;
import com.cajama.malaria.entryLogs.QueueLogActivity;
import com.cajama.malaria.entryLogs.SentLogActivity;
import com.cajama.malaria.newreport.NewReportActivity;

import android.os.Handler;

public class MainActivity extends Activity {

    private static final int UPDATE_SETTINGS = 1001;
    final Activity ctx = this;
    private Handler messageHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Intent startSyncDB = new Intent(this, SyncService.class);
        startService(startSyncDB);*/
        Intent startUpload = new Intent(this, FinalSendingService.class);
        startService(startUpload);
        /*Intent startUpdate = new Intent(this, UpdateService.class);
        startService(startUpdate);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivityForResult(settings, UPDATE_SETTINGS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    @Override
    public void onActivityResult(int request, int result, Intent data) {
        messageHandler.postDelayed(recreate, 0);
    }

    private Runnable recreate = new Runnable() {
        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= 11)
                ctx.recreate();
            else {
                onResume();
            }
            Log.w("Handler...", "Recreate requested.");
        }
    };
}