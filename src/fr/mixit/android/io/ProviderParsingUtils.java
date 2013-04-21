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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import fr.mixit.android.MixItApplication;
import fr.mixit.android.utils.Sets;


public class ProviderParsingUtils {

	protected static final boolean DEBUG_MODE = MixItApplication.DEBUG_MODE;
	private static final String TAG = ProviderParsingUtils.class.getSimpleName();

	public static final int MAX_CONTENT_PROVIDER_OPERATIONS = 50;

	public static boolean applyBatch(String autority, ContentResolver resolver, ArrayList<ContentProviderOperation> batch, boolean force) {
		return addOpeAndApplyBatch(autority, resolver, batch, force, null);
	}

	public static boolean addOpeAndApplyBatch(String authority, ContentResolver resolver, ArrayList<ContentProviderOperation> batch, boolean force,
			ContentProviderOperation ope) {
		if (ope != null) {
			batch.add(ope);
		}

		final int size = batch.size();
		if (size == 0 || size < MAX_CONTENT_PROVIDER_OPERATIONS && !force) {
			return true;
		}

		try {
			resolver.applyBatch(authority, batch); // TODO : find a way to control the ContentProviderResults[]
			batch.clear();
			return true;
		} catch (final RemoteException e) {
			throw new RuntimeException("Problem applying mBatch operation", e);
		} catch (final OperationApplicationException e) {
			throw new RuntimeException("Problem applying mBatch operation", e);
		}
	}

	public static boolean isRowExisting(Uri uri, String[] projection, ContentResolver resolver) {
		final Cursor cursor = resolver.query(uri, projection, null, null, null);
		try {
			if (!cursor.moveToFirst()) {
				return false;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return true;
	}

	/**
	 * Returns those id's from a {@link android.net.Uri} that were not found in a given set.
	 */
	public static HashSet<String> getLostIds(Set<String> ids, Uri uri, String[] projection, int idColumnIndex, ContentResolver resolver) {
		return getLostIds(ids, uri, projection, idColumnIndex, resolver, null, null);
	}

	/**
	 * Returns those id's from a {@link android.net.Uri} that were not found in a given set.
	 */
	public static HashSet<String> getLostIds(Set<String> ids, Uri uri, String[] projection, int idColumnIndex, ContentResolver resolver, String selection,
			String[] selectionArgs) {
		final HashSet<String> lostIds = Sets.newHashSet();

		final Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					while (cursor.moveToNext()) {
						final String id = cursor.getString(idColumnIndex);
						if (!ids.contains(id)) {
							lostIds.add(id);
						}
					}
				}
			} finally {
				cursor.close();
			}
		}

		if (!lostIds.isEmpty() && DEBUG_MODE) {
			Log.d(TAG, "Found " + lostIds.size() + " for " + uri.toString() + " that need to be removed.");
		}

		return lostIds;
	}

}
