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

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.mixit.android.ui.widgets.TalkItemView;


public class SessionsAdapter extends CursorAdapter {

	protected LayoutInflater mInflater;
	protected boolean mDisplayStar = true;
	protected TalkItemView.StarListener mStarListener;

	public SessionsAdapter(Context ctx) {
		this(ctx, true);
	}

	public SessionsAdapter(Context ctx, boolean displayStar) {
		super(ctx, null, 0);

		mInflater = LayoutInflater.from(ctx);
		mDisplayStar = displayStar;
	}

	public void setDisplayStar(boolean displayStar) {
		mDisplayStar = displayStar;
	}

	public void setStarListener(TalkItemView.StarListener starListener) {
		mStarListener = starListener;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final TalkItemView v = new TalkItemView(context);
		v.setStarListener(mStarListener);
		v.setShouldDisplayStar(mDisplayStar);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final TalkItemView v = (TalkItemView) view;
		v.setContent(cursor);
		// TODO : add grey background on past session
		// // Possibly indicate that the session has occurred in the past.
		// UIUtils.setSessionTitleColor(blockStart, blockEnd, titleView, subtitleView);
	}

}
