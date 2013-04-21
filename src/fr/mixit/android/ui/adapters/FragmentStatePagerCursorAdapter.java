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

package fr.mixit.android.ui.adapters;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;


public abstract class FragmentStatePagerCursorAdapter extends FragmentStatePagerAdapter {

	protected boolean mDataValid;
	protected Cursor mCursor;
	protected int mRowIDColumn;
	protected Fragment[] mFragments;
	protected Bundle mBundle;
	private static final String COLUMNS_UID = "_id";

	public FragmentStatePagerCursorAdapter(Cursor c, FragmentManager fm, Bundle b) {
		super(fm);
		init(c, b);
	}

	protected void init(Cursor c, Bundle b) {
		final boolean cursorPresent = c != null;
		mCursor = c;
		mDataValid = cursorPresent;
		mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow(COLUMNS_UID) : -1;
		if (c != null) {
			mFragments = new Fragment[c.getCount()];
		}
		mBundle = b;
	}

	public Cursor getCursor() {
		return mCursor;
	}

	@Override
	public int getCount() {
		if (mDataValid && mCursor != null) {
			return mCursor.getCount();
		} else {
			return 0;
		}
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if (!mDataValid) {
			throw new IllegalStateException("this should only be called when the cursor is valid");
		}
		if (!mCursor.moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position " + position);
		}

		final Fragment f = (Fragment) super.instantiateItem(container, position);

		mFragments[position] = f;
		return f;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
		mFragments[position] = null;
	}

	public void changeCursor(Cursor cursor) {
		final Cursor old = swapCursor(cursor);
		if (old != null) {
			old.close();
		}
	}

	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == mCursor) {
			return null;
		}
		final Cursor oldCursor = mCursor;
		mCursor = newCursor;
		if (newCursor != null) {
			mRowIDColumn = newCursor.getColumnIndexOrThrow(COLUMNS_UID);
			mDataValid = true;
			notifyDataSetChanged();
		} else {
			mRowIDColumn = -1;
			mDataValid = false;
			notifyDataSetChanged();
		}
		return oldCursor;
	}

	@Override
	public void notifyDataSetChanged() {
		if (mDataValid && mCursor != null) {
			mFragments = new Fragment[mCursor.getCount()];
		} else {
			mFragments = null;
		}
		super.notifyDataSetChanged();
	}

}
