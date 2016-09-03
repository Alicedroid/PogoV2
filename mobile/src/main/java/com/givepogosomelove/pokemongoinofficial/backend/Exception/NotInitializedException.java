package com.givepogosomelove.pokemongoinofficial.backend.Exception;

/**
 * Created by Angelo on 28.08.2016.
 */
public class NotInitializedException extends RuntimeException {
	String message;
	public NotInitializedException(Object object){
		message = object.getClass().getCanonicalName() + " was not initialized.";
	}

	@Override
	public String getMessage() {
		return message;
	}
}
