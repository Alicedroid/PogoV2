package com.givepogosomelove.pokemongoinofficial.backend;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.givepogosomelove.pokemongoinofficial.backend.Exception.NotInitializedException;
import com.givepogosomelove.pokemongoinofficial.backend.Util.AndroidDeviceManager;
import com.givepogosomelove.pokemongoinofficial.backend.Util.AndroidLocationManager;
import com.givepogosomelove.pokemongoinofficial.backend.Util.Config;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.android.IPokeCredentials;
import com.pokegoapi.auth.android.PokeLogin;

/**
 * Singleton PokemonGoManager
 */
public class PokemonGoManager {

	private static final IConfig config = Config.getInstance();
	private static final IDeviceManager deviceManager = AndroidDeviceManager.getInstance();
	private static final ISensorManager sensorManager = AndroidSensorManager.getInstance();
	private static final ILocationManager locationManager = AndroidLocationManager.getInstance();
	private static boolean initialized = false;

	/**
	 * Creates a PokemonGo instance using the given credentials.
	 * {@link(#init(Activity)} must be called before calling this.
	 * @param credentials Valid credentials
	 * @throws NotInitializedException
	 */
	public void login(@NonNull IPokeCredentials credentials) throws NotInitializedException {
		if(!initialized){
			throw new NotInitializedException(this);
		}

		PokeLogin.login(new IPokeCredentials() {
			@Override
			public PokemonGo createPokemonGoInstance() throws Throwable {
				return credentials.createPokemonGoInstance();
			}

			@Override
			public void onSuccess(PokemonGo pokemonGo) {
				deviceManager.manage(pokemonGo);
				sensorManager.listen(pokemonGo);
				locationManager.listen(pokemonGo);
				credentials.onSuccess(pokemonGo);
			}

			@Override
			public void onError(Throwable e) {
				credentials.onError(e);
			}
		});
	}

	/**
	 * Updates the manager to use the new activity. Call this with null value when closing an activity.
	 * @param currentActivity The current activity. Null if no active activity.
	 */
	public void updateActivity(@Nullable Activity currentActivity){
		if(!initialized){
			init(currentActivity);
		} else {
			config.updateCurrentActivity(currentActivity);
		}
	}



	//region singleton

	/**
	 * Initializes all dependencies
	 * @param activity
	 */
	public void init(@NonNull Activity activity){
		config.init(activity); // Needs to be called first
		deviceManager.init();
		sensorManager.init();
		locationManager.init();
		initialized = true;
	}

	private static PokemonGoManager instance = null;

	private PokemonGoManager(){}

	@NonNull
	public static PokemonGoManager getInstance(){
		if(instance == null){
			instance = new PokemonGoManager();
		}
		return instance;
	}

	//endregion


}
