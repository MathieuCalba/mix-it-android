package fr.mixit.android;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MixItApplication extends Application {

	public static final boolean DEBUG_MODE = true;

	@Override
	public void onCreate() {
		super.onCreate();

		final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).threadPoolSize(3)
				.threadPriority(Thread.NORM_PRIORITY).memoryCacheSize(1572864) // 1.5 Mb
				.discCacheSize(104865760) // 10 Mb
				.httpReadTimeout(10000) // 10 s
				.denyCacheImageMultipleSizesInMemory().build();
		ImageLoader.getInstance().init(config);
		// ImageLoader.getInstance().enableLogging();
	}

}
