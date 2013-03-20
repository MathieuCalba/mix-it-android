package fr.mixit.android.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
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
import fr.mixit.android.utils.Maps;
import fr.mixit.android.utils.Sets;


public class SessionsHandler extends JsonHandler {

	static final String TAG = SessionsHandler.class.getSimpleName();

	static final String FALSE = "false";

	static final String TAG_ID = "id";
	static final String TAG_TITLE = "title";
	static final String TAG_SUMMARY = "summary";
	static final String TAG_DESC = "description";
	static final String TAG_ROOM = "room";
	static final String TAG_IS_FAVORITE = "isFavorite";
	static final String TAG_NB_VOTES = "nbVotes";
	static final String TAG_MY_VOTE = "myVote";

	static final String TAG_INTERESTS = "interests";
	static final String TAG_SPEAKERS = "speakers";

	boolean deleteSessionsNotFoundInDB = false;
	boolean deleteSessionDataNotFoundInDB = false;
	boolean isSession = true;
	HashSet<String> sessionIds;
	HashMap<String, HashSet<String>> sessionInterestsIds;
	HashMap<String, HashSet<String>> sessionSpeakersIds;

	public SessionsHandler(boolean isSession) {
		this(isSession, false);
	}

	public SessionsHandler(boolean isSession, boolean deleteSessionDataNotFoundInDB) {
		this(isSession, deleteSessionDataNotFoundInDB, false);
	}

	public SessionsHandler(boolean isSession, boolean deleteSessionDataNotFoundInDB, boolean deleteSessionsNotFoundInDB) {
		super(MixItContract.CONTENT_AUTHORITY);

		this.isSession = isSession;
		this.deleteSessionDataNotFoundInDB = deleteSessionDataNotFoundInDB;
		this.deleteSessionsNotFoundInDB = deleteSessionsNotFoundInDB;
	}

	@Override
	public void parse(ArrayList<JSONArray> entries, ContentResolver resolver) throws JSONException {
		sessionIds = new HashSet<String>();
		sessionInterestsIds = Maps.newHashMap();
		sessionSpeakersIds = Maps.newHashMap();

		int nbEntries = 0;
		for (final JSONArray sessions : entries) {
			if (DEBUG_MODE) {
				Log.d(TAG, "Retrieved " + sessions.length() + " sessions entries.");
			}
			nbEntries += sessions.length();

			// batch.addAll(parseSessions(sessions, resolver));
			parseSessions(sessions, resolver);
		}

		if (nbEntries > 0) {
			if (deleteSessionDataNotFoundInDB) {
				for (final Map.Entry<String, HashSet<String>> entry : sessionInterestsIds.entrySet()) {
					final String sessionId = entry.getKey();
					final HashSet<String> interestIds = entry.getValue();
					final Uri sessionInterestsUri = MixItContract.Sessions.buildInterestsDirUri(sessionId);
					final HashSet<String> lostInterestIds = getLostIds(interestIds, sessionInterestsUri, InterestsQuery.PROJECTION, InterestsQuery.INTEREST_ID,
							resolver);
					for (final String lostInterestId : lostInterestIds) {
						final Uri deleteUri = MixItContract.Sessions.buildSessionInterestUri(sessionId, lostInterestId);
						final ContentProviderOperation ope = ContentProviderOperation.newDelete(deleteUri).build();
						addOpeAndApplyBatch(ope, resolver, false);
					}
				}

				for (final Map.Entry<String, HashSet<String>> entry : sessionSpeakersIds.entrySet()) {
					final String sessionId = entry.getKey();
					final HashSet<String> speakerIds = entry.getValue();
					final Uri sessionSpeakersUri = MixItContract.Sessions.buildSpeakersDirUri(sessionId);
					final HashSet<String> lostSpeakerIds = getLostIds(speakerIds, sessionSpeakersUri, SpeakersQuery.PROJECTION, SpeakersQuery.MEMBER_ID,
							resolver);
					for (final String lostSpeakerId : lostSpeakerIds) {
						final Uri deleteUri = MixItContract.Sessions.buildSessionSpeakerUri(sessionId, lostSpeakerId);
						final ContentProviderOperation ope = ContentProviderOperation.newDelete(deleteUri).build();
						addOpeAndApplyBatch(ope, resolver, false);
					}
				}
			}

			if (deleteSessionsNotFoundInDB) {
				for (final String lostId : getLostIds(sessionIds, isSession ? MixItContract.Sessions.CONTENT_URI
						: MixItContract.Sessions.CONTENT_URI_LIGNTHNING, SessionsQuery.PROJECTION, SessionsQuery.SESSION_ID, resolver)) {
					Uri deleteUri = MixItContract.Sessions.buildInterestsDirUri(lostId);
					ContentProviderOperation ope = ContentProviderOperation.newDelete(deleteUri).build();
					addOpeAndApplyBatch(ope, resolver, false);
					deleteUri = MixItContract.Sessions.buildSpeakersDirUri(lostId);
					ope = ContentProviderOperation.newDelete(deleteUri).build();
					addOpeAndApplyBatch(ope, resolver, false);
					deleteUri = MixItContract.Sessions.buildSessionUri(lostId);
					ope = ContentProviderOperation.newDelete(deleteUri).build();
					addOpeAndApplyBatch(ope, resolver, false);
				}
			}
		}
		applyBatch(resolver, true);
	}

	public void parseSessions(JSONArray entries, ContentResolver resolver) throws JSONException {
		for (int i = 0; i < entries.length(); i++) {
			final JSONObject session = entries.getJSONObject(i);
			final String id = session.getString(TAG_ID);
			sessionIds.add(id);

			final Uri sessionUri = MixItContract.Sessions.buildSessionUri(id);

			boolean sessionUpdated = false;
			boolean newSession = false;
			boolean build = false;
			ContentProviderOperation.Builder builder;

			if (isRowExisting(sessionUri, SessionsQuery.PROJECTION, resolver)) {
				builder = ContentProviderOperation.newUpdate(sessionUri);
				sessionUpdated = isSessionUpdated(sessionUri, session, resolver);
			} else {
				newSession = true;
				builder = ContentProviderOperation.newInsert(isSession ? MixItContract.Sessions.CONTENT_URI : MixItContract.Sessions.CONTENT_URI_LIGNTHNING);
				builder.withValue(MixItContract.Sessions.SESSION_ID, id);
				build = true;
			}

			if (newSession || sessionUpdated) {
				if (session.has(TAG_TITLE)) {
					builder.withValue(MixItContract.Sessions.TITLE, session.getString(TAG_TITLE));
				}
				if (session.has(TAG_SUMMARY)) {
					builder.withValue(MixItContract.Sessions.SUMMARY, session.getString(TAG_SUMMARY));
				}
				if (session.has(TAG_DESC)) {
					builder.withValue(MixItContract.Sessions.DESC, session.getString(TAG_DESC));
				}
				if (session.has(TAG_ROOM)) {
					builder.withValue(MixItContract.Sessions.ROOM_ID, session.getString(TAG_ROOM));
				}
				builder.withValue(MixItContract.Sessions.IS_SESSION, isSession ? 1 : 0);
				if (session.has(TAG_NB_VOTES)) {
					builder.withValue(MixItContract.Sessions.NB_VOTES, session.getInt(TAG_NB_VOTES));
				}
				if (session.has(TAG_MY_VOTE)) {
					builder.withValue(MixItContract.Sessions.MY_VOTE, session.getBoolean(TAG_MY_VOTE) ? 1 : 0);
				}
				if (session.has(TAG_IS_FAVORITE)) {
					builder.withValue(MixItContract.Sessions.IS_FAVORITE, session.getInt(TAG_IS_FAVORITE));
				}
				build = true;
			}
			if (build) {
				addOpeAndApplyBatch(builder.build(), resolver, false);
			}

			if (session.has(TAG_INTERESTS)) {
				final JSONArray interests = session.getJSONArray(TAG_INTERESTS);
				parseInterests(id, interests, resolver);
			}

			if (session.has(TAG_SPEAKERS)) {
				final JSONArray speakers = session.getJSONArray(TAG_SPEAKERS);
				parseSpeakers(id, speakers, resolver);
			}
		}
	}

	public void parseInterests(String sessionId, JSONArray interests, ContentResolver resolver) throws JSONException {
		final Uri sessionInterestsUri = MixItContract.Sessions.buildInterestsDirUri(sessionId);
		final HashSet<String> interestsIds = Sets.newHashSet();

		for (int j = 0; j < interests.length(); j++) {
			final int id = interests.getInt(j);
			final String interestId = String.format(Locale.getDefault(), FORMATER, id);
			interestsIds.add(interestId);

			final ContentProviderOperation ope = ContentProviderOperation.newInsert(sessionInterestsUri)
					.withValue(MixItDatabase.SessionsInterests.INTEREST_ID, interestId).withValue(MixItDatabase.SessionsInterests.SESSION_ID, sessionId)
					.build();
			addOpeAndApplyBatch(ope, resolver, false);
		}

		sessionInterestsIds.put(sessionId, interestsIds);
	}

	static final String FORMATER = "%d";

	public void parseSpeakers(String sessionId, JSONArray speakers, ContentResolver resolver) throws JSONException {
		final Uri sessionSpeakersUri = MixItContract.Sessions.buildSpeakersDirUri(sessionId);
		final HashSet<String> speakersIds = Sets.newHashSet();

		for (int j = 0; j < speakers.length(); j++) {
			final int id = speakers.getInt(j);
			final String speakerId = String.format(Locale.getDefault(), FORMATER, id);
			speakersIds.add(speakerId);
			final ContentProviderOperation ope = ContentProviderOperation.newInsert(sessionSpeakersUri)
					.withValue(MixItDatabase.SessionsSpeakers.SPEAKER_ID, speakerId).withValue(MixItDatabase.SessionsSpeakers.SESSION_ID, sessionId).build();
			addOpeAndApplyBatch(ope, resolver, false);
		}

		sessionSpeakersIds.put(sessionId, speakersIds);
	}

	private static boolean isSessionUpdated(Uri uri, JSONObject session, ContentResolver resolver) throws JSONException {
		final Cursor cursor = resolver.query(uri, SessionsQuery.PROJECTION, null, null, null);
		try {
			if (!cursor.moveToFirst()) {
				return false;
			}

			final String curTitle = !TextUtils.isEmpty(cursor.getString(SessionsQuery.TITLE)) ? cursor.getString(SessionsQuery.TITLE).toLowerCase().trim() : "";
			final String curSummary = !TextUtils.isEmpty(cursor.getString(SessionsQuery.SUMMARY)) ? cursor.getString(SessionsQuery.SUMMARY).toLowerCase()
					.trim() : "";
					final String curDesc = !TextUtils.isEmpty(cursor.getString(SessionsQuery.DESC)) ? cursor.getString(SessionsQuery.DESC)
							.toLowerCase(Locale.getDefault()).trim() : "";
							// final String curTime = !TextUtils.isEmpty(cursor.getString(SessionsQuery.TIME)) ?
							// cursor.getString(SessionsQuery.TIME).toLowerCase(Locale.getDefault()).trim() : "";
							final String curTrackId = !TextUtils.isEmpty(cursor.getString(SessionsQuery.ROOM_ID)) ? cursor.getString(SessionsQuery.ROOM_ID)
									.toLowerCase(Locale.getDefault()).trim() : "";
									final int curNbVotes = cursor.getInt(SessionsQuery.NB_VOTES);
									final int curMyVote = cursor.getInt(SessionsQuery.MY_VOTE);
									final int curIsFavorite = cursor.getInt(SessionsQuery.IS_FAVORITE);
									final String newTitle = session.has(TAG_TITLE) ? session.getString(TAG_TITLE).toLowerCase(Locale.getDefault()).trim() : curTitle;
									final String newSummary = session.has(TAG_SUMMARY) ? session.getString(TAG_SUMMARY).toLowerCase(Locale.getDefault()).trim() : curSummary;
									final String newDesc = session.has(TAG_DESC) ? session.getString(TAG_DESC).toLowerCase(Locale.getDefault()).trim() : curDesc;
									// final String newTime = session.has(TAG_TIME) ? session.getString(TAG_TIME).toLowerCase(Locale.getDefault()).trim() : curTime;
									final String newTrackId = session.has(TAG_ROOM) ? session.getString(TAG_ROOM).toLowerCase(Locale.getDefault()).trim() : curTrackId;
									final int newNbVotes = session.has(TAG_NB_VOTES) ? session.getInt(TAG_NB_VOTES) : curNbVotes;
									final int newMyVote = session.has(TAG_MY_VOTE) ? session.getBoolean(TAG_MY_VOTE) ? 1 : 0 : curMyVote;
									final int newIsFavorite = session.has(TAG_IS_FAVORITE) ? session.getInt(TAG_IS_FAVORITE) : curIsFavorite;

									return !curTitle.equals(newTitle) || !curSummary.equals(newSummary) || !curDesc.equals(newDesc)// || !curTime.equals(newTime)
											|| !curTrackId.equals(newTrackId) || curNbVotes != newNbVotes || curMyVote != newMyVote || curIsFavorite != newIsFavorite;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private interface SessionsQuery {
		String[] PROJECTION = { MixItContract.Sessions.SESSION_ID, MixItContract.Sessions.TITLE, MixItContract.Sessions.SUMMARY, MixItContract.Sessions.DESC,
				// MixItContract.Sessions.TIME,
				MixItContract.Sessions.ROOM_ID, MixItContract.Sessions.NB_VOTES, MixItContract.Sessions.MY_VOTE, MixItContract.Sessions.IS_FAVORITE };

		int SESSION_ID = 0;
		int TITLE = 1;
		int SUMMARY = 2;
		int DESC = 3;
		// int TIME = 4;
		int ROOM_ID = 4;
		int NB_VOTES = 5;
		int MY_VOTE = 6;
		int IS_FAVORITE = 7;
	}

	interface InterestsQuery {
		String[] PROJECTION = { MixItContract.Interests.INTEREST_ID, };

		int INTEREST_ID = 0;
	}

	interface SpeakersQuery {
		String[] PROJECTION = { MixItContract.Members.MEMBER_ID, };

		int MEMBER_ID = 0;
	}

}
