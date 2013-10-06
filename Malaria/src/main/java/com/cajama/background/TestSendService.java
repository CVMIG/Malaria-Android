package com.cajama.background;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.actionbarsherlock.R;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Jasper Cacby on 9/20/13.
 */
public class TestSendService extends Service {
    File reportDirectory;
    File[] reports;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        reportDirectory = new File(getExternalFilesDir(null), "ZipFiles");
        if (reportDirectory.isDirectory()) {
            if (!reportDirectory.exists()) reportDirectory.mkdirs();
        }

        reports = reportDirectory.listFiles();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        for (File report : reports) {
            RequestParams params = new RequestParams();
            try {
                params.put("file", report);
                params.put("name", "file");
                params.put("filename", report.getName());
                TestSendAsyncTask.post("api/send/", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d("TestSendService", response);
                    }
                });
            }  catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return START_NOT_STICKY;
    }
}
