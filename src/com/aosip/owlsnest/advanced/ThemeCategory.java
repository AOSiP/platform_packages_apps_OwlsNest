/*
 *  Copyright (C) 2015-2018 Android Open Source Illusion Project
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

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.util.Log;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.wrapper.OverlayManagerWrapper;
import com.android.settings.wrapper.OverlayManagerWrapper.OverlayInfo;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.aosip.support.colorpicker.ColorPickerPreference;

import java.util.ArrayList;
import java.util.List;

public class ThemeCategory extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String KEY_DARKUI_MODE = "dark_ui_mode";
    private static final String ACCENT_COLOR = "accent_color";
    private static final String ACCENT_COLOR_PROP = "persist.sys.theme.accentcolor";

    private ColorPickerPreference mThemeColor;
    private ListPreference mDarkUiModePref;
    private OverlayManagerWrapper mOverlayService;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OWLSNEST;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.theme);
        setupAccentPref();
        setupStylePref();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mThemeColor) {
            int color = (Integer) newValue;
            String hexColor = String.format("%08X", (0xFFFFFFFF & color));
            SystemProperties.set(ACCENT_COLOR_PROP, hexColor);
            mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
            mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
            mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
        } else if (preference == mDarkUiModePref) {
            int value = Integer.parseInt((String) newValue);
            Settings.Secure.putInt(getContext().getContentResolver(), Settings.Secure.THEME_MODE, value);
            mDarkUiModePref.setSummary(mDarkUiModePref.getEntries()[value]);
        }
        return true;
    }

    private void setupAccentPref() {
        mThemeColor = (ColorPickerPreference) findPreference(ACCENT_COLOR);
        String colorVal = SystemProperties.get(ACCENT_COLOR_PROP, "-1");
        int color = "-1".equals(colorVal)
                ? Color.WHITE
                : Color.parseColor("#" + colorVal);
        mThemeColor.setNewPreviewColor(color);
        mThemeColor.setOnPreferenceChangeListener(this);
    }

    private void setupStylePref() {
        mDarkUiModePref = (ListPreference) findPreference(KEY_DARKUI_MODE);
        int value = Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.THEME_MODE, 0);
        int index = mDarkUiModePref.findIndexOfValue(Integer.toString(value));
        mDarkUiModePref.setValue(Integer.toString(value));
        mDarkUiModePref.setSummary(mDarkUiModePref.getEntries()[index]);
        mDarkUiModePref.setOnPreferenceChangeListener(this);
    }

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                 @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                     final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.theme;
                    result.add(sir);
                    return result;
                }
                 @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}

