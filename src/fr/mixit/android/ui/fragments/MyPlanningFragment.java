package fr.mixit.android.ui.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import fr.mixit.android.model.PlanningSlot;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.services.MixItService;
import fr.mixit.android.ui.SessionsActivity;
import fr.mixit.android.ui.adapters.MyPlanningAdapter;
import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;


public class MyPlanningFragment extends BoundServiceFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
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
		mListView.setOnItemClickListener(this);
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
		if (msg.what == MixItService.MSG_INIT) {
			switch (msg.arg1) {
				case MixItService.Response.STATUS_OK:
					break;

				case MixItService.Response.STATUS_ERROR:
					break;

				case MixItService.Response.STATUS_NO_CONNECTIVITY:
					break;

				default:
					break;
			}

			loadStarredSession();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		Intent intent = null;

		final Cursor cursor = (Cursor) mAdapter.getItem(position);

		final int slotType = cursor.getInt(MixItContract.Sessions.PROJ_PLANNING.SLOT_TYPE);

		switch (slotType) {
			case PlanningSlot.TYPE_SESSION:
				final int nbConcurrentTalks = cursor.getInt(MixItContract.Sessions.PROJ_PLANNING.NB_CONCURRENT_TALKS);

				if (nbConcurrentTalks == 1) {
					final String sessionId = cursor.getString(MixItContract.Sessions.PROJ_PLANNING.SESSION_ID);

					final Uri sessionUri = MixItContract.Sessions.buildSessionUri(sessionId);
					intent = new Intent(Intent.ACTION_VIEW, sessionUri);
				} else {
					final long slotStart = cursor.getLong(MixItContract.Sessions.PROJ_PLANNING.START);
					final long slotEnd = cursor.getLong(MixItContract.Sessions.PROJ_PLANNING.END);

					intent = new Intent(getActivity(), SessionsActivity.class);
					intent.putExtra(SessionsActivity.EXTRA_MODE, SessionsActivity.DISPLAY_MODE_SESSIONS_DUPLICATE);
					intent.putExtra(SessionsActivity.EXTRA_SLOT_START, slotStart);
					intent.putExtra(SessionsActivity.EXTRA_SLOT_END, slotEnd);
				}
				break;

			case PlanningSlot.TYPE_LIGHTNING_TALK:
				intent = new Intent(getActivity(), SessionsActivity.class);
				intent.putExtra(SessionsActivity.EXTRA_MODE, SessionsActivity.DISPLAY_MODE_LIGHTNING_TALKS);

			default:
				break;
		}

		if (intent != null) {
			startActivity(intent);

			mListView.setItemChecked(position, true);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.my_planning, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();

		switch (id) {
			case R.id.menu_item_import_starred_session:
				final ImportStarredDialogFragment fragment = ImportStarredDialogFragment.newInstance();
				fragment.show(getFragmentManager(), ImportStarredDialogFragment.TAG);
				break;

			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}

}
