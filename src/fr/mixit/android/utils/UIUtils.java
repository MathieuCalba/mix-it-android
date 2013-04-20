package fr.mixit.android.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import fr.mixit.android_2012.R;

public class UIUtils {

	public static boolean isTablet(Context context) {
		// return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
		return context.getResources().getBoolean(R.bool.dual_screen);
	}

	/**
	 * Converts an intent into a {@link Bundle} suitable for use as fragment arguments.
	 */
	public static Bundle intentToFragmentArguments(Intent intent) {
		final Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}

		final Uri data = intent.getData();
		if (data != null) {
			arguments.putParcelable("_uri", data);
		}

		final Bundle extras = intent.getExtras();
		if (extras != null) {
			arguments.putAll(intent.getExtras());
		}

		return arguments;
	}

	/**
	 * Converts a fragment arguments bundle into an intent.
	 */
	public static Intent fragmentArgumentsToIntent(Bundle arguments) {
		final Intent intent = new Intent();
		if (arguments == null) {
			return intent;
		}

		final Uri data = arguments.getParcelable("_uri");
		if (data != null) {
			intent.setData(data);
		}

		intent.putExtras(arguments);
		intent.removeExtra("_uri");
		return intent;
	}

	/**
	 * Returns the version name we are currently in
	 * @param appPackageName - full name of the package of an app, 'com.dcg.meneame' for example.
	 */
	public static String getAppVersionName(Context context, String appPackageName) {
		if (context!= null) {
			try {
				return context.getPackageManager().getPackageInfo(appPackageName, 0).versionName;
			} catch (final PackageManager.NameNotFoundException e) {
			}
		}
		return null;
	}

}
