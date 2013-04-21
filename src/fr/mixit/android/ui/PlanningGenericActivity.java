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

import java.util.Calendar;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;

import fr.mixit.android.model.Planning;
import fr.mixit.android_2012.R;


public abstract class PlanningGenericActivity extends GenericMixItActivity implements OnNavigationListener {

	public static final int FILTER_DAY_ONE = 1304092314;
	public static final int FILTER_DAY_TWO = 1304092315;

	protected static final String STATE_FILTER = "fr.mixit.android.STATE_FILTER";

	protected int mFilter = FILTER_DAY_ONE;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		final Context context = getSupportActionBar().getThemedContext();
		final ArrayAdapter<CharSequence> listAdapter = ArrayAdapter.createFromResource(context, R.array.days, R.layout.sherlock_spinner_item);
		listAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		getSupportActionBar().setListNavigationCallbacks(listAdapter, this);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		final Calendar cal = Calendar.getInstance();
		final long timestamp = cal.getTimeInMillis();
		if (timestamp < Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.SEVEN_PM + Planning.THIRTY_MINUTES) {
			mFilter = FILTER_DAY_ONE;
		} else {
			mFilter = FILTER_DAY_TWO;
		}

		if (savedStateInstance != null) {
			mFilter = savedStateInstance.getInt(STATE_FILTER, FILTER_DAY_ONE);
		}

		int itemSelected = 0;
		switch (mFilter) {
			case FILTER_DAY_ONE:
				itemSelected = 0;
				break;

			case FILTER_DAY_TWO:
				itemSelected = 1;
				break;

			default:
				itemSelected = 0;
				break;
		}
		getSupportActionBar().setSelectedNavigationItem(itemSelected);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (itemPosition == 0 && mFilter != FILTER_DAY_ONE) {
			mFilter = FILTER_DAY_ONE;
			loadForDay(FILTER_DAY_ONE);
		} else if (itemPosition == 1 && mFilter != FILTER_DAY_TWO) {
			mFilter = FILTER_DAY_TWO;
			loadForDay(FILTER_DAY_TWO);
		}
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();

		loadForDay(mFilter);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_FILTER, mFilter);
	}

	protected abstract void loadForDay(int day);

}
