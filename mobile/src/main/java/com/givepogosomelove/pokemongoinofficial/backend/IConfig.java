package com.givepogosomelove.pokemongoinofficial.backend;

import android.app.Activity;
import android.content.Context;

import java.util.concurrent.TimeUnit;

/**
 * Created by Angelo on 02.09.2016.
 */
public interface IConfig {
	void init(Activity activity);

	void updateCurrentActivity(Activity currentActivity);

	long getSensorUpdateInterval();

	Context getContext();

	Activity getCurrentActivity();

	long getLocationUpdateInterval();
}
