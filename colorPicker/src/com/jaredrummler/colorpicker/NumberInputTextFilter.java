package com.jaredrummler.colorpicker;

import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.NumberKeyListener;

class NumberInputTextFilter extends NumberKeyListener {
	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		return super.filter(source, start, end, dest, dstart, dend);
	}

	@Override
	protected char[] getAcceptedChars() {
		return com.knziha.filepicker.widget.NumberKicker.DIGIT_CHARACTERS;
	}

	@Override
	public int getInputType() {
		return InputType.TYPE_CLASS_NUMBER;
	}
}
