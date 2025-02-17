/*
 * Copyright (C) 2016 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package com.best.deskclock.timer;

import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;

import android.widget.TextView;

import com.best.deskclock.utils.Utils;

/**
 * A controller which will format a provided time in millis to display as a timer.
 */
public final class TimerTextController {

    private final TextView mTextView;

    public TimerTextController(TextView textView) {
        mTextView = textView;
    }

    public void setTimeString(long remainingTime) {
        boolean isNegative = false;
        if (remainingTime < 0) {
            remainingTime = -remainingTime;
            isNegative = true;
        }

        int hours = (int) (remainingTime / HOUR_IN_MILLIS);
        int remainder = (int) (remainingTime % HOUR_IN_MILLIS);

        int minutes = (int) (remainder / MINUTE_IN_MILLIS);
        remainder = (int) (remainder % MINUTE_IN_MILLIS);

        int seconds = (int) (remainder / SECOND_IN_MILLIS);
        remainder = (int) (remainder % SECOND_IN_MILLIS);

        // Round up to the next second
        if (!isNegative && remainder != 0) {
            seconds++;
            if (seconds == 60) {
                seconds = 0;
                minutes++;
                if (minutes == 60) {
                    minutes = 0;
                    hours++;
                }
            }
        }

        String time = Utils.getTimeString(mTextView.getContext(), hours, minutes, seconds);
        if (isNegative && !(hours == 0 && minutes == 0 && seconds == 0)) {
            time = "−" + time;
        }

        mTextView.setText(time);
    }
}
