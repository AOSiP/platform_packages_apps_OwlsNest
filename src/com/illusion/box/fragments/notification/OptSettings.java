/*
* Copyright (C) 2014 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.illusion.box.fragments.notification;

import android.os.Bundle;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import com.illusion.box.R;
import com.illusion.box.preference.SettingsPreferenceFragment;

public class OptSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String STATUS_BAR_SHOW_WEATHER = "status_bar_show_weather";
    private static final String QS_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
    private static final String QS_LOCATION_ADVANCED = "qs_location_advanced";

    private SwitchPreference mShowWeather;
    private SwitchPreference mBrightnessSlider;
    private SwitchPreference mLocationAdvanced;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.opt_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mShowWeather = (SwitchPreference) prefSet.findPreference(STATUS_BAR_SHOW_WEATHER);
        mShowWeather.setChecked(Settings.System.getIntForUser(resolver,
            Settings.System.STATUS_BAR_SHOW_WEATHER, 1, UserHandle.USER_CURRENT) == 1);
        mShowWeather.setOnPreferenceChangeListener(this);

        mBrightnessSlider = (SwitchPreference) prefSet.findPreference(QS_SHOW_BRIGHTNESS_SLIDER);
        mBrightnessSlider.setChecked(Settings.System.getIntForUser(resolver,
            Settings.System.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT) == 1);
        mBrightnessSlider.setOnPreferenceChangeListener(this);

        mLocationAdvanced = (SwitchPreference) prefSet.findPreference(QS_LOCATION_ADVANCED);
        mLocationAdvanced.setChecked(Settings.System.getIntForUser(resolver,
            Settings.System.QS_LOCATION_ADVANCED, 1, UserHandle.USER_CURRENT) == 1);
        mLocationAdvanced.setOnPreferenceChangeListener(this);

    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mShowWeather) {
            boolean show = (Boolean) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_WEATHER, show ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mBrightnessSlider) {
            boolean show = (Boolean) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.QS_SHOW_BRIGHTNESS_SLIDER, show ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mLocationAdvanced) {
            boolean show = (Boolean) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.QS_LOCATION_ADVANCED, show ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }
}

