package fr.mixit.android.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentResolver;
import android.content.Context;
import fr.mixit.android.utils.Lists;

public class JsonExecutor {

	public static final int MAX_CONTENT_PROVIDER_OPERATIONS = 50;
	final ContentResolver resolver;

	public JsonExecutor(ContentResolver contentResolver) {
		resolver = contentResolver;
	}

	public void execute(Context context, String assetName, JsonHandler handler) throws JsonHandler.JsonHandlerException {
		try {
			final InputStream input = context.getAssets().open(assetName);
			final byte[] buffer = new byte[input.available()];
			while (input.read(buffer) != -1) {
				;
			}
			final String jsontext = new String(buffer);
			execute(jsontext, handler);
		} catch (final JsonHandler.JsonHandlerException e) {
			throw e;
		} catch (final IOException e) {
			throw new JsonHandler.JsonHandlerException("Problem parsing local asset: " + assetName, e);
		}
	}

	public void execute(String jsonText, JsonHandler handler) throws JsonHandler.JsonHandlerException {
		try {
			final ArrayList<JSONArray> entries = Lists.newArrayList();
			final JSONArray requestEntries = new JSONArray(jsonText);
			entries.add(requestEntries);
			handler.parseAndApply(entries, resolver);
		} catch (final JsonHandler.JsonHandlerException e) {
			throw e;
		} catch (final JSONException e) {
			throw new JsonHandler.JsonHandlerException("Problem parsing jsonText :" /*+ jsonText*/, e);
		}
	}

}
