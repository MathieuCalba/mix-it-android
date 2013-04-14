package fr.mixit.android.ui.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;
import fr.mixit.android.model.Planning;
import fr.mixit.android.ui.PlanningActivity;
import fr.mixit.android.ui.fragments.PlanningSlotPageFragment;
import fr.mixit.android.utils.DateUtils;


public class PlanningSlotPagerAdapter extends FragmentStatePagerAdapter {

	protected static final long TIMESTAMP_OFFSET_DAY_ONE = Planning.TIMESTAMP_OFFSET_DAY_ONE; // 25 à 00h
	protected static final long TIMESTAMP_OFFSET_DAY_TWO = Planning.TIMESTAMP_OFFSET_DAY_TWO; // 26 à 00h

	protected static final int NB_SLOT_DAY_ONE = 6;
	protected static final int NB_SLOT_DAY_TWO = 5;

	protected static final long[] SLOTS_START = { Planning.NINE_AND_A_HALF_AM, Planning.ELEVEN_AM, Planning.ONE_PM, Planning.TWO_AND_A_HALF_PM,
			Planning.FOUR_PM, Planning.FIVE_AND_A_HALF_PM };

	protected static final long[] SLOTS_END = { Planning.ELEVEN_AM, Planning.TWELVE_AND_A_HALF_PM, Planning.TWO_AND_A_HALF_PM, Planning.FOUR_PM,
			Planning.FIVE_AND_A_HALF_PM, Planning.SEVEN_PM };

	protected Context mContext;
	protected Fragment[] mFragments = new Fragment[NB_SLOT_DAY_ONE];
	protected Bundle mBundle;
	protected long mTimestampOffset;
	protected int mNbSlot = 0;

	// protected int mDay = 0;

	public PlanningSlotPagerAdapter(Context ctx, int day, FragmentManager fm, Bundle b) {
		super(fm);
		mContext = ctx;
		init(day, b);
	}

	protected void init(int day, Bundle b) {
		setDay(day);
		mBundle = b;
	}

	@Override
	public int getCount() {
		return mNbSlot;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		final Fragment f = (Fragment) super.instantiateItem(container, position);

		mFragments[position] = f;

		return f;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (mFragments != null && mFragments[position] != null) {
			((PlanningSlotPageFragment) mFragments[position]).setShouldRetainSlots(false);
		}
		super.destroyItem(container, position, object);

		mFragments[position] = null;
	}

	@Override
	public Fragment getItem(int position) {
		final PlanningSlotPageFragment fragment = PlanningSlotPageFragment.newInstance(mTimestampOffset + SLOTS_START[position], mTimestampOffset
				+ SLOTS_END[position], position);
		fragment.setShouldRetainSlots(true);
		return fragment;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return DateUtils.formatSlotTime(mContext, mTimestampOffset + SLOTS_START[position], mTimestampOffset + SLOTS_END[position]);
	}

	public void setDay(int newDay) {
		switch (newDay) {
			case PlanningActivity.FILTER_DAY_TWO:
				mNbSlot = NB_SLOT_DAY_TWO;
				mTimestampOffset = TIMESTAMP_OFFSET_DAY_TWO;
				break;

			case PlanningActivity.FILTER_DAY_ONE:
			default:
				mNbSlot = NB_SLOT_DAY_ONE;
				mTimestampOffset = TIMESTAMP_OFFSET_DAY_ONE;
				break;
		}

		notifyFragments();

		notifyDataSetChanged();
	}

	protected void notifyFragments() {
		if (mFragments != null) {
			for (int i = 0; i < mFragments.length/* && i < mNbSlot */; i++) {
				final PlanningSlotPageFragment fragment = (PlanningSlotPageFragment) mFragments[i];
				if (fragment != null) {
					fragment.updateSlots(mTimestampOffset + SLOTS_START[i], mTimestampOffset + SLOTS_END[i]);
				}
			}
		}
	}

	public static int getPositionForTimestamp(long timestamp) {
		for (int i = 0; i < NB_SLOT_DAY_ONE; i++) {
			if (timestamp <= TIMESTAMP_OFFSET_DAY_ONE + SLOTS_END[i]) {
				return i;
			}
		}

		for (int i = 0; i < NB_SLOT_DAY_TWO; i++) {
			if (timestamp <= TIMESTAMP_OFFSET_DAY_TWO + SLOTS_END[i]) {
				return i;
			}
		}

		return NB_SLOT_DAY_TWO - 1;
	}

}
