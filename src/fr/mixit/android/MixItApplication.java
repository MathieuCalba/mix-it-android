/*
 * Copyright 2011 - 2013 Mathieu Calba
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.mixit.android;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


public class MixItApplication extends Application {

	public static final boolean DEBUG_MODE = false;

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
