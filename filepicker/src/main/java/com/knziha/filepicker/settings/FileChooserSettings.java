package com.knziha.filepicker.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.knziha.filepicker.R;

import java.io.File;

import static com.knziha.filepicker.model.GlideCacheModule.DEFAULT_GLIDE_PATH;

public class FileChooserSettings extends SettingsFragmentBase implements Preference.OnPreferenceClickListener {
	public final static int id=3;
	final FilePickerOptions opt;
	
	public FileChooserSettings(FilePickerOptions opt) {
		this.opt = opt;
	}
	
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init_switch_preference(this, "use_ffmpeg_for_thumbs", opt.getOpt(FilePickerOptions.FilePickerOption.bFFMRThumbnails, true, 0)==1, null, null, null);
		init_switch_preference(this, "auto_delete_thumbs", opt.getOpt(FilePickerOptions.FilePickerOption.bLRU, true, 0)==1, null, null, null);
		findPreference("cache_p").setDefaultValue(DEFAULT_GLIDE_PATH);
		findPreference("clear_cache").setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		switch (preference.getKey()){
			case "clear_cache":
				Context context = getContext();
				AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
				builder2.setTitle("确认要删除缩略图缓存？")
						.setPositiveButton(R.string.delete, (dialog, which) -> {
							SharedPreferences defaultReader = PreferenceManager.getDefaultSharedPreferences(context);
							File path = new File(defaultReader.getString("cache_p", DEFAULT_GLIDE_PATH==null?context.getExternalCacheDir().getAbsolutePath():DEFAULT_GLIDE_PATH));
							int cc = 0;
							if(path.isDirectory()){
								for(File fI:path.listFiles())
									if(!fI.getName().equals("journal") && fI.delete())
										cc++;
							}
							Toast.makeText(getContext(), "已删除"+cc+"项", Toast.LENGTH_LONG).show();
						});
				AlertDialog dTmp = builder2.create();
				dTmp.show();
				((TextView)dTmp.findViewById(R.id.alertTitle)).setSingleLine(false);
			break;
		}
		return false;
	}

	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (preference.getKey()){
			case "use_ffmpeg_for_thumbs":
				opt.getOpt(FilePickerOptions.FilePickerOption.bFFMRThumbnails, false, (Boolean)newValue==true?1:0);
			break;
			case "auto_delete_thumbs":
				opt.getOpt(FilePickerOptions.FilePickerOption.bLRU, false, (Boolean)newValue==true?1:0);
			break;
		}
		return true;
	}

	//加载
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.chooserpreferences);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		//toolbar.setOnMenuItemClickListener(this);
		//toolbar.inflateMenu(R.menu.menu_preferences);
		return v;
	}

	@Override
	public void onClick(View view) {
		if(view.getId()== R.id.home){
			getActivity().finish();
		}
	}
}