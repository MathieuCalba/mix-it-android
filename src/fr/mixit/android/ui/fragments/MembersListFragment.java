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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.nostra13.universalimageloader.core.ImageLoader;

import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.services.MixItService;
import fr.mixit.android.ui.MembersActivity;
import fr.mixit.android.ui.adapters.MembersAdapter;
import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;


public class MembersListFragment extends BoundServiceFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

	public static final String TAG = MembersListFragment.class.getSimpleName();

	protected static final int CURSOR_MEMBERS = 1003;

	protected static final String STATE_CHECKED_POSITION = "fr.mixit.android.STATE_CHECKED_POSITION";

	protected ViewAnimator mAnimator;
	protected ListView mListView;
	protected MembersAdapter mAdapter;

	protected ImageLoader mImageLoader = ImageLoader.getInstance();

	protected int mCheckedPosition = -1;
	protected int mMode = MembersActivity.DISPLAY_MODE_ALL_MEMBERS;
	protected boolean mIsFirstLoad = true;

	public static MembersListFragment newInstance(Intent intent) {
		final MembersListFragment f = new MembersListFragment();
		f.setArguments(UIUtils.intentToFragmentArguments(intent));
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.list_content, container, false);

		mAnimator = (ViewAnimator) root.findViewById(R.id.list_animator);
		mListView = (ListView) root.findViewById(android.R.id.list);
		((TextView) root.findViewById(android.R.id.empty)).setText(R.string.empty_members);

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		// getListView().setDrawingCacheEnabled(false);

		if (savedInstanceState != null) {
			mCheckedPosition = savedInstanceState.getInt(STATE_CHECKED_POSITION, -1);
		}

		mAdapter = new MembersAdapter(getActivity(), mImageLoader);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		reload();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mImageLoader.stop();
		// if (mIsFirstLoad) {
		// refreshMembersData();
		// }
	}

	public void reload() {
		restartLoader(CURSOR_MEMBERS, getArguments(), this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_CHECKED_POSITION, mCheckedPosition);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == CURSOR_MEMBERS) {
			final Intent i = UIUtils.fragmentArgumentsToIntent(args);
			Uri membersUri = i.getData();
			if (membersUri == null) {
				switch (mMode) {
					case MembersActivity.DISPLAY_MODE_ALL_MEMBERS:
						membersUri = MixItContract.Members.CONTENT_URI;
						break;

					case MembersActivity.DISPLAY_MODE_SPEAKERS:
						membersUri = MixItContract.Members.CONTENT_URI_SPEAKERS;
						break;

					case MembersActivity.DISPLAY_MODE_STAFF:
						membersUri = MixItContract.Members.CONTENT_URI_STAFF;
						break;

					case MembersActivity.DISPLAY_MODE_LINKERS: {
						final int memberId = i.getIntExtra(MembersActivity.EXTRA_MEMBER_ID, -1);
						if (memberId == -1) {
							if (DEBUG_MODE) {
								Log.e(TAG, "Member Linkers without member id...");
							}
							return null;
						}

						membersUri = MixItContract.Members.buildLinkersDirUri(String.valueOf(memberId));
						break;
					}

					case MembersActivity.DISPLAY_MODE_LINKS: {
						final int memberId = i.getIntExtra(MembersActivity.EXTRA_MEMBER_ID, -1);
						if (memberId == -1) {
							if (DEBUG_MODE) {
								Log.e(TAG, "Member Linkers without member id...");
							}
							return null;
						}

						membersUri = MixItContract.Members.buildLinksDirUri(String.valueOf(memberId));
						break;
					}

					default:
						break;
				}
			}
			return new CursorLoader(getActivity(), membersUri, MixItContract.Members.PROJ_LIST.PROJECTION, null, null, MixItContract.Members.DEFAULT_SORT);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		final int id = loader.getId();
		if (id == CURSOR_MEMBERS) {
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

			if (mCheckedPosition >= 0 && getView() != null) {
				mListView.setItemChecked(mCheckedPosition, true);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		final int id = loader.getId();
		if (id == CURSOR_MEMBERS) {
			mAdapter.swapCursor(null);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		final Cursor cursor = (Cursor) mAdapter.getItem(position);
		final String memberId = cursor.getString(MixItContract.Members.PROJ_LIST.MEMBER_ID);
		final Uri memberUri = MixItContract.Members.buildMemberUri(memberId);
		final Intent intent = new Intent(Intent.ACTION_VIEW, memberUri);
		startActivity(intent);

		mListView.setItemChecked(position, true);
		mCheckedPosition = position;
	}

	protected void refreshMembersData() {
		if (mIsBound && mServiceReady) {
			setRefreshMode(true);

			final Message msg = Message.obtain(null, MixItService.MSG_MEMBERS, 0, 0);
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

	@Override
	protected void onMessageReceivedFromService(Message msg) {
		if (msg.what == MixItService.MSG_MEMBERS) {
			setRefreshMode(false);
			switch (msg.arg1) {
				case MixItService.Response.STATUS_OK:
					// normally don't need to do that because cursor is automatically refreshed thanks to the CursorLoader and the ContentProvider notifying
					// cursor of change in their data by uri
					// restartLoader(CURSOR_MEMBERS, getArguments(), this);
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
