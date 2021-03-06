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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.provider.MixItDatabase;
import fr.mixit.android.utils.DateUtils;
import fr.mixit.android.utils.Maps;
import fr.mixit.android.utils.Sets;


public class JsonHandlerApplyTalks extends JsonHandlerApply {

	private static final String TAG = JsonHandlerApplyTalks.class.getSimpleName();

	protected static final String TAG_ID = "id";
	protected static final String TAG_TITLE = "title";
	protected static final String TAG_SUMMARY = "summary";
	protected static final String TAG_DESC = "description";
	protected static final String TAG_FORMAT = "format";
	protected static final String TAG_LEVEL = "level";
	protected static final String TAG_LANG = "lang";
	protected static final String TAG_START = "start";
	protected static final String TAG_END = "end";
	protected static final String TAG_ROOM = "room";
	protected static final String TAG_NB_VOTES = "nbVotes";
	// protected static final String TAG_MY_VOTE = "myVote";
	// protected static final String TAG_IS_FAVORITE = "isFavorite";

	protected static final String TAG_SPEAKERS = "speakers";
	protected static final String TAG_INTERESTS = "interests";

	private static final String EMPTY = "";

	protected boolean mIsLightningTalks = false;
	protected boolean mIsFullParsing = false;
	protected boolean mIsParsingList = false;

	protected HashSet<String> mItemIds = null;
	protected HashMap<String, HashSet<String>> mItemInterestsIds;
	protected HashMap<String, HashSet<String>> mItemSpeakersIds;

	public JsonHandlerApplyTalks(boolean isFullParsing) {
		this(isFullParsing, false);
	}

	public JsonHandlerApplyTalks(boolean isFullParsing, boolean isLightningTalks) {
		super(MixItContract.CONTENT_AUTHORITY);

		mIsFullParsing = isFullParsing;
		mIsLightningTalks = isLightningTalks;

		mItemIds = new HashSet<String>();
		mItemInterestsIds = Maps.newHashMap();
		mItemSpeakersIds = Maps.newHashMap();
	}

	@Override
	public boolean parseList(JSONArray entries, ContentResolver resolver) throws JSONException {
		if (DEBUG_MODE) {
			Log.d(TAG, "Retrieved " + entries.length() + " more talks entries.");
		}

		mIsParsingList = true;

		int nbEntries = 0;
		for (int i = 0; i < entries.length(); i++) {
			final JSONObject item = entries.getJSONObject(i);
			parseItem(item, resolver);
			nbEntries++;
		}

		if (nbEntries > 0) {
			deleteItemsDataNotFound(resolver);
			if (!mIsFullParsing) {
				deleteItemsNotFound(resolver);
			}
		}

		return ProviderParsingUtils.applyBatch(mAuthority, resolver, mBatch, true);
	}

	@Override
	public boolean parseItem(JSONObject item, ContentResolver resolver) throws JSONException {
		final String id = item.getString(TAG_ID);
		mItemIds.add(id);

		final Uri itemUri = MixItContract.Sessions.buildSessionUri(id);

		boolean tagUpdated = false;
		boolean newItem = false;
		boolean build = false;
		ContentProviderOperation.Builder builder;

		if (ProviderParsingUtils.isRowExisting(itemUri, MixItContract.Sessions.PROJ_DETAIL.PROJECTION, resolver)) {
			builder = ContentProviderOperation.newUpdate(itemUri);
			tagUpdated = isItemUpdated(itemUri, item, resolver);
		} else {
			newItem = true;
			builder = ContentProviderOperation
					.newInsert(mIsLightningTalks ? MixItContract.Sessions.CONTENT_URI_LIGNTHNING : MixItContract.Sessions.CONTENT_URI);
			builder.withValue(MixItContract.Sessions.SESSION_ID, id);
			build = true;
		}

		if (newItem || tagUpdated) {
			if (item.has(TAG_TITLE)) {
				builder.withValue(MixItContract.Sessions.TITLE, item.getString(TAG_TITLE));
			}
			if (item.has(TAG_SUMMARY)) {
				builder.withValue(MixItContract.Sessions.SUMMARY, item.getString(TAG_SUMMARY));
			}
			if (item.has(TAG_DESC)) {
				builder.withValue(MixItContract.Sessions.DESC, item.getString(TAG_DESC));
			}
			if (item.has(TAG_FORMAT)) {
				builder.withValue(MixItContract.Sessions.FORMAT, mIsLightningTalks ? MixItContract.Sessions.FORMAT_LIGHTNING_TALK : item.getString(TAG_FORMAT));
			} else {
				builder.withValue(MixItContract.Sessions.FORMAT, mIsLightningTalks ? MixItContract.Sessions.FORMAT_LIGHTNING_TALK
						: MixItContract.Sessions.FORMAT_TALK);
			}
			if (item.has(TAG_LEVEL)) {
				builder.withValue(MixItContract.Sessions.LEVEL, item.getString(TAG_LEVEL));
			}
			if (item.has(TAG_LANG)) {
				builder.withValue(MixItContract.Sessions.LANG, item.getString(TAG_LANG));
			}
			if (item.has(TAG_START)) {
				final String start = item.getString(TAG_START);
				builder.withValue(MixItContract.Sessions.START, DateUtils.parseISO8601(start));
			}
			if (item.has(TAG_END)) {
				final String end = item.getString(TAG_END);
				builder.withValue(MixItContract.Sessions.END, DateUtils.parseISO8601(end));
			}
			if (item.has(TAG_ROOM)) {
				builder.withValue(MixItContract.Sessions.ROOM_ID, item.getString(TAG_ROOM));
			}
			if (item.has(TAG_NB_VOTES)) {
				builder.withValue(MixItContract.Sessions.NB_VOTES, item.getString(TAG_NB_VOTES));
			}

			build = true;
		}
		if (build) {
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, builder.build());
		}

		if (mIsFullParsing && item.has(TAG_INTERESTS)) {
			final JSONArray interests = item.getJSONArray(TAG_INTERESTS);
			parseLinkedInterests(id, interests, resolver);
		}

		if (item.has(TAG_SPEAKERS)) {
			final JSONArray speakers = item.getJSONArray(TAG_SPEAKERS);
			parseLinkedSpeakers(id, speakers, resolver);
		}

		if (!mIsParsingList) {
			deleteItemsDataNotFound(resolver);

			return ProviderParsingUtils.applyBatch(mAuthority, resolver, mBatch, true);
		}

		return true;
	}

	private static boolean isItemUpdated(Uri uri, JSONObject item, ContentResolver resolver) throws JSONException {
		final Cursor cursor = resolver.query(uri, MixItContract.Sessions.PROJ_DETAIL.PROJECTION, null, null, null);
		try {
			if (!cursor.moveToFirst()) {
				return false;
			}

			String curTitle = cursor.getString(MixItContract.Sessions.PROJ_DETAIL.TITLE);
			if (TextUtils.isEmpty(curTitle)) {
				curTitle = EMPTY;
			}

			String curSummary = cursor.getString(MixItContract.Sessions.PROJ_DETAIL.SUMMARY);
			if (TextUtils.isEmpty(curSummary)) {
				curSummary = EMPTY;
			}

			String curDesc = cursor.getString(MixItContract.Sessions.PROJ_DETAIL.DESC);
			if (TextUtils.isEmpty(curDesc)) {
				curDesc = EMPTY;
			}

			final long curStart = cursor.getLong(MixItContract.Sessions.PROJ_DETAIL.START);
			final long curEnd = cursor.getLong(MixItContract.Sessions.PROJ_DETAIL.END);

			String curRoomId = cursor.getString(MixItContract.Sessions.PROJ_DETAIL.ROOM_ID);
			if (TextUtils.isEmpty(curRoomId)) {
				curRoomId = EMPTY;
			}

			final int curNbVotes = cursor.getInt(MixItContract.Sessions.PROJ_DETAIL.NB_VOTES);
			// final int curMyVote = cursor.getInt(MixItContract.Sessions.PROJ_DETAIL.MY_VOTE);
			// final int curIsFavorite = cursor.getInt(MixItContract.Sessions.PROJ_DETAIL.IS_FAVORITE);

			String curFormat = cursor.getString(MixItContract.Sessions.PROJ_DETAIL.FORMAT);
			if (TextUtils.isEmpty(curFormat)) {
				curFormat = EMPTY;
			}

			String curLevel = cursor.getString(MixItContract.Sessions.PROJ_DETAIL.LEVEL);
			if (TextUtils.isEmpty(curLevel)) {
				curLevel = EMPTY;
			}

			String curLang = cursor.getString(MixItContract.Sessions.PROJ_DETAIL.LANG);
			if (TextUtils.isEmpty(curLang)) {
				curLang = EMPTY;
			}

			final String newTitle = item.has(TAG_TITLE) ? item.getString(TAG_TITLE).trim() : curTitle;
			final String newSummary = item.has(TAG_SUMMARY) ? item.getString(TAG_SUMMARY).trim() : curSummary;
			final String newDesc = item.has(TAG_DESC) ? item.getString(TAG_DESC).trim() : curDesc;
			long newStart = 0;
			if (item.has(TAG_START)) {
				final String start = item.getString(TAG_START);
				newStart = DateUtils.parseISO8601(start);
			} else {
				newStart = curStart;
			}
			long newEnd = 0;
			if (item.has(TAG_END)) {
				final String end = item.getString(TAG_END);
				newEnd = DateUtils.parseISO8601(end);
			} else {
				newEnd = curEnd;
			}
			final String newRoomId = item.has(TAG_ROOM) ? item.getString(TAG_ROOM).trim() : curRoomId;
			final int newNbVotes = item.has(TAG_NB_VOTES) ? item.getInt(TAG_NB_VOTES) : curNbVotes;
			// final int newMyVote = session.has(TAG_MY_VOTE) ? session.getBoolean(TAG_MY_VOTE) ? 1 : 0 : curMyVote;
			// final int newIsFavorite = session.has(TAG_IS_FAVORITE) ? session.getInt(TAG_IS_FAVORITE) : curIsFavorite;
			final String newFormat = item.has(TAG_FORMAT) ? item.getString(TAG_FORMAT).trim() : curFormat;
			final String newLevel = item.has(TAG_LEVEL) ? item.getString(TAG_LEVEL).trim() : curLevel;
			final String newLang = item.has(TAG_LANG) ? item.getString(TAG_LANG).trim() : curLang;

			return !curTitle.equals(newTitle) || //
					!curSummary.equals(newSummary) || //
					!curDesc.equals(newDesc) || //
					curStart != newStart || //
					curEnd != newEnd || //
					!curRoomId.equals(newRoomId) || //
					curNbVotes != newNbVotes || //
					// curMyVote != newMyVote ||
					// curIsFavorite != newIsFavorite || //
					!curFormat.equals(newFormat) || //
					!curLevel.equals(newLevel) || //
					!curLang.equals(newLang);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public void parseLinkedInterests(String itemId, JSONArray interests, ContentResolver resolver) throws JSONException {
		final Uri itemInterestsUri = MixItContract.Sessions.buildInterestsDirUri(itemId);
		final HashSet<String> interestsIds = Sets.newHashSet();

		for (int j = 0; j < interests.length(); j++) {
			final int id = interests.getInt(j);
			final String interestId = String.valueOf(id);
			interestsIds.add(interestId);

			final ContentProviderOperation ope = ContentProviderOperation.newInsert(itemInterestsUri) //
					.withValue(MixItDatabase.SessionsInterests.INTEREST_ID, interestId) //
					.withValue(MixItDatabase.SessionsInterests.SESSION_ID, itemId) //
					.build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
		}

		mItemInterestsIds.put(itemId, interestsIds);
	}

	public void parseLinkedSpeakers(String itemId, JSONArray speakers, ContentResolver resolver) throws JSONException {
		final Uri itemSpeakersUri = MixItContract.Sessions.buildSpeakersDirUri(itemId);
		final HashSet<String> speakersIds = Sets.newHashSet();

		for (int j = 0; j < speakers.length(); j++) {
			final int id = speakers.getInt(j);
			final String speakerId = String.valueOf(id);
			speakersIds.add(speakerId);

			final ContentProviderOperation ope = ContentProviderOperation.newInsert(itemSpeakersUri) //
					.withValue(MixItDatabase.SessionsSpeakers.SPEAKER_ID, speakerId) //
					.withValue(MixItDatabase.SessionsSpeakers.SESSION_ID, itemId) //
					.build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
		}

		mItemSpeakersIds.put(itemId, speakersIds);
	}

	protected void deleteItemsDataNotFound(ContentResolver resolver) {
		// delete deleted sessions from N-N relation with interest
		for (final Map.Entry<String, HashSet<String>> entry : mItemInterestsIds.entrySet()) {
			final String itemId = entry.getKey();
			final HashSet<String> interestIds = entry.getValue();
			final Uri itemInterestsUri = MixItContract.Sessions.buildInterestsDirUri(itemId);
			final HashSet<String> lostInterestIds = ProviderParsingUtils.getLostIds(interestIds, itemInterestsUri, MixItContract.Interests.PROJ.PROJECTION,
					MixItContract.Interests.PROJ.INTEREST_ID, resolver);
			for (final String lostInterestId : lostInterestIds) {
				final Uri deleteUri = MixItContract.Sessions.buildSessionInterestUri(itemId, lostInterestId);
				final ContentProviderOperation ope = ContentProviderOperation.newDelete(deleteUri).build();
				ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
			}
		}

		// and delete deleted sessions from N-N relation with member
		for (final Map.Entry<String, HashSet<String>> entry : mItemSpeakersIds.entrySet()) {
			final String itemId = entry.getKey();
			final HashSet<String> speakerIds = entry.getValue();
			final Uri itemSpeakersUri = MixItContract.Sessions.buildSpeakersDirUri(itemId);
			final HashSet<String> lostSpeakerIds = ProviderParsingUtils.getLostIds(speakerIds, itemSpeakersUri, MixItContract.Members.PROJ_DETAIL.PROJECTION,
					MixItContract.Members.PROJ_DETAIL.MEMBER_ID, resolver);
			for (final String lostSpeakerId : lostSpeakerIds) {
				final Uri deleteUri = MixItContract.Sessions.buildSessionSpeakerUri(itemId, lostSpeakerId);
				final ContentProviderOperation ope = ContentProviderOperation.newDelete(deleteUri).build();
				ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
			}
		}
	}

	protected void deleteItemsNotFound(ContentResolver resolver) {
		for (final String lostId : ProviderParsingUtils.getLostIds(mItemIds, mIsLightningTalks ? MixItContract.Sessions.CONTENT_URI_LIGNTHNING
				: MixItContract.Sessions.CONTENT_URI, MixItContract.Sessions.PROJ_DETAIL.PROJECTION, MixItContract.Sessions.PROJ_DETAIL.SESSION_ID, resolver)) {
			// delete session not found from N-N relation with interest
			Uri deleteUri = MixItContract.Sessions.buildInterestsDirUri(lostId);
			ContentProviderOperation ope = ContentProviderOperation.newDelete(deleteUri).build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);

			// and delete session not found from N-N relation with member
			deleteUri = MixItContract.Sessions.buildSpeakersDirUri(lostId);
			ope = ContentProviderOperation.newDelete(deleteUri).build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);

			// and delete session not found from session
			deleteUri = MixItContract.Sessions.buildSessionUri(lostId);
			ope = ContentProviderOperation.newDelete(deleteUri).build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
		}
	}

}
