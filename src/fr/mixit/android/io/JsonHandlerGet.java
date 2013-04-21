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
