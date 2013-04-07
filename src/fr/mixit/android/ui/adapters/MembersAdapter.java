package fr.mixit.android.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import fr.mixit.android.provider.MixItContract;
import fr.mixit.android_2012.R;


public class MembersAdapter extends CursorAdapter {

	protected LayoutInflater mInflater;

	protected ImageLoader mImageLoader;
	protected DisplayImageOptions mOptions;

	protected class MemberHolder {
		ImageView image;
		TextView name;
		TextView company;
	}

	public MembersAdapter(Context ctx, ImageLoader imageLoader) {
		super(ctx, null, 0);

		mInflater = LayoutInflater.from(ctx);
		mImageLoader = imageLoader;

		mOptions = new DisplayImageOptions.Builder().showImageForEmptyUrl(R.drawable.speaker_thumbnail).showStubImage(R.drawable.speaker_thumbnail)
				.cacheInMemory().cacheOnDisc()
				// .decodingType(DecodingType.MEMORY_SAVING)
				.build();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View v = mInflater.inflate(R.layout.item_member, parent, false);
		final MemberHolder holder = new MemberHolder();
		holder.image = (ImageView) v.findViewById(R.id.member_image);
		holder.name = (TextView) v.findViewById(R.id.member_name);
		holder.company = (TextView) v.findViewById(R.id.member_company);
		v.setTag(holder);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final MemberHolder holder = (MemberHolder) view.getTag();

		// TODO : maybe harmonize syntax
		final StringBuilder name = new StringBuilder();
		final String firstName = cursor.getString(MixItContract.Members.PROJ_LIST.FIRSTNAME);
		if (!TextUtils.isEmpty(firstName)) {
			name.append(firstName);
			name.append(' ');
		}
		name.append(cursor.getString(MixItContract.Members.PROJ_LIST.LASTNAME));
		holder.name.setText(name);

		holder.company.setText(cursor.getString(MixItContract.Members.PROJ_LIST.COMPANY));

		final String url = cursor.getString(MixItContract.Members.PROJ_LIST.IMAGE_URL);
		mImageLoader.displayImage(url, holder.image, mOptions);
	}

}
