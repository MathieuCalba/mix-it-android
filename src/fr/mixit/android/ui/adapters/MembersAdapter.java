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
import android.view.View;
import android.view.ViewGroup;
import fr.mixit.android.ui.widgets.MemberItemView;


public class MembersAdapter extends CursorAdapter {

	public MembersAdapter(Context ctx) {
		super(ctx, null, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final MemberItemView v = new MemberItemView(context);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final MemberItemView v = (MemberItemView) view;
		v.setContent(cursor);
	}

}
