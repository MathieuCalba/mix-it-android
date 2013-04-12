package fr.mixit.android.ui.fragments;

import android.database.Cursor;
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
import android.widget.ListView;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.ui.adapters.SessionsAdapter;
import fr.mixit.android_2012.R;


public class PlanningSlotPageFragment extends BoundServiceFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

	protected static final String EXTRA_SLOT_START = "fr.mixit.android.EXTRA_SLOT_START";
	protected static final String EXTRA_SLOT_END = "fr.mixit.android.EXTRA_SLOT_END";
	protected static final String EXTRA_POSITION = "fr.mixit.android.EXTRA_POSITION";

	protected static final int LOADER_ID_SESSIONS = 1304112000;

	protected ListView mListView;
	protected SessionsAdapter mAdapter;

	protected long mSlotStart;
	protected long mSlotEnd;
	protected int mPosition;
	protected boolean mShouldRetain = true;

	public static PlanningSlotPageFragment newInstance(long slotStart, long slotEnd, int position) {
		final PlanningSlotPageFragment f = new PlanningSlotPageFragment();
		final Bundle args = new Bundle();
		args.putLong(EXTRA_SLOT_START, slotStart);
		args.putLong(EXTRA_SLOT_END, slotEnd);
		args.putInt(EXTRA_POSITION, position);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle args = getArguments();
		if (args == null) {
			throw new IllegalArgumentException("You must create the " + PlanningSlotPageFragment.class.getSimpleName()
					+ " using PlanningSlotPageFragment#newInstance(long slotStart, long slotEnd, int day, int position)");
		}

		mSlotStart = args.getLong(EXTRA_SLOT_START, -1);
		mSlotEnd = args.getLong(EXTRA_SLOT_END, -1);

		if (mSlotStart == -1 || mSlotEnd == -1) {
			throw new IllegalArgumentException("You must create the " + PlanningSlotPageFragment.class.getSimpleName()
					+ " using PlanningSlotPageFragment#newInstance(long slotStart, long slotEnd, int day, int position)");
		}

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
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

		if (savedInstanceState != null) {
			mSlotStart = savedInstanceState.getLong(EXTRA_SLOT_START, mSlotStart);
			mSlotEnd = savedInstanceState.getLong(EXTRA_SLOT_END, mSlotEnd);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		loadSession(mSlotStart, mSlotEnd);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mShouldRetain) {
			outState.putLong(EXTRA_SLOT_START, mSlotStart);
			outState.putLong(EXTRA_SLOT_END, mSlotEnd);
		}
	}

	public void setShouldRetainSlots(boolean shouldRetain) {
		mShouldRetain = shouldRetain;
	}

	public void updateSlots(long slotStart, long slotEnd) {
		mSlotStart = slotStart;
		mSlotEnd = slotEnd;

		loadSession(slotStart, slotEnd);
	}

	protected void loadSession(long slotStart, long slotEnd) {
		final Bundle b = new Bundle();
		b.putLong(EXTRA_SLOT_START, slotStart);
		b.putLong(EXTRA_SLOT_END, slotEnd);

		restartLoader(LOADER_ID_SESSIONS + mPosition, b, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final int realId = id - mPosition;
		switch (realId) {
			case LOADER_ID_SESSIONS:
				long slotStart = 0;
				long slotEnd = 0;
				if (args != null) {
					slotStart = args.getLong(EXTRA_SLOT_START, -1);
					slotEnd = args.getLong(EXTRA_SLOT_END, -1);
				}

				if (slotStart == -1 || slotEnd == -1) {
					return null;
				}

				final StringBuilder selection = new StringBuilder(MixItContract.Sessions.START);
				selection.append("<? AND \"");
				selection.append(MixItContract.Sessions.END);
				selection.append("\">?");
				final String[] selectionArgs = { String.valueOf(slotEnd), String.valueOf(slotStart) };

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
	}

}
