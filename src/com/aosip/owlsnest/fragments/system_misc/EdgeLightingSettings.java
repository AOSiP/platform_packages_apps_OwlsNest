/*
 * Copyright (C) 2021 AOSiP
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

package com.aosip.owlsnest.fragments.system_misc;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBarController;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.aosip.owlsnest.fragments.system_misc.EdgeLightingEnabler;
import com.aosip.support.colorpicker.ColorPickerPreference;
import com.aosip.support.preference.SystemSettingListPreference;
import com.aosip.support.preference.CustomSeekBarPreference;
import com.aosip.support.preference.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class EdgeLightingSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, EdgeLightingEnabler.OnEdgeLightingChangeListener,
        Indexable {

    private static final String KEY_EDGE_LIGHT = "pulse_ambient_light";

    private ColorPickerPreference mEdgeLightColorPreference;
    private CustomSeekBarPreference mEdgeLightDurationPreference;
    private CustomSeekBarPreference mEdgeLightRepeatCountPreference;
    private SystemSettingSwitchPreference mAmbientNotificationLightEnabled;
    private SystemSettingSwitchPreference mAmbientNotificationLightHideAod;
    private SystemSettingListPreference mColorMode;
    private SystemSettingListPreference mAmbientNotificationLightTimeout;

    private EdgeLightingEnabler mEdgeLightingEnabler;

    private boolean enabled;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.edge_notifications);

        mAmbientNotificationLightEnabled = (SystemSettingSwitchPreference) findPreference("ambient_notification_light_enabled");
        mAmbientNotificationLightHideAod = (SystemSettingSwitchPreference) findPreference("ambient_notification_light_hide_aod");
        mAmbientNotificationLightTimeout = (SystemSettingListPreference) findPreference("ambient_notification_light_timeout");
        mEdgeLightRepeatCountPreference = (CustomSeekBarPreference) findPreference("ambient_light_repeat_count");
        mEdgeLightDurationPreference = (CustomSeekBarPreference) findPreference("ambient_light_duration");
        mColorMode = (SystemSettingListPreference) findPreference("ambient_notification_light_color_mode");
        mEdgeLightColorPreference = (ColorPickerPreference) findPreference("ambient_notification_light_color");

        int rCount = Settings.System.getInt(getContentResolver(),
                Settings.System.AMBIENT_LIGHT_REPEAT_COUNT, 0);
        mEdgeLightRepeatCountPreference.setValue(rCount);
        mEdgeLightRepeatCountPreference.setOnPreferenceChangeListener(this);

        int duration = Settings.System.getInt(getContentResolver(),
                Settings.System.AMBIENT_LIGHT_DURATION, 2);
        mEdgeLightDurationPreference.setValue(duration);
        mEdgeLightDurationPreference.setOnPreferenceChangeListener(this);

        int value;
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

        int edgeLightColor = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_COLOR, 0xFF3980FF);
        mEdgeLightColorPreference.setNewPreviewColor(edgeLightColor);
        mEdgeLightColorPreference.setAlphaSliderEnabled(enabled ? true : false);
        String edgeLightColorHex = String.format("#%08x", (0xFF3980FF & edgeLightColor));
        if (edgeLightColorHex.equals("#ff3980ff")) {
            mEdgeLightColorPreference.setSummary(R.string.color_default);
        } else {
            mEdgeLightColorPreference.setSummary(edgeLightColorHex);
        }
        mEdgeLightColorPreference.setOnPreferenceChangeListener(this);
        refreshPreferenceStates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEdgeLightingEnabler.teardownSwitchController();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final SettingsActivity activity = (SettingsActivity) getActivity();
        final SwitchBar switchBar = activity.getSwitchBar();
        mEdgeLightingEnabler = new EdgeLightingEnabler(getContext(),
                new SwitchBarController(switchBar), this, getSettingsLifecycle());
    }

    @Override                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mEdgeLightColorPreference) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ff3980ff")) {
                preference.setSummary(R.string.color_default);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_COLOR, intHex);
            return true;
        } else if (preference == mEdgeLightRepeatCountPreference) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.AMBIENT_LIGHT_REPEAT_COUNT, value);
            return true;
        } else if (preference == mEdgeLightDurationPreference) {
            int value = (Integer) newValue;
                Settings.System.putInt(getContentResolver(),
                    Settings.System.AMBIENT_LIGHT_DURATION, value);
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
            refreshPreferenceStates();
            return true;
        }
        return false;
    }

    @Override
    public void onChanged(boolean enabled) {
        refreshPreferenceStates();
    }

    private void refreshPreferenceStates() {
        enabled = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.NOTIFICATION_PULSE, 0, UserHandle.USER_CURRENT) == 1;
        mEdgeLightColorPreference.setEnabled(enabled ? true : false);
        mEdgeLightDurationPreference.setEnabled(enabled ? true : false);
        mEdgeLightRepeatCountPreference.setEnabled(enabled ? true : false);
        mAmbientNotificationLightEnabled.setEnabled(enabled ? true : false);
        mColorMode.setEnabled(enabled ? true : false);
        mAmbientNotificationLightHideAod.setEnabled(enabled ? true : false);
        mAmbientNotificationLightTimeout.setEnabled(enabled ? true : false);
        mAmbientNotificationLightHideAod.setEnabled(enabled ? true : false);
        mAmbientNotificationLightTimeout.setEnabled(enabled ? true : false);
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                    boolean enabled) {
                final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                final SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.edge_notifications;
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
