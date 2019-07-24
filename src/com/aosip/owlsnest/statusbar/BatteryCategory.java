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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.statusbar.IStatusBarService;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BatteryCategory extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String BATTERY_ESTIMATE_POSITION_TYPE = "battery_estimate_position";
    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 3;
    private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 4;

    private ListPreference mStatusBarBatteryShowPercent;
    private ListPreference mStatusBarBattery;
    private ListPreference mEstimatePositionType;

    private IStatusBarService mStatusBarService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.battery_settings);

        final ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarBatteryShowPercent =
                 (ListPreference) findPreference(SHOW_BATTERY_PERCENT);

        int batteryShowPercent = Settings.System.getInt(resolver,
                Settings.System.SHOW_BATTERY_PERCENT, 0);
        mStatusBarBatteryShowPercent = (ListPreference) findPreference(SHOW_BATTERY_PERCENT);
        mStatusBarBatteryShowPercent.setValue(String.valueOf(batteryShowPercent));
        mStatusBarBatteryShowPercent.setSummary(mStatusBarBatteryShowPercent.getEntry());
        mStatusBarBatteryShowPercent.setOnPreferenceChangeListener(this);

        mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);

        int batteryStyle = Settings.Secure.getInt(resolver,
                Settings.Secure.STATUS_BAR_BATTERY_STYLE, 0);
        mStatusBarBattery.setValue(String.valueOf(batteryStyle));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        enableStatusBarBatteryDependents(batteryStyle);
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        // battery estimate position
        mEstimatePositionType = (ListPreference) findPreference(BATTERY_ESTIMATE_POSITION_TYPE);
        int type = Settings.System.getInt(resolver,
                Settings.System.BATTERY_ESTIMATE_POSITION, 0);
        mEstimatePositionType.setValue(String.valueOf(type));
        mEstimatePositionType.setSummary(mEstimatePositionType.getEntry());
        mEstimatePositionType.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarBatteryShowPercent) {
            int batteryShowPercent = Integer.valueOf((String) objValue);
            int index = mStatusBarBatteryShowPercent.findIndexOfValue((String) objValue);
            Settings.System.putInt(
                    resolver, Settings.System.SHOW_BATTERY_PERCENT, batteryShowPercent);
            mStatusBarBatteryShowPercent.setSummary(
                    mStatusBarBatteryShowPercent.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarBattery) {
            int batteryStyle = Integer.valueOf((String) objValue);
            int index = mStatusBarBattery.findIndexOfValue((String) objValue);
            Settings.Secure.putInt(resolver,
                    Settings.Secure.STATUS_BAR_BATTERY_STYLE, batteryStyle);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
            enableStatusBarBatteryDependents(batteryStyle);
            return true;
        } else if (preference == mEstimatePositionType) {
            int type = Integer.valueOf((String) objValue);
            int index = mEstimatePositionType.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.BATTERY_ESTIMATE_POSITION, type);
            mEstimatePositionType.setSummary(mEstimatePositionType.getEntries()[index]);
            IStatusBarService statusBarService = IStatusBarService.Stub.asInterface(ServiceManager.checkService(Context.STATUS_BAR_SERVICE));
            if (statusBarService != null) {
                try {
                    statusBarService.restartUI();
                } catch (RemoteException e) {
                    // do nothing.
                }
            }
            return true;
        }
        return false;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        if (batteryIconStyle == STATUS_BAR_BATTERY_STYLE_TEXT
                || batteryIconStyle == STATUS_BAR_BATTERY_STYLE_HIDDEN) {
            mStatusBarBatteryShowPercent.setEnabled(false);
        } else {
            mStatusBarBatteryShowPercent.setEnabled(true);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OWLSNEST;
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
                    sir.xmlResId = R.xml.battery_settings;
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
