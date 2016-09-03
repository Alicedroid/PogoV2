package com.pokegoapi.auth.android.GoogleWebView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.util.Time;

import com.pokegoapi.auth.android.AndroidCompatPre19;
import com.pokegoapi.auth.android.IPokeCredentials;
import com.pokegoapi.auth.android.PokeCredentials;
import com.pokegoapi.auth.android.PokeLogin;
import okhttp3.OkHttpClient;
import android.provider.Settings.Secure;

/**
 * Describes credentials for Google login
 */
public abstract class GooglePokeCredentials extends PokeCredentials implements IPokeCredentials {
	private final static int HASH_BASE = 17;
	private final static boolean autoAccept = false;
	private final OkHttpClient httpClient = new OkHttpClient();
	private final Time time = new PokeLogin.PokeTime();
	private String sharedPreferencesFile = "storage";
	private String sharedPreferencesRefreshTokenName = "google_refresh_token";
	private Context context;
	private SharedPreferences sharedPreferences;
	private String accessToken = null;
	private String refreshToken;
	private Dialog dialog;
	private boolean finished = false;


	/**
	 * Logs you into Google with a WebView,
	 * using a maximum of 10 tries with a delay of 1 second between each try.
	 * It will not save the token received by Google by default,
	 * see GooglePokeCredentials(Context, String, String).
	 *
	 * @param context The @link(Activity) where you use this GoogleLoginTask.
	 */
	public GooglePokeCredentials(Context context, long seed) {
		super(seed);
		init(context);
	}


	/**
	 * Logs you into Google with a WebView,
	 * using a maximum of 10 tries with a delay of 1 second between each try.
	 * It will load the token from the specified file and variable, and save new tokens there.
	 *
	 * @param context                   The @link(Activity) where you use this GoogleLoginTask.
	 * @param sharedPreferencesFileName Name of the file you want the token to load from and save to.
	 * @param tokenVariableName         Name of variable in the file you want the token to load from and save to.
	 */
	public GooglePokeCredentials(Context context, long seed, String sharedPreferencesFileName, String tokenVariableName) {
		super(seed);
		init(context, sharedPreferencesFileName, tokenVariableName);
	}

	/**
	 * Logs you into Google with a @link(WebView).
	 *
	 * @param context  The Activity where you use this GoogleLoginTask.
	 * @param maxTries How many times we should try to log into the game,
	 *                 in case that the servers are busy. This is ignored
	 *                 if there's another problem:
	 *                 - The WebView was closed
	 *                 - The User's account is not yet verified
	 *                 - The User's account is banned
	 *                 - The User's credentials are wrong
	 *                 - or any other LoginFailedException
	 *                 In those cases the Login cycle will be broken.
	 */
	public GooglePokeCredentials(Context context, long seed, int maxTries) {
		super(seed, maxTries);
		init(context);
	}

	/**
	 * Logs you into Google with a @link(WebView).
	 *
	 * @param context                   The Activity where you use this GoogleLoginTask.
	 * @param sharedPreferencesFileName Name of the file you want the token to load from and save to.
	 * @param tokenVariableName         Name of variable in the file you want the token to load from and save to.
	 * @param maxTries                  How many times we should try to log into the game,
	 *                                  in case that the servers are busy. This is ignored
	 *                                  if there's another problem:
	 *                                  - The WebView was closed
	 *                                  - The User's account is not yet verified
	 *                                  - The User's account is banned
	 *                                  - The User's credentials are wrong
	 *                                  - or any other LoginFailedException
	 *                                  In those cases the Login cycle will be broken.
	 */
	public GooglePokeCredentials(Context context, long seed, String sharedPreferencesFileName, String tokenVariableName, int maxTries) {
		super(seed, maxTries);
		init(context, sharedPreferencesFileName, tokenVariableName);
	}

	/**
	 * Logs you into Google with a @link(WebView).
	 *
	 * @param context             The Activity where you use this GoogleLoginTask.
	 * @param maxTries            How many times we should try to log into the game,
	 *                            in case that the servers are busy. This is ignored
	 *                            if there's another problem:
	 *                            - The WebView was closed
	 *                            - The User's account is not yet verified
	 *                            - The User's account is banned
	 *                            - The User's credentials are wrong
	 *                            - or any other LoginFailedException
	 *                            In those cases the Login cycle will be broken.
	 * @param delayBetweenTriesMs How much time we should let pass between each try.
	 */
	public GooglePokeCredentials(Context context, long seed, int maxTries, long delayBetweenTriesMs) {
		super(seed, maxTries, delayBetweenTriesMs);
		init(context);
	}

	/**
	 * Logs you into Google with a @link(WebView).
	 *
	 * @param context                   The Activity where you use this GoogleLoginTask.
	 * @param sharedPreferencesFileName Name of the file you want the token to load from and save to.
	 * @param tokenVariableName         Name of variable in the file you want the token to load from and save to.
	 * @param maxTries                  How many times we should try to log into the game,
	 *                                  in case that the servers are busy. This is ignored
	 *                                  if there's another problem:
	 *                                  - The WebView was closed
	 *                                  - The User's account is not yet verified
	 *                                  - The User's account is banned
	 *                                  - The User's credentials are wrong
	 *                                  - or any other LoginFailedException
	 *                                  In those cases the Login cycle will be broken.
	 * @param delayBetweenTriesMs       How much time we should let pass between each try.
	 */
	public GooglePokeCredentials(Context context, long seed, String sharedPreferencesFileName, String tokenVariableName, int maxTries, long delayBetweenTriesMs) {
		super(seed, maxTries, delayBetweenTriesMs);
		init(context, sharedPreferencesFileName, tokenVariableName);
	}


	@Override
	public PokemonGo createPokemonGoInstance() throws Throwable {
		return doLogin(() -> {
			finished = false;


			refreshToken = GooglePokeCredentials.this.loadRefreshTokenFromDevice();

			// Check if we should request a new token
			if (refreshToken == null) {
				Throwable e = null;
				accessToken = null;


				GooglePokeCredentials.this.showWebView(); //Opens a WebView for the user to login
				while (!finished) {
					Thread.sleep(500); //May throw InterruptedException
				}

				//Check if we have received an access token
				if (accessToken != null) {
					GoogleUserCredentialProvider tokenProvider = new GoogleUserCredentialProvider(httpClient, time);
					//Request a refresh token
					tokenProvider.login(accessToken);
					//store refresh token
					GooglePokeCredentials.this.saveRefreshTokenOnDevice(tokenProvider.getRefreshToken());
				} else {
					throw new AccessTokenNotReceivedException();
				}

				if (refreshToken == null) {
					throw new RefreshTokenNotReceivedException();
				}
			}

			GoogleUserCredentialProvider googleProvider = new GoogleUserCredentialProvider(httpClient, refreshToken, time);

			PokemonGo go = new PokemonGo(httpClient, time, getSeed());
			go.login(googleProvider);
			return go;
		});

	}


	// Boilerplate ----------------------------------------------------------------------

	private void init(Context context) {
		init(context, null, null);
	}

	private void init(Context context, String sharedPreferencesFile, String sharedPreferencesRefreshTokenName) {
		this.context = context;
		this.sharedPreferencesFile = sharedPreferencesFile;
		this.sharedPreferencesRefreshTokenName = sharedPreferencesRefreshTokenName;

		if (sharedPreferencesFile != null) {
			this.sharedPreferences = context.getSharedPreferences(sharedPreferencesFile, Context.MODE_PRIVATE);
		}
	}

	private boolean isSaveTokenEnabled() {
		return sharedPreferences != null && sharedPreferencesRefreshTokenName != null;
	}

	@SuppressLint("CommitPrefEdits")
	private void saveRefreshTokenOnDevice(String refreshToken) {
		this.refreshToken = refreshToken;
		if (isSaveTokenEnabled()) {
			if (refreshToken != null) {
				sharedPreferences
						.edit()
						.putString(sharedPreferencesRefreshTokenName, refreshToken)
						.commit();
			} else {
				sharedPreferences
						.edit()
						.remove(sharedPreferencesRefreshTokenName)
						.commit();
			}
		}
	}

	private String loadRefreshTokenFromDevice() {
		if (isSaveTokenEnabled()) {
			return sharedPreferences.getString(sharedPreferencesRefreshTokenName, null);
		}

		return null;
	}


	private void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
		finished = true;
	}

	private void setAccessDenied() {
		setAccessToken(null);
	}

	private void showWebView() {
		Handler uiHandler = new Handler(Looper.getMainLooper());
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				Context context1 = context;
				final AlertDialog.Builder alert = new AlertDialog.Builder(context1);

				final WebView wv = new WebView(context1) {
					@Override
					public boolean onCheckIsTextEditor() {
						return true;
					}
				};
				wv.getSettings().setJavaScriptEnabled(true);

				wv.addJavascriptInterface(new GoogleInteractionHandler(wv, autoAccept) {

					@Override
					public void onPermissionAccepted(String html, String accessToken) {
						setAccessToken(accessToken);
						dialog.dismiss();
					}

					@Override
					public void onPermissionDenied(String html) {
						setAccessDenied();
						dialog.dismiss();
					}

					@Override
					public void onUnexpectedResult(String html) {
						setAccessDenied();
						dialog.dismiss();
					}
				}, "HTMLOUT");

				wv.requestFocus(View.FOCUS_DOWN);
				wv.setOnTouchListener((v, event) -> {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
						case MotionEvent.ACTION_UP:
							if (!v.hasFocus()) {
								v.requestFocus();
							}
							break;
					}
					return false;
				});

				alert.setView(wv);

				alert.setNegativeButton("Cancel", (dialog1, id) -> {
					finished = true;
					dialog1.dismiss();
				});

				dialog = alert.create();

				dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
				dialog.show();
				wv.loadUrl(GoogleUserCredentialProvider.LOGIN_URL);
			}
		});
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)
				|| !(obj instanceof GooglePokeCredentials)) {
			return false;
		}
		GooglePokeCredentials cred = (GooglePokeCredentials) obj;

		return AndroidCompatPre19.equals(context, cred.context)
				&& AndroidCompatPre19.equals(sharedPreferencesRefreshTokenName, cred.sharedPreferencesRefreshTokenName)
				&& AndroidCompatPre19.equals(sharedPreferencesFile, cred.sharedPreferencesFile);
	}

	@Override
	public int hashCode() {
		return AndroidCompatPre19.hash(
				HASH_BASE,
				super.hashCode(),
				context,
				sharedPreferencesRefreshTokenName,
				sharedPreferencesFile);
	}



	public class RefreshTokenNotReceivedException extends Exception {
	}

	public class AccessTokenNotReceivedException extends Exception {
	}
}
