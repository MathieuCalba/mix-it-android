package fr.mixit.android.provider;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import fr.mixit.android.MixItApplication;
import fr.mixit.android.model.Planning;
import fr.mixit.android.model.PlanningSlot;
import fr.mixit.android.provider.MixItContract.Sessions;
import fr.mixit.android.provider.MixItDatabase.MembersBadges;
import fr.mixit.android.provider.MixItDatabase.MembersInterests;
import fr.mixit.android.provider.MixItDatabase.MembersLinks;
import fr.mixit.android.provider.MixItDatabase.SessionsComments;
import fr.mixit.android.provider.MixItDatabase.SessionsInterests;
import fr.mixit.android.provider.MixItDatabase.SessionsSpeakers;
import fr.mixit.android.utils.SelectionBuilder;


public class MixItProvider extends ContentProvider {

	protected static final boolean DEBUG_MODE = MixItApplication.DEBUG_MODE;
	private static final String TAG = MixItProvider.class.getSimpleName();

	private static final int INTERESTS = 100;
	private static final int INTERESTS_ID = 101;
	private static final int INTERESTS_ID_SESSIONS = 102;
	private static final int INTERESTS_ID_LIGHTNINGS = 103;
	private static final int INTERESTS_ID_ALL_SESSIONS = 104;
	private static final int INTERESTS_ID_MEMBERS = 105;

	private static final int MEMBERS = 200;
	private static final int MEMBERS_ID = 201;
	private static final int MEMBERS_ID_INTERESTS = 202;
	private static final int MEMBERS_ID_INTERESTS_ID = 203;
	private static final int MEMBERS_ID_BADGES = 204;
	private static final int MEMBERS_ID_BADGES_ID = 205;
	private static final int MEMBERS_ID_SHARED_LINKS = 206;
	private static final int MEMBERS_ID_SHARED_LINKS_ID = 207;
	private static final int MEMBERS_ID_LINKS = 208;
	private static final int MEMBERS_ID_LINKS_ID = 209;
	private static final int MEMBERS_ID_LINKERS = 210;
	private static final int MEMBERS_ID_LINKERS_ID = 211;
	private static final int MEMBERS_ID_COMMENTS = 212;
	private static final int MEMBERS_ID_COMMENTS_ID = 213;
	private static final int SPEAKERS = 214;
	private static final int STAFF = 215;
	private static final int SPEAKERS_ID_SESSIONS = 216;
	private static final int SPEAKERS_ID_SESSIONS_ID = 217;
	private static final int SPEAKERS_ID_LIGHTNINGS = 218;
	private static final int SPEAKERS_ID_LIGHTNINGS_ID = 219;
	private static final int SPEAKERS_ID_TALKS = 220;

	private static final int SHARED_LINKS = 300;
	private static final int SHARED_LINKS_ID = 301;

	private static final int SESSIONS = 400;
	private static final int SESSIONS_ID = 401;
	private static final int SESSIONS_ID_INTERESTS = 402;
	private static final int SESSIONS_ID_INTERESTS_ID = 403;
	private static final int SESSIONS_ID_SPEAKERS = 404;
	private static final int SESSIONS_ID_SPEAKERS_ID = 405;
	private static final int SESSIONS_ID_COMMENTS = 406;
	private static final int SESSIONS_ID_COMMENTS_ID = 407;

	private static final int LIGHTNINGS = 500;
	// private static final int LIGHTNINGS_ID = 501;

	private static final int COMMENTS = 600;
	private static final int COMMENTS_ID = 601;

	private static final int ROOM_ID_SESSIONS = 700;
	private static final int ROOMS = 701;

	private static final int PLANNING = 800;

	protected static final String UNDERSCORE = "_";
	protected static final String SLASH = "/";
	protected static final String STAR = "*";
	protected static final String ALL = "all";

	protected MixItDatabase mOpenHelper;

	private static final UriMatcher sUriMatcher = buildUriMatcher();

	@Override
	public boolean onCreate() {
		final Context context = getContext();

		mOpenHelper = new MixItDatabase(context);
		return true;
	}

	/**
	 * Build and return a {@link UriMatcher} that catches all {@link Uri} variations supported by this {@link ContentProvider}.
	 */
	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = MixItContract.CONTENT_AUTHORITY;

		matcher.addURI(authority, MixItContract.PATH_INTERESTS, INTERESTS);
		matcher.addURI(authority, MixItContract.PATH_INTERESTS + SLASH + STAR, INTERESTS_ID);
		matcher.addURI(authority, MixItContract.PATH_INTERESTS + SLASH + STAR + SLASH + MixItContract.PATH_SESSIONS, INTERESTS_ID_SESSIONS);
		matcher.addURI(authority, MixItContract.PATH_INTERESTS + SLASH + STAR + SLASH + MixItContract.PATH_LIGHTNINGS, INTERESTS_ID_LIGHTNINGS);
		matcher.addURI(authority, MixItContract.PATH_INTERESTS + SLASH + STAR + SLASH + ALL + MixItContract.PATH_SESSIONS, INTERESTS_ID_ALL_SESSIONS);
		matcher.addURI(authority, MixItContract.PATH_INTERESTS + SLASH + STAR + SLASH + MixItContract.PATH_MEMBERS, INTERESTS_ID_MEMBERS);

		matcher.addURI(authority, MixItContract.PATH_MEMBERS, MEMBERS);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR, MEMBERS_ID);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_INTERESTS, MEMBERS_ID_INTERESTS);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_INTERESTS + SLASH + STAR, MEMBERS_ID_INTERESTS_ID);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_BADGES, MEMBERS_ID_BADGES);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_BADGES + SLASH + STAR, MEMBERS_ID_BADGES_ID);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_SHARED_LINKS, MEMBERS_ID_SHARED_LINKS);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_SHARED_LINKS + SLASH + STAR,
				MEMBERS_ID_SHARED_LINKS_ID);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_LINKS, MEMBERS_ID_LINKS);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_LINKS + SLASH + STAR, MEMBERS_ID_LINKS_ID);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_LINKERS, MEMBERS_ID_LINKERS);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_LINKERS + SLASH + STAR, MEMBERS_ID_LINKERS_ID);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_COMMENTS, MEMBERS_ID_COMMENTS);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_COMMENTS + SLASH + STAR, MEMBERS_ID_COMMENTS_ID);
		matcher.addURI(authority, MixItContract.PATH_SPEAKERS, SPEAKERS);
		matcher.addURI(authority, MixItContract.PATH_STAFF, STAFF);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_SESSIONS, SPEAKERS_ID_SESSIONS);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_SESSIONS + SLASH + STAR, SPEAKERS_ID_SESSIONS_ID);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_LIGHTNINGS, SPEAKERS_ID_LIGHTNINGS);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_LIGHTNINGS + SLASH + STAR, SPEAKERS_ID_LIGHTNINGS_ID);
		matcher.addURI(authority, MixItContract.PATH_MEMBERS + SLASH + STAR + SLASH + MixItContract.PATH_TALKS, SPEAKERS_ID_TALKS);

		matcher.addURI(authority, MixItContract.PATH_SHARED_LINKS, SHARED_LINKS);
		matcher.addURI(authority, MixItContract.PATH_SHARED_LINKS + SLASH + STAR, SHARED_LINKS_ID);

		matcher.addURI(authority, MixItContract.PATH_SESSIONS, SESSIONS);
		matcher.addURI(authority, MixItContract.PATH_SESSIONS + SLASH + STAR, SESSIONS_ID);
		matcher.addURI(authority, MixItContract.PATH_SESSIONS + SLASH + STAR + SLASH + MixItContract.PATH_INTERESTS, SESSIONS_ID_INTERESTS);
		matcher.addURI(authority, MixItContract.PATH_SESSIONS + SLASH + STAR + SLASH + MixItContract.PATH_INTERESTS + SLASH + STAR, SESSIONS_ID_INTERESTS_ID);
		matcher.addURI(authority, MixItContract.PATH_SESSIONS + SLASH + STAR + SLASH + MixItContract.PATH_MEMBERS, SESSIONS_ID_SPEAKERS);
		matcher.addURI(authority, MixItContract.PATH_SESSIONS + SLASH + STAR + SLASH + MixItContract.PATH_MEMBERS + SLASH + STAR, SESSIONS_ID_SPEAKERS_ID);
		matcher.addURI(authority, MixItContract.PATH_SESSIONS + SLASH + STAR + SLASH + MixItContract.PATH_COMMENTS, SESSIONS_ID_COMMENTS);
		matcher.addURI(authority, MixItContract.PATH_SESSIONS + SLASH + STAR + SLASH + MixItContract.PATH_COMMENTS + SLASH + STAR, SESSIONS_ID_COMMENTS_ID);

		matcher.addURI(authority, MixItContract.PATH_LIGHTNINGS, LIGHTNINGS);
		// matcher.addURI(authority, MixItContract.PATH_LIGHTNINGS + SLASH + STAR, LIGHTNINGS_ID);
		// matcher.addURI(authority, MixItContract.PATH_LIGHTNINGS + SLASH + STAR + SLASH + MixItContract.PATH_INTERESTS, LIGHTNINGS_ID_INTERESTS);
		// matcher.addURI(authority, MixItContract.PATH_LIGHTNINGS + SLASH + STAR + SLASH + MixItContract.PATH_INTERESTS + SLASH + STAR,
		// LIGHTNINGS_ID_INTERESTS_ID);
		// matcher.addURI(authority, MixItContract.PATH_LIGHTNINGS + SLASH + STAR + SLASH + MixItContract.PATH_MEMBERS, LIGHTNINGS_ID_SPEAKERS);
		// matcher.addURI(authority, MixItContract.PATH_LIGHTNINGS + SLASH + STAR + SLASH + MixItContract.PATH_MEMBERS + SLASH + STAR,
		// LIGHTNINGS_ID_SPEAKERS_ID);
		// matcher.addURI(authority, MixItContract.PATH_LIGHTNINGS + SLASH + STAR + SLASH + MixItContract.PATH_COMMENTS, LIGHTNINGS_ID_COMMENTS);
		// matcher.addURI(authority, MixItContract.PATH_LIGHTNINGS + SLASH + STAR + SLASH + MixItContract.PATH_COMMENTS + SLASH + STAR,
		// LIGHTNINGS_ID_COMMENTS_ID);

		matcher.addURI(authority, MixItContract.PATH_COMMENTS, COMMENTS);
		matcher.addURI(authority, MixItContract.PATH_COMMENTS + SLASH + STAR, COMMENTS_ID);

		matcher.addURI(authority, MixItContract.PATH_ROOM, ROOMS);
		matcher.addURI(authority, MixItContract.PATH_ROOM + SLASH + STAR + SLASH + MixItContract.PATH_SESSIONS, ROOM_ID_SESSIONS);

		matcher.addURI(authority, MixItContract.PATH_PLANNING, PLANNING);

		return matcher;
	}

	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case INTERESTS:
				return MixItContract.Interests.CONTENT_TYPE;
			case INTERESTS_ID:
				return MixItContract.Interests.CONTENT_ITEM_TYPE;
			case INTERESTS_ID_LIGHTNINGS:
			case INTERESTS_ID_SESSIONS:
			case INTERESTS_ID_ALL_SESSIONS:
				return MixItContract.Sessions.CONTENT_TYPE;
			case INTERESTS_ID_MEMBERS:
				return MixItContract.Members.CONTENT_TYPE;
			case MEMBERS:
				return MixItContract.Members.CONTENT_TYPE;
			case MEMBERS_ID:
				return MixItContract.Members.CONTENT_ITEM_TYPE;
			case MEMBERS_ID_BADGES:
				return MixItDatabase.Badges.CONTENT_TYPE;
			case MEMBERS_ID_BADGES_ID:
				return MixItDatabase.Badges.CONTENT_ITEM_TYPE;
			case MEMBERS_ID_INTERESTS:
				return MixItContract.Interests.CONTENT_TYPE;
			case MEMBERS_ID_INTERESTS_ID:
				return MixItContract.Interests.CONTENT_ITEM_TYPE;
			case MEMBERS_ID_SHARED_LINKS:
				return MixItContract.SharedLinks.CONTENT_TYPE;
			case MEMBERS_ID_SHARED_LINKS_ID:
				return MixItContract.SharedLinks.CONTENT_ITEM_TYPE;
			case MEMBERS_ID_LINKS:
				return MixItContract.Members.CONTENT_TYPE;
			case MEMBERS_ID_LINKS_ID:
				return MixItContract.Members.CONTENT_ITEM_TYPE;
			case MEMBERS_ID_LINKERS:
				return MixItContract.Members.CONTENT_TYPE;
			case MEMBERS_ID_LINKERS_ID:
				return MixItContract.Members.CONTENT_ITEM_TYPE;
			case MEMBERS_ID_COMMENTS:
				return MixItContract.Comments.CONTENT_TYPE;
			case MEMBERS_ID_COMMENTS_ID:
				return MixItContract.Comments.CONTENT_ITEM_TYPE;
			case SPEAKERS:
			case STAFF:
				return MixItContract.Members.CONTENT_TYPE;
			case SPEAKERS_ID_LIGHTNINGS:
			case SPEAKERS_ID_SESSIONS:
			case SPEAKERS_ID_TALKS:
				return MixItContract.Sessions.CONTENT_TYPE;
			case SPEAKERS_ID_LIGHTNINGS_ID:
			case SPEAKERS_ID_SESSIONS_ID:
				return MixItContract.Sessions.CONTENT_ITEM_TYPE;
			case SHARED_LINKS:
				return MixItContract.SharedLinks.CONTENT_TYPE;
			case SHARED_LINKS_ID:
				return MixItContract.SharedLinks.CONTENT_ITEM_TYPE;
			case LIGHTNINGS:
			case SESSIONS:
				return MixItContract.Sessions.CONTENT_TYPE;
				// case LIGHTNINGS_ID:
			case SESSIONS_ID:
				return MixItContract.Sessions.CONTENT_ITEM_TYPE;
				// case LIGHTNINGS_ID_SPEAKERS:
			case SESSIONS_ID_SPEAKERS:
				return MixItContract.Members.CONTENT_TYPE;
				// case LIGHTNINGS_ID_SPEAKERS_ID:
			case SESSIONS_ID_SPEAKERS_ID:
				return MixItContract.Members.CONTENT_ITEM_TYPE;
				// case LIGHTNINGS_ID_INTERESTS:
			case SESSIONS_ID_INTERESTS:
				return MixItContract.Interests.CONTENT_TYPE;
				// case LIGHTNINGS_ID_INTERESTS_ID:
			case SESSIONS_ID_INTERESTS_ID:
				return MixItContract.Interests.CONTENT_ITEM_TYPE;
				// case LIGHTNINGS_ID_COMMENTS:
			case SESSIONS_ID_COMMENTS:
				return MixItContract.Comments.CONTENT_TYPE;
				// case LIGHTNINGS_ID_COMMENTS_ID:
			case SESSIONS_ID_COMMENTS_ID:
				return MixItContract.Comments.CONTENT_ITEM_TYPE;
			case COMMENTS:
				return MixItContract.Comments.CONTENT_TYPE;
			case COMMENTS_ID:
				return MixItContract.Comments.CONTENT_ITEM_TYPE;
			case ROOM_ID_SESSIONS:
			case ROOMS:
				return MixItContract.Sessions.CONTENT_TYPE;
			case PLANNING:
				return MixItContract.Sessions.CONTENT_TYPE;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
		if (DEBUG_MODE) {
			Log.d(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
		}

		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		final int match = sUriMatcher.match(uri);
		switch (match) {
			case PLANNING: {
				final SelectionBuilder builder = new SelectionBuilder() //
				.table(MixItDatabase.Tables.SESSIONS)//
				.where(MixItContract.Sessions.FORMAT + "=? OR " + MixItContract.Sessions.FORMAT + "=?", //
						MixItContract.Sessions.FORMAT_TALK, MixItContract.Sessions.FORMAT_WORKSHOP) //
						.where(MixItContract.Sessions.IS_FAVORITE + "=?", String.valueOf(1));

				final Cursor cursor = builder.query(db, MixItContract.Sessions.PROJ_LIST.PROJECTION, sort);

				final MatrixCursor fullCursor = new MatrixCursor(MixItContract.Sessions.PROJ_PLANNING.PROJECTION);

				if (cursor != null && cursor.moveToFirst()) {
					final Context ctx = getContext();

					final int i = 1;
					final long nextStart = Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.EIGHT_AM;
					final long nextEnd = nextStart + Planning.ONE_HOUR_AND_HALF;
					addSessions(ctx, fullCursor, cursor, nextStart, nextEnd, i);
				}

				fullCursor.setNotificationUri(getContext().getContentResolver(), uri);
				return fullCursor;
			}

			default:
				final SelectionBuilder builder = buildExpandedSelection(uri, match);
				final Cursor cursor = builder.where(selection, selectionArgs).query(db, projection, sort);
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
				return cursor;
		}
	}

	private void addSessions(Context ctx, MatrixCursor newCursor, Cursor oldCursor, long previousSlotEnd, long nextSlotEnd, int i) {
		long currentHour = previousSlotEnd;

		if (currentHour == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.BREAKFAST_START) {
			PlanningSlot slot = new PlanningSlot.BreakfastSlot(ctx, currentHour, Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.WELCOME_START);
			i = addSessionToCursor(newCursor, slot, i);
			currentHour = Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.WELCOME_START;

			slot = new PlanningSlot.WelcomeSlot(ctx, currentHour, Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.KEYNOTE_MORNING_START);
			i = addSessionToCursor(newCursor, slot, i);
			currentHour = Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.KEYNOTE_MORNING_START;

			slot = new PlanningSlot.KeynoteSlot(ctx, currentHour, Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.PITCH_START);
			i = addSessionToCursor(newCursor, slot, i);
			currentHour = Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.PITCH_START;

			slot = new PlanningSlot.TalksPresentationSlot(ctx, currentHour, Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.TALKS_MORNING_START);
			i = addSessionToCursor(newCursor, slot, i);
			currentHour = Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.TALKS_MORNING_START;

			previousSlotEnd = currentHour;
			nextSlotEnd = currentHour + Planning.TALKS_SLOT_LENGTH;
		}

		final PlanningSlot planningSlot = getNextSessionSlot(ctx, newCursor, oldCursor, previousSlotEnd, nextSlotEnd, i);

		final long currentSlotStart = planningSlot.getStart();
		final long currentSlotEnd = planningSlot.getEnd();
		currentHour = currentSlotEnd;

		if (currentSlotStart != previousSlotEnd) {
			if (currentSlotStart == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.ONE_PM + Planning.THIRTY_MINUTES && //
					previousSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.ONE_PM || //
					currentSlotStart == Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.ONE_PM + Planning.THIRTY_MINUTES && //
					previousSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.ONE_PM) {
				final PlanningSlot slot = new PlanningSlot.LightningTalkSlot(ctx, currentSlotStart - Planning.THIRTY_MINUTES, currentSlotStart);
				i = addSessionToCursor(newCursor, slot, i);
			} else if (previousSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.TWO_PM + Planning.THIRTY_MINUTES && //
					currentSlotStart == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.THREE_PM || //
					previousSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.TWO_PM + Planning.THIRTY_MINUTES && //
					currentSlotStart == Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.THREE_PM || //
					previousSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.FOUR_PM && //
					currentSlotStart == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.FOUR_PM + Planning.THIRTY_MINUTES || //
					previousSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.FOUR_PM && //
					currentSlotStart == Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.FOUR_PM + Planning.THIRTY_MINUTES || //
					previousSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.FIVE_PM + Planning.THIRTY_MINUTES && //
					currentSlotStart == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.SIX_PM || //
					previousSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.FIVE_PM + Planning.THIRTY_MINUTES && //
					currentSlotStart == Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.SIX_PM) {
				final PlanningSlot slot = new PlanningSlot.BreakSlot(ctx, currentSlotStart - Planning.THIRTY_MINUTES, currentSlotStart);
				i = addSessionToCursor(newCursor, slot, i);
			}
		}

		i = addSessionToCursor(newCursor, planningSlot, i);

		if (currentSlotEnd != nextSlotEnd) {
			if (currentSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.TEN_AM + Planning.FORTY_FIVE_MINUTES && //
					nextSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.ELEVEN_AM + Planning.FIFTEEN_MINUTES || //
					currentSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.TEN_AM + Planning.FORTY_FIVE_MINUTES && //
					nextSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.ELEVEN_AM + Planning.FIFTEEN_MINUTES) {
				final PlanningSlot slot = new PlanningSlot.BreakSlot(ctx, currentHour, currentHour + Planning.THIRTY_MINUTES);
				i = addSessionToCursor(newCursor, slot, i);
				currentHour += Planning.BREAK_SLOT_LENGTH;
			} else if (currentSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.TWELVE_PM + Planning.FIFTEEN_MINUTES || //
					currentSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.TWELVE_PM + Planning.FIFTEEN_MINUTES) {
				final PlanningSlot slot = new PlanningSlot.LunchSlot(ctx, currentHour, currentHour + Planning.FORTY_FIVE_MINUTES);
				i = addSessionToCursor(newCursor, slot, i);
				currentHour += Planning.FORTY_FIVE_MINUTES;
			} else if (currentSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.TWELVE_PM + Planning.FORTY_FIVE_MINUTES || //
					currentSlotEnd == Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.TWELVE_PM + Planning.FORTY_FIVE_MINUTES) {
				final PlanningSlot slot = new PlanningSlot.LunchSlot(ctx, currentHour, currentHour + Planning.FIFTEEN_MINUTES);
				i = addSessionToCursor(newCursor, slot, i);
				currentHour += Planning.FIFTEEN_MINUTES;
			}
		}

		if (currentHour == Planning.TIMESTAMP_OFFSET_DAY_ONE + Planning.SEVEN_PM) {
			// TODO : add Mix-IT Party
			currentHour = Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.BREAKFAST_START;

			PlanningSlot slot = new PlanningSlot.BreakfastSlot(ctx, currentHour, Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.WELCOME_START);
			i = addSessionToCursor(newCursor, slot, i);
			currentHour = Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.WELCOME_START;

			slot = new PlanningSlot.WelcomeSlot(ctx, currentHour, Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.KEYNOTE_MORNING_START);
			i = addSessionToCursor(newCursor, slot, i);
			currentHour = Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.KEYNOTE_MORNING_START;

			slot = new PlanningSlot.KeynoteSlot(ctx, currentHour, Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.PITCH_START);
			i = addSessionToCursor(newCursor, slot, i);
			currentHour = Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.PITCH_START;

			slot = new PlanningSlot.TalksPresentationSlot(ctx, currentHour, Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.TALKS_MORNING_START);
			i = addSessionToCursor(newCursor, slot, i);
			currentHour = Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.TALKS_MORNING_START;
		}

		if (currentHour == Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.SIX_PM) {
			final PlanningSlot slot = new PlanningSlot.KeynoteSlot(ctx, currentHour, currentHour + Planning.THIRTY_MINUTES);
			i = addSessionToCursor(newCursor, slot, i);
			currentHour += Planning.THIRTY_MINUTES;
		}

		if (currentHour < Planning.TIMESTAMP_OFFSET_DAY_TWO + Planning.SIX_PM + Planning.THIRTY_MINUTES) {
			addSessions(ctx, newCursor, oldCursor, currentHour, currentHour + Planning.ONE_HOUR_AND_HALF, i);
		}
	}

	private PlanningSlot getNextSessionSlot(Context ctx, MatrixCursor newCursor, Cursor cursor, long nextStart, long nextEnd, int i) {
		final PlanningSlot planningSlot = new PlanningSlot.SessionSlot(cursor);
		if (isSessionInThisSlot(planningSlot, nextStart, nextEnd)) {
			return addSession(ctx, planningSlot, cursor);
		} else {
			final PlanningSlot s = new PlanningSlot.NoSessionSlot(ctx, nextStart, nextEnd);
			return s;
		}
	}

	private PlanningSlot addSession(Context ctx, PlanningSlot planningSlot, Cursor cursor) {
		if (cursor.moveToNext()) {
			final PlanningSlot newSession = new PlanningSlot.SessionSlot(cursor);

			if (areSessionsMultiple(planningSlot, newSession)) {
				planningSlot.addSession(ctx, newSession);
				return addSession(ctx, planningSlot, cursor);
			}
		}

		return planningSlot;
	}

	private int addSessionToCursor(MatrixCursor newCursor, PlanningSlot planningSlot, int i) {
		newCursor.newRow() //
		.add(i++) // _ID
		.add(planningSlot.getSessionId()) // SESSION_ID
		.add(planningSlot.getTitle()) // TITLE
		.add(planningSlot.getStart()) // START
		.add(planningSlot.getEnd()) // END
		.add(planningSlot.getRoomId()) // ROOM_ID
		.add(planningSlot.getFormat()) // FORMAT
		.add(planningSlot.getLevel()) // LEVEL
		.add(planningSlot.getLang()) // LANG
		.add(planningSlot.getNbConcurrent()) // NB_CONCURRENT_TALKS
		.add(planningSlot.getSlotType()); // NB_CONCURRENT_TALKS

		return i;
	}

	private boolean isSessionInThisSlot(PlanningSlot planningSlot, long slotStart, long slotEnd) {
		return planningSlot.getStart() == slotStart || planningSlot.getEnd() == slotEnd;
	}

	private boolean areSessionsMultiple(PlanningSlot sessionOri, PlanningSlot sessionNew) {
		return sessionOri.getStart() == sessionNew.getStart() || sessionOri.getEnd() == sessionNew.getEnd();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (DEBUG_MODE) {
			Log.d(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
		}

		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);

		switch (match) {
			case INTERESTS:
			case INTERESTS_ID:
				db.insertOrThrow(MixItDatabase.Tables.INTERESTS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Interests.buildInterestUri(values.getAsString(MixItContract.Interests.INTEREST_ID));
			case SPEAKERS:
			case STAFF:
			case MEMBERS:
			case MEMBERS_ID:
				db.insertOrThrow(MixItDatabase.Tables.MEMBERS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Members.buildMemberUri(values.getAsString(MixItContract.Members.MEMBER_ID));
			case MEMBERS_ID_BADGES:
			case MEMBERS_ID_BADGES_ID:
				db.insertOrThrow(MixItDatabase.Tables.MEMBERS_BADGES, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				// should be the badge's uri, but no table for badges, so member's uri
				return MixItContract.Members.buildMemberBadgeUri(values.getAsString(MixItDatabase.MembersBadges.MEMBER_ID),
						values.getAsString(MixItDatabase.MembersBadges.BADGE_ID));
			case MEMBERS_ID_INTERESTS:
			case MEMBERS_ID_INTERESTS_ID:
				db.insertOrThrow(MixItDatabase.Tables.MEMBERS_INTERESTS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Interests.buildInterestUri(values.getAsString(MixItDatabase.MembersInterests.INTEREST_ID));
			case MEMBERS_ID_LINKS:
			case MEMBERS_ID_LINKS_ID:
				db.insertOrThrow(MixItDatabase.Tables.MEMBERS_LINKS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Members.buildMemberUri(values.getAsString(MixItDatabase.MembersLinks.LINK_ID));
			case MEMBERS_ID_LINKERS:
			case MEMBERS_ID_LINKERS_ID:
				db.insertOrThrow(MixItDatabase.Tables.MEMBERS_LINKS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Members.buildMemberUri(values.getAsString(MixItDatabase.MembersLinks.MEMBER_ID));
			case MEMBERS_ID_COMMENTS:
			case MEMBERS_ID_COMMENTS_ID:
				db.insertOrThrow(MixItDatabase.Tables.COMMENTS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Comments.buildCommentUri(values.getAsString(MixItContract.Comments.COMMENT_ID));
			case SPEAKERS_ID_TALKS:
			case SPEAKERS_ID_SESSIONS:
			case SPEAKERS_ID_SESSIONS_ID:
				db.insertOrThrow(MixItDatabase.Tables.SESSIONS_SPEAKERS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Sessions.buildSessionUri(values.getAsString(MixItDatabase.SessionsSpeakers.SESSION_ID));
			case SPEAKERS_ID_LIGHTNINGS:
			case SPEAKERS_ID_LIGHTNINGS_ID:
				db.insertOrThrow(MixItDatabase.Tables.SESSIONS_SPEAKERS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Sessions.buildSessionUri(values.getAsString(MixItDatabase.SessionsSpeakers.SESSION_ID));
			case SHARED_LINKS:
			case SHARED_LINKS_ID:
				db.insertOrThrow(MixItDatabase.Tables.SHARED_LINKS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.SharedLinks.buildSharedLinkUri(values.getAsString(MixItContract.SharedLinks.SHARED_LINK_ID));
			case SESSIONS:
			case SESSIONS_ID:
				db.insertOrThrow(MixItDatabase.Tables.SESSIONS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Sessions.buildSessionUri(values.getAsString(MixItContract.Sessions.SESSION_ID));
			case SESSIONS_ID_SPEAKERS:
			case SESSIONS_ID_SPEAKERS_ID:
				// case LIGHTNINGS_ID_SPEAKERS:
				// case LIGHTNINGS_ID_SPEAKERS_ID:
				db.insertOrThrow(MixItDatabase.Tables.SESSIONS_SPEAKERS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Members.buildMemberUri(values.getAsString(MixItDatabase.SessionsSpeakers.SPEAKER_ID));
			case SESSIONS_ID_INTERESTS:
			case SESSIONS_ID_INTERESTS_ID:
				// case LIGHTNINGS_ID_INTERESTS:
				// case LIGHTNINGS_ID_INTERESTS_ID:
				db.insertOrThrow(MixItDatabase.Tables.SESSIONS_INTERESTS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Interests.buildInterestUri(values.getAsString(MixItDatabase.SessionsInterests.INTEREST_ID));
			case SESSIONS_ID_COMMENTS:
			case SESSIONS_ID_COMMENTS_ID:
				// case LIGHTNINGS_ID_COMMENTS:
				// case LIGHTNINGS_ID_COMMENTS_ID:
				db.insertOrThrow(MixItDatabase.Tables.SESSIONS_COMMENTS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Comments.buildCommentUri(values.getAsString(MixItDatabase.SessionsComments.COMMENT_ID));
			case LIGHTNINGS:
				// case LIGHTNINGS_ID:
				db.insertOrThrow(MixItDatabase.Tables.SESSIONS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Sessions.buildSessionUri(values.getAsString(MixItContract.Sessions.SESSION_ID));
			case COMMENTS:
			case COMMENTS_ID:
				db.insertOrThrow(MixItDatabase.Tables.COMMENTS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return MixItContract.Comments.buildCommentUri(values.getAsString(MixItContract.Comments.COMMENT_ID));
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		if (DEBUG_MODE) {
			Log.d(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		final int retVal = builder.where(selection, selectionArgs).update(db, values);
		getContext().getContentResolver().notifyChange(uri, null);
		return retVal;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if (DEBUG_MODE) {
			Log.d(TAG, "delete(uri=" + uri + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		final int retVal = builder.where(selection, selectionArgs).delete(db);
		getContext().getContentResolver().notifyChange(uri, null);
		return retVal;
	}

	/**
	 * Apply the given set of {@link ContentProviderOperation}, executing inside a {@link SQLiteDatabase} transaction. All changes will be rolled back if any
	 * single one fails.
	 */
	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			final int numOperations = operations.size();
			final ContentProviderResult[] results = new ContentProviderResult[numOperations];
			for (int i = 0; i < numOperations; i++) {
				results[i] = operations.get(i).apply(this, results, i);
			}
			db.setTransactionSuccessful();
			return results;
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Build a simple {@link SelectionBuilder} to match the requested {@link Uri}. This is usually enough to support {@link #insert}, {@link #update}, and
	 * {@link #delete} operations.
	 */
	private SelectionBuilder buildSimpleSelection(Uri uri) {
		final SelectionBuilder builder = new SelectionBuilder();
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case INTERESTS:
				return builder//
						.table(MixItDatabase.Tables.INTERESTS);
			case INTERESTS_ID: {
				final String interestId = MixItContract.Interests.getInterestId(uri);
				return builder//
						.table(MixItDatabase.Tables.INTERESTS)//
						.where(MixItContract.Interests.INTEREST_ID + "=?", interestId);
			}
			case MEMBERS:
				return builder//
						.table(MixItDatabase.Tables.MEMBERS);
			case MEMBERS_ID: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS)//
						.where(MixItContract.Members.MEMBER_ID + "=?", memberId);
			}
			case MEMBERS_ID_BADGES: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder //
						.table(MixItDatabase.Tables.MEMBERS_BADGES)//
						.where(MembersBadges.MEMBER_ID + "=?", memberId);
			}
			case MEMBERS_ID_BADGES_ID: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				final String badgeId = MixItContract.Members.getBadgeId(uri);
				return builder //
						.table(MixItDatabase.Tables.MEMBERS_BADGES)//
						.where(MembersBadges.MEMBER_ID + "=?", memberId)//
						.where(MembersBadges.BADGE_ID + "=?", badgeId);
			}
			case MEMBERS_ID_INTERESTS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS_INTERESTS)//
						.where(MembersInterests.MEMBER_ID + "=?", memberId);
			}
			case MEMBERS_ID_INTERESTS_ID: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				final String interestId = MixItContract.Members.getInterestId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS_INTERESTS)//
						.where(MembersInterests.MEMBER_ID + "=?", memberId)//
						.where(MembersInterests.INTEREST_ID + "=?", interestId);
			}
			case MEMBERS_ID_SHARED_LINKS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.SHARED_LINKS)//
						.where(MixItContract.SharedLinks.MEMBER_ID + "=?", memberId);
			}
			case MEMBERS_ID_SHARED_LINKS_ID: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				final String sharedLinkId = MixItContract.Members.getSharedLinkId(uri);
				return builder//
						.table(MixItDatabase.Tables.SHARED_LINKS)//
						.where(MixItContract.SharedLinks.MEMBER_ID + "=?", memberId)//
						.where(MixItContract.SharedLinks.SHARED_LINK_ID + "=?", sharedLinkId);
			}
			case MEMBERS_ID_LINKS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS_LINKS)//
						.where(MembersLinks.MEMBER_ID + "=?", memberId);
			}
			case MEMBERS_ID_LINKS_ID: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				final String linkId = MixItContract.Members.getLinkId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS_LINKS)//
						.where(MembersLinks.MEMBER_ID + "=?", memberId)//
						.where(MembersLinks.LINK_ID + "=?", linkId);
			}
			case MEMBERS_ID_LINKERS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS_LINKS)//
						.where(MembersLinks.LINK_ID + "=?", memberId);
			}
			case MEMBERS_ID_LINKERS_ID: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				final String linkerId = MixItContract.Members.getLinkerId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS_LINKS)//
						.where(MembersLinks.LINK_ID + "=?", memberId)//
						.where(MembersLinks.MEMBER_ID + "=?", linkerId);
			}
			case MEMBERS_ID_COMMENTS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.COMMENTS)//
						.where(MixItContract.Comments.AUTHOR_ID + "=?", memberId);
			}
			case MEMBERS_ID_COMMENTS_ID: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				final String commentId = MixItContract.Members.getCommentId(uri);
				return builder//
						.table(MixItDatabase.Tables.COMMENTS)//
						.where(MixItContract.Comments.AUTHOR_ID + "=?", memberId)//
						.where(MixItContract.Comments.COMMENT_ID + "=?", commentId);
			}
			case SPEAKERS:
			case STAFF: {
				return builder//
						.table(MixItDatabase.Tables.MEMBERS);
			}
			case SPEAKERS_ID_LIGHTNINGS:
			case SPEAKERS_ID_SESSIONS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_SPEAKERS)//
						.where(SessionsSpeakers.SPEAKER_ID + "=?", memberId);
			}
			case SPEAKERS_ID_TALKS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_SPEAKERS)//
						.where(SessionsSpeakers.SPEAKER_ID + "=?", memberId);
			}
			case SPEAKERS_ID_LIGHTNINGS_ID:
			case SPEAKERS_ID_SESSIONS_ID: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				final String sessionId = MixItContract.Members.getSessionId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_SPEAKERS)//
						.where(SessionsSpeakers.SPEAKER_ID + "=?", memberId)//
						.where(SessionsSpeakers.SESSION_ID + "=?", sessionId);
			}
			case SHARED_LINKS:
				return builder//
						.table(MixItDatabase.Tables.SHARED_LINKS);
			case SHARED_LINKS_ID: {
				final String sharedLinkId = MixItContract.SharedLinks.getSharedLinkId(uri);
				return builder//
						.table(MixItDatabase.Tables.SHARED_LINKS)//
						.where(MixItContract.SharedLinks.SHARED_LINK_ID + "=?", sharedLinkId);
			}
			case SESSIONS:
				return builder//
						.table(MixItDatabase.Tables.SESSIONS)//
						.where(MixItContract.Sessions.FORMAT + "=? OR " + MixItContract.Sessions.FORMAT + "=?", //
								MixItContract.Sessions.FORMAT_TALK, MixItContract.Sessions.FORMAT_WORKSHOP);
			case SESSIONS_ID: {
				final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS)//
						.where(MixItContract.Sessions.SESSION_ID + "=?", sessionsId);
			}
			case SESSIONS_ID_SPEAKERS: {
				final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_SPEAKERS)//
						.where(SessionsSpeakers.SESSION_ID + "=?", sessionsId);
			}
			case SESSIONS_ID_SPEAKERS_ID: {
				final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				final String speakerId = MixItContract.Sessions.getSpeakerId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_SPEAKERS)//
						.where(SessionsSpeakers.SESSION_ID + "=?", sessionsId)//
						.where(SessionsSpeakers.SPEAKER_ID + "=?", speakerId);
			}
			case SESSIONS_ID_INTERESTS: {
				final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_INTERESTS)//
						.where(SessionsInterests.SESSION_ID + "=?", sessionsId);
			}
			case SESSIONS_ID_INTERESTS_ID: {
				final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				final String interestId = MixItContract.Sessions.getInterestIdFromSessionInterests(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_INTERESTS)//
						.where(SessionsInterests.SESSION_ID + "=?", sessionsId)//
						.where(SessionsInterests.INTEREST_ID + "=?", interestId);
			}
			case SESSIONS_ID_COMMENTS: {
				final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_COMMENTS)//
						.where(SessionsComments.SESSION_ID + "=?", sessionsId);
			}
			case SESSIONS_ID_COMMENTS_ID: {
				final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				final String commentId = MixItContract.Sessions.getCommentId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_COMMENTS)//
						.where(SessionsComments.SESSION_ID + "=?", sessionsId)//
						.where(SessionsComments.COMMENT_ID + "=?", commentId);
			}
			case LIGHTNINGS:
				return builder//
						.table(MixItDatabase.Tables.SESSIONS)//
						.where(MixItContract.Sessions.FORMAT + "=?", MixItContract.Sessions.FORMAT_LIGHTNING_TALK);
				// case LIGHTNINGS_ID: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(MixItContract.Sessions.SESSION_ID + "=?", sessionsId);
				// }
				// case LIGHTNINGS_ID_SPEAKERS: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS_SPEAKERS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(SessionsSpeakers.SESSION_ID + "=?", sessionsId);
				// }
				// case LIGHTNINGS_ID_SPEAKERS_ID: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// final String speakerId = MixItContract.Sessions.getSpeakerId(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS_SPEAKERS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(SessionsSpeakers.SESSION_ID + "=?", sessionsId)
				// .where(SessionsSpeakers.SPEAKER_ID + "=?", speakerId);
				// }
				// case LIGHTNINGS_ID_INTERESTS: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS_INTERESTS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(SessionsInterests.SESSION_ID + "=?", sessionsId);
				// }
				// case LIGHTNINGS_ID_INTERESTS_ID: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// final String interestId = MixItContract.Sessions.getInterestIdFromSessionInterests(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS_INTERESTS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(SessionsInterests.SESSION_ID + "=?", sessionsId)
				// .where(SessionsInterests.INTEREST_ID + "=?", interestId);
				// }
				// case LIGHTNINGS_ID_COMMENTS: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS_COMMENTS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(SessionsComments.SESSION_ID + "=?", sessionsId);
				// }
				// case LIGHTNINGS_ID_COMMENTS_ID: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// final String commentId = MixItContract.Sessions.getCommentId(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS_COMMENTS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(SessionsComments.SESSION_ID + "=?", sessionsId)
				// .where(SessionsComments.COMMENT_ID + "=?", commentId);
				// }
			case COMMENTS:
				return builder//
						.table(MixItDatabase.Tables.COMMENTS);
			case COMMENTS_ID: {
				final String commentId = MixItContract.Comments.getCommentId(uri);
				return builder//
						.table(MixItDatabase.Tables.COMMENTS)//
						.where(MixItContract.Comments.COMMENT_ID + "=?", commentId);
			}
			case ROOM_ID_SESSIONS: {
				final String roomId = MixItContract.Sessions.getRoomId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS)//
						.where(Sessions.ROOM_ID + "=?", roomId);
			}
			case ROOMS: {
				return builder //
						.table(MixItDatabase.Tables.SESSIONS)//
						.groupBy(MixItContract.Sessions.ROOM_ID);
			}
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	/**
	 * Build an advanced {@link SelectionBuilder} to match the requested {@link Uri}. This is usually only used by {@link #query}, since it performs table joins
	 * useful for {@link Cursor} data.
	 */
	private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
		final SelectionBuilder builder = new SelectionBuilder();
		switch (match) {
			case INTERESTS:
				return builder//
						.table(MixItDatabase.Tables.INTERESTS)//
						.map(MixItContract.Interests.SESSIONS_COUNT, Subquery.TAG_SESSIONS_COUNT)//
						.map(MixItContract.Interests.MEMBERS_COUNT, Subquery.TAG_MEMBERS_COUNT);
			case INTERESTS_ID: {
				final String interestId = MixItContract.Interests.getInterestId(uri);
				return builder//
						.table(MixItDatabase.Tables.INTERESTS)//
						.where(MixItContract.Interests.INTEREST_ID + "=?", interestId);
			}
			case INTERESTS_ID_SESSIONS: {
				final String interestId = MixItContract.Sessions.getInterestIdFromInterestSessions(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_INTERESTS_JOIN_SESSIONS)//
						.mapToTable(MixItContract.Sessions._ID, MixItDatabase.Tables.SESSIONS)//
						.mapToTable(MixItContract.Sessions.SESSION_ID, MixItDatabase.Tables.SESSIONS)//
						.where(MixItContract.Sessions.FORMAT + "=? OR " + MixItContract.Sessions.FORMAT + "=?", //
								MixItContract.Sessions.FORMAT_TALK, MixItContract.Sessions.FORMAT_WORKSHOP)//
								.where(MixItDatabase.SessionsInterests.INTEREST_ID + "=?", interestId);
			}
			case INTERESTS_ID_LIGHTNINGS: {
				final String interestId = MixItContract.Sessions.getInterestIdFromInterestSessions(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_INTERESTS_JOIN_SESSIONS)//
						.mapToTable(MixItContract.Sessions._ID, MixItDatabase.Tables.SESSIONS)//
						.mapToTable(MixItContract.Sessions.SESSION_ID, MixItDatabase.Tables.SESSIONS)//
						.where(MixItContract.Sessions.FORMAT + "=?", MixItContract.Sessions.FORMAT_LIGHTNING_TALK) //
						.where(MixItDatabase.SessionsInterests.INTEREST_ID + "=?", interestId);
			}
			case INTERESTS_ID_ALL_SESSIONS: {
				final String interestId = MixItContract.Interests.getInterestId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_INTERESTS_JOIN_SESSIONS)//
						.mapToTable(MixItContract.Sessions._ID, MixItDatabase.Tables.SESSIONS)//
						.mapToTable(MixItContract.Sessions.SESSION_ID, MixItDatabase.Tables.SESSIONS)//
						.where(MixItDatabase.SessionsInterests.INTEREST_ID + "=?", interestId);
			}
			case INTERESTS_ID_MEMBERS: {
				final String interestId = MixItContract.Interests.getInterestId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS_INTERESTS_JOIN_MEMBERS)//
						.mapToTable(MixItContract.Members._ID, MixItDatabase.Tables.MEMBERS)//
						.mapToTable(MixItContract.Members.MEMBER_ID, MixItDatabase.Tables.MEMBERS)//
						.where(MixItDatabase.MembersInterests.INTEREST_ID + "=?", interestId);
			}
			case MEMBERS:
				return builder//
						.table(MixItDatabase.Tables.MEMBERS);
			case MEMBERS_ID: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS)//
						.where(MixItDatabase.Tables.MEMBERS + "." + MixItContract.Members.MEMBER_ID + "=?", memberId);
			}
			case MEMBERS_ID_INTERESTS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS_INTERESTS_JOIN_INTERESTS)//
						.mapToTable(MixItContract.Interests._ID, MixItDatabase.Tables.INTERESTS)//
						.mapToTable(MixItContract.Interests.INTEREST_ID, MixItDatabase.Tables.INTERESTS)//
						// .map(MixItContract.Interests.MEMBERS_COUNT, Subquery.TAG_MEMBERS_COUNT)//
						.where(MixItDatabase.Tables.MEMBERS_INTERESTS + "." + MixItDatabase.MembersInterests.MEMBER_ID + "=?", memberId);
			}
			case MEMBERS_ID_BADGES: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS_BADGES_JOIN_MEMBERS)//
						.mapToTable(MixItContract.Members._ID, MixItDatabase.Tables.MEMBERS)//
						.mapToTable(MixItContract.Members.MEMBER_ID, MixItDatabase.Tables.MEMBERS)//
						.where(MixItDatabase.Tables.MEMBERS_BADGES + "." + MixItDatabase.MembersBadges.MEMBER_ID + "=?", memberId);
			}
			case MEMBERS_ID_SHARED_LINKS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.SHARED_LINKS)//
						.where(MixItContract.Members.MEMBER_ID + "=?", memberId);
			}
			case MEMBERS_ID_LINKS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS_LINKS_JOIN_MEMBERS)//
						.mapToTable(MixItContract.Members._ID, MixItDatabase.Tables.MEMBERS)//
						.mapToTable(MixItContract.Members.MEMBER_ID, MixItDatabase.Tables.MEMBERS)//
						.where(MixItDatabase.Tables.MEMBERS_LINKS + "." + MixItDatabase.MembersLinks.MEMBER_ID + "=?", memberId);
			}
			case MEMBERS_ID_LINKERS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS_LINKERS_JOIN_MEMBERS)//
						.mapToTable(MixItContract.Members._ID, MixItDatabase.Tables.MEMBERS)//
						.mapToTable(MixItContract.Members.MEMBER_ID, MixItDatabase.Tables.MEMBERS)//
						.where(MixItDatabase.Tables.MEMBERS_LINKS + "." + MixItDatabase.MembersLinks.LINK_ID + "=?", memberId);
			}
			case MEMBERS_ID_COMMENTS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.MEMBERS_JOIN_COMMENTS)//
						.mapToTable(MixItContract.Members._ID, MixItDatabase.Tables.MEMBERS)//
						.mapToTable(MixItContract.Members.MEMBER_ID, MixItDatabase.Tables.MEMBERS)//
						.where(MixItContract.Comments.AUTHOR_ID + "=?", memberId);
			}
			case SPEAKERS: {
				return builder.table(MixItDatabase.Tables.MEMBERS) //
						.where(MixItContract.Members.TYPE + "=?", String.valueOf(MixItContract.Members.TYPE_SPEAKER));
				// return builder.table(MixItDatabase.Tables.MEMBERS + ", " + MixItDatabase.Tables.SESSIONS + ", " + MixItDatabase.Tables.SESSIONS_SPEAKERS)//
				// .mapToTable(MixItContract.Members._ID, MixItDatabase.Tables.MEMBERS)//
				// .mapToTable(MixItContract.Members.MEMBER_ID, MixItDatabase.Tables.MEMBERS)//
				// .mapToTable(MixItContract.Sessions.SESSION_ID, MixItDatabase.Tables.SESSIONS)//
				// .where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + MixItDatabase.SessionsSpeakers.SPEAKER_ID + "=" //
				// + MixItDatabase.Tables.MEMBERS + "." + MixItContract.Members.MEMBER_ID, (String[]) null)//
				// .where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + MixItDatabase.SessionsSpeakers.SESSION_ID + "=" //
				// + MixItDatabase.Tables.SESSIONS + "." + MixItContract.Sessions.SESSION_ID, (String[]) null)//
				// .where(MixItContract.Sessions.FORMAT + "=? OR " + MixItContract.Sessions.FORMAT + "=?", //
				// MixItContract.Sessions.FORMAT_TALK, MixItContract.Sessions.FORMAT_WORKSHOP) //
				// .distinct();
			}
			case STAFF: {
				return builder.table(MixItDatabase.Tables.MEMBERS) //
						.where(MixItContract.Members.TYPE + "=?", String.valueOf(MixItContract.Members.TYPE_STAFF));
			}
			case SPEAKERS_ID_SESSIONS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_SPEAKERS_JOIN_SESSIONS)//
						.mapToTable(MixItContract.Sessions._ID, MixItDatabase.Tables.SESSIONS)//
						.mapToTable(MixItContract.Sessions.SESSION_ID, MixItDatabase.Tables.SESSIONS)//
						.where(MixItContract.Sessions.FORMAT + "=? OR " + MixItContract.Sessions.FORMAT + "=?", //
								MixItContract.Sessions.FORMAT_TALK, MixItContract.Sessions.FORMAT_WORKSHOP) //
								.where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + SessionsSpeakers.SPEAKER_ID + "=?", memberId);
			}
			// case SPEAKERS_ID_SESSIONS_ID: {
			// final String memberId = MixItContract.Members.getMemberId(uri);
			// final String sessionId = MixItContract.Members.getSessionId(uri);
			// return builder.table(MixItDatabase.Tables.SESSIONS_SPEAKERS_JOIN_SESSIONS)
			// .mapToTable(MixItContract.Sessions._ID, MixItDatabase.Tables.SESSIONS)
			// .mapToTable(MixItContract.Sessions.SESSION_ID, MixItDatabase.Tables.SESSIONS)
			// .where(MixItDatabase.Tables.SESSIONS + "." + MixItContract.Sessions.IS_SESSION + "=?", "1")
			// .where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + SessionsSpeakers.SPEAKER_ID + "=?", memberId)
			// .where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + SessionsSpeakers.SESSION_ID + "=?", sessionId);
			// }
			case SPEAKERS_ID_LIGHTNINGS: {
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_SPEAKERS_JOIN_SESSIONS)//
						.mapToTable(MixItContract.Sessions._ID, MixItDatabase.Tables.SESSIONS)//
						.mapToTable(MixItContract.Sessions.SESSION_ID, MixItDatabase.Tables.SESSIONS)//
						.where(MixItContract.Sessions.FORMAT + "=?", MixItContract.Sessions.FORMAT_LIGHTNING_TALK) //
						.where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + SessionsSpeakers.SPEAKER_ID + "=?", memberId);
			}
			// case SPEAKERS_ID_LIGHTNINGS_ID: {
			// final String memberId = MixItContract.Members.getMemberId(uri);
			// final String sessionId = MixItContract.Members.getSessionId(uri);
			// return builder.table(MixItDatabase.Tables.SESSIONS_SPEAKERS_JOIN_SESSIONS)
			// .mapToTable(MixItContract.Sessions._ID, MixItDatabase.Tables.SESSIONS)
			// .mapToTable(MixItContract.Sessions.SESSION_ID, MixItDatabase.Tables.SESSIONS)
			// .where(MixItDatabase.Tables.SESSIONS + "." + MixItContract.Sessions.IS_SESSION + "=?", "0")
			// .where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + SessionsSpeakers.SPEAKER_ID + "=?", memberId)
			// .where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + SessionsSpeakers.SESSION_ID + "=?", sessionId);
			// }
			case SPEAKERS_ID_TALKS:
				final String memberId = MixItContract.Members.getMemberId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_SPEAKERS_JOIN_SESSIONS)//
						.mapToTable(MixItContract.Sessions._ID, MixItDatabase.Tables.SESSIONS)//
						.mapToTable(MixItContract.Sessions.SESSION_ID, MixItDatabase.Tables.SESSIONS)//
						.where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + SessionsSpeakers.SPEAKER_ID + "=?", memberId);
			case SHARED_LINKS:
				return builder//
						.table(MixItDatabase.Tables.SHARED_LINKS);
			case SHARED_LINKS_ID: {
				final String sharedLinkId = MixItContract.SharedLinks.getSharedLinkId(uri);
				return builder//
						.table(MixItDatabase.Tables.SHARED_LINKS)//
						.where(MixItContract.SharedLinks.SHARED_LINK_ID + "=?", sharedLinkId);
			}
			case SESSIONS:
				return builder//
						.table(MixItDatabase.Tables.SESSIONS)//
						.where(MixItContract.Sessions.FORMAT + "=? OR " + MixItContract.Sessions.FORMAT + "=?", //
								MixItContract.Sessions.FORMAT_TALK, MixItContract.Sessions.FORMAT_WORKSHOP);
			case SESSIONS_ID: {
				final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS)//
						.where(MixItContract.Sessions.SESSION_ID + "=?", sessionsId);
			}
			case SESSIONS_ID_SPEAKERS: {
				final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_SPEAKERS_JOIN_MEMBERS)//
						.mapToTable(MixItContract.Members._ID, MixItDatabase.Tables.MEMBERS)//
						.mapToTable(MixItContract.Members.MEMBER_ID, MixItDatabase.Tables.MEMBERS)//
						.where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + SessionsSpeakers.SESSION_ID + "=?", sessionsId);
			}
			// case SESSIONS_ID_SPEAKERS_ID: {
			// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
			// final String speakerId = MixItContract.Sessions.getSpeakerId(uri);
			// return builder.table(MixItDatabase.Tables.SESSIONS_SPEAKERS_JOIN_MEMBERS)
			// .mapToTable(MixItContract.Sessions._ID, MixItDatabase.Tables.SESSIONS)
			// .mapToTable(MixItContract.Sessions.SESSION_ID, MixItDatabase.Tables.SESSIONS)
			// .mapToTable(MixItContract.Members._ID, MixItDatabase.Tables.MEMBERS)
			// .mapToTable(MixItContract.Members.MEMBER_ID, MixItDatabase.Tables.MEMBERS)
			// .where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + SessionsSpeakers.SESSION_ID + "=?", sessionsId)
			// .where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + SessionsSpeakers.SPEAKER_ID + "=?", speakerId);
			// }
			case SESSIONS_ID_INTERESTS: {
				final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_INTERESTS_JOIN_INTERESTS)//
						.mapToTable(MixItContract.Interests._ID, MixItDatabase.Tables.INTERESTS)//
						.mapToTable(MixItContract.Interests.INTEREST_ID, MixItDatabase.Tables.INTERESTS)//
						.map(MixItContract.Interests.SESSIONS_COUNT, Subquery.TAG_SESSIONS_COUNT)//
						.where(MixItDatabase.Tables.SESSIONS_INTERESTS + "." + SessionsInterests.SESSION_ID + "=?", sessionsId);
			}
			// case SESSIONS_ID_INTERESTS_ID: {
			// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
			// final String interestId = MixItContract.Sessions.getInterestId(uri);
			// return builder.table(MixItDatabase.Tables.SESSIONS_INTERESTS_JOIN_INTERESTS)
			// .mapToTable(MixItContract.Interests._ID, MixItDatabase.Tables.INTERESTS)
			// .mapToTable(MixItContract.Interests.INTEREST_ID, MixItDatabase.Tables.INTERESTS)
			// .where(MixItDatabase.Tables.SESSIONS_INTERESTS + "." + SessionsInterests.SESSION_ID + "=?", sessionsId)
			// .where(MixItDatabase.Tables.SESSIONS_INTERESTS + "." + SessionsInterests.INTEREST_ID + "=?", interestId);
			// }
			case SESSIONS_ID_COMMENTS: {
				final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS_COMMENTS_JOIN_COMMENTS)//
						.mapToTable(MixItContract.Comments._ID, MixItDatabase.Tables.COMMENTS)//
						.mapToTable(MixItContract.Comments.COMMENT_ID, MixItDatabase.Tables.COMMENTS)//
						.where(MixItDatabase.Tables.SESSIONS_COMMENTS + "." + SessionsComments.SESSION_ID + "=?", sessionsId);
			}
			// case SESSIONS_ID_COMMENTS_ID: {
			// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
			// final String commentId = MixItContract.Sessions.getCommentId(uri);
			// return builder.table(MixItDatabase.Tables.SESSIONS_COMMENTS_JOIN_COMMENTS)
			// .mapToTable(MixItContract.Comments._ID, MixItDatabase.Tables.COMMENTS)
			// .mapToTable(MixItContract.Comments.COMMENT_ID, MixItDatabase.Tables.COMMENTS)
			// .where(MixItDatabase.Tables.SESSIONS_COMMENTS + "." + SessionsComments.SESSION_ID + "=?", sessionsId)
			// .where(MixItDatabase.Tables.SESSIONS_COMMENTS + "." + SessionsComments.COMMENT_ID + "=?", commentId);
			// }
			case LIGHTNINGS:
				return builder//
						.table(MixItDatabase.Tables.SESSIONS)//
						.where(MixItContract.Sessions.FORMAT + "=?", MixItContract.Sessions.FORMAT_LIGHTNING_TALK);
				// case LIGHTNINGS_ID: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(MixItContract.Sessions.SESSION_ID + "=?", sessionsId);
				// }
				// case LIGHTNINGS_ID_SPEAKERS: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS_SPEAKERS_JOIN_MEMBERS)
				// .mapToTable(MixItContract.Sessions._ID, MixItDatabase.Tables.SESSIONS)
				// .mapToTable(MixItContract.Sessions.SESSION_ID, MixItDatabase.Tables.SESSIONS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + SessionsSpeakers.SESSION_ID + "=?", sessionsId);
				// }
				// case LIGHTNINGS_ID_SPEAKERS_ID: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// final String speakerId = MixItContract.Sessions.getSpeakerId(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS_SPEAKERS_JOIN_MEMBERS)
				// .mapToTable(MixItContract.Sessions._ID, MixItDatabase.Tables.SESSIONS)
				// .mapToTable(MixItContract.Sessions.SESSION_ID, MixItDatabase.Tables.SESSIONS)
				// .mapToTable(MixItContract.Members._ID, MixItDatabase.Tables.MEMBERS)
				// .mapToTable(MixItContract.Members.MEMBER_ID, MixItDatabase.Tables.MEMBERS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + SessionsSpeakers.SESSION_ID + "=?", sessionsId)
				// .where(MixItDatabase.Tables.SESSIONS_SPEAKERS + "." + SessionsSpeakers.SPEAKER_ID + "=?", speakerId);
				// }
				// case LIGHTNINGS_ID_INTERESTS: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS_INTERESTS_JOIN_INTERESTS)
				// .mapToTable(MixItContract.Interests._ID, MixItDatabase.Tables.INTERESTS)
				// .mapToTable(MixItContract.Interests.INTEREST_ID, MixItDatabase.Tables.INTERESTS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(MixItDatabase.Tables.SESSIONS_INTERESTS + "." + SessionsInterests.SESSION_ID + "=?", sessionsId);
				// }
				// case LIGHTNINGS_ID_INTERESTS_ID: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// final String interestId = MixItContract.Sessions.getInterestId(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS_INTERESTS_JOIN_INTERESTS)
				// .mapToTable(MixItContract.Interests._ID, MixItDatabase.Tables.INTERESTS)
				// .mapToTable(MixItContract.Interests.INTEREST_ID, MixItDatabase.Tables.INTERESTS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(MixItDatabase.Tables.SESSIONS_INTERESTS + "." + SessionsInterests.SESSION_ID + "=?", sessionsId)
				// .where(MixItDatabase.Tables.SESSIONS_INTERESTS + "." + SessionsInterests.INTEREST_ID + "=?", interestId);
				// }
				// case LIGHTNINGS_ID_COMMENTS: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS_COMMENTS_JOIN_COMMENTS)
				// .mapToTable(MixItContract.Comments._ID, MixItDatabase.Tables.COMMENTS)
				// .mapToTable(MixItContract.Comments.COMMENT_ID, MixItDatabase.Tables.COMMENTS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(MixItDatabase.Tables.SESSIONS_COMMENTS + "." + SessionsComments.SESSION_ID + "=?", sessionsId);
				// }
				// case LIGHTNINGS_ID_COMMENTS_ID: {
				// final String sessionsId = MixItContract.Sessions.getSessionId(uri);
				// final String commentId = MixItContract.Sessions.getCommentId(uri);
				// return builder.table(MixItDatabase.Tables.SESSIONS_COMMENTS_JOIN_COMMENTS)
				// .mapToTable(MixItContract.Comments._ID, MixItDatabase.Tables.COMMENTS)
				// .mapToTable(MixItContract.Comments.COMMENT_ID, MixItDatabase.Tables.COMMENTS)
				// .where(MixItContract.Sessions.IS_SESSION + "=?", "0")
				// .where(MixItDatabase.Tables.SESSIONS_COMMENTS + "." + SessionsComments.SESSION_ID + "=?", sessionsId)
				// .where(MixItDatabase.Tables.SESSIONS_COMMENTS + "." + SessionsComments.COMMENT_ID + "=?", commentId);
				// }
			case COMMENTS:
				return builder//
						.table(MixItDatabase.Tables.COMMENTS);
			case COMMENTS_ID: {
				final String commentId = MixItContract.Comments.getCommentId(uri);
				return builder//
						.table(MixItDatabase.Tables.COMMENTS)//
						.where(MixItContract.Comments.COMMENT_ID + "=?", commentId);
			}
			case ROOM_ID_SESSIONS: {
				final String roomId = MixItContract.Sessions.getRoomId(uri);
				return builder//
						.table(MixItDatabase.Tables.SESSIONS)//
						.where(MixItContract.Sessions.ROOM_ID + "=?", roomId);
			}
			case ROOMS: {
				return builder //
						.table(MixItDatabase.Tables.SESSIONS)//
						.groupBy(MixItContract.Sessions.ROOM_ID);
			}
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	private interface Subquery {
		String TAG_SESSIONS_COUNT = "(SELECT COUNT(" + MixItDatabase.Tables.SESSIONS_INTERESTS + "." + MixItDatabase.SessionsInterests.INTEREST_ID + ")" + //
				" FROM " + MixItDatabase.Tables.SESSIONS_INTERESTS + //
				" WHERE " + MixItDatabase.Tables.SESSIONS_INTERESTS + "." + MixItDatabase.SessionsInterests.INTEREST_ID + "=" + //
				MixItDatabase.Tables.INTERESTS + "." + MixItContract.Interests.INTEREST_ID + ")";
		String TAG_MEMBERS_COUNT = "(SELECT COUNT(" + MixItDatabase.Tables.MEMBERS_INTERESTS + "." + MixItDatabase.MembersInterests.INTEREST_ID + ")" + //
				" FROM " + MixItDatabase.Tables.MEMBERS_INTERESTS + //
				" WHERE " + MixItDatabase.Tables.MEMBERS_INTERESTS + "." + MixItDatabase.MembersInterests.INTEREST_ID + "=" + //
				MixItDatabase.Tables.INTERESTS + "." + MixItContract.Interests.INTEREST_ID + ")";
	}

}
