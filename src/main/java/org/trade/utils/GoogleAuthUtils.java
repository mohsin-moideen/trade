package org.trade.utils;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

public class GoogleAuthUtils {
	private static GoogleAuthenticator gAuth = new GoogleAuthenticator();

	private static String getKey() {
		final GoogleAuthenticatorKey key = gAuth.createCredentials();
		return key.getKey();
	}

	public static boolean authorize(String userId, int otp) {
		String secretKey = getSecretKeyByUserId(userId);
		return gAuth.authorize(secretKey, otp);
	}

	private static String getSecretKeyByUserId(String userId) {
		// TODO: Get this from Param store
		return "OYSMLWO6EJ4C2P3W";
	}

	public static void main(String[] args) {
		System.out.println(getKey());
		System.out.println(authorize("", 346104));
	}
}
