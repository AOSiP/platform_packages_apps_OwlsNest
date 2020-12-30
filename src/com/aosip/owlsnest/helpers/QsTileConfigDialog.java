/*
 * Copyright (C) 2015-2021 AOSiP
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
 *
 * Author: calebcabob <calphonic@gmail.com>
 */

package com.aosip.owlsnest.helpers;

import android.app.Dialog;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.android.settings.R;

/**
 * Creates a floating dialog with seekbar sliders designed for
 * setting the number of columns/rows shown in QS panel.
 */
public class QsTileConfigDialog extends DialogFragment {

    public static final String TAG_QS_TILE_CONFIG_DIALOG = "QsTileConfigDialog";

    private Button okButton;
    private SeekBar portColumns;
    private SeekBar portRows;
    private SeekBar landColumns;
    private SeekBar landRows;
    private TextView pcText;
    private TextView prText;
    private TextView lcText;
    private TextView lrText;

    private Dialog mDialog;
    private View mView;

    private int portColumnsValue;
    private int portRowsValue;
    private int landColumnsValue;
    private int landRowsValue;
    private int mSeekBarProgress;
    private boolean isPortColumnsActive = false;
    private boolean isPortRowsActive = false;
    private boolean isLandColumnsActive = false;
    private boolean isLandRowsActive = false;

    public QsTileConfigDialog() {
    }

    public static QsTileConfigDialog newInstance() {
        return new QsTileConfigDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mView != null) {
            mDialog.setContentView(mView);
            mDialog.setCanceledOnTouchOutside(true);
            mDialog.show();
        }
        mDialog = new Dialog(requireActivity(), R.style.QsTileDialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        mView = (inflater.inflate(R.layout.qs_tile_config_dialog, null));
        applyDialogParams();

        return mDialog;
    }

    private void applyDialogParams() {
        mDialog.setCanceledOnTouchOutside(true);

        Window window = mDialog.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams a = window.getAttributes();
        a.dimAmount = .4f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                    Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.qs_tile_config_dialog, container, false);

        if (mView != null) {
            okButton = mView.findViewById(R.id.submit);
            portColumns = mView.findViewById(R.id.portrait_columns_seekbar);
            portRows = mView.findViewById(R.id.portrait_rows_seekbar);
            landColumns = mView.findViewById(R.id.landscape_columns_seekbar);
            landRows = mView.findViewById(R.id.landscape_rows_seekbar);

            pcText = mView.findViewById(R.id.pcText);
            prText = mView.findViewById(R.id.prText);
            lcText = mView.findViewById(R.id.lcText);
            lrText = mView.findViewById(R.id.lrText);

            getInitialSeekBarPositions();
            setInitialSeekBarPositions();

            pcText.setText(String.valueOf(portColumnsValue));
            prText.setText(String.valueOf(portRowsValue));
            lcText.setText(String.valueOf(landColumnsValue));
            lrText.setText(String.valueOf(landRowsValue));

            initializeDialogVariables(mView, mSeekBarProgress);
        }

        return mView;
    }

    private void initializeDialogVariables(@NonNull View mView, int mSeekBarProgress) {

        if (portColumns != null) {
            portColumns.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isPortColumnsActive = true;
                    updateActiveSeekbar(mSeekBarProgress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    isPortColumnsActive = true;
                    updateActiveSeekbar(mSeekBarProgress);
                }
            });
        }

        if (portRows != null) {
            portRows.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isPortRowsActive = true;
                    updateActiveSeekbar(mSeekBarProgress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    isPortRowsActive = true;
                    updateActiveSeekbar(mSeekBarProgress);
                }
            });
        }

        if (landColumns != null) {
            landColumns.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isLandColumnsActive = true;
                    updateActiveSeekbar(mSeekBarProgress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    isLandColumnsActive = true;
                    updateActiveSeekbar(mSeekBarProgress);
                }
            });
        }

        if (landRows != null) {
            landRows.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isLandRowsActive = true;
                    updateActiveSeekbar(mSeekBarProgress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    isLandRowsActive = true;
                    updateActiveSeekbar(mSeekBarProgress);
                }
            });
        }

        if (okButton != null) {
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Remove background dimming once dialog closes
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                    collectValuesForSettings();
                    updateSettings();
                    dismiss();
                }
            });
        }
    }

    private void updateActiveSeekbar(int mSeekBarProgress) {
        if (isPortColumnsActive) {
            mSeekBarProgress = portColumns.getProgress();
            pcText.setText(String.valueOf(mSeekBarProgress));
        }
        if (isPortRowsActive) {
            mSeekBarProgress = portRows.getProgress();
            prText.setText(String.valueOf(mSeekBarProgress));
        }
        if (isLandColumnsActive) {
            mSeekBarProgress = landColumns.getProgress();
            lcText.setText(String.valueOf(mSeekBarProgress));
        }
        if (isLandRowsActive) {
            mSeekBarProgress = landRows.getProgress();
            lrText.setText(String.valueOf(mSeekBarProgress));
        }
    }

    /**
     *  Get seekbar progress values from Settings first.
     */
    private void getInitialSeekBarPositions() {
        final ContentResolver resolver = getActivity().getContentResolver();
        portColumnsValue = Settings.System.getIntForUser(resolver,
                Settings.System.QS_COLUMNS_PORTRAIT, 3, UserHandle.USER_CURRENT);
        portRowsValue = Settings.System.getIntForUser(resolver,
                Settings.System.QS_ROWS_PORTRAIT, 3, UserHandle.USER_CURRENT);
        landColumnsValue = Settings.System.getIntForUser(resolver,
                Settings.System.QS_COLUMNS_LANDSCAPE, 4, UserHandle.USER_CURRENT);
        landRowsValue = Settings.System.getIntForUser(resolver,
                Settings.System.QS_ROWS_LANDSCAPE, 2, UserHandle.USER_CURRENT);
    }

    /**
     *  Set initial seekbar values using values from current
     *  system settings (@getInitialSeekBarPositions).
     */
    private void setInitialSeekBarPositions() {
        portColumns.setProgress(portColumnsValue);
        portRows.setProgress(portRowsValue);
        landColumns.setProgress(landColumnsValue);
        landRows.setProgress(landRowsValue);
    }

    /**
     *  This is called immediately before applying final values to system
     *  settings. We need to get the position (getProgress) from
     *  each seekbar first.
     */
    private void collectValuesForSettings() {
        portColumnsValue = (portColumns.getProgress());
        portRowsValue = (portRows.getProgress());
        landColumnsValue = (landColumns.getProgress());
        landRowsValue = (landRows.getProgress());
    }

    /**
     *  Apply the new values to system settings.
     */
    private void updateSettings() {
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.QS_COLUMNS_PORTRAIT, portColumnsValue);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.QS_ROWS_PORTRAIT, portRowsValue);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.QS_COLUMNS_LANDSCAPE, landColumnsValue);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.QS_ROWS_LANDSCAPE, landRowsValue);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
