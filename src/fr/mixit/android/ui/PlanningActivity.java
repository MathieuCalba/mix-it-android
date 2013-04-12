package fr.mixit.android.ui;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.viewpagerindicator.PageIndicator;

import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.ui.adapters.PlanningRoomPagerAdapter;
import fr.mixit.android.ui.adapters.PlanningSlotPagerAdapter;
import fr.mixit.android.ui.fragments.BoundServiceFragment;
import fr.mixit.android_2012.R;


public class PlanningActivity extends GenericMixItActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnNavigationListener, OnPageChangeListener,
BoundServiceFragment.BoundServiceContract {

	public static final int FILTER_DAY_ONE = 1304092314;
	public static final int FILTER_DAY_TWO = 1304092315;

	protected static final String STATE_FILTER = "fr.mixit.android.STATE_FILTER";
	protected static final String STATE_ROOM = "fr.mixit.android.STATE_ROOM";
	protected static final String STATE_SLOT = "fr.mixit.android.STATE_SLOT";

	protected static final int LOADER_ID_ROOMS = 1304092308;

	protected ViewPager mViewPager;
	protected PageIndicator mViewPagerIndicator;
	protected PlanningRoomPagerAdapter mRoomAdapter;
	protected PlanningSlotPagerAdapter mSlotAdapter;

	protected boolean mIsPlanningDisplayedBySlot = true;

	protected int mFilter = FILTER_DAY_ONE;
	protected int mCurrentRoomPosition;
	protected int mCurrentSlotPosition;

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
			mCurrentRoomPosition = savedStateInstance.getInt(STATE_ROOM, 0);
			mCurrentSlotPosition = savedStateInstance.getInt(STATE_SLOT, 0);
		}

		if (mCurrentSlotPosition == 0) {
			mCurrentSlotPosition = PlanningSlotPagerAdapter.getPositionForTimestamp(new Date().getTime());
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

		mViewPager = (ViewPager) findViewById(R.id.pager);

		final FragmentManager fm = getSupportFragmentManager();
		if (mIsPlanningDisplayedBySlot) {
			mSlotAdapter = new PlanningSlotPagerAdapter(context, mFilter, fm, null);
			mViewPager.setAdapter(mSlotAdapter);
		} else {
			mRoomAdapter = new PlanningRoomPagerAdapter(fm);
			mViewPager.setAdapter(mRoomAdapter);
		}

		mViewPagerIndicator = (PageIndicator) findViewById(R.id.indicator);
		mViewPagerIndicator.setViewPager(mViewPager);
		mViewPagerIndicator.setOnPageChangeListener(this);
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_planning;
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
		if (mIsPlanningDisplayedBySlot) {
			outState.putInt(STATE_SLOT, mCurrentSlotPosition);
		} else {
			outState.putInt(STATE_ROOM, mCurrentRoomPosition);
		}
	}

	protected void loadForDay(int day) {
		if (mIsPlanningDisplayedBySlot) {
			if (mSlotAdapter != null) {
				mSlotAdapter.setDay(day);
			}
		} else {
			final Bundle b = new Bundle();
			b.putInt(STATE_FILTER, day);
			getSupportLoaderManager().restartLoader(LOADER_ID_ROOMS, b, this);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle b) {
		switch (id) {
			case LOADER_ID_ROOMS:
				int day = FILTER_DAY_ONE;
				if (b != null) {
					day = b.getInt(STATE_FILTER, FILTER_DAY_ONE);
				}

				final StringBuilder selection = new StringBuilder(MixItContract.Sessions.START);
				selection.append(">=");
				long startTimestamp;
				long endTimestamp;
				switch (day) {
					case FILTER_DAY_TWO:
						startTimestamp = 1366956000000L;
						endTimestamp = 1367006400000L;
						break;

					case FILTER_DAY_ONE:
					default:
						startTimestamp = 1366869600000L;
						endTimestamp = 1366920000000L;
						break;
				}
				;
				selection.append(startTimestamp);
				selection.append(" AND \"");
				selection.append(MixItContract.Sessions.END);
				selection.append("\"<=");
				selection.append(endTimestamp);

				return new CursorLoader(this, MixItContract.Sessions.buildRoomsDirUri(), MixItContract.Sessions.PROJ_ROOM.PROJECTION, selection.toString(),
						null, MixItContract.Sessions.ROOM_SORT);

			default:
				break;
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		final int id = loader.getId();

		switch (id) {
			case LOADER_ID_ROOMS:
				if (!mIsPlanningDisplayedBySlot) {
					mRoomAdapter.swapCursor(mFilter, cursor);
					mViewPager.setCurrentItem(mCurrentRoomPosition);
				}

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		final int id = loader.getId();

		switch (id) {
			case LOADER_ID_ROOMS:
				if (!mIsPlanningDisplayedBySlot) {
					mRoomAdapter.swapCursor(null);
				}

			default:
				break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		mCurrentRoomPosition = position;
		mCurrentSlotPosition = position;
	}

}
