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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import fr.mixit.android.utils.UIUtils;
import fr.mixit.android_2012.R;


public class AboutFragment extends SherlockDialogFragment {

	public static final String TAG = AboutFragment.class.getSimpleName();

	public static AboutFragment newInstance(Intent intent) {
		final AboutFragment f = new AboutFragment();
		f.setArguments(UIUtils.intentToFragmentArguments(intent));
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Dialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_about, container, false);
		final TextView version = (TextView) v.findViewById(R.id.about);
		version.setText(getString(R.string.about_text, //
				UIUtils.getAppVersionName(getActivity(), getActivity().getPackageName()), //
				getString(R.string.license_action_bar_sherlock), //
				getString(R.string.license_universal_image_loader), //
				getString(R.string.license_view_pager_indicator), //
				getString(R.string.license_crouton)));
		return v;
	}

}
