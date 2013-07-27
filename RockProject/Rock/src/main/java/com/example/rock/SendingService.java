package com.example.rock;

import android.app.Service;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by Jasper on 7/26/13.
 */
public class SendingService extends Service {
    String TAG = "SendingService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // start sending compressed data to server (need server API and files)
        String url = "http://192.168.43.91:80/api/text/";
        File file = new File(Environment.getExternalStorageDirectory()+"/download/",
                "Malaria-debug-unaligned.apk");

        File file1 = new File(Environment.getExternalStorageDirectory()+"/download/", "Justice+League+-EFD.apk");

        if (file1.exists()) {
            Toast.makeText(getApplicationContext(), "Merong file!", Toast.LENGTH_LONG).show();
        }
        else Toast.makeText(getApplicationContext(), "Error: Walang file!", Toast.LENGTH_LONG).show();

        /*try {
            new AsyncHttpGetTask(file1).execute(new URL(url));
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        // for posting files
        new AsyncHttpPostTask(url).execute(file1);
        // for posting strings
        new AsyncPostStringTask(url).execute("Asdadasd");

        /*try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setRequestMethod("POST");
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        /*Toast.makeText(getApplicationContext(), "starting send", Toast.LENGTH_LONG).show();
        try {
            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httppost = new HttpPost(url);

            InputStreamEntity reqEntity = new InputStreamEntity(
                    new FileInputStream(file), -1);
            reqEntity.setContentType("binary/octet-stream");
            reqEntity.setChunked(false); // Send in multiple parts if needed
            httppost.setEntity(reqEntity);
            HttpResponse response = httpclient.execute(httppost);
            //Do something with response...
            Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
            Log.d(TAG, response.toString());

        } catch (Exception e) {
            // show error
        }*/

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy().");
        /*NotificationHelper nh = new NotificationHelper(getApplicationContext());
        nh.createNotification(2);*/
    }

    public class AsyncPostStringTask extends AsyncTask<String, Void, String> {
        String url;

        public AsyncPostStringTask(String url) {
            this.url = url;
        }

        /*public String getPage(URL url) throws IOException {
            final URLConnection connection = url.openConnection();
            HttpPost httpRequest = null;

            try {
                *//**//*httpRequest = new HttpPost(url.toURI());
                httpRequest.addHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryNUgWMoat5gpFpnnc");

                MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                mpEntity.addPart("name", new StringBody(file.getName().substring(file.getName().lastIndexOf(".")+1)));
                mpEntity.addPart("fileData", new FileBody(file));
                httpRequest.setEntity(mpEntity);*//**//*
                httpRequest = new HttpPost(url.toURI());

                MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "----WebKitFormBoundaryNUgWMoat5gpFpnnc", Charset.forName("UTF-8"));
                FileBody fb = new FileBody(file, "application/octet-stream");
                mpEntity.addPart("file", fb);
                httpRequest.addHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryNUgWMoat5gpFpnnc");
                httpRequest.setHeader("Content-Disposition", "form-data");

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);

            *//**//*HttpEntity entity = response.getEntity();
            BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
            InputStream stream = bufHttpEntity.getContent();

            String ct = connection.getContentType();

            final BufferedReader reader;

            if (ct.indexOf("charset=") != -1) {
                ct = ct.substring(ct.indexOf("charset=") + 8);
                reader = new BufferedReader(new InputStreamReader(stream, ct));
            }else {
                reader = new BufferedReader(new InputStreamReader(stream));
            }

            final StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            stream.close();
            return sb.toString();*//**//*
            return "asdf";
        }*/

        @Override
        protected String doInBackground(String... strings) {
            HttpPost post = null;
            HttpClient client = null;
            try {
                client = new DefaultHttpClient();
                post = new HttpPost(url);

                MultipartEntity mp = new MultipartEntity();
                ContentBody stringBody = new StringBody(strings[0]);
                mp.addPart("message", stringBody);
                post.setEntity(mp);

            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d(TAG, String.valueOf(post.getRequestLine()));

            try {
                HttpResponse response = client.execute(post);

                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                final StringBuilder out = new StringBuilder();
                String line;
                try {
                    while ((line = rd.readLine()) != null) {
                        out.append(line);
                    }
                }
                catch (Exception e) {

                }
                //wr.close();
                try {
                    rd.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //final String serverResponse = slurp(is);
                Log.d(TAG, "serverResponse: " + out.toString());




            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // TODO Auto-generated method stub
            return null;
        }
    }

    public class AsyncHttpPostTask extends AsyncTask<File, Void, String> {
        private final String TAG = AsyncHttpPostTask.class.getSimpleName();
        private String server;


        public AsyncHttpPostTask(final String server) {
            this.server = server;
        }

        @Override
        protected String doInBackground(File... params) {
            Log.d(TAG, "doInBackground");
            HttpClient http = AndroidHttpClient.newInstance("MyApp");
            HttpPost method = new HttpPost(this.server);

            try {
                MultipartEntity mp = new MultipartEntity();
                ContentBody cbFile = new FileBody(params[0], "text/plain");
                ContentBody cbFilename = new StringBody("File.txt");
                ContentBody cbName = new StringBody("file");
                mp.addPart("name", cbName);
                mp.addPart("filename", cbFilename);
                mp.addPart("file", cbFile);
                method.setEntity(mp);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //method.setEntity(new FileEntity(params[0], "text/plain"));

            Log.d(TAG, String.valueOf(method.getRequestLine()));

            try {
                HttpResponse response = http.execute(method);

                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                final StringBuilder out = new StringBuilder();
                String line;
                try {
                    while ((line = rd.readLine()) != null) {
                        out.append(line);
                    }
                }
                catch (Exception e) {

                }
                //wr.close();
                try {
                    rd.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //final String serverResponse = slurp(is);
                Log.d(TAG, "serverResponse: " + out.toString());




            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // TODO Auto-generated method stub
            return null;
        }

    }
}
