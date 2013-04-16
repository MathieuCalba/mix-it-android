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

	protected ImageLoader mImageLoader = ImageLoader.getInstance();
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

		final String imageUrl = c.getString(MixItContract.Members.PROJ_LIST.IMAGE_URL);
		mImageLoader.displayImage(imageUrl, mImage, mOptions);
	}

}
