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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.keyboardsurfer.android.widget.crouton.Style;
import fr.mixit.android.model.PlanningSlot;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.services.MixItService;
import fr.mixit.android.ui.SessionsActivity;
import fr.mixit.android.ui.adapters.MyPlanningAdapter;
import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;


public class MyPlanningFragment extends BoundServiceFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
WarningImportStarredSessionDialogFragment.WarningImportStarredSessionDialogContract, ImportStarredDialogFragment.ImportStarredDialogContract {

	public static final String TAG = MyPlanningFragment.class.getSimpleName();

	protected static final int LOADER_ID_STARRED_SESSIONS = 1304131837;

	protected ViewAnimator mAnimator;
	protected ListView mListView;
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
		final View root = inflater.inflate(R.layout.list_content, container, false);
		mAnimator = (ViewAnimator) root.findViewById(R.id.list_animator);
		mListView = (ListView) root.findViewById(android.R.id.list);
		final TextView emptyV = (TextView) root.findViewById(android.R.id.empty);
		emptyV.setText(R.string.empty_my_planning);
		emptyV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showWarningBeforeImportingStarredSession();
			}
		});
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new MyPlanningAdapter(getSherlockActivity().getSupportActionBar().getThemedContext());
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		loadStarredSession();
	}

	protected void loadStarredSession() {
		if (mAnimator.getDisplayedChild() == 2) {
			mAnimator.showNext();
		}

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
					mAdapter.swapCursor(null);
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
					mAdapter.swapCursor(cursor);
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
				mAdapter.swapCursor(null);
				break;

			default:
				break;
		}
	}

	@Override
	protected void onMessageReceivedFromService(Message msg) {
		switch (msg.what) {
			case MixItService.MSG_INIT:
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
				break;

			case MixItService.MSG_GET_STARRED_SESSION:
				Style croutonStyle = null;
				String croutonText = null;
				if (getActivity() != null && !isDetached()) {
					switch (msg.arg1) {
						case MixItService.Response.STATUS_OK:
							croutonStyle = Style.CONFIRM;
							croutonText = getString(R.string.import_starred_state_success);
							break;

						case MixItService.Response.STATUS_ERROR:
							croutonStyle = Style.ALERT;
							croutonText = getString(R.string.import_starred_state_error);
							break;

						case MixItService.Response.STATUS_NO_CONNECTIVITY:
							croutonStyle = Style.ALERT;
							croutonText = getString(R.string.import_starred_state_error);
							break;

						default:
							break;
					}
				}

				if (croutonStyle != null && croutonText != null) {
					showCrouton(croutonText, croutonStyle);
				}

				setRefreshMode(false);
				loadStarredSession();
				break;

			default:
				break;
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
				showWarningBeforeImportingStarredSession();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	protected void showWarningBeforeImportingStarredSession() {
		final WarningImportStarredSessionDialogFragment fragment = WarningImportStarredSessionDialogFragment.newInstance();
		fragment.setTargetFragment(this, WarningImportStarredSessionDialogFragment.WARNING_NO_SYNC_STAR_SESSION);
		fragment.show(getFragmentManager(), WarningImportStarredSessionDialogFragment.TAG);
	}

	@Override
	public void onWarningClickOk() {
		showImportDialog();
	}

	protected void showImportDialog() {
		final ImportStarredDialogFragment fragment = ImportStarredDialogFragment.newInstance();
		fragment.setTargetFragment(this, ImportStarredDialogFragment.SELECT_MEMBER);
		fragment.show(getFragmentManager(), ImportStarredDialogFragment.TAG);
	}

	@Override
	public void onMemberSelect(String memberId) {
		getStarredSessions(memberId);
	}

	@Override
	public void onCancel() {
	}

	protected void getStarredSessions(String memberId) {
		try {
			final int id = Integer.valueOf(memberId);
			getStarredSessions(id);
		} catch (final NumberFormatException e) {
			if (DEBUG_MODE) {
				Log.e(TAG, "Impossible to convert " + memberId + " to an integer");
			}
		}
	}

	protected void getStarredSessions(int memberId) {
		if (mIsBound && mServiceReady) {
			final Message msg = Message.obtain(null, MixItService.MSG_GET_STARRED_SESSION, 0, 0);
			msg.replyTo = mMessenger;
			final Bundle b = new Bundle();
			b.putInt(MixItService.EXTRA_MEMBER_ID, memberId);
			msg.setData(b);
			try {
				mService.send(msg);
				setRefreshMode(true);
			} catch (final RemoteException e) {
				e.printStackTrace();
			}
		}
	}

}
