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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.ActionBar;

import fr.mixit.android.ui.fragments.BoundServiceFragment;
import fr.mixit.android.ui.fragments.SessionDetailsFragment;
import fr.mixit.android.ui.fragments.SessionDetailsFragment.SessionDetailsContract;
import fr.mixit.android_2012.R;


public class SessionDetailsActivity extends GenericMixItActivity implements SessionDetailsContract, BoundServiceFragment.BoundServiceContract {

	protected SessionDetailsFragment mSessionDetailsFrag;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		final FragmentManager fm = getSupportFragmentManager();
		mSessionDetailsFrag = (SessionDetailsFragment) fm.findFragmentByTag(SessionDetailsFragment.TAG);
		if (mSessionDetailsFrag == null) {
			mSessionDetailsFrag = SessionDetailsFragment.newInstance(getIntent());
			fm.beginTransaction().add(R.id.content_session_details, mSessionDetailsFrag, SessionDetailsFragment.TAG).commit();
		}
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_sessions_details;
	}

	@Override
	public void refreshMenu() {
		supportInvalidateOptionsMenu();
	}

	@Override
	public void refreshList() {
		// Nothing to do because there is no list to refresh in this activity
	}

	@Override
	public void setActionBarTitle(String title) {
		final ActionBar bar = getSupportActionBar();
		bar.setTitle(title);
		bar.setDisplayShowTitleEnabled(true);
	}

	@Override
	protected int getActivityLevel() {
		return 2;
	}

	@Override
	protected Intent getParentIntent(int level) {
		switch (level) {
			case 1:
				return new Intent(this, SessionsActivity.class);

			default:
				return super.getParentIntent(level);
		}
	}

}
