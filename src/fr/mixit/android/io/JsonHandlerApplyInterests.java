package fr.mixit.android.io;

import java.util.HashSet;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import fr.mixit.android.provider.MixItContract;


public class JsonHandlerApplyInterests extends JsonHandlerApply {

	private static final String TAG = JsonHandlerApplyInterests.class.getSimpleName();

	protected static final String TAG_ID = "id";
	protected static final String TAG_NAME = "name";

	protected HashSet<String> mItemIds = null;

	public JsonHandlerApplyInterests() {
		super(MixItContract.CONTENT_AUTHORITY);
	}

	@Override
	public boolean parseList(JSONArray entries, ContentResolver resolver) throws JSONException {
		mItemIds = new HashSet<String>();

		if (DEBUG_MODE) {
			Log.d(TAG, "Retrieved " + entries.length() + " more interests entries.");
		}

		int nbEntries = 0;
		for (int i = 0; i < entries.length(); i++) {
			final JSONObject item = entries.getJSONObject(i);
			parseItem(item, resolver);
			nbEntries++;
		}

		if (nbEntries > 0) {
			deleteItemsLinkedDataNotFound(resolver);
			deleteItemsNotFound(resolver);
		}

		return ProviderParsingUtils.applyBatch(mAuthority, resolver, mBatch, true);
	}

	@Override
	public boolean parseItem(JSONObject item, ContentResolver resolver) throws JSONException {
		final String id = item.getString(TAG_ID);
		mItemIds.add(id);

		final Uri itemUri = MixItContract.Interests.buildInterestUri(id);

		boolean itemUpdated = false;
		boolean newItem = false;
		boolean build = false;
		ContentProviderOperation.Builder builder;

		if (ProviderParsingUtils.isRowExisting(itemUri, MixItContract.Interests.PROJ.PROJECTION, resolver)) {
			builder = ContentProviderOperation.newUpdate(itemUri);
			itemUpdated = isItemUpdated(itemUri, item, resolver);
		} else {
			newItem = true;
			builder = ContentProviderOperation.newInsert(MixItContract.Interests.CONTENT_URI);
			builder.withValue(MixItContract.Interests.INTEREST_ID, id);
			build = true;
		}

		if (newItem || itemUpdated) {
			builder.withValue(MixItContract.Interests.NAME, item.getString(TAG_NAME));
			build = true;
		}
		if (build) {
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, builder.build());
		}

		return true;
	}

	private static boolean isItemUpdated(Uri uri, JSONObject item, ContentResolver resolver) throws JSONException {
		final Cursor cursor = resolver.query(uri, MixItContract.Interests.PROJ.PROJECTION, null, null, null);
		try {
			if (!cursor.moveToFirst()) {
				return false;
			}

			final String curName = cursor.getString(MixItContract.Interests.PROJ.NAME).toLowerCase(Locale.getDefault()).trim();

			final String newName = item.has(TAG_NAME) ? item.getString(TAG_NAME).toLowerCase(Locale.getDefault()).trim() : curName;

			return !curName.equals(newName);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	protected void deleteItemsLinkedDataNotFound(ContentResolver resolver) {
		// no linked data for the parsing of interests
	}

	protected void deleteItemsNotFound(ContentResolver resolver) {
		for (final String lostId : ProviderParsingUtils.getLostIds(mItemIds, MixItContract.Interests.CONTENT_URI, MixItContract.Interests.PROJ.PROJECTION,
				MixItContract.Interests.PROJ.INTEREST_ID, resolver)) {
			// delete interests not found from N-N relation with session
			Uri deleteUri = MixItContract.Sessions.buildInterestsDirUri(lostId);
			ContentProviderOperation ope = ContentProviderOperation.newDelete(deleteUri).build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);

			// and delete interests not found from N-N relation with member
			deleteUri = MixItContract.Members.buildInterestsDirUri(lostId);
			ope = ContentProviderOperation.newDelete(deleteUri).build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);

			// and delete interests not found from interest
			deleteUri = MixItContract.Interests.buildInterestUri(lostId);
			ope = ContentProviderOperation.newDelete(deleteUri).build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
		}
	}
}
