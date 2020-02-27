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
import android.graphics.Color;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.Utils;

import com.aosip.support.preference.ColorSelectPreference;
import com.aosip.support.preference.GlobalSettingMasterSwitchPreference;
import com.aosip.support.preference.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class NotificationHolder extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String PULSE_AMBIANT_LIGHT_PREF = "pulse_ambient_light";
    private static final String PULSE_COLOR_PREF = "ambient_notification_light_color";
    private static final String AMBIENT_NOTIFICATION_LIGHT_ACCENT_PREF = "ambient_notification_light_accent";
    private static final String PULSE_TIMEOUT_PREF = "ambient_notification_light_timeout";
    private static final String PULSE_COLOR_MODE_PREF = "ambient_notification_light_color_mode";
    private static final String PREF_HEADS_UP = "heads_up";
    private static final String STATUS_BAR_TICKER = "status_bar_show_ticker";
    private static final String ALERT_SLIDER_PREF = "alert_slider_notifications";

    private ColorSelectPreference mPulseLightColorPref;
    private GlobalSettingMasterSwitchPreference mHeadsUp;
    private ListPreference mColorMode;
    private ListPreference mPulseTimeout;
    private Preference mBatteryLightPref;
    private Preference mAlertSlider;
    private SwitchPreference mTicker;
    private SystemSettingSwitchPreference mPulseEdgeLights;
    private static final int MENU_RESET = Menu.FIRST;
    private int mDefaultColor;
    private int mColor;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OWLSNEST;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notification);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = getResources();

        mDefaultColor = getResources().getInteger(
                com.android.internal.R.integer.config_ambientNotificationDefaultColor);
        mPulseEdgeLights = (SystemSettingSwitchPreference) findPreference(PULSE_AMBIANT_LIGHT_PREF);
        boolean mPulseNotificationEnabled = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.DOZE_ENABLED, 0) != 0;
        mPulseEdgeLights.setEnabled(mPulseNotificationEnabled);

        setHasOptionsMenu(true);

        mPulseLightColorPref = (ColorSelectPreference) findPreference(PULSE_COLOR_PREF);
        mColor = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_COLOR, mDefaultColor);
        mPulseLightColorPref.setColor(mColor);
        mPulseLightColorPref.setOnPreferenceChangeListener(this);

        mPulseTimeout = (ListPreference) findPreference(PULSE_TIMEOUT_PREF);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.AOD_NOTIFICATION_PULSE_TIMEOUT, 0);

        mPulseTimeout.setValue(Integer.toString(value));
        mPulseTimeout.setSummary(mPulseTimeout.getEntry());
        mPulseTimeout.setOnPreferenceChangeListener(this);

        mColorMode = (ListPreference) findPreference(PULSE_COLOR_MODE_PREF);
        boolean colorModeAutomatic = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_COLOR_AUTOMATIC, 0) != 0;
        boolean colorModeAccent = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_ACCENT, 0) != 0;
        if (colorModeAutomatic) {
            value = 0;
        } else if (colorModeAccent) {
            value = 1;
        } else {
            value = 2;
       }

        mColorMode.setValue(Integer.toString(value));
        mColorMode.setSummary(mColorMode.getEntry());
        mColorMode.setOnPreferenceChangeListener(this);

        mBatteryLightPref = (Preference) findPreference("charging_light");
        PreferenceScreen prefSet = getPreferenceScreen();
        if (!getResources().getBoolean(
                com.android.internal.R.bool.config_deviceHasLED)) {
            prefSet.removePreference(mBatteryLightPref);
        }

        mHeadsUp = (GlobalSettingMasterSwitchPreference)
                findPreference(PREF_HEADS_UP);
        mHeadsUp.setChecked(Settings.Global.getInt(getContentResolver(),
                Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 1) == 1);
        mHeadsUp.setOnPreferenceChangeListener(this);

        mTicker = (SystemSettingSwitchPreference) findPreference(STATUS_BAR_TICKER);
        mTicker.setChecked((Settings.System.getInt(getContentResolver(),
             Settings.System.STATUS_BAR_SHOW_TICKER, 0) == 1));
        mTicker.setOnPreferenceChangeListener(this);

        mAlertSlider = (Preference) prefScreen.findPreference(ALERT_SLIDER_PREF);
        boolean mAlertSliderAvailable = res.getBoolean(
                com.android.internal.R.bool.config_hasAlertSlider);
        if (!mAlertSliderAvailable)
            prefScreen.removePreference(mAlertSlider);
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
        if  (preference == mTicker) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_TICKER, value ? 1 : 0);
            isHeadsUpEnabledCheck();
            return true;
        } else if (preference == mPulseLightColorPref) {
            ColorSelectPreference lightPref = (ColorSelectPreference) preference;
            Settings.System.putInt(getContentResolver(),
                     Settings.System.NOTIFICATION_PULSE_COLOR, lightPref.getColor());
            mColor = lightPref.getColor();
            mPulseLightColorPref.setColor(mColor);
            return true;
        } else if (preference == mPulseTimeout) {
            int value = Integer.valueOf((String) newValue);
            int index = mPulseTimeout.findIndexOfValue((String) newValue);
            mPulseTimeout.setSummary(mPulseTimeout.getEntries()[index]);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.AOD_NOTIFICATION_PULSE_TIMEOUT, value);
            return true;
        } else if (preference == mColorMode) {
            int value = Integer.valueOf((String) newValue);
            int index = mColorMode.findIndexOfValue((String) newValue);
            mColorMode.setSummary(mColorMode.getEntries()[index]);
            if (value == 0) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PULSE_COLOR_AUTOMATIC, 1);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PULSE_ACCENT, 0);
            } else if (value == 1) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PULSE_COLOR_AUTOMATIC, 0);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PULSE_ACCENT, 1);
            } else {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PULSE_COLOR_AUTOMATIC, 0);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PULSE_ACCENT, 0);
            }
            return true;
        } else if (preference == mHeadsUp) {
            Boolean value = (Boolean) newValue;
            Settings.Global.putInt(getContentResolver(),
                    Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, value ? 1 : 0);
            isHeadsUpEnabledCheck();
            return true;
        }
       return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_backup_restore)
                .setAlphabeticShortcut('r')
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefaults();
                return true;
        }
        return false;
    }

    protected void resetToDefaults() {
        Settings.System.putInt(getContentResolver(), Settings.System.NOTIFICATION_PULSE_COLOR,
                mDefaultColor);
        mPulseLightColorPref.setColor(mDefaultColor);
    }

    private void refreshView() {
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    private void isHeadsUpEnabledCheck() {
        boolean headsupEnabled = Settings.Global.getInt(getContentResolver(),
                Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 1) == 1;

        if (headsupEnabled) {
            mTicker.setEnabled(false);
            mTicker.setChecked(false);
        } else {
            mTicker.setEnabled(true);
            mTicker.setChecked(true);
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
                    final Resources res = context.getResources();
                    boolean mAlertSliderAvailable = res.getBoolean(
                            com.android.internal.R.bool.config_hasAlertSlider);
                    if (!mAlertSliderAvailable)
                        keys.add(ALERT_SLIDER_PREF);
                return keys;
            }
        };
}

