package com.pokegoapi.auth.android;

import android.util.Log;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.util.Time;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Contains methods that give you
 */
public class PokeLogin {
	private final static HashMap<IPokeCredentials, PokemonGo> pokemonGoMap = new HashMap<>();

	private static Observable<PokemonGo> getPokemonGoObservable(final IPokeCredentials pokeCredentials) {
		return Observable
				.just(pokeCredentials)
				.map(pokeCredentials1 -> {
					try {
						PokemonGo go = pokeCredentials.createPokemonGoInstance();
						pokemonGoMap.put(pokeCredentials, go);
						return go;
					} catch (Throwable e) {
						throw Exceptions.propagate(e);
					}
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());
	}

	private static void subscribePokemonGoObservable(final IPokeCredentials pokeCredentials) {
		getPokemonGoObservable(pokeCredentials)
				.subscribe(new Observer<PokemonGo>() {
					@Override
					public void onCompleted() {
					}

					@Override
					public void onError(Throwable e) {
						pokeCredentials.onError(e);
					}

					@Override
					public void onNext(PokemonGo pokemonGo) {
						pokeCredentials.onSuccess(pokemonGo);
					}
				});
	}

	/**
	 * Asynchonously creates a new PokemonGo instance and caches it for reuse, or picks up an already cached one.
	 *
	 * @param pokeCredentials Some login credentials
	 */
	public static void login(IPokeCredentials pokeCredentials) {
		PokemonGo go;
		if (pokemonGoMap.containsKey(pokeCredentials)) {
			if ((go = pokemonGoMap.get(pokeCredentials)) != null) {
				pokeCredentials.onSuccess(go);
				return;
			} else {
				pokemonGoMap.remove(pokeCredentials);
			}
		}
		subscribePokemonGoObservable(pokeCredentials);
	}

	/**
	 * Repeats the given method in case that the API throws any Exception except for LoginFailedException.
	 *
	 * @param onRepeat Will be repeated
	 * @param times    how often should it repeat
	 * @param delayMs  how long should it wait after fail
	 */
	public static void repeatAsyncWhenServerBusy(int times, int delayMs, Func onRepeat) {
		repeatAsyncWhenServerBusy(times, delayMs, onRepeat, e -> {
		}, () -> {
		});
	}

	/**
	 * Repeats the given method in case that the API throws any Exception except for LoginFailedException.
	 *
	 * @param onRepeat   Will be repeated
	 * @param onComplete Will be called after either onRepeat or onError or both have been successfully called
	 * @param times      how often should it repeat
	 * @param delayMs    how long should it wait after fail
	 */
	public static void repeatAsyncWhenServerBusy(int times, int delayMs, Func onRepeat, Func onComplete) {
		repeatAsyncWhenServerBusy(times, delayMs, onRepeat, e -> {
		}, onComplete);
	}

	/**
	 * Repeats the given method in case that the API throws any Exception except for LoginFailedException.
	 *
	 * @param onRepeat Will be repeated
	 * @param onError  Will be called on error after which onRepeat will not be called anymore
	 * @param times    how often should it repeat
	 * @param delayMs  how long should it wait after fail
	 */
	public static void repeatAsyncWhenServerBusy(int times, int delayMs, Func onRepeat, ThrowableFunc onError) {
		repeatAsyncWhenServerBusy(times, delayMs, onRepeat, onError, () -> {
		});
	}

	/**
	 * Repeats the given method in case that the API throws any Exception except for LoginFailedException.
	 *
	 * @param onRepeat   Will be repeated
	 * @param onError    Will be called on error after which onRepeat will not be called anymore
	 * @param onComplete Will be called after either onRepeat or onError or both have been successfully called
	 * @param times      how often should it repeat
	 * @param delayMs    how long should it wait after fail
	 */
	public static void repeatAsyncWhenServerBusy(int times, int delayMs, final Func onRepeat, final ThrowableFunc onError, final Func onComplete) {
		Observable
				.just(null)
				.map(o -> {
					try {
						onRepeat.call();
					} catch (Throwable e) {
						Log.e("PokeLogin", "Exception was thrown in given onRepeat method", e);
						throw Exceptions.propagate(e);
					}
					return null;
				})
				.retryWhen(new RetryWithDelay(times, delayMs, e -> !(e instanceof LoginFailedException)))
				.subscribeOn(AndroidSchedulers.mainThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Observer<Object>() {
					@Override
					public void onCompleted() {
						Log.i("PokeLogin", "Completed request");
						try {
							onComplete.call();
						} catch (Throwable e2) {
							Log.e("PokeLogin", "Exception was thrown in given onComplete method", e2);
						}
					}

					@Override
					public void onError(Throwable e) {
						try {
							onError.call(e);
						} catch (Throwable e2) {
							Log.e("PokeLogin", "Exception was thrown in given onError method", e2);
						}
					}

					@Override
					public void onNext(Object o) {

					}
				});
	}

	private static IPokeCredentials findKey(PokemonGo go) {
		for (Map.Entry<IPokeCredentials, PokemonGo> a : pokemonGoMap.entrySet()) {
			if (AndroidCompatPre19.equals(a.getValue(), go)) {
				return a.getKey();
			}
		}
		return null;
	}

	/**
	 * Removes your cached PokemonGo instance.
	 * Better use logout(IPokeCredentials), because that is faster -> o(1) than this -> o(n)
	 *
	 * @param go PokemonGo instance
	 */
	public static void logout(PokemonGo go) {
		IPokeCredentials removeKey = findKey(go);
		if (removeKey != null) {
			pokemonGoMap.remove(removeKey);
		}
	}

	/**
	 * Removes your cached PokemonGo instance
	 *
	 * @param credentials Credentials you used to login
	 */
	public static void logout(IPokeCredentials credentials) {
		pokemonGoMap.remove(credentials);
	}


	public interface ThrowableFunc {
		void call(Throwable e);
	}

	public interface Func {
		void call() throws Throwable;
	}

	private interface RetryCondition {
		boolean check(Throwable throwable);
	}

	/**
	 * Using PokemonGo constructor without an implementation of Time has been deprecated.
	 */
	public static class PokeTime implements Time {

		@Override
		public long currentTimeMillis() {
			return System.currentTimeMillis();
		}
	}

	private static class RetryWithDelay implements
			Func1<Observable<? extends Throwable>, Observable<?>> {

		private final int maxRetries;
		private final int retryDelayMillis;
		private int retryCount;
		private RetryCondition condition;

		public RetryWithDelay(final int maxRetries, final int retryDelayMillis, final RetryCondition condition) {
			this.maxRetries = maxRetries;
			this.retryDelayMillis = retryDelayMillis;
			this.retryCount = 0;
			this.condition = condition;
		}

		@Override
		public Observable<?> call(Observable<? extends Throwable> attempts) {
			return attempts
					.flatMap(new Func1<Throwable, Observable<?>>() {
						@Override
						public Observable<?> call(Throwable throwable) {
							if (++retryCount < maxRetries && condition.check(throwable)) {
								// When this Observable calls onNext, the original
								// Observable will be retried (i.e. re-subscribed).
								return Observable.timer(retryDelayMillis,
										TimeUnit.MILLISECONDS);
							}

							// Max retries hit. Just pass the error along.
							return Observable.error(throwable);
						}
					});
		}
	}

}
