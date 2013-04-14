package fr.mixit.android.ui.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewAnimator;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.services.MixItService;
import fr.mixit.android.ui.SessionsActivity;
import fr.mixit.android.ui.adapters.SessionsAdapter;
import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;


public class SessionsListFragment extends BoundServiceFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

	public static final String TAG = SessionsListFragment.class.getSimpleName();

	protected static final int CURSOR_SESSIONS = 1002;

	protected static final String STATE_CHECKED_POSITION = "fr.mixit.android.STATE_CHECKED_POSITION";

	protected ViewAnimator mAnimator;
	protected ListView mListView;
	protected SessionsAdapter mAdapter;

	protected int mCheckedPosition = -1;
	protected int mMode = SessionsActivity.DISPLAY_MODE_SESSIONS;
	boolean mIsFirstLoad = true;

	public static SessionsListFragment newInstance(Intent intent) {
		final SessionsListFragment f = new SessionsListFragment();
		f.setArguments(UIUtils.intentToFragmentArguments(intent));
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.list_content, container, false);
		mAnimator = (ViewAnimator) root.findViewById(R.id.list_animator);
		mListView = (ListView) root.findViewById(android.R.id.list);
		((TextView) root.findViewById(android.R.id.empty)).setText(R.string.empty_sessions);
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		if (savedInstanceState != null) {
			mCheckedPosition = savedInstanceState.getInt(STATE_CHECKED_POSITION, -1);
		}

		mAdapter = new SessionsAdapter(getActivity(),
				!(mMode == SessionsActivity.DISPLAY_MODE_SESSIONS_STARRED || mMode == SessionsActivity.DISPLAY_MODE_LIGHTNING_TALKS));
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		reload();
		// if (mIsFirstLoad) {
		// refreshSessionsData();
		// }
	}

	public void reload() {
		restartLoader(CURSOR_SESSIONS, getArguments(), this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_CHECKED_POSITION, mCheckedPosition);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == CURSOR_SESSIONS) {
			final Intent i = UIUtils.fragmentArgumentsToIntent(args);
			Uri sessionsUri = i.getData();
			if (sessionsUri == null) {
				sessionsUri = mMode == SessionsActivity.DISPLAY_MODE_LIGHTNING_TALKS ? MixItContract.Sessions.CONTENT_URI_LIGNTHNING
						: MixItContract.Sessions.CONTENT_URI;
			}

			final StringBuilder selection = new StringBuilder();
			if (mMode == SessionsActivity.DISPLAY_MODE_SESSIONS_STARRED) {
				selection.append(MixItContract.Sessions.IS_FAVORITE);
				selection.append("=1");
			}
			if (mMode == SessionsActivity.DISPLAY_MODE_SESSIONS_DUPLICATE) {
				long slotStart = 0;
				long slotEnd = 0;
				if (args != null) {
					slotStart = args.getLong(SessionsActivity.EXTRA_SLOT_START, -1);
					slotEnd = args.getLong(SessionsActivity.EXTRA_SLOT_END, -1);
				}

				if (slotStart == -1 || slotEnd == -1) {
					return null;
				}

				selection.append(MixItContract.Sessions.START);
				selection.append('<');
				selection.append(slotEnd);
				selection.append(" AND \"");
				selection.append(MixItContract.Sessions.END);
				selection.append("\">");
				selection.append(slotStart);
			}

			return new CursorLoader(getActivity(), sessionsUri, MixItContract.Sessions.PROJ_LIST.PROJECTION, selection.toString(), null,
					MixItContract.Sessions.DEFAULT_SORT);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		final int id = loader.getId();
		if (id == CURSOR_SESSIONS) {
			if (data == null) {
				switch (mAnimator.getDisplayedChild()) {
					case 0:
						mAnimator.showPrevious();
						break;

					case 1:
						mAnimator.showNext();
						break;

					default:
						break;
				}
			} else {
				switch (mAnimator.getDisplayedChild()) {
					case 0:
						mAnimator.showNext();
						break;

					case 2:
						mAnimator.showPrevious();
						break;

					default:
						break;
				}
			}
			mAdapter.swapCursor(data);
			mAdapter.setDisplayStar(!(mMode == SessionsActivity.DISPLAY_MODE_SESSIONS_STARRED || mMode == SessionsActivity.DISPLAY_MODE_LIGHTNING_TALKS));

			if (mCheckedPosition >= 0 && getView() != null) {
				mListView.setItemChecked(mCheckedPosition, true);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		final int id = loader.getId();
		if (id == CURSOR_SESSIONS) {
			mAdapter.swapCursor(null);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		final Cursor cursor = (Cursor) mAdapter.getItem(position);
		final String sessionId = cursor.getString(MixItContract.Sessions.PROJ_LIST.SESSION_ID);
		final Uri sessionUri = MixItContract.Sessions.buildSessionUri(sessionId);
		final Intent intent = new Intent(Intent.ACTION_VIEW, sessionUri);
		startActivity(intent);

		mListView.setItemChecked(position, true);
		mCheckedPosition = position;
	}

	protected void refreshSessionsData() {
		if (mIsBound && mServiceReady) {
			Message msg = null;
			if (mMode == SessionsActivity.DISPLAY_MODE_SESSIONS) {
				msg = Message.obtain(null, MixItService.MSG_TALKS, 0, 0);
			} else if (mMode == SessionsActivity.DISPLAY_MODE_LIGHTNING_TALKS) {
				msg = Message.obtain(null, MixItService.MSG_LIGHTNING_TALKS, 0, 0);
			}
			if (msg != null) {
				setRefreshMode(true);

				msg.replyTo = mMessenger;
				final Bundle b = new Bundle();
				msg.setData(b);
				try {
					mService.send(msg);
				} catch (final RemoteException e) {
					e.printStackTrace();
				}

				mIsFirstLoad = false;
			} else {
				setRefreshMode(false);
			}
		}
	}

	@Override
	protected void onMessageReceivedFromService(Message msg) {
		if (msg.what == MixItService.MSG_TALKS || msg.what == MixItService.MSG_LIGHTNING_TALKS) {
			setRefreshMode(false);
			switch (msg.arg1) {
				case MixItService.Response.STATUS_OK:
					// normally don't need to do that because cursor is automatically refreshed thanks to the CursorLoader and the ContentProvider notifying
					// cursor of change in their data by uri
					// restartLoader(CURSOR_TALKS, getArguments(), this);
					break;

				case MixItService.Response.STATUS_ERROR:
					break;

				case MixItService.Response.STATUS_NO_CONNECTIVITY:
					break;

				default:
					break;
			}
		}
	}

	public void clearCheckedPosition() {
		if (mCheckedPosition >= 0) {
			mListView.setItemChecked(mCheckedPosition, false);
			mCheckedPosition = -1;
		}
	}

	public void setDisplayMode(int displayMode) {
		mMode = displayMode;

		mIsFirstLoad = true;

		clearCheckedPosition();
	}

}
