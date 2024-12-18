/*
 * Copyright (C) 2015 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package com.best.deskclock.settings;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.media.AudioManager.STREAM_ALARM;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.best.deskclock.R;
import com.best.deskclock.RingtonePreviewKlaxon;
import com.best.deskclock.data.DataModel;

public class AlarmVolumePreference extends Preference {

    private static final long ALARM_PREVIEW_DURATION_MS = 5000;

    private SeekBar mSeekbar;
    private boolean mPreviewPlaying;

    public AlarmVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final Context context = getContext();
        final AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);

        // Disable click feedback for this preference.
        holder.itemView.setClickable(false);

        // Hide the value of the seekbar located to the right of it to have a seekbar that fills the screen
        final TextView seekBarValue = (TextView) holder.findViewById(R.id.seekbar_value);
        seekBarValue.setVisibility(View.GONE);

        // Minimum volume for alarm is not 0, calculate it.
        int maxVolume = audioManager.getStreamMaxVolume(STREAM_ALARM) - getMinVolume(audioManager);
        mSeekbar = (SeekBar) holder.findViewById(R.id.seekbar);
        mSeekbar.setMax(maxVolume);
        mSeekbar.setProgress(audioManager.getStreamVolume(STREAM_ALARM) - getMinVolume(audioManager));

        onSeekbarChanged();

        final ContentObserver volumeObserver = new ContentObserver(mSeekbar.getHandler()) {
            @Override
            public void onChange(boolean selfChange) {
                // Volume was changed elsewhere, update our slider.
                mSeekbar.setProgress(audioManager.getStreamVolume(STREAM_ALARM) - getMinVolume(audioManager));
            }
        };

        mSeekbar.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                context.getContentResolver().registerContentObserver(Settings.System.CONTENT_URI,
                        true, volumeObserver);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                context.getContentResolver().unregisterContentObserver(volumeObserver);
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int newVolume = progress + getMinVolume(audioManager);
                    audioManager.setStreamVolume(STREAM_ALARM, newVolume, 0);
                }
                onSeekbarChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!mPreviewPlaying) {
                    // If we are not currently playing, start.
                    RingtonePreviewKlaxon.start(context, DataModel.getDataModel().getAlarmRingtoneUriFromSettings());
                    mPreviewPlaying = true;
                    seekBar.postDelayed(() -> {
                        RingtonePreviewKlaxon.stop(context);
                        mPreviewPlaying = false;
                    }, ALARM_PREVIEW_DURATION_MS);
                }
            }
        });
    }

    private void onSeekbarChanged() {
        mSeekbar.setEnabled(doesDoNotDisturbAllowAlarmPlayback());
    }

    private boolean doesDoNotDisturbAllowAlarmPlayback() {
        return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) || doesDoNotDisturbAllowAlarmPlaybackNPlus();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private boolean doesDoNotDisturbAllowAlarmPlaybackNPlus() {
        final NotificationManager notificationManager = (NotificationManager)
                getContext().getSystemService(NOTIFICATION_SERVICE);
        return notificationManager.getCurrentInterruptionFilter() !=
                NotificationManager.INTERRUPTION_FILTER_NONE;
    }

    private int getMinVolume(AudioManager audioManager) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ? audioManager.getStreamMinVolume(STREAM_ALARM) : 0;
    }
}
