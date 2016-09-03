package com.givepogosomelove.pokemongoinofficial.backend.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.givepogosomelove.pokemongoinofficial.backend.Exception.NotInitializedException;
import com.givepogosomelove.pokemongoinofficial.backend.IConfig;
import com.pokegoapi.auth.android.PokeLogin;

import java.util.Random;

import okhttp3.OkHttpClient;

/**
 * Created by Angelo on 27.08.2016.
 */
public class Config implements IConfig {
	private static final String SP_FILENAME = "data";
	private static final String SP_GOOGLE_REFRESH_TOKEN = "google_refresh_token";
	private static final String SP_SEED = "seed";
	private static Config ourInstance = new Config();
	private static Context context;
	private static Long seed;
	private static SharedPreferences sharedPreferences;
	private static boolean initialized = false;
	private static OkHttpClient okHttpClient = new OkHttpClient();
	private static PokeLogin.PokeTime time = new PokeLogin.PokeTime();
	private static Activity currentActivity;
	public static final long startTime = System.currentTimeMillis();

	private Config() {
	}

	public static Config getInstance() {
		return Config.ourInstance;
	}

	public Context getContext() {
		return context;
	}


	public boolean isInitialized() {
		return Config.initialized;
	}

	public SharedPreferences getSharedPreferences() throws NotInitializedException {
		if (!Config.initialized) {
			throw new NotInitializedException(this);
		}
		return Config.sharedPreferences;
	}

	/**
	 * @return A 64bit Long seed for Pokemon Go
	 */
	public long getSeed() {
		return Config.seed;
	}

	public void genSeed(){
		//Load saved seed. If none exists, create a new random seed and save it.
		Config.seed = Config.sharedPreferences.getLong(SP_SEED, 0L);
		if (Config.seed == 0L) {
			Config.seed = new Random().nextLong();
			Config.sharedPreferences
					.edit()
					.putLong(SP_SEED, seed)
					.commit();
		}
	}

	/**
	 * Initializes this singleton
	 *
	 * @param activity The current activity
	 */
	@SuppressLint("CommitPrefEdits")
	public void init(Activity activity) {
		Config.initialized = true;
		Config.context = activity.getApplicationContext();
		Config.currentActivity = activity;
		Config.sharedPreferences = Config.context.getSharedPreferences(Config.SP_FILENAME, Context.MODE_PRIVATE);
		genSeed();

		initDependencies();
	}


	/**
	 * Initializes Dependencies, once the context has been set.
	 */
	private void initDependencies() {
		AndroidDeviceManager.getInstance().init();
	}

	public PokeLogin.PokeTime getTime() {
		return Config.time;
	}

	public OkHttpClient getOkHttpClient() {
		return Config.okHttpClient;
	}

	public Activity getCurrentActivity() {
		return Config.currentActivity;
	}

	public void updateCurrentActivity(Activity activity) {
		Config.currentActivity = activity;
	}
}

