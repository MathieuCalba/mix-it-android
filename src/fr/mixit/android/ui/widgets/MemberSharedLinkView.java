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


public class MemberSharedLinkView extends LinearLayout {

	protected TextView mTitle;
	protected TextView mSubTitle;

	public MemberSharedLinkView(Context context) {
		super(context);

		init(context);
	}

	public MemberSharedLinkView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public MemberSharedLinkView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);
	}

	protected void init(Context context) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.member_shared_link_item, this, true);

		final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		setLayoutParams(lp);

		final int padding = context.getResources().getDimensionPixelSize(R.dimen.margin_small);
		setPadding(padding, padding, padding, padding);

		setOrientation(VERTICAL);

		setBackgroundColor(context.getResources().getColor(R.color.light_gray));

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
