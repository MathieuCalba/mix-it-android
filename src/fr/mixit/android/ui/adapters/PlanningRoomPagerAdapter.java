package fr.mixit.android.ui.adapters;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.ui.PlanningActivity;
import fr.mixit.android.ui.fragments.PlanningRoomPageFragment;


public class PlanningRoomPagerAdapter extends FragmentStatePagerCursorAdapter {

	protected int mDay = PlanningActivity.FILTER_DAY_ONE;

	public PlanningRoomPagerAdapter(FragmentManager fm) {
		super(null, fm, null);
	}

	@Override
	public Fragment getItem(int position) {
		if (mCursor != null && position >= 0 && position < mCursor.getCount() && mCursor.moveToPosition(position)) {
			final String room = mCursor.getString(MixItContract.Sessions.PROJ_ROOM.ROOM_ID);
			return PlanningRoomPageFragment.newInstance(room, mDay, position);
		}
		return PlanningRoomPageFragment.newInstance(null, mDay, position);
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
		return super.swapCursor(newCursor);
	}
}
