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
import fr.mixit.android.ui.fragments.BoundServiceFragment.BoundServiceContract;
import fr.mixit.android.ui.fragments.MemberDetailsFragment;
import fr.mixit.android.ui.fragments.MemberDetailsFragment.MemberDetailsContract;
import fr.mixit.android.ui.fragments.MembersListFragment;
import fr.mixit.android.ui.fragments.SessionDetailsFragment;
import fr.mixit.android.ui.fragments.SessionDetailsFragment.SessionDetailsContract;
import fr.mixit.android.utils.IntentUtils;
import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;


public class MembersActivity extends GenericMixItActivity implements OnNavigationListener, MemberDetailsContract, SessionDetailsContract, BoundServiceContract {

	private static final String TAG = MembersActivity.class.getSimpleName();

	public static final int DISPLAY_MODE_ALL_MEMBERS = 1204101929;
	public static final int DISPLAY_MODE_SPEAKERS = 1204101930;
	public static final int DISPLAY_MODE_STAFF = 1204101931;
	public static final int DISPLAY_MODE_LINKS = 1204101932;
	public static final int DISPLAY_MODE_LINKERS = 1204101933;

	public static final String EXTRA_DISPLAY_MODE = "fr.mixit.android.EXTRA_DISPLAY_MODE";
	public static final String EXTRA_MEMBER_ID = "fr.mixit.android.EXTRA_MEMBER_ID";

	protected static final String STATE_DISPLAY_MODE = "fr.mixit.android.STATE_DISPLAY_MODE";

	protected MembersListFragment mMembersListFrag;
	protected MemberDetailsFragment mMemberDetailsFrag;

	protected int mTopFragCommitId = -1;

	protected int mMode = DISPLAY_MODE_SPEAKERS;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		mMode = getIntent().getIntExtra(EXTRA_DISPLAY_MODE, DISPLAY_MODE_SPEAKERS);

		if (savedStateInstance != null) {
			mMode = savedStateInstance.getInt(STATE_DISPLAY_MODE, DISPLAY_MODE_SPEAKERS);
		}

		if (mMode != DISPLAY_MODE_LINKS && mMode != DISPLAY_MODE_LINKERS) {
			final Context context = getSupportActionBar().getThemedContext();
			final ArrayAdapter<CharSequence> listAdapter = ArrayAdapter.createFromResource(context, R.array.members, R.layout.sherlock_spinner_item);
			listAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

			getSupportActionBar().setListNavigationCallbacks(listAdapter, this);
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		}

		final FragmentManager fm = getSupportFragmentManager();
		mMembersListFrag = (MembersListFragment) fm.findFragmentByTag(MembersListFragment.TAG);
		if (mMembersListFrag == null) {
			mMembersListFrag = MembersListFragment.newInstance(getIntent());
			fm.beginTransaction().add(R.id.content_members_list, mMembersListFrag, MembersListFragment.TAG).commit();
		}

		if (mMode != DISPLAY_MODE_LINKS && mMode != DISPLAY_MODE_LINKERS) {
			int itemSelected = 0;
			switch (mMode) {
				case DISPLAY_MODE_ALL_MEMBERS:
					itemSelected = 0;
					break;

				case DISPLAY_MODE_SPEAKERS:
					itemSelected = 1;
					break;

				default:
					itemSelected = 0;
					break;
			}
			getSupportActionBar().setSelectedNavigationItem(itemSelected);
		}

		mMembersListFrag.setDisplayMode(mMode);

		if (UIUtils.isTablet(this)) {
			mMemberDetailsFrag = (MemberDetailsFragment) fm.findFragmentByTag(MemberDetailsFragment.TAG);
			if (mMemberDetailsFrag == null) {
				mMemberDetailsFrag = MemberDetailsFragment.newInstance(getIntent());
				fm.beginTransaction().add(R.id.content_member_details, mMemberDetailsFrag, MemberDetailsFragment.TAG).commit();
			}
		}
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_members;
	}

	@Override
	protected void initActionBar() {
		super.initActionBar();
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (itemPosition == 0 && mMode != DISPLAY_MODE_ALL_MEMBERS) {
			mMode = DISPLAY_MODE_ALL_MEMBERS;
			refresh(-1);
		} else if (itemPosition == 1 && mMode != DISPLAY_MODE_SPEAKERS) {
			mMode = DISPLAY_MODE_SPEAKERS;
			refresh(-1);
		} else if (itemPosition == 2 && mMode != DISPLAY_MODE_STAFF) {
			mMode = DISPLAY_MODE_STAFF;
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
		if (uri != null && uri.getAuthority().equals(MixItContract.Members.CONTENT_URI.getAuthority())) {
			final boolean addToBackStack = intent.getBooleanExtra(IntentUtils.EXTRA_FROM_ADD_TO_BACKSTACK, false);
			final FragmentManager fm = getSupportFragmentManager();
			// MEMBER
			if (uri.getEncodedPath().startsWith(SLASH + MixItContract.PATH_MEMBERS) || uri.getEncodedPath().startsWith(SLASH + MixItContract.PATH_SPEAKERS)) {
				if (UIUtils.isTablet(this)) {
					if (addToBackStack) {
						final MemberDetailsFragment frag = MemberDetailsFragment.newInstance(intent);
						final FragmentTransaction ft = fm.beginTransaction();
						ft.replace(R.id.content_member_details, frag);
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
						if (mMemberDetailsFrag != null) {
							final int memberId = Integer.parseInt(MixItContract.Members.getMemberId(uri));
							mMemberDetailsFrag.setMemberId(memberId);
							return;
						} else {
							Log.e(TAG, "no fragment member details found but device is tablet");
						}
					}
				} else {
					super.startActivityFromFragment(fragment, intent, requestCode);
					return;
				}
			} else
				// SESSIONS
				if (uri.getEncodedPath().startsWith(SLASH + MixItContract.PATH_SESSIONS) || uri.getEncodedPath().startsWith(SLASH + MixItContract.PATH_LIGHTNINGS)) {
					if (UIUtils.isTablet(this)) {
						final SessionDetailsFragment frag = SessionDetailsFragment.newInstance(intent);
						final FragmentTransaction ft = fm.beginTransaction();
						ft.replace(R.id.content_member_details, frag);
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

	protected void refresh(int memberId) {
		mMembersListFrag.setDisplayMode(mMode);
		mMembersListFrag.reload();

		if (mMemberDetailsFrag != null) {
			mMemberDetailsFrag.setMemberId(memberId);
		}
	}

	@Override
	public void refreshMenu() {
		supportInvalidateOptionsMenu();
	}

	@Override
	public void refreshList() {
		// Nothing to do because we don't display the list of sessions
	}

	@Override
	public void setActionBarTitle(String title) {
		// Nothing to
	}

}
