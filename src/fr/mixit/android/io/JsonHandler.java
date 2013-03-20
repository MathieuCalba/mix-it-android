package fr.mixit.android.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import fr.mixit.android.MixItApplication;
import fr.mixit.android.utils.Lists;
import fr.mixit.android.utils.Sets;

/**
 * Abstract class that handles reading and parsing an {@link org.json.JSONArray} into a set of {@link android.content.ContentProviderOperation}. It catches
 * exceptions and rethrows them as {@link JsonHandlerException}. Any local {@link android.content.ContentProvider} exceptions are considered unrecoverable.
 * <p>
 * This class is only designed to handle simple one-way synchronization.
 */
public abstract class JsonHandler {

	static final boolean DEBUG_MODE = MixItApplication.DEBUG_MODE;
	static final String TAG = JsonHandler.class.getSimpleName();

	private final String authority;

	public JsonHandler(String anAuthority) {
		authority = anAuthority;
	}

	/**
	 * Parse the given {@link org.json.JSONArray}, turning into a series of {@link android.content.ContentProviderOperation} that are immediately applied using
	 * the given {@link android.content.ContentResolver}.
	 */
	public void parseAndApply(ArrayList<JSONArray> entries, ContentResolver resolver) throws JsonHandlerException {
		try {
			//final ArrayList<ContentProviderOperation> batch =
			parse(entries, resolver);
			//		resolver.applyBatch(authority, batch);
		} catch (final JSONException e) {
			throw new JsonHandlerException("Problem parsing JSON response", e);
			// } catch (final RemoteException e) {
			// throw new RuntimeException("Problem applying batch operation", e);
			// } catch (final OperationApplicationException e) {
			// throw new RuntimeException("Problem applying batch operation", e);
		}
	}

	private final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();

	protected void applyBatch(ContentResolver resolver, boolean force) {
		addOpeAndApplyBatch(null, resolver, force);
	}

	protected void addOpeAndApplyBatch(ContentProviderOperation ope, ContentResolver resolver, boolean force) {
		if (ope != null) {
			batch.add(ope);
		}

		final int size = batch.size();
		if (size == 0 || size < JsonExecutor.MAX_CONTENT_PROVIDER_OPERATIONS && !force) {
			return;
		}

		try {
			resolver.applyBatch(authority, batch);
			batch.clear();
		} catch (final RemoteException e) {
			throw new RuntimeException("Problem applying batch operation", e);
		} catch (final OperationApplicationException e) {
			throw new RuntimeException("Problem applying batch operation", e);
		}
	}

	/**
	 * Parse the given {@link JSONHandler}, returning a set of {@link android.content.ContentProviderOperation} that will bring the
	 * {@link android.content.ContentProvider} into sync with the parsed data.
	 */
	public abstract void parse(ArrayList<JSONArray> entries, ContentResolver resolver) throws JSONException;

	protected static boolean isRowExisting(Uri uri, String[] projection, ContentResolver resolver) {
		final Cursor cursor = resolver.query(uri, projection, null, null, null);
		try {
			if (!cursor.moveToFirst()) {
				return false;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return true;
	}

	/**
	 * Returns those id's from a {@link android.net.Uri} that were not found in a given set.
	 */
	protected static HashSet<String> getLostIds(Set<String> ids, Uri uri, String[] projection, int idColumnIndex, ContentResolver resolver) {
		final HashSet<String> lostIds = Sets.newHashSet();

		final Cursor cursor = resolver.query(uri, projection, null, null, null);
		try {
			while (cursor.moveToNext()) {
				final String id = cursor.getString(idColumnIndex);
				if (!ids.contains(id)) {
					lostIds.add(id);
				}
			}
		} finally {
			cursor.close();
		}

		if (!lostIds.isEmpty() && DEBUG_MODE) {
			Log.d(TAG, "Found " + lostIds.size() + " for " + uri.toString() + " that need to be removed.");
		}

		return lostIds;
	}

	/**
	 * General {@link java.io.IOException} that indicates a problem occured while parsing or applying an {@link org.json.JSONArray}.
	 */
	public static class JsonHandlerException extends IOException {
		private static final long serialVersionUID = -384549840810346698L;

		public JsonHandlerException(String message) {
			super(message);
		}

		public JsonHandlerException(String message, Throwable cause) {
			super(message);
			initCause(cause);
		}

		@Override
		public String toString() {
			if (getCause() != null) {
				return getLocalizedMessage() + ": " + getCause();
			} else {
				return getLocalizedMessage();
			}
		}
	}

}
