package fr.mixit.android.ui;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;

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
