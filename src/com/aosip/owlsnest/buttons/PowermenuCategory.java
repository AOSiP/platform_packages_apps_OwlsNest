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

package com.aosip.owlsnest.buttons;

import android.content.ContentResolver;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.internal.utils.du.DUActionUtils;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PowermenuCategory extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String SCREEN_OFF_ANIMATION = "screen_off_animation";
    private static final String TORCH_POWER_BUTTON_GESTURE = "torch_power_button_gesture";
    private static final String KEY_LOCKSCREEN_CLOCK_SELECTION = "lockscreen_clock_selection";
    private static final String KEY_LOCKSCREEN_DATE_SELECTION = "lockscreen_date_selection";

    private ListPreference mScreenOffAnimation;
    private ListPreference mTorchPowerButton;
    private ListPreference mLockscreenClockSelection;
    private ListPreference mLockscreenDateSelection;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OWLSNEST;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.powermenu);
        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mScreenOffAnimation = (ListPreference) findPreference(SCREEN_OFF_ANIMATION);
        int screenOffStyle = Settings.System.getInt(resolver,
                Settings.System.SCREEN_OFF_ANIMATION, 0);
        mScreenOffAnimation.setValue(String.valueOf(screenOffStyle));
        mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntry());
        mScreenOffAnimation.setOnPreferenceChangeListener(this);

        if (!DUActionUtils.deviceSupportsFlashLight(getContext())) {
            Preference toRemove = prefScreen.findPreference(TORCH_POWER_BUTTON_GESTURE);
            if (toRemove != null) {
                prefScreen.removePreference(toRemove);
            }
        } else {
            mTorchPowerButton = (ListPreference) findPreference(TORCH_POWER_BUTTON_GESTURE);
            int mTorchPowerButtonValue = Settings.Secure.getInt(resolver,
                    Settings.Secure.TORCH_POWER_BUTTON_GESTURE, 0);
            mTorchPowerButton.setValue(Integer.toString(mTorchPowerButtonValue));
            mTorchPowerButton.setSummary(mTorchPowerButton.getEntry());
            mTorchPowerButton.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mScreenOffAnimation) {
            String value = (String) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.SCREEN_OFF_ANIMATION, Integer.valueOf(value));
            int valueIndex = mScreenOffAnimation.findIndexOfValue(value);
            mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntries()[valueIndex]);
            return true;
        } else if (preference == mTorchPowerButton) {
            int mTorchPowerButtonValue = Integer.valueOf((String) newValue);
            int index = mTorchPowerButton.findIndexOfValue((String) newValue);
            mTorchPowerButton.setSummary(
                    mTorchPowerButton.getEntries()[index]);
            Settings.Secure.putInt(resolver, Settings.Secure.TORCH_POWER_BUTTON_GESTURE,
                    mTorchPowerButtonValue);
            if (mTorchPowerButtonValue == 1) {
                //if doubletap for torch is enabled, switch off double tap for camera
                Settings.Secure.putInt(resolver, Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
                        1);
            }
            return true;
        }
        return false;
    }
}

