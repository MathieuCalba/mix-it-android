package fr.mixit.android.ui;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.ui.widgets.HScroll;
import fr.mixit.android.ui.widgets.TalkItemView;
import fr.mixit.android.ui.widgets.VScroll;
import fr.mixit.android_2012.R;


public class PlanningFullActivity extends PlanningGenericActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	protected static final String EXTRA_DAY = "fr.mixit.android.EXTRA_DAY";

	protected static final int LOADER_ID_SESSIONS = 1304131244;

	private float mX, mY;
	// private float curX, curY;

	protected VScroll mVScroll;
	protected HScroll mHScroll;
	protected LinearLayout mGridPlanning;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		mVScroll = (VScroll) findViewById(R.id.planning_v_scroll);
		mHScroll = (HScroll) findViewById(R.id.planning_h_scroll);
		mGridPlanning = (LinearLayout) findViewById(R.id.planning_grid);
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_planning_full;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float curX, curY;

		switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN:
				mX = event.getX();
				mY = event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				curX = event.getX();
				curY = event.getY();
				mVScroll.scrollBy((int) (mX - curX), (int) (mY - curY));
				mHScroll.scrollBy((int) (mX - curX), (int) (mY - curY));
				mX = curX;
				mY = curY;
				break;
			case MotionEvent.ACTION_UP:
				curX = event.getX();
				curY = event.getY();
				mVScroll.scrollBy((int) (mX - curX), (int) (mY - curY));
				mHScroll.scrollBy((int) (mX - curX), (int) (mY - curY));
				break;
		}

		return true;
	}

	@Override
	protected void loadForDay(int day) {
		final Bundle b = new Bundle();
		b.putInt(STATE_FILTER, day);
		getSupportLoaderManager().restartLoader(LOADER_ID_SESSIONS, b, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle b) {
		switch (id) {
			case LOADER_ID_SESSIONS:
				int day = PlanningActivity.FILTER_DAY_ONE;
				if (b != null) {
					day = b.getInt(EXTRA_DAY, PlanningActivity.FILTER_DAY_ONE);
				}

				long startTimestamp;
				long endTimestamp;
				switch (day) {
					case PlanningActivity.FILTER_DAY_TWO:
						startTimestamp = 1366956000000L;
						endTimestamp = 1367006400000L;
						break;

					case PlanningActivity.FILTER_DAY_ONE:
					default:
						startTimestamp = 1366869600000L;
						endTimestamp = 1366920000000L;
						break;
				}

				final StringBuilder selection = new StringBuilder(MixItContract.Sessions.START);
				selection.append(">=?");
				selection.append(" AND \"");
				selection.append(MixItContract.Sessions.END);
				selection.append("\"<=?");
				final String[] selectionArgs = { String.valueOf(startTimestamp), String.valueOf(endTimestamp) };

				return new CursorLoader(this, MixItContract.Sessions.CONTENT_URI, MixItContract.Sessions.PROJ_LIST.PROJECTION, selection.toString(),
						selectionArgs, MixItContract.Sessions.PLANNING_FULL_SORT);

			default:
				break;
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		final int id = loader.getId();

		switch (id) {
			case LOADER_ID_SESSIONS:
				createPlanning(cursor);
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		final int id = loader.getId();

		switch (id) {
			case LOADER_ID_SESSIONS:
				clearPlanning();
				break;

			default:
				break;
		}
	}

	protected void createPlanning(Cursor c) {
		if (c == null || !c.moveToFirst()) {
			clearPlanning();
			return;
		}

		addHeader(c);

		while (!c.isAfterLast()) {
			addToPlanning(c);

			c.moveToNext();
		}
	}

	int mNbRoom = 0;

	protected void addHeader(Cursor c) {
		mNbRoom = 0;
		final int nbSessions = c.getCount();

		if (nbSessions > 0) {
			final Context context = getSupportActionBar().getThemedContext();
			final Resources res = context.getResources();
			final LayoutInflater inflater = LayoutInflater.from(context);

			final LinearLayout roomsLayout = new LinearLayout(context);
			final TextView emptyView = new TextView(context);
			emptyView.setMinHeight(res.getDimensionPixelSize(R.dimen.planning_min_height_header));
			final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			roomsLayout.addView(emptyView, 0, lp);
			final LinearLayout.LayoutParams lpRoom = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0F);

			String firstRoom = null;
			String lastRoom = null;

			for (int i = 0; i < nbSessions; i++) {
				if (firstRoom == null) {
					firstRoom = c.getString(MixItContract.Sessions.PROJ_LIST.ROOM_ID);
					lastRoom = firstRoom;
				} else {
					lastRoom = c.getString(MixItContract.Sessions.PROJ_LIST.ROOM_ID);
				}

				if (lastRoom.equalsIgnoreCase(firstRoom)) {
					break;
				}

				mNbRoom++;

				final TextView room = (TextView) inflater.inflate(R.layout.planning_header_room, roomsLayout, false);
				room.setText(lastRoom);
				roomsLayout.addView(room, i + 1, lpRoom);
			}

			mGridPlanning.addView(roomsLayout, lp);
		}
	}

	protected void addWelcome() {
		if (mNbRoom > 0) {
			final Context context = getSupportActionBar().getThemedContext();
			final Resources res = context.getResources();
			final LayoutInflater inflater = LayoutInflater.from(context);

			final LinearLayout welcomLayout = new LinearLayout(context);
			final TextView timeView = (TextView) inflater.inflate(R.layout.planning_time, welcomLayout, false);
			final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			welcomLayout.addView(timeView, 0, lp);
			final LinearLayout.LayoutParams lpRoom = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0F);

			final String welcome = res.getString(R.string.planning_welcome);

			for (int i = 0; i < mNbRoom; i++) {
				final TextView room = (TextView) inflater.inflate(R.layout.planning_welcome, welcomLayout, false);
				room.setText(welcome);
				welcomLayout.addView(room, i + 1, lpRoom);
			}

			mGridPlanning.addView(welcomLayout, lp);
		}
	}

	protected void addToPlanning(Cursor c) {
		final Context context = getSupportActionBar().getThemedContext();

		final TalkItemView talkView = new TalkItemView(context);
		talkView.setContent(c);

		// final GridLayout.Spec rowSpec = GridLayout.spec(start);
		// final GridLayout.Spec colSpec = GridLayout.spec(start);
		//
		// final GridLayout.LayoutParams lp = new GridLayout.LayoutParams(rowSpec, colSpec);
		// mGridPlanning.addView(talkView, lp);
	}

	protected void clearPlanning() {
		mGridPlanning.removeAllViews();
	}

}
