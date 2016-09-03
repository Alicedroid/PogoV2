package com.givepogosomelove.pokemongoinofficial.backend;

import android.support.annotation.NonNull;

import com.pokegoapi.api.PokemonGo;

/**
 * Created by Angelo on 02.09.2016.
 */
public interface ISensorManager {
	void listen(PokemonGo pokemonGo);

	void init();

	void stop(@NonNull PokemonGo pokemonGo);

	void stopAll();
}
