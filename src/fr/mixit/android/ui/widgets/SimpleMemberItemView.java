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
	}

}
