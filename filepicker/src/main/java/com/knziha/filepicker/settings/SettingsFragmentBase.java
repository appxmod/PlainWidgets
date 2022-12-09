package com.knziha.filepicker.settings;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.preference.CMN;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.knziha.filepicker.R;


public abstract class SettingsFragmentBase extends PreferenceFragmentCompat implements View.OnClickListener ,
												Preference.OnPreferenceChangeListener {
	protected Toolbar navBar;
	protected TextView text1;
	protected int resId=R.string.settings;
	protected ViewGroup preferenceLayout;
	protected boolean bNavBarBelowList;
	protected boolean bNavBarClickAsIcon;
	protected boolean bHasReturnBtn = true;
	protected int mPreferenceId;
	protected int mNavBarHeight;
	protected int mNavBarPaddingTop;
	private final static SparseIntArray lstPos = new SparseIntArray();
	
	public SettingsFragmentBase() {
		mLayoutResId = R.layout.preference_layout;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return false;
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		if(mPreferenceId !=0)addPreferencesFromResource(mPreferenceId);
	}
	
	
	public static Preference init_switch_preference(SettingsFragmentBase preference, String key, Object val, Object dynamicSummary, Object dynamicTitle, Preference pref) {
		Preference perfer = pref!=null?pref:preference.findPreference(key);
		if(perfer != null){
			if(val!=null) perfer.setDefaultValue(val);
			if(dynamicSummary instanceof Integer) perfer.setSummary((int)dynamicSummary);
			else if(dynamicSummary!=null)  perfer.setSummary(String.valueOf(dynamicSummary));
			if(dynamicTitle!=null)  perfer.setTitle(perfer.getTitle()+String.valueOf(dynamicTitle));
			perfer.setOnPreferenceChangeListener(preference);
		}
		return perfer;
	}

	public static Preference init_number_info_preference(SettingsFragmentBase preference, String key, int index, int infoArrayRes, Object dynamicTitle, Preference p) {
		Preference perfer = preference.findPreference(key);
		if(perfer != null){
			((ListPreference)perfer).setValue(String.valueOf(index));
			if(dynamicTitle!=null)  perfer.setTitle(perfer.getTitle()+String.valueOf(dynamicTitle));
			perfer.setOnPreferenceChangeListener(preference);
		}
		return perfer;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (preferenceLayout==null) {
			preferenceLayout = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
			//CMN.recurseLogCascade(v);
			Bundle args = getArguments();
			int title = args==null?0:args.getInt("title", 0);
			if(title!=0) resId=title;
			navBar = preferenceLayout.findViewById(R.id.toolbar);
			text1= navBar.findViewById(R.id.text1);
			text1.setText(resId);
			text1.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);
			
			mList.setPadding(0, 0, 0, 0);
			
			if(GlobalOptions.isDark) {
				navBar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
				navBar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
			} else {
				navBar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
				navBar.getNavigationIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
			}
			
			navBar.setNavigationOnClickListener(this);
			navBar.setOnClickListener(this);
			
			if (bNavBarBelowList || mNavBarHeight>0) {
				FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) navBar.getLayoutParams();
				if(bNavBarBelowList)lp.gravity = Gravity.BOTTOM;
				int h = lp.height;
				if(mNavBarHeight>0)lp.height = h = mNavBarHeight;
				lp = (FrameLayout.LayoutParams) mList.getLayoutParams();
				if (bNavBarBelowList) {
					lp.topMargin = 0;
					lp.bottomMargin = h;
				} else {
					lp.topMargin = h;
				}
				if(mNavBarHeight<GlobalOptions.density*50)
					navBar.getNavigationBtn().setBackground(null);
				navBar.setPadding(0, mNavBarPaddingTop, 0, 0);
			}
			
			if (bRestoreListPos) {
				mList.scrollToPosition(getLastScrolledPos());
			}
		}
		else if(preferenceLayout.getParent()!=null) {
			((ViewGroup)preferenceLayout.getParent()).removeView(preferenceLayout);
		}
		
		mList.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		//mList.setEdgeEffectFactory(androidx.preference.R.drawable.the_header);
		
		return preferenceLayout;
	}
	
	@Override
	public void onClick(View view) {
		if(bHasReturnBtn && (view.getId()==R.id.home||bNavBarClickAsIcon&&view.getId()==R.id.toolbar)){
			if (getParentFragment() instanceof DialogFragment) {
				((DialogFragment)getParentFragment()).dismiss();
			}
			else if (getActivity()!=null) {
				getActivity().finish();
			}
		}
	}
	
	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		//CMN.Log("onViewStateRestored::", lstPos.get(mPreferenceId), mPreferenceId);
		super.onViewStateRestored(savedInstanceState);
		if(mList!=null)
			mList.scrollToPosition(lstPos.get(mPreferenceId));
	}
	
	@Override
	public void onDestroyView() {
		try {
			lstPos.put(mPreferenceId, ((LinearLayoutManager)mList.getLayoutManager()).findFirstVisibleItemPosition());
		} catch (Exception e) {
			if(GlobalOptions.debug)CMN.Log(e);
		}
		//CMN.Log("onDestroyView::", lstPos.get(mPreferenceId), mPreferenceId);
		super.onDestroyView();
	}
}