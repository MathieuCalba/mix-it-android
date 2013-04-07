
package fr.mixit.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import fr.mixit.android.services.MixItService;
import fr.mixit.android_2012.R;

public class HomeActivity extends GenericMixItActivity {

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);

		findViewById(R.id.session_bt).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final Intent i = new Intent(HomeActivity.this, SessionsActivity.class);
				startActivity(i);
			}
		});

		findViewById(R.id.speaker_bt).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final Intent i = new Intent(HomeActivity.this, MembersActivity.class);
				startActivity(i);
			}
		});
	}

	@Override
	protected int getContentLayoutId() {
		return R.layout.activity_home;
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
