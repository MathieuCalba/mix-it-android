package fr.mixit.android.ui.widgets;

import android.content.Context;
import android.database.Cursor;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fr.mixit.android.model.PlanningSlot;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.utils.DateUtils;
import fr.mixit.android_2012.R;


public class TalkItemView extends RelativeLayout implements OnCheckedChangeListener {

	protected TextView mTitle;
	protected TextView mSubTitle;
	// protected ImageView mStar;
	protected CheckBox mStar;

	protected boolean mDisplayStar = true;

	protected String mIdTalk;
	protected String mTalkTitle;

	public interface StarListener {
		void onStarTouched(String idTalk, String titleTalk, boolean state);
	}

	protected StarListener mStarListener;

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

		setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);

		mTitle = (TextView) findViewById(R.id.talk_title);
		mSubTitle = (TextView) findViewById(R.id.talk_subtitle);
		// mStar = (ImageView) findViewById(R.id.talk_star_button);
		// final Drawable drawable = mStar.getDrawable();
		// drawable.setColorFilter(new LightingColorFilter(context.getResources().getColor(R.color.star_color), 1));
		mStar = (CheckBox) findViewById(R.id.talk_star_button);
		final Drawable drawable = context.getResources().getDrawable(R.drawable.btn_star);
		drawable.setColorFilter(new LightingColorFilter(context.getResources().getColor(R.color.star_color), 1));
		mStar.setButtonDrawable(drawable);
		mStar.setOnCheckedChangeListener(this);
	}

	public void setStarListener(StarListener starListener) {
		mStarListener = starListener;
	}

	public void setShouldDisplayStar(boolean displayStar) {
		mDisplayStar = displayStar;
	}

	public void setContent(Cursor c) {
		if (c == null) {
			return;
		}

		mIdTalk = c.getString(MixItContract.Sessions.PROJ_LIST.SESSION_ID);

		mTalkTitle = c.getString(MixItContract.Sessions.PROJ_LIST.TITLE);
		final String format = c.getString(MixItContract.Sessions.PROJ_LIST.FORMAT);

		if (format == null || format.equalsIgnoreCase(MixItContract.Sessions.FORMAT_LIGHTNING_TALK)) {
			mSubTitle.setVisibility(View.GONE);
		} else {
			final String room = c.getString(MixItContract.Sessions.PROJ_LIST.ROOM_ID);
			final long start = c.getLong(MixItContract.Sessions.PROJ_LIST.START);
			final long end = c.getLong(MixItContract.Sessions.PROJ_LIST.END);

			if (TextUtils.isEmpty(room)) {
				mSubTitle.setText(DateUtils.formatSessionTime(getContext(), start, end));// "On DDD, from HH:MM to HH:MM"
			} else {
				mSubTitle.setText(DateUtils.formatSessionTime(getContext(), start, end, room));// "On DDD, from HH:MM to HH:MM, in " + room
			}
			mSubTitle.setVisibility(View.VISIBLE);
		}

		mTitle.setText(mTalkTitle + " [" + format + "]");

		// final Drawable drawable = mStar.getDrawable();
		// drawable.setColorFilter(new LightingColorFilter(getContext().getResources().getColor(R.color.star_color), 1));

		final boolean starred = c.getInt(MixItContract.Sessions.PROJ_LIST.IS_FAVORITE) != 0;
		mStar.setOnCheckedChangeListener(null);
		mStar.setChecked(starred);
		mStar.setOnCheckedChangeListener(this);
		mStar.setVisibility(mDisplayStar && !MixItContract.Sessions.FORMAT_LIGHTNING_TALK.equalsIgnoreCase(format) ? View.VISIBLE : View.GONE);
		// mStar.setVisibility(mDisplayStar ? starred ? View.VISIBLE : View.INVISIBLE : View.GONE);
	}

	public void setContentPlanning(Cursor c) {
		if (c == null) {
			return;
		}

		final int slotType = c.getInt(MixItContract.Sessions.PROJ_PLANNING.SLOT_TYPE);
		final int nbConcurrentTalk = c.getInt(MixItContract.Sessions.PROJ_PLANNING.NB_CONCURRENT_TALKS);

		switch (slotType) {
			case PlanningSlot.TYPE_SESSION:
			case PlanningSlot.TYPE_NO_SESSION:
			case PlanningSlot.TYPE_LIGHTNING_TALK:
			case PlanningSlot.TYPE_KEYNOTE: {
				final String title = c.getString(MixItContract.Sessions.PROJ_PLANNING.TITLE);
				final String room = c.getString(MixItContract.Sessions.PROJ_PLANNING.ROOM_ID);
				final long start = c.getLong(MixItContract.Sessions.PROJ_PLANNING.START);
				final long end = c.getLong(MixItContract.Sessions.PROJ_PLANNING.END);

				if (nbConcurrentTalk == 1) {
					final String format = c.getString(MixItContract.Sessions.PROJ_PLANNING.FORMAT);

					mTitle.setText(title + " [" + format + "]");
				} else {
					mTitle.setText(title);
				}
				if (TextUtils.isEmpty(room)) {
					mSubTitle.setText(DateUtils.formatPlanningSessionTime(getContext(), start, end));// "On DDD, from HH:MM to HH:MM"
				} else {
					mSubTitle.setText(DateUtils.formatPlanningSessionTime(getContext(), start, end, room));// "On DDD, from HH:MM to HH:MM, in " + room
				}
				mSubTitle.setVisibility(View.VISIBLE);

				break;
			}

			case PlanningSlot.TYPE_BREAK:
			case PlanningSlot.TYPE_BREAKFAST:
			case PlanningSlot.TYPE_LUNCH:
			case PlanningSlot.TYPE_TALKS_PRESENTATION:
			case PlanningSlot.TYPE_WELCOME: {
				final String title = c.getString(MixItContract.Sessions.PROJ_PLANNING.TITLE);

				mTitle.setText(title);
				mSubTitle.setVisibility(View.GONE);

				break;
			}

			default:
				mTitle.setText("Missing Slot type management");
				mSubTitle.setVisibility(View.GONE);

				break;
		}

		mStar.setVisibility(View.GONE);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (mStarListener != null) {
			mStarListener.onStarTouched(mIdTalk, mTalkTitle, isChecked);
		}
	}

}
