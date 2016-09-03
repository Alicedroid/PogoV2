package com.pokegoapi.auth.android;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.exceptions.AsyncPokemonGoException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

/**
 * Interface for credentials. You can either create your own implementation of IPokeCredentials
 * or just use PTCPokeCredentials and GooglePokeCredentials (recommended).
 */
public interface IPokeCredentials {
	/**
	 * @return An instance of PokemonGo.
	 * @throws LoginFailedException
	 * @throws RemoteServerException
	 * @throws AsyncPokemonGoException
	 */
	PokemonGo createPokemonGoInstance() throws Throwable;

	/**
	 * Will be called if the login process succeeds.
	 *
	 * @param pokemonGo Instance of PokemonGo
	 */
	void onSuccess(PokemonGo pokemonGo);

	/**
	 * Will be called if the login process fails.
	 *
	 * @param e Throwable which caused the fail
	 */
	void onError(Throwable e);
}
