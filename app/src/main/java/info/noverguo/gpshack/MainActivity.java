package info.noverguo.gpshack;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.noverguo.gpshack.callback.ResultCallback;
import info.noverguo.gpshack.receiver.ActionReceiver;
import info.noverguo.gpshack.receiver.ResetReceiver;
import info.noverguo.gpshack.service.GpsOffsetServiceBinder;
import info.noverguo.gpshack.service.LocalGpsOffsetHelper;

public class MainActivity extends BasePermissionsActivity {
    GpsOffsetServiceBinder gpsOffsetServiceBinder;
    LocationManager locationManager;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.et_old_latitude)
    EditText etOldLatitude;
    @Bind(R.id.et_old_longitude)
    EditText etOldLongitude;
    @Bind(R.id.et_latitude)
    EditText etLatitude;
    @Bind(R.id.et_longitude)
    EditText etLongitude;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    boolean needUpdateOffset = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        gpsOffsetServiceBinder = GpsOffsetServiceBinder.get(getApplicationContext());
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    gpsOffsetServiceBinder.setLatitude(Double.valueOf(etOldLatitude.getText().toString()));
                    gpsOffsetServiceBinder.setLongitude(Double.valueOf(etOldLongitude.getText().toString()));
                    needUpdateOffset = true;
                    updateOffset();
                } catch (NumberFormatException e) {
                    Snackbar.make(fab, "请输入正确的经纬度", Snackbar.LENGTH_LONG).show();
                }
            }
        });
        updateView();
        getLocation(new LocationCallback() {
            @Override
            public void onLocation(Location location) {
                if (needUpdateOffset) {
                    return;
                }
                updateLocation(location);
            }
        }, false);
        LocalGpsOffsetHelper.get(getApplicationContext()).getLatitudeOffset(new ResultCallback<Double>() {
            @Override
            public void onResult(final Double res) {
            }
        });
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getLocation(new LocationCallback() {
                    @Override
                    public void onLocation(Location location) {
                        updateLocation(location);
                    }
                }, true);
            }
        }, ActionReceiver.getIntentFilter());
        setGpsStatusListener();
    }

    private void setGpsStatusListener() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.addGpsStatusListener(new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                Log.i("GpsHack", event + ", " + gpsStatus.getMaxSatellites() + ", " + gpsStatus.getTimeToFirstFix());
                //                Iterator<GpsSatellite> iterator = gpsStatus.getSatellites().iterator();
                //                int i = 0;
                //                while(iterator.hasNext()) {
                //                    GpsSatellite satellite = iterator.next();
                //                    Log.i("GpsHack", i++ + ": " + satellite.getPrn() + ", " + satellite.getAzimuth() + ", " + satellite.getElevation() + ",
                // " + satellite.getSnr() + ", " + satellite.hasAlmanac() + ", " + satellite.hasEphemeris() + ", " + satellite.usedInFix());
                //                }
            }
        });
    }

    private void getLocation(final LocationCallback callback, final boolean once) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            callback.onLocation(lastKnownLocation);
            if (once) {
                return;
            }
        }
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location == null) {
                    return;
                }
                callback.onLocation(location);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                        .PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (once) {
                    locationManager.removeUpdates(this);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };
        List<String> providers = locationManager.getProviders(true);
        if (providers == null) {
            return;
        }
        for (String provider : providers) {
            locationManager.requestLocationUpdates(provider, 2000, 5, locationListener);
        }
    }

    private void updateOffset() {
        gpsOffsetServiceBinder.setLatitudeOffset(Double.valueOf(etLatitude.getText().toString()) - gpsOffsetServiceBinder.getLatitude());
        gpsOffsetServiceBinder.setLongitudeOffset(Double.valueOf(etLongitude.getText().toString()) - gpsOffsetServiceBinder.getLongitude());
        ResetReceiver.sendReset(getApplicationContext());
    }

    private void updateLocation(Location location) {
        gpsOffsetServiceBinder.setLatitude(location.getLatitude());
        gpsOffsetServiceBinder.setLongitude(location.getLongitude());
        updateView();
    }

    private void updateView() {
        etLongitude.setText((gpsOffsetServiceBinder.getLongitude() + gpsOffsetServiceBinder.getLongitudeOffset()) + "");
        etLatitude.setText((gpsOffsetServiceBinder.getLatitude() + gpsOffsetServiceBinder.getLatitudeOffset()) + "");
        etOldLatitude.setText(gpsOffsetServiceBinder.getLatitude() + "");
        etOldLongitude.setText(gpsOffsetServiceBinder.getLongitude() + "");
    }

    private interface LocationCallback {
        void onLocation(Location location);
    }

}
