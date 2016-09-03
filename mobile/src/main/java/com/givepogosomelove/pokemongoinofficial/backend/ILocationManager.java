package com.givepogosomelove.pokemongoinofficial.backend;

import android.support.annotation.NonNull;

import com.pokegoapi.api.PokemonGo;

/**
 * Created by Angelo on 03.09.2016.
 */
public interface ILocationManager {
	void init();

	void listen(PokemonGo pokemonGo);

	void stop(@NonNull PokemonGo pokemonGo);

	void stopAll();
}
