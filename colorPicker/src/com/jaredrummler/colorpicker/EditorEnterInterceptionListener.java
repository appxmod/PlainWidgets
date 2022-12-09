package com.jaredrummler.colorpicker;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

class EditorEnterInterceptionListener implements TextView.OnEditorActionListener {@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		return actionId==EditorInfo.IME_ACTION_NEXT || event!=null && event.getKeyCode()==KeyEvent.KEYCODE_ENTER;
	}
}
