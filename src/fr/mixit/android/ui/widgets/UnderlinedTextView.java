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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;
import fr.mixit.android_2012.R;


public class UnderlinedTextView extends TextView {

	private final Paint mPaint = new Paint();
	private int mUnderlineHeight;

	public UnderlinedTextView(Context context) {
		super(context);

		manageAttributes(null, R.attr.UnderlinedTextViewStyle);
	}

	public UnderlinedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		manageAttributes(attrs, R.attr.UnderlinedTextViewStyle);
	}

	public UnderlinedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		manageAttributes(attrs, defStyle);
	}

	protected void manageAttributes(AttributeSet attrs, int defStyle) {
		final Context context = getContext();
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UnderlinedTextView, defStyle, 0);

		final Resources res = context.getResources();
		final int underlineColor = a.getColor(R.styleable.UnderlinedTextView_line_color, res.getColor(R.color.blue_mixit));
		setUnderlineColor(underlineColor);

		final int underlineHeight = a.getDimensionPixelSize(R.styleable.UnderlinedTextView_line_height, res.getDimensionPixelSize(R.dimen.underline_height));
		setUnderlineHeight(underlineHeight);

		a.recycle();
	}

	@Override
	public void setPadding(int left, int top, int right, int bottom) {
		super.setPadding(left, top, right, mUnderlineHeight + bottom);
	}

	public void setUnderlineHeight(int underlineHeight) {
		if (underlineHeight < 0) {
			underlineHeight = 0;
		}
		if (underlineHeight != mUnderlineHeight) {
			mUnderlineHeight = underlineHeight;
			setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() + underlineHeight);
		}
	}

	public void setUnderlineColor(int underlineColor) {
		if (mPaint.getColor() != underlineColor) {
			mPaint.setColor(underlineColor);
			invalidate();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawRect(0, getHeight() - mUnderlineHeight, getWidth(), getHeight(), mPaint);
	}
}
