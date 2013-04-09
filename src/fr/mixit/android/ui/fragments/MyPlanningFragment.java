package fr.mixit.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;

public class MyPlanningFragment extends BoundServiceFragment {

	public static MyPlanningFragment newInstance(Intent intent) {
		final MyPlanningFragment f = new MyPlanningFragment();
		f.setArguments(UIUtils.intentToFragmentArguments(intent));
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_my_planning, container, false);
		return root;
	}

	@Override
	protected void onMessageReceivedFromService(Message msg) {
		// TODO Auto-generated method stub

	}

}
