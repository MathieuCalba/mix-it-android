/*
 * Copyright 2011 - 2013 Mathieu Calba
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
