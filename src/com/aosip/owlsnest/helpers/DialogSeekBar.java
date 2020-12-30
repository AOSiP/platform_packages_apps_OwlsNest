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

package com.aosip.owlsnest.helpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSeekBar;

import com.android.settings.R;

/**
 * SeekBar that allows setting both a minimum and maximum value.
 * This was taken from IntervalSeekBar.
 */
public class DialogSeekBar extends AppCompatSeekBar {

    private float mMin;
    private float mMax;
    private float mDefault;

    private DialogSeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener;

    public DialogSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray seekBar = context.obtainStyledAttributes(attrs,
                R.styleable.DialogSeekBar, 0, 0);

        /* Set the default params for each SeekBar to 1-6 */
        mMax = seekBar.getFloat(R.styleable.DialogSeekBar_maxValue, (float) 6);
        mMin = seekBar.getFloat(R.styleable.DialogSeekBar_minValue, (float) 1);
        mDefault = seekBar.getFloat(R.styleable.DialogSeekBar_defaultQsValue, (float) 1);

        int digits = seekBar.getInt(R.styleable.DialogSeekBar_digits, 0);

        if (mMin > mMax) {
            float val = mMax;
            mMax = mMin;
            mMin = val;
        }

        setMax(findFloatValue(mMax));
        setProgressFloat(mDefault);

        seekBar.recycle();
    }

    public float getProgressFloat() {
        return (getProgress());
    }

    public void setProgressFloat(float progress) {
        setProgress(findFloatValue(progress));
    }

    private int findFloatValue(float value) {
        return (int) value;
    }

    public float getMinimum() {
        return mMin;
    }

    public float getMaximum() {
        return mMax;
    }

    public float getDefault() {
        return mDefault;
    }

    public void setMaximum(float max) {
        mMax = max;
        setMax((int) mMax);
    }

    public void setMinimum(float min) {
        mMin = min;
    }
}
