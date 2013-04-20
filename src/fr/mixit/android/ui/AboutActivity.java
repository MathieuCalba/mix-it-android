package fr.mixit.android.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import fr.mixit.android.ui.fragments.AboutFragment;
import fr.mixit.android_2012.R;

public class AboutActivity extends GenericMixItActivity {

	AboutFragment mAboutFrag;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		final FragmentManager fm = getSupportFragmentManager();
		mAboutFrag = (AboutFragment) fm.findFragmentByTag(AboutFragment.TAG);
		if (mAboutFrag == null) {
			mAboutFrag = AboutFragment.newInstance(getIntent());
			fm.beginTransaction().add(R.id.content_about, mAboutFrag, AboutFragment.TAG).commit();
		}
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_about;
	}

}
