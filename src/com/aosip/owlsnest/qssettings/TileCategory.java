/*
 * Copyright (C) 2017 Androis Open Source Illusion Project
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

package com.aosip.owlsnest.qssettings;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;

import com.aosip.owlsnest.preference.CustomSeekBarPreference;
import com.android.internal.util.aosip.aosipUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.List;

public class TileCategory extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    
    private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
    private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
    private static final String PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";
    private static final String PREF_COLUMNS = "qs_layout_columns";
    private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
    private static final String PREF_ROWS_LANDSCAPE = "qs_rows_landscape";
    private static final String CATEGORY_WEATHER = "weather_category";
    private static final String WEATHER_ICON_PACK = "weather_icon_pack";
    private static final String DEFAULT_WEATHER_ICON_PACKAGE = "org.omnirom.omnijaws";
    private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";
    private static final String CHRONUS_ICON_PACK_INTENT = "com.dvtonder.chronus.ICON_PACK";

    private ListPreference mTileAnimationStyle;
    private ListPreference mTileAnimationDuration;
    private ListPreference mTileAnimationInterpolator;
    private ListPreference mWeatherIconPack;
    private CustomSeekBarPreference mQsColumns;
    private CustomSeekBarPreference mRowsPortrait;
    private CustomSeekBarPreference mRowsLandscape;
    private PreferenceCategory mWeatherCategory;

    private final Configuration mCurConfig = new Configuration();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.aosip_tiles);
        PreferenceScreen prefSet = getPreferenceScreen();

        ContentResolver resolver = getActivity().getContentResolver();

        int defaultValue;

        // Tile Animations
        mTileAnimationStyle = (ListPreference) findPreference(PREF_TILE_ANIM_STYLE);
        int tileAnimationStyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_STYLE, 0,
                UserHandle.USER_CURRENT);
        mTileAnimationStyle.setValue(String.valueOf(tileAnimationStyle));
        updateTileAnimationStyleSummary(tileAnimationStyle);
        updateAnimTileStyle(tileAnimationStyle);
        mTileAnimationStyle.setOnPreferenceChangeListener(this);

        mTileAnimationDuration = (ListPreference) findPreference(PREF_TILE_ANIM_DURATION);
        int tileAnimationDuration = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_DURATION, 2000,
                UserHandle.USER_CURRENT);
        mTileAnimationDuration.setValue(String.valueOf(tileAnimationDuration));
        updateTileAnimationDurationSummary(tileAnimationDuration);
        mTileAnimationDuration.setOnPreferenceChangeListener(this);

        mTileAnimationInterpolator = (ListPreference) findPreference(PREF_TILE_ANIM_INTERPOLATOR);
        int tileAnimationInterpolator = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_INTERPOLATOR, 0,
                UserHandle.USER_CURRENT);
        mTileAnimationInterpolator.setValue(String.valueOf(tileAnimationInterpolator));
        updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
        mTileAnimationInterpolator.setOnPreferenceChangeListener(this);

        mQsColumns = (CustomSeekBarPreference) findPreference(PREF_COLUMNS);
        int columnsQs = Settings.System.getInt(resolver,
                Settings.System.QS_LAYOUT_COLUMNS, 3);
        mQsColumns.setValue(columnsQs / 1);
        mQsColumns.setOnPreferenceChangeListener(this);

        mRowsPortrait = (CustomSeekBarPreference) findPreference(PREF_ROWS_PORTRAIT);
        int rowsPortrait = Settings.System.getInt(resolver,
                Settings.System.QS_ROWS_PORTRAIT, 3);
        mRowsPortrait.setValue(rowsPortrait / 1);
        mRowsPortrait.setOnPreferenceChangeListener(this);

        defaultValue = getResources().getInteger(com.android.internal.R.integer.config_qs_num_rows_landscape_default);
        mRowsLandscape = (CustomSeekBarPreference) findPreference(PREF_ROWS_LANDSCAPE);
        int rowsLandscape = Settings.System.getInt(resolver,
                Settings.System.QS_ROWS_LANDSCAPE, defaultValue);
        mRowsLandscape.setValue(rowsLandscape / 1);
        mRowsLandscape.setOnPreferenceChangeListener(this);

        mWeatherCategory = (PreferenceCategory) findPreference(CATEGORY_WEATHER);
        if (mWeatherCategory != null && !isOmniJawsServiceInstalled()) {
            prefSet.removePreference(mWeatherCategory);
        } else {
            String settingJawsPackage = Settings.System.getString(getContentResolver(),
                    Settings.System.OMNIJAWS_WEATHER_ICON_PACK);
            if (settingJawsPackage == null) {
                settingJawsPackage = DEFAULT_WEATHER_ICON_PACKAGE;
            }
            mWeatherIconPack = (ListPreference) findPreference(WEATHER_ICON_PACK);

            List<String> jawsentries = new ArrayList<String>();
            List<String> jawsvalues = new ArrayList<String>();
            getAvailableWeatherIconPacks(jawsentries, jawsvalues);
            mWeatherIconPack.setEntries(jawsentries.toArray(new String[jawsentries.size()]));
            mWeatherIconPack.setEntryValues(jawsvalues.toArray(new String[jawsvalues.size()]));

            int jawsvalueIndex = mWeatherIconPack.findIndexOfValue(settingJawsPackage);
            if (jawsvalueIndex == -1) {
                // no longer found
                settingJawsPackage = DEFAULT_WEATHER_ICON_PACKAGE;
                Settings.System.putString(getContentResolver(),
                        Settings.System.OMNIJAWS_WEATHER_ICON_PACK, settingJawsPackage);
                jawsvalueIndex = mWeatherIconPack.findIndexOfValue(settingJawsPackage);
            }
            mWeatherIconPack.setValueIndex(jawsvalueIndex >= 0 ? jawsvalueIndex : 0);
            mWeatherIconPack.setSummary(mWeatherIconPack.getEntry());
            mWeatherIconPack.setOnPreferenceChangeListener(this);
        }
    }


    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.OWLSNEST;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        final String key = preference.getKey();
        if (preference == mTileAnimationStyle) {
            int tileAnimationStyle = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.ANIM_TILE_STYLE,
                    tileAnimationStyle, UserHandle.USER_CURRENT);
            updateTileAnimationStyleSummary(tileAnimationStyle);
            updateAnimTileStyle(tileAnimationStyle);
            return true;
        } else if (preference == mTileAnimationDuration) {
            int tileAnimationDuration = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.ANIM_TILE_DURATION,
                    tileAnimationDuration, UserHandle.USER_CURRENT);
            updateTileAnimationDurationSummary(tileAnimationDuration);
            return true;
        } else if (preference == mTileAnimationInterpolator) {
            int tileAnimationInterpolator = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.ANIM_TILE_INTERPOLATOR,
                    tileAnimationInterpolator, UserHandle.USER_CURRENT);
            updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
            return true;
        } else if (preference == mQsColumns) {
            int qsColumns = (Integer) newValue;
            Settings.System.putInt(resolver, Settings.System.QS_LAYOUT_COLUMNS, qsColumns * 1);
            return true;
        } else if (preference == mRowsPortrait) {
            int rowsPortrait = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.QS_ROWS_PORTRAIT, rowsPortrait * 1);
            return true;
        } else if (preference == mRowsLandscape) {
            int rowsLandscape = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.QS_ROWS_LANDSCAPE, rowsLandscape * 1);
            return true;
        } else if (preference == mWeatherIconPack) {
            String value = (String) newValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.OMNIJAWS_WEATHER_ICON_PACK, value);
            int valueIndex = mWeatherIconPack.findIndexOfValue(value);
            mWeatherIconPack.setSummary(mWeatherIconPack.getEntries()[valueIndex]);
            return true;
      }
      return false;
    }

    private boolean isOmniJawsServiceInstalled() {
        return aosipUtils.isAvailableApp(WEATHER_SERVICE_PACKAGE, getActivity());
    }

    private void updateTileAnimationStyleSummary(int tileAnimationStyle) {
        String prefix = (String) mTileAnimationStyle.getEntries()[mTileAnimationStyle.findIndexOfValue(String
                .valueOf(tileAnimationStyle))];
        mTileAnimationStyle.setSummary(getResources().getString(R.string.qs_set_animation_style, prefix));
    }

    private void updateTileAnimationDurationSummary(int tileAnimationDuration) {
        String prefix = (String) mTileAnimationDuration.getEntries()[mTileAnimationDuration.findIndexOfValue(String
                .valueOf(tileAnimationDuration))];
        mTileAnimationDuration.setSummary(getResources().getString(R.string.qs_set_animation_duration, prefix));
    }

    private void updateTileAnimationInterpolatorSummary(int tileAnimationInterpolator) {
        String prefix = (String) mTileAnimationInterpolator.getEntries()[mTileAnimationInterpolator.findIndexOfValue(String
                .valueOf(tileAnimationInterpolator))];
        mTileAnimationInterpolator.setSummary(getResources().getString(R.string.qs_set_animation_interpolator, prefix));
    }

    private void updateAnimTileStyle(int tileAnimationStyle) {
        if (mTileAnimationDuration != null) {
            if (tileAnimationStyle == 0) {
                mTileAnimationDuration.setSelectable(false);
                mTileAnimationInterpolator.setSelectable(false);
            } else {
                mTileAnimationDuration.setSelectable(true);
                mTileAnimationInterpolator.setSelectable(true);
            }
        }
    }

    private void getAvailableWeatherIconPacks(List<String> entries, List<String> values) {
        Intent i = new Intent();
        PackageManager packageManager = getPackageManager();
        i.setAction("org.omnirom.WeatherIconPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                values.add(0, r.activityInfo.name);
            } else {
                values.add(r.activityInfo.name);
            }
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                entries.add(0, label);
            } else {
                entries.add(label);
            }
        }
        i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(CHRONUS_ICON_PACK_INTENT);
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            values.add(packageName + ".weather");
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            entries.add(label);
        }
    }
}


