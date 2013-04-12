package fr.mixit.android.ui.adapters;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.ui.PlanningActivity;
import fr.mixit.android.ui.fragments.PlanningRoomPageFragment;


public class PlanningRoomPagerAdapter extends FragmentStatePagerCursorAdapter {

	protected int mDay = PlanningActivity.FILTER_DAY_ONE;

	public PlanningRoomPagerAdapter(FragmentManager fm) {
		super(null, fm, null);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (mFragments != null && mFragments[position] != null) {
			((PlanningRoomPageFragment) mFragments[position]).setShouldRetainSlots(false);
		}
		super.destroyItem(container, position, object);
	}

	@Override
	public Fragment getItem(int position) {
		PlanningRoomPageFragment fragment = null;
		if (mCursor != null && position >= 0 && position < mCursor.getCount() && mCursor.moveToPosition(position)) {
			final String room = mCursor.getString(MixItContract.Sessions.PROJ_ROOM.ROOM_ID);
			fragment = PlanningRoomPageFragment.newInstance(room, mDay, position);
		} else {
			fragment = PlanningRoomPageFragment.newInstance(null, mDay, position);
		}
		fragment.setShouldRetainSlots(true);
		return fragment;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		if (mCursor != null && position >= 0 && position < mCursor.getCount() && mCursor.moveToPosition(position)) {
			final String room = mCursor.getString(MixItContract.Sessions.PROJ_ROOM.ROOM_ID);
			return room;
		}
		return "";
	}

	public Cursor swapCursor(int day, Cursor newCursor) {
		mDay = day;
		notifyFragments();
		final Cursor c = super.swapCursor(newCursor);
		return c;
	}

	protected void notifyFragments() {
		if (mFragments != null) {
			for (int i = 0; i < mFragments.length/* && i < mNbSlot */; i++) {
				final PlanningRoomPageFragment fragment = (PlanningRoomPageFragment) mFragments[i];
				if (fragment != null) {
					fragment.updateDay(mDay);
				}
			}
		}
	}

}
