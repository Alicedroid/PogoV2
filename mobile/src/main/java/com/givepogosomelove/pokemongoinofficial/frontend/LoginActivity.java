package com.givepogosomelove.pokemongoinofficial.frontend;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import com.givepogosomelove.pokemongoinofficial.R;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Stats;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.auth.android.GoogleWebView.GooglePokeCredentials;
import com.pokegoapi.auth.android.PTC.PTCPokeCredentials;
import com.pokegoapi.auth.android.PokeLogin;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

	private static final String SAMPLE_PTC_USERNAME = "CodyGinns6299";
	private static final String SAMPLE_PTC_PASSWORD = "PCodyGinns6299";

	// Define some place to load the refresh token from or to save a new one
	private static final String sharedPreferencesFile = "token_storage";
	private static final String sharedPreferencesRefreshTokenName = "google_refresh_token";

	// UI references.
	private View mLoginForm;
	private EditText mUsernameView;
	private EditText mPasswordView;
	private ProgressBar mProgressBar;

	/**
	 * Will be called if the login process succeeds.
	 * If this method causes an exception, be prepared, that onError still gets called, even if the
	 * PokemonGo instance has already been created.
	 *
	 * @param go Instance of PokemonGo
	 */
	private void onLoginSuccess(final PokemonGo go) {
		Log.i("Login", "Logged in successfully!");
		final PlayerProfile profile = go.getPlayerProfile();
		Toast.makeText(
				this,
				"Yay! Hello, " + profile.getPlayerData().getUsername(),
				Toast.LENGTH_LONG
		).show();

		// If server busy, repeat the following for a maximum of 3 tries with a delay of 1500 ms between each try
		// When I tested this, even 1000ms delay was too short
		// APICall --> wait 1.5s --> APICall --> wait 1.5s --> APICall => max 3s
		PokeLogin.repeatAsyncWhenServerBusy(
				3,
				1500,
				() -> {
					// PlayerProfile#getStats() may throw a RemoteServerException or AsyncPokemonGoException
					// if servers are busy, or if you're banned
					Stats stats = profile.getStats();
					int level = stats.getLevel();
					if (level > 4) {
						Toast.makeText(
								LoginActivity.this,
								String.format(Locale.ENGLISH, "Unbelievable!\nYou're already level %d!", level),
								Toast.LENGTH_LONG
						).show();
					} else {
						Toast.makeText(
								LoginActivity.this,
								String.format(Locale.ENGLISH, "Wow, you're still level %d...", level),
								Toast.LENGTH_LONG
						).show();
					}

				}, e ->
						Toast.makeText(
								LoginActivity.this,
								"You're banned tho :(",
								Toast.LENGTH_LONG
						).show(),
				() -> PokeLogin.logout(go));
	}

	/**
	 * Will be called if the login process fails
	 *
	 * @param e Cause of the fail
	 */
	private void onLoginError(Throwable e) {
		Log.e("Login", "Login didn't work", e);
		Toast.makeText(
				this,
				"Oh, that didn't work :(",
				Toast.LENGTH_SHORT
		).show();
	}

	/**
	 * Attempts to sign in the ptc account specified by the login form.
	 * If there are form errors (invalid username, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private void attemptLoginPtc() {
		// Store values at the time of the login attempt.
		String username = mUsernameView.getText().toString();
		String password = mPasswordView.getText().toString();

		if (!ptcPreConditions(username, password)) {
			return;
		}

		// Show a progress spinner, and kick off a background task to
		// perform the user login attempt.
		showProgress(true);
		PokeLogin.login(new PTCPokeCredentials(username, password) {
			@Override
			public void onSuccess(PokemonGo pokemonGo) {
				onLoginSuccess(pokemonGo);
				showProgress(false);
			}

			@Override
			public void onError(Throwable e) {
				onLoginError(e);
				showProgress(false);
			}
		});

	}

	/**
	 * Attempts to sign in the google account specified by the login form.
	 */
	private void attemptLoginGoogle() {
		showProgress(true);

		//Login with Google and autosave the received refresh token
		PokeLogin.login(new GooglePokeCredentials(this, sharedPreferencesFile, sharedPreferencesRefreshTokenName) {
			@Override
			public void onSuccess(PokemonGo pokemonGo) {
				onLoginSuccess(pokemonGo);
				showProgress(false);
			}

			@Override
			public void onError(Throwable e) {
				onLoginError(e);
				showProgress(false);
			}
		});
	}


	/*
	 * ----------------------------- Boilerplate -----------------------------
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		bindUI();
	}

	/**
	 * Binds UI elements of the activity to fields of this class and defines events
	 */
	private void bindUI() {
		// Set up the login form.
		mLoginForm = findViewById(R.id.login_form);
		mUsernameView = (EditText) findViewById(R.id.username);
		mUsernameView.setText(SAMPLE_PTC_USERNAME);
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setText(SAMPLE_PTC_PASSWORD);

		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					PokemonGoLoginActivity.this.attemptLoginPtc();
					return true;
				}
				return false;
			}
		});

		mProgressBar = (ProgressBar) findViewById(R.id.login_progress);

		Button mPTCLoginButton = (Button) findViewById(R.id.ptc_sign_in_button);
		mPTCLoginButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						PokemonGoLoginActivity.this.attemptLoginPtc();
					}
				}
		);


		Button mGoogleLoginButton = (Button) findViewById(R.id.google_sign_in_button);
		mGoogleLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				PokemonGoLoginActivity.this.attemptLoginGoogle();
			}
		});
	}

	/**
	 * Resets error messages and checks if login would be possible with the given username or password.
	 * Shows new error messages on error.
	 *
	 * @param username PTC username
	 * @param password PTC password
	 * @return true if possible, else false
	 */
	private boolean ptcPreConditions(String username, String password) {
		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPTCPasswordValid(password)) {
			mPasswordView.setError("Password is invalid");
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(username)) {
			mUsernameView.setError("Username is required");
			focusView = mUsernameView;
			cancel = true;
		} else if (!isPTCUsernameValid(username)) {
			mUsernameView.setError("Username is invalid");
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
			return false;
		}
		return true;
	}

	/**
	 * Checks if given username is a valid PTC username
	 *
	 * @param username username
	 * @return true if valid, else false
	 */
	private boolean isPTCUsernameValid(@NonNull String username) {
		return !(username.length() < 6 || 16 < username.length())
				&& !username.matches("(?!0-9a-zA-Z_).");
	}

	/**
	 * Checks if given password is a valid PTC password
	 *
	 * @param password password
	 * @return true if valid, else false
	 */
	private boolean isPTCPasswordValid(@NonNull String password) {
		return !(password.length() < 6 || 15 < password.length());
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		if (show) {
			// Check if no view has focus:
			View view = this.getCurrentFocus();
			if (view != null) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		}

		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginForm.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});

			mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressBar.animate().setDuration(shortAnimTime).alpha(
					show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}


}
