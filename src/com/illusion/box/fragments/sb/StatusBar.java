/*
 * Copyright (C) 2013 SlimRoms Project
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

package com.illusion.box.fragments.sb;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;

import com.illusion.box.R;
import com.illusion.box.preference.SettingsPreferenceFragment;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceScreen;
import android.provider.Settings.SettingNotFoundException;
//import android.telephony.MSimTelephonyManager;
import com.illusion.box.Utils;
import com.android.internal.util.slim.DeviceUtils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "StatusBarSettings";

    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    private static final String KEY_STATUS_BAR_TICKER = "status_bar_ticker_enabled";
    private static final String STATUS_BAR_CARRIER = "status_bar_carrier";
    private static final String STATUS_BAR_CARRIER_COLOR = "status_bar_carrier_color";

    static final int DEFAULT_STATUS_CARRIER_COLOR = 0xffffffff;

    private SwitchPreference mStatusBarBrightnessControl;
    private SwitchPreference mTicker;
    SwitchPreference mStatusBarCarrier;
    ColorPickerPreference mCarrierColorPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar_settings);

        int intColor;
        String hexColor;

        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();
        PackageManager pm = getPackageManager();
        Resources systemUiResources;
        try {
            systemUiResources = pm.getResourcesForApplication("com.android.systemui");
        } catch (Exception e) {
            Log.e(TAG, "can't access systemui resources",e);
            return;
        }

        // MIUI-like carrier Label
        mStatusBarCarrier = (SwitchPreference) findPreference(STATUS_BAR_CARRIER);
        mStatusBarCarrier.setChecked((Settings.System.getInt(getContentResolver(),
               Settings.System.STATUS_BAR_CARRIER, 0) == 1));
        mStatusBarCarrier.setOnPreferenceChangeListener(this);

       // MIUI-like carrier Label color
       mCarrierColorPicker = (ColorPickerPreference) findPreference(STATUS_BAR_CARRIER_COLOR);
       mCarrierColorPicker.setOnPreferenceChangeListener(this);
       intColor = Settings.System.getInt(getContentResolver(),
               Settings.System.STATUS_BAR_CARRIER_COLOR, DEFAULT_STATUS_CARRIER_COLOR);
               hexColor = String.format("#%08x", (0xffffffff & intColor));
       mCarrierColorPicker.setSummary(hexColor);
       mCarrierColorPicker.setNewPreviewColor(intColor);

        mTicker = (SwitchPreference) prefSet.findPreference(KEY_STATUS_BAR_TICKER);
        final boolean tickerEnabled = systemUiResources.getBoolean(systemUiResources.getIdentifier(
                    "com.android.systemui:bool/enable_ticker", null, null));
        mTicker.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_TICKER_ENABLED, tickerEnabled ? 1 : 0) == 1);
        mTicker.setOnPreferenceChangeListener(this);

        mStatusBarBrightnessControl =
            (SwitchPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarBrightnessControl.setChecked((Settings.System.getInt(getContentResolver(),
                            Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
        mStatusBarBrightnessControl.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mStatusBarCarrier) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_CARRIER, mStatusBarCarrier.isChecked() ? 1 : 0);
            return true;
            }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarBrightnessControl) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL,
                    (Boolean) objValue ? 1 : 0);
            return true;
        } else if (preference == mTicker) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_TICKER_ENABLED,
                    (Boolean) objValue ? 1 : 0);
            return true;
        } else if (preference == mCarrierColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                   Settings.System.STATUS_BAR_CARRIER_COLOR, intHex);
            return true;
        }
        return false;
    }
}
