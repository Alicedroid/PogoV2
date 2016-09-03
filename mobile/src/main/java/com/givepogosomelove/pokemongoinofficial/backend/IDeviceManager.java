package com.givepogosomelove.pokemongoinofficial.backend;

import com.pokegoapi.api.PokemonGo;

/**
 * Created by Angelo on 02.09.2016.
 */
public interface IDeviceManager {
	void manage(PokemonGo pokemonGo);

	void init();
}
