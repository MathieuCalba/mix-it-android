package fr.mixit.android.io;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import fr.mixit.android.MixItApplication;
import fr.mixit.android.utils.Lists;


/**
 * Abstract class that handles reading and parsing an {@link org.json.JSONArray} into a set of {@link android.content.ContentProviderOperation}. It catches
 * exceptions and rethrows them as {@link JsonHandlerException}. Any local {@link android.content.ContentProvider} exceptions are considered unrecoverable.
 * <p>
 * This class is only designed to handle simple one-way synchronization.
 */
public abstract class JsonHandlerApply {

	protected static final boolean DEBUG_MODE = MixItApplication.DEBUG_MODE;

	protected final String mAuthority;

	public JsonHandlerApply(String anAuthority) {
		super();
		mAuthority = anAuthority;
	}

	/**
	 * Parse the given {@link org.json.JSONArray}, turning into a series of {@link android.content.ContentProviderOperation} that are immediately applied using
	 * the given {@link android.content.ContentResolver}.
	 */
	public boolean parseAndApply(JSONArray entries, ContentResolver resolver) throws JsonHandlerException {
		try {
			return parseList(entries, resolver);
		} catch (final JSONException e) {
			throw new JsonHandlerException("Problem parsing JSON response", e);
		}
	}

	/**
	 * Parse the given {@link org.json.JSONObject}, turning into a series of {@link android.content.ContentProviderOperation} that are immediately applied using
	 * the given {@link android.content.ContentResolver}.
	 */
	public boolean parseAndApply(JSONObject entry, ContentResolver resolver) throws JsonHandlerException {
		try {
			return parseItem(entry, resolver);
		} catch (final JSONException e) {
			throw new JsonHandlerException("Problem parsing JSON response", e);
		}
	}

	protected final ArrayList<ContentProviderOperation> mBatch = Lists.newArrayList();

	/**
	 * Parse the given {@link org.json.JSONArray}, synchronizing data into the {@link android.content.ContentProvider}.
	 */
	public abstract boolean parseList(JSONArray entries, ContentResolver resolver) throws JSONException;

	/**
	 * Parse the given {@link org.json.JSONObject}, synchronizing data into the {@link android.content.ContentProvider}.
	 */
	public abstract boolean parseItem(JSONObject entry, ContentResolver resolver) throws JSONException;

}
