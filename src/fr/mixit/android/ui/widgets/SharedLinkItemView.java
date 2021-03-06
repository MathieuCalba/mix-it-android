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

package fr.mixit.android.ui.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.mixit.android.provider.MixItContract;
import fr.mixit.android_2012.R;


public class SharedLinkItemView extends LinearLayout {

	protected TextView mTitle;
	protected TextView mSubTitle;

	public SharedLinkItemView(Context context) {
		super(context);

		init(context);
	}

	public SharedLinkItemView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public SharedLinkItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);
	}

	protected void init(Context context) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.item_shared_link, this, true);

		final int padding = context.getResources().getDimensionPixelSize(R.dimen.margin_small);
		setPadding(padding, padding, padding, padding);

		setOrientation(VERTICAL);

		mTitle = (TextView) findViewById(R.id.shared_link_title);
		mSubTitle = (TextView) findViewById(R.id.shared_link_subtitle);
	}

	public void setContent(Cursor c) {
		if (c == null) {
			return;
		}

		final String name = c.getString(MixItContract.SharedLinks.PROJ.NAME);
		final String url = c.getString(MixItContract.SharedLinks.PROJ.URL);

		mTitle.setText(name);
		mSubTitle.setText(url);
	}

}
