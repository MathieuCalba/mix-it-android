package fr.mixit.android.ui;

import java.util.Date;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.viewpagerindicator.PageIndicator;

import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.ui.adapters.PlanningRoomPagerAdapter;
import fr.mixit.android.ui.adapters.PlanningSlotPagerAdapter;
import fr.mixit.android.ui.fragments.BoundServiceFragment;
import fr.mixit.android_2012.R;


public class PlanningActivity extends PlanningGenericActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnPageChangeListener,
BoundServiceFragment.BoundServiceContract {

	protected static final String STATE_ROOM = "fr.mixit.android.STATE_ROOM";
	protected static final String STATE_SLOT = "fr.mixit.android.STATE_SLOT";

	protected static final int LOADER_ID_ROOMS = 1304092308;

	protected ViewPager mViewPager;
	protected PageIndicator mViewPagerIndicator;
	protected PlanningRoomPagerAdapter mRoomAdapter;
	protected PlanningSlotPagerAdapter mSlotAdapter;

	protected boolean mIsPlanningDisplayedBySlot = false;

	protected int mCurrentRoomPosition;
	protected int mCurrentSlotPosition;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		if (savedStateInstance != null) {
			mCurrentRoomPosition = savedStateInstance.getInt(STATE_ROOM, 0);
			mCurrentSlotPosition = savedStateInstance.getInt(STATE_SLOT, 0);
		}

		if (mCurrentSlotPosition == 0) {
			mCurrentSlotPosition = PlanningSlotPagerAdapter.getPositionForTimestamp(new Date().getTime());
		}

		mViewPager = (ViewPager) findViewById(R.id.pager);

		final FragmentManager fm = getSupportFragmentManager();
		if (mIsPlanningDisplayedBySlot) {
			mSlotAdapter = new PlanningSlotPagerAdapter(this, mFilter, fm, null);
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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mIsPlanningDisplayedBySlot) {
			outState.putInt(STATE_SLOT, mCurrentSlotPosition);
		} else {
			outState.putInt(STATE_ROOM, mCurrentRoomPosition);
		}
	}

	@Override
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

				final StringBuilder selection = new StringBuilder(MixItContract.Sessions.START);
				selection.append(">=");
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
