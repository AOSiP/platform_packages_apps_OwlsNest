/*
 * Copyright (C) 2017 Android Open Source Illusion Project
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

package com.aosip.owlsnest.advanced;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.Spannable;
import android.text.TextUtils;
import android.widget.EditText;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.aosip.owlsnest.preference.CustomSeekBarPreference;

public class SystemCategory extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String SCREENSHOT_DELAY = "screenshot_delay";
    private static final String SCREENRECORD_CHORD_TYPE = "screenrecord_chord_type";
    private static final String PREF_AOSIP_SETTINGS_SUMMARY = "aosip_settings_summary";

    private Preference mCustomSummary;
    private String mCustomSummaryText;
    private CustomSeekBarPreference mScreenshotDelay;
    private ListPreference mScreenrecordChordType;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.OWLSNEST;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.aosip_system);
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mScreenshotDelay = (CustomSeekBarPreference) findPreference(SCREENSHOT_DELAY);
        int screenshotDelay = Settings.System.getInt(resolver,
                Settings.System.SCREENSHOT_DELAY, 1000);
        mScreenshotDelay.setValue(screenshotDelay / 1);
        mScreenshotDelay.setOnPreferenceChangeListener(this);
        int recordChordValue = Settings.System.getInt(resolver,
                Settings.System.SCREENRECORD_CHORD_TYPE, 0);
        mScreenrecordChordType = initActionList(SCREENRECORD_CHORD_TYPE,
                recordChordValue);

        mCustomSummary = (Preference) prefScreen.findPreference(PREF_AOSIP_SETTINGS_SUMMARY);
        updateCustomSummaryTextString();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mScreenshotDelay) {
            int screenshotDelay = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SCREENSHOT_DELAY, screenshotDelay * 1);
            return true;
        } else if  (preference == mScreenrecordChordType) {
            handleActionListChange(mScreenrecordChordType, newValue,
                    Settings.System.SCREENRECORD_CHORD_TYPE);
            return true;
        }
        return false;
    }

     private ListPreference initActionList(String key, int value) {
         ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
         list.setValue(Integer.toString(value));
         list.setSummary(list.getEntry());
         list.setOnPreferenceChangeListener(this);
         return list;
     }
 
     private void handleActionListChange(ListPreference pref, Object newValue, String setting) {
         String value = (String) newValue;
         int index = pref.findIndexOfValue(value);
         pref.setSummary(pref.getEntries()[index]);
         Settings.System.putInt(getActivity().getContentResolver(), setting, Integer.valueOf(value));
     }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mCustomSummary) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.custom_summary_title);
            alert.setMessage(R.string.custom_summary_explain);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(TextUtils.isEmpty(mCustomSummaryText) ? "" : mCustomSummaryText);
            input.setSelection(input.getText().length());
            alert.setView(input);
            alert.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = ((Spannable) input.getText()).toString().trim();
                            Settings.System.putString(resolver, Settings.System.AOSIP_SETTINGS_SUMMARY, value);
                            updateCustomSummaryTextString();
                        }
                    });
            alert.setNegativeButton(getString(android.R.string.cancel), null);
            alert.show();
        } else {
            return super.onPreferenceTreeClick(preference);
        }
        return false;
    }

    private void updateCustomSummaryTextString() {
        mCustomSummaryText = Settings.System.getString(
                getActivity().getContentResolver(), Settings.System.AOSIP_SETTINGS_SUMMARY);

        if (TextUtils.isEmpty(mCustomSummaryText)) {
            mCustomSummary.setSummary(R.string.owlsnest_summary_title);
        } else {
            mCustomSummary.setSummary(mCustomSummaryText);
        } 
    }
}

