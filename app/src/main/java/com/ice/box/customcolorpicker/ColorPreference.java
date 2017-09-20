/*
 * Copyright (C) 2017 JRummy Apps Inc.
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

package com.ice.box.customcolorpicker;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.Preference;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.ice.box.R;
import com.ice.box.helpers.TweaksHelper;

import static com.ice.box.helpers.Constants.battery_bar_color;
import static com.ice.box.helpers.Constants.tweaks_airplane_mode_color;
import static com.ice.box.helpers.Constants.tweaks_battery_color;
import static com.ice.box.helpers.Constants.tweaks_header_icon_color;
import static com.ice.box.helpers.Constants.tweaks_header_text_color;
import static com.ice.box.helpers.Constants.tweaks_mobile_roaming_color;
import static com.ice.box.helpers.Constants.tweaks_mobile_signal_color;
import static com.ice.box.helpers.Constants.tweaks_mobile_type_color;
import static com.ice.box.helpers.Constants.tweaks_navbar_icon_color;
import static com.ice.box.helpers.Constants.tweaks_notif_footer_background_color;
import static com.ice.box.helpers.Constants.tweaks_notif_footer_text_color;
import static com.ice.box.helpers.Constants.tweaks_notification_background_color;
import static com.ice.box.helpers.Constants.tweaks_notification_icon_color;
import static com.ice.box.helpers.Constants.tweaks_notification_summary_text_color;
import static com.ice.box.helpers.Constants.tweaks_notification_title_text_color;
import static com.ice.box.helpers.Constants.tweaks_qs_background_color;
import static com.ice.box.helpers.Constants.tweaks_qs_divider_color;
import static com.ice.box.helpers.Constants.tweaks_qs_drag_handle_background_color;
import static com.ice.box.helpers.Constants.tweaks_qs_drag_handle_icon_color;
import static com.ice.box.helpers.Constants.tweaks_qs_icon_off_color;
import static com.ice.box.helpers.Constants.tweaks_qs_icon_on_color;
import static com.ice.box.helpers.Constants.tweaks_qs_slider_color;
import static com.ice.box.helpers.Constants.tweaks_qs_text_color;
import static com.ice.box.helpers.Constants.tweaks_statusbar_battery_percent_color;
import static com.ice.box.helpers.Constants.tweaks_statusbar_clock_color;
import static com.ice.box.helpers.Constants.tweaks_statusbar_icon_color;
import static com.ice.box.helpers.Constants.tweaks_wifi_signal_color;


/**
 * A Preference to select a color
 */
public class ColorPreference extends Preference implements ColorPickerDialogListener {

    private static final int SIZE_NORMAL = 0;
    private static final int SIZE_LARGE = 1;

    private OnShowDialogListener onShowDialogListener;
    private int color = Color.BLACK;
    private boolean showDialog;
    @ColorPickerDialog.DialogType
    private int dialogType;
    private int colorShape;
    private boolean allowPresets;
    private boolean allowCustom;
    private boolean showAlphaSlider;
    private boolean showColorShades;
    private int previewSize;
    private int[] presets;
    private int dialogTitle;
    //Dirty workaround to save colors to Setttings System
    int newColor = 0;
    String systemKey = "";
    TweaksHelper tweaksHelper;


    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ColorPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setPersistent(false);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPreference);
        showDialog = a.getBoolean(R.styleable.ColorPreference_cpv_showDialog, true);
        //noinspection WrongConstant
        dialogType = a.getInt(R.styleable.ColorPreference_cpv_dialogType, ColorPickerDialog.TYPE_PRESETS);
        colorShape = a.getInt(R.styleable.ColorPreference_cpv_colorShape, ColorShape.CIRCLE);
        allowPresets = a.getBoolean(R.styleable.ColorPreference_cpv_allowPresets, true);
        allowCustom = a.getBoolean(R.styleable.ColorPreference_cpv_allowCustom, true);
        showAlphaSlider = a.getBoolean(R.styleable.ColorPreference_cpv_showAlphaSlider, false);
        showColorShades = a.getBoolean(R.styleable.ColorPreference_cpv_showColorShades, true);
        previewSize = a.getInt(R.styleable.ColorPreference_cpv_previewSize, SIZE_NORMAL);
        final int presetsResId = a.getResourceId(R.styleable.ColorPreference_cpv_colorPresets, 0);
        dialogTitle = a.getResourceId(R.styleable.ColorPreference_cpv_dialogTitle, R.string.cpv_default_title);
        if (presetsResId != 0) {
            presets = getContext().getResources().getIntArray(presetsResId);
        } else {
            presets = ColorPickerDialog.MATERIAL_COLORS;
        }
        if (colorShape == ColorShape.CIRCLE) {
            setWidgetLayoutResource(
                    previewSize == SIZE_LARGE ? R.layout.cpv_preference_circle_large : R.layout.cpv_preference_circle);
        } else {
            setWidgetLayoutResource(
                    previewSize == SIZE_LARGE ? R.layout.cpv_preference_square_large : R.layout.cpv_preference_square
            );
        }
        a.recycle();
    }

    @Override
    protected void onClick() {
        super.onClick();
        if (onShowDialogListener != null) {
            onShowDialogListener.onShowColorPickerDialog((String) getTitle(), color);
        } else if (showDialog) {
            ColorPickerDialog dialog = ColorPickerDialog.newBuilder()
                    .setDialogType(dialogType)
                    .setDialogTitle(dialogTitle)
                    .setColorShape(colorShape)
                    .setPresets(presets)
                    .setAllowPresets(allowPresets)
                    .setAllowCustom(allowCustom)
                    .setShowAlphaSlider(showAlphaSlider)
                    .setShowColorShades(showColorShades)
                    .setColor(color)
                    .create();
            dialog.setColorPickerDialogListener(ColorPreference.this);
            Activity activity = (Activity) getContext();
            dialog.show(activity.getFragmentManager(), getFragmentTag());
        }
    }

    @Override
    protected void onAttachedToActivity() {
        super.onAttachedToActivity();

        if (showDialog) {
            Activity activity = (Activity) getContext();
            ColorPickerDialog fragment =
                    (ColorPickerDialog) activity.getFragmentManager().findFragmentByTag(getFragmentTag());
            if (fragment != null) {
                // re-bind preference to fragment
                fragment.setColorPickerDialogListener(this);
            }
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ColorPanelView preview = (ColorPanelView) view.findViewById(R.id.cpv_preference_preview_color_panel);
        if (preview != null) {
            preview.setColor(color);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            color = getPersistedInt(0xFF000000);
        } else {
            color = (Integer) defaultValue;
            persistInt(color);
            if (getKey().equals("tweaks_statusbar_icon_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_statusbar_icon_color", tweaks_statusbar_icon_color);
            }
            if (getKey().equals("tweaks_statusbar_clock_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_statusbar_clock_color", tweaks_statusbar_clock_color);
            }
            if (getKey().equals("tweaks_notification_icon_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_notification_icon_color", tweaks_notification_icon_color);
            }
            if (getKey().equals("tweaks_notification_icon_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_notification_icon_color", tweaks_notification_icon_color);
            }
            if (getKey().equals("tweaks_statusbar_battery_percent_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_statusbar_battery_percent_color", tweaks_statusbar_battery_percent_color);
            }
            if (getKey().equals("tweaks_wifi_signal_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_wifi_signal_color", tweaks_wifi_signal_color);
            }
            if (getKey().equals("tweaks_mobile_signal_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_mobile_signal_color", tweaks_mobile_signal_color);
            }
            if (getKey().equals("tweaks_mobile_type_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_mobile_type_color", tweaks_mobile_type_color);
            }
            if (getKey().equals("tweaks_mobile_roaming_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_mobile_roaming_color", tweaks_mobile_roaming_color);
            }
            if (getKey().equals("tweaks_airplane_mode_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_airplane_mode_color", tweaks_airplane_mode_color);
            }
            if (getKey().equals("tweaks_battery_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_battery_color", tweaks_battery_color);
            }
            if (getKey().equals("tweaks_navbar_icon_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_navbar_icon_color", tweaks_navbar_icon_color);
            }
            if (getKey().equals("tweaks_header_text_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_header_text_color", tweaks_header_text_color);
            }
            if (getKey().equals("tweaks_header_icon_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_header_icon_color", tweaks_header_icon_color);
            }
            if (getKey().equals("tweaks_qs_background_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_qs_background_color", tweaks_qs_background_color);
            }
            if (getKey().equals("tweaks_qs_text_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_qs_text_color", tweaks_qs_text_color);
            }
            if (getKey().equals("tweaks_qs_icon_on_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_qs_icon_on_color", tweaks_qs_icon_on_color);
            }
            if (getKey().equals("tweaks_qs_icon_off_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_qs_icon_off_color", tweaks_qs_icon_off_color);
            }
            if (getKey().equals("tweaks_qs_divider_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_qs_divider_color", tweaks_qs_divider_color);
            }
            if (getKey().equals("tweaks_qs_slider_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_qs_slider_color", tweaks_qs_slider_color);
            }
            if (getKey().equals("tweaks_qs_drag_handle_background_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_qs_drag_handle_background_color", tweaks_qs_drag_handle_background_color);
            }
            if (getKey().equals("tweaks_qs_drag_handle_icon_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_qs_drag_handle_icon_color", tweaks_qs_drag_handle_icon_color);
            }

            if (getKey().equals("tweaks_notification_background_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_notification_background_color", tweaks_notification_background_color);
            }
            if (getKey().equals("tweaks_notif_footer_text_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_notif_footer_text_color", tweaks_notif_footer_text_color);
            }

            if (getKey().equals("tweaks_notif_footer_background_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_notif_footer_background_color", tweaks_notif_footer_background_color);
            }

            if (getKey().equals("tweaks_notification_summary_text_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_notification_summary_text_color", tweaks_notification_summary_text_color);
            }

            if (getKey().equals("tweaks_notification_title_text_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "tweaks_notification_title_text_color", tweaks_notification_title_text_color);
            }
            if (getKey().equals("battery_bar_color")) {
                color = Settings.System.getInt(this.getContext().getContentResolver(), "battery_bar_color", battery_bar_color);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, Color.BLACK);
    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        saveValue(color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        // no-op
    }

    /**
     * Set the new color
     *
     * @param color The newly selected color
     */
    public void saveValue(@ColorInt int color) {
        tweaksHelper = new TweaksHelper(this.getContext());
        this.color = color;
        //commented to avoid setting preference color without having it saved in Settings system
        persistInt(this.color);
        notifyChanged();
        callChangeListener(color);
        //here comes the magic, stores selected color for given key into settings system
        //systemkey is updated by getFragmentTag class
        try {
            Settings.System.putInt(getContext().getContentResolver(), systemKey, this.color);
            if(systemKey.equals("tweaks_qs_text_color")) {
                tweaksHelper.createRebootNotification();
            }
            if(systemKey.equals("tweaks_qs_icon_on_color")) {
                tweaksHelper.createRebootNotification();
            }
            if(systemKey.equals("tweaks_qs_icon_off_color")) {
                tweaksHelper.createRebootNotification();
            }
            if(systemKey.equals("tweaks_qs_divider_color")) {
                tweaksHelper.createRebootNotification();
            }
            if(systemKey.equals("tweaks_qs_slider_color")) {
                tweaksHelper.createRebootNotification();
            }
            if(systemKey.equals("tweaks_notification_background_color")) {
                tweaksHelper.createRebootNotification();
            }
            if(systemKey.equals("tweaks_notification_title_text_color")) {
                tweaksHelper.createRebootNotification();
            }
            if(systemKey.equals("tweaks_notification_summary_text_color")) {
                tweaksHelper.createRebootNotification();
            }
            if(systemKey.equals("tweaks_navbar_icon_color")) {
                tweaksHelper.createRebootNotification();
            }
            //do the above for all preferences that need a reboot
        } catch (Exception e) {
            tweaksHelper.MakeToast(getContext().getResources().getString(R.string.error_write_settings));
        }

        //Log.d("ICEDEBUG", "KEY: " + systemKey + " VALUE: " +  this.color);
    }

    public int exportValue(@ColorInt int color) {
        this.color = color;
        return this.color;
    }

    /**
     * Set the colors shown in the {@link ColorPickerDialog}.
     *
     * @param presets An array of color ints
     */
    public void setPresets(@NonNull int[] presets) {
        this.presets = presets;
    }

    /**
     * Get the colors that will be shown in the {@link ColorPickerDialog}.
     *
     * @return An array of color ints
     */
    public int[] getPresets() {
        return presets;
    }

    /**
     * The listener used for showing the {@link ColorPickerDialog}.
     * Call {@link #saveValue(int)} after the user chooses a color.
     * If this is set then it is up to you to show the dialog.
     *
     * @param listener The listener to show the dialog
     */
    public void setOnShowDialogListener(OnShowDialogListener listener) {
        onShowDialogListener = listener;
    }

    /**
     * The tag used for the {@link ColorPickerDialog}.
     *
     * @return The tag
     */
    public String getFragmentTag() {
        //Log.d("ICEDEBUG", "AWESOME: " + getKey());
        systemKey = getKey();
        return "color_" + getKey();

    }

    public interface OnShowDialogListener {

        void onShowColorPickerDialog(String title, int currentColor);
    }

}
