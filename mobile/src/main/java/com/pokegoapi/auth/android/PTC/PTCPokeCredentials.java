package com.pokegoapi.auth.android.PTC;

import com.givepogosomelove.pokemongoinofficial.backend.PokemonGoManager;
import com.givepogosomelove.pokemongoinofficial.backend.Util.AndroidDeviceManager;
import com.givepogosomelove.pokemongoinofficial.backend.Util.Config;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.device.DeviceInfo;
import com.pokegoapi.api.device.DeviceInfos;
import com.pokegoapi.api.device.LocationFixes;
import com.pokegoapi.api.device.SensorInfo;
import com.pokegoapi.api.device.SensorInfos;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import com.pokegoapi.auth.android.AndroidCompatPre19;
import com.pokegoapi.auth.android.GoogleWebView.GooglePokeCredentials;
import com.pokegoapi.auth.android.IPokeCredentials;
import com.pokegoapi.auth.android.PokeCredentials;
import com.pokegoapi.auth.android.PokeLogin;

import POGOProtos.Networking.Envelopes.SignatureOuterClass;
import okhttp3.OkHttpClient;

public abstract class PTCPokeCredentials extends PokeCredentials implements IPokeCredentials {
	private final static int HASH_BASE = 12;
	private final String username;
	private final String password;

	public PTCPokeCredentials(long seed, String username, String password) {
		super(seed);

		this.username = username;
		this.password = password;
	}

	public PTCPokeCredentials(long seed, String username, String password,
							  int loginMaxTries, long loginDelayBetweenTriesMs) {
		super(seed, loginMaxTries, loginDelayBetweenTriesMs);

		this.username = username;
		this.password = password;
	}


	@Override
	public PokemonGo createPokemonGoInstance() throws Throwable {
		final OkHttpClient httpClient = new OkHttpClient();

		return doLogin(() -> {
			Config config = Config.getInstance();
			PokemonGo go = new PokemonGo(config.getOkHttpClient(), config.getTime(), config.getSeed());
			PokemonGoManager.getInstance().manage(go);
			go.login(new PtcCredentialProvider(config.getOkHttpClient(), username, password));

			return go;
		});
	}


	// ----------- Boilerplate --------------------------------------------------------


	@Override
	public int hashCode() {
		return AndroidCompatPre19.hash(
				HASH_BASE,
				super.hashCode(),
				this.username,
				this.password
		);
	}

	@Override
	public boolean equals(Object obj) {

		if (!super.equals(obj)
				|| !(obj instanceof PTCPokeCredentials)) {
			return false;
		}

		PTCPokeCredentials cred = (PTCPokeCredentials) obj;

		return AndroidCompatPre19.equals(username, cred.username)
				&& AndroidCompatPre19.equals(password, cred.password);
	}

	@Override
	public abstract void onSuccess(PokemonGo pokemonGo);

	@Override
	public abstract void onError(Throwable e);

}
