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

package fr.mixit.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import fr.mixit.android.ui.MapActivity;
import fr.mixit.android.ui.MembersActivity;
import fr.mixit.android.ui.PlanningActivity;
import fr.mixit.android.ui.SessionsActivity;
import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;


public class ExploreFragment extends BoundServiceFragment implements OnClickListener {

	public static ExploreFragment newInstance(Intent intent) {
		final ExploreFragment f = new ExploreFragment();
		f.setArguments(UIUtils.intentToFragmentArguments(intent));
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_explore, container, false);

		root.findViewById(R.id.sessions_bt).setOnClickListener(this);
		root.findViewById(R.id.speakers_bt).setOnClickListener(this);
		root.findViewById(R.id.map_bt).setOnClickListener(this);
		root.findViewById(R.id.planning_bt).setOnClickListener(this);

		return root;
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.sessions_bt) {
			if (getActivity() != null && !isDetached()) {
				final Intent i = new Intent(getActivity(), SessionsActivity.class);
				startActivity(i);
			}
		} else if (id == R.id.speakers_bt) {
			if (getActivity() != null && !isDetached()) {
				final Intent i = new Intent(getActivity(), MembersActivity.class);
				startActivity(i);
			}
		} else if (id == R.id.map_bt) {
			if (getActivity() != null && !isDetached()) {
				final Intent i = new Intent(getActivity(), MapActivity.class);
				startActivity(i);
			}
		} else if (id == R.id.planning_bt) {
			if (getActivity() != null && !isDetached()) {
				final Intent i = new Intent(getActivity(), PlanningActivity.class);
				startActivity(i);
			}
		}
	}

	@Override
	protected void onMessageReceivedFromService(Message msg) {
	}

}
