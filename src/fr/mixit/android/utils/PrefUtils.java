package fr.mixit.android.utils;

public interface PrefUtils {

	static final int VERSION_NONE = 0;
	static final int VERSION_LOCAL_2011 = 1;
	static final int VERSION_REMOTE_2011 = 5;
	static final int VERSION_LOCAL_2012 = 10;
	static final int VERSION_REMOTE_2012 = 15;
	static final int VERSION_LOCAL_2013 = 20;
	static final int VERSION_REMOTE_2013 = 25;
	static final int VERSION_LOCAL = VERSION_LOCAL_2013;
	static final int VERSION_REMOTE = VERSION_REMOTE_2013;

	static final String MIXITSCHED_SYNC = "mixitsched_sync";
	static final String LOCAL_VERSION = "local_version";
	static final String LAST_REMOTE_SYNC = "last_remote_sync";

	public static final String SETTINGS_NAME = "MixItScheduleSettings";
}
