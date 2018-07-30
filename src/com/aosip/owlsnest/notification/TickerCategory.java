/*
 * Copyright (C) 2015-2018 Android Open Source Illusion Project
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
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class TickerCategory extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private static final String TAG = "TickerCategory";

    private ListPreference mTickerAnimation;
    private ListPreference mTickerMode;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OWLSNEST;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.ticker);

        mTickerMode = (ListPreference) findPreference("ticker_mode");
        mTickerMode.setOnPreferenceChangeListener(this);
        int tickerMode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_TICKER,
                0, UserHandle.USER_CURRENT);
        mTickerMode.setValue(String.valueOf(tickerMode));
        mTickerMode.setSummary(mTickerMode.getEntry());

        mTickerAnimation = (ListPreference) findPreference("status_bar_ticker_animation_mode");
        mTickerAnimation.setOnPreferenceChangeListener(this);
        int tickerAnimationMode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_TICKER_ANIMATION_MODE,
                1, UserHandle.USER_CURRENT);
        mTickerAnimation.setValue(String.valueOf(tickerAnimationMode));
        mTickerAnimation.setSummary(mTickerAnimation.getEntry());
      }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference.equals(mTickerMode)) {
            int tickerMode = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_TICKER, tickerMode, UserHandle.USER_CURRENT);
            int index = mTickerMode.findIndexOfValue((String) newValue);
            mTickerMode.setSummary(
                    mTickerMode.getEntries()[index]);
            return true;
        } else if (preference.equals(mTickerAnimation)) {
            int tickerAnimationMode = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_TICKER_ANIMATION_MODE, tickerAnimationMode, UserHandle.USER_CURRENT);
            int index = mTickerAnimation.findIndexOfValue((String) newValue);
            mTickerAnimation.setSummary(
                    mTickerAnimation.getEntries()[index]);
            return true;
        }
       return false;
    }
}

