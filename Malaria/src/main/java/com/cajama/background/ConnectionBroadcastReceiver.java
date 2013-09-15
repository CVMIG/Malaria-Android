package com.cajama.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.cajama.malaria.R;

/**
 * Created by Jasper on 7/26/13.
 */
public class ConnectionBroadcastReceiver extends BroadcastReceiver {
    String TAG = "ConnectionBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo activeWifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isConnected = activeNetInfo != null && (activeNetInfo.isConnectedOrConnecting() || activeWifiInfo.isConnectedOrConnecting());
        if (isConnected) {
            Log.d(TAG, "connected " +isConnected);
            Intent startServiceIntent = new Intent(context, FinalSendingService.class);
            context.startService(startServiceIntent);
        }
        else Log.d(TAG, "not connected " +isConnected);

    }


}
