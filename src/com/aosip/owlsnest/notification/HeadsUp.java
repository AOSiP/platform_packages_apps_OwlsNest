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

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.aosip.owlsnest.preference.AppMultiSelectListPreference;
import com.aosip.owlsnest.preference.ScrollAppsViewPreference;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SearchIndexable
public class HeadsUp extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String PREF_ADD_STOPLIST_PACKAGES = "add_stoplist_packages";
    private static final String PREF_ADD_BLACKLIST_PACKAGES = "add_blacklist_packages";
    private static final String PREF_BLACKLIST_APPS_LIST_SCROLLER = "blacklist_apps_list_scroller";
    private static final String PREF_HEADS_UP_SNOOZE_TIME = "heads_up_snooze_time";
    private static final String PREF_HEADS_UP_TIME_OUT = "heads_up_time_out";
    private static final String PREF_STOPLIST_APPS_LIST_SCROLLER = "stoplist_apps_list_scroller";

    private AppMultiSelectListPreference mAddStoplistPref;
    private AppMultiSelectListPreference mAddBlacklistPref;
    private ListPreference mHeadsUpSnoozeTime;
    private ListPreference mHeadsUpTimeOut;
    private ScrollAppsViewPreference mStoplistScroller;
    private ScrollAppsViewPreference mBlacklistScroller;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OWLSNEST;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.headsup);
        final ContentResolver resolver = getActivity().getContentResolver();

        Resources systemUiResources;
        try {
            systemUiResources = getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (Exception e) {
            return;
        }

        int defaultSnooze = systemUiResources.getInteger(systemUiResources.getIdentifier(
                    "com.android.systemui:integer/heads_up_default_snooze_length_ms", null, null));
        mHeadsUpSnoozeTime = (ListPreference) findPreference(PREF_HEADS_UP_SNOOZE_TIME);
        mHeadsUpSnoozeTime.setOnPreferenceChangeListener(this);
        int headsUpSnooze = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_NOTIFICATION_SNOOZE, defaultSnooze);
        mHeadsUpSnoozeTime.setValue(String.valueOf(headsUpSnooze));
        updateHeadsUpSnoozeTimeSummary(headsUpSnooze);

        int defaultTimeOut = systemUiResources.getInteger(systemUiResources.getIdentifier(
                    "com.android.systemui:integer/heads_up_notification_decay", null, null));
        mHeadsUpTimeOut = (ListPreference) findPreference(PREF_HEADS_UP_TIME_OUT);
        mHeadsUpTimeOut.setOnPreferenceChangeListener(this);
        int headsUpTimeOut = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_TIMEOUT, defaultTimeOut);
        mHeadsUpTimeOut.setValue(String.valueOf(headsUpTimeOut));
        updateHeadsUpTimeOutSummary(headsUpTimeOut);

        mStoplistScroller = (ScrollAppsViewPreference) findPreference(PREF_STOPLIST_APPS_LIST_SCROLLER);
        mBlacklistScroller = (ScrollAppsViewPreference) findPreference(PREF_BLACKLIST_APPS_LIST_SCROLLER);

        mAddStoplistPref =  (AppMultiSelectListPreference) findPreference(PREF_ADD_STOPLIST_PACKAGES);
        mAddBlacklistPref = (AppMultiSelectListPreference) findPreference(PREF_ADD_BLACKLIST_PACKAGES);

        final String valuesStoplist = Settings.System.getString(resolver,
                Settings.System.HEADS_UP_STOPLIST_VALUES);
        if (!TextUtils.isEmpty(valuesStoplist)) {
            Collection<String> stopList = Arrays.asList(valuesStoplist.split(":"));
            mStoplistScroller.setVisible(true);
            mStoplistScroller.setValues(stopList);
            mAddStoplistPref.setValues(stopList);
        } else {
            mStoplistScroller.setVisible(false);
        }

        final String valuesBlacklist = Settings.System.getString(resolver,
                Settings.System.HEADS_UP_BLACKLIST_VALUES);
        if (!TextUtils.isEmpty(valuesBlacklist)) {
            Collection<String> blackList = Arrays.asList(valuesBlacklist.split(":"));
            mBlacklistScroller.setVisible(true);
            mBlacklistScroller.setValues(blackList);
            mAddBlacklistPref.setValues(blackList);
        } else {
            mBlacklistScroller.setVisible(false);
        }

        mAddStoplistPref.setOnPreferenceChangeListener(this);
        mAddBlacklistPref.setOnPreferenceChangeListener(this);
      }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHeadsUpSnoozeTime) {
            int headsUpSnooze = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_NOTIFICATION_SNOOZE,
                    headsUpSnooze);
            updateHeadsUpSnoozeTimeSummary(headsUpSnooze);
            return true;
        } else if (preference == mHeadsUpTimeOut) {
            int headsUpTimeOut = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_TIMEOUT, headsUpTimeOut);
            updateHeadsUpTimeOutSummary(headsUpTimeOut);
            return true;
        } else if (preference == mAddStoplistPref) {
            Collection<String> valueList = (Collection<String>) newValue;
            mStoplistScroller.setVisible(false);
            if (valueList != null) {
                Settings.System.putString(getContentResolver(),
                        Settings.System.HEADS_UP_STOPLIST_VALUES,
                        TextUtils.join(":", valueList));
                mStoplistScroller.setVisible(true);
                mStoplistScroller.setValues(valueList);
            } else {
                Settings.System.putString(getContentResolver(),
                        Settings.System.HEADS_UP_STOPLIST_VALUES, "");
            }
            return true;
        } else if (preference == mAddBlacklistPref) {
            Collection<String> valueList = (Collection<String>) newValue;
            mBlacklistScroller.setVisible(false);
            if (valueList != null) {
                Settings.System.putString(getContentResolver(),
                        Settings.System.HEADS_UP_BLACKLIST_VALUES,
                        TextUtils.join(":", valueList));
                mBlacklistScroller.setVisible(true);
                mBlacklistScroller.setValues(valueList);
            } else {
                Settings.System.putString(getContentResolver(),
                        Settings.System.HEADS_UP_BLACKLIST_VALUES, "");
            }
            return true;
        }
       return false;
    }

    private void updateHeadsUpSnoozeTimeSummary(int value) {
        if (value == 0) {
            mHeadsUpSnoozeTime.setSummary(getResources().getString(R.string.heads_up_snooze_disabled_summary));
        } else if (value == 60000) {
            mHeadsUpSnoozeTime.setSummary(getResources().getString(R.string.heads_up_snooze_summary_one_minute));
        } else {
            String summary = getResources().getString(R.string.heads_up_snooze_summary, value / 60 / 1000);
            mHeadsUpSnoozeTime.setSummary(summary);
        }
    }

    private void updateHeadsUpTimeOutSummary(int value) {
        String summary = getResources().getString(R.string.heads_up_time_out_summary,
                value / 1000);
        mHeadsUpTimeOut.setSummary(summary);
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

