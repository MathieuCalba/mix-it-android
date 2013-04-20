package fr.mixit.android.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;

import fr.mixit.android.MixItApplication;
import fr.mixit.android.services.MixItService;


public abstract class BoundServiceFragment extends SherlockFragment {

	protected static final boolean DEBUG_MODE = MixItApplication.DEBUG_MODE;
	public static final String TAG = BoundServiceFragment.class.getSimpleName();

	public interface BoundServiceContract {
		public void setRefreshMode(boolean state);
	}

	/** Flag indicating whether we have called bind on the mService. */
	protected boolean mIsBound = false;
	/**
	 * Flag indicating whether the mService is bound and we have registered to it.
	 */
	protected boolean mServiceReady = false;

	/** Messenger for communicating with mService. */
	protected Messenger mService = null;

	/**
	 * Handler of incoming messages from mService.
	 */
	@SuppressLint("HandlerLeak")
	protected class IncomingHandler extends Handler {
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

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/**
	 * Class for interacting with the main interface of the mService.
	 */
	private final ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder aService) {
			// This is called when the connection with the mService has been established, giving us the mService object we can use to interact with the
			// mService. We are communicating with our mService through an IDL interface, so get a client-side representation of that from the raw mService
			// object.
			mService = new Messenger(aService);

			// We want to monitor the mService for as long as we are connected to it.
			try {
				final Message msg = Message.obtain(null, MixItService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (final RemoteException e) {
				// In this case the mService has crashed before we could even do anything with it; we can count on soon being disconnected (and then reconnected
				// if it can be restarted) so there is no need to do anything here.
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the mService has been unexpectedly disconnected -- that is, its process crashed.
			mService = null;
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			@SuppressWarnings("unused")
			final BoundServiceContract contract = (BoundServiceContract) activity;
		} catch (final ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement " + BoundServiceContract.class.getName());
		}
	}

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
		if (getActivity() == null || isDetached()) {
			if (DEBUG_MODE) {
				Log.d(TAG, "Fragment is detached from hos activity or getActivity is null, so impossible to bind service");
			}
			return;
		}
		getActivity().bindService(new Intent(getActivity(), MixItService.class), mConnection, Context.BIND_AUTO_CREATE);
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
			if (getActivity() == null || isDetached()) {
				if (DEBUG_MODE) {
					Log.d(TAG, "Fragment is detached from hos activity or getActivity is null, so impossible to unbind service");
				}
			} else {
				getActivity().unbindService(mConnection);
			}
			mIsBound = false;
		}
	}

	/**
	 * When the mService is bound and we are registered to it, we refresh the Data from WebService with the {@link MixItService}
	 */
	protected void onServiceReady() {
		mServiceReady = true;
	}

	/**
	 * Called when a message other than register and unregister is received from the mService. Be careful, you need to treat the information, and refresh UI
	 * before calling the super method.
	 * 
	 * @param msg the message received from the mService
	 */
	abstract protected void onMessageReceivedFromService(Message msg);

	/**
	 * Called when the parent activity is no longer registered to this mService, ergo the mService won't treat possible request from us
	 */
	protected void onServiceNotReady() {
		mServiceReady = false;
	}

	protected void setRefreshMode(boolean state) {
		if (getActivity() != null && !isDetached()) {
			((BoundServiceContract) getActivity()).setRefreshMode(state);
		}
	}

	protected void restartLoader(int id, Bundle args, LoaderCallbacks<Cursor> callback) {
		if (getActivity() == null || isDetached()) {
			return;
		}
		getLoaderManager().restartLoader(id, args, callback);
	}

}
