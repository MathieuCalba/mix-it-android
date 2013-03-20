
package fr.mixit.android.ui;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import fr.mixit.android.services.MixItService;
import fr.mixit.android_2012.R;

public class HomeActivity extends GenericMixItActivity {

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
					// Toast.makeText(this, R.string.error_initialization_application, Toast.LENGTH_SHORT).show();
					break;

				case MixItService.Response.STATUS_NO_CONNECTIVITY:
					// Toast.makeText(this, R.string.error_initialization_application, Toast.LENGTH_SHORT).show();
					break;

				default:
					break;
			}
		}
	}

}
