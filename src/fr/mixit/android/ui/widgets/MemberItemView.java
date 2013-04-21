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
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import fr.mixit.android.provider.MixItContract;
import fr.mixit.android_2012.R;


public class MemberItemView extends RelativeLayout {

	protected TextView mTitle;
	protected TextView mSubTitle;
	protected ImageView mImage;

	protected ImageLoader mImageLoader;
	protected DisplayImageOptions mOptions;

	public MemberItemView(Context context) {
		super(context);

		init(context);
	}

	public MemberItemView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	public MemberItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);
	}

	protected void init(Context context) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.item_member, this, true);

		final int padding = context.getResources().getDimensionPixelSize(R.dimen.margin_small);
		setPadding(padding, padding, padding, padding);

		mTitle = (TextView) findViewById(R.id.member_title);
		mSubTitle = (TextView) findViewById(R.id.member_subtitle);
		mImage = (ImageView) findViewById(R.id.member_image);

		mImageLoader = ImageLoader.getInstance();
		mOptions = new DisplayImageOptions.Builder()//
		.showImageForEmptyUrl(R.drawable.speaker_thumbnail)//
		.showStubImage(R.drawable.speaker_thumbnail) //
		.cacheInMemory()//
		.cacheOnDisc()//
		// .decodingType(DecodingType.MEMORY_SAVING)
		.build();
	}

	public void setContent(Cursor c) {
		if (c == null) {
			return;
		}

		final String firstName = c.getString(MixItContract.Members.PROJ_LIST.FIRSTNAME);
		final String lastName = c.getString(MixItContract.Members.PROJ_LIST.LASTNAME);
		mTitle.setText(lastName + " " + firstName);

		final String company = c.getString(MixItContract.Members.PROJ_LIST.COMPANY);
		mSubTitle.setText(company);

		if (mImageLoader != null) {
			final String imageUrl = c.getString(MixItContract.Members.PROJ_LIST.IMAGE_URL);
			mImageLoader.displayImage(imageUrl, mImage, mOptions);
		}
	}

}
