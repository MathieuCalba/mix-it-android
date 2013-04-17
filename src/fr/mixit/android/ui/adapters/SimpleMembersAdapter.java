package fr.mixit.android.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import fr.mixit.android.ui.widgets.SimpleMemberItemView;


public class SimpleMembersAdapter extends MembersAdapter {

	public SimpleMembersAdapter(Context ctx) {
		super(ctx);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final SimpleMemberItemView v = new SimpleMemberItemView(context);
		return v;
	}

}
