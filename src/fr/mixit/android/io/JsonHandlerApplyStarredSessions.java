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

import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;
import fr.mixit.android.provider.MixItContract;


public class JsonHandlerApplyStarredSessions extends JsonHandlerApply {

	private static final String TAG = JsonHandlerApplyStarredSessions.class.getSimpleName();

	protected static final String TAG_ID = "id";

	protected int mMemberId;

	protected HashSet<String> mItemIds = null;

	public JsonHandlerApplyStarredSessions(int memberId) {
		super(MixItContract.CONTENT_AUTHORITY);

		mMemberId = memberId;

		mItemIds = new HashSet<String>();
	}

	@Override
	public boolean parseList(JSONArray entries, ContentResolver resolver) throws JSONException {
		if (DEBUG_MODE) {
			Log.d(TAG, "Retrieved " + entries.length() + " more starred session entries.");
		}

		for (int i = 0; i < entries.length(); i++) {
			final JSONObject item = entries.getJSONObject(i);
			parseItem(item, resolver);
		}

		return ProviderParsingUtils.applyBatch(mAuthority, resolver, mBatch, true);
	}

	@Override
	public boolean parseItem(JSONObject item, ContentResolver resolver) throws JSONException {
		final String id = item.getString(TAG_ID);
		mItemIds.add(id);

		final Uri itemUri = MixItContract.Sessions.buildSessionUri(id);

		ContentProviderOperation.Builder builder = null;

		boolean build = false;

		if (ProviderParsingUtils.isRowExisting(itemUri, MixItContract.Sessions.PROJ_DETAIL.PROJECTION, resolver)) {
			builder = ContentProviderOperation.newUpdate(itemUri);
			builder.withValue(MixItContract.Sessions.IS_FAVORITE, 1);
			build = true;
		} else {
			if (DEBUG_MODE) {
				Log.e(TAG, "Impossible to set starred session with id " + id + " because this session was not found");
			}
		}

		if (build && builder != null) {
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, builder.build());
		}

		return true;
	}

}
