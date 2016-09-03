package com.givepogosomelove.pokemongoinofficial.backend;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import com.givepogosomelove.pokemongoinofficial.backend.Util.Config;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.device.SensorInfo;
import com.pokegoapi.api.device.SensorInfos;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;

/**
 * Created by Angelo on 02.09.2016.
 */
public class AndroidSensorManager implements ISensorManager, SensorInfos, SensorEventListener {
	private static final AndroidSensorManager instance = new AndroidSensorManager();
	private static final IConfig CONFIG = Config.getInstance();

	//List of PokemonGo to notify when sensor infos changed
	private ArrayList<PokemonGo> pokemonGoList = new ArrayList<>();

	//True if already listening to sensors, else False.
	private boolean listening = false;

	//Sensors
	private SensorManager senSensorManager;
	private Sensor senAccelerometer;
	private Sensor senGyroscope;
	private Sensor senMagnetometer;
	private Sensor senNormalizedAccelerometer; //Linear acceleration in Android terms

	//Cached sensor data
	private double accelRawX, accelRawY, accelRawZ;
	private double accelNormalizedX, accelNormalizedY, accelNormalizedZ;
	private double angleNormalizedX, angleNormalizedY, angleNormalizedZ;
	private double gyroRawX, gyroRawY, gyroRawZ;

	//Used for calculation of normalized angle
	private float[] mGravity;
	private float[] mGeomagnetic;

	private AndroidSensorManager() {

	}

	public static AndroidSensorManager getInstance() {
		return instance;
	}

	/**
	 * Initializes all sensors.
	 */
	@Override
	public void init() {
		senSensorManager = (SensorManager) Config.getInstance().getContext().getSystemService(Context.SENSOR_SERVICE);
		senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		senMagnetometer = senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		senNormalizedAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
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

	private Subscription pokemonGoNotifier;

	//---------------------------------------------------------------------

	private synchronized void startListening() {
		if (listening) {
			return;
		}

		senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		senSensorManager.registerListener(this, senGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
		senSensorManager.registerListener(this, senMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
		senSensorManager.registerListener(this, senNormalizedAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

		pokemonGoNotifier = Observable
				.interval(0L, CONFIG.getSensorUpdateInterval(), TimeUnit.MILLISECONDS, new Scheduler() {
					@Override
					public Worker createWorker() {
						PokemonGo[] pokemonGos = (PokemonGo[]) pokemonGoList.toArray();
						for (PokemonGo go:
								pokemonGos) {
							go.setSensorInfo(new SensorInfo(AndroidSensorManager.this));
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

		senSensorManager.unregisterListener(this, senAccelerometer);
		senSensorManager.unregisterListener(this, senGyroscope);
		senSensorManager.unregisterListener(this, senMagnetometer);
		senSensorManager.unregisterListener(this, senNormalizedAccelerometer);

		listening = false;
	}

	//------------------------------------------------------
	@Override
	public void onSensorChanged(@NonNull SensorEvent sensorEvent) {
		final int sensorType = sensorEvent.sensor.getType();
		switch (sensorType) {
			case Sensor.TYPE_ACCELEROMETER:
				//Raw Accelerometer
				accelRawX = sensorEvent.values[0];
				accelRawY = sensorEvent.values[1];
				accelRawZ = sensorEvent.values[2];
				mGravity = sensorEvent.values;
				updateAngle();
				break;
			case Sensor.TYPE_GYROSCOPE:
				//Raw Gyroscope
				gyroRawX = sensorEvent.values[0];
				gyroRawY = sensorEvent.values[1];
				gyroRawZ = sensorEvent.values[2];
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				mGeomagnetic = sensorEvent.values;
				updateAngle();
				break;
			case Sensor.TYPE_LINEAR_ACCELERATION:
				//Normalized Acceleration
				accelNormalizedX = sensorEvent.values[0];
				accelNormalizedY = sensorEvent.values[1];
				accelNormalizedZ = sensorEvent.values[2];
				break;
		}


	}

	private void updateAngle() {
		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				angleNormalizedX = orientation[0]; // azimut
				angleNormalizedY = orientation[1]; // pitch
				angleNormalizedZ = orientation[2]; // roll
			}
		}
	}

	@Override
	public void onAccuracyChanged(@NonNull Sensor sensor, int i) {
	}

	//--------------------------------------------------------

	/**
	 * @return timestamp snapshot in ms since start
	 */
	@Override
	public long getTimestampSnapshot() {
		return System.currentTimeMillis() - Config.startTime;
	}

	/**
	 * @return accelerometer axes, always 3
	 */
	@Override
	public long getAccelerometerAxes() {
		return 3;
	}

	/**
	 * @return accel normalized x
	 */
	@Override
	public double getAccelNormalizedX() {
		return accelNormalizedX;
	}

	/**
	 * @return accel normalized y
	 */
	@Override
	public double getAccelNormalizedY() {
		return accelNormalizedY;
	}

	/**
	 * @return accel normalized z
	 */
	@Override
	public double getAccelNormalizedZ() {
		return accelNormalizedZ;
	}

	/**
	 * @return accel raw x
	 */
	@Override
	public double getAccelRawX() {
		return accelRawX;
	}

	/**
	 * @return accel raw y
	 */
	@Override
	public double getAccelRawY() {
		return accelRawY;
	}

	/**
	 * @return accel raw z
	 */
	@Override
	public double getAccelRawZ() {
		return accelRawZ;
	}

	/**
	 * @return angel normalized x
	 */
	@Override
	public double getAngleNormalizedX() {
		return angleNormalizedX; //azimut
	}

	/**
	 * @return angel normalized y
	 */
	@Override
	public double getAngleNormalizedY() {
		return angleNormalizedY; //pitch
	}

	/**
	 * @return angel normalized z
	 */
	@Override
	public double getAngleNormalizedZ() {
		return angleNormalizedZ; //roll
	}

	/**
	 * @return gyroscope raw x
	 */
	@Override
	public double getGyroscopeRawX() {
		return gyroRawX;
	}

	/**
	 * @return gyroscope raw y
	 */
	@Override
	public double getGyroscopeRawY() {
		return gyroRawY;
	}

	/**
	 * @return gyroscope raw z
	 */
	@Override
	public double getGyroscopeRawZ() {
		return gyroRawZ;
	}
}
