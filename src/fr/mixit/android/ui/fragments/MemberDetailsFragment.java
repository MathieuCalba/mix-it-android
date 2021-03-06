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

package fr.mixit.android.ui.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.petebevin.markdown.MarkdownProcessor;

import de.keyboardsurfer.android.widget.crouton.Style;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.services.MixItService;
import fr.mixit.android.tasks.SessionAsyncTaskLoader;
import fr.mixit.android.ui.MembersActivity;
import fr.mixit.android.ui.widgets.SharedLinkItemView;
import fr.mixit.android.ui.widgets.TalkItemView;
import fr.mixit.android.utils.IntentUtils;
import fr.mixit.android.utils.PrefUtils;
import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;


public class MemberDetailsFragment extends BoundServiceFragment implements LoaderManager.LoaderCallbacks<Cursor>,
WarningStarSessionDialogFragment.WarningStarSessionDialogContract, SessionAsyncTaskLoader.StarSessionListener {

	public static final String TAG = MemberDetailsFragment.class.getSimpleName();

	protected static final int CURSOR_MEMBER = 1012;
	// protected static final int CURSOR_BADGES = 1013;
	protected static final int CURSOR_LINKS = 1014;
	protected static final int CURSOR_LINKERS = 1015;
	protected static final int CURSOR_SHARED_LINKS = 1016;
	protected static final int CURSOR_TALKS = 1017;
	protected static final int CURSOR_INTERESTS = 1018;
	// protected static final int CURSOR_STARRED_SESSIONS = 1019;

	protected static final String EXTRA_MEMBER_ID = "fr.mixit.android.EXTRA_MEMBER_ID";

	protected static final String STATE_FIRST_LOAD = "fr.mixit.android.STATE_FIRST_LOAD";

	boolean mIsFirstLoad = true;

	protected int mMemberId;
	protected String mMemberName;

	protected ViewAnimator mViewAnimator;
	protected TextView mName;
	protected TextView mCompany;
	protected TextView mShortDesc;
	protected TextView mNbConsult;
	protected ImageView mImage;
	protected Button mLinks;
	protected Button mLinkers;
	protected TextView mBio;
	protected TextView mSharedLinksTitle;
	protected LinearLayout mSharedLinks;
	protected TextView mTalksTitle;
	protected LinearLayout mTalks;
	protected LinearLayout mInterests;

	protected ImageLoader mImageLoader = ImageLoader.getInstance();
	protected DisplayImageOptions mOptions;

	public interface MemberDetailsContract {
		public void setActionBarTitle(String title);
	}

	public static MemberDetailsFragment newInstance(Intent intent) {
		final MemberDetailsFragment f = new MemberDetailsFragment();
		f.setArguments(UIUtils.intentToFragmentArguments(intent));
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof MemberDetailsContract)) {
			throw new IllegalArgumentException(activity.getClass().getName() + " must implement MemberDetailsContract");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mOptions = new DisplayImageOptions.Builder().showImageForEmptyUrl(R.drawable.speaker_thumbnail).showStubImage(R.drawable.speaker_thumbnail)
				.cacheInMemory().cacheOnDisc()
				// .decodingType(DecodingType.MEMORY_SAVING)
				.build();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_member_details, container, false);

		mViewAnimator = (ViewAnimator) root.findViewById(R.id.member_detail_animator);
		mName = (TextView) root.findViewById(R.id.member_name);
		mCompany = (TextView) root.findViewById(R.id.member_company);
		mShortDesc = (TextView) root.findViewById(R.id.member_short_desc);
		mNbConsult = (TextView) root.findViewById(R.id.member_consults);
		mImage = (ImageView) root.findViewById(R.id.member_picture);
		mLinks = (Button) root.findViewById(R.id.member_links);
		mLinks.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (getActivity() != null && !isDetached()) {
					final Intent intent = new Intent(getActivity(), MembersActivity.class);
					intent.putExtra(MembersActivity.EXTRA_DISPLAY_MODE, MembersActivity.DISPLAY_MODE_LINKS);
					intent.putExtra(MembersActivity.EXTRA_MEMBER_NAME, mMemberName);
					intent.putExtra(MembersActivity.EXTRA_MEMBER_ID, mMemberId);
					startActivity(intent);
				}
			}
		});
		mLinkers = (Button) root.findViewById(R.id.member_linkers);
		mLinkers.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (getActivity() != null && !isDetached()) {
					final Intent intent = new Intent(getActivity(), MembersActivity.class);
					intent.putExtra(MembersActivity.EXTRA_DISPLAY_MODE, MembersActivity.DISPLAY_MODE_LINKERS);
					intent.putExtra(MembersActivity.EXTRA_MEMBER_NAME, mMemberName);
					intent.putExtra(MembersActivity.EXTRA_MEMBER_ID, mMemberId);
					startActivity(intent);
				}
			}
		});
		mBio = (TextView) root.findViewById(R.id.member_bio_value);
		mSharedLinksTitle = (TextView) root.findViewById(R.id.member_shared_links_title);
		mSharedLinks = (LinearLayout) root.findViewById(R.id.member_shared_links_value);
		mTalksTitle = (TextView) root.findViewById(R.id.member_talks_title);
		mTalks = (LinearLayout) root.findViewById(R.id.member_talks_value);
		mInterests = (LinearLayout) root.findViewById(R.id.member_interests_value);

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			mIsFirstLoad = savedInstanceState.getBoolean(STATE_FIRST_LOAD, true);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		reload();

		if (mIsFirstLoad && mMemberId != 0) {
			refreshMemberData();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_FIRST_LOAD, mIsFirstLoad);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mImageLoader.stop();
	}

	public void setMemberId(int memberId) {
		if (mMemberId != memberId) {
			mMemberId = memberId;

			mIsFirstLoad = true;

			final Bundle b = getArguments();
			b.putInt(EXTRA_MEMBER_ID, memberId);

			reload();

			refreshMemberData();
		}
	}

	protected void reload() {
		final Bundle args = getArguments();
		final Intent i = UIUtils.fragmentArgumentsToIntent(args);
		final Uri memberUri = i.getData();
		if (memberUri == null) {
			if (!args.containsKey(EXTRA_MEMBER_ID) || args.getInt(EXTRA_MEMBER_ID) == -1) {
				clear();
				return;
			}
		}

		restartLoader(CURSOR_MEMBER, args, this);
		restartLoader(CURSOR_LINKS, args, this);
		restartLoader(CURSOR_LINKERS, args, this);
		restartLoader(CURSOR_TALKS, args, this);
		restartLoader(CURSOR_INTERESTS, args, this);
		restartLoader(CURSOR_SHARED_LINKS, args, this);
	}

	protected void clear() {
		showNoMemberSelected();
		displayMember(null);
		displayLinks(null);
		displayLinkers(null);
		displaySharedLinks(null);
		displayTalks(null);
		displayInterests(null);
	}

	protected int fetchIdMember(Uri uriMember, Bundle b) {
		if (uriMember == null) {
			if (b.containsKey(EXTRA_MEMBER_ID)) {
				return b.getInt(EXTRA_MEMBER_ID, -1);
			}
		} else {
			try {
				return Integer.parseInt(MixItContract.Members.getMemberId(uriMember));
			} catch (final NumberFormatException e) {
			}
		}
		return -1;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case CURSOR_MEMBER: {
				final Intent i = UIUtils.fragmentArgumentsToIntent(args);
				mMemberId = fetchIdMember(i.getData(), args);
				if (mMemberId == -1) {
					Log.e(TAG, "this case should have been detected before in reload() method");
					displayMember(null);
					return null;
				} else {
					final Uri memberUri = MixItContract.Members.buildMemberUri(String.valueOf(mMemberId));
					return new CursorLoader(getActivity(), memberUri, MixItContract.Members.PROJ_DETAIL.PROJECTION, null, null, null);
				}
			}

			case CURSOR_LINKS: {
				final Intent i = UIUtils.fragmentArgumentsToIntent(args);
				mMemberId = fetchIdMember(i.getData(), args);
				if (mMemberId == -1) {
					Log.e(TAG, "this case should have been detected before in reload() method");
					displayLinks(null);
					return null;
				} else {
					final Uri linksUri = MixItContract.Members.buildLinksDirUri(String.valueOf(mMemberId));
					return new CursorLoader(getActivity(), linksUri, MixItContract.Members.PROJ_LIST.PROJECTION, null, null, MixItContract.Members.DEFAULT_SORT);
				}
			}

			case CURSOR_LINKERS: {
				final Intent i = UIUtils.fragmentArgumentsToIntent(args);
				mMemberId = fetchIdMember(i.getData(), args);
				if (mMemberId == -1) {
					Log.e(TAG, "this case should have been detected before in reload() method");
					displayLinkers(null);
					return null;
				} else {
					final Uri linkersUri = MixItContract.Members.buildLinkersDirUri(String.valueOf(mMemberId));
					return new CursorLoader(getActivity(), linkersUri, MixItContract.Members.PROJ_LIST.PROJECTION, null, null,
							MixItContract.Members.DEFAULT_SORT);
				}
			}

			case CURSOR_TALKS: {
				final Intent i = UIUtils.fragmentArgumentsToIntent(args);
				mMemberId = fetchIdMember(i.getData(), args);
				if (mMemberId == -1) {
					Log.e(TAG, "this case should have been detected before in reload() method");
					displayTalks(null);
					return null;
				} else {
					final Uri talksUri = MixItContract.Members.buildSessionsDirUri(String.valueOf(mMemberId), null);
					return new CursorLoader(getActivity(), talksUri, MixItContract.Sessions.PROJ_LIST.PROJECTION, null, null,
							MixItContract.Sessions.DEFAULT_SORT);
				}
			}

			case CURSOR_INTERESTS: {
				final Intent i = UIUtils.fragmentArgumentsToIntent(args);
				mMemberId = fetchIdMember(i.getData(), args);
				if (mMemberId == -1) {
					Log.e(TAG, "this case should have been detected before in reload() method");
					displayInterests(null);
					return null;
				} else {
					final Uri interestsUri = MixItContract.Members.buildInterestsDirUri(String.valueOf(mMemberId));
					return new CursorLoader(getActivity(), interestsUri, MixItContract.Interests.PROJ.PROJECTION, null, null,
							MixItContract.Interests.DEFAULT_SORT);
				}
			}

			case CURSOR_SHARED_LINKS: {
				final Intent i = UIUtils.fragmentArgumentsToIntent(args);
				mMemberId = fetchIdMember(i.getData(), args);
				if (mMemberId == -1) {
					Log.e(TAG, "this case should have been detected before in reload() method");
					displaySharedLinks(null);
					return null;
				} else {
					final Uri sharesLinksUri = MixItContract.Members.buildSharedLinksDirUri(String.valueOf(mMemberId));
					return new CursorLoader(getActivity(), sharesLinksUri, MixItContract.SharedLinks.PROJ.PROJECTION, null, null,
							MixItContract.SharedLinks.DEFAULT_SORT);
				}
			}

			// case CURSOR_STARRED_SESSIONS :{
			// final Intent i = UIUtils.fragmentArgumentsToIntent(args);
			// mMemberId = fetchIdMember(i.getData(), args);
			// if (mMemberId == -1) {
			// Log.e(TAG, "this case should have been detected before in reload() method");
			// displaySessions(null);
			// return null;
			// } else {
			// Uri sessionsUri = MixItContract.Members.buildSessionsDirUri(String.valueOf(mMemberId), id == CURSOR_TALKS);
			// return new CursorLoader(getActivity(), sessionsUri, SessionsAdapter.SessionsQuery.PROJECTION, null, null, MixItContract.Sessions.DEFAULT_SORT);
			// }
			// }

			default:
				break;
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		final int id = loader.getId();
		switch (id) {
			case CURSOR_MEMBER:
				displayMember(data);
				break;

			case CURSOR_LINKS:
				displayLinks(data);
				break;

			case CURSOR_LINKERS:
				displayLinkers(data);
				break;

			case CURSOR_TALKS:
				displayTalks(data);
				break;

			case CURSOR_INTERESTS:
				displayInterests(data);
				break;

			case CURSOR_SHARED_LINKS:
				displaySharedLinks(data);
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		final int id = loader.getId();
		switch (id) {
			case CURSOR_MEMBER:
				displayMember(null);
				break;

			case CURSOR_LINKS:
				displayLinks(null);
				break;

			case CURSOR_LINKERS:
				displayLinkers(null);
				break;

			case CURSOR_TALKS:
				displayTalks(null);
				break;

			case CURSOR_INTERESTS:
				displayInterests(null);
				break;

			case CURSOR_SHARED_LINKS:
				displaySharedLinks(null);
				break;

			default:
				break;
		}
	}

	protected void showMember() {
		final int displayedChild = mViewAnimator.getDisplayedChild();
		if (displayedChild == 0) {
			mViewAnimator.showNext();
		} else if (displayedChild == 2) {
			mViewAnimator.showPrevious();
		}
	}

	protected void showNoMemberSelected() {
		final int displayedChild = mViewAnimator.getDisplayedChild();
		if (displayedChild == 1) {
			mViewAnimator.showNext();
		} else if (displayedChild == 0) {
			mViewAnimator.showPrevious();
		}
	}

	protected void displayMember(Cursor c) {
		String firstName = null;
		String lastName = null;
		String company = null;
		Spanned shortDesc = null;
		String imageUrl = null;
		int nbConsults = 0;
		String bio = null;
		Spanned bioSpanned = null;
		String level = null;
		int memberType = MixItContract.Members.TYPE_MEMBER;

		if (c != null && c.moveToFirst()) {
			showMember();

			firstName = c.getString(MixItContract.Members.PROJ_DETAIL.FIRSTNAME);
			lastName = c.getString(MixItContract.Members.PROJ_DETAIL.LASTNAME);

			company = c.getString(MixItContract.Members.PROJ_DETAIL.COMPANY);
			final MarkdownProcessor m = new MarkdownProcessor();
			final String shortDescHTML = m.markdown(c.getString(MixItContract.Members.PROJ_DETAIL.SHORT_DESC));
			shortDesc = Html.fromHtml(shortDescHTML);
			nbConsults = c.getInt(MixItContract.Members.PROJ_DETAIL.NB_CONSULT);
			bio = c.getString(MixItContract.Members.PROJ_DETAIL.LONG_DESC);
			imageUrl = c.getString(MixItContract.Members.PROJ_DETAIL.IMAGE_URL);
			memberType = c.getInt(MixItContract.Members.PROJ_DETAIL.TYPE);
			level = c.getString(MixItContract.Members.PROJ_DETAIL.LEVEL);
		} else {
			showNoMemberSelected();
			mImage.setImageDrawable(null);
		}

		fillHeader(firstName, lastName, company, shortDesc, imageUrl, nbConsults, memberType, level);

		if (TextUtils.isEmpty(bio) && getActivity() != null && !isDetached()) {
			bio = getActivity().getString(R.string.member_bio_empty);
			mBio.setText(bio);
		} else {
			final MarkdownProcessor m = new MarkdownProcessor();
			final String longDescHTML = m.markdown(bio);
			bioSpanned = Html.fromHtml(longDescHTML);
			mBio.setText(bioSpanned);
		}

		if (getActivity() != null && !isDetached()) {
			String memberTypeName = null;
			switch (memberType) {
				case MixItContract.Members.TYPE_SPEAKER:
					memberTypeName = getString(R.string.speaker_detail_title);
					break;

				case MixItContract.Members.TYPE_SPONSOR:
					memberTypeName = getString(R.string.sponsor_detail_title);
					break;

				case MixItContract.Members.TYPE_STAFF:
					memberTypeName = getString(R.string.staff_detail_title);
					break;

				case MixItContract.Members.TYPE_MEMBER:
				default:
					memberTypeName = getString(R.string.member_detail_title);
					break;
			}
			((MemberDetailsContract) getActivity()).setActionBarTitle(memberTypeName);
		}
	}

	protected void fillHeader(String firstName, String lastName, String company, Spanned shortDesc, String imageUrl, int nbConsults, int memberType,
			String level) {
		final StringBuilder nameStr = new StringBuilder();
		if (!TextUtils.isEmpty(firstName)) {
			nameStr.append(firstName);
			nameStr.append(' ');
		}
		nameStr.append(lastName);
		mMemberName = nameStr.toString();

		mName.setText(mMemberName);
		switch (memberType) {
			case MixItContract.Members.TYPE_SPONSOR:
				mCompany.setText(getString(R.string.sponsors_company_member_detail, company, level));
				break;

			case MixItContract.Members.TYPE_STAFF:
				mCompany.setText(getString(R.string.staff_company_member_detail, company));
				break;

			case MixItContract.Members.TYPE_SPEAKER:
			case MixItContract.Members.TYPE_MEMBER:
			default:
				mCompany.setText(company);
				break;
		}
		mShortDesc.setText(shortDesc);
		mImageLoader.displayImage(imageUrl, mImage, mOptions);
		mNbConsult.setText(getString(R.string.nb_consult, nbConsults));
	}

	protected void displayLinks(Cursor c) {
		int nbLinks = 0;

		if (c != null && c.moveToFirst()) {
			nbLinks = c.getCount();
		}

		mLinks.setText(getString(R.string.member_nb_links, nbLinks));
		mLinks.setEnabled(nbLinks != 0);
	}

	protected void displayLinkers(Cursor c) {
		int nbLinkers = 0;

		if (c != null && c.moveToFirst()) {
			nbLinkers = c.getCount();
		}

		mLinkers.setText(getString(R.string.member_nb_linkers, nbLinkers));
		mLinkers.setEnabled(nbLinkers != 0);
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	protected void displaySharedLinks(Cursor c) {
		mSharedLinks.removeAllViews();

		final Context ctx = getActivity();
		if (ctx == null) {
			return;
		}

		final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		final int margin = ctx.getResources().getDimensionPixelSize(R.dimen.margin_small);
		lp.setMargins(0, margin, 0, 0);

		if (c != null && c.moveToFirst()) {
			mSharedLinksTitle.setVisibility(View.VISIBLE);
			mSharedLinks.setVisibility(View.VISIBLE);
			do {
				final String sharedLinkUrl = c.getString(MixItContract.SharedLinks.PROJ.URL);
				final SharedLinkItemView sharedLinkView = new SharedLinkItemView(ctx);
				sharedLinkView.setBackgroundResource(R.drawable.item_detail_state_list_bkg);
				sharedLinkView.setContent(c);
				sharedLinkView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						onSharedLinkItemClick(sharedLinkUrl);
					}
				});
				mSharedLinks.addView(sharedLinkView, lp);
			} while (c.moveToNext());
		} else {
			mSharedLinksTitle.setVisibility(View.GONE);
			mSharedLinks.setVisibility(View.GONE);
		}
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	protected void displayTalks(Cursor c) {
		mTalks.removeAllViews();

		final Context ctx = getActivity();
		if (ctx == null) {
			return;
		}

		final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		final int margin = ctx.getResources().getDimensionPixelSize(R.dimen.margin_small);
		lp.setMargins(0, margin, 0, 0);

		if (c != null && c.moveToFirst()) {
			mTalksTitle.setVisibility(View.VISIBLE);
			mTalks.setVisibility(View.VISIBLE);
			do {
				final String talkId = c.getString(MixItContract.Sessions.PROJ_LIST.SESSION_ID);
				final TalkItemView talkView = new TalkItemView(ctx);
				talkView.setStarListener(new TalkItemView.StarListener() {

					@Override
					public void onStarTouched(String idTalk, String titleTalk, boolean state) {
						favoriteSessionWithDialog(idTalk, titleTalk, state);
					}
				});
				talkView.setBackgroundResource(R.drawable.item_detail_state_list_bkg);
				talkView.setContent(c, false);
				talkView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						onTalkItemClick(talkId);
					}
				});
				mTalks.addView(talkView, lp);
			} while (c.moveToNext());
		} else {
			mTalksTitle.setVisibility(View.GONE);
			mTalks.setVisibility(View.GONE);
		}
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	protected void displayInterests(Cursor c) {
		mInterests.removeAllViews();

		final Context ctx = getActivity();
		if (ctx == null) {
			return;
		}

		final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		final int margin = ctx.getResources().getDimensionPixelSize(R.dimen.margin_small);
		lp.setMargins(0, margin, 0, 0);

		final LayoutInflater inflater = LayoutInflater.from(ctx);

		if (c != null && c.moveToFirst()) {
			do {
				final String interestId = c.getString(MixItContract.Interests.PROJ.INTEREST_ID);
				final String interestName = c.getString(MixItContract.Interests.PROJ.NAME);

				final TextView interestView = (TextView) inflater.inflate(R.layout.item_interest, mInterests, false);
				interestView.setText(interestName);
				interestView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						onInterestItemClick(interestId, interestName);
					}
				});
				mInterests.addView(interestView, lp);
			} while (c.moveToNext());
		} else {
			inflater.inflate(R.layout.empty_text_view, mInterests, true);
			final TextView emptyView = (TextView) mInterests.findViewById(R.id.empty_text_view);
			emptyView.setText(R.string.member_empty_interests);
		}
	}

	protected static final String HTTP = "http://";

	public void onSharedLinkItemClick(String url) {
		if (!url.startsWith(HTTP) && !url.startsWith(HTTP)) {
			url = HTTP + url;
		}
		final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
	}

	public void onTalkItemClick(String sessionId) {
		final Uri sessionUri = MixItContract.Sessions.buildSessionUri(sessionId);
		final Intent intent = new Intent(Intent.ACTION_VIEW, sessionUri);
		intent.putExtra(IntentUtils.EXTRA_FROM_ADD_TO_BACKSTACK, true);
		startActivity(intent);
	}

	public void onInterestItemClick(String interestId, String name) {
		// final Uri interestUri = MixItContract.Interests.buildInterestUri(interestId);
		// final Intent intent = new Intent(Intent.ACTION_VIEW, interestUri);
		// intent.putExtra(InterestsActivity.EXTRA_INTEREST_NAME, name);
		// intent.putExtra(InterestsActivity.EXTRA_IS_FROM_SESSION, false);
		// startActivity(intent);
	}

	@Override
	protected void onServiceReady() {
		super.onServiceReady();

		if (mIsFirstLoad && mMemberId != 0) {
			refreshMemberData();
		}
	}

	protected void refreshMemberData() {
		if (mIsBound && mServiceReady) {
			setRefreshMode(true);

			final Message msg = Message.obtain(null, MixItService.MSG_MEMBER, 0, 0);
			msg.replyTo = mMessenger;
			final Bundle b = new Bundle();
			b.putInt(MixItService.EXTRA_ID, mMemberId);
			msg.setData(b);
			try {
				mService.send(msg);
			} catch (final RemoteException e) {
				e.printStackTrace();
			}

			mIsFirstLoad = false;
		}
	}

	@Override
	protected void onMessageReceivedFromService(Message msg) {
		if (msg.what == MixItService.MSG_MEMBER) {
			setRefreshMode(false);

			switch (msg.arg1) {
				case MixItService.Response.STATUS_OK:
					reload();
					break;

				case MixItService.Response.STATUS_ERROR:
					break;

				case MixItService.Response.STATUS_NO_CONNECTIVITY:
					break;

				default:
					break;
			}
		}
	}

	protected void favoriteSessionWithDialog(String sessionId, String sessionTitle, boolean addFavorite) {
		final boolean isWarningStarSessionShouldBeShown = PrefUtils.isWarningStarSessionShouldBeShown(getActivity());
		if (isWarningStarSessionShouldBeShown) {
			final WarningStarSessionDialogFragment frag = WarningStarSessionDialogFragment.newInstance(sessionId, sessionTitle, addFavorite);
			frag.setTargetFragment(this, WarningStarSessionDialogFragment.WARNING_NO_SYNC_STAR_SESSION);
			frag.show(getFragmentManager(), WarningStarSessionDialogFragment.TAG);
		} else {
			favoriteSession(sessionId, sessionTitle, addFavorite);
		}
	}

	protected void favoriteSession(String sessionId, String sessionTitle, boolean addFavorite) {
		if (getActivity() != null && !isDetached()) {
			final ContentResolver cr = getActivity().getContentResolver();
			new SessionAsyncTaskLoader(cr).starSession(sessionId, sessionTitle, addFavorite, this);
		}
	}

	@Override
	public void onWarningClickOk(String sessionId, String sessionTitle, boolean vote) {
		PrefUtils.setWarningStarSessionShouldBeShown(getActivity(), false);

		favoriteSession(sessionId, sessionTitle, vote);
	}

	@Override
	public void onStarSessionSuccessfull(String sessionId, String sessionTitle, boolean vote) {
		showCrouton(getString(vote ? R.string.star_session_success : R.string.unstar_session_success, sessionTitle), Style.CONFIRM);
	}

	@Override
	public void onStarSessionFailed(String sessionId, String sessionTitle, boolean vote, String error) {
		showCrouton(getString(vote ? R.string.star_session_failed : R.string.unstar_session_failed, sessionTitle), Style.ALERT);
	}

}
