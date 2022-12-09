package com.jaredrummler.colorpicker;

/**
 * Created by liao on 2017/4/13.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 参见:AnkiHelper->com.mmjang.ankihelper.data.settings.java
 */
public class Settings {
    private static Settings settings = null;
	private final static String paletteName = "Palette";
	
	private SharedPreferences preferences;
    private SharedPreferences preferencesLegacy;
	
	private Settings(Context context) {
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if(preferences.getString(paletteName, null)==null)
			preferencesLegacy = context.getSharedPreferences("color_settings", Context.MODE_PRIVATE);
    }

    public static Settings getInstance(Context context) {
        if (settings == null) {
            settings = new Settings(context);
        }
        return settings;
    }

	public String getAllPresets() {
		String ret = preferences.getString(paletteName, null);
		if(ret==null && preferencesLegacy!=null)
			ret = preferencesLegacy.getString("Alets", "[]");
		return ret;
	}

	public void setAllPresets(String data) {
		preferences.edit().putString(paletteName, data).apply();
	}
}