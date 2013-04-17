package fr.mixit.android.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;


public class PrefUtils {

	public static final int VERSION_NONE = 0;
	public static final int VERSION_LOCAL_2011 = 1;
	public static final int VERSION_REMOTE_2011 = 5;
	public static final int VERSION_LOCAL_2012 = 10;
	public static final int VERSION_REMOTE_2012 = 15;
	public static final int VERSION_LOCAL_2013 = 20;
	public static final int VERSION_REMOTE_2013 = 25;
	public static final int VERSION_LOCAL = VERSION_LOCAL_2013;
	public static final int VERSION_REMOTE = VERSION_REMOTE_2013;

	public static final String MIXITSCHED_SYNC = "mixitsched_sync";
	public static final String LOCAL_VERSION = "local_version";
	// public static final String LAST_REMOTE_SYNC = "last_remote_sync";

	public static final String SETTINGS_NAME = "MixItScheduleSettings";

	public static final String HIDDEN_SETTINGS = "fr.mixit.android.HIDDEN_SETTINGS";
	public static final String LAST_REMOTE_SYNC = "fr.mixit.android.LAST_REMOTE_SYNC";
	public static final String IS_WARNING_STAR_SESSION_SHOULD_BE_SHOWN = "fr.mixit.android.IS_WARNING_STAR_SESSION_SHOULD_BE_SHOWN";

	public static long getLastRemoteSync(Context ctx) {
		if (ctx == null) {
			return 0;
		}

		final SharedPreferences pref = ctx.getSharedPreferences(HIDDEN_SETTINGS, Context.MODE_PRIVATE);
		final long value = pref.getLong(LAST_REMOTE_SYNC, 0);
		return value;
	}

	@SuppressLint("CommitPrefEdits")
	public static void setLastRemoteSync(Context ctx, long value) {
		if (ctx == null) {
			return;
		}

		final SharedPreferences pref = ctx.getSharedPreferences(HIDDEN_SETTINGS, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = pref.edit();
		editor.putLong(LAST_REMOTE_SYNC, value);
		commitOrApply(editor);
	}

	public static boolean isWarningStarSessionShouldBeShown(Context ctx) {
		if (ctx == null) {
			return false;
		}

		final SharedPreferences pref = ctx.getSharedPreferences(HIDDEN_SETTINGS, Context.MODE_PRIVATE);
		final boolean value = pref.getBoolean(IS_WARNING_STAR_SESSION_SHOULD_BE_SHOWN, true);
		return value;
	}

	@SuppressLint("CommitPrefEdits")
	public static void setWarningStarSessionShouldBeShown(Context ctx, boolean value) {
		if (ctx == null) {
			return;
		}

		final SharedPreferences pref = ctx.getSharedPreferences(HIDDEN_SETTINGS, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(IS_WARNING_STAR_SESSION_SHOULD_BE_SHOWN, value);
		commitOrApply(editor);
	}

	@TargetApi(9)
	public static void commitOrApply(SharedPreferences.Editor editor) {
		if (editor == null) {
			return;
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			editor.commit();
		} else {
			editor.apply();
		}
	}
}
