/*
 * Copyright (C) 2015-2019 Android Open Source Illusion Project
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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.internal.util.aosip.aosipUtils;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.aosip.support.preference.CustomSeekBarPreference;
import com.aosip.support.preference.SecureSettingIntListPreference;
import com.aosip.support.preference.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatusbarHolder extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String KEY_STATUS_BAR_LOGO = "status_bar_logo";
    private static final String CLOCK_POSITION = "status_bar_clock_position";
    private static final String PREF_AM_PM_STYLE = "status_bar_am_pm";
    private static final String PREF_CLOCK_DATE_DISPLAY = "clock_date_display";
    private static final String PREF_CLOCK_DATE_STYLE = "clock_date_style";
    private static final String PREF_CLOCK_DATE_FORMAT = "clock_date_format";
    private static final String PREF_STATUS_BAR_CLOCK = "status_bar_show_clock";
    private static final String PREF_CLOCK_DATE_POSITION = "clock_date_position";

    public static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
    public static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
    private static final int CUSTOM_CLOCK_DATE_FORMAT_INDEX = 18;

    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String STATUS_BAR_BATTERY_TEXT_CHARGING = "status_bar_battery_text_charging";
    private static final String BATTERY_PERCENTAGE_HIDDEN = "0";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";

    private static final int BATTERY_STYLE_Q = 0;
    private static final int BATTERY_STYLE_DOTTED_CIRCLE = 1;
    private static final int BATTERY_STYLE_CIRCLE = 2;
    private static final int BATTERY_STYLE_TEXT = 3;
    private static final int BATTERY_STYLE_HIDDEN = 4;

    private static final String NETWORK_TRAFFIC_FONT_SIZE  = "network_traffic_font_size";

    private static final String KEY_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String KEY_STATUS_BAR_SHOW_CARRIER = "status_bar_show_carrier";

    private SecureSettingIntListPreference mClockPosition;
    private ListPreference mClockAmPmStyle;
    private ListPreference mClockDateDisplay;
    private ListPreference mClockDateStyle;
    private ListPreference mClockDateFormat;
    private ListPreference mClockDatePosition;
    private SwitchPreference mStatusBarClock;

    private ListPreference mBatteryPercent;
    private ListPreference mBatteryStyle;
    private SwitchPreference mBatteryCharging;

    private ListPreference mNetTrafficLocation;
    private ListPreference mNetTrafficType;
    private CustomSeekBarPreference mNetTrafficSize;
    private CustomSeekBarPreference mThreshold;
    private SystemSettingSwitchPreference mShowArrows;

    private SwitchPreference mShowKronicLogo;
    private ListPreference mLogoStyle;

    private ListPreference mShowCarrierLabel;
    private Preference mCustomCarrierLabel;

    private String mCustomCarrierLabelText;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar);
        final ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();

        mShowKronicLogo = (SwitchPreference) findPreference(KEY_STATUS_BAR_LOGO);
        mShowKronicLogo.setChecked((Settings.System.getInt(getContentResolver(),
             Settings.System.STATUS_BAR_LOGO, 0) == 1));
        mShowKronicLogo.setOnPreferenceChangeListener(this);

        mLogoStyle = (ListPreference) findPreference("status_bar_logo_style");
        mLogoStyle.setOnPreferenceChangeListener(this);
        int logoStyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_LOGO_STYLE,
                0, UserHandle.USER_CURRENT);
        mLogoStyle.setValue(String.valueOf(logoStyle));
        mLogoStyle.setSummary(mLogoStyle.getEntry());

        mClockPosition = (SecureSettingIntListPreference) findPreference(CLOCK_POSITION);

        // Adjust status bar clock prefs for notched devices
        if (aosipUtils.hasNotch(getActivity())) {
            mClockPosition.setEntries(R.array.clock_position_entries_notch);
            mClockPosition.setEntryValues(R.array.clock_position_values_notch);
        } else {
            mClockPosition.setEntries(R.array.clock_position_entries);
            mClockPosition.setEntryValues(R.array.clock_position_values);
        }

        mClockAmPmStyle = (ListPreference) findPreference(PREF_AM_PM_STYLE);
        mClockAmPmStyle.setOnPreferenceChangeListener(this);
        mClockAmPmStyle.setValue(Integer.toString(Settings.Secure.getInt(getActivity()
                .getContentResolver(), Settings.Secure.STATUSBAR_CLOCK_AM_PM_STYLE,
                0)));
        boolean is24hour = DateFormat.is24HourFormat(getActivity());
        if (is24hour) {
            mClockAmPmStyle.setSummary(R.string.status_bar_am_pm_info);
        } else {
            mClockAmPmStyle.setSummary(mClockAmPmStyle.getEntry());
        }
        mClockAmPmStyle.setEnabled(!is24hour);

        mClockDateDisplay = (ListPreference) findPreference(PREF_CLOCK_DATE_DISPLAY);
        mClockDateDisplay.setOnPreferenceChangeListener(this);
        mClockDateDisplay.setValue(Integer.toString(Settings.Secure.getInt(getActivity()
                .getContentResolver(), Settings.Secure.STATUSBAR_CLOCK_DATE_DISPLAY,
                0)));
        mClockDateDisplay.setSummary(mClockDateDisplay.getEntry());

        mClockDateStyle = (ListPreference) findPreference(PREF_CLOCK_DATE_STYLE);
        mClockDateStyle.setOnPreferenceChangeListener(this);
        mClockDateStyle.setValue(Integer.toString(Settings.Secure.getInt(getActivity()
                .getContentResolver(), Settings.Secure.STATUSBAR_CLOCK_DATE_STYLE,
                0)));
        mClockDateStyle.setSummary(mClockDateStyle.getEntry());

        mClockDateFormat = (ListPreference) findPreference(PREF_CLOCK_DATE_FORMAT);
        mClockDateFormat.setOnPreferenceChangeListener(this);
        String value = Settings.Secure.getString(getActivity().getContentResolver(),
                Settings.Secure.STATUSBAR_CLOCK_DATE_FORMAT);
        if (value == null || value.isEmpty()) {
            value = "EEE";
        }
        int index = mClockDateFormat.findIndexOfValue((String) value);
        if (index == -1) {
            mClockDateFormat.setValueIndex(CUSTOM_CLOCK_DATE_FORMAT_INDEX);
        } else {
            mClockDateFormat.setValue(value);
        }

        parseClockDateFormats();

        mStatusBarClock = (SwitchPreference) findPreference(PREF_STATUS_BAR_CLOCK);
        mStatusBarClock.setChecked((Settings.Secure.getInt(
                getActivity().getApplicationContext().getContentResolver(),
                Settings.Secure.STATUS_BAR_CLOCK, 1) == 1));
        mStatusBarClock.setOnPreferenceChangeListener(this);

        mClockDatePosition = (ListPreference) findPreference(PREF_CLOCK_DATE_POSITION);
        mClockDatePosition.setOnPreferenceChangeListener(this);
        mClockDatePosition.setValue(Integer.toString(Settings.Secure.getInt(getActivity()
                .getContentResolver(), Settings.Secure.STATUSBAR_CLOCK_DATE_POSITION,
                0)));
        mClockDatePosition.setSummary(mClockDatePosition.getEntry());

         boolean mClockDateToggle = Settings.Secure.getInt(getActivity().getContentResolver(),
                    Settings.Secure.STATUSBAR_CLOCK_DATE_DISPLAY, 0) != 0;
        if (!mClockDateToggle) {
            mClockDateStyle.setEnabled(false);
            mClockDateFormat.setEnabled(false);
            mClockDatePosition.setEnabled(false);
        }

        mBatteryPercent = (ListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);
        mBatteryCharging = (SwitchPreference) findPreference(STATUS_BAR_BATTERY_TEXT_CHARGING);
        mBatteryStyle = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        int batterystyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_Q);
        mBatteryStyle.setOnPreferenceChangeListener(this);

        updateBatteryOptions(batterystyle);

        int NetTrafficSize = Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_FONT_SIZE, 42);
        mNetTrafficSize = (CustomSeekBarPreference) findPreference(NETWORK_TRAFFIC_FONT_SIZE);
        mNetTrafficSize.setValue(NetTrafficSize / 1);
        mNetTrafficSize.setOnPreferenceChangeListener(this);

        int type = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_TYPE, 0, UserHandle.USER_CURRENT);
        mNetTrafficType = (ListPreference) findPreference("network_traffic_type");
        mNetTrafficType.setValue(String.valueOf(type));
        mNetTrafficType.setSummary(mNetTrafficType.getEntry());
        mNetTrafficType.setOnPreferenceChangeListener(this);

        mNetTrafficLocation = (ListPreference) findPreference("network_traffic_location");
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_VIEW_LOCATION, 0, UserHandle.USER_CURRENT);
        mNetTrafficLocation.setOnPreferenceChangeListener(this);

        int trafvalue = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 1, UserHandle.USER_CURRENT);
        mThreshold = (CustomSeekBarPreference) findPreference("network_traffic_autohide_threshold");
        mThreshold.setValue(trafvalue);
        mThreshold.setOnPreferenceChangeListener(this);
        mShowArrows = (SystemSettingSwitchPreference) findPreference("network_traffic_arrow");

        int netMonitorEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_STATE, 0, UserHandle.USER_CURRENT);
        if (netMonitorEnabled == 1) {
            mNetTrafficLocation.setValue(String.valueOf(location+1));
            updateTrafficLocation(location+1);
        } else {
            mNetTrafficLocation.setValue("0");
            updateTrafficLocation(0); 
        }
        mNetTrafficLocation.setSummary(mNetTrafficLocation.getEntry());

        mShowCarrierLabel = (ListPreference) findPreference(KEY_STATUS_BAR_SHOW_CARRIER);
        int showCarrierLabel = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_SHOW_CARRIER, 1);
        mShowCarrierLabel.setValue(String.valueOf(showCarrierLabel));
        mShowCarrierLabel.setSummary(mShowCarrierLabel.getEntry());
        mShowCarrierLabel.setOnPreferenceChangeListener(this);

        mCustomCarrierLabel = (Preference) findPreference(KEY_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        AlertDialog dialog;
        if  (preference == mShowKronicLogo) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_LOGO, value ? 1 : 0);
            return true;
        } else if (preference.equals(mLogoStyle)) {
            int logoStyle = Integer.parseInt(((String) objValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_LOGO_STYLE, logoStyle, UserHandle.USER_CURRENT);
            int index = mLogoStyle.findIndexOfValue((String) objValue);
            mLogoStyle.setSummary(
                    mLogoStyle.getEntries()[index]);
            return true;
        } else if (preference == mNetTrafficLocation) {
            int location = Integer.valueOf((String) objValue);
            int index = mNetTrafficLocation.findIndexOfValue((String) objValue);
            mNetTrafficLocation.setSummary(mNetTrafficLocation.getEntries()[index]);
            if (location > 0) {
                // Convert the selected location mode from our list {0,1,2} and store it to "view location" setting: 0=sb; 1=expanded sb
                Settings.System.putIntForUser(getActivity().getContentResolver(),
                        Settings.System.NETWORK_TRAFFIC_VIEW_LOCATION, location-1, UserHandle.USER_CURRENT);
                // And also enable the net monitor
                Settings.System.putIntForUser(getActivity().getContentResolver(),
                        Settings.System.NETWORK_TRAFFIC_STATE, 1, UserHandle.USER_CURRENT);
                updateTrafficLocation(location+1);
            } else { // Disable net monitor completely
                Settings.System.putIntForUser(getActivity().getContentResolver(),
                        Settings.System.NETWORK_TRAFFIC_STATE, 0, UserHandle.USER_CURRENT);
                updateTrafficLocation(location);
            }
            return true;
        } else if (preference == mThreshold) {
            int val = (Integer) objValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, val,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mNetTrafficType) {
            int val = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_TYPE, val,
                    UserHandle.USER_CURRENT);
            int index = mNetTrafficType.findIndexOfValue((String) objValue);
            mNetTrafficType.setSummary(mNetTrafficType.getEntries()[index]);
            return true;
        }  else if (preference == mNetTrafficSize) {
            int width = ((Integer)objValue).intValue();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_FONT_SIZE, width);
            return true;
        } else if (preference == mClockAmPmStyle) {
            int val = Integer.parseInt((String) objValue);
            int index = mClockAmPmStyle.findIndexOfValue((String) objValue);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.STATUSBAR_CLOCK_AM_PM_STYLE, val);
            mClockAmPmStyle.setSummary(mClockAmPmStyle.getEntries()[index]);
            return true;
        } else if (preference == mClockDateDisplay) {
            int val = Integer.parseInt((String) objValue);
            int index = mClockDateDisplay.findIndexOfValue((String) objValue);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.STATUSBAR_CLOCK_DATE_DISPLAY, val);
            mClockDateDisplay.setSummary(mClockDateDisplay.getEntries()[index]);
            if (val == 0) {
                mClockDateStyle.setEnabled(false);
                mClockDateFormat.setEnabled(false);
                mClockDatePosition.setEnabled(false);
            } else {
                mClockDateStyle.setEnabled(true);
                mClockDateFormat.setEnabled(true);
                mClockDatePosition.setEnabled(true);
            }
            return true;
        } else if (preference == mClockDateStyle) {
            int val = Integer.parseInt((String) objValue);
            int index = mClockDateStyle.findIndexOfValue((String) objValue);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.STATUSBAR_CLOCK_DATE_STYLE, val);
            mClockDateStyle.setSummary(mClockDateStyle.getEntries()[index]);
            parseClockDateFormats();
            return true;
        } else if (preference == mStatusBarClock) {
            Settings.Secure.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.Secure.STATUS_BAR_CLOCK,
                    (Boolean) objValue ? 1 : 0);
            return true;
        } else if (preference == mClockDateFormat) {
            int index = mClockDateFormat.findIndexOfValue((String) objValue);

             if (index == CUSTOM_CLOCK_DATE_FORMAT_INDEX) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.clock_date_string_edittext_title);
                alert.setMessage(R.string.clock_date_string_edittext_summary);

                final EditText input = new EditText(getActivity());
                String oldText = Settings.Secure.getString(
                    getActivity().getContentResolver(),
                    Settings.Secure.STATUSBAR_CLOCK_DATE_FORMAT);
                if (oldText != null) {
                    input.setText(oldText);
                }
                alert.setView(input);

                alert.setPositiveButton(R.string.menu_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        String value = input.getText().toString();
                        if (value.equals("")) {
                            return;
                        }
                        Settings.Secure.putString(getActivity().getContentResolver(),
                            Settings.Secure.STATUSBAR_CLOCK_DATE_FORMAT, value);
                         return;
                    }
                });

                alert.setNegativeButton(R.string.menu_cancel,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int which) {
                        return;
                    }
                });
                dialog = alert.create();
                dialog.show();
            } else {
                if ((String) objValue != null) {
                    Settings.Secure.putString(getActivity().getContentResolver(),
                        Settings.Secure.STATUSBAR_CLOCK_DATE_FORMAT, (String) objValue);
                }
            }
            return true;
        } else if (preference == mClockDatePosition) {
            int val = Integer.parseInt((String) objValue);
            int index = mClockDatePosition.findIndexOfValue((String) objValue);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.STATUSBAR_CLOCK_DATE_POSITION, val);
            mClockDatePosition.setSummary(mClockDatePosition.getEntries()[index]);
            parseClockDateFormats();
            return true;
        } else if (preference == mBatteryStyle) {
            int value = Integer.parseInt((String) objValue);
            updateBatteryOptions(value);
            return true;
        } else if (preference == mShowCarrierLabel) {
            int showCarrierLabel = Integer.valueOf((String) objValue);
            int index = mShowCarrierLabel.findIndexOfValue((String) objValue);
            Settings.System.putInt(resolver, Settings.System.
                    STATUS_BAR_SHOW_CARRIER, showCarrierLabel);
            mShowCarrierLabel.setSummary(mShowCarrierLabel.getEntries()[index]);
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        super.onPreferenceTreeClick(preference);
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference.getKey().equals(KEY_CUSTOM_CARRIER_LABEL)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            int maxLength = 10;
            input.setText(TextUtils.isEmpty(mCustomCarrierLabelText) ? "" : mCustomCarrierLabelText);
            input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
            input.setSelection(input.getText().length());
            alert.setView(input);
            alert.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = ((Spannable) input.getText()).toString().trim();
                            Settings.System.putString(resolver, Settings.System.CUSTOM_CARRIER_LABEL, value);
                            updateCustomLabelTextSummary();
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_CUSTOM_CARRIER_LABEL_CHANGED);
                            getActivity().sendBroadcast(i);
                        }
                    });
            alert.setNegativeButton(getString(android.R.string.cancel), null);
            alert.show();
        }
        return true;
    }

    private void updateCustomLabelTextSummary() {
        mCustomCarrierLabelText = Settings.System.getString(
            getActivity().getContentResolver(), Settings.System.CUSTOM_CARRIER_LABEL);

        if (TextUtils.isEmpty(mCustomCarrierLabelText)) {
            mCustomCarrierLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomCarrierLabel.setSummary(mCustomCarrierLabelText);
        }
    }

    private void parseClockDateFormats() {
        String[] dateEntries = getResources().getStringArray(R.array.clock_date_format_entries_values);
        CharSequence parsedDateEntries[];
        parsedDateEntries = new String[dateEntries.length];
        Date now = new Date();
         int lastEntry = dateEntries.length - 1;
        int dateFormat = Settings.Secure.getInt(getActivity()
                .getContentResolver(), Settings.Secure.STATUSBAR_CLOCK_DATE_STYLE, 0);
        for (int i = 0; i < dateEntries.length; i++) {
            if (i == lastEntry) {
                parsedDateEntries[i] = dateEntries[i];
            } else {
                String newDate;
                CharSequence dateString = DateFormat.format(dateEntries[i], now);
                if (dateFormat == CLOCK_DATE_STYLE_LOWERCASE) {
                    newDate = dateString.toString().toLowerCase();
                } else if (dateFormat == CLOCK_DATE_STYLE_UPPERCASE) {
                    newDate = dateString.toString().toUpperCase();
                } else {
                    newDate = dateString.toString();
                }
                 parsedDateEntries[i] = newDate;
            }
        }
        mClockDateFormat.setEntries(parsedDateEntries);
    }

    private void updateBatteryOptions(int batterystyle) {
        boolean enabled = batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN;
        if (batterystyle == BATTERY_STYLE_HIDDEN) {
            mBatteryPercent.setValue(BATTERY_PERCENTAGE_HIDDEN);
            mBatteryPercent.setSummary(mBatteryPercent.getEntry());
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        }
        mBatteryCharging.setEnabled(enabled);
        mBatteryPercent.setEnabled(enabled);
    }

    public void updateTrafficLocation(int location) {
        switch(location){ 
            case 0:
                mThreshold.setEnabled(false);
                mShowArrows.setEnabled(false);
                mNetTrafficType.setEnabled(false);
                mNetTrafficSize.setEnabled(false);
                break;
            case 1:
            case 2:
                mThreshold.setEnabled(true);
                mShowArrows.setEnabled(true);
                mNetTrafficType.setEnabled(true);
                mNetTrafficSize.setEnabled(true);
                break;
            default: 
                break;
        }
    }

    public void onResume(Context context) {
        super.onResume();

        // Adjust status bar clock prefs for notched devices
        if (aosipUtils.hasNotch(getActivity())) {
            mClockPosition.setEntries(R.array.clock_position_entries_notch);
            mClockPosition.setEntryValues(R.array.clock_position_values_notch);
        } else {
            mClockPosition.setEntries(R.array.clock_position_entries);
            mClockPosition.setEntryValues(R.array.clock_position_values);
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
                    sir.xmlResId = R.xml.statusbar;
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
