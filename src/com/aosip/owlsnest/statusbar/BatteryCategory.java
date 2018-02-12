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

package com.aosip.owlsnest.statusbar;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class BatteryCategory extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String BATTERY_STYLE = "battery_style";
    private static final String BATTERY_PERCENT = "show_battery_percent";

    private ListPreference mBatteryIconStyle;
    private ListPreference mBatteryPercentage;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.battery);

        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        int batteryStyle = Settings.Secure.getInt(resolver,
                Settings.Secure.STATUS_BAR_BATTERY_STYLE, 0);
        mBatteryIconStyle = (ListPreference) findPreference(BATTERY_STYLE);
        mBatteryIconStyle.setValue(Integer.toString(batteryStyle));
        int valueIndex = mBatteryIconStyle.findIndexOfValue(String.valueOf(batteryStyle));
        mBatteryIconStyle.setSummary(mBatteryIconStyle.getEntries()[valueIndex]);
        mBatteryIconStyle.setOnPreferenceChangeListener(this);

        int showPercent = Settings.System.getInt(resolver,
                Settings.System.SHOW_BATTERY_PERCENT, 1);
        mBatteryPercentage = (ListPreference) findPreference(BATTERY_PERCENT);
        mBatteryPercentage.setValue(Integer.toString(showPercent));
        valueIndex = mBatteryPercentage.findIndexOfValue(String.valueOf(showPercent));
        mBatteryPercentage.setSummary(mBatteryPercentage.getEntries()[valueIndex]);
        mBatteryPercentage.setOnPreferenceChangeListener(this);
        boolean hideForcePercentage = batteryStyle == 6 || batteryStyle == 7; /*text or hidden style*/
        mBatteryPercentage.setEnabled(!hideForcePercentage);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mBatteryIconStyle) {
            int value = Integer.valueOf((String) objValue);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.STATUS_BAR_BATTERY_STYLE, value);
            int valueIndex = mBatteryIconStyle
                    .findIndexOfValue((String) objValue);
            mBatteryIconStyle
                    .setSummary(mBatteryIconStyle.getEntries()[valueIndex]);
            boolean hideForcePercentage = value == 6 || value == 7;/*text or hidden style*/
            mBatteryPercentage.setEnabled(!hideForcePercentage);
            return true;
        } else  if (preference == mBatteryPercentage) {
            int value = Integer.valueOf((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SHOW_BATTERY_PERCENT, value);
            int valueIndex = mBatteryPercentage
                    .findIndexOfValue((String) objValue);
            mBatteryPercentage
                    .setSummary(mBatteryPercentage.getEntries()[valueIndex]);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }

}
