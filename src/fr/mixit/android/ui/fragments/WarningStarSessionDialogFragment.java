package fr.mixit.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import com.actionbarsherlock.app.SherlockDialogFragment;

import fr.mixit.android_2012.R;


public class WarningStarSessionDialogFragment extends SherlockDialogFragment {

	public static final String TAG = WarningStarSessionDialogFragment.class.getSimpleName();

	public static final int WARNING_NO_SYNC_STAR_SESSION = 1304141822;

	public static final String EXTRA_SESSION_ID = SessionDetailsFragment.EXTRA_SESSION_ID;
	public static final String EXTRA_SESSION_TITLE = "fr.mixit.android.EXTRA_SESSION_TITLE";
	public static final String EXTRA_VOTE = "fr.mixit.android.EXTRA_VOTE";

	protected String mSessionId;
	protected String mSessionTitle;
	protected boolean mVote = true;

	public interface WarningStarSessionDialogContract {
		void onWarningClickOk(String sessionId, String sessionTitle, boolean vote);
	}

	public static WarningStarSessionDialogFragment newInstance(String sessionId, String sessionTitle, boolean vote) {
		final WarningStarSessionDialogFragment fragment = new WarningStarSessionDialogFragment();

		final Bundle args = new Bundle();
		args.putString(EXTRA_SESSION_ID, sessionId);
		args.putString(EXTRA_SESSION_TITLE, sessionTitle);
		args.putBoolean(EXTRA_VOTE, vote);

		fragment.setArguments(args);

		return fragment;
	}

	public WarningStarSessionDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle args = getArguments();
		if (args == null) {
			throw new IllegalArgumentException("You must create a " + WarningStarSessionDialogFragment.class.getSimpleName()
					+ " using WarningStarSessionDialogFragment#newInstance() in order to pass the session id, session title and vote value");
		}

		mVote = args.getBoolean(EXTRA_VOTE, true);
		mSessionId = args.getString(EXTRA_SESSION_ID);
		mSessionTitle = args.getString(EXTRA_SESSION_TITLE);

		if (TextUtils.isEmpty(mSessionId)) {
			throw new IllegalArgumentException("No session id provided. You must create a " + WarningStarSessionDialogFragment.class.getSimpleName()
					+ " using WarningStarSessionDialogFragment#newInstance() in order to pass the session id, session title and vote value");
		}

		if (TextUtils.isEmpty(mSessionTitle)) {
			throw new IllegalArgumentException("No session title provided. You must create a " + WarningStarSessionDialogFragment.class.getSimpleName()
					+ " using WarningStarSessionDialogFragment#newInstance() in order to pass the session id, session title and vote value");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.warning_star_session_not_sync_with_site).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				starSession();
			}
		});
		return builder.create();
	}

	protected void starSession() {
		final Fragment f = getTargetFragment();
		if (f == null) {
			if (SessionDetailsFragment.DEBUG_MODE) {
				Log.e(TAG, "No target fragment configured ! Requiered to get back the action of OK click");
			}

			return;
		}

		if (!(f instanceof WarningStarSessionDialogContract)) {
			if (SessionDetailsFragment.DEBUG_MODE) {
				Log.e(TAG, "The target fragment ( " + f.getClass().getName() + ") does not implement " + WarningStarSessionDialogContract.class.getName());
			}

			return;
		}

		final WarningStarSessionDialogContract contract = (WarningStarSessionDialogContract) f;
		contract.onWarningClickOk(mSessionId, mSessionTitle, mVote);
	}
}