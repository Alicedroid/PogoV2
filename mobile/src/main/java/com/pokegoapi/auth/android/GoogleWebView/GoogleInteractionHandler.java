package com.pokegoapi.auth.android.GoogleWebView;

import android.support.annotation.NonNull;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Handles the interactions with the WebView used for the Google login.
 */
abstract class GoogleInteractionHandler {

	/**
	 * Takes a WebView, sets UA and binds the Javascript interfaces.
	 *
	 * @param wv         The WebView to watch over
	 * @param autoAccept If true, accepts google terms automatically, else the user will be asked.
	 */
	GoogleInteractionHandler(@NonNull final WebView wv, final boolean autoAccept) {
		wv.getSettings().setUserAgentString("Mozilla/5.0 (Android; Mobile; rv:26.0) Gecko/26.0 Firefox/26.0");
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				view.loadUrl(
						"javascript: " +
								"(function() {" +
								(autoAccept
										? "	window.setInterval(function(){" +
										"		var submitButton = document.getElementById('submit_approve_access');" +
										"		if(submitButton != null && !submitButton.hasAttribute('disabled')){" +
										"			submitButton.click();" +
										"		}" +
										"	}, 100);"
										: "") +
								"		HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); " +
								"}) ();");
			}

		});

	}


	/**
	 * Searches for patterns in the received html document and handles them accordingly.
	 *
	 * @param html HTML of the received document
	 */
	@SuppressWarnings("unused")
	@JavascriptInterface
	void processHTML(String html) {
		int pos;
		if ((pos = html.indexOf("<input id=\"code\"")) != -1) {
			// Permission accepted.
			int begin = html.indexOf("value=\"", pos) + 7;
			int end = html.indexOf("\"", begin);

			onPermissionAccepted(html, html.substring(begin, end));

		} else if (html.contains("<p id=\"access_denied\">")) {
			// Permission denied.
			onPermissionDenied(html);

		} else if (!html.contains("<form id=\"connect-approve\"")           // Do you want to accept permissions?-Dialog
				&& !html.contains("<input id=\"signIn\"")                   // Login Dialog
				&& !html.contains("<a id=\"account-chooser-add-account\"")  // Login with different account Dialog
				&& !html.contains("<input id=\"next\" name=\"signIn\"")        // Create new google account Dialog
				&& !html.contains("<head><meta http-equiv=\"refresh\"")        // Logout redirector
				&& !html.contains("action=\"/signin/challenge")                // Two-Way-Authentication Dialog
			// tested: SMS, BackupCode, Google Authenticator App
			// untested: Dongle, Secondary phone, Google Prompt...
			//TODO: test Two-Way-Authentication using Dongle
			//TODO: test Two-Way-Authentication using Secondary Phone
			//TODO: test Two-Way-Authentication using Google Prompt
				) {

			onUnexpectedResult(html);
		}
	}

	/**
	 * Will be called if the user accepted the terms
	 *
	 * @param html        html document
	 * @param accessToken received access token
	 */
	abstract void onPermissionAccepted(String html, String accessToken);

	/**
	 * Will be called if the user denied the terms
	 *
	 * @param html html document
	 */
	abstract void onPermissionDenied(String html);

	/**
	 * Will be called if the resulting page was unexpected
	 *
	 * @param html html document
	 */
	abstract void onUnexpectedResult(String html);

}
