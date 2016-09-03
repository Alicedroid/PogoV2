package com.pokegoapi.auth.android;

import android.util.Log;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.Locale;

import com.pokegoapi.auth.android.GoogleWebView.GooglePokeCredentials;


public abstract class PokeCredentials implements IPokeCredentials {
	private final static int HASH_BASE = 5;
	private final int loginMaxTries;
	private final long loginDelayBetweenTriesMs;
	private final long seed;

	/**
	 * Logs into PokemonGO using default amount of tries and default amount of delay between tries.
	 */
	protected PokeCredentials(long seed) {
		this(seed, 10);
	}

	/**
	 * Logs into PokemonGO using given amount of tries and default amount of delay between tries.
	 *
	 * @param loginMaxTries
	 */
	protected PokeCredentials(long seed, int loginMaxTries) {
		this(seed, loginMaxTries, 1000);
	}

	/**
	 * Logs into PokemonGO using given amount of tries and given amount of delay between tries.
	 *
	 * @param loginMaxTries
	 * @param loginDelayBetweenTriesMs
	 */
	protected PokeCredentials(long seed, int loginMaxTries, long loginDelayBetweenTriesMs) {
		this.loginMaxTries = loginMaxTries;
		this.loginDelayBetweenTriesMs = loginDelayBetweenTriesMs;
		this.seed = seed;
	}

	protected PokemonGo doLogin(IPokeLoginMethod iPokeLoginMethod) throws Throwable {
		RemoteServerException remoteServerException = null;

		for (int i = 0; i < loginMaxTries; i++) {
			try {
				return iPokeLoginMethod.login();

			} catch (RemoteServerException exception) {
				// Server busy (or ban?)
				Log.e("Login", String.format("Try %d/%d", 1 + i, loginMaxTries), exception);

				if (i < loginMaxTries - 1) {
					Thread.sleep(loginDelayBetweenTriesMs);
				} else {
					Log.i("Login", "Server probably busy. MAX_TRIES_SIGN_IN reached.");
					remoteServerException = exception;
				}
			}
		}

		throw new MaxTriesReachedException(
				String.format(
						Locale.ENGLISH, "Tried and failed %d times.", loginMaxTries
				),
				remoteServerException);

	}


	public long getSeed() {
		return seed;
	}

	@Override
	public int hashCode() {
		return AndroidCompatPre19.hash(
				HASH_BASE,
				loginMaxTries,
				loginDelayBetweenTriesMs
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null
				|| !(obj instanceof PokeCredentials)) {
			return false;
		}

		PokeCredentials cred = (PokeCredentials) obj;

		return AndroidCompatPre19.equals(loginDelayBetweenTriesMs, cred.loginDelayBetweenTriesMs)
				&& AndroidCompatPre19.equals(loginMaxTries, cred.loginMaxTries);

	}

	public interface IPokeLoginMethod {
		PokemonGo login() throws LoginFailedException, RemoteServerException, InterruptedException, MaxTriesReachedException, GooglePokeCredentials.RefreshTokenNotReceivedException, GooglePokeCredentials.AccessTokenNotReceivedException;
	}

	public class MaxTriesReachedException extends Exception {
		public MaxTriesReachedException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
