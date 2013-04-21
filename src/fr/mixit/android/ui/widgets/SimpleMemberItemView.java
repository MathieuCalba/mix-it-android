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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import fr.mixit.android_2012.R;


public class SimpleMemberItemView extends MemberItemView {

	public SimpleMemberItemView(Context context) {
		super(context);
	}

	public SimpleMemberItemView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	public SimpleMemberItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void init(Context context) {
		super.init(context);

		setMinimumHeight(context.getResources().getDimensionPixelSize(R.dimen.small_touchable_height));
		mSubTitle.setVisibility(View.GONE);
		mImage.setVisibility(View.GONE);
		mImageLoader = null;
	}

}
