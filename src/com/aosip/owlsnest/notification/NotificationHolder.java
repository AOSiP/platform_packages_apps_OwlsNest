/*
 *  Copyright (C) 2015-2020 Android Open Source Illusion Project
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

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.aosip.support.preference.GlobalSettingMasterSwitchPreference;
import com.aosip.support.preference.SystemSettingMasterSwitchPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class NotificationHolder extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String PREF_HEADS_UP = "heads_up";
    private static final String PREF_TICKER = "ticker";

    private GlobalSettingMasterSwitchPreference mHeadsUp;
    private SystemSettingMasterSwitchPreference mTicker;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OWLSNEST;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notification);

        mHeadsUp = (GlobalSettingMasterSwitchPreference)
                findPreference(PREF_HEADS_UP);
        mHeadsUp.setChecked(Settings.Global.getInt(getContentResolver(),
                Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 1) == 1);
        mHeadsUp.setOnPreferenceChangeListener(this);

        mTicker = (SystemSettingMasterSwitchPreference)
                findPreference(PREF_TICKER);
        mTicker.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_TICKER, 0) == 1);
        mTicker.setOnPreferenceChangeListener(this);
      }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHeadsUp) {
            Boolean value = (Boolean) newValue;
            Settings.Global.putInt(getContentResolver(),
                    Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, value ? 1 : 0);
            isHeadsUpEnabledCheck();
            return true;
        } else if (preference == mTicker) {
            Boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_TICKER, value ? 1 : 0);
            isHeadsUpEnabledCheck();
            return true;
        }
       return false;
    }

    private void isHeadsUpEnabledCheck() {
        boolean headsupEnabled = Settings.Global.getInt(getContentResolver(),
                Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 1) == 1;
        boolean tickerEnabled = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_TICKER, 1) == 1;

        if (headsupEnabled) {
            mTicker.setChecked(false);
        }

        if (tickerEnabled) {
            mHeadsUp.setChecked(false);
        }
    }

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
                ArrayList<SearchIndexableResource> result =
                    new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.notification;
                    result.add(sir);
                    return result;
            }

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                List<String> keys = super.getNonIndexableKeys(context);
                return keys;
            }
        };
}

