package com.givepogosomelove.pokemongoinofficial.backend.Util;

import android.location.Location;

import com.givepogosomelove.pokemongoinofficial.backend.IDeviceManager;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.device.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all device stuff
 */
public class AndroidDeviceManager implements IDeviceManager {
	private static final AndroidDeviceManager instance = new AndroidDeviceManager();// This singleton
	private AndroidDeviceManager() {
	}
	public static AndroidDeviceManager getInstance() {
		return instance;
	}

	@Override
	public void manage(PokemonGo pokemonGo) {
		pokemonGo.setDeviceInfo(new DeviceInfo(new AndroidDeviceInfos()));
	}

	@Override
	public void init() {

	}

}
