package fr.mixit.android.ui;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.widget.TabHost;
import fr.mixit.android.services.MixItService;
import fr.mixit.android.ui.adapters.TabsAdapter;
import fr.mixit.android.ui.fragments.BoundServiceFragment;
import fr.mixit.android.ui.fragments.ExploreFragment;
import fr.mixit.android.ui.fragments.MyPlanningFragment;
import fr.mixit.android_2012.R;


public class HomeActivity extends GenericMixItActivity implements BoundServiceFragment.BoundServiceContract {

	protected static final String TAB_MY_PLANNING = "TAB_MY_PLANNING";
	protected static final String TAB_EXPLORE = "TAB_EXPLORE";

	protected static final String STATE_CURRENT_TAB = "fr.mixit.android.STATE_CURRENT_TAB";

	protected TabHost mTabHost;
	protected ViewPager mViewPager;
	protected TabsAdapter mTabsAdapter;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager) findViewById(R.id.pager);

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);

		initTabs();

		if (savedStateInstance != null) {
			mTabHost.setCurrentTabByTag(savedStateInstance.getString(STATE_CURRENT_TAB));
		}
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_home;
	}

	protected void initTabs() {
		mTabsAdapter.addTab(mTabHost.newTabSpec(TAB_MY_PLANNING).setIndicator(getString(R.string.my_planning_tab)), MyPlanningFragment.class, null);
		mTabsAdapter.addTab(mTabHost.newTabSpec(TAB_EXPLORE).setIndicator(getString(R.string.my_planning_tab)), ExploreFragment.class, null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_CURRENT_TAB, mTabHost.getCurrentTabTag());
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
	protected void onMessageReceivedFromService(android.os.Message msg) {
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

			setRefreshMode(false);
		}
	}

}
