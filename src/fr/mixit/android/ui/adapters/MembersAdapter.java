package fr.mixit.android.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import fr.mixit.android.ui.widgets.MemberItemView;


public class MembersAdapter extends CursorAdapter {

	public MembersAdapter(Context ctx) {
		super(ctx, null, 0);
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
