package fr.mixit.android.ui.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.ui.adapters.MyPlanningAdapter;
import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;


public class MyPlanningFragment extends BoundServiceFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final String TAG = MyPlanningFragment.class.getSimpleName();

	protected static final int LOADER_ID_STARRED_SESSIONS = 1304131837;

	protected StickyListHeadersListView mListView;
	protected MyPlanningAdapter mAdapter;

	public static MyPlanningFragment newInstance(Intent intent) {
		final MyPlanningFragment f = new MyPlanningFragment();
		f.setArguments(UIUtils.intentToFragmentArguments(intent));
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_my_planning, container, false);
		mListView = (StickyListHeadersListView) root.findViewById(R.id.planning_list);
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new MyPlanningAdapter(getActivity());
		mListView.setAdapter(mAdapter);
	}

	@Override
	public void onStart() {
		super.onStart();

		loadStarredSession();
	}

	protected void loadStarredSession() {
		restartLoader(LOADER_ID_STARRED_SESSIONS, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case LOADER_ID_STARRED_SESSIONS:
				return new CursorLoader(getActivity(), MixItContract.Sessions.CONTENT_URI_PLANNING, MixItContract.Sessions.PROJ_PLANNING.PROJECTION, null,
						null, MixItContract.Sessions.PLANNING_FULL_SORT);

			default:
				break;
		}

		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		final int id = loader.getId();

		switch (id) {
			case LOADER_ID_STARRED_SESSIONS:
				if (cursor == null || !cursor.moveToFirst()) {
					clearPlanning();
					// TODO : invite user to import his favorite talks
				} else {
					displayPlanning(cursor);
				}
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		final int id = loader.getId();

		switch (id) {
			case LOADER_ID_STARRED_SESSIONS:
				clearPlanning();
				break;

			default:
				break;
		}
	}

	protected void clearPlanning() {
		mAdapter.swapCursor(null);
	}

	protected void displayPlanning(Cursor c) {
		mAdapter.swapCursor(c);
	}

	@Override
	protected void onMessageReceivedFromService(Message msg) {
	}

}
