package com.cajama.background;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cajama.malaria.R;
import com.cajama.malaria.encryption.RSA;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

/**
 * Created by Jasper on 8/4/13.
 */
public class FinalSendingService extends Service {
    final String TAG = "FinalSendingService";
    String onResult = "";
    SendFileAsyncTask asyncTask;
    File sentList, reportsDirectory;
    File[] reports;
    int count, tries=0;

    SendFileAsyncTask.OnAsyncResult onAsyncResult = new SendFileAsyncTask.OnAsyncResult() {
        @Override
        public void onResult(int resultCode, String message) {
            try {
                append_report(resultCode, message);
            } catch (Exception e) {
                Log.d(TAG, "error!");
                e.printStackTrace();
                stopSelf();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    	//if (asyncTask.getStatus() == Status.FINISHED || asyncTask.getStatus() == Status.PENDING) {
        asyncTask = new SendFileAsyncTask(getString(R.string.server_address).concat(getString(R.string.api_send)));
        asyncTask.setOnResultListener(onAsyncResult);
    	//}
    	
    	reportsDirectory = new File(String.valueOf(Environment.getExternalStorageDirectory()) + "/Android/data/com.cajama.malaria/files/ZipFiles");
        if (!reportsDirectory.exists()) reportsDirectory.mkdir();
        reports = reportsDirectory.listFiles();

        sentList = new File(String.valueOf(Environment.getExternalStorageDirectory()) + "/Android/data/com.cajama.malaria/files/sent_log.txt");
        if (!sentList.exists()) {
            try {
                sentList.createNewFile();
                Log.d(TAG, "Created sentlist file");
            } catch (IOException e) {
                Log.d(TAG, "Failed to create sentList.txt!");
                e.printStackTrace();
                stopSelf();
            }
        }

        count = 0;

        Log.d(TAG, "onCreate()");
        //Toast.makeText(getApplicationContext(), "Sending reports...", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        Bundle extras = null;
        if (intent!=null) extras = intent.getExtras();
        String result = "";
        if (extras != null) result = extras.getString("message");

        Log.d(TAG, result);

        if (isConnected()) {
            Log.d(TAG, "may internet!");
            if (!result.isEmpty()) {
                try {
                    String[] split = result.split("\n");
                    byte[] skByte = split[1].getBytes();
                    MessageDigest sha = MessageDigest.getInstance("SHA-1");
                    skByte = sha.digest(skByte);
                    //skByte = Arrays.copyOf(skByte, 16);
                    /*RSA rsa = new RSA(skByte);
                    new PostStringAsyncTask().execute(rsa.encryptRSA(skByte));*/
                    Log.d(TAG, "Posting new user and pass");
                    new PostStringAsyncTask().execute(split[0]+"\n"+byteArrayToHexString(skByte));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "failed to encrypt retpyed password");
                    stopSelf();
                }
            }

            else sendFile(count);

            return ret;
        }
        else {
            Toast.makeText(getApplicationContext(), "No internet connection!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "no internet connection");
            //return START_NOT_STICKY;
            return ret;
        }
    }

    public static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    public void sendFile(int count) {
        if (reportsDirectory.exists()) {
            reports = reportsDirectory.listFiles();
            if (count < reports.length) {
            //for (File report : reports) {
                Log.d(TAG, "# of files to send: " + String.valueOf(reports.length));
                if(asyncTask.getStatus() == AsyncTask.Status.PENDING) {
                    asyncTask.execute(reports[count]);
                }
                else if(asyncTask.getStatus() == AsyncTask.Status.RUNNING){
                    //reports = reportsDirectory.listFiles();
                    Log.d(TAG, "task alreading running: " + reports.length + " on queue");
                    //asyncTask.execute(reports);
                }
                else if(asyncTask.getStatus() == AsyncTask.Status.FINISHED){
                    //asyncTask.execute(report);
                    //reports = reportsDirectory.listFiles();
                    //stopSelf();
                    //asyncTask.execute(reports);
                    Log.d(TAG, "task finished!");
                    sendNext();
                }
            }
            else {
                Log.d(TAG, "No reports to send");
                count = 0;
                stopSelf();
            }
            //}
        }
    }

    public void append_report(int resultCode, String message) throws IOException {
        if (resultCode == 1) {
            FileWriter fileWriter = new FileWriter(sentList, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            String[] split = message.split("_");
            bufferedWriter.write(split[0]+"\n");
            bufferedWriter.write(split[1]+"\n");
            bufferedWriter.write(split[2].substring(0, split[2].length()-4)+"\n");
            bufferedWriter.close();
            Log.d(TAG, message + " added to sent list");
            count++;
            sendFile(count);
        }
        else if (resultCode == -1) {
            startDialog(-1);
        }
        else {
            Log.d(TAG, message + " not added to sent list");
            count++;
            sendFile(count);
        }
    }

    public void startDialog(int tries) {
        Intent intent = new Intent(this.getApplicationContext(), DialogActivity.class);
        intent.putExtra("passwd", this.getClass().getCanonicalName());
        intent.putExtra("tries", String.valueOf(tries));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void checkResult() {
        Log.d(TAG, "onResult = " + onResult);
        if (onResult.equals("OK") || onResult.endsWith("5")) sendNext();
        else {
            tries++;
            if (tries < 5) startDialog(5-tries);
            else sendNext();
        }
    }

    public void sendNext() {
        count++;
        sendFile(count);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        Toast.makeText(getApplicationContext(), "No more reports to send!", Toast.LENGTH_LONG).show();
        /*Intent intent1 = new Intent(getApplicationContext(), FinalSendingService.class);
    	startService(intent1);*/
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        //NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo activeWifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return activeWifiInfo != null && activeWifiInfo.isConnectedOrConnecting();
    }

    public class PostStringAsyncTask extends AsyncTask<String, Void, String> {
        String url = getString(R.string.server_address).concat(getString(R.string.api_retry));

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, strings[0]);
            HttpPost post = null;
            HttpClient client = null;
            MultipartEntity mp = null;
            try {
                client = new DefaultHttpClient();
                post = new HttpPost(url);

                mp = new MultipartEntity();
                ContentBody stringBody = new StringBody(strings[0]);
                mp.addPart("message", stringBody);
                post.setEntity(mp);

                Log.d(TAG, String.valueOf(post.getRequestLine()));

                HttpResponse response = client.execute(post);
                Log.d(TAG, "response: "+ response.getStatusLine());

                BufferedReader getReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                final StringBuilder getResultBuilder = new StringBuilder();
                String getResult;
                try {
                    while ((getResult = getReader.readLine()) != null) {
                        getResultBuilder.append(getResult);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "error in reading get result");
                    e.printStackTrace();
                    stopSelf();
                }

                getReader.close();

                onResult = getResultBuilder.toString();
                checkResult();
            } catch (Exception e) {
                e.printStackTrace();
                stopSelf();
            }

            return null;
        }
    }
}
