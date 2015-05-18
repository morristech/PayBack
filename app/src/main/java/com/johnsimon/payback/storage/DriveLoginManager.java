package com.johnsimon.payback.storage;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.plus.Plus;
import com.johnsimon.payback.BuildConfig;
import com.johnsimon.payback.async.NullPromise;
import com.johnsimon.payback.async.Promise;

public class DriveLoginManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private final static int REQUEST_CODE_RESOLUTION = 14795;

	public final static String PREFERENCE_ACCOUNT_NAME = "ACCOUNT_NAME";

	private GoogleApiClient client;
	private Activity activity;

	private boolean hasLoggedIn = false;

	public Promise<LoginResult> loginResult = new Promise<>();
	public NullPromise connectedPromise = new NullPromise();

	public DriveLoginManager(Activity activity) {
		this.activity = activity;
	}

	public GoogleApiClient getClient() {
		return client;
	}

	public void go() {
		this.client = new GoogleApiClient.Builder(activity)
            .addApi(Drive.API)
            .addScope(Drive.SCOPE_APPFOLDER)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
				.addApi(Plus.API)
            .build();

		client.connect();
	}

	public void go(GoogleApiClient client) {
		this.client = client;

		if(!client.isConnectionCallbacksRegistered(this)) {
			client.registerConnectionCallbacks(this);
			client.registerConnectionFailedListener(this);
		}

		if(client.isConnected()) {
			onConnected(null);
		} else {
			client.connect();
		}
	}

	public void disconnect() {
		client.disconnect();
	}

	@Override
	public void onConnected(Bundle bundle) {
		if(hasLoggedIn) {
			connectedPromise.fire();
			loginResult.fire(new LoginResult(getAccountName()));
		} else {
			client.clearDefaultAccountAndReconnect();
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (activity.isFinishing())
			return;

		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
			} catch (IntentSender.SendIntentException e) {
				// Unable to resolve, message user appropriately
				//client.connect();
			}
		} else {
			GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
		}
	}

	public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		switch (requestCode) {
			case REQUEST_CODE_RESOLUTION:
				if (resultCode == Activity.RESULT_OK) {
					hasLoggedIn = true;
					client.connect();

					//String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

				} else if(resultCode == Activity.RESULT_CANCELED) {
					loginResult.fire(new LoginResult());
				}
				return true;
		}
		return false;
	}

	private String getAccountName() {
		return Plus.AccountApi.getAccountName(client);

	}

	public static class LoginResult {
		public boolean success = false;
		public String accountName = null;

		//Cancelled
		public LoginResult() {
		}

		//Success
		public LoginResult(String accountName) {
			success = true;
			this.accountName = accountName;
		}
	}

	/*public class GooAccMgr {
		/*private static final String ACC_NAME = "account_name";
		public  static final int FAIL = -1;
		public  static final int UNCHANGED =  0;
		public  static final int CHANGED = +1;

		private String mCurrEmail = null;  // cache locally

		public Account[] getAllAccnts(Context ctx) {
		}

		public Account getPrimaryAccnt(Context ctx) {
			Account[] accts = getAllAccnts(ctx);
			return accts == null || accts.length == 0 ? null : accts[0];
		}

		public Account getActiveAccnt(Context ctx) {
			return email2Accnt(ctx, getActiveEmail(ctx));
		}

		public String getActiveEmail(Context ctx) {
			if (mCurrEmail != null) {
				return mCurrEmail;
			}
			mCurrEmail = ctx == null ? null : pfs(ctx).getString(ACC_NAME, null);
			return mCurrEmail;
		}

		public Account email2Accnt(Context ctx, String emil) {
			if (emil != null) {
				Account[] accounts =
						AccountManager.get(acx(ctx)).getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
				for (Account account : accounts) {
					if (emil.equalsIgnoreCase(account.name)) {
						return account;
					}
				}
			}
			return null;
		}

		/**
		 * Stores a new email in persistent app storage, reporting result
		 * @param ctx activity context
		 * @param newEmail new email, optionally null
		 * @return FAIL, CHANGED or UNCHANGED (based on the following table)
		 * OLD    NEW   SAVED   RESULT
		 * ERROR                FAIL
		 * null   null  null    FAIL
		 * null   new   new     CHANGED
		 * old    null  old     UNCHANGED
		 * old != new   new     CHANGED
		 * old == new   new     UNCHANGED
		 *//*
		public int setEmail(Context ctx, String newEmail) {
			int result = FAIL;  // 0  0

			String prevEmail = getActiveEmail(ctx);
			if        ((prevEmail == null) && (newEmail != null)) {
				result = CHANGED;
			} else if ((prevEmail != null) && (newEmail == null)) {
				result = UNCHANGED;
			} else if ((prevEmail != null) && (newEmail != null)) {
				result = prevEmail.equalsIgnoreCase(newEmail) ? UNCHANGED : CHANGED;
			}
			if (result == CHANGED) {
				mCurrEmail = newEmail;
				pfs(ctx).edit().putString(ACC_NAME, newEmail).apply();
			}
			return result;
		}

		private Context acx(Context ctx) {
			return ctx == null ? null : ctx.getApplicationContext();
		}

	}*/
}
