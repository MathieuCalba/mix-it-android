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

package fr.mixit.android.tasks;

import java.lang.ref.WeakReference;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;
import fr.mixit.android.MixItApplication;
import fr.mixit.android.provider.MixItContract;


public class SessionAsyncTaskLoader extends AsyncQueryHandler {

	protected static final boolean DEBUG_MODE = MixItApplication.DEBUG_MODE;
	private static final String TAG = SessionAsyncTaskLoader.class.getSimpleName();

	protected static final int TOKEN_STAR_SESSION = 1304141728;

	public interface StarSessionListener {

		void onStarSessionSuccessfull(String sessionId, String sessionTitle, boolean vote);

		void onStarSessionFailed(String sessionId, String sessionTitle, boolean vote, String error);
	}

	public SessionAsyncTaskLoader(ContentResolver cr) {
		super(cr);
	}

	public void starSession(String sessionId, String sessionTitle, boolean vote, StarSessionListener listener) {
		final ContentValues values = new ContentValues();
		values.put(MixItContract.Sessions.IS_FAVORITE, vote ? 1 : 0);
		final StarSessionCookie cookie = new StarSessionCookie(listener, sessionId, sessionTitle, vote);
		final Uri uri = MixItContract.Sessions.buildSessionUri(sessionId);
		startUpdate(TOKEN_STAR_SESSION, cookie, uri, values, null, null);
	}

	@Override
	protected void onUpdateComplete(int token, Object cookie, int result) {
		super.onUpdateComplete(token, cookie, result);

		switch (token) {
			case TOKEN_STAR_SESSION:
				if (!(cookie instanceof StarSessionCookie)) {
					if (DEBUG_MODE) {
						Log.e(TAG, "Cookie received in TOKEN_STAR_SESSION update request is not a " + StarSessionCookie.class);
					}
					return;
				}

				final StarSessionCookie cookieStarSession = (StarSessionCookie) cookie;

				if (result == 0) {
					if (DEBUG_MODE) {
						Log.e(TAG, "No session has been updated, seems that this session is no longer store in database");
					}

					final WeakReference<SessionAsyncTaskLoader.StarSessionListener> refListener = cookieStarSession.mRefListener;
					if (refListener != null) {
						final SessionAsyncTaskLoader.StarSessionListener listener = refListener.get();
						if (listener != null) {
							listener.onStarSessionFailed(cookieStarSession.mSessionId, cookieStarSession.mSessionTitle, cookieStarSession.mVote, "");
						}
					}

					return;
				}

				final WeakReference<SessionAsyncTaskLoader.StarSessionListener> refListener = cookieStarSession.mRefListener;
				if (refListener != null) {
					final SessionAsyncTaskLoader.StarSessionListener listener = refListener.get();
					if (listener != null) {
						listener.onStarSessionSuccessfull(cookieStarSession.mSessionId, cookieStarSession.mSessionTitle, cookieStarSession.mVote);
					}
				}
				break;

			default:
				break;
		}
	}

	protected static class StarSessionCookie {
		final WeakReference<SessionAsyncTaskLoader.StarSessionListener> mRefListener;
		final String mSessionId;
		final String mSessionTitle;
		final boolean mVote;

		public StarSessionCookie(SessionAsyncTaskLoader.StarSessionListener listener, String sessionId, String sessionTitle, boolean vote) {
			super();
			mRefListener = new WeakReference<SessionAsyncTaskLoader.StarSessionListener>(listener);
			mSessionId = sessionId;
			mSessionTitle = sessionTitle;
			mVote = vote;
		}

	}
}
