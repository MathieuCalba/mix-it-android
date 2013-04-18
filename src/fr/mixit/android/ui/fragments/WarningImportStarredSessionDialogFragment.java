package fr.mixit.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.actionbarsherlock.app.SherlockDialogFragment;

import fr.mixit.android.MixItApplication;
import fr.mixit.android_2012.R;


public class WarningImportStarredSessionDialogFragment extends SherlockDialogFragment {

	public static final String TAG = WarningImportStarredSessionDialogFragment.class.getSimpleName();
	protected static final boolean DEBUG_MODE = MixItApplication.DEBUG_MODE;

	public static final int WARNING_NO_SYNC_STAR_SESSION = 1304141822;

	public interface WarningImportStarredSessionDialogContract {
		void onWarningClickOk();
	}

	public static WarningImportStarredSessionDialogFragment newInstance() {
		final WarningImportStarredSessionDialogFragment fragment = new WarningImportStarredSessionDialogFragment();
		return fragment;
	}

	public WarningImportStarredSessionDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.warning_import_starred_session_not_sync_with_site).setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				importStarredSession();
			}
		});
		return builder.create();
	}

	protected void importStarredSession() {
		final Fragment f = getTargetFragment();
		if (f == null) {
			if (DEBUG_MODE) {
				Log.e(TAG, "No target fragment configured ! Requiered to get back the action of OK click");
			}

			return;
		}

		if (!(f instanceof WarningImportStarredSessionDialogContract)) {
			if (DEBUG_MODE) {
				Log.e(TAG,
						"The target fragment ( " + f.getClass().getName() + ") does not implement " + WarningImportStarredSessionDialogContract.class.getName());
			}

			return;
		}

		final WarningImportStarredSessionDialogContract contract = (WarningImportStarredSessionDialogContract) f;
		contract.onWarningClickOk();
	}
}
