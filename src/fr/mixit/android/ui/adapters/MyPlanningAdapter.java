package fr.mixit.android.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;

import fr.mixit.android.model.Planning;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.ui.widgets.TalkItemView;
import fr.mixit.android.utils.DateUtils;
import fr.mixit.android_2012.R;


public class MyPlanningAdapter extends CursorAdapter implements StickyListHeadersAdapter {

	protected LayoutInflater mInflater;

	public MyPlanningAdapter(Context context) {
		super(context, null, 0);

		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final TalkItemView v = new TalkItemView(context);
		v.setShouldDisplayStar(false);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final TalkItemView v = (TalkItemView) view;
		v.setContentPlanning(cursor);
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		TextView header = null;
		if (convertView == null) {
			header = (TextView) mInflater.inflate(R.layout.item_planning_header, parent, false);
			convertView = header;
		} else {
			header = (TextView) convertView;
		}

		final Cursor c = getCursor();
		if (c != null && c.moveToPosition(position)) {
			final long start = c.getLong(MixItContract.Sessions.PROJ_PLANNING.START);

			header.setText(DateUtils.formatPlanningHeader(start));
		}

		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		final Cursor c = getCursor();
		if (c != null && c.moveToPosition(position)) {
			final long start = c.getLong(MixItContract.Sessions.PROJ_PLANNING.START);
			if (start > Planning.TIMESTAMP_OFFSET_DAY_TWO) {
				return 1;
			}
		}
		return 0;
	}

}
