package com.example.rock;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Jasper on 7/26/13.
 */
public class Test extends Activity {
    private DownloadManager mgr = null;
    private long lastDownload = -1L;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(onComplete);
    }

    public void startDownload(View v) {
        Uri uri = Uri.parse("http://commonsware.com/misc/test.mp4");

        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();

        lastDownload = mgr.enqueue(new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(
                        DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Demo")
                .setDescription("Something useful. No, really.")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "test.mp4"));

        v.setEnabled(false);
    }

    public void queryStatus(View v) {
        Cursor c = mgr.query(new DownloadManager.Query().setFilterById(lastDownload));

        if (c == null) {
            Toast.makeText(this, "Download not found!", Toast.LENGTH_LONG).show();
        } else {
            c.moveToFirst();

            Log.d(getClass().getName(),
                    "COLUMN_ID: " + c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)));
            Log.d(getClass().getName(),
                    "COLUMN_BYTES_DOWNLOADED_SO_FAR: "
                            + c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));
            Log.d(getClass().getName(),
                    "COLUMN_LAST_MODIFIED_TIMESTAMP: "
                            + c.getLong(c.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)));
            Log.d(getClass().getName(),
                    "COLUMN_LOCAL_URI: " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
            Log.d(getClass().getName(),
                    "COLUMN_STATUS: " + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)));
            Log.d(getClass().getName(),
                    "COLUMN_REASON: " + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));

            Toast.makeText(this, statusMessage(c), Toast.LENGTH_LONG).show();
        }
    }

    public void viewLog(View v) {
        startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
    }

    private String statusMessage(Cursor c) {
        String msg = "???";

        switch (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            case DownloadManager.STATUS_FAILED:
                msg = "Download failed!";
                break;

            case DownloadManager.STATUS_PAUSED:
                msg = "Download paused!";
                break;

            case DownloadManager.STATUS_PENDING:
                msg = "Download pending!";
                break;

            case DownloadManager.STATUS_RUNNING:
                msg = "Download in progress!";
                break;

            case DownloadManager.STATUS_SUCCESSFUL:
                msg = "Download complete!";
                break;

            default:
                msg = "Download is nowhere in sight";
                break;
        }

        return (msg);
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Toast.makeText(ctxt, "Download complete!", Toast.LENGTH_LONG).show();
        }
    };
}
