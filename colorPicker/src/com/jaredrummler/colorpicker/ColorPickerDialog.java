/*
 * Copyright (C) 2017 Jared Rummler
 * Copyright (C) 2019 KnIfER
 * Copyright (C) 2020 KnIfER
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

package com.jaredrummler.colorpicker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.DialogFragment;
import androidx.preference.CMN;

import com.jaredrummler.android.colorpicker.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

/**
 * <p>A dialog to pick a color.</p>
 * <p>The {@link Activity activity} that shows this dialog should implement {@link ColorPickerListener}</p>
 * <p>Now optimised to suffice for painting applications. </p>
 * <p>Example usage:</p>
 * <pre>
 *   ColorPickerDialog.newBuilder().show(activity);
 * </pre>
 */
public class ColorPickerDialog extends DialogFragment implements
		ColorPickerView.OnColorChangedListener, TextWatcher, View.OnClickListener, View.OnLongClickListener {
	private static final String ARG_Initial_Color = "init";
	public static int longClickGesture;
	public int longClickGestureSize=3;
	public Object tag;
	View mContentView;
	boolean resetCVLayout;
	int mAlphaLock = 255;
	boolean forceAlphaLock = false;
	/*public static final int LongClickAction_NONE=0;
	public static final int LongClickAction_TRANSFER=1;
	public static final int LongClickAction_COLORSPACE=2;
	public static final int LongClickAction_COPYVAL=3;*/

	private int[] intValsTmp;
	static final int ALPHA_THRESHOLD = 165;

	public Integer previewDedicatedColor;
	private WeakReference<ColorSpaceDialog> chooseSPFragment;

	/**Material design colors used as the default color presets*/
	public static final int[] MATERIAL_COLORS = {
			0xFFF44336, // RED 500
			0xFFE91E63, // PINK 500
			0xFFFF2C93, // LIGHT PINK 500
			0xFF9C27B0, // PURPLE 500
			0xFF673AB7, // DEEP PURPLE 500
			0xFF3F51B5, // INDIGO 500
			0xFF2196F3, // BLUE 500
			0xFF03A9F4, // LIGHT BLUE 500
			0xFF00BCD4, // CYAN 500
			0xFF009688, // TEAL 500
			0xFF4CAF50, // GREEN 500
			0xFF8BC34A, // LIGHT GREEN 500
			0xFFCDDC39, // LIME 500
			0xFFFFEB3B, // YELLOW 500
			0xFFFFC107, // AMBER 500
			0xFFFF9800, // ORANGE 500
			0xFF795548, // BROWN 500
			0xFF607D8B, // BLUE GREY 500
			0xFF9E9E9E, // GREY 500
	};

	final ArrayList<Integer> colors = new ArrayList<>();
	int previewingColor;
	int selectedPosition;
	int oldColor;
	int saberColor=-1;
	private boolean lastClicked;
	private long lastClickTime;
	private int collapseOffset;
	private int collapsePosition;
	private boolean isDirty;
	private int oldOrientation;
	
	public static ColorPickerDialog newInstance(Context context, int initialColor) {
		ColorPickerDialog dialog = new ColorPickerDialog();
		Bundle args = new Bundle();
		args.putInt(ARG_Initial_Color, dialog.initialColor = initialColor);
		dialog.setArguments(args);
		dialog.createView(context);
		return dialog;
	}

	ColorPickerListener listener;
	OnDialogCreatedListener dialogCreateListener;
	boolean bEnabled = true;
	public ViewGroup rootView;
	ViewGroup views_holder;
	public ViewGroup views_holder0;
	//View picker_view;

	// -- PRESETS --------------------------
	ColorPaletteAdapter adapter;
	LinearLayout shadesLayout;
	TextView transparencyPercText;

	// -- CUSTOM ---------------------------
	ColorPickerView colorViewPicker;
	ColorPanelView newColorPanel, oldColorPanel;
	WitEditText hexEditText;
	private boolean fromEditText;
	private int initialColor = -1;
	private int initialPosition = -1;
	private int rawColor = -1;

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(oldOrientation != newConfig.orientation){
			if(pickerview!=null && pickerview.getVisibility()==View.VISIBLE){
				long time = System.currentTimeMillis();

				LayoutParams lp = colorViewPicker.getLayoutParams();
				DisplayMetrics dm = rootView.getContext().getResources().getDisplayMetrics();
				lp.height = dm.widthPixels <= dm.heightPixels?dm.widthPixels :(int)(dm.heightPixels- (32+40) * dm.density);
				colorViewPicker.setLayoutParams(lp);//正方形才舒心嘛！
				if(dm.heightPixels>getResources().getDimension(R.dimen.cpv_column_widthX2)+dm.widthPixels){
					CollapseGridView();
				}else if(!gridView.isExpanded()){//expand
					ExpandGridView(time);
				}
				lastClickTime=time;
			}
			oldOrientation=newConfig.orientation;
		}
	}

	public void dismissSelected() {
		if(oldColor!=colors_get(selectedPosition))
			putColor(selectedPosition, colors_get(selectedPosition));
		checkPalette();
		super.dismiss();
	}

	private void checkPalette() {
		if(isDirty){
			Settings.getInstance(getActivity())
					.setAllPresets(presets.toString());
			isDirty=false;
		}
	}
	
	public LinearLayout bottomBar;
	public Button btnConfirm, btnCustomize;
	
	public void createView(Context context) {
		if(context==null)
			return;
		LayoutInflater factory = LayoutInflater.from(context);
		rootView = (ViewGroup) factory.inflate(R.layout.cpv_dialog_presets, null);
		shadesLayout = rootView.findViewById(R.id.shades_layout);
		gridView = rootView.findViewById(R.id.gridView);
		if(GlobalOptions.isDark){
			rootView.getChildAt(1).setBackgroundColor(Color.BLACK);
			rootView.getChildAt(2).setBackgroundColor(Color.BLACK);
			rootView.findViewById(R.id.shades_divider).setBackgroundColor(Color.WHITE);
		}
		
		adapter = new ColorPaletteAdapter();
		
		gridView.setAdapter(adapter);
		
		bottomBar = (LinearLayout) VU.findViewById(rootView, R.id.bottom);
		btnCustomize = VU.craftBtn(context, R.id.custom, R.string.cpv_custom, bottomBar);
		VU.craftLinearPadding(context, bottomBar);
		btnConfirm = VU.craftBtn(context, R.id.confirm_button, R.string.confirm,  bottomBar);
		
		
		views_holder0 = rootView.findViewById(R.id.views_holder0);
		views_holder = views_holder0.findViewById(R.id.views_holder);
		views_holder.setLayerType(View.LAYER_TYPE_NONE, null);
		rootView.findViewById(R.id.clicker).setOnClickListener(this);
		btnConfirm.setOnClickListener(this);
		
		//自定义颜色
		btnCustomize.setOnClickListener(this);
		btnCustomize.setOnLongClickListener(this);
		
		if (colors.size()==0) {
			loadPresets();//xxxyyy
		}
		
		createColorShades(factory, colors_get(selectedPosition));
	}
	
	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if(initialColor==-1 && getArguments()!=null && getArguments().containsKey(ARG_Initial_Color))
			initialColor  = getArguments().getInt(ARG_Initial_Color);
		
		if (rootView==null) {
			createView(getActivity());
		} else
			VU.removeIfParentBeOrNotBe(rootView, null, false);
		if(mContentView!=null) {
			if(mContentView.getParent()!=null)
				((ViewGroup)mContentView.getParent()).removeView(mContentView);
			rootView.addView(mContentView, 1);
			if (resetCVLayout) {
				mContentView.setLayoutParams(rootView.getChildAt(0).getLayoutParams());
			}
		}
		
		AlertDialog dialog = new AlertDialog.Builder(getContext(), Build.VERSION.SDK_INT >= 24 ? R.style.dialog_fullscreen : R.style.dialog_fullscreennonono)
				.setView(rootView)
				.setAutoBG(false)
				.create();
		
		Window win = dialog.getWindow();
		win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		win.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,  WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		//win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		return dialog;
	}
	
	private void ExpandGridView(long time) {
		gridView.setExpanded(true);//because I cannot handle gridview scrolling in a scrollview
		if(time-lastClickTime>300){
			collapsePosition=gridView.getFirstVisiblePosition();
			if(gridView.getChildCount()>0)collapseOffset=gridView.getChildAt(0).getTop();
			else collapseOffset=0;
		}
	}

	private void CollapseGridView() {
		if(gridView.isExpanded()) {//collapse
			gridView.setExpanded(false);
			gridView.post(() -> {
				//CMNF.Log(collapsePosition,"collapsePositioncollapsePosition", gridView.getChildCount());
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					gridView.setSelectionFromTop(0, (int) (-collapsePosition / 6 * (getResources().getDimension(R.dimen.cpv_column_width) + gridView.getVerticalSpacing()) + collapseOffset));
				} else {
					gridView.scrollTo(0, 0);
					gridView.setSelection(collapsePosition);
					gridView.scrollBy(0, -collapseOffset);
				}
			});
		}
	}

	@Override public void onStart() {
		super.onStart();
		AlertDialog dialog = (AlertDialog) getDialog();
		if(dialog==null) return;
		Window win = dialog.getWindow();
		if(win==null) return;
		win.setWindowAnimations(R.style.dialog_animation);
		win.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		//if(bFullScreen) win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|Window.FEATURE_NO_TITLE, WindowManager.LayoutParams.FLAG_FULLSCREEN|Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			win.setNavigationBarColor(0);
			win.setStatusBarColor(0);
		}

		//win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		//win.getDecorView().findViewById(R.id.parentPanel).setLayoutParams(win.getDecorView().findViewById(R.id.parentPanel).getLayoutParams());
		//win.getDecorView().setPadding(0, 0, 0, 0);
	}

	/** 取消才会被调用 */
	@Override public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if(listener !=null) {
			listener.onDialogDismissed(this, rawColor);
		}
		checkPalette();
	}

	@Override public void onSaveInstanceState(Bundle outState) {
		//CMNF.Log("ColorPickerDialog - onSaveInstanceState", selectedPosition);
		//outState.putInt(ARG_POSITION, selectedPosition);
		if(initialPosition==-1 && colors.size()>0)
			getArguments().putInt(ARG_Initial_Color, colors_get(0));
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
	}

	
	public void setColorPickerListener(ColorPickerListener value) {
		this.listener = value;
	}
	
	public void setOnDialogCreatedListener(OnDialogCreatedListener value) {
		this.dialogCreateListener = value;
	}

	// region Custom Picker
	//xxx
	boolean isPickerAttached=false;
	View pickerview;
	View createPickerView() {
		View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.cpv_dialog_color_picker, views_holder, false);
		colorViewPicker = contentView.findViewById(R.id.cpv_color_picker_view);
		oldColorPanel = contentView.findViewById(R.id.cpv_color_panel_old);
		newColorPanel = contentView.findViewById(R.id.cpv_color_panel_new);
		oldColorPanel.setOnClickListener(this);
		newColorPanel.setOnClickListener(this);
		ImageView arrowRight = contentView.findViewById(R.id.cpv_arrow_right);
		contentView.findViewById(R.id.cpv_hex_cap).setOnClickListener(this);
		hexEditText = contentView.findViewById(R.id.cpv_hex);

		try {
			final TypedValue value = new TypedValue();
			TypedArray typedArray =
					getActivity().obtainStyledAttributes(value.data, new int[]{android.R.attr.textColorPrimary});
			int arrowColor = typedArray.getColor(0, Color.BLACK);
			typedArray.recycle();
			arrowRight.setColorFilter(arrowColor);
		} catch (Exception ignored) {  }

		colorViewPicker.setAlphaSliderVisible(true);
		oldColorPanel.setColor(colors_get(selectedPosition));
		if(saberColor!=-1) {
			colorViewPicker.setColor(saberColor, true);
			newColorPanel.setColor(saberColor);
			setHex(saberColor);
		}else {
			colorViewPicker.setColor(colors_get(selectedPosition), true);
			newColorPanel.setColor(colors_get(selectedPosition));
			setHex(colors_get(selectedPosition));
		}

		colorViewPicker.setOnColorChangedListener(this);

		if(GlobalOptions.isDark) hexEditText.setTextColor(Color.WHITE);

		hexEditText.setContextTextChangeListener(() -> {
			hexEditText.removeTextChangedListener(ColorPickerDialog.this);
			hexEditText.addTextChangedListener(ColorPickerDialog.this);
		});

		hexEditText.setOnKeyListener((view, i, keyEvent) -> {
			hexEditText.removeTextChangedListener(ColorPickerDialog.this);
			hexEditText.addTextChangedListener(ColorPickerDialog.this);
			return false;
		});

		hexEditText.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) {
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(hexEditText, InputMethodManager.SHOW_IMPLICIT);
			}
		});

		return contentView;
	}

	@Override
	public void onColorChanged(int newColor) {
		lastClicked=true;
		newColorPanel.setColor(newColor);
		colors.set(selectedPosition,newColor);
		InvalideOneViewAt(selectedPosition, false);
		if (!fromEditText) {
			setHex(newColor);
			//closeHexInput();
		}
		previewColor(newColor);
	}

	private void InvalideOneViewAt(int selectedPosition, boolean fromAdapter) {
		int fvp = gridView.getFirstVisiblePosition();
		int idx = selectedPosition-fvp;
		if(idx>=0){
			View v = gridView.getChildAt(idx);
			if(v!=null && v.getTag() instanceof ViewHolder){
				tintViewsAt((ViewHolder) v.getTag(), selectedPosition, fromAdapter);
			}
		}
	}

	void tintViewsAt(ViewHolder holder, int position, boolean fromAdapter) {
		holder.itemView.setTag(R.id.home, position);
		int color = colors_get(position);
		int alpha = Color.alpha(color);
		if(forceAlphaLock || alpha > mAlphaLock) {
			alpha = mAlphaLock;
			color = (0xFF000000&(alpha<<24)) | (0x00FFFFFF&color);
		}
		//colorPanelView.setSelected(selectedPosition == position);
		holder.colorPanelView.setColor(color);
		if(selectedPosition == position){
			holder.ticker.setImageResource(R.drawable.cpv_preset_checked);
			int targetTicketColor=Color.WHITE;
			int sep = 0x10;
			if(alpha <= sep || alpha <= 0x80 && holder.colorPanelView.getLuminance()>0.8)
				targetTicketColor = 0xFF000000|color;
			if(holder.colorPanelView.getLuminance()>0.65 && (fromAdapter || !lastClicked)){
				targetTicketColor=Color.BLACK;
			}
			holder.ticker.setColorFilter(targetTicketColor, PorterDuff.Mode.SRC_IN);
		}else{
			holder.ticker.setImageResource(0);
		}
	}

	void closeHexInput() {
		if (hexEditText!=null && hexEditText.hasFocus()) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(hexEditText.getWindowToken(), 0);
			hexEditText.clearFocus();
		}
	}

	@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
	@Override public void onTextChanged(CharSequence s, int start, int before, int count) {  }
	@Override public void afterTextChanged(Editable s) {
		fromEditText = true;
		if (hexEditText.isFocused()) {
			int color = parseColorString(s.toString());
			if (color != colorViewPicker.getColor()) {
				colorViewPicker.setColor(color, true);
			}
		}
		fromEditText = false;
	}

	private void setHex(int color) {
		hexEditText.removeTextChangedListener(this);
		hexEditText.setText(String.format("%08X", (color)));
	}

	private int parseColorString(String colorString) throws NumberFormatException {
		int a, r, g, b = 0;
		if (colorString.startsWith("#")) {
			colorString = colorString.substring(1);
		}
		if (colorString.length() == 0) {
			r = 0;
			a = 255;
			g = 0;
		} else if (colorString.length() <= 2) {
			a = 255;
			r = 0;
			b = Integer.parseInt(colorString, 16);
			g = 0;
		} else if (colorString.length() == 3) {
			a = 255;
			r = Integer.parseInt(colorString.substring(0, 1), 16);
			g = Integer.parseInt(colorString.substring(1, 2), 16);
			b = Integer.parseInt(colorString.substring(2, 3), 16);
		} else if (colorString.length() == 4) {
			a = 255;
			r = Integer.parseInt(colorString.substring(0, 2), 16);
			g = r;
			r = 0;
			b = Integer.parseInt(colorString.substring(2, 4), 16);
		} else if (colorString.length() == 5) {
			a = 255;
			r = Integer.parseInt(colorString.substring(0, 1), 16);
			g = Integer.parseInt(colorString.substring(1, 3), 16);
			b = Integer.parseInt(colorString.substring(3, 5), 16);
		} else if (colorString.length() == 6) {
			a = 255;
			r = Integer.parseInt(colorString.substring(0, 2), 16);
			g = Integer.parseInt(colorString.substring(2, 4), 16);
			b = Integer.parseInt(colorString.substring(4, 6), 16);
		} else if (colorString.length() == 7) {
			a = Integer.parseInt(colorString.substring(0, 1), 16);
			r = Integer.parseInt(colorString.substring(1, 3), 16);
			g = Integer.parseInt(colorString.substring(3, 5), 16);
			b = Integer.parseInt(colorString.substring(5, 7), 16);
		} else if (colorString.length() == 8) {
			a = Integer.parseInt(colorString.substring(0, 2), 16);
			r = Integer.parseInt(colorString.substring(2, 4), 16);
			g = Integer.parseInt(colorString.substring(4, 6), 16);
			b = Integer.parseInt(colorString.substring(6, 8), 16);
		} else {
			b = -1;
			g = -1;
			r = -1;
			a = -1;
		}
		return Color.argb(a, r, g, b);
	}

	// -- endregion --

	// region Presets Picker
	//xxx
	NestedGridView gridView;
	JSONArray presets;
	int mPresetIdx=0;

	private void loadPresets() {
		if(initialPosition == -1)
			colors.add(initialColor);

		SwitchToPreset(0);
		selectedPosition=Math.min(selectedPosition, colors.size());
		initialPosition=Math.min(initialPosition, colors.size());

		rawColor=oldColor=colors_get(selectedPosition);
	}

	private void putColor(int position, int integer) {
		if(initialPosition==-1 && position==0) return;
		isDirty=true;
		position=Math.min(position, colors.size());
		if(presets==null) presets = new JSONArray();
		JSONArray target = null;
		try {
			target = (JSONArray) presets.get(mPresetIdx);
		} catch (JSONException e) {  }
		int delta=(initialPosition==-1/*代表初始没有选中，亦即是以具体颜色初始化选择器对话框的。*/)?-1:0;
		if(target==null || target.length()==0 || position-delta>=target.length()) {//初始化容器
			try {int size=colors.size()+delta;
				target = new JSONArray();
				for (int i = 0; i < size; i++) {
					target.put(colors_get(i-delta));
				}
				presets.put(Math.min(mPresetIdx, presets.length()), target);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		try {
			target.put(position+delta, integer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private int colors_get(int i) {
		if(colors.size()>0 && i<colors.size())
			return colors.get(i);
		return 0;
	}

	private void SwitchToPreset(int pos){
		JSONArray target = null;
		try {
			if(presets==null)
				presets = new JSONArray(Settings.getInstance(getActivity())
						.getAllPresets());
			target = (JSONArray) presets.get(pos);
		} catch (Exception e) {}
		if(presets==null) presets=new JSONArray();
		boolean added=false;
		if(target==null) {//占位
			target=new JSONArray();
			try {
				presets.put(Math.min(pos, presets.length()), target);
			} catch (JSONException e) { }
		} else {
			if(target.length()>0){
				colors.ensureCapacity(target.length()+colors.size());
				for (int i = 0; i < target.length(); i++) {
					try {
						colors.add(target.getInt(i));
					} catch (JSONException e) { }
				}
				added=true;
			}
		}
		if(!added){
			colors.ensureCapacity(colors.size()+ MATERIAL_COLORS.length);
			for (int i = 0; i < MATERIAL_COLORS.length; i++)
				colors.add(MATERIAL_COLORS[i]);
		}
		mPresetIdx=Math.min(pos, presets.length());
	}

	void createColorShades(LayoutInflater factory, @ColorInt final int color) {
		final int[] colorShades = getColorShades(color);
		if (shadesLayout.getChildCount() != 0) {
			for (int i = 0; i < shadesLayout.getChildCount(); i++) {
				FrameLayout layout = (FrameLayout) shadesLayout.getChildAt(i);
				final ColorPanelView cpv = (ColorPanelView) layout.getChildAt(0);
				ImageView iv = (ImageView) layout.getChildAt(1);
				cpv.setColor(colorShades[i]);
				cpv.setTag(false);
				iv.setImageDrawable(null);
			}
			return;
		}

		int layoutResId = R.layout.cpv_color_item_square;
		for (int colorShade : colorShades) {
			final View view = factory.inflate(layoutResId, shadesLayout, false);
			final ColorPanelView colorPanelView = view.findViewById(R.id.cpv_color_panel_view);
			colorPanelView.setId(R.id.shaders);
			colorPanelView.setColor(colorShade);
			colorPanelView.setBorderColor(Color.TRANSPARENT);
			colorPanelView.setOnClickListener(this); //shade 子项
			colorPanelView.setOnLongClickListener(this);
			shadesLayout.addView(view);
		}
	}

	private int shadeColor(@ColorInt int color, double percent) {
		String hex = String.format("#%06X", (0xFFFFFF & color));
		long f = Long.parseLong(hex.substring(1), 16);
		double t = percent < 0 ? 0 : 255;
		double p = percent < 0 ? percent * -1 : percent;
		long R = f >> 16;
		long G = f >> 8 & 0x00FF;
		long B = f & 0x0000FF;
		int alpha = Color.alpha(color);
		int red = (int) (Math.round((t - R) * p) + R);
		int green = (int) (Math.round((t - G) * p) + G);
		int blue = (int) (Math.round((t - B) * p) + B);
		return Color.argb(alpha, red, green, blue);
	}

	double[] ColorPattern;
	private int[] getColorShades(@ColorInt int color) {
		if(intValsTmp==null){
			if(GlobalOptions.isLarge)
				ColorPattern = new double[]{0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.333, 0.25, 0.166, -0.125, -0.25, -0.375, -0.5, -0.675, -0.7, -0.775, -0.8, -0.9, -0.99};
			else
				ColorPattern = new double[]{0.9, 0.7, 0.5, 0.333, 0.166, -0.125, -0.25, -0.375, -0.5, -0.675, -0.7, -0.775};
			intValsTmp = new int[ColorPattern.length];
		}
		for (int i = 0; i < ColorPattern.length; i++) {
			intValsTmp[i] = shadeColor(color, ColorPattern[i]);
		}
		return intValsTmp;
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		int percentage = (int) ((double) progress * 100 / 255);
		transparencyPercText.setText(String.format(Locale.ENGLISH, "%d%%", percentage));
		int alpha = 255 - progress;
		// update items in GridView:
		for (int i = 0; i < colors.size(); i++) {
			int color = colors_get(i);
			int red = Color.red(color);
			int green = Color.green(color);
			int blue = Color.blue(color);
			colors.set(i,Color.argb(alpha, red, green, blue));
		}
		adapter.notifyDataSetChanged();
		// update shades:
		for (int i = 0; i < shadesLayout.getChildCount(); i++) {
			FrameLayout layout = (FrameLayout) shadesLayout.getChildAt(i);
			ColorPanelView cpv = layout.findViewById(R.id.cpv_color_panel_view);
			ImageView iv = layout.findViewById(R.id.cpv_color_image_view);
			if (layout.getTag() == null) {
				// save the original border color
				layout.setTag(cpv.getBorderColor());
			}
			int color = cpv.getColor();
			color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
			if (alpha <= ALPHA_THRESHOLD) {
				cpv.setBorderColor(color | 0xFF000000);
			} else {
				cpv.setBorderColor((Integer) layout.getTag());
			}
			if (cpv.getTag() != null && (Boolean) cpv.getTag()) {
				// The alpha changed on the selected shaded color. Update the checkmark color filter.
				if (alpha <= ALPHA_THRESHOLD) {
					iv.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
				} else {
					if (ColorUtils.calculateLuminance(color) >= 0.65) {
						iv.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
					} else {
						iv.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
					}
				}
			}
			cpv.setColor(color);
		}
	}

	//long-click
	@Override
	public boolean onLongClick(View v) {
		int id = v.getId();
		if(id==R.id.color_item){
			ViewHolder holder = (ViewHolder) v.getTag();
			ColorPanelView colorPanelView = holder.colorPanelView;
			colorPanelView.showHint();
			int Pos = (int) v.getTag(R.id.home);
			if(longClickGesture==1){
				int newColor = colors_get(selectedPosition);
				if(previewDedicatedColor!=null){
					newColor=previewDedicatedColor;
					previewDedicatedColor=null;
				}
				colors.set(Pos,newColor);
				colorPanelView.setColor(newColor);
				putColor(Pos, newColor);
			}
			else if(longClickGesture==2){
				showColorSpaceDial(colorPanelView, colors_get(Pos), Pos);
			}
			return true;
		}
		if(id==R.id.shaders){
			ColorPanelView colorPanelView = ((ColorPanelView)v);
			colorPanelView.showHint();
			return true;
		}
		if(id==R.id.custom){
			longClickGesture=(longClickGesture+1)%longClickGestureSize;
			showTopToast(getContext(),"Gesture#"+longClickGesture);
			return true;
		}
		if(id==R.id.color_display){
			showTopToast(getContext(), "Gesture#1 << #"+Integer.toHexString(previewingColor));
			previewDedicatedColor=previewingColor;
			return true;
		}
		return false;
	}

	//click
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (!bEnabled && v!=btnConfirm && id!=R.id.clicker) {
			return; //todo toast
		}
		boolean b1;
		//核心点击事件
		if(id==R.id.color_item){
			closeHexInput();
			lastClicked=false;
			int Pos = (int) v.getTag(R.id.home);
			if(saberColor!=-1) {
				saberColor=-1;
				if(isPickerAttached) {
					colorViewPicker.setColor(colors_get(selectedPosition), false);
					oldColorPanel.setColor(colors_get(selectedPosition));
					newColorPanel.setColor(colors_get(selectedPosition));
				}
			}
			else if (selectedPosition == Pos) {//相等表示确认
				if(listener!=null && !listener.onColorSelected(ColorPickerDialog.this, colors_get(Pos), true))
					return;
				dismissSelected();
				return;
			}

			previewColor(colors_get(Pos));

			if(oldColor!=colors_get(selectedPosition))//离巢之鸟 心印乡颜
				putColor(selectedPosition, colors_get(selectedPosition));

			//listener.onColorSelected(Pos);

			int newColor = colors_get(Pos);
			if(isPickerAttached) {
				colorViewPicker.setColor(newColor, false);
				oldColorPanel.setColor(newColor);
				newColorPanel.setColor(newColor);
				setHex(newColor);
			}
			createColorShades(null, newColor);
			oldColor = colors_get(Pos);
			int oldPos = selectedPosition;
			selectedPosition = Pos;
			InvalideOneViewAt(oldPos, true);
			InvalideOneViewAt(Pos, true);
		}
		//shade 子项
		else if(id==R.id.shaders){
			ColorPanelView colorPanelView = (ColorPanelView) v;
			saberColor = colorPanelView.getColor();
			if (v.getTag() instanceof Boolean && (Boolean) v.getTag()) {//相等表示确认
				if(listener !=null && !listener.onColorSelected(this, colorPanelView.getColor(), true))
					return;
				dismissSelected();
				return; //already selected
			}else {//new selection
				if(isPickerAttached) {
					colorViewPicker.setColor(saberColor, false);
					//oldColorPanel.setColor(saberColor);
					newColorPanel.setColor(saberColor);
				}
				previewColor(saberColor);
			}
			//colors.set(selectedPosition,colorPanelView.getColor());
			for (int i = 0; i < shadesLayout.getChildCount(); i++) {//全部重置
				FrameLayout layout = (FrameLayout) shadesLayout.getChildAt(i);
				ColorPanelView cpv = (ColorPanelView) layout.getChildAt(0);
				ImageView iv = (ImageView) layout.getChildAt(1);
				iv.setImageResource(cpv == v ? R.drawable.cpv_preset_checked : 0);
				if (cpv == v && ColorUtils.calculateLuminance(cpv.getColor()) >= 0.65 ||
						Color.alpha(cpv.getColor()) <= ALPHA_THRESHOLD) {
					iv.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
				} else {
					iv.setColorFilter(null);
				}
				cpv.setTag(cpv == v);
			}
		}
		else if(id==R.id.confirm_button){
			if(listener !=null && !listener.onColorSelected(this, colors_get(selectedPosition), false))
				return;
			dismissSelected();
		}
		else if(id==R.id.clicker){
			dismiss();
		}
		//选择色彩空间
		else if(id==R.id.cpv_hex_cap){
			showColorSpaceDial(null, colors_get(selectedPosition), selectedPosition);
		}
		//自定义颜色
		else if(id==R.id.custom){
			if(pickerview==null){
				pickerview=createPickerView();
				pickerview.setVisibility(View.GONE);
				views_holder.addView(pickerview,0);
			}
			long time = System.currentTimeMillis();
			if(VU.isVisible(pickerview)) {
				VU.setVisible(pickerview, false);
				CollapseGridView();
			}else {
				if(colorViewPicker.getColor()!=colors_get(selectedPosition))
					colorViewPicker.setColor(colors_get(selectedPosition));
				VU.setVisible(pickerview, true);

				LayoutParams lp = colorViewPicker.getLayoutParams();
				DisplayMetrics dm = rootView.getContext().getResources().getDisplayMetrics();
				lp.height = (int)((dm.widthPixels <= dm.heightPixels?dm.widthPixels :dm.heightPixels - (40) * dm.density) - (32) * dm.density);
				colorViewPicker.setLayoutParams(lp);//正方形才舒心嘛！

				if(dm.heightPixels<views_holder0.getHeight()+lp.height){//expand
					ExpandGridView(time);
				}
			}
			lastClickTime=time;
			isPickerAttached=!isPickerAttached;
		}
		//旧色新颜
		else if((b1=id==R.id.cpv_color_panel_old)||id==R.id.cpv_color_panel_new){
			int newColor = b1?oldColor:newColorPanel.getColor();
			colors.set(selectedPosition,newColor);
			InvalideOneViewAt(selectedPosition, false);
			if(isPickerAttached)
				colorViewPicker.setColor(newColor, false);
			setHex(newColor);
		}

	}

	void previewColor(int newColor) {
		previewingColor=newColor;
		if(listener !=null)
			listener.onPreviewSelectedColor(ColorPickerDialog.this, newColor);
	}
	
	public void setInitialColor(int newColor) {
		initialColor = newColor;
		try {
			getArguments().putInt(ARG_Initial_Color, newColor);
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
	
	public void setPreviewColor(int newColor) {
		if (colors.size() > 0) {
			colors.set(0, newColor);
			datasetChanged();
		}
		previewColor(newColor);
	}

//	@Override
//	public void onActivityCreated(Bundle s) {
//		Window win=getDialog()==null?null:getDialog().getWindow();
//		if(win!=null){
//			if(bFullScreen) win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|Window.FEATURE_NO_TITLE, WindowManager.LayoutParams.FLAG_FULLSCREEN|Window.FEATURE_NO_TITLE);
//		}
//		super.onActivityCreated(s);
//	}

	public class ColorPaletteAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return colors.size();
		}

		@Override
		public Object getItem(int position) {
			return colors_get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			final ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder(parent.getContext());
				holder.itemView.setOnClickListener(ColorPickerDialog.this);
				holder.itemView.setOnLongClickListener(ColorPickerDialog.this);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			tintViewsAt(holder, position, true);
			return holder.itemView;
		}
	}

	static final class ViewHolder {
		View itemView;
		ImageView ticker;
		ColorPanelView colorPanelView;
		ViewHolder(Context context) {
			int layoutResId = R.layout.cpv_color_item_circle;
			itemView = View.inflate(context, layoutResId, null);
			colorPanelView = itemView.findViewById(R.id.cpv_color_panel_view);
			ticker = itemView.findViewById(R.id.cpv_color_image_view);
			itemView.setTag(this);
		}
	}

	public void onDedicatedColorChanged(int newColor, ColorPanelView newColorPanel, int Pos) {
		isDirty=true;
		if(newColorPanel!=null)
			newColorPanel.setColor(newColor);
		colors.set(Pos,newColor);
		InvalideOneViewAt(Pos, false);
		if(hexEditText!=null && Pos==selectedPosition){
			colorViewPicker.setColor(newColor);
			if(this.newColorPanel!=null)
				this.newColorPanel.setColor(newColor);
			setHex(newColor);
		}
	}

	public void showColorSpaceDial(ColorPanelView colorPanelView, int color, int position) {
		ColorSpaceDialog chooseDialog;
		if(chooseSPFragment==null || chooseSPFragment.get()==null) {
			chooseSPFragment = new WeakReference<>(chooseDialog = new ColorSpaceDialog());
		}else
			chooseDialog = chooseSPFragment.get();
		chooseDialog.color=color;
		chooseDialog.position=position;
		chooseDialog.colorPanelView=colorPanelView;
		chooseDialog.colorpicker=this;
		chooseDialog.show(getActivity().getSupportFragmentManager(), "PickSpaceDialog");
	}

	public static void showTopToast(Context context, String msg) {
		Toast cheatSheet = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		cheatSheet.setGravity(Gravity.TOP | Gravity.CENTER, 0, (int) (24*context.getResources().getDisplayMetrics().density));
		cheatSheet.show();
	}
	
	public void setContentView(View cv, boolean resetLayout) {
		if (rootView != null) {
			if (mContentView != null) {
				rootView.removeView(mContentView);
			}
			if(cv.getParent()!=null)
				((ViewGroup)cv.getParent()).removeView(mContentView);
			rootView.addView(cv, 1);
			if (resetLayout) {
				cv.setLayoutParams(rootView.getChildAt(0).getLayoutParams());
			}
		}
		resetCVLayout = resetLayout;
		mContentView = cv;
	}
	
	@Nullable
	@Override
	public View getView() {
		return rootView;
	}
	
	public void alphaLock(int val) {
		mAlphaLock = val;
	}
	
	public int alphaLock() {
		return mAlphaLock;
	}
	
	public void forceAlphaLock(boolean val) {
		forceAlphaLock = val;
	}
	
	public void datasetChanged() {
		if (colors.size() > 0 && adapter!=null) {
			adapter.notifyDataSetChanged();
		}
	}
	
	public int getPreviewingColor() {
		return previewingColor;
	}
	
	public View getContentView() {
		return mContentView;
	}
	
	public void setEnabled(boolean enabled) {
		if (bEnabled != enabled) {
			bEnabled = enabled;
			if (rootView != null) {
				views_holder.setAlpha(enabled ? 1 : 0.1f);
				if (VU.isVisibleV2(pickerview)) {
					VU.setVisible(pickerview, false);
				}
			}
		}
	}
}
