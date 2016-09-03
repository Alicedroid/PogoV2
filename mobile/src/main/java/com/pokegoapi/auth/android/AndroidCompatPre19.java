package com.pokegoapi.auth.android;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A collection of methods that have not been implemented in android before API 19
 */
public class AndroidCompatPre19 {
	/**
	 * API < 19 compatible implementation of java.util.Objects.hashCode(Object)
	 * Executes hashCode() of the given object and returns the result only if it is not null, else returns 0;
	 *
	 * @param obj The object you want the hash from
	 * @return The hash of the given object
	 */
	@SuppressWarnings("WeakerAccess")
	public static int hash(@Nullable Object obj) {
		return obj == null ? 0 : obj.hashCode();
	}

	/**
	 * API < 19 compatible implementation of java.util.Object.hash(Object...)
	 * Collects the hashes of all given objects and calculates a mixed hash out of them.
	 *
	 * @param hashBase Some number. Should to be distinct between classes, but shared between instances.
	 * @param objects  The objects you want the hash from
	 * @return A mixed hash of all given objects.
	 */
	public static int hash(int hashBase, @NonNull Object... objects) {
		int hash = hashBase;
		for (Object obj : objects) {
			hash = 37 * hash + AndroidCompatPre19.hash(obj);
		}
		return hash;
	}

	/**
	 * API < 19 compatible implementation of java.util.Objects.equals(Object, Object).
	 * Checks if the two given objects are equal by calling obj.equals(obj2).
	 * Handles null objects.
	 *
	 * @param obj  Some Object
	 * @param obj2 Some other Object
	 * @return True if equal, else false.
	 */
	public static boolean equals(@Nullable Object obj, @Nullable Object obj2) {
		if (obj == null || obj2 == null) {
			return obj == obj2;
		} else {
			return obj.equals(obj2);
		}
	}
}
