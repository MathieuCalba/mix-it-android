package fr.mixit.android.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import fr.mixit.android.ui.widgets.MemberItemView;
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
		final MemberItemView v = new MemberItemView(context);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final MemberItemView v = (MemberItemView) view;
		v.setContent(cursor);
	}

}
