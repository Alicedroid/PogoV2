package com.givepogosomelove.pokemongoinofficial.backend.Util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.ArraySet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages permissions
 * TODO make it feature based, not permission based
 * feature1 -> permission1, permission2, permission3
 * feature2 -> permission1, permission5
 * feature3 -> permission4
 */
public class PermissionManager implements ActivityCompat.OnRequestPermissionsResultCallback {
	private static final PermissionManager instance = new PermissionManager();
	private static final PermissionSet manifestPermissions = new PermissionSet(20);
	private static final PermissionSet grantedPermissions = new PermissionSet(20);
	private static final int REQUESTCODE_REQUEST_ALL = 500;


	private PermissionManager() {
		manifestPermissions
				.add2(new Permission(Manifest.permission.INTERNET))
				.add2(new Permission(Manifest.permission.ACCESS_FINE_LOCATION))
				.add2(new Permission(Manifest.permission.ACCESS_COARSE_LOCATION))
				.add2(new Permission(Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS))
				.add2(new Permission(Manifest.permission.ACCESS_NETWORK_STATE))
				.add2(new Permission(Manifest.permission.ACCESS_WIFI_STATE))
				.add2(new Permission(Manifest.permission.BATTERY_STATS))
				.add2(new Permission(Manifest.permission.WAKE_LOCK))
				.add2(new Permission(Manifest.permission.VIBRATE));
	}

	public static PermissionManager getInstance() {
		return instance;
	}

	/**
	 * @return True if all permissions are already granted, else False
	 */
	public boolean requestAll() {
		grantedPermissions.clear(); //Remove all previously granted permissions

		ArrayList<String> grantPermissionStrings = new ArrayList<>(20);
		for (Permission permission : manifestPermissions) {
			// check if already granted
			if (ContextCompat
					.checkSelfPermission(
							Config.getInstance().getContext(),
							permission.name
					) != PackageManager.PERMISSION_GRANTED) {

				//If not granted, ask for it
				grantPermissionStrings.add(permission.name);
			} else {
				//If already granted, add to granted
				grantedPermissions.add(permission);
			}
		}

		if (!grantPermissionStrings.isEmpty()) {
			//ask for not yet granted permissions
			ActivityCompat.requestPermissions(Config.getInstance().getCurrentActivity(),
					(String[]) grantPermissionStrings.toArray(),
					REQUESTCODE_REQUEST_ALL);

			return false;
		} else {
			onPermissionsUpdated();
			return true;
		}
	}

	/**
	 * Callback for the result from requesting permissions. This method
	 * is invoked for every call on {@link ActivityCompat#requestPermissions(Activity,
	 * String[], int)}.
	 * <p>
	 * <strong>Note:</strong> It is possible that the permissions request interaction
	 * with the user is interrupted. In this case you will receive empty permissions
	 * and results arrays which should be treated as a cancellation.
	 * </p>
	 *
	 * @param requestCode  The request code passed in {@link ActivityCompat#requestPermissions(
	 *Activity, String[], int)}
	 * @param permissions  The requested permissions. Never null.
	 * @param grantResults The grant results for the corresponding permissions
	 *                     which is either {@link PackageManager#PERMISSION_GRANTED}
	 *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
	 * @see ActivityCompat#requestPermissions(Activity, String[], int)
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		for (int i = 0; i < permissions.length; i++) {
			if (permissions[i] != null && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
				grantedPermissions.add(new Permission(permissions[i]));
			}
		}
		onPermissionsUpdated();
	}

	private void onPermissionsUpdated() {

	}
}

class PermissionSet extends ArrayList<Permission> {

	public PermissionSet(int i) {
		super(i);
	}

	public PermissionSet add2(Permission permission) {
		add(permission);
		return this;
	}

	@Override
	public boolean add(Permission permission) {
		//no duplicates
		if (this.indexOf(permission) == -1) {
			return super.add(permission);
		}
		return true;
	}


}


class Permission {
	public final String name;

	Permission(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof Permission && ((Permission) obj).name.equals(this.name);
	}
}