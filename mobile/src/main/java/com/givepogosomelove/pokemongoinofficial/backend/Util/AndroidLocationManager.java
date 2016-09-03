package com.givepogosomelove.pokemongoinofficial.backend.Util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.givepogosomelove.pokemongoinofficial.backend.IConfig;
import com.givepogosomelove.pokemongoinofficial.backend.ILocationManager;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.device.LocationFixes;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import POGOProtos.Networking.Envelopes.SignatureOuterClass;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;


/**
 * Created by Angelo on 13.08.2016.
 */
public class AndroidLocationManager implements ActivityCompat.OnRequestPermissionsResultCallback, ILocationManager, GpsStatus.Listener {
	private static final AndroidLocationManager INSTANCE = new AndroidLocationManager();
	private final IConfig CONFIG = Config.getInstance();

	private final long GPS_LOCATION_REFRESH_TIME_MILLISECONDS = 10000L;
	private final long NETWORK_LOCATION_REFRESH_TIME_MILLISECONDS = 0l;
	private final float LOCATION_REFRESH_DISTANCE_METERS = 0.0f; // TODO: Tweaking these
	private LocationManager locationManager;
	private Location lastKnownLocation;
	private double lastKnownAltitude = 20d + (new Random().nextDouble() * 50);
	private boolean canGetNetworkLocation;
	private boolean canGetGPSLocation;
	private boolean hasNetworkPermission;
	private boolean hasGPSPermission;
	private boolean hasAlreadyAsked = false;
	private final cLocationListener mLocationListener = new cLocationListener();
	private final int REQUEST_GPS_PERMISSION = 40;
	private final int REQUEST_NETWORK_PERMISSION = 34;

	private boolean initialized = false;

	private ArrayList<PokemonGo> pokemonGoList = new ArrayList<>();
	private ArrayList<SignatureOuterClass.Signature.LocationFix> lastLocationFixes = new ArrayList<>();
	private boolean listening;
	private Subscription pokemonGoNotifier;


	private AndroidLocationManager() {
	}

	@Override
	public void init() {
		locationManager = (LocationManager) CONFIG
				.getContext()
				.getSystemService(Context.LOCATION_SERVICE);
		initialized = true;

		checkGPSPermission();
		//requestLocationUpdates();
	}

	/**
	 * Tells the SensorManager to make the given PokemonGo object listen to it's sensors.
	 *
	 * @param pokemonGo Object that you want to listen to the sensors.
	 */
	@Override
	public void listen(@NonNull PokemonGo pokemonGo) {
		pokemonGoList.add(pokemonGo);
		startListening();
	}

	@Override
	public void stop(@NonNull PokemonGo pokemonGo){
		pokemonGoList.remove(pokemonGo);

		if(pokemonGoList.isEmpty()){
			stopListening();
		}
	}

	@Override
	public void stopAll(){
		pokemonGoList.clear();
		stopListening();
	}

	private synchronized void startListening() {
		if (listening) {
			return;
		}

		pokemonGoNotifier = Observable
				.interval(0L, CONFIG.getLocationUpdateInterval(), TimeUnit.MILLISECONDS, new Scheduler() {
					@Override
					public Worker createWorker() {
						//Copy Location
						double latitude = lastKnownLocation.getLatitude();
						double longitude = lastKnownLocation.getLongitude();
						double altitude = lastKnownLocation.getAltitude();

						LocationFixes fixes = new LocationFixes();
						fixes.addAll(lastLocationFixes);
						PokemonGo[] pokemonGos = (PokemonGo[]) pokemonGoList.toArray();

						for (PokemonGo go:
								pokemonGos) {
							go.setLocation(latitude, longitude, altitude);
							go.setLocationFixes(fixes);
						}
						return null;
					}
				})
				.subscribe();

		listening = true;
	}

	private synchronized void stopListening() {
		if (!listening) {
			return;
		}

		pokemonGoNotifier.unsubscribe();

		try {
			locationManager.removeUpdates(mLocationListener);
		} catch (SecurityException e) {

		}

		listening = false;
	}


	public static AndroidLocationManager getInstance() {
        return INSTANCE;
    }




    public boolean isLocationEnabled() {
        return canGetGPSLocation || canGetNetworkLocation;
    }

    /**
     * HOOK for PokemonService and other users.
     */
    public void onLocationDisabled() {
    }

    /**
     * HOOK for PokemonService and other users.
     */
    public void onLocationEnabled() {
    }

    /**
     * HOOK for PokemonService and other users.
     *
     * @param lastKnownLocation
     */
    public void onLocationChanged(Location lastKnownLocation) {
    }

	private void doOnPermissionAccepted(){
		try {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_LOCATION_REFRESH_TIME_MILLISECONDS,
					LOCATION_REFRESH_DISTANCE_METERS, mLocationListener);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, NETWORK_LOCATION_REFRESH_TIME_MILLISECONDS,
					LOCATION_REFRESH_DISTANCE_METERS, mLocationListener);
		} catch (SecurityException e) {
			Log.e("Location", "Permission for GPS location updates denied", e);
		}
	}
	private void doOnPermissionDenied(){

	}

    public boolean checkGPSPermission() {
		String permission = Manifest.permission.ACCESS_FINE_LOCATION;

        if (ContextCompat
				.checkSelfPermission(
						Config.getInstance().getContext(),
						permission
				) != PackageManager.PERMISSION_GRANTED) {
			//Permission is not granted yet

            ActivityCompat.requestPermissions(CONFIG.getCurrentActivity(),
                        new String[]{permission},
                        REQUEST_GPS_PERMISSION);

            return false;
        } else {
			//Permission is already granted
			doOnPermissionAccepted();
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if(requestCode == REQUEST_GPS_PERMISSION){
			// If request is cancelled, the result arrays are empty.
			hasGPSPermission = grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED;
			if(hasGPSPermission){
				doOnPermissionAccepted();
			} else {
				doOnPermissionDenied();
			}
		}


    }

    private void requestLocationUpdates() {
		checkGPSPermission();


		//Permission is being requested by checkGPSPermission()
    }

    public void start() {
        //hasNetworkPermission = checkGPSPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        //hasGPSPermission = checkGPSPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        requestLocationUpdates();
    }

    public void cancel() {
        try {
            locationManager.removeUpdates(mLocationListener);
        } catch (SecurityException e) {

        }
    }


	@Override
	public void onGpsStatusChanged(int i) {
		lastLocationFixes.clear();

	}


	private class cLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(final Location location) {
            filterBadLocations(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {
			//Get state before provider was enabled
            final boolean enabled = isLocationEnabled();

            if (s.equals(LocationManager.GPS_PROVIDER)) {
                canGetGPSLocation = false;
            } else if (s.equals(LocationManager.NETWORK_PROVIDER)) {
                canGetNetworkLocation = false;
            }

            if (!enabled) {
				//If all providers were disabled before, call onLocationEnabled
                onLocationEnabled();
            }
        }

        @Override
        public void onProviderDisabled(String s) {
            if (s.equals(LocationManager.GPS_PROVIDER)) {
                canGetGPSLocation = true;
            } else if (s.equals(LocationManager.NETWORK_PROVIDER)) {
                canGetNetworkLocation = true;
            }

            if (!isLocationEnabled()) {
				//If all providers are disabled now, call onLocationDisabled
                onLocationDisabled();
            }
        }

		public void filterBadLocations(final Location location) {
			if (lastKnownLocation != null) {
				final long timeSinceLastUpdate = (System.currentTimeMillis() - lastKnownLocation.getTime());
				if (location.getAccuracy() < 15.0f) {
					Log.i("Location", location.getProvider() + " Location accepted. Accuracy < 15m.");
				} else if (location.getAccuracy() < lastKnownLocation.getAccuracy() + 2.0f) {
					Log.i("Location", location.getProvider() + " Location accepted. New accuracy better than old one or same.");
				} else if (timeSinceLastUpdate > GPS_LOCATION_REFRESH_TIME_MILLISECONDS - 2l) {
					Log.i("Location", location.getProvider() + " Location accepted. Timeout reached.");
				} else {
					Log.i("Location", location.getProvider() + " Location discarded. Accuracy: " + location.getAccuracy() + "; timeSinceLast: " + timeSinceLastUpdate / 1000 + "s");
					return;
				}
			} else {
				Log.i("Location", "Location initialized.");
			}

			lastKnownLocation = location;

			if (location.hasAltitude()) {
				lastKnownAltitude = location.getAltitude();
			} else {
				lastKnownLocation.setAltitude(lastKnownAltitude);
			}

			AndroidLocationManager.this.onLocationChanged(lastKnownLocation);
		}
    }
}
