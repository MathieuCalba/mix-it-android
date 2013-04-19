package fr.mixit.android.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;

import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.ui.fragments.BoundServiceFragment;
import fr.mixit.android.ui.fragments.MemberDetailsFragment;
import fr.mixit.android.ui.fragments.MemberDetailsFragment.MemberDetailsContract;
import fr.mixit.android.ui.fragments.SessionDetailsFragment;
import fr.mixit.android.ui.fragments.SessionDetailsFragment.SessionDetailsContract;
import fr.mixit.android.ui.fragments.SessionsListFragment;
import fr.mixit.android.utils.DateUtils;
import fr.mixit.android.utils.IntentUtils;
import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;


public class SessionsActivity extends GenericMixItActivity implements OnNavigationListener, SessionDetailsContract, MemberDetailsContract,
BoundServiceFragment.BoundServiceContract {

	private static final String TAG = SessionsActivity.class.getSimpleName();

	public static final String EXTRA_MODE = "fr.mixit.android.EXTRA_MODE";
	public static final String EXTRA_SLOT_START = "fr.mixit.android.EXTRA_START";
	public static final String EXTRA_SLOT_END = "fr.mixit.android.EXTRA_END";

	public static final int DISPLAY_MODE_SESSIONS = 1204101927;
	public static final int DISPLAY_MODE_LIGHTNING_TALKS = 1204101928;
	public static final int DISPLAY_MODE_SESSIONS_STARRED = 1204101929;
	public static final int DISPLAY_MODE_SESSIONS_DUPLICATE = 1204101930;

	protected static final String STATE_DISPLAY_MODE = "fr.mixit.android.STATE_DISPLAY_MODE";

	protected SessionsListFragment mSessionsListFrag;
	protected SessionDetailsFragment mSessionDetailsFrag;

	protected int mTopFragCommitId = -1;

	protected int mMode = DISPLAY_MODE_SESSIONS;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		mMode = getIntent().getIntExtra(EXTRA_MODE, DISPLAY_MODE_SESSIONS);

		if (mMode != DISPLAY_MODE_SESSIONS_STARRED && mMode != DISPLAY_MODE_SESSIONS_DUPLICATE) {
			final Context context = getSupportActionBar().getThemedContext();
			final ArrayAdapter<CharSequence> listAdapter = ArrayAdapter.createFromResource(context, R.array.sessions, R.layout.sherlock_spinner_item);
			listAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

			getSupportActionBar().setListNavigationCallbacks(listAdapter, this);
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		}

		if (savedStateInstance != null) {
			mMode = savedStateInstance.getInt(STATE_DISPLAY_MODE, mMode);
		}

		final FragmentManager fm = getSupportFragmentManager();
		mSessionsListFrag = (SessionsListFragment) fm.findFragmentByTag(SessionsListFragment.TAG);
		if (mSessionsListFrag == null) {
			mSessionsListFrag = SessionsListFragment.newInstance(getIntent());
			fm.beginTransaction().add(R.id.content_sessions_list, mSessionsListFrag, SessionsListFragment.TAG).commit();
		}

		if (mMode != DISPLAY_MODE_SESSIONS_STARRED && mMode != DISPLAY_MODE_SESSIONS_DUPLICATE) {
			int itemSelected = 0;
			switch (mMode) {
				case DISPLAY_MODE_SESSIONS:
					itemSelected = 0;
					break;

				case DISPLAY_MODE_LIGHTNING_TALKS:
					itemSelected = 1;
					break;

				default:
					itemSelected = 0;
					break;
			}
			getSupportActionBar().setSelectedNavigationItem(itemSelected);
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		} else if (mMode == DISPLAY_MODE_SESSIONS_DUPLICATE) {
			final ActionBar sab = getSupportActionBar();

			final long slotStart = getIntent().getLongExtra(SessionsActivity.EXTRA_SLOT_START, -1);
			final long slotEnd = getIntent().getLongExtra(SessionsActivity.EXTRA_SLOT_END, -1);

			final String title = DateUtils.formatDayTime(this, slotStart, slotEnd);
			sab.setTitle(title);
			getSupportActionBar().setDisplayShowTitleEnabled(true);
		} else {
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}
		mSessionsListFrag.setDisplayMode(mMode);

		if (UIUtils.isTablet(this)) {
			mSessionDetailsFrag = (SessionDetailsFragment) fm.findFragmentByTag(SessionDetailsFragment.TAG);
			if (mSessionDetailsFrag == null) {
				mSessionDetailsFrag = SessionDetailsFragment.newInstance(getIntent());
				fm.beginTransaction().add(R.id.content_session_details, mSessionDetailsFrag, SessionDetailsFragment.TAG).commit();
			}
		}
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_sessions;
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (itemPosition == 0 && mMode != DISPLAY_MODE_SESSIONS) {
			mMode = DISPLAY_MODE_SESSIONS;
			refresh(-1);
		} else if (itemPosition == 1 && mMode != DISPLAY_MODE_LIGHTNING_TALKS) {
			mMode = DISPLAY_MODE_LIGHTNING_TALKS;
			refresh(-1);
		}
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_DISPLAY_MODE, mMode);
	}

	@Override
	public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
		final Uri uri = intent.getData();
		if (uri != null && uri.getAuthority().equals(MixItContract.Sessions.CONTENT_URI.getAuthority())) {
			final boolean addToBackStack = intent.getBooleanExtra(IntentUtils.EXTRA_FROM_ADD_TO_BACKSTACK, false);
			final FragmentManager fm = getSupportFragmentManager();
			// SESSION
			if (uri.getEncodedPath().startsWith(SLASH + MixItContract.PATH_SESSIONS) || uri.getEncodedPath().startsWith(SLASH + MixItContract.PATH_LIGHTNINGS)) {
				if (UIUtils.isTablet(this)) {
					if (addToBackStack) {
						final SessionDetailsFragment frag = SessionDetailsFragment.newInstance(intent);
						final FragmentTransaction ft = fm.beginTransaction();
						ft.replace(R.id.content_session_details, frag);
						ft.addToBackStack(null);
						if (mTopFragCommitId == -1) {
							mTopFragCommitId = ft.commit();
						} else {
							ft.commit();
						}
						return;
					} else {
						if (mTopFragCommitId != -1) {
							fm.popBackStack(mTopFragCommitId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
							mTopFragCommitId = -1;
						}
						if (mSessionDetailsFrag != null) {
							final int sessionId = Integer.parseInt(MixItContract.Sessions.getSessionId(uri));
							mSessionDetailsFrag.setSessionId(sessionId);
							return;
						} else {
							Log.e(TAG, "no fragment session details found but device is tablet");
						}
					}
				} else {
					super.startActivityFromFragment(fragment, intent, requestCode);
					return;
				}
			} else
				// MEMBERS
				if (uri.getEncodedPath().startsWith(SLASH + MixItContract.PATH_MEMBERS) || uri.getEncodedPath().startsWith(SLASH + MixItContract.PATH_SPEAKERS)) {
					if (UIUtils.isTablet(this)) {
						final MemberDetailsFragment frag = MemberDetailsFragment.newInstance(intent);
						final FragmentTransaction ft = fm.beginTransaction();
						ft.replace(R.id.content_session_details, frag);
						ft.addToBackStack(null);
						if (mTopFragCommitId == -1) {
							mTopFragCommitId = ft.commit();
						} else {
							ft.commit();
						}
						return;
					} else {
						super.startActivityFromFragment(fragment, intent, requestCode);
						return;
					}
				}
		}
		super.startActivityFromFragment(fragment, intent, requestCode);
	}

	protected void refresh(int sessionId) {
		mSessionsListFrag.setDisplayMode(mMode);
		mSessionsListFrag.reload();

		if (mSessionDetailsFrag != null) {
			mSessionDetailsFrag.setSessionId(sessionId);
		}
	}

	@Override
	public void refreshMenu() {
		supportInvalidateOptionsMenu();
	}

	@Override
	public void refreshList() {
		mSessionsListFrag.reload();
	}

	@Override
	public void setActionBarTitle(String title) {
		// Nothing to
	}

}
