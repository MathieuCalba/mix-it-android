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
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import fr.mixit.android_2012.R;


public class MapFragment extends SherlockFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_map, container, false);

		final View map = v.findViewById(R.id.map_image);
		map.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				launchMap();
			}
		});

		return v;
	}

	protected void launchMap() {
		final Intent i = new Intent(
				Intent.ACTION_VIEW,
				Uri.parse("https://maps.google.fr/maps?q=SUPINFO+Lyon,+16+Rue+Jean+Desparmet,+Lyon&hl=en&ie=UTF8&ll=45.735781,4.877694&spn=0.010229,0.021586&sll=45.757956,4.835121&sspn=0.163601,0.345383&oq=SUPINFO+LYON+Les+Jardins+d%5C'Osaka+16+Rue+Jean+Desparmet+69008+LYON&hq=SUPINFO+Lyon,&hnear=16+Rue+Jean+Desparmet,+69008+Lyon,+Rh%C3%B4ne,+Rh%C3%B4ne-Alpes&t=m&z=16"));
		startActivity(i);
	}

}
