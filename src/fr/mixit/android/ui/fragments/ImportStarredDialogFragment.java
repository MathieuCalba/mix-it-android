package fr.mixit.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.ui.adapters.SimpleMembersAdapter;
import fr.mixit.android.ui.fragments.WarningImportStarredSessionDialogFragment.WarningImportStarredSessionDialogContract;
import fr.mixit.android_2012.R;


public class ImportStarredDialogFragment extends BoundServiceDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final String TAG = ImportStarredDialogFragment.class.getSimpleName();

	public static final int SELECT_MEMBER = 1304172317;

	protected static final int CURSOR_MEMBERS = 1003;

	protected SimpleMembersAdapter mAdapter;

	public interface ImportStarredDialogContract {
		void onMemberSelect(String memberId);

		void onCancel();
	}

	public static ImportStarredDialogFragment newInstance() {
		final ImportStarredDialogFragment fragment = new ImportStarredDialogFragment();
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mAdapter = new SimpleMembersAdapter(getSherlockActivity().getSupportActionBar().getThemedContext());

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.pick_your_name)//
		.setAdapter(mAdapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final Cursor cursor = (Cursor) mAdapter.getItem(which);
				final String memberId = cursor.getString(MixItContract.Members.PROJ_LIST.MEMBER_ID);

				final ImportStarredDialogContract contract = getListener();
				if (contract != null) {
					contract.onMemberSelect(memberId);
				}

			}
		});
		return builder.create();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		final ImportStarredDialogContract contract = getListener();
		if (contract != null) {
			contract.onCancel();
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		reload();
	}

	public void reload() {
		restartLoader(CURSOR_MEMBERS, getArguments(), this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == CURSOR_MEMBERS) {
			final Uri membersUri = MixItContract.Members.CONTENT_URI;
			return new CursorLoader(getActivity(), membersUri, MixItContract.Members.PROJ_LIST.PROJECTION, null, null, MixItContract.Members.DEFAULT_SORT);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		final int id = loader.getId();
		if (id == CURSOR_MEMBERS) {
			mAdapter.swapCursor(data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		final int id = loader.getId();
		if (id == CURSOR_MEMBERS) {
			mAdapter.swapCursor(null);
		}
	}

	protected ImportStarredDialogContract getListener() {
		final Fragment f = getTargetFragment();
		if (f == null) {
			if (DEBUG_MODE) {
				Log.e(TAG, "No target fragment configured ! Requiered to get back the action of member click");
			}

			return null;
		}

		if (!(f instanceof WarningImportStarredSessionDialogContract)) {
			if (DEBUG_MODE) {
				Log.e(TAG, "The target fragment ( " + f.getClass().getName() + ") does not implement " + ImportStarredDialogContract.class.getName());
			}

			return null;
		}

		final ImportStarredDialogContract contract = (ImportStarredDialogContract) f;
		return contract;
	}

	@Override
	protected void onMessageReceivedFromService(Message msg) {
	}

}
