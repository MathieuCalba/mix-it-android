package fr.mixit.android.ui.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android_2012.R;


public class SpeakerTalkView extends LinearLayout {

	protected TextView mTitle;
	protected TextView mSubTitle;

	public SpeakerTalkView(Context context) {
		super(context);

		init(context);
	}

	public SpeakerTalkView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public SpeakerTalkView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);
	}

	protected void init(Context context) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.speaker_talk_item, this, true);

		final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		setLayoutParams(lp);

		final int padding = context.getResources().getDimensionPixelSize(R.dimen.margin_small);
		setPadding(padding, padding, padding, padding);

		setOrientation(VERTICAL);

		setBackgroundColor(context.getResources().getColor(R.color.light_gray));

		mTitle = (TextView) findViewById(R.id.talk_title);
		mSubTitle = (TextView) findViewById(R.id.talk_subtitle);
	}

	public void setContent(Cursor c) {
		if (c == null) {
			return;
		}

		final String title = c.getString(MixItContract.Sessions.PROJ_LIST.TITLE);
		final String format = c.getString(MixItContract.Sessions.PROJ_LIST.FORMAT);
		final String room = c.getString(MixItContract.Sessions.PROJ_LIST.ROOM_ID);
		// final long start = c.getLong(MixItContract.Sessions.PROJ_LIST.TIME_START);
		// final long end = c.getLong(MixItContract.Sessions.PROJ_LIST.TIME_END);

		mTitle.setText(title + " [" + format + "]");
		mSubTitle.setText("On DDD, from HH:MM to HH:MM, in + " + room);
	}

}
