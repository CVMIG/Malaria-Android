package com.example.rock;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Jasper on 7/22/13.
 */
public class TestDownloaderService extends Service {
    private static final String TAG = "TestDownloaderService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, TAG + " Started", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStartCommand");
        try {
            if (internetAvailable()) new DownloaderTask(getApplicationContext()).execute(new URL("https://dl.dropboxusercontent.com/u/60324424/Malaria-debug-unaligned.apk"));
            else {
                Log.d(TAG, "no internet");
                Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return START_FLAG_REDELIVERY;
        }
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, TAG + " Destroyed", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }

    public boolean internetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (info != null) {
                if (info.isConnected()) return true;
            }
        }
        return false;
    }

    private class DownloaderTask extends AsyncTask<URL, Integer, Boolean> {
        private static final String TAG = "TestDownloaderService$DownloaderTask";
        NotificationHelper nh;
        Intent installIntent;

        public DownloaderTask(Context context) {
            nh = new NotificationHelper(context);
        }

        @Override
        protected void onPreExecute() {
            //nh.createNotification();
        }

        @Override
        protected Boolean doInBackground(URL... urls) {
            boolean succeeded = false;
            URL downloadPath = urls[0];
            if (downloadPath != null) {
                installIntent = Update(downloadPath);
            }
            return true;
        }

        protected void onProgressUpdate(Integer... progress) {
            //This method runs on the UI thread, it receives progress updates
            //from the background thread and publishes them to the status bar
            nh.progressUpdate(progress[0]);
        }

        @Override
        protected void onPostExecute(Boolean result)    {
            //The task is complete, tell the status bar about it
            //nh.completed();
            Log.d(TAG, "asynctask done!");
            if (installIntent != null) {
                Log.d(TAG, "may update na i-install");
                startActivity(installIntent);
                nh.completed(1);
            }
            else {
                Log.d(TAG, "di kelangan i-update");
            }
        }

        public Intent Update(URL apkurl){
            Intent intent =  null;
            try {

                HttpURLConnection c = (HttpURLConnection) apkurl.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();

                String raw = c.getHeaderField("Content-Disposition");
                String[] attributes;
                String filename;
                if(raw != null && raw.indexOf("=") != -1) {
                    attributes = raw.split("=");
                    Log.d(TAG, "filename: "+raw);
                } else {
                    attributes = c.getURL().getFile().split("/");
                    Log.d(TAG, "filename: "+attributes[attributes.length-1]);
                }

                filename = attributes[attributes.length-1];

                int contentLength = Integer.parseInt(c.getHeaderField("Content-Length"));

                String PATH = Environment.getExternalStorageDirectory() + "/download/";
                File file = new File(PATH);
                file.mkdirs();
                File outputFile = new File(file, filename);
                // CHECK!!! eto ung part na i-che-check kung updated ung app (pwedeng via package version code/name[safer!] or kung nag-eexists ung file)
                // will depend on naming scheme and version coding of app

                PackageInfo pInfo = getPackageManager().getPackageInfo("com.cajama.malaria", 0);

                Log.d(TAG, "PackageName = " + pInfo.packageName + "VersionCode = " + pInfo.versionCode);

                if (!outputFile.exists()) {
                    nh.createNotification(1);

                    FileOutputStream fos = new FileOutputStream(outputFile);

                    InputStream is = c.getInputStream();

                    byte[] buffer = new byte[1024];
                    int len1 = 0;
                    int total = 0;
                    while ((len1 = is.read(buffer)) != -1) {
                        total += len1;
                        publishProgress(total * 100 / contentLength);
                        fos.write(buffer, 0, len1);
                    }
                    fos.close();
                    is.close();//till here, it works fine - .apk is download to my sdcard in download file

                    /*Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse(PATH + "app.apk"))
                            .setType("application/android.com.app");
                    startActivity(promptInstall);//installation is not working*/
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + filename)), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.d(TAG, "Download done!");
                }
                else {
                    Log.d(TAG, "file exists");
                }
            } catch (IOException e) {
                Log.e(TAG, "Errrrrrrrrrrrrrorrrrrrrr!");
            } catch (Exception e) {
                Log.e(TAG, "erroooooorrrrr!!!!!!");
            } finally {
                return intent;
            }
        }
    }
}
