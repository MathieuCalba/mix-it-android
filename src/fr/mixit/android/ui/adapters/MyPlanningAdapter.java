package fr.mixit.android.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.ui.widgets.TalkItemView;
import fr.mixit.android.ui.widgets.UnderlinedTextView;
import fr.mixit.android.utils.DateUtils;
import fr.mixit.android_2012.R;


public class MyPlanningAdapter extends CursorAdapter {

	protected LayoutInflater mInflater;

	class PlanningViewHolder {
		UnderlinedTextView mHeader;
		TalkItemView mTalk;
	}

	public MyPlanningAdapter(Context context) {
		super(context, null, 0);

		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View v = mInflater.inflate(R.layout.item_planning_talk, parent, false);
		final PlanningViewHolder holder = new PlanningViewHolder();
		holder.mHeader = (UnderlinedTextView) v.findViewById(R.id.item_planning_header);
		holder.mTalk = (TalkItemView) v.findViewById(R.id.item_planning_talk);
		holder.mTalk.setShouldDisplayStar(false);
		v.setTag(holder);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final PlanningViewHolder holder = (PlanningViewHolder) view.getTag();

		if (cursor != null) {
			final int oldPosition = cursor.getPosition();
			final long start = cursor.getLong(MixItContract.Sessions.PROJ_PLANNING.START);
			final String header = (String) DateUtils.formatPlanningHeader(start);

			long oldStart = 0;
			String oldHeader = null;
			if (cursor.moveToPrevious()) {
				oldStart = cursor.getLong(MixItContract.Sessions.PROJ_PLANNING.START);
				oldHeader = (String) DateUtils.formatPlanningHeader(oldStart);
			}

			if (header.equalsIgnoreCase(oldHeader)) {
				holder.mHeader.setVisibility(View.GONE);
			} else {
				holder.mHeader.setVisibility(View.VISIBLE);
				holder.mHeader.setText(header);
			}

			cursor.moveToPosition(oldPosition);
		}

		holder.mTalk.setContentPlanning(cursor);
	}

}
