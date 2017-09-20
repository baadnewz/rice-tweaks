package com.ice.box.fragments;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ice.box.R;
import com.ice.box.helpers.SeekDialog;
import com.ice.box.helpers.TweaksHelper;

import static com.ice.box.helpers.Constants.ScramblePinKey;
import static com.ice.box.helpers.Constants.batteryPercentageFlashKey;


@SuppressWarnings("unused")
public class App extends PreferenceFragment implements
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {
    SeekDialog seekDialog;
    TweaksHelper tweaksHelper;
    Preference filterPref;


    //BatteryPercentageFlash SeekDialog
    private final int batteryPercentageFlashSeekMax = 15;
    private SeekBar batteryPercentageFlashSeek;
    private TextView batteryPercentageFlashValue;
    private Button batteryPercentageFlashPlus;
    private Button batteryPercentageFlashMinus;
    private Button batteryPercentageFlashApply;
    private AlertDialog batteryPercentageFlashDialog;
    private int batteryPercentageFlashProgress;
    //Plus button
    private final Button.OnClickListener batteryPercentageFlashPlusListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // So the value can't go too high
            if (batteryPercentageFlashProgress != batteryPercentageFlashSeekMax) {
                batteryPercentageFlashProgress++;
                batteryPercentageFlashValue.setText(getResources().getString(R.string.battery_percentage_flash_title) + ": " + String.valueOf(batteryPercentageFlashProgress));
                batteryPercentageFlashSeek.setProgress(batteryPercentageFlashProgress);
            }
        }
    };
    //Minus button
    private final Button.OnClickListener batteryPercentageFlashMinusListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // So the value can't go too low
            if (batteryPercentageFlashProgress != 0) {
                batteryPercentageFlashProgress--;
                batteryPercentageFlashValue.setText(getResources().getString(R.string.battery_percentage_flash_title) + ": " + String.valueOf(batteryPercentageFlashProgress));
                batteryPercentageFlashSeek.setProgress(batteryPercentageFlashProgress);
            }
        }
    };
    private final Button.OnClickListener batteryPercentageFlashApplyListener = new Button.OnClickListener() {
        public void onClick(View v) {
            int j = batteryPercentageFlashProgress;
            try {
                Settings.System.putInt(getContext().getContentResolver(),
                        batteryPercentageFlashKey, j);
                tweaksHelper.killPackage("com.sec.android.app.camera");
                //RootUtils.runCommand("kill -9 com.sec.android.app.camera");
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
            tweaksHelper.MakeToast(getResources().getString(R.string.battery_percentage_flash_title) + ": " + String.valueOf(batteryPercentageFlashProgress));
            batteryPercentageFlashDialog.dismiss();
            Log.d(this.getClass().getName(), "Successful!" + j);

        }
    };
    //Listener
    private final SeekBar.OnSeekBarChangeListener batteryPercentageFlashSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // This what you do when the slider is changing
            batteryPercentageFlashValue.setText(getResources().getString(R.string.battery_percentage_flash_title) + ": " + String.valueOf(progress));
            batteryPercentageFlashProgress = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    //END BATTERY PERCENTAGE Flash SEEKBAR

    private int mThemeId = R.style.ThemeLight;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_preference);
        tweaksHelper = new TweaksHelper(this.getContext());
        seekDialog = new SeekDialog(this.getContext());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        SwitchPreference checkPref;
        boolean checked;
        mThemeId = sharedPref.getInt("THEMEID", mThemeId);
        if (mThemeId == R.style.ThemeDark) {
            filterPref = findPreference("application");
            filterPref.setLayoutResource(R.layout.application_dark);
        }

        filterPref = findPreference("tweaks_battery_flash_level");
        filterPref.setOnPreferenceClickListener(this);

        filterPref = findPreference("tweaks_lockscreen_rotation");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_lockscreen_rotation");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_lockscreen_rotation", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_lockscreen_guide_text");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_lockscreen_guide_text");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_lockscreen_guide_text", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference("tweaks_quick_unlock");
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference("tweaks_quick_unlock");
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                "tweaks_quick_unlock", 0) == 1);
        checkPref.setChecked(checked);

        filterPref = findPreference(ScramblePinKey);
        filterPref.setOnPreferenceClickListener(this);
        checkPref = (SwitchPreference) findPreference(ScramblePinKey);
        checked = (Settings.System.getInt(this.getContext().getContentResolver(),
                ScramblePinKey, 0) == 1);
        checkPref.setChecked(checked);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("tweaks_lockscreen_rotation")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_lockscreen_rotation",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
                tweaksHelper.createSystemUINotification();
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }

        if (preference.getKey().equals("tweaks_lockscreen_guide_text")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_lockscreen_guide_text",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_quick_unlock")) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        "tweaks_quick_unlock",
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals("tweaks_battery_flash_level")) {
            try {
                batteryPercentageFlashDialog = seekDialog.getSeekDialog(SeekDialog.SeekEnum.eBatterySize);
                batteryPercentageFlashDialog.show();
                // Find all the IDs on the dialog (this has to be done after the dislog has been shown
                // other wise you will crash the app
                batteryPercentageFlashSeek = (SeekBar) batteryPercentageFlashDialog.findViewById(R.id.seekbar1);
                batteryPercentageFlashValue = (TextView) batteryPercentageFlashDialog.findViewById(R.id.seek1Value);
                batteryPercentageFlashMinus = (Button) batteryPercentageFlashDialog.findViewById(R.id.minus1);
                batteryPercentageFlashPlus = (Button) batteryPercentageFlashDialog.findViewById(R.id.add1);
                batteryPercentageFlashApply = (Button) batteryPercentageFlashDialog.findViewById(R.id.apply);
                // Set the maximum the slider can go up to
                batteryPercentageFlashSeek.setMax(batteryPercentageFlashSeekMax);
                // Get the current value
                batteryPercentageFlashProgress = (Settings.System.getInt(this.getContext().getContentResolver(),
                        batteryPercentageFlashKey, 5));
                // Set the value to the seek bar
                batteryPercentageFlashSeek.setProgress(batteryPercentageFlashProgress);
                // Set the text above the seek bar
                batteryPercentageFlashValue.setText(getResources().getString(R.string.battery_percentage_flash_title) + ": " + String.valueOf(batteryPercentageFlashProgress));
                //Set the Seek bar listener
                batteryPercentageFlashSeek.setOnSeekBarChangeListener(batteryPercentageFlashSeekListener);
                //Set the button listeners
                batteryPercentageFlashPlus.setOnClickListener(batteryPercentageFlashPlusListener);
                batteryPercentageFlashMinus.setOnClickListener(batteryPercentageFlashMinusListener);
                batteryPercentageFlashApply.setOnClickListener(batteryPercentageFlashApplyListener);
                return true;
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        if (preference.getKey().equals(ScramblePinKey)) {
            boolean checked = ((SwitchPreference) preference).isChecked();
            int value = (checked ? 1 : 0);
            try {
                Settings.System.putInt(
                        this.getContext().getContentResolver(),
                        ScramblePinKey,
                        value);
                Log.d(this.getClass().getName(), "Successful!");
            } catch (Exception e) {
                tweaksHelper.MakeToast(getResources().getString(R.string.error_write_settings));
            }
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}