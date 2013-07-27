package com.example.rock;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends Activity {
    Button update;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        update = (Button) findViewById(R.id.button);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Update("https://dl.dropboxusercontent.com/u/60324424/Malaria-debug-unaligned.apk");
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

            tv = (TextView)findViewById(R.id.textView);
            tv.setText(apkurl);

            /*Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse(PATH + "app.apk"))
                    .setType("application/android.com.app");
            startActivity(promptInstall);//installation is not working*/
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + "app.apk")), "application/vnd.android.package-archive");
            startActivity(intent);

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Update error!", Toast.LENGTH_LONG).show();
        }
    }
}
