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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.petebevin.markdown.MarkdownProcessor;

import fr.mixit.android.provider.MixItContract;
import fr.mixit.android.services.MixItService;
import fr.mixit.android.tasks.SessionAsyncTaskLoader;
import fr.mixit.android.ui.widgets.MemberItemView;
import fr.mixit.android.utils.DateUtils;
import fr.mixit.android.utils.IntentUtils;
import fr.mixit.android.utils.PrefUtils;
import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;


public class SessionDetailsFragment extends BoundServiceFragment implements LoaderManager.LoaderCallbacks<Cursor>,
WarningStarSessionDialogFragment.WarningStarSessionDialogContract, SessionAsyncTaskLoader.StarSessionListener {

	public static final String TAG = SessionDetailsFragment.class.getSimpleName();

	protected static final int CURSOR_SESSION = 1006;
	protected static final int CURSOR_SPEAKERS = 1007;
	protected static final int CURSOR_INTERESTS = 1008;

	protected static final String EXTRA_SESSION_ID = "fr.mixit.android.EXTRA_SESSION_ID";

	protected static final String STATE_FIRST_LOAD = "fr.mixit.android.STATE_FIRST_LOAD";

	protected boolean mIsFirstLoad = true;

	protected int mSessionId;
	protected boolean mIsVoted = false;
	protected String mSessionFormat = MixItContract.Sessions.FORMAT_TALK;
	protected String mTitleStr;

	protected ViewAnimator mViewAnimator;
	protected TextView mTitle;
	protected TextView mSubTitle;
	protected TextView mSummary;
	protected LinearLayout mSpeakers;
	protected LinearLayout mInterests;

	// protected ImageLoader mImageLoader = ImageLoader.getInstance();

	public interface SessionDetailsContract {
		public void refreshMenu();

		public void refreshList();

		public void setActionBarTitle(String title);
	}

	public static SessionDetailsFragment newInstance(Intent intent) {
		final SessionDetailsFragment f = new SessionDetailsFragment();
		f.setArguments(UIUtils.intentToFragmentArguments(intent));
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof SessionDetailsContract)) {
			throw new IllegalArgumentException(activity.getClass().getName() + " must implement SessionDetailsContract");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_session_details, container, false);

		mViewAnimator = (ViewAnimator) root.findViewById(R.id.session_detail_animator);
		mTitle = (TextView) root.findViewById(R.id.session_title);
		mSubTitle = (TextView) root.findViewById(R.id.session_subtitle);
		mSummary = (TextView) root.findViewById(R.id.session_summary_value);
		mSpeakers = (LinearLayout) root.findViewById(R.id.session_speakers_value);
		mInterests = (LinearLayout) root.findViewById(R.id.session_interests_value);

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

		if (mIsFirstLoad && mSessionId != 0) {
			refreshSessionData();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_FIRST_LOAD, mIsFirstLoad);
	}

	// @Override
	// public void onStop() {
	// super.onStop();
	//
	// mImageLoader.stop();
	// }

	public void setSessionId(int sessionId) {
		if (mSessionId != sessionId) {
			mSessionId = sessionId;

			mIsFirstLoad = true;

			final Bundle b = getArguments();
			b.putInt(EXTRA_SESSION_ID, sessionId);

			reload();

			refreshSessionData();
		}
	}

	protected void reload() {
		final Bundle args = getArguments();
		final Intent i = UIUtils.fragmentArgumentsToIntent(args);
		final Uri sessionUri = i.getData();
		if (sessionUri == null) {
			if (!args.containsKey(EXTRA_SESSION_ID) || args.getInt(EXTRA_SESSION_ID) == -1) {
				clear();
				return;
			}
		}

		restartLoader(CURSOR_SESSION, args, this);
		restartLoader(CURSOR_SPEAKERS, args, this);
		restartLoader(CURSOR_INTERESTS, args, this);
	}

	protected void clear() {
		showNoSessionSelected();
		displaySession(null);
		displaySpeakers(null);
		displayInterests(null);
	}

	protected int fetchIdSession(Uri uriSession, Bundle b) {
		if (uriSession == null) {
			if (b.containsKey(EXTRA_SESSION_ID)) {
				return b.getInt(EXTRA_SESSION_ID, -1);
			}
		} else {
			try {
				return Integer.parseInt(MixItContract.Sessions.getSessionId(uriSession));
			} catch (final NumberFormatException e) {
			}
		}
		return -1;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == CURSOR_SESSION) {
			final Intent i = UIUtils.fragmentArgumentsToIntent(args);
			mSessionId = fetchIdSession(i.getData(), args);
			if (mSessionId == -1) {
				Log.e(TAG, "this case should have been detected before in reload() method");
				displaySession(null);
				return null;
			} else {
				final Uri sessionUri = MixItContract.Sessions.buildSessionUri(String.valueOf(mSessionId));
				return new CursorLoader(getActivity(), sessionUri, MixItContract.Sessions.PROJ_DETAIL.PROJECTION, null, null, null);
			}
		} else if (id == CURSOR_SPEAKERS) {
			final Intent i = UIUtils.fragmentArgumentsToIntent(args);
			mSessionId = fetchIdSession(i.getData(), args);
			if (mSessionId == -1) {
				Log.e(TAG, "this case should have been detected before in reload() method");
				displaySpeakers(null);
				return null;
			} else {
				final Uri speakersUri = MixItContract.Sessions.buildSpeakersDirUri(String.valueOf(mSessionId));
				return new CursorLoader(getActivity(), speakersUri, MixItContract.Members.PROJ_LIST.PROJECTION, null, null, null);
			}
		} else if (id == CURSOR_INTERESTS) {
			final Intent i = UIUtils.fragmentArgumentsToIntent(args);
			mSessionId = fetchIdSession(i.getData(), args);
			if (mSessionId == -1) {
				Log.e(TAG, "this case should have been detected before in reload() method");
				displayInterests(null);
				return null;
			} else {
				final Uri interestsUri = MixItContract.Sessions.buildInterestsDirUri(String.valueOf(mSessionId));
				return new CursorLoader(getActivity(), interestsUri, MixItContract.Interests.PROJ.PROJECTION, null, null, MixItContract.Interests.DEFAULT_SORT);
			}
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		final int id = loader.getId();
		if (id == CURSOR_SESSION) {
			displaySession(data);
		} else if (id == CURSOR_SPEAKERS) {
			displaySpeakers(data);
		} else if (id == CURSOR_INTERESTS) {
			displayInterests(data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		final int id = loader.getId();
		if (id == CURSOR_SESSION) {
			displaySession(null);
		} else if (id == CURSOR_SPEAKERS) {
			displaySpeakers(null);
		} else if (id == CURSOR_INTERESTS) {
			displayInterests(null);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater mInflater) {
		super.onCreateOptionsMenu(menu, mInflater);

		mInflater.inflate(R.menu.session_details, menu);
		final MenuItem actionItem = menu.findItem(R.id.menu_item_vote_favorite);
		actionItem.setVisible(false);

		// final MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
		// final ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
		// actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		final MenuItem actionItem = menu.findItem(R.id.menu_item_vote_favorite);
		if (mSessionFormat == null || mSessionFormat.equalsIgnoreCase(MixItContract.Sessions.FORMAT_LIGHTNING_TALK)) {
			actionItem.setVisible(false);
			// if (mIsVoted) {
			// actionItem.setTitle(R.string.action_bar_vote_delete);
			// actionItem.setIcon(R.drawable.ic_vote_down);
			// } else {
			// actionItem.setTitle(R.string.action_bar_vote_add);
			// actionItem.setIcon(R.drawable.ic_vote_up);
			// }
		} else {
			actionItem.setVisible(true);
			if (mIsVoted) {
				actionItem.setTitle(R.string.action_bar_favorite_delete);
				actionItem.setIcon(R.drawable.ic_starred);
			} else {
				actionItem.setTitle(R.string.action_bar_favorite_add);
				actionItem.setIcon(R.drawable.ic_star);
			}
		}

	}

	// actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
	// final ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
	// actionProvider.setShareIntent(createShareIntent());
	// }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		if (id == R.id.menu_item_vote_favorite) {
			// if (!mIsSession) {
			// voteForLightning(!mIsVoted);
			// } else {
			favoriteSessionWithDialog(String.valueOf(mSessionId), mTitleStr, !mIsVoted);
			// }
		}
		return super.onOptionsItemSelected(item);
	}

	protected void showSession() {
		final int displayedChild = mViewAnimator.getDisplayedChild();
		if (displayedChild == 0) {
			mViewAnimator.showNext();
		} else if (displayedChild == 2) {
			mViewAnimator.showPrevious();
		}
	}

	protected void showNoSessionSelected() {
		final int displayedChild = mViewAnimator.getDisplayedChild();
		if (displayedChild == 1) {
			mViewAnimator.showNext();
		} else if (displayedChild == 0) {
			mViewAnimator.showPrevious();
		}
	}

	protected void displaySession(Cursor c) {
		long start = 0;
		long end = 0;
		Spanned summary = null;
		String room = null;

		if (c != null && c.moveToFirst()) {
			showSession();

			mTitleStr = c.getString(MixItContract.Sessions.PROJ_DETAIL.TITLE);
			mTitleStr = c.getString(MixItContract.Sessions.PROJ_DETAIL.TITLE);
			start = c.getLong(MixItContract.Sessions.PROJ_DETAIL.START);
			end = c.getLong(MixItContract.Sessions.PROJ_DETAIL.END);
			final MarkdownProcessor m = new MarkdownProcessor();
			final String summaryHTML = m.markdown(c.getString(MixItContract.Sessions.PROJ_DETAIL.DESC));
			summary = Html.fromHtml(summaryHTML);
			room = c.getString(MixItContract.Sessions.PROJ_DETAIL.ROOM_ID);
			mSessionFormat = c.getString(MixItContract.Sessions.PROJ_DETAIL.FORMAT);
			mIsVoted = c.getInt(MixItContract.Sessions.FORMAT_LIGHTNING_TALK.equalsIgnoreCase(mSessionFormat) ? MixItContract.Sessions.PROJ_DETAIL.MY_VOTE
					: MixItContract.Sessions.PROJ_DETAIL.IS_FAVORITE) == 1 ? true : false;
		} else {
			showNoSessionSelected();
		}
		fillHeader(mTitleStr, mSessionFormat, start, end, room);
		mSummary.setText(summary);

		if (getActivity() != null && !isDetached()) {
			((SessionDetailsContract) getActivity()).refreshMenu();
			((SessionDetailsContract) getActivity()).setActionBarTitle(mSessionFormat);
		}
	}

	protected void fillHeader(String title, String format, long start, long end, String room) {
		mTitle.setText(title + " [" + format + "]");
		mSubTitle.setText(DateUtils.formatSessionTime(getActivity(), start, end, room));// "On DDD, from HH:MM to HH:MM, in " + room
		// mSubTitle.setText(time + " @ " + room); // TODO : change time, room display in session details
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	protected void displaySpeakers(Cursor c) {
		mSpeakers.removeAllViews();

		final Context ctx = getActivity();
		if (ctx == null) {
			return;
		}

		final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		final int margin = ctx.getResources().getDimensionPixelSize(R.dimen.margin_small);
		lp.setMargins(0, margin, 0, 0);

		if (c != null && c.moveToFirst()) {
			do {
				final String speakerId = c.getString(MixItContract.Members.PROJ_LIST.MEMBER_ID);
				final MemberItemView speakerView = new MemberItemView(ctx);
				speakerView.setBackgroundResource(R.drawable.item_detail_state_list_bkg);
				speakerView.setContent(c);
				speakerView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						onSpeakerItemClick(speakerId);
					}
				});
				mSpeakers.addView(speakerView, lp);
			} while (c.moveToNext());
		} else {
			final LayoutInflater inflater = LayoutInflater.from(ctx);
			inflater.inflate(R.layout.empty_text_view, mSpeakers, true);
			final TextView emptyView = (TextView) mSpeakers.findViewById(R.id.empty_text_view);
			emptyView.setText(R.string.sessions_empty_speakers);
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
			emptyView.setText(R.string.sessions_empty_interests);
		}
	}

	protected void onSpeakerItemClick(String memberId) {
		final Uri memberUri = MixItContract.Members.buildMemberUri(memberId);
		final Intent intent = new Intent(Intent.ACTION_VIEW, memberUri);
		intent.putExtra(IntentUtils.EXTRA_FROM_ADD_TO_BACKSTACK, true);
		startActivity(intent);
	}

	protected void onInterestItemClick(String interestId, String name) {
		// final Uri interestUri = MixItContract.Interests.buildInterestUri(interestId);
		// final Intent intent = new Intent(Intent.ACTION_VIEW, interestUri);
		// intent.putExtra(InterestsActivity.EXTRA_INTEREST_NAME, name);
		// intent.putExtra(InterestsActivity.EXTRA_IS_FROM_SESSION, true);
		// startActivity(intent);
	}

	protected Intent createShareIntent() {
		final Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_session_subject));
		shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_session_text, mTitleStr, "http://www.mix-it.fr/sessions"));
		return shareIntent;
	}

	@Override
	protected void onServiceReady() {
		super.onServiceReady();

		if (mIsFirstLoad && mSessionId != 0) {
			refreshSessionData();
		}
	}

	public void refreshSessionData() {
		if (mIsBound && mServiceReady) {
			setRefreshMode(true);

			final Message msg = Message.obtain(null,
					MixItContract.Sessions.FORMAT_LIGHTNING_TALK.equalsIgnoreCase(mSessionFormat) ? MixItService.MSG_LIGHTNING_TALK : MixItService.MSG_TALK, 0,
							0);
			msg.replyTo = mMessenger;
			final Bundle b = new Bundle();
			b.putInt(MixItService.EXTRA_ID, mSessionId);
			msg.setData(b);
			try {
				mService.send(msg);
			} catch (final RemoteException e) {
				e.printStackTrace();
			}

			mIsFirstLoad = false;
		}
	}

	// protected void voteForLightning(boolean addVote) {
	// if (mIsBound && mServiceReady) {
	// setRefreshMode(true);
	//
	// final Message msg = Message.obtain(null, MixItService.MSG_VOTE_LIGHTNING_TALK, 0, 0);
	// msg.replyTo = mMessenger;
	// final Bundle b = new Bundle();
	// b.putBoolean(MixItService.EXTRA_STATE_VOTE, addVote);
	// b.putInt(MixItService.EXTRA_SESSION_ID, mSessionId);
	// msg.setData(b);
	// try {
	// mService.send(msg);
	// } catch (final RemoteException e) {
	// e.printStackTrace();
	// }
	// }
	// }

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
		// if (mIsBound && mServiceReady) {
		// setRefreshMode(true);
		//
		// final Message msg = Message.obtain(null, MixItService.MSG_STAR_SESSION, 0, 0);
		// msg.replyTo = mMessenger;
		// final Bundle b = new Bundle();
		// b.putBoolean(MixItService.EXTRA_STATE_STAR, addFavorite);
		// b.putInt(MixItService.EXTRA_SESSION_ID, mSessionId);
		// msg.setData(b);
		// try {
		// mService.send(msg);
		// } catch (final RemoteException e) {
		// e.printStackTrace();
		// }
		// }
	}

	@Override
	public void onWarningClickOk(String sessionId, String sessionTitle, boolean vote) {
		PrefUtils.setWarningStarSessionShouldBeShown(getActivity(), false);

		favoriteSession(sessionId, sessionTitle, vote);
	}

	@Override
	public void onStarSessionSuccessfull(String sessionId, String sessionTitle, boolean vote) {
		// TODO : show a crouton instead
		Toast.makeText(getActivity(), getActivity().getString(vote ? R.string.star_session_success : R.string.unstar_session_success, sessionTitle),
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onStarSessionFailed(String sessionId, String sessionTitle, boolean vote, String error) {
		// TODO : show a crouton instead
		Toast.makeText(getActivity(), getActivity().getString(vote ? R.string.star_session_failed : R.string.unstar_session_failed, sessionTitle),
				Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onMessageReceivedFromService(Message msg) {
		if (msg.what == MixItService.MSG_TALK || msg.what == MixItService.MSG_LIGHTNING_TALK) {
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
		// else if (msg.what == MixItService.MSG_VOTE_LIGHTNING_TALK) {
		// setRefreshMode(false);
		//
		// switch (msg.arg1) {
		// case MixItService.Response.STATUS_OK:
		// reload();
		// mContract.refreshList();
		// break;
		//
		// case MixItService.Response.STATUS_ERROR:
		// Toast.makeText(getActivity(), R.string.error_vote_lightning_talk, Toast.LENGTH_SHORT).show();
		// break;
		//
		// case MixItService.Response.STATUS_NO_CONNECTIVITY:
		// Toast.makeText(getActivity(), R.string.functionnality_need_connectivity, Toast.LENGTH_SHORT).show();
		// break;
		//
		// default:
		// break;
		// }
		// } else if (msg.what == MixItService.MSG_STAR_SESSION) {
		// setRefreshMode(false);
		//
		// switch (msg.arg1) {
		// case MixItService.Response.STATUS_OK:
		// reload();
		// mContract.refreshList();
		// break;
		//
		// case MixItService.Response.STATUS_ERROR:
		// Toast.makeText(getActivity(), R.string.error_star_session, Toast.LENGTH_SHORT).show();
		// break;
		//
		// case MixItService.Response.STATUS_NO_CONNECTIVITY:
		// Toast.makeText(getActivity(), R.string.functionnality_need_connectivity, Toast.LENGTH_SHORT).show();
		// break;
		//
		// default:
		// break;
		// }
		// }
	}

}
