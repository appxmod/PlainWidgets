/*
 * Copyright 2018 The Android Open Source Project
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

package com.knziha.filepicker.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.res.TypedArrayUtils;
import androidx.fragment.app.DialogFragment;
import androidx.preference.DialogPreference;
import androidx.preference.DialogShowablePreference;
import androidx.preference.EditTextPreference;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;

import com.knziha.filepicker.R;

/**
 * A {@link DialogPreference} that shows a {@link EditText} in the dialog.
 *
 * <p>This preference saves a Integer value.
 */
public class IntPreference extends EditTextPreference implements DialogShowablePreference {
	
	public IntPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		mSuffix = attrs.getAttributeValue(null, "suffix");
		bIsNumberEdit = true;
	}

	public IntPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public IntPreference(Context context, AttributeSet attrs) {
		this(context, attrs, TypedArrayUtils.getAttr(context, androidx.preference.R.attr.editTextPreferenceStyle,
				android.R.attr.editTextPreferenceStyle));
	}

	public IntPreference(Context context) {
		this(context, null);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}
	
	@Override
	public void setDefaultValue(Object defaultValue) {
		super.setDefaultValue(defaultValue);
		//if(!isPersistent())
		{
			onSetInitialValue(defaultValue);
		}
	}
	
	@Override
	protected void onSetInitialValue(Object defaultValue) {
		mText = String.valueOf(getPersistedInt(defaultValue instanceof Integer? (Integer) defaultValue :0));
		setSummary(mText);
	}

	@Override
	public void setText(String text) {
		int val;
		try {
			val=TextUtils.getTrimmedLength(text)==0?0:Integer.parseInt(text);
		} catch (NumberFormatException e) {
			Toast.makeText(getContext(), "???????????????", Toast.LENGTH_SHORT).show();
			return;
		}

		final boolean wasBlocking = shouldDisableDependents();

		mText = text;
		setSummary(mText);

		persistInt(val);

		final boolean isBlocking = shouldDisableDependents();
		if (isBlocking != wasBlocking) {
			notifyDependencyChange(isBlocking);
		}

		notifyChanged();
	}

	@Override
	public DialogFragment newInstance() {
		return EditTextPreferenceDialogFragmentCompat.newInstance(getKey());
	}
}
