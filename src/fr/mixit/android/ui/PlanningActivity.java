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

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

import com.viewpagerindicator.PageIndicator;

import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.ui.adapters.PlanningRoomPagerAdapter;
import fr.mixit.android.ui.adapters.PlanningSlotPagerAdapter;
import fr.mixit.android.ui.fragments.BoundServiceFragment;
import fr.mixit.android.ui.fragments.MemberDetailsFragment;
import fr.mixit.android.ui.fragments.MemberDetailsFragment.MemberDetailsContract;
import fr.mixit.android.ui.fragments.SessionDetailsFragment;
import fr.mixit.android.ui.fragments.SessionDetailsFragment.SessionDetailsContract;
import fr.mixit.android.utils.IntentUtils;
import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;


public class PlanningActivity extends PlanningGenericActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnPageChangeListener, SessionDetailsContract,
MemberDetailsContract, BoundServiceFragment.BoundServiceContract {

	private static final String TAG = PlanningActivity.class.getSimpleName();

	protected static final String STATE_ROOM = "fr.mixit.android.STATE_ROOM";
	protected static final String STATE_SLOT = "fr.mixit.android.STATE_SLOT";

	protected static final int LOADER_ID_ROOMS = 1304092308;

	protected ViewPager mViewPager;
	protected PageIndicator mViewPagerIndicator;
	protected PlanningRoomPagerAdapter mRoomAdapter;
	protected PlanningSlotPagerAdapter mSlotAdapter;
	protected SessionDetailsFragment mSessionDetailsFrag;

	protected boolean mIsPlanningDisplayedBySlot = true;

	protected int mTopFragCommitId = -1;

	protected int mCurrentRoomPosition = 0;
	protected int mCurrentSlotPosition = -1;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		if (savedStateInstance != null) {
			mCurrentRoomPosition = savedStateInstance.getInt(STATE_ROOM, 0);
			mCurrentSlotPosition = savedStateInstance.getInt(STATE_SLOT, -1);
		}

		if (mCurrentSlotPosition == -1) {
			mCurrentSlotPosition = PlanningSlotPagerAdapter.getPositionForTimestamp(Calendar.getInstance().getTimeInMillis());
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

		if (UIUtils.isTablet(this)) {
			mSessionDetailsFrag = (SessionDetailsFragment) fm.findFragmentByTag(SessionDetailsFragment.TAG);
			if (mSessionDetailsFrag == null) {
				mSessionDetailsFrag = SessionDetailsFragment.newInstance(getIntent());
				fm.beginTransaction().add(R.id.content_session_details, mSessionDetailsFrag, SessionDetailsFragment.TAG).commit();
			}
		}
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_planning;
	}

	@Override
	protected void initActionBar() {
		super.initActionBar();
		getSupportActionBar().setDisplayShowTitleEnabled(false);
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
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (itemPosition == 0 && mFilter != FILTER_DAY_ONE) {
			mFilter = FILTER_DAY_ONE;
			loadForDay(FILTER_DAY_ONE);
			if (mSessionDetailsFrag != null) {
				mSessionDetailsFrag.setSessionId(-1);
			}
		} else if (itemPosition == 1 && mFilter != FILTER_DAY_TWO) {
			mFilter = FILTER_DAY_TWO;
			loadForDay(FILTER_DAY_TWO);
			if (mSessionDetailsFrag != null) {
				mSessionDetailsFrag.setSessionId(-1);
			}
		}
		return true;
	}

	@Override
	protected void loadForDay(int day) {
		if (mIsPlanningDisplayedBySlot) {
			if (mSlotAdapter != null) {
				mSlotAdapter.setDay(day);
			}
			if (mViewPager != null) {
				mViewPager.setCurrentItem(mCurrentSlotPosition);
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
					if (mRoomAdapter != null) {
						mRoomAdapter.swapCursor(mFilter, cursor);
					}
					if (mViewPager != null) {
						mViewPager.setCurrentItem(mCurrentRoomPosition);
					}
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

	@Override
	public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
		final Uri uri = intent.getData();
		if (uri != null && uri.getAuthority().equals(MixItContract.Sessions.CONTENT_URI.getAuthority())) {
			final boolean addToBackStack = intent.getBooleanExtra(IntentUtils.EXTRA_FROM_ADD_TO_BACKSTACK, false);
			final FragmentManager fm = getSupportFragmentManager();
			// SESSION
			if (uri.getEncodedPath().startsWith(SLASH + MixItContract.PATH_SESSIONS) || uri.getEncodedPath().startsWith(SLASH + MixItContract.PATH_LIGHTNINGS)) {
				if (UIUtils.isTablet(this)) {
					if (addToBackStack) {
						final SessionDetailsFragment frag = SessionDetailsFragment.newInstance(intent);
						final FragmentTransaction ft = fm.beginTransaction();
						ft.replace(R.id.content_session_details, frag);
						ft.addToBackStack(null);
						if (mTopFragCommitId == -1) {
							mTopFragCommitId = ft.commit();
						} else {
							ft.commit();
						}
						return;
					} else {
						if (mTopFragCommitId != -1) {
							fm.popBackStack(mTopFragCommitId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
							mTopFragCommitId = -1;
						}
						if (mSessionDetailsFrag != null) {
							final int sessionId = Integer.parseInt(MixItContract.Sessions.getSessionId(uri));
							mSessionDetailsFrag.setSessionId(sessionId);
							return;
						} else {
							Log.e(TAG, "no fragment session details found but device is tablet");
						}
					}
				} else {
					super.startActivityFromFragment(fragment, intent, requestCode);
					return;
				}
			} else
				// MEMBERS
				if (uri.getEncodedPath().startsWith(SLASH + MixItContract.PATH_MEMBERS) || uri.getEncodedPath().startsWith(SLASH + MixItContract.PATH_SPEAKERS)) {
					if (UIUtils.isTablet(this)) {
						final MemberDetailsFragment frag = MemberDetailsFragment.newInstance(intent);
						final FragmentTransaction ft = fm.beginTransaction();
						ft.replace(R.id.content_session_details, frag);
						ft.addToBackStack(null);
						if (mTopFragCommitId == -1) {
							mTopFragCommitId = ft.commit();
						} else {
							ft.commit();
						}
						return;
					} else {
						super.startActivityFromFragment(fragment, intent, requestCode);
						return;
					}
				}
		}
		super.startActivityFromFragment(fragment, intent, requestCode);
	}

	@Override
	public void refreshMenu() {
		supportInvalidateOptionsMenu();
	}

	@Override
	public void refreshList() {
		// Nothing to do
	}

	@Override
	public void setActionBarTitle(String title) {
		// Nothing to
	}

}
