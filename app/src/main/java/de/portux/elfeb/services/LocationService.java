package de.portux.elfeb.services;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import de.portux.elfeb.R;
import de.portux.elfeb.model.GPSPosition;

public class LocationService extends Service {

  public class LocationServiceBinder extends Binder {
    public LocationService getService() {
      return LocationService.this;
    }
  }

  private static final String TAG = LocationService.class.getSimpleName();
  private static final int LOCATION_REQUEST_FASTEST_INTERVAL = 5000;

  private final LocationServiceBinder mBinder;

  private LocationManager mLocationManager;

  private Location mCurrentLocation;

  public LocationService() {
    mBinder = new LocationServiceBinder();
  }

  @Override
  public IBinder onBind(Intent intent) {
    Log.d(TAG, "Binding LocationService");
    try {
      mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
      String locationProvider = mLocationManager.getBestProvider(createLocationProviderCriteria(), true);

      mLocationManager.requestLocationUpdates(locationProvider, LOCATION_REQUEST_FASTEST_INTERVAL, 0, locationUpdateCallback);
      Log.d(TAG, "Successfully set up listener for location updates");
    } catch (SecurityException e) {
      Log.e(TAG, "Could not set up listener for location updates. No permission?");
      mCurrentLocation = null;
    }
    return mBinder;
  }

  @Override
  public void onDestroy() {
    mLocationManager.removeUpdates(locationUpdateCallback);
  }

  public Location getCurrentLocation() {
    return mCurrentLocation;
  }

  public GPSPosition getCurrentLocationAsGPSPosition() {
    if (mCurrentLocation == null) {
      return null;
    }
    return GPSPosition.of(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
  }

  public boolean isLocated() {
    return mCurrentLocation != null;
  }

  private Criteria createLocationProviderCriteria() {
    Criteria locationCriteria = new Criteria();
    locationCriteria.setAccuracy(Criteria.ACCURACY_FINE);
    locationCriteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
    return locationCriteria;
  }

  private final LocationListener locationUpdateCallback = new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
      if (location != null) {
        if (mCurrentLocation == null) {
          Toast locatedNotification = Toast.makeText(getApplicationContext(), getString(R.string.location_found), Toast.LENGTH_SHORT);
          locatedNotification.show();
        }
        mCurrentLocation = location;
      }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
  };

}
