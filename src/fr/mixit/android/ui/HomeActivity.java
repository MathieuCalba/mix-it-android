package fr.mixit.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

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
	protected ProgressBar mProgressBar;
	protected TextView mInstruction;
	protected ActionBarTabsAdapter mTabsAdapter;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mProgressBar = (ProgressBar) findViewById(R.id.list_progress);
		mInstruction = (TextView) findViewById(R.id.instruction);

		final long lastSync = PrefUtils.getLastRemoteSync(this);
		if (lastSync == 0L) {
			showProgress();
		} else {
			initTabs();

			final ActionBar bar = getSupportActionBar();
			if (savedStateInstance != null) {
				bar.setSelectedNavigationItem(savedStateInstance.getInt(STATE_CURRENT_TAB, 0));
			}
		}
	}

	@Override
	protected void initActionBar() {
		super.initActionBar();

		final ActionBar bar = getSupportActionBar();
		bar.setIcon(R.drawable.ic_action_bar_bis);
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_home;
	}

	protected void showProgress() {
		mViewPager.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
		mInstruction.setVisibility(View.VISIBLE);
	}

	protected void showContent() {
		mProgressBar.setVisibility(View.GONE);
		mInstruction.setVisibility(View.GONE);
		if (mViewPager.getVisibility() != View.VISIBLE) {
			mViewPager.setVisibility(View.VISIBLE);
			initTabs();
		}
	}

	protected void initTabs() {
		final ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mTabsAdapter = new ActionBarTabsAdapter(this, mViewPager);

		mTabsAdapter.addTab(bar.newTab().setText(getString(R.string.my_planning_tab)), MyPlanningFragment.class, null);
		mTabsAdapter.addTab(bar.newTab().setText(getString(R.string.explore_tab)), ExploreFragment.class, null);
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
					break;

				case MixItService.Response.STATUS_NO_CONNECTIVITY:
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

}
