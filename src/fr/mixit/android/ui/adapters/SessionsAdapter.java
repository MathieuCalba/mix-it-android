package fr.mixit.android.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android_2012.R;


public class SessionsAdapter extends CursorAdapter {

	protected LayoutInflater mInflater;
	protected boolean mDisplayStar = true;

	protected class SessionHolder {
		View room;
		ImageView starred;
		TextView title;
		TextView subtitle;
	}

	public SessionsAdapter(Context ctx) {
		this(ctx, true);
	}

	public SessionsAdapter(Context ctx, boolean displayStar) {
		super(ctx, null, 0);

		mInflater = LayoutInflater.from(ctx);
		mDisplayStar = displayStar;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View v = mInflater.inflate(R.layout.item_session, parent, false);

		final SessionHolder holder = new SessionHolder();

		holder.starred = (ImageView) v.findViewById(R.id.star_button);
		final Drawable drawable = holder.starred.getDrawable();
		drawable.setColorFilter(new LightingColorFilter(mContext.getResources().getColor(R.color.star_color), 1));
		holder.title = (TextView) v.findViewById(R.id.session_title);
		holder.subtitle = (TextView) v.findViewById(R.id.session_subtitle);
		holder.subtitle.setVisibility(View.GONE);
		holder.room = v.findViewById(R.id.session_room);

		v.setTag(holder);

		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final SessionHolder holder = (SessionHolder) view.getTag();

		holder.title.setText(cursor.getString(MixItContract.Sessions.PROJ_LIST.TITLE));

		// TODO : add time, speaker, room information
		// final long blockStart = cursor.getLong(SessionsQuery.BLOCK_START);
		// final long blockEnd = cursor.getLong(SessionsQuery.BLOCK_END);
		// final String roomName = cursor.getString(SessionsQuery.ROOM_NAME);
		// final String company = formatSessionSubtitle(blockStart, blockEnd, roomName, mContext);
		// holder.subtitle.setText(company);

		// String track = cursor.getString(SessionsQuery.TRACK);
		// int color = android.R.color.transparent;
		// if (Track.Agility.name().equals(track)) {
		// color = R.color.agility;
		// } else if (Track.Techy.name().equals(track)) {
		// color = R.color.techy;
		// } else if (Track.Trendy.name().equals(track)) {
		// color = R.color.trendy;
		// } else if (Track.Weby.name().equals(track)) {
		// color = R.color.weby;
		// } else if (Track.Gamy.name().equals(track)) {
		// color = R.color.gamy;
		// }
		// holder.track.setBackgroundColor(mContext.getResources().getColor(color));

		final boolean starred = cursor.getInt(MixItContract.Sessions.PROJ_LIST.IS_FAVORITE) != 0;
		holder.starred.setVisibility(mDisplayStar && starred ? View.VISIBLE : View.INVISIBLE);

		// TODO : add grey background on past session
		// // Possibly indicate that the session has occurred in the past.
		// UIUtils.setSessionTitleColor(blockStart, blockEnd, titleView, subtitleView);
	}

}
