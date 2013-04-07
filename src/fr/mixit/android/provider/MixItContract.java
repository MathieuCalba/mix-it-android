package fr.mixit.android.provider;

import android.net.Uri;
import android.provider.BaseColumns;


public class MixItContract {

	public static final String CONTENT_AUTHORITY = "fr.mixit.android";

	protected static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	public static final String PATH_INTERESTS = "interests";
	public static final String PATH_BADGES = "badges";
	public static final String PATH_MEMBERS = "members";
	public static final String PATH_SPEAKERS = "speakers";
	public static final String PATH_STAFF = "staff";
	public static final String PATH_SHARED_LINKS = "shared_links";
	public static final String PATH_LINKERS = "linkers";
	public static final String PATH_LINKS = "links";
	public static final String PATH_TALKS = "talks";
	public static final String PATH_SESSIONS = "sessions";
	public static final String PATH_LIGHTNINGS = "lightnings";
	public static final String PATH_ROOM = "room";
	public static final String PATH_COMMENTS = "comments";

	public static final String CONTENT_TYPE_START = "vnd.android.cursor.";
	public static final String CONTENT_TYPE_DIR = "dir";
	public static final String CONTENT_TYPE_ITEM = "item";
	public static final String VENDOR = "/vnd.mixit.";

	interface InterestsColumns {
		String INTEREST_ID = "interest_id";
		String NAME = "name";
	}

	public static class Interests implements InterestsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_INTERESTS).build();

		public static final String CONTENT_TYPE = CONTENT_TYPE_START + CONTENT_TYPE_DIR + VENDOR + PATH_INTERESTS;
		public static final String CONTENT_ITEM_TYPE = CONTENT_TYPE_START + CONTENT_TYPE_ITEM + VENDOR + PATH_INTERESTS;

		public static final String SESSIONS_COUNT = "sessions_count";
		public static final String MEMBERS_COUNT = "members_count";

		public static final String DEFAULT_SORT = MixItDatabase.Tables.INTERESTS + "." + InterestsColumns.NAME + " ASC";

		public static interface PROJ {
			String[] PROJECTION = { //
					Interests._ID, //
					Interests.INTEREST_ID, //
					Interests.NAME //
			};

			int _ID = 0;
			int INTEREST_ID = 1;
			int NAME = 2;
		}

		public static interface PROJ_WITH_COUNT {
			String[] PROJECTION = { //
					Interests._ID, //
					Interests.INTEREST_ID, //
					Interests.NAME, //
					Interests.MEMBERS_COUNT //
			};

			int _ID = 0;
			int INTEREST_ID = 1;
			int NAME = 2;
			int MEMBER_COUNT = 3;
		}

		public static Uri buildInterestUri(String interestId) {
			return CONTENT_URI.buildUpon().appendPath(interestId).build();
		}

		public static Uri buildSessionsDir(String interestId) {
			return CONTENT_URI.buildUpon().appendPath(interestId).appendPath(MixItProvider.ALL + PATH_SESSIONS).build();
		}

		public static Uri buildMembersDir(String interestId) {
			return CONTENT_URI.buildUpon().appendPath(interestId).appendPath(PATH_MEMBERS).build();
		}

		public static String getInterestId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	// SessionIDs[] (where the member is a speaker), AccountIDs[] (at least Mix-IT, and maybe Twitter, Google and Linked),
	interface MembersColumns {
		String MEMBER_ID = "member_id";
		String LOGIN = "login";
		String EMAIL = "email";
		String FIRSTNAME = "firstname";
		String LASTNAME = "lastname";
		String COMPANY = "company";
		String SHORT_DESC = "short_desc";
		String LONG_DESC = "long_desc";
		String TICKET_REGISTERED = "registered";
		String IMAGE_URL = "image_url";
		String NB_CONSULT = "nb_consult";
		String TYPE = "type";
		String LEVEL = "level";
	}

	public static class Members implements MembersColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MEMBERS).build();
		public static final Uri CONTENT_URI_SPEAKERS = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SPEAKERS).build();
		public static final Uri CONTENT_URI_STAFF = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STAFF).build();

		public static final String CONTENT_TYPE = CONTENT_TYPE_START + CONTENT_TYPE_DIR + VENDOR + PATH_MEMBERS;
		public static final String CONTENT_ITEM_TYPE = CONTENT_TYPE_START + CONTENT_TYPE_ITEM + VENDOR + PATH_MEMBERS;

		public static final String DEFAULT_SORT = "UPPER(" + MixItDatabase.Tables.MEMBERS + "." + MembersColumns.LASTNAME + ") ASC, " + //
				MixItDatabase.Tables.MEMBERS + "." + MembersColumns.FIRSTNAME + " ASC";

		public static final int TYPE_MEMBER = 0;
		public static final int TYPE_STAFF = 1;
		public static final int TYPE_SPEAKER = 2;
		public static final int TYPE_SPONSOR = 3;

		public static interface PROJ_LIST {
			String[] PROJECTION = { //
					Members._ID, //
					Members.MEMBER_ID, //
					Members.FIRSTNAME, //
					Members.LASTNAME, //
					Members.IMAGE_URL, //
					Members.TYPE, //
					Members.COMPANY //
			};

			int _ID = 0;
			int MEMBER_ID = 1;
			int FIRSTNAME = 2;
			int LASTNAME = 3;
			int IMAGE_URL = 4;
			int TYPE = 5;
			int COMPANY = 6;
		}

		public static interface PROJ_DETAIL {
			String[] PROJECTION = { //
					Members._ID, //
					Members.MEMBER_ID, //
					Members.FIRSTNAME, //
					Members.LASTNAME, //
					Members.IMAGE_URL, //
					Members.TYPE, //
					Members.COMPANY, //
					Members.SHORT_DESC, //
					Members.LONG_DESC, //
					Members.TICKET_REGISTERED, //
					Members.NB_CONSULT, //
					Members.LOGIN, //
					Members.EMAIL, //
					Members.LEVEL //
			};

			int _ID = 0;
			int MEMBER_ID = 1;
			int FIRSTNAME = 2;
			int LASTNAME = 3;
			int IMAGE_URL = 4;
			int TYPE = 5;
			int COMPANY = 6;
			int SHORT_DESC = 7;
			int LONG_DESC = 8;
			int TICKET_REGISTERED = 9;
			int NB_CONSULT = 10;
			int LOGIN = 11;
			int EMAIL = 12;
			int LEVEL = 13;
		}

		public static interface PROJ_LINKS {
			String[] PROJECTION = { MixItDatabase.MembersLinks.LINK_ID, MixItDatabase.MembersLinks.MEMBER_ID };

			int LINK_ID = 0;
			int LINKER_ID = 1;
		}

		public static Uri buildMemberUri(String memberId) {
			return CONTENT_URI.buildUpon().appendPath(memberId).build();
		}

		public static Uri buildBadgesDirUri(String memberId) {
			return CONTENT_URI.buildUpon().appendPath(memberId).appendPath(PATH_BADGES).build();
		}

		public static Uri buildMemberBadgeUri(String memberId, String badgeId) {
			return CONTENT_URI.buildUpon().appendPath(memberId).appendPath(PATH_BADGES).appendPath(badgeId).build();
		}

		public static Uri buildInterestsDirUri(String memberId) {
			return CONTENT_URI.buildUpon().appendPath(memberId).appendPath(PATH_INTERESTS).build();
		}

		public static Uri buildMemberInterestUri(String memberId, String interestId) {
			return CONTENT_URI.buildUpon().appendPath(memberId).appendPath(PATH_INTERESTS).appendPath(interestId).build();
		}

		public static Uri buildSharedLinksDirUri(String memberId) {
			return CONTENT_URI.buildUpon().appendPath(memberId).appendPath(PATH_SHARED_LINKS).build();
		}

		public static Uri buildMemberSharedLinkUri(String memberId, String sharedLinkId) {
			return CONTENT_URI.buildUpon().appendPath(memberId).appendPath(PATH_SHARED_LINKS).appendPath(sharedLinkId).build();
		}

		public static Uri buildLinkersDirUri(String memberId) {
			return CONTENT_URI.buildUpon().appendPath(memberId).appendPath(PATH_LINKERS).build();
		}

		public static Uri buildMemberLinkerUri(String memberId, String linkerId) {
			return CONTENT_URI.buildUpon().appendPath(memberId).appendPath(PATH_LINKERS).appendPath(linkerId).build();
		}

		public static Uri buildLinksDirUri(String memberId) {
			return CONTENT_URI.buildUpon().appendPath(memberId).appendPath(PATH_LINKS).build();
		}

		public static Uri buildMemberLinkUri(String memberId, String linkId) {
			return CONTENT_URI.buildUpon().appendPath(memberId).appendPath(PATH_LINKS).appendPath(linkId).build();
		}

		public static Uri buildCommentsDirUri(String memberId) {
			return CONTENT_URI.buildUpon().appendPath(memberId).appendPath(PATH_COMMENTS).build();
		}

		public static Uri buildMemberCommentUri(String memberId, String commentId) {
			return CONTENT_URI.buildUpon().appendPath(memberId).appendPath(PATH_COMMENTS).appendPath(commentId).build();
		}

		public static Uri buildSessionsDirUri(String speakerId, Boolean isSession) {
			String pathToAppend = null;
			if (isSession == null) {
				pathToAppend = PATH_TALKS;
			} else if (isSession) {
				pathToAppend = PATH_SESSIONS;
			} else {
				pathToAppend = PATH_LIGHTNINGS;
			}
			return CONTENT_URI.buildUpon().appendPath(speakerId).appendPath(pathToAppend).build();
		}

		public static Uri buildSpeakerSessionUri(String speakerId, String sessionId) {
			return CONTENT_URI.buildUpon().appendPath(speakerId).appendPath(PATH_SESSIONS).appendPath(sessionId).build();
		}

		public static String getMemberId(Uri uri) {
			return uri.getPathSegments().get(1);
		}

		public static String getInterestId(Uri uri) {
			return uri.getPathSegments().get(3);
		}

		public static String getBadgeId(Uri uri) {
			return uri.getPathSegments().get(3);
		}

		public static String getSharedLinkId(Uri uri) {
			return uri.getPathSegments().get(3);
		}

		public static String getLinkId(Uri uri) {
			return uri.getPathSegments().get(3);
		}

		public static String getLinkerId(Uri uri) {
			return uri.getPathSegments().get(3);
		}

		public static String getCommentId(Uri uri) {
			return uri.getPathSegments().get(3);
		}

		public static String getSessionId(Uri uri) {
			return uri.getPathSegments().get(3);
		}
	}

	interface SharedLinksColumns {
		String SHARED_LINK_ID = "shared_link_id";
		String MEMBER_ID = "member_id";
		String ORDER_NUM = "order_num";
		String NAME = "name";
		String URL = "url";
	}

	public static class SharedLinks implements SharedLinksColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHARED_LINKS).build();

		public static final String CONTENT_TYPE = CONTENT_TYPE_START + CONTENT_TYPE_DIR + VENDOR + PATH_SHARED_LINKS;
		public static final String CONTENT_ITEM_TYPE = CONTENT_TYPE_START + CONTENT_TYPE_ITEM + VENDOR + PATH_SHARED_LINKS;

		public static final String DEFAULT_SORT = MixItDatabase.Tables.SHARED_LINKS + "." + SharedLinksColumns.ORDER_NUM + " ASC";

		public static interface PROJ {
			public String[] PROJECTION = { //
					SharedLinks._ID, //
					SharedLinks.SHARED_LINK_ID, //
					SharedLinks.MEMBER_ID, //
					SharedLinks.ORDER_NUM, //
					SharedLinks.NAME, //
					SharedLinks.URL //
			};

			int _ID = 0;
			int SHARED_LINK_ID = 1;
			int MEMBER_ID = 2;
			int ORDER_NUM = 3;
			int NAME = 4;
			int URL = 5;
		}

		public static Uri buildSharedLinkUri(String sharedLinkId) {
			return CONTENT_URI.buildUpon().appendPath(sharedLinkId).build();
		}

		public static String getSharedLinkId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	interface SessionsColumns {
		String SESSION_ID = "session_id";
		String TITLE = "title";
		String SUMMARY = "summary";
		String DESC = "desc";
		String TIME = "time";
		String ROOM_ID = "room_id";
		String NB_VOTES = "nb_vote";
		String MY_VOTE = "my_vote";
		String IS_FAVORITE = "is_favorite";
		String FORMAT = "format";
		String LEVEL = "level";
		String LANG = "lang";
	}

	public static class Sessions implements SessionsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SESSIONS).build();
		public static final Uri CONTENT_URI_LIGNTHNING = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LIGHTNINGS).build();

		public static final String CONTENT_TYPE = CONTENT_TYPE_START + CONTENT_TYPE_DIR + VENDOR + PATH_SESSIONS;
		public static final String CONTENT_ITEM_TYPE = CONTENT_TYPE_START + CONTENT_TYPE_ITEM + VENDOR + PATH_SESSIONS;

		public static final String DEFAULT_SORT = MixItDatabase.Tables.SESSIONS + "." + SessionsColumns.SESSION_ID + " ASC";

		public static final String FORMAT_TALK = "Talk";
		public static final String FORMAT_LIGHTNING_TALK = "Lightning Talk";
		public static final String FORMAT_WORKSHOP = "Workshop";

		public static interface PROJ_LIST {
			public String[] PROJECTION = { //
					Sessions._ID, //
					Sessions.SESSION_ID, //
					Sessions.TITLE, //
					Sessions.TIME, //
					Sessions.ROOM_ID, //
					Sessions.IS_FAVORITE, //
					Sessions.FORMAT, //
					Sessions.LEVEL, //
					Sessions.LANG //
			};

			int _ID = 0;
			int SESSION_ID = 1;
			int TITLE = 2;
			int TIME = 3;
			int ROOM_ID = 4;
			int IS_FAVORITE = 5;
			int FORMAT = 6;
			int LEVEL = 7;
			int LANG = 8;
		}

		public static interface PROJ_DETAIL {
			public String[] PROJECTION = { //
					Sessions._ID, //
					Sessions.SESSION_ID, //
					Sessions.TITLE, //
					Sessions.TIME, //
					Sessions.ROOM_ID, //
					Sessions.IS_FAVORITE, //
					Sessions.FORMAT, //
					Sessions.LEVEL, //
					Sessions.LANG, //
					Sessions.SUMMARY, //
					Sessions.DESC, //
					Sessions.NB_VOTES, //
					Sessions.MY_VOTE //
			};

			int _ID = 0;
			int SESSION_ID = 1;
			int TITLE = 2;
			int TIME = 3;
			int ROOM_ID = 4;
			int IS_FAVORITE = 5;
			int FORMAT = 6;
			int LEVEL = 7;
			int LANG = 8;
			int SUMMARY = 9;
			int DESC = 10;
			int NB_VOTES = 11;
			int MY_VOTE = 12;
		}

		public static Uri buildSessionUri(String sessionId) {
			return CONTENT_URI.buildUpon().appendPath(sessionId).build();
		}

		public static Uri buildSpeakersDirUri(String sessionId) {
			return CONTENT_URI.buildUpon().appendPath(sessionId).appendPath(PATH_MEMBERS).build();
		}

		public static Uri buildSessionSpeakerUri(String sessionId, String speakerId) {
			return CONTENT_URI.buildUpon().appendPath(sessionId).appendPath(PATH_MEMBERS).appendPath(speakerId).build();
		}

		public static Uri buildInterestsDirUri(String sessionId) {
			return CONTENT_URI.buildUpon().appendPath(sessionId).appendPath(PATH_INTERESTS).build();
		}

		public static Uri buildSessionInterestUri(String sessionId, String interestId) {
			return CONTENT_URI.buildUpon().appendPath(sessionId).appendPath(PATH_INTERESTS).appendPath(interestId).build();
		}

		public static Uri buildCommentsDirUri(String sessionId) {
			return CONTENT_URI.buildUpon().appendPath(sessionId).appendPath(PATH_COMMENTS).build();
		}

		public static Uri buildSessionCommentUri(String sessionId, String commentId) {
			return CONTENT_URI.buildUpon().appendPath(sessionId).appendPath(PATH_COMMENTS).appendPath(commentId).build();
		}

		public static Uri buildSessionsUri(String idRoom) {
			return BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROOM).appendPath(idRoom).appendPath(PATH_SESSIONS).build();
		}

		public static Uri buildSessionsUri(String interestId, boolean isSession) {
			return BASE_CONTENT_URI.buildUpon().appendPath(PATH_INTERESTS).appendPath(interestId).appendPath(isSession ? PATH_SESSIONS : PATH_LIGHTNINGS)
					.build();
		}

		public static String getSessionId(Uri uri) {
			return uri.getPathSegments().get(1);
		}

		public static String getSpeakerId(Uri uri) {
			return uri.getPathSegments().get(3);
		}

		public static String getInterestIdFromSessionInterests(Uri uri) {
			return uri.getPathSegments().get(3);
		}

		public static String getCommentId(Uri uri) {
			return uri.getPathSegments().get(3);
		}

		public static String getRoomId(Uri uri) {
			return uri.getPathSegments().get(1);
		}

		public static String getInterestIdFromInterestSessions(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	interface CommentsColumns {
		String COMMENT_ID = "comment_id";
		String AUTHOR_ID = "author_id";
		String CONTENT = "content";
		String PUBLISH_DATE = "publish_date";
		String SESSION_ID = "session_id";
	}

	public static class Comments implements CommentsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COMMENTS).build();

		public static final String CONTENT_TYPE = CONTENT_TYPE_START + CONTENT_TYPE_DIR + VENDOR + PATH_COMMENTS;
		public static final String CONTENT_ITEM_TYPE = CONTENT_TYPE_START + CONTENT_TYPE_ITEM + VENDOR + PATH_COMMENTS;

		// TODO : maybe sort comments in the other order (old to recent ?)
		public static final String DEFAULT_SORT = MixItDatabase.Tables.COMMENTS + "." + CommentsColumns.PUBLISH_DATE + " DESC";

		public static interface PROJ {
			public String[] PROJ = { //
					Comments._ID, //
					Comments.COMMENT_ID, //
					Comments.AUTHOR_ID, //
					Comments.CONTENT, //
					Comments.PUBLISH_DATE, //
					Comments.SESSION_ID //
			};

			int _ID = 0;
			int COMMENT_ID = 1;
			int AUTHOR_ID = 2;
			int CONTENT = 3;
			int PUBLISH_DATE = 4;
			int SESSION_ID = 5;
		}

		public static Uri buildCommentUri(String commentId) {
			return CONTENT_URI.buildUpon().appendPath(commentId).build();
		}

		public static String getCommentId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	private MixItContract() {
	}

}
