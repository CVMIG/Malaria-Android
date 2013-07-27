package com.example.rock;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyService extends Service {
    private static final String TAG = "MyService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Log.d(TAG, "onCreate");

        /*NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String title = "Service!!!";
        String message = "Start service!";

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(soundUri);

        notificationManager.notify(0, mBuilder.build());*/
         // Set looping
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStartCommand");
        Update("https://dl.dropboxusercontent.com/u/60324424/Malaria-debug-unaligned.apk");
        return START_NOT_STICKY;
    }

    public void Update(String apkurl){
        try {

            URL url = new URL(apkurl);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();

            String PATH = Environment.getExternalStorageDirectory() + "/download/";
            File file = new File(PATH);
            file.mkdirs();
            File outputFile = new File(file, "app.apk");
            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();//till here, it works fine - .apk is download to my sdcard in download file

            Toast.makeText(this, "Download complete, installing...", Toast.LENGTH_LONG).show();

            /*Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse(PATH + "app.apk"))
                    .setType("application/android.com.app");
            startActivity(promptInstall);//installation is not working*/
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + "app.apk")), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Update error!", Toast.LENGTH_LONG).show();
        }
    }
}