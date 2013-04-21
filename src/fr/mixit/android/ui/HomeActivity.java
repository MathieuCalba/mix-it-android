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

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import fr.mixit.android.services.MixItService;
import fr.mixit.android.ui.adapters.ActionBarTabsAdapter;
import fr.mixit.android.ui.fragments.AboutFragment;
import fr.mixit.android.ui.fragments.BoundServiceFragment;
import fr.mixit.android.ui.fragments.ExploreFragment;
import fr.mixit.android.ui.fragments.MyPlanningFragment;
import fr.mixit.android.utils.PrefUtils;
import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;


public class HomeActivity extends GenericMixItActivity implements BoundServiceFragment.BoundServiceContract {

	protected static final String TAB_MY_PLANNING = "TAB_MY_PLANNING";
	protected static final String TAB_EXPLORE = "TAB_EXPLORE";

	protected static final String STATE_CURRENT_TAB = "fr.mixit.android.STATE_CURRENT_TAB";

	protected ViewPager mViewPager;
	protected LinearLayout mTabletContent;
	protected ProgressBar mProgressBar;
	protected TextView mInstruction;
	protected TextView mError;
	protected ActionBarTabsAdapter mTabsAdapter;
	protected boolean mIsFirstInitDone = false;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mTabletContent = (LinearLayout) findViewById(R.id.content);
		mProgressBar = (ProgressBar) findViewById(R.id.list_progress);
		mInstruction = (TextView) findViewById(R.id.instruction);
		mError = (TextView) findViewById(R.id.error);
		mError.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				init();
				showProgress();
			}
		});

		final long lastSync = PrefUtils.getLastRemoteSync(this);
		mIsFirstInitDone = lastSync != 0L;
		if (!mIsFirstInitDone) {
			showProgress();
		} else {
			showContent(true);

			if (mViewPager != null) {
				final ActionBar bar = getSupportActionBar();
				if (savedStateInstance != null) {
					bar.setSelectedNavigationItem(savedStateInstance.getInt(STATE_CURRENT_TAB, 0));
				}
			}
		}
	}

	@Override
	protected void initActionBar() {
		super.initActionBar();

		final ActionBar bar = getSupportActionBar();
		bar.setIcon(R.drawable.ic_action_bar_bis);
		bar.setDisplayShowTitleEnabled(false);
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_home;
	}

	protected void showProgress() {
		if (mViewPager != null) {
			mViewPager.setVisibility(View.GONE);
		}
		if (mTabletContent != null) {
			mTabletContent.setVisibility(View.GONE);
		}
		mProgressBar.setVisibility(View.VISIBLE);
		mInstruction.setVisibility(View.VISIBLE);
		mError.setVisibility(View.GONE);
	}

	protected void showContent() {
		showContent(false);
	}

	protected void showContent(boolean forceInitTab) {
		mIsFirstInitDone = true;
		mProgressBar.setVisibility(View.GONE);
		mInstruction.setVisibility(View.GONE);
		mError.setVisibility(View.GONE);
		if (mViewPager != null && (mViewPager.getVisibility() != View.VISIBLE || forceInitTab)) {
			mViewPager.setVisibility(View.VISIBLE);
			initTabs();
		} else if (mTabletContent != null && (mTabletContent.getVisibility() != View.VISIBLE || forceInitTab)) {
			mTabletContent.setVisibility(View.VISIBLE);
			initFragments();
		}
	}

	protected void showError() {
		if (mViewPager != null) {
			mViewPager.setVisibility(View.GONE);
		}
		if (mTabletContent != null) {
			mTabletContent.setVisibility(View.GONE);
		}
		mProgressBar.setVisibility(View.GONE);
		mInstruction.setVisibility(View.GONE);
		mError.setVisibility(View.VISIBLE);
	}

	protected void initTabs() {
		final ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mTabsAdapter = new ActionBarTabsAdapter(this, mViewPager);

		mTabsAdapter.addTab(bar.newTab().setText(getString(R.string.my_planning_tab)), MyPlanningFragment.class, null);
		mTabsAdapter.addTab(bar.newTab().setText(getString(R.string.explore_tab)), ExploreFragment.class, null);
	}

	protected void initFragments() {
		final FragmentManager fm = getSupportFragmentManager();
		Fragment mMyPlanningFrag = fm.findFragmentByTag(MyPlanningFragment.TAG);
		if (mMyPlanningFrag == null) {
			mMyPlanningFrag = MyPlanningFragment.newInstance(getIntent());
			fm.beginTransaction().add(R.id.content_my_planning, mMyPlanningFrag, MyPlanningFragment.TAG).commit();
		}

		Fragment mExploreFrag = fm.findFragmentByTag(ExploreFragment.TAG);
		if (mExploreFrag == null) {
			mExploreFrag = ExploreFragment.newInstance(getIntent());
			fm.beginTransaction().add(R.id.content_explore, mExploreFrag, ExploreFragment.TAG).commit();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(STATE_CURRENT_TAB, getSupportActionBar().getSelectedNavigationIndex());
	}

	@Override
	protected void onServiceReady() {
		super.onServiceReady();

		init();
	}

	protected void init() {
		if (mIsBound && mIsServiceReady) {
			final Message msg = Message.obtain(null, MixItService.MSG_INIT, 0, 0);
			msg.replyTo = mMessenger;
			final Bundle b = new Bundle();
			msg.setData(b);
			try {
				mService.send(msg);
				setRefreshMode(true);
				if (!mIsFirstInitDone) {
					showProgress();
				}

			} catch (final RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == R.id.menu_item_about) {
			if (UIUtils.isTablet(this)) {
				final FragmentManager fm = getSupportFragmentManager();
				AboutFragment aboutFrag = (AboutFragment) fm.findFragmentByTag(AboutFragment.TAG);
				if (aboutFrag == null) {
					aboutFrag = AboutFragment.newInstance(getIntent());
				}
				aboutFrag.show(fm, AboutFragment.TAG);
			} else {
				startActivity(new Intent(this, AboutActivity.class));
			}
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onMessageReceivedFromService(android.os.Message msg) {
		if (msg.what == MixItService.MSG_INIT) {
			switch (msg.arg1) {
				case MixItService.Response.STATUS_OK:
					showContent();
					break;

				case MixItService.Response.STATUS_ERROR:
					if (!mIsFirstInitDone) {
						showError();
					}
					break;

				case MixItService.Response.STATUS_NO_CONNECTIVITY:
					if (!mIsFirstInitDone) {
						showError();
					}
					break;

				default:
					break;
			}

			setRefreshMode(false);
		}
	}

	@Override
	protected int getActivityLevel() {
		return 0;
	}

	@Override
	protected void onDestroy() {
		// Workaround until there's a way to detach the Activity from Crouton while there are still some in the Queue.
		Crouton.clearCroutonsForActivity(this);
		super.onDestroy();
	}

}
