package fr.mixit.android.io;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;


public class JsonExecutor {
	protected final ContentResolver mResolver;

	public JsonExecutor() {
		this(null);
	}

	public JsonExecutor(ContentResolver contentResolver) {
		super();

		mResolver = contentResolver;
	}

	public void executeAndInsert(Context context, String assetName, JsonHandlerApply handler) throws JsonHandlerException {
		try {
			final InputStream input = context.getAssets().open(assetName);
			final byte[] buffer = new byte[input.available()];
			while (input.read(buffer) != -1) {
				;
			}
			final String jsontext = new String(buffer);
			executeAndInsert(jsontext, handler);
		} catch (final JsonHandlerException e) {
			throw e;
		} catch (final IOException e) {
			throw new JsonHandlerException("Problem parsing local asset: " + assetName, e);
		}
	}

	public void executeAndInsert(String jsonText, JsonHandlerApply handler) throws JsonHandlerException {
		try {
			final JSONArray requestEntries = new JSONArray(jsonText);
			handler.parseAndApply(requestEntries, mResolver);
		} catch (final JsonHandlerException e) {
			throw e;
		} catch (final JSONException e) {
			try {
				final JSONObject resquestEntry = new JSONObject(jsonText);
				handler.parseAndApply(resquestEntry, mResolver);
			} catch (final JsonHandlerException ex) {
				throw ex;
			} catch (final JSONException ex) {
				throw new JsonHandlerException("Problem parsing jsonText :" + jsonText + ". Neither a JSONArray, " + e.toString() + "nor a JSONObject"
						+ ex.toString());
			}
		}
	}

	public <T> T executeAndGet(Context context, String assetName, JsonHandlerGet<T> handler) throws JsonHandlerException {
		try {
			final InputStream input = context.getAssets().open(assetName);
			final byte[] buffer = new byte[input.available()];
			while (input.read(buffer) != -1) {
				;
			}
			final String jsontext = new String(buffer);
			return executeAndGet(jsontext, handler);
		} catch (final JsonHandlerException e) {
			throw e;
		} catch (final IOException e) {
			throw new JsonHandlerException("Problem parsing local asset: " + assetName, e);
		}
	}

	public <T> T executeAndGet(String jsonText, JsonHandlerGet<T> handler) throws JsonHandlerException {
		try {
			final JSONArray requestEntries = new JSONArray(jsonText);
			return handler.parseAndGet(requestEntries);
		} catch (final JsonHandlerException e) {
			throw e;
		} catch (final JSONException e) {
			try {
				final JSONObject resquestEntry = new JSONObject(jsonText);
				return handler.parseAndGet(resquestEntry);
			} catch (final JsonHandlerException ex) {
				throw ex;
			} catch (final JSONException ex) {
				throw new JsonHandlerException("Problem parsing jsonText :" + jsonText + ". Neither a JSONArray, " + e.toString() + "nor a JSONObject"
						+ ex.toString());
			}
		}
	}

}
