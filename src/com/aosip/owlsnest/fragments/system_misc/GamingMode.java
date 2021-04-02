/*
 * Copyright (C) 2019 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aosip.owlsnest.fragments.system_misc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBarController;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.aosip.support.preference.AppListPreference;
import com.aosip.support.preference.PackageListAdapter;
import com.aosip.support.preference.PackageListAdapter.PackageItem;
import com.aosip.support.preference.SystemSettingListPreference;
import com.aosip.support.preference.SystemSettingSwitchPreference;
import com.aosip.owlsnest.fragments.system_misc.GamingModeEnabler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SearchIndexable
public class GamingMode extends SettingsPreferenceFragment implements
        Preference.OnPreferenceClickListener, GamingModeEnabler.OnGamingModeChangeListener,
        Indexable {

    private static final int DIALOG_GAMING_APPS = 1;

    private SystemSettingListPreference mRingerMode;
    private SystemSettingListPreference mGamingNotification;
    private SystemSettingListPreference mHeadsUpDisable;
    // private SystemSettingSwitchPreference mHardwareKeysDisable;
    private SystemSettingSwitchPreference mManualBrightness;
    private SystemSettingSwitchPreference mDynamicMode;

    private PackageListAdapter mPackageAdapter;
    private PackageManager mPackageManager;
    private PreferenceGroup mGamingPrefList;
    private Preference mAddGamingPref;

    private String mGamingPackageList;
    private Map<String, Package> mGamingPackages;
    private Context mContext;

    private TextView mTextView;

    private static final int KEY_MASK_HOME = 0x01;
    private static final int KEY_MASK_BACK = 0x02;
    private static final int KEY_MASK_MENU = 0x04;
    private static final int KEY_MASK_APP_SWITCH = 0x10;

    private GamingModeEnabler mGamingModeEnabler;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Get launch-able applications
        addPreferencesFromResource(R.xml.gaming_mode_settings);

        final PreferenceScreen prefScreen = getPreferenceScreen();

        mRingerMode = (SystemSettingListPreference) findPreference("gaming_mode_ringer_mode");
        mGamingNotification = (SystemSettingListPreference) findPreference("gaming_mode_notifications");
        mHeadsUpDisable = (SystemSettingListPreference) findPreference("gaming_mode_headsup_toggle");
        // mHardwareKeysDisable = (SystemSettingSwitchPreference) findPreference("gaming_mode_hw_keys_toggle");
        mManualBrightness = (SystemSettingSwitchPreference) findPreference("gaming_mode_manual_brightness_toggle");
        mDynamicMode = (SystemSettingSwitchPreference) findPreference("gaming_mode_dynamic_state");

        /* if (!hasHWkeys()) {
            prefScreen.removePreference(mHardwareKeysDisable);
        } */
        
        mPackageManager = getPackageManager();
        mPackageAdapter = new PackageListAdapter(getActivity());

        mGamingPrefList = (PreferenceGroup) findPreference("gamingmode_applications");
        mGamingPrefList.setOrderingAsAdded(false);

        mGamingPackages = new HashMap<String, Package>();

        mAddGamingPref = findPreference("gaming_mode_add_packages");

        mAddGamingPref.setOnPreferenceClickListener(this);

        mContext = getActivity().getApplicationContext();

        refreshPreferenceStates();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final SettingsActivity activity = (SettingsActivity) getActivity();
        final SwitchBar switchBar = activity.getSwitchBar();
        mGamingModeEnabler = new GamingModeEnabler(getContext(),
                new SwitchBarController(switchBar), this, getSettingsLifecycle());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mGamingModeEnabler.teardownSwitchController();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCustomApplicationPrefs();
        refreshPreferenceStates();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }

    @Override
    public int getDialogMetricsCategory(int dialogId) {
        if (dialogId == DIALOG_GAMING_APPS) {
            return MetricsProto.MetricsEvent.OWLSNEST;
        }
        return 0;
    }

    /**
     * Utility classes and supporting methods
     */
    @Override
    public Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Dialog dialog;
        final ListView list = new ListView(getActivity());
        list.setAdapter(mPackageAdapter);

        builder.setTitle(R.string.profile_choose_app);
        builder.setView(list);
        dialog = builder.create();

        switch (id) {
            case DIALOG_GAMING_APPS:
                list.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Add empty application definition, the user will be able to edit it later
                        PackageItem info = (PackageItem) parent.getItemAtPosition(position);
                        addCustomApplicationPref(info.packageName, mGamingPackages);
                        dialog.cancel();
                    }
                });
        }
        return dialog;
    }

    /**
     * Application class
     */
    private static class Package {
        public String name;
        /**
         * Stores all the application values in one call
         * @param name
         */
        public Package(String name) {
            this.name = name;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(name);
            return builder.toString();
        }

        public static Package fromString(String value) {
            if (TextUtils.isEmpty(value)) {
                return null;
            }

            try {
                Package item = new Package(value);
                return item;
            } catch (NumberFormatException e) {
                return null;
            }
        }

    };

    /* private boolean hasHWkeys() {
        final int deviceKeys = getContext().getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        // read bits for present hardware keys
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;
        return (hasHomeKey || hasBackKey || hasMenuKey || hasAppSwitchKey);
    } */

    private void refreshCustomApplicationPrefs() {
        if (!parsePackageList()) {
            return;
        }

        // Add the Application Preferences
        if (mGamingPrefList != null) {
            mGamingPrefList.removeAll();

            for (Package pkg : mGamingPackages.values()) {
                try {
                    Preference pref = createPreferenceFromInfo(pkg);
                    mGamingPrefList.addPreference(pref);
                } catch (PackageManager.NameNotFoundException e) {
                    // Do nothing
                }
            }

            // Keep these at the top
            mAddGamingPref.setOrder(0);
            // Add 'add' options
            mGamingPrefList.addPreference(mAddGamingPref);
        }
    }

    @Override                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mAddGamingPref) {
            showDialog(DIALOG_GAMING_APPS);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_delete_title)
                    .setMessage(R.string.dialog_delete_message)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (preference == mGamingPrefList.findPreference(preference.getKey())) {
                                removeApplicationPref(preference.getKey(), mGamingPackages);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);

            builder.show();
        }
        refreshPreferenceStates();
        return true;
    }

    @Override
    public void onChanged(boolean enabled) {
        refreshPreferenceStates();
    }

    private void refreshPreferenceStates() {
        boolean enabled = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.GAMING_MODE_ACTIVE, 0, UserHandle.USER_CURRENT) == 1;
        mRingerMode.setEnabled(enabled ? true : false);
        mGamingNotification.setEnabled(enabled ? true : false);
        mHeadsUpDisable.setEnabled(enabled ? true : false);
        mManualBrightness.setEnabled(enabled ? true : false);
        mDynamicMode.setEnabled(enabled ? true : false);
        mGamingPrefList.setEnabled(enabled ? true : false);
    }

    private void addCustomApplicationPref(String packageName, Map<String,Package> map) {
        Package pkg = map.get(packageName);
        if (pkg == null) {
            pkg = new Package(packageName);
            map.put(packageName, pkg);
            savePackageList(false, map);
            refreshCustomApplicationPrefs();
        }
    }

    private Preference createPreferenceFromInfo(Package pkg)
            throws PackageManager.NameNotFoundException {
        PackageInfo info = mPackageManager.getPackageInfo(pkg.name,
                PackageManager.GET_META_DATA);
        Preference pref =
                new AppListPreference(getActivity());

        pref.setKey(pkg.name);
        pref.setTitle(info.applicationInfo.loadLabel(mPackageManager));
        pref.setIcon(info.applicationInfo.loadIcon(mPackageManager));
        pref.setPersistent(false);
        pref.setOnPreferenceClickListener(this);
        return pref;
    }

    private void removeApplicationPref(String packageName, Map<String,Package> map) {
        if (map.remove(packageName) != null) {
            savePackageList(false, map);
            refreshCustomApplicationPrefs();
        }
    }

    private boolean parsePackageList() {
        boolean parsed = false;

        final String gamingModeString = Settings.System.getString(getContentResolver(),
                Settings.System.GAMING_MODE_VALUES);

        if (!TextUtils.equals(mGamingPackageList, gamingModeString)) {
            mGamingPackageList = gamingModeString;
            mGamingPackages.clear();
            parseAndAddToMap(gamingModeString, mGamingPackages);
            parsed = true;
        }

        return parsed;
    }

    private void parseAndAddToMap(String baseString, Map<String,Package> map) {
        if (baseString == null) {
            return;
        }

        final String[] array = TextUtils.split(baseString, "\\|");
        for (String item : array) {
            if (TextUtils.isEmpty(item)) {
                continue;
            }
            Package pkg = Package.fromString(item);
            map.put(pkg.name, pkg);
        }
    }

    private void savePackageList(boolean preferencesUpdated, Map<String,Package> map) {
        String setting = map == mGamingPackages ? Settings.System.GAMING_MODE_VALUES : Settings.System.GAMING_MODE_DUMMY;

        List<String> settings = new ArrayList<String>();
        for (Package app : map.values()) {
            settings.add(app.toString());
        }
        final String value = TextUtils.join("|", settings);
        if (preferencesUpdated) {
            if (TextUtils.equals(setting, Settings.System.GAMING_MODE_VALUES)) {
                mGamingPackageList = value;
            }
        }
        Settings.System.putString(getContentResolver(),
                setting, value);
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                    boolean enabled) {
                final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                final SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.gaming_mode_settings;
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
