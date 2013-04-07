package fr.mixit.android.ui.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import fr.mixit.android.provider.MixItContract;
import fr.mixit.android_2012.R;


public class SessionSpeakerView extends LinearLayout {

	protected TextView mSpeakerName;
	protected ImageView mSpeakerImage;

	protected ImageLoader mImageLoader = ImageLoader.getInstance();
	protected DisplayImageOptions mOptions;

	public SessionSpeakerView(Context context) {
		super(context);

		init(context);
	}

	public SessionSpeakerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public SessionSpeakerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);
	}

	protected void init(Context context) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.session_speaker_item, this, true);

		final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		setLayoutParams(lp);

		final int padding = context.getResources().getDimensionPixelSize(R.dimen.margin_small);
		setPadding(padding, padding, padding, padding);

		setOrientation(HORIZONTAL);

		setBackgroundColor(context.getResources().getColor(R.color.light_gray));

		mSpeakerName = (TextView) findViewById(R.id.speaker_name);
		mSpeakerImage = (ImageView) findViewById(R.id.speaker_picture);

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

		mSpeakerName.setText(lastName + " " + firstName);

		final String imageUrl = c.getString(MixItContract.Members.PROJ_LIST.IMAGE_URL);
		mImageLoader.displayImage(imageUrl, mSpeakerImage, mOptions);
	}

}
