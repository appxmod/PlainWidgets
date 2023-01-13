package com.jaredrummler.colorpicker;

//package net.margaritov.preference.colorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CMN;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A preference type that allows a user to choose a time
 *
 * @author Sergey Margaritov
 */
public class ColorPickerPreference
        extends
		Preference
		implements
		Preference.OnPreferenceClickListener,
		ColorPickerListener {
    View mView;
    //ColorPickerDialog mDialog;
    private int mValue = Color.BLACK;
    private float mDensity = 0;
	private ColorPickerListener onColorChangedListener;
	private boolean showingDlg;
	
	public ColorPickerPreference(Context context) {
        super(context);
        init(context, null);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**Method edited by
     * @author Anna Berkovitch
     * added functionality to accept hex string as defaultValue
     * and to properly persist resources reference string, such as @color/someColor
     * previously persisted 0*/
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        int colorInt;
        String mHexDefaultValue = a.getString(index);
        if (mHexDefaultValue != null && mHexDefaultValue.startsWith("#")) {
            colorInt = convertToColorInt(mHexDefaultValue);
        } else {
			colorInt =  a.getColor(index, Color.BLACK);
        }
		return colorInt;
    }
	
	@Override
	public void setDefaultValue(Object defaultValue) {
		super.setDefaultValue(defaultValue);
		if(!isPersistent()){
			onSetInitialValue(defaultValue);
			notifyChanged();
		}
	}
	
	@Override
    protected void onSetInitialValue(Object defaultValue) {
		int value =  defaultValue instanceof Integer?(int)defaultValue:0;
		mValue = getPersistedInt(value);
    }

    private void init(Context context, AttributeSet attrs) {
        mDensity = getContext().getResources().getDisplayMetrics().density;
        setOnPreferenceClickListener(this);
    }

	@Override
	public void onBindViewHolder(PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		mView = holder.itemView;
		//setPreviewColor();
		LinearLayout widgetFrameView = mView.findViewById(android.R.id.widget_frame);
		if (widgetFrameView == null) return;
        if(widgetFrameView.getTag()==null){
			ImageView iView = new ImageView(getContext());
			widgetFrameView.setVisibility(View.VISIBLE);
			widgetFrameView.setPadding(
					widgetFrameView.getPaddingLeft(),
					widgetFrameView.getPaddingTop(),
					(int) (mDensity * 8),
					widgetFrameView.getPaddingBottom()
			);
			// remove already create preview image
			int count = widgetFrameView.getChildCount();
			if (count > 0) {
				widgetFrameView.removeViews(0, count);
			}
			widgetFrameView.addView(iView);
			widgetFrameView.setMinimumWidth(0);
			iView.setBackgroundDrawable(new AlphaPatternDrawable((int) (5 * mDensity)));
			iView.setImageBitmap(getPreviewBitmap(iView.getDrawable()));
			widgetFrameView.setTag(iView);
		} else {
			ImageView iView = (ImageView) widgetFrameView.getTag();
			iView.setImageBitmap(getPreviewBitmap(iView.getDrawable()));
		}
    }
    
    private Bitmap getPreviewBitmap(Drawable drawable) {
        int d = (int) (mDensity * 31); //30dip
        int color = mValue;
        Bitmap bm = drawable instanceof BitmapDrawable?((BitmapDrawable) drawable).getBitmap():Bitmap.createBitmap(d, d, Config.ARGB_8888);
        int w = bm.getWidth();
        int h = bm.getHeight();
        int c;
        for (int i = 0; i < w; i++) {
            for (int j = i; j < h; j++) {
                c = (i <= 1 || j <= 1 || i >= w - 2 || j >= h - 2) ? Color.GRAY : color;
                bm.setPixel(i, j, c);
                if (i != j) {
                    bm.setPixel(j, i, c);
                }
            }
        }
        return bm;
    }

	@Override
	public void onPreviewSelectedColor(ColorPickerDialog dialog, int color) {
		if(onColorChangedListener!=null)
			onColorChangedListener.onPreviewSelectedColor(dialog, color);
	}

	@Override
	public void onDialogDismissed(ColorPickerDialog dialog, int color) {
		showingDlg = false;
		if(onColorChangedListener!=null) onColorChangedListener.onDialogDismissed(dialog, color);
	}

	public void setOnColorChangedListener(ColorPickerListener listener) {
    	if(listener != this)
			onColorChangedListener = listener;
	}

	public boolean onPreferenceClick(Preference preference) {
        showDialog(null);
        return false;
    }

    protected void showDialog(Bundle state) {
		if(showingDlg)
			return;
		showingDlg = true;
		ColorPickerDialog mDialog = ColorPickerDialog.newInstance(null, mValue);
		try {
			mDialog.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "color");
			mDialog.setColorPickerListener(this);
		} catch (Exception e) {
			CMN.Log(e);
		}
	}

    public static String convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }

    /**
     * Method currently used by onGetDefaultValue method to
     * convert hex string provided in android:defaultValue to color integer.
     *
     * @param color
     * @return A string representing the hex value of color,
     * without the alpha value
     * @author Charles Rosaaen
     */
    public static String convertToRGB(int color) {
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + red + green + blue;
    }

    /** For custom purposes. Not used by ColorPickerPreferrence */
    public static int convertToColorInt(String argb) {
        if (!argb.startsWith("#")) {
            argb = "#" + argb;
        }
        return Color.parseColor(argb);
    }

	@Override
	public boolean onColorSelected(ColorPickerDialog dialog, int color, boolean doubleTap) {
		saveValue(color);
		showingDlg = false;
		return true;
	}

	public void saveValue(int color) {
    	if(callChangeListener(color)){
			mValue = color;
			if(isPersistent())
				persistInt(mValue);
			notifyChanged();
		}
	}
}