/*
 * Copyright (C) 2015-2020 Android Open Source Illusion Project
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

package com.aosip.owlsnest.buttons;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.widget.LockPatternUtils;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class ButtonsHolder extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String KEY_LOCKDOWN_IN_POWER_MENU = "lockdown_in_power_menu";
    private static final String SCREEN_OFF_ANIMATION = "screen_off_animation";
    private static final int MY_USER_ID = UserHandle.myUserId();

    private SwitchPreference mPowerMenuLockDown;
    private ListPreference mScreenOffAnimation;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OWLSNEST;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.buttons);
        final PreferenceScreen prefSet = getPreferenceScreen();
        final LockPatternUtils lockPatternUtils = new LockPatternUtils(getActivity());

        mPowerMenuLockDown = (SwitchPreference) findPreference(KEY_LOCKDOWN_IN_POWER_MENU);
        if (lockPatternUtils.isSecure(MY_USER_ID)) {
            mPowerMenuLockDown.setChecked((Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCKDOWN_IN_POWER_MENU, 0) == 1));
            mPowerMenuLockDown.setOnPreferenceChangeListener(this);
        } else {
            prefSet.removePreference(mPowerMenuLockDown);
        }

        // Screen Off Animations
        mScreenOffAnimation = (ListPreference) findPreference(SCREEN_OFF_ANIMATION);
        int screenOffStyle = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_OFF_ANIMATION, 0);
        mScreenOffAnimation.setValue(String.valueOf(screenOffStyle));
        mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntry());
        mScreenOffAnimation.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPowerMenuLockDown) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.LOCKDOWN_IN_POWER_MENU, value ? 1 : 0);
            return true;
        } else if (preference == mScreenOffAnimation) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_ANIMATION, Integer.valueOf((String) newValue));
            int valueIndex = mScreenOffAnimation.findIndexOfValue((String) newValue);
            mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntries()[valueIndex]);
            return true;
        }
        return false;
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
                    sir.xmlResId = R.xml.buttons;
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

