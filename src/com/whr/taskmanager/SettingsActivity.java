/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.whr.taskmanager;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Settings for the Alarm Clock.
 */
public class SettingsActivity extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return false;
	}
}