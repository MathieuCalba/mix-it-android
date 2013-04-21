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

package fr.mixit.android.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import fr.mixit.android.ui.fragments.AboutFragment;
import fr.mixit.android_2012.R;

public class AboutActivity extends GenericMixItActivity {

	AboutFragment mAboutFrag;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		final FragmentManager fm = getSupportFragmentManager();
		mAboutFrag = (AboutFragment) fm.findFragmentByTag(AboutFragment.TAG);
		if (mAboutFrag == null) {
			mAboutFrag = AboutFragment.newInstance(getIntent());
			fm.beginTransaction().add(R.id.content_about, mAboutFrag, AboutFragment.TAG).commit();
		}
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_about;
	}

}
