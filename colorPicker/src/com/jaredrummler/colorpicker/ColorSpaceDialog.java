package com.jaredrummler.colorpicker;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.DialogFragment;
import com.jaredrummler.android.colorpicker.R;

/** A Convenient Color-Space Dial Panel.(CCSDP)<br/>
 * 各种色彩空间<br/>
 * @author KnIfER<br/>
 * licence Apache2.0 */
@SuppressLint("SetTextI18n")
public class ColorSpaceDialog extends DialogFragment
{
	int color;
	int position;
	private float[][] colorVals = new float[4][3];
	private double[] doubleValsTmp = new double[3];
	private ColorDrawable mColorDrawable;

	public ColorPanelView colorPanelView;
	public ColorPickerDialog colorpicker;
	private ViewGroup root;
	ImageView color_view;
	ViewGroup rgb_color_view;
	ViewGroup hsv_color_view;
	ViewGroup lab_color_view;
	ViewGroup xyz_color_view;
	private ViewGroup[] vpArr;
	EditText[] color_editors = new EditText[12];
	private TextWatcher OTC;
	private boolean isDirty;


	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		if(root==null){
			ViewGroup _root = (ViewGroup) inflater.inflate(R.layout.cpv_dialog_color_spaces, container, false);
			color_view = (ImageView) _root.getChildAt(0);
			rgb_color_view = (ViewGroup) _root.getChildAt(1);
			hsv_color_view = (ViewGroup) _root.getChildAt(2);
			lab_color_view = (ViewGroup) _root.getChildAt(3);
			xyz_color_view = (ViewGroup) _root.getChildAt(4);
			((TextView)rgb_color_view.getChildAt(0)).setText("RGB:");
			((TextView)hsv_color_view.getChildAt(0)).setText("HSV:");
			((TextView)lab_color_view.getChildAt(0)).setText("LAB:");
			((TextView)xyz_color_view.getChildAt(0)).setText("XYZ:");
			OTC = new TextWatcher(){
				@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
				@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
				@SuppressLint("ResourceType")
				@Override public void afterTextChanged(Editable s) {
					try {
						String str = s.toString();
						int pointIdx = str.lastIndexOf('.');
						if(pointIdx>0){
							--pointIdx;
							boolean deleted=false;
							while(pointIdx>0 && (pointIdx = str.lastIndexOf('.', pointIdx))>=0){
								if(s.charAt(pointIdx)=='.'){
									deleted = true;
									s.delete(pointIdx, pointIdx+1);
								}
								--pointIdx;
							}
							if(deleted) str = s.toString();
						}
						EditText CurrentFocused = (EditText) getDialog().getWindow().getCurrentFocus();
						//CMN.Log("afterTextChanged", CurrentFocused);
						if(CurrentFocused!=null){
							int currentGroupId = CurrentFocused.getId() / 3;
							float[] valArr = colorVals[currentGroupId];
							int current = CurrentFocused.getId() - currentGroupId * 3;
							valArr[current] = Float.parseFloat(str);
							setColor(rebuildColorBySpace(currentGroupId, valArr), currentGroupId);
						}
					} catch (Exception ignored){  }
				}
			};
			color_view.setImageDrawable(mColorDrawable=new ColorDrawable(0xffffff00));
			color_view.setOnClickListener(v -> {
				if(isDirty) colorpicker.onDedicatedColorChanged(color, colorPanelView, position);
				dismiss();
			});
			int i; int j; ViewGroup vpI; int id;
			vpArr = new ViewGroup[]{rgb_color_view, hsv_color_view, lab_color_view, xyz_color_view};
			InputFilter[] filter = new InputFilter[]{new NumberInputTextFilter()};
			EditorEnterInterceptionListener ea = new EditorEnterInterceptionListener();
			for (j = 0; j < 4; j++) {
				vpI = vpArr[j];
				for (i=0; i < 3; i++) {
					id = i+3*j;
					color_editors[id] =   (EditText) vpI.getChildAt(i+1);
					color_editors[id].setId(id);
					color_editors[id].addTextChangedListener(OTC);
					color_editors[id].setFilters(filter);
					color_editors[id].setOnEditorActionListener(ea);
				}
			}
			root=_root;
		} else if(root.getParent()!=null){
			((ViewGroup)root.getParent()).removeView(root);
		}
		return root;
	}

	private int rebuildColorBySpace(int groupId, float[] valArr) {
		switch (groupId){
			case 0:
			return Color.argb(255, (int)valArr[0], (int)valArr[1], (int)valArr[2]);
			case 1:
			return ColorUtils.HSLToColor(valArr);
			case 2:
			return ColorUtils.LABToColor(valArr[0], valArr[1], valArr[2]);
			case 3:
			return ColorUtils.XYZToColor(valArr[0], valArr[1], valArr[2]);
		}
		return color;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_FRAME, 0);
	}

	@Override
	public void onResume() {
		super.onResume();
		if(getDialog()!=null) {
			Window window = getDialog().getWindow();
			if(window!= null) {
				window.setLayout(-2,-2);
			}
			setColor(color, -1);
			getDialog().setCanceledOnTouchOutside(true);
			color_view.setOnLongClickListener(colorpicker);
		}
	}

	private void setColor(int color, int currentFocused) {
		//CMNF.Log("setColor");
		mColorDrawable.setColor(color);
		int i; int j; int id;
		colorToRGB(color, colorVals[0]);
		ColorUtils.colorToHSL(color, colorVals[1]);
		ColorUtils.colorToLAB(color, doubleValsTmp);
		transferTmpVals(colorVals[2]);
		ColorUtils.colorToXYZ(color, doubleValsTmp);
		transferTmpVals(colorVals[3]);
		for (j = 0; j < 4; j++) {
			if(j!=currentFocused)
			for (i=0; i < 3; i++) {
				id = i+3*j;
				EditText etColorComponent = color_editors[id];
				etColorComponent.removeTextChangedListener(OTC);
				etColorComponent.setText(String.format("%.2f", colorVals[j][i]));
				etColorComponent.addTextChangedListener(OTC);
			}
		}
		if(this.color!=color){
			isDirty=true;
			this.color=color;
			colorpicker.previewColor(color);
		}
	}

	private void transferTmpVals(float[] colorVal) {
		for (int i = 0; i < 3; i++) {
			colorVal[i] = (float) doubleValsTmp[i];
		}
	}

	private void colorToRGB(int color, float[] colorVal) {
		colorVal[0] = Color.red(color);
		colorVal[1] = Color.green(color);
		colorVal[2] = Color.blue(color);
	}
}