/*
 * Copyright 2011 - 2013 Mathieu Calba
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.mixit.android.ui.fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.keyboardsurfer.android.widget.crouton.Style;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.tasks.SessionAsyncTaskLoader;
import fr.mixit.android.ui.PlanningActivity;
import fr.mixit.android.ui.adapters.SessionsAdapter;
import fr.mixit.android.ui.widgets.TalkItemView;
import fr.mixit.android.utils.PrefUtils;
import fr.mixit.android_2012.R;


public class PlanningRoomPageFragment extends BoundServiceFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
WarningStarSessionDialogFragment.WarningStarSessionDialogContract, SessionAsyncTaskLoader.StarSessionListener {

	protected static final String EXTRA_ROOM = "fr.mixit.android.EXTRA_ROOM";
	protected static final String EXTRA_DAY = "fr.mixit.android.EXTRA_DAY";
	protected static final String EXTRA_POSITION = "fr.mixit.android.EXTRA_POSITION";

	protected static final int LOADER_ID_SESSIONS = 1304092000;

	protected ListView mListView;
	protected SessionsAdapter mAdapter;

	protected String mRoom;
	protected int mDay;
	protected int mPosition;
	protected boolean mShouldRetain = true;

	public static PlanningRoomPageFragment newInstance(String room, int day, int position) {
		final PlanningRoomPageFragment f = new PlanningRoomPageFragment();
		final Bundle args = new Bundle();
		args.putString(EXTRA_ROOM, room);
		args.putInt(EXTRA_DAY, day);
		args.putInt(EXTRA_POSITION, position);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle args = getArguments();
		if (args == null) {
			throw new IllegalArgumentException("You must create the " + PlanningRoomPageFragment.class.getSimpleName()
					+ " using PlanningRoomPageFragment#newInstance(String room)");
		}

		mRoom = args.getString(EXTRA_ROOM);

		if (TextUtils.isEmpty(mRoom)) {
			throw new IllegalArgumentException("You must create the " + PlanningRoomPageFragment.class.getSimpleName()
					+ " using PlanningRoomPageFragment#newInstance(String room)");
		}

		mDay = args.getInt(EXTRA_DAY, PlanningActivity.FILTER_DAY_ONE);

		mPosition = args.getInt(EXTRA_POSITION, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_planning_room_page, container, false);

		mListView = (ListView) root.findViewById(android.R.id.list);

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new SessionsAdapter(getActivity(), true);
		mAdapter.setStarListener(new TalkItemView.StarListener() {

			@Override
			public void onStarTouched(String idTalk, String titleTalk, boolean state) {
				favoriteSessionWithDialog(idTalk, titleTalk, state);
			}
		});

		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

		if (savedInstanceState != null) {
			mDay = savedInstanceState.getInt(EXTRA_DAY, mDay);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		loadSession(mDay, mRoom);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mShouldRetain) {
			outState.putInt(EXTRA_DAY, mDay);
		}
	}

	public void setShouldRetainSlots(boolean shouldRetain) {
		mShouldRetain = shouldRetain;
	}

	public void updateDay(int day) {
		mDay = day;

		loadSession(mDay, mRoom);
	}

	protected void loadSession(int day, String room) {
		final Bundle b = new Bundle();
		b.putInt(EXTRA_DAY, day);
		b.putString(EXTRA_ROOM, room);

		restartLoader(LOADER_ID_SESSIONS + mPosition, b, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final int realId = id - mPosition;
		switch (realId) {
			case LOADER_ID_SESSIONS:
				int day = PlanningActivity.FILTER_DAY_ONE;
				String room = null;
				if (args != null) {
					day = args.getInt(EXTRA_DAY, PlanningActivity.FILTER_DAY_ONE);
					room = args.getString(EXTRA_ROOM);
				}

				if (TextUtils.isEmpty(room)) {
					room = "";
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

				final StringBuilder selection = new StringBuilder(MixItContract.Sessions.ROOM_ID);
				selection.append("=?");
				selection.append(" AND ");
				selection.append(MixItContract.Sessions.START);
				selection.append(">=?");
				selection.append(" AND \"");
				selection.append(MixItContract.Sessions.END);
				selection.append("\"<=?");
				final String[] selectionArgs = { room, String.valueOf(startTimestamp), String.valueOf(endTimestamp) };

				return new CursorLoader(getActivity(), MixItContract.Sessions.CONTENT_URI, MixItContract.Sessions.PROJ_LIST.PROJECTION, selection.toString(),
						selectionArgs, MixItContract.Sessions.PLANNING_SORT);

			default:
				break;
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		final int id = loader.getId();
		final int realId = id - mPosition;
		switch (realId) {
			case LOADER_ID_SESSIONS:
				mAdapter.swapCursor(cursor);
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		final int id = loader.getId();
		final int realId = id - mPosition;
		switch (realId) {
			case LOADER_ID_SESSIONS:
				mAdapter.swapCursor(null);
				break;

			default:
				break;
		}
	}

	@Override
	protected void onMessageReceivedFromService(Message msg) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Cursor cursor = (Cursor) mAdapter.getItem(position);
		final String sessionId = cursor.getString(MixItContract.Sessions.PROJ_LIST.SESSION_ID);
		final Uri sessionUri = MixItContract.Sessions.buildSessionUri(sessionId);
		final Intent intent = new Intent(Intent.ACTION_VIEW, sessionUri);
		startActivity(intent);

		mListView.setItemChecked(position, true);
	}

	protected void favoriteSessionWithDialog(String sessionId, String sessionTitle, boolean addFavorite) {
		final boolean isWarningStarSessionShouldBeShown = PrefUtils.isWarningStarSessionShouldBeShown(getActivity());
		if (isWarningStarSessionShouldBeShown) {
			final WarningStarSessionDialogFragment frag = WarningStarSessionDialogFragment.newInstance(sessionId, sessionTitle, addFavorite);
			frag.setTargetFragment(this, WarningStarSessionDialogFragment.WARNING_NO_SYNC_STAR_SESSION);
			frag.show(getFragmentManager(), WarningStarSessionDialogFragment.TAG);
		} else {
			favoriteSession(sessionId, sessionTitle, addFavorite);
		}
	}

	protected void favoriteSession(String sessionId, String sessionTitle, boolean addFavorite) {
		if (getActivity() != null && !isDetached()) {
			final ContentResolver cr = getActivity().getContentResolver();
			new SessionAsyncTaskLoader(cr).starSession(sessionId, sessionTitle, addFavorite, this);
		}
	}

	@Override
	public void onWarningClickOk(String sessionId, String sessionTitle, boolean vote) {
		PrefUtils.setWarningStarSessionShouldBeShown(getActivity(), false);

		favoriteSession(sessionId, sessionTitle, vote);
	}

	@Override
	public void onStarSessionSuccessfull(String sessionId, String sessionTitle, boolean vote) {
		showCrouton(getString(vote ? R.string.star_session_success : R.string.unstar_session_success, sessionTitle), Style.CONFIRM);
	}

	@Override
	public void onStarSessionFailed(String sessionId, String sessionTitle, boolean vote, String error) {
		showCrouton(getString(vote ? R.string.star_session_failed : R.string.unstar_session_failed, sessionTitle), Style.ALERT);
	}

}
