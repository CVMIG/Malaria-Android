package com.cajama.malaria.newreport;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.cajama.malaria.R;

/**
 * Created by GMGA on 7/28/13.
 */
public class GetLocation implements LocationListener {
    private String latitude;
    private String longitude;
    private Context myContext;

    public GetLocation(Context mContext){
        myContext = mContext;

        LocationManager locationManager = (LocationManager) myContext.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        onLocationChanged(location);
    }

    public void onLocationChanged(Location location) {
       // double lat = location.getLatitude();
       // double lng = location.getLongitude();
        double lat = 14.6484329;
        double lng = 121.0684466;
        latitude = String.valueOf(lat);
        longitude = String.valueOf(lng);
    }

    public String getLatitude(){
        return latitude;
    }

    public String getLongitude(){
        return longitude;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(myContext, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(myContext, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }
}
