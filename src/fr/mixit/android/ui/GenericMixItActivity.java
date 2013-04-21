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

package fr.mixit.android.ui;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import fr.mixit.android.MixItApplication;
import fr.mixit.android.services.MixItService;
import fr.mixit.android_2012.R;


public abstract class GenericMixItActivity extends SherlockFragmentActivity {

	protected static final boolean DEBUG_MODE = MixItApplication.DEBUG_MODE;
	private static final String TAG = GenericMixItActivity.class.getSimpleName();

	protected static final char SLASH = '/';

	/** Flag indicating whether we have called bind on the mService. */
	protected boolean mIsBound = false;
	/** Flag indicating whether the mService is bound and we have registered to it. */
	protected boolean mIsServiceReady = false;

	/** Messenger for communicating with mService. */
	protected Messenger mService = null;

	/** Handler of incoming messages from mService. */
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MixItService.MSG_REGISTER_CLIENT:
					onServiceReady();
					break;
				case MixItService.MSG_UNREGISTER_CLIENT:
					onServiceNotReady();
					break;
				default:
					if (DEBUG_MODE) {
						Log.d(TAG, "onMessageReceivedFromService(): What:" + msg.what + " - Status:" + msg.arg1);
					}
					onMessageReceivedFromService(msg);
			}
		}
	}

	/** Target we publish for mClients to send messages to IncomingHandler. */
	protected final Messenger mMessenger = new Messenger(new IncomingHandler());

	/** Class for interacting with the main interface of the mService. */
	private final ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder aService) {
			// This is called when the connection with the mService has been established, giving us the mService object we can use to interact with the
			// mService.
			// We are communicating with our mService through an IDL interface, so get a client-side representation of that from the raw mService object.
			mService = new Messenger(aService);

			// We want to monitor the mService for as long as we are connected to it.
			try {
				final Message msg = Message.obtain(null, MixItService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (final RemoteException e) {
				// In this case the mService has crashed before we could do anything with it; we can count on soon being disconnected (and then reconnected if
				// it
				// can be restarted) so there is no need to do anything here.
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the mService has been unexpectedly disconnected -- that is, its process crashed.
			mService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedStateInstance) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.onCreate(savedStateInstance);

		initActionBar();

		setContentView(getContentLayoutId());
	}

	protected void initActionBar() {
		final boolean isUpEnabled = getActivityLevel() > 0;
		final ActionBar sab = getSupportActionBar();
		sab.setDisplayHomeAsUpEnabled(isUpEnabled);
		sab.setHomeButtonEnabled(isUpEnabled);
		sab.setIcon(R.drawable.ic_action_bar);
		setRefreshMode(false);
	}

	public void setRefreshMode(boolean state) {
		setSupportProgressBarIndeterminateVisibility(state);
	}

	protected abstract int getContentLayoutId();

	@Override
	public void onStart() {
		super.onStart();

		doBindService();
	}

	@Override
	public void onStop() {
		super.onStop();

		doUnbindService();
	}

	protected void doBindService() {
		if (DEBUG_MODE) {
			Log.d(TAG, "doBindService()");
		}
		// Establish a connection with the mService. We use an explicit class name because there is no reason to be able to let other applications replace our
		// component.
		bindService(new Intent(this, MixItService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	protected void doUnbindService() {
		if (DEBUG_MODE) {
			Log.d(TAG, "doUnbindService()");
		}
		if (mIsBound) {
			// If we have received the mService, and hence registered with it, then now is the time to unregister.
			if (mService != null) {
				try {
					final Message msg = Message.obtain(null, MixItService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (final RemoteException e) {
					// There is nothing special we need to do if the mService has crashed.
				}
			}

			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	/** When the mService is bound and we are registered to it, we refresh the Data from WebService with the {@link MixItService} */
	protected void onServiceReady() {
		mIsServiceReady = true;
	}

	/**
	 * Called when the parent activity is no longer registered to this mService, ergo the mService won't treat possible request from us
	 */
	protected void onServiceNotReady() {
		mIsServiceReady = false;
	}

	/**
	 * Called when a message other than register and unregister is received from the mService. Be careful, you need to treat the information, and refresh UI
	 * before calling the super method.
	 * 
	 * @param msg the message received from the mService
	 */
	protected void onMessageReceivedFromService(Message msg) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				final int levelActivity = getActivityLevel();
				final Intent upIntent = getParentIntent(levelActivity - 1);
				if (upIntent == null) {
					// no parent activity ie. this activity is already on the top of the stack, so up button should not be touchable
					return false;
				}

				if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
					// This activity is not part of the application's task, so create a new task with a synthesized back stack.
					final TaskStackBuilder builder = TaskStackBuilder.create(this);
					for (int i = 0; i < levelActivity; i++) {
						final Intent intent = getParentIntent(i);
						builder.addNextIntent(intent);
					}
					builder.startActivities();
					finish();
				} else {
					// This activity is part of the application's task, so simply navigate up to the hierarchical parent activity.
					NavUtils.navigateUpTo(this, upIntent);
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected int getActivityLevel() {
		return 1;
	}

	protected Intent getParentIntent(int level) {
		if (level > getActivityLevel() || level < 0) {
			return null;
		}

		switch (level) {
			case 0:
				return new Intent(this, HomeActivity.class);
			default:
				return null;
		}
	}

}
