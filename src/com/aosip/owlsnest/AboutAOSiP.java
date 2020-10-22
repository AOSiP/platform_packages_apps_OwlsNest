/*
 * Copyright (C) 2015-2020 AOSiP
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

import android.app.Dialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.android.settings.R;

import java.util.Objects;

public class AboutAOSiP extends DialogFragment {

    public static final String TAG_ABOUT_AOSIP = "AboutAOSiP";

    private Dialog dialog;
    private View view;

    public AboutAOSiP() {
    }

    public static AboutAOSiP newInstance() {
        return new AboutAOSiP ();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new Dialog(requireActivity(), R.style.AboutAosip);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.about_aosip, container, false);

        if (view != null) {
            setLinkMovementMethods();
        }

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        view = (inflater.inflate(R.layout.about_aosip, null));

        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    private void setLinkMovementMethods() {
        TextView telegram = view.findViewById(R.id.telegramSummary);
        telegram.setMovementMethod(LinkMovementMethod.getInstance());

        TextView github = view.findViewById(R.id.githubSummary);
        github.setMovementMethod(LinkMovementMethod.getInstance());

        TextView gerrit = view.findViewById(R.id.gerritSummary);
        gerrit.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
