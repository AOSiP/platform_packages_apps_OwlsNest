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

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.aosip.owlsnest.statusbar.CarrierLabelSettings;
import com.aosip.owlsnest.statusbar.ClockCategory;
import com.aosip.owlsnest.statusbar.CustomLogo;
import com.aosip.owlsnest.statusbar.BatteryCategory;
import com.aosip.owlsnest.statusbar.BatteryBarCategory;
import com.aosip.owlsnest.statusbar.IconsCategory;
import com.aosip.owlsnest.statusbar.TrafficCategory;
import com.aosip.owlsnest.PagerSlidingTabStrip;

public class StatusbarHolder extends SettingsPreferenceFragment {

    ViewPager mViewPager;
    String titleString[];
    ViewGroup mContainer;
    PagerSlidingTabStrip mTabs;

    static Bundle mSavedState;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContainer = container;

        View view = inflater.inflate(R.layout.preference_ui, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mTabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        StatusBarAdapter StatusBarAdapter = new StatusBarAdapter(getFragmentManager());
        mViewPager.setAdapter(StatusBarAdapter);
        mTabs.setViewPager(mViewPager);

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.OWLSNEST;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    class StatusBarAdapter extends FragmentPagerAdapter {
        String titles[] = getTitles();
        private Fragment frags[] = new Fragment[titles.length];

        public StatusBarAdapter(FragmentManager fm) {
            super(fm);
            frags[0] = new BatteryCategory();
            frags[1] = new BatteryBarCategory();
            frags[2] = new CarrierLabelSettings();
            frags[3] = new ClockCategory();
            frags[4] = new CustomLogo();
            frags[5] = new IconsCategory();
            frags[6] = new TrafficCategory();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return frags.length;
        }
    }

    private String[] getTitles() {
        String titleString[];
        titleString = new String[]{
                    getString(R.string.battery_category),
                    getString(R.string.battery_bar_title),
                    getString(R.string.carrier_label_settings_title),
                    getString(R.string.clock_category),
                    getString(R.string.sb_custom_logos),
                    getString(R.string.icon_category),
                    getString(R.string.network_traffic_title)};
        return titleString;
        }
    }
