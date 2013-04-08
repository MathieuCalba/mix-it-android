package fr.mixit.android.ui.widgets;

import android.content.Context;
import android.database.Cursor;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.utils.DateUtils;
import fr.mixit.android_2012.R;


public class TalkItemView extends RelativeLayout {

	protected TextView mTitle;
	protected TextView mSubTitle;
	protected ImageView mStar;

	protected boolean mDisplayStar = true;

	public TalkItemView(Context context) {
		super(context);

		init(context);
	}

	public TalkItemView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	public TalkItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);
	}

	protected void init(Context context) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.item_talk, this, true);

		final int padding = context.getResources().getDimensionPixelSize(R.dimen.margin_small);
		setPadding(padding, padding, padding, padding);

		setBackgroundColor(context.getResources().getColor(R.color.light_gray));

		mTitle = (TextView) findViewById(R.id.talk_title);
		mSubTitle = (TextView) findViewById(R.id.talk_subtitle);
		mStar = (ImageView) findViewById(R.id.talk_star_button);
		final Drawable drawable = mStar.getDrawable();
		drawable.setColorFilter(new LightingColorFilter(context.getResources().getColor(R.color.star_color), 1));
	}

	public void setShouldDisplayStar(boolean displayStar) {
		mDisplayStar = displayStar;
	}

	public void setContent(Cursor c) {
		if (c == null) {
			return;
		}

		final String title = c.getString(MixItContract.Sessions.PROJ_LIST.TITLE);
		final String format = c.getString(MixItContract.Sessions.PROJ_LIST.FORMAT);
		final String room = c.getString(MixItContract.Sessions.PROJ_LIST.ROOM_ID);
		final long start = c.getLong(MixItContract.Sessions.PROJ_LIST.START);
		final long end = c.getLong(MixItContract.Sessions.PROJ_LIST.END);

		mTitle.setText(title + " [" + format + "]");
		mSubTitle.setText(DateUtils.formatSessionTime(getContext(), start, end, room));// "On DDD, from HH:MM to HH:MM, in " + room
		// final Drawable drawable = holder.starred.getDrawable();
		// drawable.setColorFilter(new LightingColorFilter(mContext.getResources().getColor(R.color.star_color), 1));

		final boolean starred = c.getInt(MixItContract.Sessions.PROJ_LIST.IS_FAVORITE) != 0;
		mStar.setVisibility(mDisplayStar && starred ? View.VISIBLE : View.INVISIBLE);
	}

}
