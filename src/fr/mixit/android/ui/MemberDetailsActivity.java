package fr.mixit.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import fr.mixit.android.ui.fragments.BoundServiceFragment;
import fr.mixit.android.ui.fragments.MemberDetailsFragment;
import fr.mixit.android_2012.R;


public class MemberDetailsActivity extends GenericMixItActivity implements /* MemberDetailsContract, */BoundServiceFragment.BoundServiceContract {

	protected MemberDetailsFragment mMemberDetailsFrag;

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		final FragmentManager fm = getSupportFragmentManager();
		mMemberDetailsFrag = (MemberDetailsFragment) fm.findFragmentByTag(MemberDetailsFragment.TAG);
		if (mMemberDetailsFrag == null) {
			mMemberDetailsFrag = MemberDetailsFragment.newInstance(getIntent());
			fm.beginTransaction().add(R.id.content_member_details, mMemberDetailsFrag, MemberDetailsFragment.TAG).commit();
		}
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_members_details;
	}

	// @Override
	// public void startActivityFromFragment(Fragment fragment, Intent intent,
	// int requestCode) {
	// if (UIUtils.isTablet(this)) {
	// Log.e(TAG, "How are we here ?");
	// // if (mMemberDetailsFrag != null) {
	// // Uri memberUri = intent.getData();
	// // int memberId;
	// // if (memberUri != null) {
	// // memberId = Integer.parseInt(MixItContract.Members.getMemberId(memberUri));
	// // mMemberDetailsFrag.setMemberId(memberId);
	// // } else {
	// // Log.e(TAG, "no uri found");
	// // }
	// // } else {
	// // Log.e(TAG, "no fragment member details found but device is tablet");
	// // }
	// } else {
	// super.startActivityFromFragment(fragment, intent, requestCode);
	// }
	// }

	public Intent getParentIntent() {
		return new Intent(this, MembersActivity.class);
	}

	public Intent getGrandParentIntent() {
		return new Intent(this, HomeActivity.class);
	}

	public Intent getGreatGrandParentIntent() {
		return null;
	}

}