package fr.mixit.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import fr.mixit.android.services.MixItService;
import fr.mixit.android_2012.R;


public class ImportStarredDialogFragment extends BoundServiceDialogFragment {

	public static final String TAG = ImportStarredDialogFragment.class.getSimpleName();

	public static ImportStarredDialogFragment newInstance() {
		final ImportStarredDialogFragment fragment = new ImportStarredDialogFragment();
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.warning_star_session_not_sync_with_site).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				getStarredSessions(7);
			}
		});
		return builder.create();
	}

	protected void getStarredSessions(int memberId) {
		if (mIsBound && mServiceReady) {
			final Message msg = Message.obtain(null, MixItService.MSG_GET_STARRED_SESSION, 0, 0);
			msg.replyTo = mMessenger;
			final Bundle b = new Bundle();
			b.putInt(MixItService.EXTRA_MEMBER_ID, memberId);
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
	protected void onMessageReceivedFromService(Message msg) {
		if (msg.what == MixItService.MSG_GET_STARRED_SESSION) {
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
