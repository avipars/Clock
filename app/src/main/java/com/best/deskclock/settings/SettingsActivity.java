/*
 * Copyright (C) 2015 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package com.best.deskclock.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.ListPreferenceDialogFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.TwoStatePreference;

import com.best.deskclock.R;
import com.best.deskclock.Utils;
import com.best.deskclock.data.DataModel;
import com.best.deskclock.data.TimeZones;
import com.best.deskclock.data.Weekdays;
import com.best.deskclock.ringtone.RingtonePickerActivity;
import com.best.deskclock.widget.CollapsingToolbarBaseActivity;

import java.util.Objects;

/**
 * Settings for the Alarm Clock.
 */
public final class SettingsActivity extends CollapsingToolbarBaseActivity {

    public static final String KEY_PERMISSION_MESSAGE = "key_permission_message";
    public static final String KEY_THEME = "key_theme";
    public static final String SYSTEM_THEME = "0";
    public static final String LIGHT_THEME = "1";
    public static final String DARK_THEME = "2";
    public static final String KEY_ACCENT_COLOR = "key_accent_color";
    public static final String DEFAULT_ACCENT_COLOR = "0";
    public static final String BLUE_GRAY_ACCENT_COLOR = "1";
    public static final String BROWN_ACCENT_COLOR = "2";
    public static final String GREEN_ACCENT_COLOR = "3";
    public static final String INDIGO_ACCENT_COLOR = "4";
    public static final String ORANGE_ACCENT_COLOR = "5";
    public static final String PINK_ACCENT_COLOR = "6";
    public static final String RED_ACCENT_COLOR = "7";
    public static final String KEY_DARK_MODE = "dark_mode";
    public static final String KEY_DEFAULT_DARK_MODE = "default";
    public static final String KEY_AMOLED_DARK_MODE = "amoled";
    public static final String KEY_CARD_BACKGROUND = "key_card_background";
    public static final String KEY_CARD_BACKGROUND_BORDER = "key_card_background_border";
    public static final String KEY_VIBRATIONS = "key_vibrations";
    public static final String KEY_DEFAULT_ALARM_RINGTONE = "default_alarm_ringtone";
    public static final String KEY_ALARM_SNOOZE = "snooze_duration";
    public static final String KEY_ALARM_CRESCENDO = "alarm_crescendo_duration";
    public static final String KEY_TIMER_CRESCENDO = "timer_crescendo_duration";
    public static final String KEY_TIMER_RINGTONE = "timer_ringtone";
    public static final String KEY_TIMER_VIBRATE = "timer_vibrate";
    public static final String KEY_AUTO_SILENCE = "auto_silence";
    public static final String KEY_CLOCK_STYLE = "clock_style";
    public static final String KEY_CLOCK_DISPLAY_SECONDS = "display_clock_seconds";
    public static final String KEY_HOME_TZ = "home_time_zone";
    public static final String KEY_AUTO_HOME_CLOCK = "automatic_home_clock";
    public static final String KEY_DATE_TIME = "date_time";
    public static final String KEY_SS_SETTINGS = "screensaver_settings";
    public static final String KEY_VOLUME_BUTTONS = "volume_button_setting";
    public static final String KEY_POWER_BUTTONS = "power_button";
    public static final String KEY_WEEK_START = "week_start";
    public static final String KEY_FLIP_ACTION = "flip_action";
    public static final String KEY_SHAKE_ACTION = "shake_action";
    public static final String DEFAULT_VOLUME_BEHAVIOR = "0";
    public static final String VOLUME_BEHAVIOR_SNOOZE = "1";
    public static final String VOLUME_BEHAVIOR_DISMISS = "2";
    public static final String DEFAULT_POWER_BEHAVIOR = "0";
    public static final String POWER_BEHAVIOR_SNOOZE = "1";
    public static final String POWER_BEHAVIOR_DISMISS = "2";
    public static final String KEY_PERMISSIONS_MANAGEMENT = "permissions_management";
    public static final String PREFS_FRAGMENT_TAG = "prefs_fragment";
    public static final String PREFERENCE_DIALOG_FRAGMENT_TAG = "preference_dialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the prefs fragment in code to ensure it's created before PreferenceDialogFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new PrefsFragment(), PREFS_FRAGMENT_TAG)
                    .disallowAddToBackStack()
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, Menu.NONE, 0, R.string.about_title)
                .setIcon(R.drawable.ic_about).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            final Intent settingIntent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(settingIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PrefsFragment extends PreferenceFragmentCompat implements
            Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        Preference mPermissionMessage;

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                getPreferenceManager().setStorageDeviceProtected();
            }
            addPreferencesFromResource(R.xml.settings);
            hidePreferences();
            loadTimeZoneList();
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // By default, do not recreate the DeskClock activity
            requireActivity().setResult(RESULT_CANCELED);
        }

        @Override
        public void onResume() {
            super.onResume();
            refresh();
        }

        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            switch (pref.getKey()) {
                case KEY_THEME -> {
                    final ListPreference themePref = (ListPreference) pref;
                    final int index = themePref.findIndexOfValue((String) newValue);
                    themePref.setSummary(themePref.getEntries()[index]);
                    switch (index) {
                        case 0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        case 1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        case 2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                }
                case KEY_ACCENT_COLOR -> {
                    final ListPreference themePref = (ListPreference) pref;
                    final int index = themePref.findIndexOfValue((String) newValue);
                    themePref.setSummary(themePref.getEntries()[index]);
                    switch (index) {
                        case 0 -> ThemeController.applyAccentColor(ThemeController.AccentColor.DEFAULT);
                        case 1 -> ThemeController.applyAccentColor(ThemeController.AccentColor.BLUE_GRAY);
                        case 2 -> ThemeController.applyAccentColor(ThemeController.AccentColor.BROWN);
                        case 3 -> ThemeController.applyAccentColor(ThemeController.AccentColor.GREEN);
                        case 4 -> ThemeController.applyAccentColor(ThemeController.AccentColor.INDIGO);
                        case 5 -> ThemeController.applyAccentColor(ThemeController.AccentColor.ORANGE);
                        case 6 -> ThemeController.applyAccentColor(ThemeController.AccentColor.PINK);
                        case 7 -> ThemeController.applyAccentColor(ThemeController.AccentColor.RED);
                    }
                }
                case KEY_DARK_MODE -> {
                    final ListPreference amoledPref = (ListPreference) pref;
                    final int darkModeIndex = amoledPref.findIndexOfValue((String) newValue);
                    amoledPref.setSummary(amoledPref.getEntries()[darkModeIndex]);
                    if (Utils.isNight(requireActivity().getResources())) {
                        switch (darkModeIndex) {
                            case 0 -> ThemeController.applyDarkMode(ThemeController.DarkMode.DEFAULT_DARK_MODE);
                            case 1 -> ThemeController.applyDarkMode(ThemeController.DarkMode.AMOLED);
                        }
                    }
                }
                case KEY_CARD_BACKGROUND -> {
                    final TwoStatePreference cardBackgroundPref = (TwoStatePreference) pref;
                    cardBackgroundPref.setChecked(DataModel.getDataModel().isCardBackgroundDisplayed());
                    Utils.setVibrationTime(requireContext(), 50);
                }
                case KEY_CARD_BACKGROUND_BORDER -> {
                    final TwoStatePreference cardBackgroundBorderPref = (TwoStatePreference) pref;
                    cardBackgroundBorderPref.setChecked(DataModel.getDataModel().isCardBackgroundBorderDisplayed());
                    Utils.setVibrationTime(requireContext(), 50);
                }
                case KEY_VIBRATIONS -> {
                    final TwoStatePreference vibrationsPref = (TwoStatePreference) pref;
                    vibrationsPref.setChecked(DataModel.getDataModel().isVibrationsEnabled());
                    Utils.setVibrationTime(requireContext(), 50);
                }
                case KEY_CLOCK_STYLE, KEY_ALARM_CRESCENDO, KEY_HOME_TZ, KEY_ALARM_SNOOZE,
                        KEY_TIMER_CRESCENDO, KEY_VOLUME_BUTTONS, KEY_POWER_BUTTONS, KEY_FLIP_ACTION,
                        KEY_SHAKE_ACTION, KEY_WEEK_START -> {
                    final ListPreference preference = (ListPreference) pref;
                    final int index = preference.findIndexOfValue((String) newValue);
                    preference.setSummary(preference.getEntries()[index]);
                }
                case KEY_CLOCK_DISPLAY_SECONDS -> {
                    DataModel.getDataModel().setDisplayClockSeconds((boolean) newValue);
                    Utils.setVibrationTime(requireContext(), 50);
                }
                case KEY_AUTO_SILENCE -> {
                    final String delay = (String) newValue;
                    updateAutoSnoozeSummary((ListPreference) pref, delay);
                }
                case KEY_AUTO_HOME_CLOCK -> {
                    final boolean autoHomeClockEnabled = ((TwoStatePreference) pref).isChecked();
                    final Preference homeTimeZonePref = findPreference(KEY_HOME_TZ);
                    Objects.requireNonNull(homeTimeZonePref).setEnabled(!autoHomeClockEnabled);
                    Utils.setVibrationTime(requireContext(), 50);
                }
                case KEY_TIMER_VIBRATE -> {
                    final TwoStatePreference timerVibratePref = (TwoStatePreference) pref;
                    DataModel.getDataModel().setTimerVibrate(timerVibratePref.isChecked());
                    Utils.setVibrationTime(requireContext(), 50);
                }
                case KEY_DEFAULT_ALARM_RINGTONE -> pref.setSummary(DataModel.getDataModel().getAlarmRingtoneTitle());
                case KEY_TIMER_RINGTONE -> pref.setSummary(DataModel.getDataModel().getTimerRingtoneTitle());
            }
            // Set result so DeskClock knows to refresh itself
            requireActivity().setResult(RESULT_OK);
            return true;
        }


        @Override
        public boolean onPreferenceClick(@NonNull Preference pref) {
            final Context context = getActivity();
            if (context == null) {
                return false;
            }

            switch (pref.getKey()) {
                case KEY_SS_SETTINGS -> {
                    final Intent screensaverSettingsIntent = new Intent(context, ScreensaverSettingsActivity.class);
                    startActivity(screensaverSettingsIntent);
                    return true;
                }
                case KEY_DATE_TIME -> {
                    final Intent dialogIntent = new Intent(Settings.ACTION_DATE_SETTINGS);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialogIntent);
                    return true;
                }
                case KEY_DEFAULT_ALARM_RINGTONE -> {
                    startActivity(RingtonePickerActivity.createAlarmRingtonePickerIntentForSettings(context));
                    return true;
                }
                case KEY_TIMER_RINGTONE -> {
                    startActivity(RingtonePickerActivity.createTimerRingtonePickerIntent(context));
                    return true;
                }
                case KEY_PERMISSION_MESSAGE, KEY_PERMISSIONS_MANAGEMENT -> {
                    final Intent permissionsManagementIntent = new Intent(context, PermissionsManagementActivity.class);
                    startActivity(permissionsManagementIntent);
                    requireActivity().setResult(RESULT_OK);
                    return true;
                }
            }

            return false;
        }

        @Override
        public void onDisplayPreferenceDialog(@NonNull Preference preference) {
            // Only single-selection lists are currently supported.
            final PreferenceDialogFragmentCompat f;
            if (preference instanceof ListPreference) {
                f = ListPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            } else {
                throw new IllegalArgumentException("Unsupported DialogPreference type");
            }
            showDialog(f);
        }

        private void showDialog(PreferenceDialogFragmentCompat fragment) {
            // Don't show dialog if one is already shown.
            if (getParentFragmentManager().findFragmentByTag(PREFERENCE_DIALOG_FRAGMENT_TAG) != null) {
                return;
            }
            // Always set the target fragment, this is required by PreferenceDialogFragment
            // internally.
            fragment.setTargetFragment(this, 0);
            // Don't use getChildFragmentManager(), it causes issues on older platforms when the
            // target fragment is being restored after an orientation change.
            fragment.show(getParentFragmentManager(), PREFERENCE_DIALOG_FRAGMENT_TAG);
        }

        private void hidePreferences() {
            mPermissionMessage = findPreference(KEY_PERMISSION_MESSAGE);
            final Preference vibrations = findPreference(KEY_VIBRATIONS);
            final Preference timerVibrate = findPreference(KEY_TIMER_VIBRATE);
            final boolean hasVibrator = ((Vibrator) Objects.requireNonNull(timerVibrate).getContext()
                    .getSystemService(VIBRATOR_SERVICE)).hasVibrator();

            if (mPermissionMessage != null) {
                mPermissionMessage.setVisible(PermissionsManagementActivity.areEssentialPermissionsNotGranted(requireContext()));
            }

            Objects.requireNonNull(vibrations).setVisible(hasVibrator);

            timerVibrate.setVisible(hasVibrator);
        }

        /**
         * Reconstruct the timezone list.
         */
        private void loadTimeZoneList() {
            final TimeZones timezones = DataModel.getDataModel().getTimeZones();
            final ListPreference homeTimezonePref = findPreference(KEY_HOME_TZ);
            Objects.requireNonNull(homeTimezonePref).setEntryValues(timezones.getTimeZoneIds());
            homeTimezonePref.setEntries(timezones.getTimeZoneNames());
            homeTimezonePref.setSummary(homeTimezonePref.getEntry());
            homeTimezonePref.setOnPreferenceChangeListener(this);
        }

        private void refresh() {
            mPermissionMessage.setVisible(PermissionsManagementActivity.areEssentialPermissionsNotGranted(requireContext()));
            final SpannableStringBuilder builder = new SpannableStringBuilder();
            final String messagePermission = requireContext().getString(R.string.settings_permission_message);
            final Spannable redMessagePermission = new SpannableString(messagePermission);
            if (messagePermission != null) {
                redMessagePermission.setSpan(new ForegroundColorSpan(Color.RED), 0, messagePermission.length(), 0);
                redMessagePermission.setSpan(new StyleSpan(Typeface.BOLD), 0, messagePermission.length(), 0);
            }
            builder.append(redMessagePermission);
            mPermissionMessage.setTitle(builder);
            mPermissionMessage.setOnPreferenceClickListener(this);

            final ListPreference themePref = findPreference(KEY_THEME);
            Objects.requireNonNull(themePref).setSummary(themePref.getEntry());
            themePref.setOnPreferenceChangeListener(this);

            final ListPreference colorPref = findPreference(KEY_ACCENT_COLOR);
            Objects.requireNonNull(colorPref).setSummary(colorPref.getEntry());
            colorPref.setOnPreferenceChangeListener(this);

            final ListPreference amoledModePref = findPreference(KEY_DARK_MODE);
            Objects.requireNonNull(amoledModePref).setSummary(amoledModePref.getEntry());
            amoledModePref.setOnPreferenceChangeListener(this);

            final SwitchPreferenceCompat cardBackgroundPref = findPreference(KEY_CARD_BACKGROUND);
            Objects.requireNonNull(cardBackgroundPref).setOnPreferenceChangeListener(this);

            final SwitchPreferenceCompat cardBackgroundBorderPref = findPreference(KEY_CARD_BACKGROUND_BORDER);
            Objects.requireNonNull(cardBackgroundBorderPref).setOnPreferenceChangeListener(this);

            final SwitchPreferenceCompat vibrationsPref = findPreference(KEY_VIBRATIONS);
            Objects.requireNonNull(vibrationsPref).setOnPreferenceChangeListener(this);

            final ListPreference autoSilencePref = findPreference(KEY_AUTO_SILENCE);
            String delay = Objects.requireNonNull(autoSilencePref).getValue();
            updateAutoSnoozeSummary(autoSilencePref, delay);
            autoSilencePref.setOnPreferenceChangeListener(this);

            final ListPreference clockStylePref = findPreference(KEY_CLOCK_STYLE);
            Objects.requireNonNull(clockStylePref).setSummary(clockStylePref.getEntry());
            clockStylePref.setOnPreferenceChangeListener(this);

            final ListPreference volumeButtonsPref = findPreference(KEY_VOLUME_BUTTONS);
            Objects.requireNonNull(volumeButtonsPref).setSummary(volumeButtonsPref.getEntry());
            volumeButtonsPref.setOnPreferenceChangeListener(this);

            final ListPreference powerButtonsPref = findPreference(KEY_POWER_BUTTONS);
            Objects.requireNonNull(powerButtonsPref).setSummary(powerButtonsPref.getEntry());
            powerButtonsPref.setOnPreferenceChangeListener(this);

            final Preference clockSecondsPref = findPreference(KEY_CLOCK_DISPLAY_SECONDS);
            Objects.requireNonNull(clockSecondsPref).setOnPreferenceChangeListener(this);

            final Preference autoHomeClockPref = findPreference(KEY_AUTO_HOME_CLOCK);
            final boolean autoHomeClockEnabled =
                    ((TwoStatePreference) Objects.requireNonNull(autoHomeClockPref)).isChecked();
            autoHomeClockPref.setOnPreferenceChangeListener(this);

            final ListPreference homeTimezonePref = findPreference(KEY_HOME_TZ);
            Objects.requireNonNull(homeTimezonePref).setEnabled(autoHomeClockEnabled);
            refreshListPreference(homeTimezonePref);

            refreshListPreference(Objects.requireNonNull(findPreference(KEY_ALARM_CRESCENDO)));
            refreshListPreference(Objects.requireNonNull(findPreference(KEY_TIMER_CRESCENDO)));
            refreshListPreference(Objects.requireNonNull(findPreference(KEY_ALARM_SNOOZE)));

            final Preference dateAndTimeSetting = findPreference(KEY_DATE_TIME);
            Objects.requireNonNull(dateAndTimeSetting).setOnPreferenceClickListener(this);

            final Preference screensaverSettings = findPreference(KEY_SS_SETTINGS);
            Objects.requireNonNull(screensaverSettings).setOnPreferenceClickListener(this);

            final ListPreference weekStartPref = findPreference(KEY_WEEK_START);
            // Set the default value programmatically
            final Weekdays.Order weekdayOrder = DataModel.getDataModel().getWeekdayOrder();
            final Integer firstDay = weekdayOrder.getCalendarDays().get(0);
            final String value = String.valueOf(firstDay);
            final int idx = Objects.requireNonNull(weekStartPref).findIndexOfValue(value);
            weekStartPref.setValueIndex(idx);
            weekStartPref.setSummary(weekStartPref.getEntries()[idx]);
            weekStartPref.setOnPreferenceChangeListener(this);

            final Preference alarmRingtonePref = findPreference(KEY_DEFAULT_ALARM_RINGTONE);
            Objects.requireNonNull(alarmRingtonePref).setOnPreferenceClickListener(this);
            alarmRingtonePref.setSummary(DataModel.getDataModel().getAlarmRingtoneTitle());

            final Preference timerRingtonePref = findPreference(KEY_TIMER_RINGTONE);
            Objects.requireNonNull(timerRingtonePref).setOnPreferenceClickListener(this);
            timerRingtonePref.setSummary(DataModel.getDataModel().getTimerRingtoneTitle());

            final ListPreference flipActionPref = findPreference(KEY_FLIP_ACTION);
            setupFlipOrShakeAction(flipActionPref);

            final ListPreference shakeActionPref = findPreference(KEY_SHAKE_ACTION);
            setupFlipOrShakeAction(shakeActionPref);

            final SwitchPreferenceCompat timerVibratePref = findPreference(KEY_TIMER_VIBRATE);
            Objects.requireNonNull(timerVibratePref).setOnPreferenceChangeListener(this);

            final Preference permissionsManagement = findPreference(KEY_PERMISSIONS_MANAGEMENT);
            Objects.requireNonNull(permissionsManagement).setOnPreferenceClickListener(this);
        }

        private void setupFlipOrShakeAction(ListPreference preference) {
            if (preference != null) {
                SensorManager sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
                if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
                    preference.setValue("0");  // Turn it off
                    preference.setVisible(false);
                } else {
                    preference.setSummary(preference.getEntry());
                    preference.setOnPreferenceChangeListener(this);
                }
            }
        }

        private void refreshListPreference(ListPreference preference) {
            preference.setSummary(preference.getEntry());
            preference.setOnPreferenceChangeListener(this);
        }

        private void updateAutoSnoozeSummary(ListPreference listPref, String delay) {
            int i = Integer.parseInt(delay);
            if (i == -1) {
                listPref.setSummary(R.string.auto_silence_never);
            } else {
                listPref.setSummary(Utils.getNumberFormattedQuantityString(requireActivity(),
                        R.plurals.auto_silence_summary, i));
            }
        }
    }
}
