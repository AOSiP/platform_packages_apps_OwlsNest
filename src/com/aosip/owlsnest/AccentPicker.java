/*
 * Copyright (C) 2018 The Dirty Unicorns Project
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

package com.aosip.owlsnest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.ThemeUtils;

public class AccentPicker extends InstrumentedDialogFragment
        implements OnClickListener {

    private static final String TAG_ACCENT_PICKER = "accent_picker";

    private View mView;
    private int mUserId;

    private IOverlayManager mOverlayManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId = UserHandle.myUserId();
        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mView = LayoutInflater.from(getActivity()).inflate(
                R.layout.accent_picker, null);
        initView();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mView)
                .setNegativeButton(R.string.cancel, this)
                .setNeutralButton(R.string.theme_accent_picker_default, this)
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private void initView() {
        ContentResolver resolver = getActivity().getContentResolver();

        Button accents[] = new Button[20];
        if (mView != null) {
            accents[0] = mView.findViewById(R.id.redAccent);
            accents[1] = mView.findViewById(R.id.pinkAccent);
            accents[2] = mView.findViewById(R.id.purpleAccent);
            accents[3] = mView.findViewById(R.id.deeppurpleAccent);
            accents[4] = mView.findViewById(R.id.indigoAccent);
            accents[5] = mView.findViewById(R.id.blueAccent);
            accents[6] = mView.findViewById(R.id.lightblueAccent);
            accents[7] = mView.findViewById(R.id.cyanAccent);
            accents[8] = mView.findViewById(R.id.tealAccent);
            accents[9] = mView.findViewById(R.id.greenAccent);
            accents[10] = mView.findViewById(R.id.lightgreenAccent);
            accents[11] = mView.findViewById(R.id.limeAccent);
            accents[12] = mView.findViewById(R.id.yellowAccent);
            accents[13] = mView.findViewById(R.id.amberAccent);
            accents[14] = mView.findViewById(R.id.orangeAccent);
            accents[15] = mView.findViewById(R.id.deeporangeAccent);
            accents[16] = mView.findViewById(R.id.brownAccent);
            accents[17] = mView.findViewById(R.id.greyAccent);
            accents[18] = mView.findViewById(R.id.bluegreyAccent);
            accents[19] = mView.findViewById(R.id.blackAccent);
        }

        for (int i=0;i<accents.length;i++) {
            final int n = i+1; // Need something better than this ;_;
            if (accents[i] != null) {
                accents[i].setOnClickListener(new View.OnClickListener() {
                   @Override
                    public void onClick(View v) {
                        Settings.System.putIntForUser(resolver,
                                Settings.System.ACCENT_PICKER, n,
                                UserHandle.USER_CURRENT);
                        dismiss();
                    }
                });
            }
        }

        // Change the accent picker button depending on whether or not the dark
        // theme is applied
        accents[19].setBackgroundColor(getResources().getColor(
                ThemeUtils.canUseBlackAccent() ?
                        R.color.accent_picker_dark_accent :
                        R.color.accent_picker_white_accent));
        accents[19].setBackgroundTintList(getResources().getColorStateList(
                ThemeUtils.canUseBlackAccent() ?
                        R.color.accent_picker_dark_accent :
                        R.color.accent_picker_white_accent));

        GridLayout gridlayout;
        if (mView != null) {

            int intOrientation = getResources().getConfiguration().orientation;
            gridlayout = mView.findViewById(R.id.Gridlayout);
            // Lets split this up instead of creating two different layouts
            // just so we can change the columns
            gridlayout.setColumnCount(intOrientation ==
                    Configuration.ORIENTATION_PORTRAIT ? 5 : 8);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (which == AlertDialog.BUTTON_NEGATIVE) {
           dismiss();
        }
        if (which == AlertDialog.BUTTON_NEUTRAL) {
           Settings.System.putIntForUser(resolver,
                   Settings.System.ACCENT_PICKER, 0, UserHandle.USER_CURRENT);
           dismiss();
        }
    }

    public static void show(Fragment parent) {
        if (!parent.isAdded()) return;

        final AccentPicker dialog = new AccentPicker();
        dialog.setTargetFragment(parent, 0);
        dialog.show(parent.getFragmentManager(), TAG_ACCENT_PICKER);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }
}
