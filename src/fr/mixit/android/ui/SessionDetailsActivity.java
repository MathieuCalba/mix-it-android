package fr.mixit.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import fr.mixit.android.ui.fragments.BoundServiceFragment;
import fr.mixit.android.ui.fragments.SessionDetailsFragment;
import fr.mixit.android.ui.fragments.SessionDetailsFragment.SessionDetailsContract;
import fr.mixit.android_2012.R;


public class SessionDetailsActivity extends GenericMixItActivity implements SessionDetailsContract, BoundServiceFragment.BoundServiceContract {

	protected SessionDetailsFragment mSessionDetailsFrag;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		final FragmentManager fm = getSupportFragmentManager();
		mSessionDetailsFrag = (SessionDetailsFragment) fm.findFragmentByTag(SessionDetailsFragment.TAG);
		if (mSessionDetailsFrag == null) {
			mSessionDetailsFrag = SessionDetailsFragment.newInstance(getIntent());
			fm.beginTransaction().add(R.id.content_session_details, mSessionDetailsFrag, SessionDetailsFragment.TAG).commit();
		}
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_sessions_details;
	}

	@Override
	public void refreshMenu() {
		supportInvalidateOptionsMenu();
	}

	@Override
	public void refreshList() {
		// Nothing to do because there is no list to refresh in this activity
	}

	@Override
	protected int getActivityLevel() {
		return 2;
	}

	@Override
	protected Intent getParentIntent(int level) {
		switch (level) {
			case 1:
				return new Intent(this, SessionsActivity.class);

			default:
				return super.getParentIntent(level);
		}
	}

}
