package fr.mixit.android.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.mixit.android.ui.widgets.TalkItemView;


public class SessionsAdapter extends CursorAdapter {

	protected LayoutInflater mInflater;
	protected boolean mDisplayStar = true;

	public SessionsAdapter(Context ctx) {
		this(ctx, true);
	}

	public SessionsAdapter(Context ctx, boolean displayStar) {
		super(ctx, null, 0);

		mInflater = LayoutInflater.from(ctx);
		mDisplayStar = displayStar;
	}

	public void setDisplayStar(boolean displayStar) {
		mDisplayStar = displayStar;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final TalkItemView v = new TalkItemView(context);
		v.setShouldDisplayStar(mDisplayStar);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final TalkItemView v = (TalkItemView) view;
		v.setContent(cursor);
		// TODO : add grey background on past session
		// // Possibly indicate that the session has occurred in the past.
		// UIUtils.setSessionTitleColor(blockStart, blockEnd, titleView, subtitleView);
	}

}
