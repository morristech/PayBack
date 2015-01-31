package com.johnsimon.payback.storage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.NotificationCallback;
import com.johnsimon.payback.async.NullCallback;
import com.johnsimon.payback.async.Subscription;
import com.johnsimon.payback.core.DataActivityInterface;
import com.johnsimon.payback.ui.FeedActivity;
import com.johnsimon.payback.ui.SettingsActivity;

public class StorageManager {
	public final static String PREFERENCE_STORAGE_TYPE = "STORAGE_TYPE";
	public final static int STORAGE_TYPE_LOCAL = 0;
	public final static int STORAGE_TYPE_DRIVE = 1;

	private static LocalStorage localStorage = null;
	private static Storage storage = null;

	public static DriveLoginManager loginManager = null;

	public static LocalStorage getLocalStorage(Context context) {
		if(localStorage == null) {
			localStorage = new LocalStorage(context);
		}
		return localStorage;
	}

	public static SharedPreferences getPreferences(Context context) {
		return getLocalStorage(context).getPreferences();
	}

    public static Storage getStorage(Activity context) {
		if(storage == null) {
			LocalStorage localStorage = getLocalStorage(context);
			switch (localStorage.getPreferences().getInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_LOCAL)) {
				case STORAGE_TYPE_LOCAL:
					storage = localStorage;
					break;
				case STORAGE_TYPE_DRIVE:
					DriveConnector connector = new DriveConnector(context);

					DriveStorage driveStorage = new DriveStorage(context, connector.client, localStorage);
					driveStorage.listen(connector.connectedPromise);

					storage = driveStorage;
					break;
			}
		}
		//TODO innan release: ta bort
		if(storage.isDriveStorage()) {
			storage.asDriveStorage().activity = context;
		}

        return storage;
    }

	public static boolean isDrive(Activity context) {
		return getStorage(context).isDriveStorage();
	}

	public static void migrateToDrive(final Activity context) {
		final DriveLoginManager loginManager = new DriveLoginManager(context);
		setLoginManager(loginManager);

		loginManager.loginResult.then(new Callback<Boolean>() {
			@Override
			public void onCalled(Boolean success) {
				if(success){
					DriveStorage driveStorage = new DriveStorage(context, loginManager.getClient(), localStorage);
					driveStorage.listen(loginManager.connectedPromise);
					localStorage.getPreferences().edit().putInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_DRIVE).apply();

					setStorage(driveStorage);
					restart(context);
				} else {
					loginManager.disconnect();

				}
				setLoginManager(null);
			}
		});

		loginManager.go();
	}

	public static void changeDriveAccount(Activity context) {
		if(storage.isDriveStorage()) {
			final DriveStorage driveStorage = storage.asDriveStorage();

			final DriveLoginManager loginManager = new DriveLoginManager(context);
			setLoginManager(loginManager);

			loginManager.loginResult.then(new Callback<Boolean>() {
				@Override
				public void onCalled(Boolean success) {
					if(success) {
						localStorage.wipe();
						driveStorage.listen(loginManager.connectedPromise);
					} else {

					}
					setLoginManager(null);
				}
			});

			loginManager.go(driveStorage.getClient());
		}
	}

	public static void migrateToLocal(Activity context) {
		storage.asDriveStorage().disconnect();

		localStorage.wipe();
		localStorage.getPreferences().edit().putInt(PREFERENCE_STORAGE_TYPE, STORAGE_TYPE_LOCAL).apply();

		setStorage(localStorage);
		restart(context);
	}

	private static void setStorage(Storage storage) {
		StorageManager.storage = storage;
	}

	private static void setLoginManager(DriveLoginManager loginManager) {
		StorageManager.loginManager = loginManager;
	}

	private static void restart(Activity context) {
		context.finishAffinity();
		//context.startActivity(new Intent(context, context.getClass()));
		context.startActivity(new Intent(context, FeedActivity.class));
	}
}
