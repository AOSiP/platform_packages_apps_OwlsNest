/*
 * Copyright (C) 2017 The ABC rom
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
package com.aosip.owlsnest.notification;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto;

import com.aosip.support.preference.SystemSettingSwitchPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.aosip.support.preference.ColorSelectPreference;

public class BatteryLightSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private ColorSelectPreference mLowColor;
    private ColorSelectPreference mMediumColor;
    private ColorSelectPreference mFullColor;
    private ColorSelectPreference mReallyFullColor;
    private SystemSettingSwitchPreference mLowBatteryBlinking;

    private PreferenceCategory mColorCategory;
    private int mColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.battery_light_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        mColorCategory = (PreferenceCategory) findPreference("battery_light_cat");

        mLowBatteryBlinking = (SystemSettingSwitchPreference)prefSet.findPreference("battery_light_low_blinking");
        if (getResources().getBoolean(
                        com.android.internal.R.bool.config_ledCanPulse)) {
            mLowBatteryBlinking.setChecked(Settings.System.getIntForUser(getContentResolver(),
                            Settings.System.BATTERY_LIGHT_LOW_BLINKING, 0, UserHandle.USER_CURRENT) == 1);
            mLowBatteryBlinking.setOnPreferenceChangeListener(this);
        } else {
            prefSet.removePreference(mLowBatteryBlinking);
        }

        if (getResources().getBoolean(com.android.internal.R.bool.config_multiColorBatteryLed)) {
            mColor = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.BATTERY_LIGHT_LOW_COLOR, 0xFFFF0000,
                            UserHandle.USER_CURRENT);
            mLowColor = (ColorSelectPreference) findPreference("battery_light_low_color");
            mLowColor.setColor(mColor);
            mLowColor.setOnPreferenceChangeListener(this);

            mColor = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.BATTERY_LIGHT_MEDIUM_COLOR, 0xFFFFFF00,
                            UserHandle.USER_CURRENT);
            mMediumColor = (ColorSelectPreference) findPreference("battery_light_medium_color");
            mMediumColor.setColor(mColor);
            mMediumColor.setOnPreferenceChangeListener(this);

            mColor = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.BATTERY_LIGHT_FULL_COLOR, 0xFFFFFF00,
                            UserHandle.USER_CURRENT);
            mFullColor = (ColorSelectPreference) findPreference("battery_light_full_color");
            mFullColor.setColor(mColor);
            mFullColor.setOnPreferenceChangeListener(this);

            mColor = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.BATTERY_LIGHT_REALLYFULL_COLOR, 0xFF00FF00,
                            UserHandle.USER_CURRENT);
            mReallyFullColor = (ColorSelectPreference) findPreference("battery_light_reallyfull_color");
            mReallyFullColor.setColor(mColor);
            mReallyFullColor.setOnPreferenceChangeListener(this);
        } else {
            prefSet.removePreference(mColorCategory);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mLowColor)) {
            ColorSelectPreference lowPref = (ColorSelectPreference) preference;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.BATTERY_LIGHT_LOW_COLOR, lowPref.getColor(),
                    UserHandle.USER_CURRENT);
            mColor = lowPref.getColor();
            mLowColor.setColor(mColor);
            return true;
        } else if (preference.equals(mMediumColor)) {
            ColorSelectPreference mediumPref = (ColorSelectPreference) preference;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.BATTERY_LIGHT_MEDIUM_COLOR, mediumPref.getColor(),
                    UserHandle.USER_CURRENT);
            mColor = mediumPref.getColor();
            mMediumColor.setColor(mColor);
            return true;
        } else if (preference.equals(mFullColor)) {
            ColorSelectPreference fullPref = (ColorSelectPreference) preference;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.BATTERY_LIGHT_FULL_COLOR, fullPref.getColor(),
                    UserHandle.USER_CURRENT);
            mColor = fullPref.getColor();
            mFullColor.setColor(mColor);
            return true;
        } else if (preference.equals(mReallyFullColor)) {
            ColorSelectPreference reallyPref = (ColorSelectPreference) preference;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.BATTERY_LIGHT_REALLYFULL_COLOR, reallyPref.getColor(),
                    UserHandle.USER_CURRENT);
            mColor = reallyPref.getColor();
            mReallyFullColor.setColor(mColor);
            return true;
        } else if (preference == mLowBatteryBlinking) {
            boolean value = (Boolean) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.BATTERY_LIGHT_LOW_BLINKING, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            mLowBatteryBlinking.setChecked(value);
            return true;
        }
        return false;
    }
}
