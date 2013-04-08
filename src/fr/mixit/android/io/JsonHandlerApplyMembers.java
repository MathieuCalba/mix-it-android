package fr.mixit.android.io;

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


public class JsonHandlerApplyMembers extends JsonHandlerApply {

	private static final String TAG = JsonHandlerApplyMembers.class.getSimpleName();

	protected static final String TAG_ID = "id";
	protected static final String TAG_FIRSTNAME = "firstname";
	protected static final String TAG_LASTNAME = "lastname";
	protected static final String TAG_LOGIN = "login";
	// protected static final String TAG_EMAIL = "email";
	protected static final String TAG_COMPANY = "company";
	protected static final String TAG_SHORT_DESC = "shortdesc";
	protected static final String TAG_LONG_DESC = "longdesc";
	protected static final String TAG_IMAGE_URL = "urlimage";
	protected static final String TAG_NB_CONSULTS = "nbConsults";
	protected static final String TAG_LOGO = "logo";
	protected static final String TAG_LEVEL = "level";
	// protected static final String TAG_TICKETING_REGISTERED = "ticketingRegistered";

	protected static final String TAG_LINKS = "links";
	protected static final String TAG_LINKERS = "linkers";
	protected static final String TAG_INTERESTS = "interests";
	// protected static final String TAG_BADGES = "badges";

	protected static final String TAG_SHARED_LINKS = "sharedLinks";
	protected static final String TAG_ORDER_NUM = "ordernum";
	protected static final String TAG_NAME = "name";
	protected static final String TAG_URL = "url";

	// protected static final String TAG_ACCOUNTS = "accounts";
	// protected static final String TAG_GOOGLE = "Google";
	// protected static final String TAG_GOOGLE_ID = "googleId";
	// protected static final String TAG_TWITTER = "Twitter";
	// protected static final String TAG_SCREEN_NAME = "screenName";
	// protected static final String TAG_LINKEDIN = "LinkedIn";
	// protected static final String TAG_PROVIDER = "provider";
	// protected static final String TAG_LAST_STATUS_ID = "lastStatusId";// ???
	// protected static final String TAG_LAST_FETCHED = "lastFetched";// ???

	private static final String EMPTY = "";

	protected int mMemberType = MixItContract.Members.TYPE_MEMBER;
	protected boolean mIsFullParsing = false;
	protected boolean mIsParsingList = false;

	protected HashSet<String> mItemIds = null;
	protected HashMap<String, HashSet<String>> mItemLinksIds;
	protected HashMap<String, HashSet<String>> mItemLinkersIds;
	protected HashMap<String, HashSet<String>> mItemSharedLinksIds;
	protected HashMap<String, HashSet<String>> mItemInterestsIds;

	public JsonHandlerApplyMembers(boolean isFullParsing) {
		this(isFullParsing, MixItContract.Members.TYPE_MEMBER);
	}

	public JsonHandlerApplyMembers(boolean isFullParsing, int memberType) {
		super(MixItContract.CONTENT_AUTHORITY);

		mIsFullParsing = isFullParsing;
		mMemberType = memberType;

		mItemIds = new HashSet<String>();
		mItemLinksIds = Maps.newHashMap();
		mItemLinkersIds = Maps.newHashMap();
		mItemSharedLinksIds = Maps.newHashMap();
		mItemInterestsIds = Maps.newHashMap();
	}

	@Override
	public boolean parseList(JSONArray entries, ContentResolver resolver) throws JSONException {
		if (DEBUG_MODE) {
			Log.d(TAG, "Retrieved " + entries.length() + " more members entries.");
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

		final Uri itemUri = MixItContract.Members.buildMemberUri(id);

		boolean tagUpdated = false;
		boolean newItem = false;
		boolean build = false;
		ContentProviderOperation.Builder builder;

		if (ProviderParsingUtils.isRowExisting(itemUri, MixItContract.Members.PROJ_DETAIL.PROJECTION, resolver)) {
			builder = ContentProviderOperation.newUpdate(itemUri);
			tagUpdated = isItemUpdated(itemUri, item, resolver, mMemberType);
		} else {
			newItem = true;
			builder = ContentProviderOperation.newInsert(MixItContract.Members.CONTENT_URI);
			builder.withValue(MixItContract.Members.MEMBER_ID, id);
			builder.withValue(MixItContract.Members.TYPE, mMemberType);
			build = true;
		}

		if (newItem || tagUpdated) {
			if (item.has(TAG_FIRSTNAME)) {
				String firstName = item.getString(TAG_FIRSTNAME);
				if (!TextUtils.isEmpty(firstName)) {
					firstName = firstName.toLowerCase(Locale.getDefault());
					firstName = firstName.substring(0, 1).toUpperCase(Locale.getDefault()) + firstName.substring(1);
				}
				builder.withValue(MixItContract.Members.FIRSTNAME, firstName);
			}
			if (item.has(TAG_LASTNAME)) {
				String lastName = item.getString(TAG_LASTNAME);
				if (!TextUtils.isEmpty(lastName)) {
					lastName = lastName.toLowerCase(Locale.getDefault());
					lastName = lastName.substring(0, 1).toUpperCase(Locale.getDefault()) + lastName.substring(1);
				}
				builder.withValue(MixItContract.Members.LASTNAME, lastName);
			}
			if (item.has(TAG_LOGIN)) {
				builder.withValue(MixItContract.Members.LOGIN, item.getString(TAG_LOGIN));
			}
			if (item.has(TAG_COMPANY)) {
				builder.withValue(MixItContract.Members.COMPANY, item.getString(TAG_COMPANY));
			}
			if (item.has(TAG_SHORT_DESC)) {
				builder.withValue(MixItContract.Members.SHORT_DESC, item.getString(TAG_SHORT_DESC));
			}
			if (item.has(TAG_LONG_DESC)) {
				builder.withValue(MixItContract.Members.LONG_DESC, item.getString(TAG_LONG_DESC));
			}
			if (item.has(TAG_LOGO)) {
				builder.withValue(MixItContract.Members.IMAGE_URL, item.getString(TAG_LOGO));
			} else if (item.has(TAG_IMAGE_URL) && mMemberType != MixItContract.Members.TYPE_SPONSOR) {
				builder.withValue(MixItContract.Members.IMAGE_URL, item.getString(TAG_IMAGE_URL));
			}
			if (item.has(TAG_NB_CONSULTS)) {
				builder.withValue(MixItContract.Members.NB_CONSULT, item.getString(TAG_NB_CONSULTS));
			}
			if (item.has(TAG_LEVEL)) {
				builder.withValue(MixItContract.Members.LEVEL, item.getString(TAG_LEVEL));
			}

			build = true;
		}
		if (build) {
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, builder.build());
		}

		if (mIsFullParsing) {
			if (item.has(TAG_LINKS)) {
				final JSONArray interests = item.getJSONArray(TAG_LINKS);
				parseLinks(id, interests, resolver);
			}
			if (item.has(TAG_LINKERS)) {
				final JSONArray interests = item.getJSONArray(TAG_LINKERS);
				parseLinkers(id, interests, resolver);
			}
			if (item.has(TAG_INTERESTS)) {
				final JSONArray interests = item.getJSONArray(TAG_INTERESTS);
				parseLinkedInterests(id, interests, resolver);
			}
			if (item.has(TAG_SHARED_LINKS)) {
				final JSONArray interests = item.getJSONArray(TAG_SHARED_LINKS);
				parseSharedLinks(id, interests, resolver);
			}
		}

		if (!mIsParsingList) {
			deleteItemsDataNotFound(resolver);

			return ProviderParsingUtils.applyBatch(mAuthority, resolver, mBatch, true);
		}

		return true;
	}

	private static boolean isItemUpdated(Uri uri, JSONObject item, ContentResolver resolver, int newMemberType) throws JSONException {
		final Cursor cursor = resolver.query(uri, MixItContract.Members.PROJ_DETAIL.PROJECTION, null, null, null);
		try {
			if (!cursor.moveToFirst()) {
				return false;
			}

			String curFirstName = cursor.getString(MixItContract.Members.PROJ_DETAIL.FIRSTNAME);
			if (TextUtils.isEmpty(curFirstName)) {
				curFirstName = EMPTY;
			}
			String curLastName = cursor.getString(MixItContract.Members.PROJ_DETAIL.LASTNAME);
			if (TextUtils.isEmpty(curLastName)) {
				curLastName = EMPTY;
			}

			String curLogin = cursor.getString(MixItContract.Members.PROJ_DETAIL.LOGIN);
			if (TextUtils.isEmpty(curLogin)) {
				curLogin = EMPTY;
			}

			String curCompany = cursor.getString(MixItContract.Members.PROJ_DETAIL.COMPANY);
			if (TextUtils.isEmpty(curCompany)) {
				curCompany = EMPTY;
			}

			String curShortDesc = cursor.getString(MixItContract.Members.PROJ_DETAIL.SHORT_DESC);
			if (TextUtils.isEmpty(curShortDesc)) {
				curShortDesc = EMPTY;
			}

			String curLongDesc = cursor.getString(MixItContract.Members.PROJ_DETAIL.LONG_DESC);
			if (TextUtils.isEmpty(curLongDesc)) {
				curLongDesc = EMPTY;
			}

			String curImageUrl = cursor.getString(MixItContract.Members.PROJ_DETAIL.IMAGE_URL);
			if (TextUtils.isEmpty(curImageUrl)) {
				curImageUrl = EMPTY;
			}

			final int curNbConsults = cursor.getInt(MixItContract.Members.PROJ_DETAIL.NB_CONSULT);

			String curLevel = cursor.getString(MixItContract.Members.PROJ_DETAIL.LEVEL);
			if (TextUtils.isEmpty(curLevel)) {
				curLevel = EMPTY;
			}

			// final int curMemberType = cursor.getInt(MixItContract.Members.PROJ_DETAIL.TYPE);

			final String newFirstName = item.has(TAG_FIRSTNAME) ? item.getString(TAG_FIRSTNAME).trim() : curFirstName;
			final String newLastName = item.has(TAG_LASTNAME) ? item.getString(TAG_LASTNAME).trim() : curLastName;
			final String newLogin = item.has(TAG_LOGIN) ? item.getString(TAG_LOGIN).trim() : curLogin;
			final String newCompany = item.has(TAG_COMPANY) ? item.getString(TAG_COMPANY).trim() : curCompany;
			final String newShortDesc = item.has(TAG_SHORT_DESC) ? item.getString(TAG_SHORT_DESC).trim() : curShortDesc;
			final String newLongDesc = item.has(TAG_LONG_DESC) ? item.getString(TAG_LONG_DESC).trim() : curLongDesc;
			final String newImageUrl = item.has(TAG_LOGO) ? item.getString(TAG_LOGO).trim() : item.has(TAG_IMAGE_URL) ? item.getString(TAG_IMAGE_URL).trim()
					: curImageUrl;
			final int newNbConsults = item.has(TAG_NB_CONSULTS) ? item.getInt(TAG_NB_CONSULTS) : curNbConsults;
			final String newLevel = item.has(TAG_LEVEL) ? item.getString(TAG_LEVEL).trim() : curLevel;

			return !curFirstName.equalsIgnoreCase(newFirstName) || //
					!curLastName.equalsIgnoreCase(newLastName) || //
					!curLogin.equals(newLogin) || //
					!curCompany.equals(newCompany) || //
					!curShortDesc.equals(newShortDesc) || //
					!curLongDesc.equals(newLongDesc) || //
					!curImageUrl.equals(newImageUrl) || //
					curNbConsults != newNbConsults || //
					!curLevel.equals(newLevel)
					/* || // curMemberType != newMemberType && newMemberType != MixItContract.Members.TYPE_MEMBER */;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private static boolean isSharedLinkUpdated(Uri uri, String itemId, JSONObject sharedLink, ContentResolver resolver) throws JSONException {
		final String[] selectionArgs = { itemId };
		final Cursor cursor = resolver.query(uri, MixItContract.SharedLinks.PROJ.PROJECTION, MixItContract.SharedLinks.MEMBER_ID + " = ?", selectionArgs, null);
		try {
			if (!cursor.moveToFirst()) {
				return false;
			}

			final int curOrderNum = cursor.getInt(MixItContract.SharedLinks.PROJ.ORDER_NUM);
			final String curName = cursor.getString(MixItContract.SharedLinks.PROJ.NAME).toLowerCase(Locale.getDefault()).trim();
			final String curUrl = cursor.getString(MixItContract.SharedLinks.PROJ.URL).toLowerCase(Locale.getDefault()).trim();

			final int newOrderNum = sharedLink.has(TAG_ORDER_NUM) ? sharedLink.getInt(TAG_ORDER_NUM) : curOrderNum;
			final String newName = sharedLink.has(TAG_NAME) ? sharedLink.getString(TAG_NAME).toLowerCase(Locale.getDefault()).trim() : curName;
			final String newUrl = sharedLink.has(TAG_URL) ? sharedLink.getString(TAG_URL).toLowerCase(Locale.getDefault()).trim() : curUrl;

			return !curName.equals(newName) || //
					!curUrl.equals(newUrl) || //
					curOrderNum != newOrderNum;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public void parseLinkedInterests(String itemId, JSONArray interests, ContentResolver resolver) throws JSONException {
		final Uri itemInterestsUri = MixItContract.Members.buildInterestsDirUri(itemId);
		final HashSet<String> interestsIds = Sets.newHashSet();

		for (int j = 0; j < interests.length(); j++) {
			final int id = interests.getInt(j);
			final String interestId = String.valueOf(id);
			interestsIds.add(interestId);

			final ContentProviderOperation ope = ContentProviderOperation.newInsert(itemInterestsUri) //
					.withValue(MixItDatabase.MembersInterests.INTEREST_ID, interestId) //
					.withValue(MixItDatabase.MembersInterests.MEMBER_ID, itemId) //
					.build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
		}

		mItemInterestsIds.put(itemId, interestsIds);
	}

	public void parseLinks(String itemId, JSONArray links, ContentResolver resolver) throws JSONException {
		final Uri itemLinksUri = MixItContract.Members.buildLinksDirUri(itemId);
		final HashSet<String> linksIds = Sets.newHashSet();

		for (int j = 0; j < links.length(); j++) {
			final int id = links.getInt(j);
			final String linkId = String.valueOf(id);
			linksIds.add(linkId);

			final ContentProviderOperation ope = ContentProviderOperation.newInsert(itemLinksUri) //
					.withValue(MixItDatabase.MembersLinks.LINK_ID, linkId) //
					.withValue(MixItDatabase.MembersLinks.MEMBER_ID, itemId) //
					.build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
		}

		mItemLinksIds.put(itemId, linksIds);
	}

	public void parseLinkers(String itemId, JSONArray linkers, ContentResolver resolver) throws JSONException {
		final Uri itemLinkersUri = MixItContract.Members.buildLinkersDirUri(itemId);
		final HashSet<String> linkersIds = Sets.newHashSet();

		for (int j = 0; j < linkers.length(); j++) {
			final int id = linkers.getInt(j);
			final String linkerId = String.valueOf(id);
			linkersIds.add(linkerId);

			final ContentProviderOperation ope = ContentProviderOperation.newInsert(itemLinkersUri) //
					.withValue(MixItDatabase.MembersLinks.LINK_ID, itemId) //
					.withValue(MixItDatabase.MembersLinks.MEMBER_ID, linkerId) //
					.build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
		}

		mItemLinkersIds.put(itemId, linkersIds);
	}

	private void parseSharedLinks(String itemId, JSONArray sharedLinks, ContentResolver resolver) throws JSONException {
		final HashSet<String> sharedLinksIds = Sets.newHashSet();

		for (int j = 0; j < sharedLinks.length(); j++) {
			final JSONObject sharedLink = sharedLinks.getJSONObject(j);
			final String id = sharedLink.getString(TAG_ID);
			sharedLinksIds.add(id);

			final Uri sharedLinkUri = MixItContract.SharedLinks.buildSharedLinkUri(id);

			boolean sharedLinkUpdated = false;
			boolean newSharedLink = false;
			boolean build = false;
			ContentProviderOperation.Builder builder;

			if (ProviderParsingUtils.isRowExisting(sharedLinkUri, MixItContract.SharedLinks.PROJ.PROJECTION, resolver)) {
				builder = ContentProviderOperation.newUpdate(sharedLinkUri);
				sharedLinkUpdated = isSharedLinkUpdated(sharedLinkUri, itemId, sharedLink, resolver);
			} else {
				newSharedLink = true;
				builder = ContentProviderOperation.newInsert(MixItContract.SharedLinks.CONTENT_URI);
				builder.withValue(MixItContract.SharedLinks.SHARED_LINK_ID, id);
				build = true;
			}

			if (newSharedLink || sharedLinkUpdated) {
				if (sharedLink.has(TAG_ORDER_NUM)) {
					builder.withValue(MixItContract.SharedLinks.ORDER_NUM, sharedLink.getInt(TAG_ORDER_NUM));
				}
				if (sharedLink.has(TAG_NAME)) {
					builder.withValue(MixItContract.SharedLinks.NAME, sharedLink.getString(TAG_NAME));
				}
				if (sharedLink.has(TAG_URL)) {
					builder.withValue(MixItContract.SharedLinks.URL, sharedLink.getString(TAG_URL));
				}
				builder.withValue(MixItContract.SharedLinks.MEMBER_ID, itemId);
				build = true;
			}
			if (build) {
				ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, builder.build());
			}
		}

		mItemSharedLinksIds.put(itemId, sharedLinksIds);
	}

	protected void deleteItemsDataNotFound(ContentResolver resolver) {
		// delete deleted members from N-N relation with interest
		for (final Map.Entry<String, HashSet<String>> entry : mItemInterestsIds.entrySet()) {
			final String itemId = entry.getKey();
			final HashSet<String> interestIds = entry.getValue();
			final Uri itemInterestIds = MixItContract.Members.buildInterestsDirUri(itemId);
			final HashSet<String> lostInterestIds = ProviderParsingUtils.getLostIds(interestIds, itemInterestIds, MixItContract.Interests.PROJ.PROJECTION,
					MixItContract.Interests.PROJ.INTEREST_ID, resolver);
			for (final String lostInterestId : lostInterestIds) {
				final Uri deleteUri = MixItContract.Members.buildMemberInterestUri(itemId, lostInterestId);
				final ContentProviderOperation ope = ContentProviderOperation.newDelete(deleteUri).build();
				ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
			}
		}

		// and delete deleted members from N-N relation with members (links)
		for (final Map.Entry<String, HashSet<String>> entry : mItemLinksIds.entrySet()) {
			final String itemId = entry.getKey();
			final HashSet<String> linkIds = entry.getValue();
			final Uri itemLinksUri = MixItContract.Members.buildLinksDirUri(itemId);
			final HashSet<String> lostLinkIds = ProviderParsingUtils.getLostIds(linkIds, itemLinksUri, MixItContract.Members.PROJ_LINKS.PROJECTION,
					MixItContract.Members.PROJ_LINKS.LINK_ID, resolver);
			for (final String lostLinkId : lostLinkIds) {
				final Uri deleteUri = MixItContract.Members.buildMemberLinkUri(itemId, lostLinkId);
				final ContentProviderOperation ope = ContentProviderOperation.newDelete(deleteUri).build();
				ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
			}
		}

		// and delete deleted members from N-N relation with members (linkers)
		for (final Map.Entry<String, HashSet<String>> entry : mItemLinkersIds.entrySet()) {
			final String itemId = entry.getKey();
			final HashSet<String> linkerIds = entry.getValue();
			final Uri itemLinkersUri = MixItContract.Members.buildLinkersDirUri(itemId);
			final HashSet<String> lostLinkerIds = ProviderParsingUtils.getLostIds(linkerIds, itemLinkersUri, MixItContract.Members.PROJ_LINKS.PROJECTION,
					MixItContract.Members.PROJ_LINKS.LINKER_ID, resolver);
			for (final String lostLinkerId : lostLinkerIds) {
				final Uri deleteUri = MixItContract.Members.buildMemberLinkerUri(itemId, lostLinkerId);
				final ContentProviderOperation ope = ContentProviderOperation.newDelete(deleteUri).build();
				ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
			}
		}

		// and delete deleted sharedLinks from 1-N relation with members
		for (final Map.Entry<String, HashSet<String>> entry : mItemSharedLinksIds.entrySet()) {
			final String itemId = entry.getKey();
			final HashSet<String> sharedLinkIds = entry.getValue();
			final Uri itemSharedLinksUri = MixItContract.Members.buildSharedLinksDirUri(itemId);
			final HashSet<String> lostSharedLinkIds = ProviderParsingUtils.getLostIds(sharedLinkIds, itemSharedLinksUri,
					MixItContract.SharedLinks.PROJ.PROJECTION, MixItContract.SharedLinks.PROJ.SHARED_LINK_ID, resolver);
			for (final String lostSharedLinkId : lostSharedLinkIds) {
				final Uri deleteUri = MixItContract.Members.buildMemberSharedLinkUri(itemId, lostSharedLinkId);
				final ContentProviderOperation ope = ContentProviderOperation.newDelete(deleteUri).build();
				ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
			}
		}
	}

	protected void deleteItemsNotFound(ContentResolver resolver) {
		final String[] args = { String.valueOf(mMemberType) };
		for (final String lostId : ProviderParsingUtils.getLostIds(mItemIds, MixItContract.Members.CONTENT_URI, MixItContract.Members.PROJ_DETAIL.PROJECTION,
				MixItContract.Members.PROJ_DETAIL.MEMBER_ID, resolver, MixItContract.Members.TYPE + " = ?", args)) {
			// delete members not found from N-N relation with interest
			Uri deleteUri = MixItContract.Members.buildInterestsDirUri(lostId);
			ContentProviderOperation ope = ContentProviderOperation.newDelete(deleteUri).build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);

			// and delete members not found from 1-N relation with sharedLinks
			deleteUri = MixItContract.Members.buildSharedLinksDirUri(lostId);
			ope = ContentProviderOperation.newDelete(deleteUri).build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);

			// and delete members not found from N-N relation with members (links and linkers)
			deleteUri = MixItContract.Members.buildMemberUri(lostId);
			ope = ContentProviderOperation.newDelete(deleteUri).build();
			ProviderParsingUtils.addOpeAndApplyBatch(mAuthority, resolver, mBatch, false, ope);
		}
	}
}
