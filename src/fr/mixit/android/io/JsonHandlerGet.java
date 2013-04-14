package fr.mixit.android.io;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.mixit.android.MixItApplication;


/**
 * Abstract class that handles reading and parsing an {@link org.json.JSONArray} into a set of {@link android.content.ContentProviderOperation}. It catches
 * exceptions and rethrows them as {@link JsonHandlerException}. Any local {@link android.content.ContentProvider} exceptions are considered unrecoverable.
 */
public abstract class JsonHandlerGet<T> {

	protected static final boolean DEBUG_MODE = MixItApplication.DEBUG_MODE;

	public JsonHandlerGet() {
		super();
	}

	/**
	 * Parse the given {@link org.json.JSONArray}, turning into a set of T.
	 */
	public T parseAndGet(JSONArray entries) throws JsonHandlerException {
		try {
			return parseList(entries);
		} catch (final JSONException e) {
			throw new JsonHandlerException("Problem parsing JSON response", e);
		}
	}

	/**
	 * Parse the given {@link org.json.JSONObject}, turning into a T.
	 */
	public T parseAndGet(JSONObject entry) throws JsonHandlerException {
		try {
			return parseItem(entry);
		} catch (final JSONException e) {
			throw new JsonHandlerException("Problem parsing JSON response", e);
		}
	}

	/**
	 * Parse the given {@link org.json.JSONArray}, returning a set of T.
	 */
	public abstract T parseList(JSONArray entries) throws JSONException;

	/**
	 * Parse the given {@link org.json.JSONObject}, returning a T.
	 */
	public abstract T parseItem(JSONObject entry) throws JSONException;

}
